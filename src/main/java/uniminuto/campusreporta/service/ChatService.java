package uniminuto.campusreporta.service;

import java.time.OffsetDateTime;
import org.springframework.stereotype.Service;
import uniminuto.campusreporta.dto.ChatRequestDTO;
import uniminuto.campusreporta.dto.ChatResponseDTO;
import uniminuto.campusreporta.prompt.PromptBuilder;
import uniminuto.campusreporta.prompt.SystemPrompt;

@Service
public class ChatService {
    private final DataIngestionService dataIngestionService;
    private final OllamaClient ollamaClient;
    private final PromptBuilder promptBuilder;

    public ChatService(
            DataIngestionService dataIngestionService,
            OllamaClient ollamaClient,
            PromptBuilder promptBuilder) {
        this.dataIngestionService = dataIngestionService;
        this.ollamaClient = ollamaClient;
        this.promptBuilder = promptBuilder;
    }

    public ChatResponseDTO ask(ChatRequestDTO request) {
        DataIngestionService.ContextData context = dataIngestionService.retrieve(request.pregunta());
        String prompt = promptBuilder.build(request.pregunta(), context.text());
        String answer = ollamaClient.generate(SystemPrompt.INSTRUCTIONS, prompt);
        return new ChatResponseDTO(answer, OffsetDateTime.now(), context.documents());
    }
}
