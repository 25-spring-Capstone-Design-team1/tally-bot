package com.tallybot.backend.tallybot_back.controller;

import com.tallybot.backend.tallybot_back.dto.BotCalculateDto;
import com.tallybot.backend.tallybot_back.dto.BotResponseDto;
import com.tallybot.backend.tallybot_back.service.CalculateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/calculate")
public class BotCalculateController {

    private final CalculateService calculateService;

    @PostMapping("/start")
    public ResponseEntity<Long> startCalculate(@RequestBody BotCalculateDto request) {
        Long calculateId = calculateService.startCalculate(request);
        return ResponseEntity.ok(calculateId);
    }

    @GetMapping("/{calculateId}/result")
    public ResponseEntity<BotResponseDto> getCalculateResult(@PathVariable Long calculateId) {
        BotResponseDto response = calculateService.resultReturn(calculateId);
        return ResponseEntity.ok(response);
    }
}



