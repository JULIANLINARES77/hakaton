package uniminuto.campusreporta.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ReporteRequestDTO(
        @NotBlank(message = "La descripción del daño es obligatoria.")
        @Size(max = 5000, message = "La descripción no puede superar 5000 caracteres.")
        String descripcionDano,
        @NotNull(message = "La ubicación es obligatoria.")
        @Positive(message = "La ubicación debe ser un identificador válido.")
        Long ubicacionId,
        @NotNull(message = "El usuario que reporta es obligatorio.")
        @Positive(message = "El usuario debe ser un identificador válido.")
        Long usuarioReportaId) {
}
