package com.tallybot.backend.tallybot_back.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tallybot.backend.tallybot_back.dto.ChatDto;
import com.tallybot.backend.tallybot_back.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;
    private final ObjectMapper objectMapper;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadChatFile(@RequestParam("file") MultipartFile file) {
        try {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            List<ChatDto> chatDtoList = objectMapper.readValue(
                    content,
                    new TypeReference<List<ChatDto>>() {}
            );

            chatService.saveChats(chatDtoList); // 채팅 저장

            return ResponseEntity.ok("업로드 성공! 채팅 수: " + chatDtoList.size());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("에러: " + e.getMessage());
        }
    }

}
