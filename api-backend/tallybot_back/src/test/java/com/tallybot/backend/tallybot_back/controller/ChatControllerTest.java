package com.tallybot.backend.tallybot_back.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tallybot.backend.tallybot_back.dto.ChatDto;
import com.tallybot.backend.tallybot_back.service.ChatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChatController.class)
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChatService chatService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void uploadChatFile_shouldReturnSuccess() throws Exception {
        // 테스트용 ChatDto 리스트 생성
        List<ChatDto> chatDtos = List.of(
                new ChatDto(1L, LocalDateTime.parse("2024-04-30T15:30:00"), "철수", "안녕"),
                new ChatDto(1L, LocalDateTime.parse("2024-04-30T15:31:00"), "영희", "반가워")
        );

        // ChatDto 리스트를 JSON 문자열로 변환
        String jsonContent = objectMapper.writeValueAsString(chatDtos);

        // MultipartFile 형태로 변환 (txt 파일처럼)
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "chat.txt",
                MediaType.TEXT_PLAIN_VALUE,
                jsonContent.getBytes(StandardCharsets.UTF_8)
        );

        // 서비스가 호출됐을 때 에러 없이 통과되도록 설정
        doNothing().when(chatService).saveChats(chatDtos);

        // 실제 API 호출 테스트
        mockMvc.perform(multipart("/chat/upload").file(mockFile))
                .andExpect(status().isOk())
                .andExpect(content().string("업로드 성공! 채팅 수: " + chatDtos.size()));
    }
}
