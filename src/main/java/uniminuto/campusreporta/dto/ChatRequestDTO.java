package uniminuto.campusreporta.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatRequestDTO(
        @NotBlank(message = "Escribe una pregunta para el asistente.")
        @Size(max = 1200, message = "La pregunta no puede superar 1200 caracteres.")
        String pregunta) {
}
