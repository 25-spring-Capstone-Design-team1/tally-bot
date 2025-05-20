package com.tallybot.backend.tallybot_back.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SettlementDto {
    private String place;     // 결제 장소
    private Long payerId;     // 결제자
    private String item;      // 결제 항목
    private int amount;       // 결제 총액

    private List<Long> participantIds;
    private Map<String, Integer> constants;
    private Map<String, Integer> ratios;
}
