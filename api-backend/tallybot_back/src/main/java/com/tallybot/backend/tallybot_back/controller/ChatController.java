package com.tallybot.backend.tallybot_back.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.tallybot.backend.tallybot_back.dto.*;
import com.tallybot.backend.tallybot_back.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.aspectj.bridge.Message;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadChat(@Valid @RequestBody List<@Valid ChatDto> chatDtoList) {

        boolean allExist = chatService.groupAndMembersExist(chatDtoList);
        if (!allExist) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Group or member not found for given data."));
        }
        chatService.saveChats(chatDtoList); // 채팅 저장
        return ResponseEntity.ok(new MessageResponse("Upload successful. Chat count: " + chatDtoList.size()));
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<ChatResponseDto>> getChatsByGroup(@PathVariable Long groupId) {
        List<ChatResponseDto> chatList = chatService.getChatsByGroup(groupId);
        return ResponseEntity.ok(chatList);
    }

}
