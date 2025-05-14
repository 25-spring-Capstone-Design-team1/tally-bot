//package com.tallybot.backend.tallybot_back.controller;
//
//import com.tallybot.backend.tallybot_back.dto.*;
//import com.tallybot.backend.tallybot_back.service.*;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//@CrossOrigin(origins = "http://localhost:9002")
//@RestController
//@RequiredArgsConstructor
//@RequestMapping("/api/front")
//public class FrontController {
//
//    private final FrontService frontService;
//    private final CalculateService calculateService;
//    private final SettlementService settlementService;
//
//    @GetMapping("/{calculateId}/full-result")
//    public ResponseEntity<FrontResponseDto> getFullResult(@PathVariable Long calculateId) {
//        FrontResponseDto response = frontService.getFullResult(calculateId);
//        return ResponseEntity.ok(response);
//    }
//
//    @PostMapping("/{calculateId}/recalculate")
//    public ResponseEntity<String> recalculate(@PathVariable Long calculateId) {
//        calculateService.recalculate(calculateId);
//        return ResponseEntity.ok("재정산 완료");
//    }
//
//    @PostMapping("/{calculateId}/complete")
//    public ResponseEntity<String> markAsCompleted(@PathVariable Long calculateId) {
//        frontService.markAsCompleted(calculateId);
//        return ResponseEntity.ok("정산 완료 상태로 변경됨");
//    }
//
//    @PostMapping("/update")
//    public ResponseEntity<String> updateSettlement(@RequestBody SettlementUpdateRequest request) {
//        settlementService.applySettlementUpdate(request);
//        return ResponseEntity.ok("정산 내역 업데이트 완료");
//    }
//
//}
