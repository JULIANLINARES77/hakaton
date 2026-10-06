package uniminuto.campusreporta.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UbicacionRequestDTO(
        @NotBlank(message = "El bloque es obligatorio.")
        @Size(max = 80, message = "El bloque no puede superar 80 caracteres.")
        String bloque,
        @NotBlank(message = "El salón es obligatorio.")
        @Size(max = 80, message = "El salón no puede superar 80 caracteres.")
        String salon,
        @Size(max = 500, message = "La descripción no puede superar 500 caracteres.")
        String descripcion) {
}
