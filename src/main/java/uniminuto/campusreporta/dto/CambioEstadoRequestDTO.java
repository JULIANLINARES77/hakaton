package uniminuto.campusreporta.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import uniminuto.campusreporta.entity.EstadoReporte;

public record CambioEstadoRequestDTO(
        @NotNull(message = "El nuevo estado es obligatorio.")
        EstadoReporte estado,
        @NotNull(message = "El usuario que realiza el cambio es obligatorio.")
        @Positive(message = "El usuario debe ser un identificador válido.")
        Long usuarioCambioId,
        @Size(max = 2000, message = "La observación no puede superar 2000 caracteres.")
        String observacion) {
}
