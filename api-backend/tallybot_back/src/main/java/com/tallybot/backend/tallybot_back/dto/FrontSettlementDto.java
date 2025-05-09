package com.tallybot.backend.tallybot_back.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FrontSettlementDto {
    private Long settlementId;
    private String place;
    private String item;
    private int amount;
    private FrontMemberDto payer;
    private List<FrontMemberDto> participants;
    private Map<String, Integer> constants;
    private Map<String, Integer> ratios;
}
