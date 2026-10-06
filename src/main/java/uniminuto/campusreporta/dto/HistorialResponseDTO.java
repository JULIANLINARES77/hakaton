package uniminuto.campusreporta.dto;

import java.time.OffsetDateTime;
import uniminuto.campusreporta.entity.EstadoReporte;

public record HistorialResponseDTO(
        Long id,
        Long reporteId,
        EstadoReporte estadoAnterior,
        EstadoReporte estadoNuevo,
        OffsetDateTime fechaCambio,
        Long usuarioCambioId,
        String nombreUsuarioCambio,
        String observacion) {
}
