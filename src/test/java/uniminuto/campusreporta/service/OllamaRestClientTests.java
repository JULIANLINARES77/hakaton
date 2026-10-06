package uniminuto.campusreporta.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import uniminuto.campusreporta.config.OllamaProperties;
import uniminuto.campusreporta.exception.AiProviderException;

class OllamaRestClientTests {
    @Test
    void enviaSystemPromptPreguntaModeloYDevuelveTexto() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://ollama.test");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        OllamaRestClient client = new OllamaRestClient(builder.build(), properties());

        server.expect(requestTo("http://ollama.test/api/chat"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.model").value("llama3.2:3b"))
                .andExpect(jsonPath("$.stream").value(false))
                .andExpect(jsonPath("$.messages[0].role").value("system"))
                .andExpect(jsonPath("$.messages[0].content").value("Reglas del sistema"))
                .andExpect(jsonPath("$.messages[1].role").value("user"))
                .andExpect(jsonPath("$.messages[1].content").value("Pregunta y contexto"))
                .andRespond(withSuccess("""
                        {"model":"llama3.2:3b","message":{"role":"assistant","content":" Hay dos reportes pendientes. "},"done":true}
                        """, MediaType.APPLICATION_JSON));

        assertThat(client.generate("Reglas del sistema", "Pregunta y contexto"))
                .isEqualTo("Hay dos reportes pendientes.");
        server.verify();
    }

    @Test
    void informaCuandoFaltaMemoriaParaCargarElModelo() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://ollama.test");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        OllamaRestClient client = new OllamaRestClient(builder.build(), properties());
        server.expect(requestTo("http://ollama.test/api/chat"))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("{\"error\":\"unable to allocate CPU buffer\"}")
                        .contentType(MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.generate("sistema", "pregunta"))
                .isInstanceOf(AiProviderException.class)
                .hasMessageContaining("falta de memoria disponible")
                .hasMessageContaining("Cierra aplicaciones");
        server.verify();
    }

    @Test
    void informaComandoCuandoNoEstaDescargadoElModelo() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://ollama.test");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        OllamaRestClient client = new OllamaRestClient(builder.build(), properties());
        server.expect(requestTo("http://ollama.test/api/chat"))
                .andRespond(withResourceNotFound());

        assertThatThrownBy(() -> client.generate("sistema", "pregunta"))
                .isInstanceOf(AiProviderException.class)
                .hasMessageContaining("ollama pull llama3.2:3b");
        server.verify();
    }

    private OllamaProperties properties() {
        OllamaProperties properties = new OllamaProperties();
        properties.setBaseUrl("http://ollama.test");
        properties.setModel("llama3.2:3b");
        return properties;
    }
}
