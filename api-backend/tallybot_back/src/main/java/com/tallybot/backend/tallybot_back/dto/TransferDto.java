package com.tallybot.backend.tallybot_back.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TransferDto {
    private String payerNickname;
    private String payeeNickname;
    private int amount;
}
