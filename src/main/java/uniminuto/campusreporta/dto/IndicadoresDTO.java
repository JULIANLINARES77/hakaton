package uniminuto.campusreporta.dto;

import java.util.List;

public record IndicadoresDTO(
        long pendientes,
        long enProceso,
        long resueltos,
        long total,
        List<ReporteResponseDTO> reportesRecientes) {
}
