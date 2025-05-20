package com.tallybot.backend.tallybot_back.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tallybot.backend.tallybot_back.domain.Calculate;
import com.tallybot.backend.tallybot_back.dto.*;
import com.tallybot.backend.tallybot_back.repository.*;
import com.tallybot.backend.tallybot_back.service.*;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Optional;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UpdateController.class)
public class UpdateControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SettlementService settlementService;

    @MockitoBean
    private CalculateRepository calculateRepository;

    @MockitoBean
    private MemberRepository memberRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void addSettlement_success() throws Exception {
        SettlementUpdateRequest request = validAddRequest();
        Mockito.when(settlementService.fieldExists(eq("add"), any())).thenReturn(true);
        Mockito.when(calculateRepository.findById(42L)).thenReturn(Optional.of(new Calculate()));
        Mockito.when(settlementService.applySettlementUpdate(any())).thenReturn(42L);

        mockMvc.perform(post("/api/update/settlement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.settlementId").value(42));
    }

    @Test
    void updateSettlement_success() throws Exception {
        SettlementUpdateRequest request = validUpdateRequest();
        Mockito.when(settlementService.fieldExists(eq("update"), any())).thenReturn(true);
        Mockito.when(calculateRepository.findById(42L)).thenReturn(Optional.of(new Calculate()));
        Mockito.when(settlementService.applySettlementUpdate(any())).thenReturn(42L);

        mockMvc.perform(post("/api/update/settlement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.settlementId").value(42));
    }

    @Test
    void deleteSettlement_success() throws Exception {
        SettlementUpdateRequest request = new SettlementUpdateRequest();
        request.setCalculateId(42L);
        request.setSettlementId(31L);
        request.setField("delete");

        Mockito.when(settlementService.fieldExists(eq("delete"), any())).thenReturn(true);
        Mockito.when(calculateRepository.findById(42L)).thenReturn(Optional.of(new Calculate()));
        Mockito.when(settlementService.applySettlementUpdate(any())).thenReturn(31L);

        mockMvc.perform(post("/api/update/settlement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.settlementId").value(31));
    }

    @Test
    void fail_when_invalidField() throws Exception {
        SettlementUpdateRequest request = validAddRequest();
        request.setField("invalidField");

        mockMvc.perform(post("/api/update/settlement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid field type. Must be one of: add, update, delete."));
    }

    @Test
    void fail_when_missingRequiredFields() throws Exception {
        SettlementUpdateRequest request = new SettlementUpdateRequest(); // 비어있는 요청
        request.setField("add");
        request.setCalculateId(42L);

        Mockito.when(settlementService.fieldExists("add", request)).thenReturn(false);

        mockMvc.perform(post("/api/update/settlement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Missing required fields in one or more."));
    }

    @Test
    void fail_when_calculateNotFound() throws Exception {
        SettlementUpdateRequest request = validAddRequest();
        Mockito.when(settlementService.fieldExists(eq("add"), any())).thenReturn(true);
        Mockito.when(calculateRepository.findById(42L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/update/settlement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Calculate entity not found."));
    }

    private SettlementUpdateRequest validAddRequest() {
        SettlementUpdateRequest request = new SettlementUpdateRequest();
        request.setCalculateId(42L);
        request.setField("add");
        request.setNewValue(Map.of(
                "place", "가츠벤또",
                "item", "점심",
                "amount", 30000,
                "payer", 1001,
                "participants", List.of(1002, 1004)
        ));
        request.setConstants(Map.of("1001", 5000, "1002", 0, "1004", 0));
        request.setRatios(Map.of("1001", 0, "1002", 1, "1004", 2));
        request.setSum(3);
        return request;
    }

    private SettlementUpdateRequest validUpdateRequest() {
        SettlementUpdateRequest request = new SettlementUpdateRequest();
        request.setCalculateId(42L);
        request.setSettlementId(31L);
        request.setField("update");
        request.setNewValue(Map.of(
                "payer", 1001,
                "participants", List.of(1001, 1002, 1004)
        ));
        request.setConstants(Map.of("1001", 5000, "1002", 0, "1004", 0));
        request.setRatios(Map.of("1001", 0, "1002", 1, "1004", 2));
        request.setSum(3);
        return request;
    }
}
