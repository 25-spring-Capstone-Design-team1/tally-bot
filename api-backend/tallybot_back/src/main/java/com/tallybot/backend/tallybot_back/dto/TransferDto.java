package com.tallybot.backend.tallybot_back.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
@Getter
@AllArgsConstructor
public class TransferDto {
    private Long payerId;
    private Long payeeId;
    private int amount;
}
