package uniminuto.campusreporta.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uniminuto.campusreporta.dto.ChatRequestDTO;
import uniminuto.campusreporta.dto.DocumentoUsadoDTO;
import uniminuto.campusreporta.prompt.PromptBuilder;

@ExtendWith(MockitoExtension.class)
class ChatServiceTests {
    @Mock
    private DataIngestionService dataIngestionService;

    @Mock
    private OllamaClient ollamaClient;

    private final PromptBuilder promptBuilder = new PromptBuilder();

    @Test
    void chatEntregaPreguntaYContextoRealAlProveedorYDevuelveFuentes() {
        var source = new DocumentoUsadoDTO("reporte", "42", java.util.Map.of("estado", "Pendiente"));
        when(dataIngestionService.retrieve("¿Qué está pendiente?"))
                .thenReturn(new DataIngestionService.ContextData("PostgreSQL: CR-42 está pendiente.", List.of(source)));
        when(ollamaClient.generate(anyString(), anyString())).thenReturn("El reporte 42 sigue pendiente.");

        ChatService service = new ChatService(dataIngestionService, ollamaClient, promptBuilder);
        var response = service.ask(new ChatRequestDTO("¿Qué está pendiente?"));

        ArgumentCaptor<String> systemPrompt = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> userPrompt = ArgumentCaptor.forClass(String.class);
        verify(ollamaClient).generate(systemPrompt.capture(), userPrompt.capture());
        assertThat(systemPrompt.getValue()).contains("CampusReporta");
        assertThat(userPrompt.getValue()).contains("CR-42 está pendiente", "¿Qué está pendiente?");
        assertThat(response.respuesta()).isEqualTo("El reporte 42 sigue pendiente.");
        assertThat(response.timestamp()).isNotNull();
        assertThat(response.documentosUsados()).containsExactly(source);
    }
}
