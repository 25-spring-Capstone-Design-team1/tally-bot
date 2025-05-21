package com.tallybot.backend.tallybot_back.dto;

import lombok.*;

import java.util.List;

@Getter
@AllArgsConstructor
public class FrontSettlementListDto {
    private Integer settlementCount;
    private List<FrontSettlementDto> settlements;
}

