package com.tallybot.backend.tallybot_back.controller;

import com.tallybot.backend.tallybot_back.dto.CalculateRequestDto;
import com.tallybot.backend.tallybot_back.dto.CalculateIdDto;
import com.tallybot.backend.tallybot_back.dto.ErrorResponse;
//import com.tallybot.backend.tallybot_back.dto.ResponseDetailDto;
import com.tallybot.backend.tallybot_back.service.CalculateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/calculate")
public class CalculateController {

    private final CalculateService calculateService;

    @PostMapping("/start")
    public ResponseEntity<?> startCalculate(@Valid @RequestBody CalculateRequestDto request) {

        // 시간 유효성 체크
        if (request.getStartTime().isAfter(request.getEndTime())) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Start time must be before end time."));
        }

//        // 그룹 존재 여부 확인
//        if (!calculateService.groupExists(request.getGroupId())) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                    .body(new ErrorResponse("Group not found."));
//        }

        // 정산 시작
        Long calculateId = calculateService.startCalculate(request);
        return ResponseEntity.ok(new CalculateIdDto(calculateId));
    }

//    @GetMapping("/{calculateId}/result")
//    public ResponseEntity<ResponseDetailDto> getCalculateResult(@PathVariable Long calculateId) {
//        ResponseDetailDto response = calculateService.resultReturn(calculateId);
//        return ResponseEntity.ok(response);
//    }
}



