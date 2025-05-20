package com.tallybot.backend.tallybot_back.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class TransferListDto {
    private int transferCount;
    private List<TransferDto> transfers;
}
