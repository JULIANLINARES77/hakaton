package uniminuto.campusreporta.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import uniminuto.campusreporta.dto.CambioEstadoRequestDTO;
import uniminuto.campusreporta.dto.ReporteRequestDTO;
import uniminuto.campusreporta.entity.EstadoReporte;
import uniminuto.campusreporta.entity.RolUsuario;
import uniminuto.campusreporta.entity.Ubicacion;
import uniminuto.campusreporta.entity.Usuario;
import uniminuto.campusreporta.exception.BusinessException;
import uniminuto.campusreporta.repository.HistorialEstadoRepository;
import uniminuto.campusreporta.repository.UbicacionRepository;
import uniminuto.campusreporta.repository.UsuarioRepository;

@SpringBootTest
@Transactional
class ReporteServiceIntegrationTests {
    @Autowired
    private ReporteService reporteService;

    @Autowired
    private HistorialEstadoRepository historialRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UbicacionRepository ubicacionRepository;

    private Usuario estudiante;
    private Usuario mantenimiento;
    private Usuario docente;
    private Ubicacion ubicacion;

    @BeforeEach
    void prepararCatalogos() {
        String token = UUID.randomUUID().toString();
        estudiante = usuarioRepository.save(usuario("Estudiante " + token, "estudiante-" + token + "@test.example", RolUsuario.ESTUDIANTE));
        mantenimiento = usuarioRepository.save(usuario("Mantenimiento " + token, "mantenimiento-" + token + "@test.example", RolUsuario.MANTENIMIENTO));
        docente = usuarioRepository.save(usuario("Docente " + token, "docente-" + token + "@test.example", RolUsuario.DOCENTE));

        ubicacion = new Ubicacion();
        ubicacion.setBloque("Bloque de prueba " + token);
        ubicacion.setSalon("101");
        ubicacion = ubicacionRepository.save(ubicacion);
    }

    @Test
    void crearReporteInicializaPendienteYRegistraHistorial() {
        var reporte = reporteService.crear(new ReporteRequestDTO(
                "Filtración en el techo del salón.",
                ubicacion.getId(),
                estudiante.getId()));

        assertThat(reporte.estado()).isEqualTo(EstadoReporte.PENDIENTE);
        var historial = historialRepository.buscarPorReporteOrdenado(reporte.id());
        assertThat(historial).hasSize(1);
        assertThat(historial.getFirst().getEstadoAnterior()).isNull();
        assertThat(historial.getFirst().getEstadoNuevo()).isEqualTo(EstadoReporte.PENDIENTE);
        assertThat(historial.getFirst().getUsuarioCambio().getId()).isEqualTo(estudiante.getId());
    }

    @Test
    void cambioDeEstadoSoloPermiteMantenimientoYRegistraElActor() {
        var reporte = reporteService.crear(new ReporteRequestDTO(
                "Luminaria dañada.",
                ubicacion.getId(),
                estudiante.getId()));

        assertThatThrownBy(() -> reporteService.cambiarEstado(reporte.id(),
                new CambioEstadoRequestDTO(EstadoReporte.EN_PROCESO, docente.getId(), "Intento no autorizado.")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Solo el personal de mantenimiento");

        var actualizado = reporteService.cambiarEstado(reporte.id(),
                new CambioEstadoRequestDTO(EstadoReporte.EN_PROCESO, mantenimiento.getId(), "Técnico asignado."));
        assertThat(actualizado.estado()).isEqualTo(EstadoReporte.EN_PROCESO);

        var historial = historialRepository.buscarPorReporteOrdenado(reporte.id());
        assertThat(historial).hasSize(2);
        assertThat(historial.getFirst().getEstadoNuevo()).isEqualTo(EstadoReporte.EN_PROCESO);
        assertThat(historial.getFirst().getUsuarioCambio().getId()).isEqualTo(mantenimiento.getId());
        assertThat(historial.getFirst().getObservacion()).isEqualTo("Técnico asignado.");
    }

    @Test
    void filtrosIndicadoresYPdfFuncionanConElMismoReporte() {
        reporteService.crear(new ReporteRequestDTO(
                "Ventana rota en el aula.",
                ubicacion.getId(),
                estudiante.getId()));

        var page = reporteService.listar(
                EstadoReporte.PENDIENTE,
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(1),
                estudiante.getId(),
                ubicacion.getId(),
                PageRequest.of(0, 10));
        var indicadores = reporteService.obtenerIndicadores();
        byte[] pdf = reporteService.generarPdf(
                EstadoReporte.PENDIENTE, null, null, estudiante.getId(), ubicacion.getId());

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(indicadores.pendientes()).isGreaterThanOrEqualTo(1);
        assertThat(indicadores.reportesRecientes()).isNotEmpty();
        assertThat(pdf).startsWith("%PDF".getBytes(java.nio.charset.StandardCharsets.US_ASCII));
    }

    private Usuario usuario(String nombre, String correo, RolUsuario rol) {
        Usuario usuario = new Usuario();
        usuario.setNombreCompleto(nombre);
        usuario.setCorreo(correo);
        usuario.setRol(rol);
        return usuario;
    }
}
