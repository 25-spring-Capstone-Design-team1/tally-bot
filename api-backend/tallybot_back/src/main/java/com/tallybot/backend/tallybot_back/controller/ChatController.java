package com.tallybot.backend.tallybot_back.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tallybot.backend.tallybot_back.domain.Chat;
import com.tallybot.backend.tallybot_back.repository.ChatRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatRepository chatRepository;
    private final ObjectMapper objectMapper;

    public ChatController(ChatRepository chatRepository, ObjectMapper objectMapper) {
        this.chatRepository = chatRepository;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadChatFile(@RequestParam("file") MultipartFile file) {
        try {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            List<Chat> chatList = objectMapper.readValue(
                    content,
                    new TypeReference<List<Chat>>() {}
            );

            chatRepository.saveAll(chatList); // 채팅 저장

            return ResponseEntity.ok("업로드 성공! 채팅 수: " + chatList.size());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("에러: " + e.getMessage());
        }
    }

}
