package com.tallybot.backend.tallybot_back.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ResponseBriefSettlement {
    private Long settlementId;
    private String place;
    private String payerNickname;
    private int memberNum;
    private int amount;
    private List<String> nicknames;
}
