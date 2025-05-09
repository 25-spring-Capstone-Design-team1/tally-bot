package com.tallybot.backend.tallybot_back.controller;

import com.tallybot.backend.tallybot_back.dto.BotCalculateDto;
import com.tallybot.backend.tallybot_back.dto.ResponseDetailDto;
import com.tallybot.backend.tallybot_back.service.CalculateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/calculate")
public class CalculateController {

    private final CalculateService calculateService;

    @PostMapping("/start")
    public ResponseEntity<Long> startCalculate(@RequestBody BotCalculateDto request) {
        Long calculateId = calculateService.startCalculate(request);
        return ResponseEntity.ok(calculateId);
    }

    @GetMapping("/{calculateId}/result")
    public ResponseEntity<ResponseDetailDto> getCalculateResult(@PathVariable Long calculateId) {
        ResponseDetailDto response = calculateService.resultReturn(calculateId);
        return ResponseEntity.ok(response);
    }
}



