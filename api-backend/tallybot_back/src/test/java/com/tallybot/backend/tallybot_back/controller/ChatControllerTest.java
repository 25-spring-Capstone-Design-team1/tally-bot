package com.tallybot.backend.tallybot_back.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tallybot.backend.tallybot_back.dto.ChatDto;
import com.tallybot.backend.tallybot_back.service.ChatService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;


import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChatController.class)
@ActiveProfiles({"test", "mock-data"})  // 이 프로파일 조합으로 별도 컨텍스트 생성
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ChatService chatService;

    @Test
    @DisplayName("채팅 업로드 성공")
    void uploadChat_Success() throws Exception {
        // 테스트용 ChatDto 리스트 생성
        List<ChatDto> chatList = List.of(
                new ChatDto(1L, LocalDateTime.now(), 1001L, "안녕"),
                new ChatDto(1L, LocalDateTime.now(), 1002L, "반가워")
        );

        given(chatService.groupAndMembersExist(anyList())).willReturn(true);

        mockMvc.perform(post("/api/chat/upload")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(chatList)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Upload successful. Chat count: 2"));

    }

    @Test
    @DisplayName("404 Not Found : 그룹/멤버 없음")
    void uploadChat_groupOrMemberNotFound() throws Exception {
        List<ChatDto> chatList = List.of(
                new ChatDto(1L, LocalDateTime.now(), 9999L, "없는 사람")
        );

        given(chatService.groupAndMembersExist(anyList())).willReturn(false);

        mockMvc.perform(post("/api/chat/upload")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(chatList)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Group or member not found for given data."));
    }

    @Test
    @DisplayName("400 Bad Request : JSON 형식 오류")
    void uploadChat_jsonFormatError() throws Exception {
        String invalidJson = "[ { \"groupId\": 1, \"timestamp\": \"오류\", \"memberId\": 1001, \"message\": \"하이\" } ]";

        mockMvc.perform(post("/api/chat/upload")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid input"));
    }

    @Test
    @DisplayName("400 Bad Request : 필수 필드 누락")
    void uploadChat_missingFields() throws Exception {
        String missingFieldJson = "[ { \"groupId\": 1, \"memberId\": 1001, \"message\": \"하이\" } ]";

        mockMvc.perform(post("/api/chat/upload")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(missingFieldJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Essential field must not be null"));
    }
}
