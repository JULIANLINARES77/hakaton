package uniminuto.campusreporta.service;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uniminuto.campusreporta.dto.CambioEstadoRequestDTO;
import uniminuto.campusreporta.dto.HistorialResponseDTO;
import uniminuto.campusreporta.dto.IndicadoresDTO;
import uniminuto.campusreporta.dto.ReporteRequestDTO;
import uniminuto.campusreporta.dto.ReporteResponseDTO;
import uniminuto.campusreporta.entity.EstadoReporte;
import uniminuto.campusreporta.entity.HistorialEstado;
import uniminuto.campusreporta.entity.Reporte;
import uniminuto.campusreporta.entity.RolUsuario;
import uniminuto.campusreporta.entity.Ubicacion;
import uniminuto.campusreporta.entity.Usuario;
import uniminuto.campusreporta.exception.BusinessException;
import uniminuto.campusreporta.exception.ResourceNotFoundException;
import uniminuto.campusreporta.mapper.HistorialEstadoMapper;
import uniminuto.campusreporta.mapper.ReporteMapper;
import uniminuto.campusreporta.repository.EstadoReporteCount;
import uniminuto.campusreporta.repository.HistorialEstadoRepository;
import uniminuto.campusreporta.repository.ReporteRepository;
import uniminuto.campusreporta.repository.UbicacionRepository;
import uniminuto.campusreporta.repository.UsuarioRepository;

@Service
@Transactional(readOnly = true)
public class ReporteServiceImpl implements ReporteService {
    private static final ZoneId ZONA_CAMPUS = ZoneId.of("America/Bogota");
    private static final int LIMITE_RECIENTES = 5;
    private static final OffsetDateTime FECHA_INICIAL = OffsetDateTime.parse("0001-01-01T00:00:00Z");
    private static final OffsetDateTime FECHA_FINAL = OffsetDateTime.parse("9999-12-31T23:59:59Z");

    private final ReporteRepository reporteRepository;
    private final UsuarioRepository usuarioRepository;
    private final UbicacionRepository ubicacionRepository;
    private final HistorialEstadoRepository historialRepository;
    private final ReporteMapper reporteMapper;
    private final HistorialEstadoMapper historialMapper;

    public ReporteServiceImpl(
            ReporteRepository reporteRepository,
            UsuarioRepository usuarioRepository,
            UbicacionRepository ubicacionRepository,
            HistorialEstadoRepository historialRepository,
            ReporteMapper reporteMapper,
            HistorialEstadoMapper historialMapper) {
        this.reporteRepository = reporteRepository;
        this.usuarioRepository = usuarioRepository;
        this.ubicacionRepository = ubicacionRepository;
        this.historialRepository = historialRepository;
        this.reporteMapper = reporteMapper;
        this.historialMapper = historialMapper;
    }

    @Override
    @Transactional
    public ReporteResponseDTO crear(ReporteRequestDTO request) {
        Usuario reportante = buscarUsuario(request.usuarioReportaId());
        Ubicacion ubicacion = buscarUbicacion(request.ubicacionId());

        Reporte reporte = new Reporte();
        reporte.setDescripcionDano(request.descripcionDano().trim());
        reporte.setUbicacion(ubicacion);
        reporte.setUsuarioReporta(reportante);
        reporte.setEstado(EstadoReporte.PENDIENTE);
        reporte = reporteRepository.saveAndFlush(reporte);

        HistorialEstado inicio = new HistorialEstado();
        inicio.setReporte(reporte);
        inicio.setEstadoAnterior(null);
        inicio.setEstadoNuevo(EstadoReporte.PENDIENTE);
        inicio.setUsuarioCambio(reportante);
        inicio.setObservacion("Reporte creado.");
        historialRepository.save(inicio);
        return reporteMapper.toDto(reporte);
    }

    @Override
    public Page<ReporteResponseDTO> listar(
            EstadoReporte estado,
            LocalDate desde,
            LocalDate hasta,
            Long usuarioId,
            Long ubicacionId,
            Pageable pageable) {
        validarRangoFechas(desde, hasta);
        OffsetDateTime start = desde == null ? null : desde.atStartOfDay(ZONA_CAMPUS).toOffsetDateTime();
        OffsetDateTime endExclusive = hasta == null ? null : hasta.plusDays(1).atStartOfDay(ZONA_CAMPUS).toOffsetDateTime();
        if (usuarioId != null) {
            buscarUsuario(usuarioId);
        }
        if (ubicacionId != null) {
            buscarUbicacion(ubicacionId);
        }
        return buscarFiltrados(estado, start, endExclusive, usuarioId, ubicacionId, pageable)
                .map(reporteMapper::toDto);
    }

    @Override
    public ReporteResponseDTO obtener(Long id) {
        return reporteMapper.toDto(buscarReporte(id));
    }

    @Override
    public List<HistorialResponseDTO> historial(Long id) {
        buscarReporte(id);
        return historialMapper.toDtoList(historialRepository.buscarPorReporteOrdenado(id));
    }

    @Override
    @Transactional
    public ReporteResponseDTO cambiarEstado(Long id, CambioEstadoRequestDTO request) {
        Reporte reporte = buscarReporte(id);
        Usuario actor = buscarUsuario(request.usuarioCambioId());
        if (actor.getRol() != RolUsuario.MANTENIMIENTO) {
            throw new BusinessException("Solo el personal de mantenimiento puede cambiar el estado.", org.springframework.http.HttpStatus.FORBIDDEN);
        }
        if (reporte.getEstado() == request.estado()) {
            throw new BusinessException("El reporte ya se encuentra en el estado solicitado.");
        }

        EstadoReporte anterior = reporte.getEstado();
        reporte.setEstado(request.estado());
        reporteRepository.saveAndFlush(reporte);

        HistorialEstado cambio = new HistorialEstado();
        cambio.setReporte(reporte);
        cambio.setEstadoAnterior(anterior);
        cambio.setEstadoNuevo(request.estado());
        cambio.setUsuarioCambio(actor);
        cambio.setObservacion(request.observacion() == null || request.observacion().isBlank()
                ? null
                : request.observacion().trim());
        historialRepository.save(cambio);
        return reporteMapper.toDto(reporte);
    }

    @Override
    public IndicadoresDTO obtenerIndicadores() {
        Map<EstadoReporte, Long> counts = new EnumMap<>(EstadoReporte.class);
        for (EstadoReporteCount count : reporteRepository.contarPorEstado()) {
            counts.put(count.getEstado(), count.getCantidad());
        }
        List<ReporteResponseDTO> recientes = reporteMapper.toDtoList(
                reporteRepository.buscarRecientes(Pageable.ofSize(LIMITE_RECIENTES)));
        long pendientes = counts.getOrDefault(EstadoReporte.PENDIENTE, 0L);
        long enProceso = counts.getOrDefault(EstadoReporte.EN_PROCESO, 0L);
        long resueltos = counts.getOrDefault(EstadoReporte.RESUELTO, 0L);
        return new IndicadoresDTO(pendientes, enProceso, resueltos, pendientes + enProceso + resueltos, recientes);
    }

    @Override
    public byte[] generarPdf(EstadoReporte estado, LocalDate desde, LocalDate hasta, Long usuarioId, Long ubicacionId) {
        validarRangoFechas(desde, hasta);
        OffsetDateTime start = desde == null ? null : desde.atStartOfDay(ZONA_CAMPUS).toOffsetDateTime();
        OffsetDateTime endExclusive = hasta == null ? null : hasta.plusDays(1).atStartOfDay(ZONA_CAMPUS).toOffsetDateTime();
        if (usuarioId != null) {
            buscarUsuario(usuarioId);
        }
        if (ubicacionId != null) {
            buscarUbicacion(ubicacionId);
        }
        List<Reporte> reportes = buscarFiltrados(
                estado, start, endExclusive, usuarioId, ubicacionId, Pageable.unpaged()).getContent();
        return crearDocumentoPdf(reportes);
    }

    private Page<Reporte> buscarFiltrados(
            EstadoReporte estado,
            OffsetDateTime desde,
            OffsetDateTime hastaExclusivo,
            Long usuarioId,
            Long ubicacionId,
            Pageable pageable) {
        return reporteRepository.buscarFiltrados(
                estado != null,
                estado == null ? EstadoReporte.PENDIENTE : estado,
                desde != null,
                desde == null ? FECHA_INICIAL : desde,
                hastaExclusivo != null,
                hastaExclusivo == null ? FECHA_FINAL : hastaExclusivo,
                usuarioId != null,
                usuarioId == null ? 0L : usuarioId,
                ubicacionId != null,
                ubicacionId == null ? 0L : ubicacionId,
                pageable);
    }

    private byte[] crearDocumentoPdf(List<Reporte> reportes) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(output);
            PdfDocument pdf = new PdfDocument(writer);
            try (Document document = new Document(pdf)) {
                document.add(new Paragraph("CampusReporta · Reportes del campus")
                        .setFontSize(18)
                        .setBold()
                        .setFontColor(new com.itextpdf.kernel.colors.DeviceRgb(10, 61, 145)));
                document.add(new Paragraph("Generado: " + OffsetDateTime.now(ZONA_CAMPUS))
                        .setFontSize(9)
                        .setFontColor(ColorConstants.GRAY));
                document.add(new Paragraph("Registros: " + reportes.size()).setFontSize(10));

                Table table = new Table(UnitValue.createPercentArray(new float[] {8, 35, 19, 13, 25}))
                        .useAllAvailableWidth();
                for (String title : List.of("ID", "Descripción", "Ubicación", "Estado", "Reportado por")) {
                    table.addHeaderCell(new Cell().add(new Paragraph(title).setBold().setFontSize(9))
                            .setBackgroundColor(new com.itextpdf.kernel.colors.DeviceRgb(235, 242, 252)));
                }
                for (Reporte reporte : reportes) {
                    table.addCell(pdfCell(String.valueOf(reporte.getId())));
                    table.addCell(pdfCell(reporte.getDescripcionDano()));
                    table.addCell(pdfCell(reporte.getUbicacion().getBloque() + " · " + reporte.getUbicacion().getSalon()));
                    table.addCell(pdfCell(reporte.getEstado().getValor()));
                    table.addCell(pdfCell(reporte.getUsuarioReporta().getNombreCompleto()));
                }
                document.add(table);
            }
            return output.toByteArray();
        } catch (RuntimeException | java.io.IOException exception) {
            throw new IllegalStateException("No fue posible generar el PDF de reportes.", exception);
        }
    }

    private Cell pdfCell(String value) {
        return new Cell().add(new Paragraph(value == null ? "" : value).setFontSize(8));
    }

    private Reporte buscarReporte(Long id) {
        return reporteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el reporte con id " + id + "."));
    }

    private Usuario buscarUsuario(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el usuario con id " + id + "."));
    }

    private Ubicacion buscarUbicacion(Long id) {
        return ubicacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la ubicación con id " + id + "."));
    }

    private void validarRangoFechas(LocalDate desde, LocalDate hasta) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new BusinessException("La fecha inicial no puede ser posterior a la fecha final.",
                    org.springframework.http.HttpStatus.BAD_REQUEST);
        }
    }
}
