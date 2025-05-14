package com.tallybot.backend.tallybot_back.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
//import com.tallybot.backend.tallybot_back.dto.ResponseDetailDto;
import com.tallybot.backend.tallybot_back.dto.CalculateRequestDto;
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
import static org.mockito.BDDMockito.given;
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
    @DisplayName("정산 요청 성공")
    void startCalculate_success() throws Exception {
        CalculateRequestDto request = new CalculateRequestDto(
                1L,
                LocalDateTime.of(2024, 5, 1, 14, 0),
                LocalDateTime.of(2024, 5, 1, 15, 0)
        );

        given(calculateService.groupExists(1L)).willReturn(true);
        given(calculateService.startCalculate(any())).willReturn(42L);

        mockMvc.perform(post("/api/calculate/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.calculateId").value(42));
    }


    @Test
    @DisplayName("404 Not Found : 그룹 없음")
    void startCalculate_groupNotFound() throws Exception {
        CalculateRequestDto request = new CalculateRequestDto(
                999L,
                LocalDateTime.of(2024, 5, 1, 14, 0),
                LocalDateTime.of(2024, 5, 1, 15, 0)
        );

        given(calculateService.groupExists(999L)).willReturn(false);

        mockMvc.perform(post("/api/calculate/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Group not found."));
    }

    @Test
    @DisplayName("400 Bad Request : endTime이 startTime보다 이를 때")
    void startCalculate_invalidTimeRange() throws Exception {
        CalculateRequestDto request = new CalculateRequestDto(
                1L,
                LocalDateTime.of(2024, 5, 1, 15, 0),
                LocalDateTime.of(2024, 5, 1, 14, 0)
        );

        given(calculateService.groupExists(1L)).willReturn(true);

        mockMvc.perform(post("/api/calculate/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Start time must be before end time."));
    }
}
