package uniminuto.campusreporta.dto;

import java.time.OffsetDateTime;
import uniminuto.campusreporta.entity.EstadoReporte;

public record ReporteResponseDTO(
        Long id,
        String descripcionDano,
        Long ubicacionId,
        String bloque,
        String salon,
        Long usuarioReportaId,
        String nombreUsuarioReporta,
        String correoUsuarioReporta,
        EstadoReporte estado,
        OffsetDateTime fechaReporte) {
}
