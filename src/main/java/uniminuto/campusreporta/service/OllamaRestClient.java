package uniminuto.campusreporta.service;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestClientException;
import uniminuto.campusreporta.config.OllamaProperties;
import uniminuto.campusreporta.exception.AiProviderException;

@Component
public class OllamaRestClient implements OllamaClient {
    private final RestClient restClient;
    private final OllamaProperties properties;

    public OllamaRestClient(RestClient ollamaHttpClient, OllamaProperties properties) {
        this.restClient = ollamaHttpClient;
        this.properties = properties;
    }

    @Override
    public String generate(String systemInstruction, String prompt) {
        Map<String, Object> request = Map.of(
                "model", properties.getModel(),
                "stream", false,
                "messages", List.of(
                        Map.of("role", "system", "content", systemInstruction),
                        Map.of("role", "user", "content", prompt)),
                "options", Map.of("temperature", 0.2, "num_predict", 900));
        try {
            Map<?, ?> response = restClient.post()
                    .uri("/api/chat")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(Map.class);
            String text = extractContent(response);
            if (text == null || text.isBlank()) {
                throw new AiProviderException("Ollama no devolvió una respuesta de texto.", 502);
            }
            return text.trim();
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                throw new AiProviderException(
                        "Ollama no encuentra el modelo " + properties.getModel()
                                + ". Ejecuta: ollama pull " + properties.getModel(),
                        503,
                        exception);
            }
            if (exception.getResponseBodyAsString().toLowerCase(Locale.ROOT)
                    .contains("unable to allocate cpu buffer")) {
                throw new AiProviderException(
                        "Ollama no pudo cargar el modelo " + properties.getModel()
                                + " por falta de memoria disponible. Cierra aplicaciones que consuman RAM "
                                + "y vuelve a intentarlo.",
                        503,
                        exception);
            }
            throw new AiProviderException(
                    "Ollama no pudo procesar la consulta (HTTP "
                            + exception.getStatusCode().value() + ").",
                    exception.getStatusCode().is5xxServerError() ? 503 : 502,
                    exception);
        } catch (ResourceAccessException exception) {
            throw new AiProviderException(
                    "No fue posible conectar con Ollama en " + properties.getBaseUrl()
                            + ". Confirma que Ollama esté iniciado y que el modelo esté descargado.",
                    503,
                    exception);
        } catch (RestClientException exception) {
            throw new AiProviderException("Ocurrió un error al comunicarse con Ollama.", 502, exception);
        }
    }

    private String extractContent(Map<?, ?> response) {
        if (response == null || !(response.get("message") instanceof Map<?, ?> message)) {
            return null;
        }
        Object content = message.get("content");
        return content instanceof String text ? text : null;
    }
}
