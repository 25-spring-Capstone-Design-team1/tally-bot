//package com.tallybot.backend.tallybot_back.controller;
//
//import com.tallybot.backend.tallybot_back.dto.PythonRequestDto;
//import com.tallybot.backend.tallybot_back.dto.SettlementDto;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api")
//public class MockGPTController {
//
//    @PostMapping("/process")
//    public ResponseEntity<SettlementDto[]> mockGptResponse(@RequestBody PythonRequestDto request) {
//        SettlementDto mock1 = new SettlementDto(
//                "고깃집",           // place
//                4L,                // payerId
//                "삼겹살",           // item
//                30000,             // amount
//                List.of(4L, 5L),   // participantIds
//                Map.of("4", 5000, "5", 0), // constants
//                Map.of("4", 3, "5", 2)     // ratios
//        );
//
//        SettlementDto mock2 = new SettlementDto(
//                "카페",             // place
//                4L,                // payerId
//                "아메리카노",       // item
//                12000,             // amount
//                List.of(4L, 5L),   // participantIds
//                Map.of("4", 0, "5", 0),    // constants
//                Map.of("4", 1, "5", 1)     // ratios (반반)
//        );
//
//        return ResponseEntity.ok(new SettlementDto[]{mock1, mock2});
//    }
//}
