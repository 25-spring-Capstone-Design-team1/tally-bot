package com.tallybot.backend.tallybot_back.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class FrontSettlementDto {
    private Long settlementId;
    private String place;
    private String item;
    private int amount;
    private FrontMemberDto payer;
    private List<FrontMemberDto> participants;
}
