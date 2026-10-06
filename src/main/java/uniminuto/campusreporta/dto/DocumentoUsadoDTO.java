package uniminuto.campusreporta.dto;

import java.util.Map;

public record DocumentoUsadoDTO(String type, String id, Map<String, String> metadata) {
}
