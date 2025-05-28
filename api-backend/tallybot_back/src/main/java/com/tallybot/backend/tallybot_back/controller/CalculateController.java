package com.tallybot.backend.tallybot_back.controller;

import com.tallybot.backend.tallybot_back.dto.*;
import com.tallybot.backend.tallybot_back.domain.*;
import com.tallybot.backend.tallybot_back.repository.CalculateDetailRepository;
import com.tallybot.backend.tallybot_back.repository.CalculateRepository;
import com.tallybot.backend.tallybot_back.repository.SettlementRepository;
import com.tallybot.backend.tallybot_back.service.CalculateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/calculate")
public class CalculateController {

    private final CalculateService calculateService;
    private final CalculateRepository calculateRepository;
    private final SettlementRepository settlementRepository;
    private final CalculateDetailRepository calculateDetailRepository;

    @PostMapping("/start")
    public ResponseEntity<?> startCalculate(@Valid @RequestBody CalculateRequestDto request) {

        // 시간 유효성 체크
        if (request.getStartTime().isAfter(request.getEndTime())) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Start time must be before end time."));
        }

        // 그룹 존재 여부 확인
        if (!calculateService.groupExists(request.getGroupId())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Group not found."));
        }

        // 정산 시작
        Long calculateId = calculateService.startCalculate(request);
        return ResponseEntity.ok(new CalculateIdDto(calculateId));
    }

    @GetMapping("/{calculateId}/brief-result")
    public ResponseEntity<?> getBotResult(@PathVariable Long calculateId) {
        if (calculateId == null || calculateId <= 0) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Calculate ID must be a positive number."));
        }

        Optional<Calculate> optionalCalculate = calculateRepository.findById(calculateId);

        if (optionalCalculate.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("정산할 대화 내용이 없거나, 정산 결과가 존재하지 않습니다."));
        }

        Calculate calculate = optionalCalculate.get();

        if (calculate.getStatus() == CalculateStatus.CALCULATING) {
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(new MessageResponse("Calculate result is still being processed. Please try again later."));
        }

        BotResponseDto response = calculateService.botResultReturn(calculate);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{calculateId}/settlements")
    public ResponseEntity<?> getSettlementList(@PathVariable Long calculateId) {
        if (calculateId == null || calculateId <= 0) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Calculate ID must be positive."));
        }

        Optional<Calculate> optionalCalculate = calculateRepository.findById(calculateId);
        if (optionalCalculate.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Calculate not found."));
        }

        Calculate calculate = optionalCalculate.get();
        List<Settlement> settlements = settlementRepository.findByCalculate(calculate);

        //settlement별 처리
        List<FrontSettlementDto> result = settlements.stream().map(settlement -> {
            Set<Participant> participants = settlement.getParticipants();

            // 정산 대상자 ID 리스트
            List<Long> participantIds = participants.stream()
                    .map(p -> p.getParticipantKey().getMember().getMemberId())
                    .toList();

            // constants map
            Map<String, Integer> constants = participants.stream()
                    .collect(Collectors.toMap(
                            p -> String.valueOf(p.getParticipantKey().getMember().getMemberId()),
                            Participant::getConstant
                    ));

            // ratios map
            Map<String, Integer> ratios = participants.stream()
                    .collect(Collectors.toMap(
                            p -> String.valueOf(p.getParticipantKey().getMember().getMemberId()),
                            p -> p.getRatio().toInt()
                    ));

            //sum 계산
            int ratioSum = ratios.values().stream().mapToInt(Integer::intValue).sum();

            return new FrontSettlementDto(
                    settlement.getSettlementId(),
                    settlement.getPlace(),
                    settlement.getItem(),
                    settlement.getAmount(),
                    settlement.getPayer().getMemberId(),
                    participantIds,
                    constants,
                    ratios,
                    ratioSum
            );
        }).toList();

        return ResponseEntity.ok(new FrontSettlementListDto(result.size(), result));
    }

    @GetMapping("/{calculateId}/transfers")
    public ResponseEntity<?> getTransferList(@PathVariable Long calculateId) {
        if (calculateId == null || calculateId <= 0) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Calculate ID must be positive."));
        }

        Optional<Calculate> optionalCalculate = calculateRepository.findById(calculateId);
        if (optionalCalculate.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Calculate not found."));
        }

        Calculate calculate = optionalCalculate.get();

        List<CalculateDetail> details = calculateDetailRepository.findAllByCalculate(calculate);
        List<TransferDto> transfers = details.stream()
                .map(d -> new TransferDto(
                        d.getPayer().getMemberId(),
                        d.getPayee().getMemberId(),
                        d.getAmount()
                ))
                .toList();

        return ResponseEntity.ok(new TransferListDto(transfers.size(), transfers));
    }

    @PostMapping("/complete")
    public ResponseEntity<?> completeCalculate(@RequestBody Map<String, Long> body) {
        Long calculateId = body.get("calculateId");

        // 유효성 검사
        if (calculateId == null || calculateId <= 0) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Calculate ID must be positive."));
        }

        // 존재 확인
        Calculate calculate = calculateRepository.findById(calculateId).orElse(null);
        if (calculate == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("Calculate entity not found."));
        }

        // 상태 변경
        calculate.setStatus(CalculateStatus.COMPLETED);
        calculateRepository.save(calculate);

        return ResponseEntity.ok(Map.of("message", "Calculation marked as completed."));
    }

    @PostMapping("/recalculate")
    public ResponseEntity<?> recalculate(@RequestBody RecalculateRequestDto request) {
        if (request.getCalculateId() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Calculate ID must not be null."));
        }

        Calculate calculate = calculateRepository.findById(request.getCalculateId())
                .orElseThrow(() -> new NoSuchElementException("Calculate entity not found."));
        calculate.setStatus(CalculateStatus.CALCULATING);
        calculateRepository.save(calculate);

        try {
            calculateService.recalculate(request.getCalculateId());
            return ResponseEntity.ok(Map.of("message", "Recalculation completed successfully."));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Calculate entity not found."));
        }
    }


}



