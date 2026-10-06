package uniminuto.campusreporta.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record ChatResponseDTO(
        String respuesta,
        OffsetDateTime timestamp,
        List<DocumentoUsadoDTO> documentosUsados) {
}
