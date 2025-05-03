package com.tallybot.backend.tallybot_back.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tallybot.backend.tallybot_back.dto.CalculateDto;
import com.tallybot.backend.tallybot_back.dto.ResponseDto;
import com.tallybot.backend.tallybot_back.service.CalculateService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CalculateController.class)
class CalculateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CalculateService calculateService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("정산 시작 요청 테스트")
    void startCalculateTest() throws Exception {
        // given
        CalculateDto request = new CalculateDto();
        request.setGroupId(1L);
        request.setStartTime(LocalDateTime.parse("2024-04-30T10:00:00"));
        request.setEndTime(LocalDateTime.parse("2024-04-30T11:00:00"));

        when(calculateService.startCalculate(any(CalculateDto.class))).thenReturn(42L); // 가짜 calculateId

        // when + then
        mockMvc.perform(post("/calculate/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("42"));
    }

    @Test
    @DisplayName("정산 결과 조회 테스트")
    void getCalculateResultTest() throws Exception {
        // given
        Long calculateId = 42L;
        ResponseDto fakeResponse = new ResponseDto("https://tallybot.me/calculate/42", List.of());

        when(calculateService.resultReturn(calculateId)).thenReturn(fakeResponse);

        // when + then
        mockMvc.perform(get("/calculate/42/result"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("https://tallybot.me/calculate/42"))
                .andExpect(jsonPath("$.details").isArray());
    }
}
