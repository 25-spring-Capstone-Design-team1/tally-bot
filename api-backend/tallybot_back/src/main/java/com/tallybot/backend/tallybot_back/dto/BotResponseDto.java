package com.tallybot.backend.tallybot_back.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class BotResponseDto {
    private String groupUrl;
    private String calculateUrl;
    private List<TransferDto> transfers;
}
