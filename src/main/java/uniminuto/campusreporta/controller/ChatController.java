package uniminuto.campusreporta.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uniminuto.campusreporta.dto.ChatRequestDTO;
import uniminuto.campusreporta.dto.ChatResponseDTO;
import uniminuto.campusreporta.service.ChatService;

@RestController
@RequestMapping("/api/ia")
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponseDTO> chat(@Valid @RequestBody ChatRequestDTO request) {
        return ResponseEntity.ok(chatService.ask(request));
    }
}
