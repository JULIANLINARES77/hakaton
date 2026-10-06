package uniminuto.campusreporta.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import uniminuto.campusreporta.dto.ReporteRequestDTO;
import uniminuto.campusreporta.dto.UbicacionRequestDTO;
import uniminuto.campusreporta.dto.UsuarioRequestDTO;
import uniminuto.campusreporta.entity.EstadoReporte;
import uniminuto.campusreporta.entity.HistorialEstado;
import uniminuto.campusreporta.entity.Reporte;
import uniminuto.campusreporta.entity.RolUsuario;
import uniminuto.campusreporta.entity.Ubicacion;
import uniminuto.campusreporta.entity.Usuario;

class MapperTests {
    private final UsuarioMapper usuarioMapper = new UsuarioMapperImpl();
    private final UbicacionMapper ubicacionMapper = new UbicacionMapperImpl();
    private final ReporteMapper reporteMapper = new ReporteMapperImpl();
    private final HistorialEstadoMapper historialMapper = new HistorialEstadoMapperImpl();

    @Test
    void usuarioMapperConvierteRequestYResponse() {
        var request = new UsuarioRequestDTO("Ana Torres", "ana@example.edu", RolUsuario.ESTUDIANTE);

        Usuario usuario = usuarioMapper.toEntity(request);
        usuario.setId(12L);
        var response = usuarioMapper.toDto(usuario);

        assertThat(usuario.getCreatedAt()).isNull();
        assertThat(usuario.getUpdatedAt()).isNull();
        assertThat(response).satisfies(dto -> {
            assertThat(dto.id()).isEqualTo(12L);
            assertThat(dto.nombreCompleto()).isEqualTo("Ana Torres");
            assertThat(dto.correo()).isEqualTo("ana@example.edu");
            assertThat(dto.rol()).isEqualTo(RolUsuario.ESTUDIANTE);
        });
        assertThat(usuarioMapper.toDtoList(List.of(usuario))).containsExactly(response);
    }

    @Test
    void ubicacionMapperConvierteAmbasDirecciones() {
        var request = new UbicacionRequestDTO("Bloque A", "101", "Aula principal");

        Ubicacion ubicacion = ubicacionMapper.toEntity(request);
        ubicacion.setId(23L);
        var response = ubicacionMapper.toDto(ubicacion);

        assertThat(response.id()).isEqualTo(23L);
        assertThat(response.etiqueta()).isEqualTo("Bloque A · 101");
        assertThat(ubicacionMapper.toDtoList(List.of(ubicacion))).containsExactly(response);
    }

    @Test
    void reporteMapperConvierteRelacionesAnidadasAlResponse() {
        Usuario reportante = new Usuario();
        reportante.setId(12L);
        reportante.setNombreCompleto("Ana Torres");
        reportante.setCorreo("ana@example.edu");

        Ubicacion ubicacion = new Ubicacion();
        ubicacion.setId(23L);
        ubicacion.setBloque("Bloque A");
        ubicacion.setSalon("101");

        Reporte reporte = new Reporte();
        reporte.setId(34L);
        reporte.setDescripcionDano("Ventana rota");
        reporte.setUbicacion(ubicacion);
        reporte.setUsuarioReporta(reportante);
        reporte.setEstado(EstadoReporte.PENDIENTE);
        reporte.setFechaReporte(OffsetDateTime.parse("2026-10-06T09:00:00-05:00"));

        var response = reporteMapper.toDto(reporte);

        assertThat(response.id()).isEqualTo(34L);
        assertThat(response.descripcionDano()).isEqualTo("Ventana rota");
        assertThat(response.ubicacionId()).isEqualTo(23L);
        assertThat(response.bloque()).isEqualTo("Bloque A");
        assertThat(response.salon()).isEqualTo("101");
        assertThat(response.usuarioReportaId()).isEqualTo(12L);
        assertThat(response.nombreUsuarioReporta()).isEqualTo("Ana Torres");
        assertThat(response.correoUsuarioReporta()).isEqualTo("ana@example.edu");
        assertThat(response.estado()).isEqualTo(EstadoReporte.PENDIENTE);
    }

    @Test
    void historialMapperConvierteReporteYUsuarioRelacionados() {
        Reporte reporte = new Reporte();
        reporte.setId(34L);
        Usuario actor = new Usuario();
        actor.setId(56L);
        actor.setNombreCompleto("Marta Gómez");

        HistorialEstado historial = new HistorialEstado();
        historial.setId(78L);
        historial.setReporte(reporte);
        historial.setEstadoAnterior(EstadoReporte.PENDIENTE);
        historial.setEstadoNuevo(EstadoReporte.EN_PROCESO);
        historial.setFechaCambio(OffsetDateTime.parse("2026-10-06T09:30:00-05:00"));
        historial.setUsuarioCambio(actor);
        historial.setObservacion("Técnico asignado.");

        var response = historialMapper.toDto(historial);

        assertThat(response.id()).isEqualTo(78L);
        assertThat(response.reporteId()).isEqualTo(34L);
        assertThat(response.estadoAnterior()).isEqualTo(EstadoReporte.PENDIENTE);
        assertThat(response.estadoNuevo()).isEqualTo(EstadoReporte.EN_PROCESO);
        assertThat(response.usuarioCambioId()).isEqualTo(56L);
        assertThat(response.nombreUsuarioCambio()).isEqualTo("Marta Gómez");
        assertThat(response.observacion()).isEqualTo("Técnico asignado.");
    }
}
