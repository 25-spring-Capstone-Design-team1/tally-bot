//package com.tallybot.backend.tallybot_back.controller;
//
//import com.fasterxml.jackson.annotation.JsonProperty;
//import com.tallybot.backend.tallybot_back.dto.PythonRequestDto;
//import lombok.AllArgsConstructor;
//import lombok.Data;
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
//    public ResponseEntity<MockGptResponse> mockGptResponse(@RequestBody PythonRequestDto request) {
//
//        ResultItem resultItem = new ResultItem("4", "커피", 10000, "n분의1");
//        SecondaryResultItem secondaryResultItem = new SecondaryResultItem("커피", "카페");
//
//        FinalResultItem finalResultItem = new FinalResultItem(
//                "카페",
//                "4",
//                "커피",
//                10000,
//                List.of("4", "5"),
//                Map.of("4", 0, "5", 0),
//                Map.of("4", 1, "5", 1)
//        );
//
//        MockGptResponse mockResponse = new MockGptResponse(
//                List.of(resultItem),
//                List.of(secondaryResultItem),
//                List.of(finalResultItem)
//        );
//
//        return ResponseEntity.ok(mockResponse);
//    }
//
//    // --- 내부 응답 클래스 정의 ---
//
//    @Data
//    @AllArgsConstructor
//    static class MockGptResponse {
//        private List<ResultItem> result;
//
//        @JsonProperty("secondary_result")
//        private List<SecondaryResultItem> secondaryResult;
//
//        @JsonProperty("final_result")
//        private List<FinalResultItem> finalResult;
//    }
//
//    @Data
//    @AllArgsConstructor
//    static class ResultItem {
//        private String speaker;
//        private String item;
//        private int amount;
//
//        @JsonProperty("hint_type")
//        private String hintType;
//    }
//
//    @Data
//    @AllArgsConstructor
//    static class SecondaryResultItem {
//        private String item;
//        private String place;
//    }
//
//    @Data
//    @AllArgsConstructor
//    static class FinalResultItem {
//        private String place;
//        private String payer;
//        private String item;
//        private int amount;
//        private List<String> participants;
//        private Map<String, Integer> constants;
//        private Map<String, Integer> ratios;
//    }
//}
