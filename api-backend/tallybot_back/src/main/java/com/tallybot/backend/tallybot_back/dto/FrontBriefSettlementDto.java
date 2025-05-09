package com.tallybot.backend.tallybot_back.dto;

import lombok.*;

import java.util.List;

@Getter
@AllArgsConstructor
public class FrontBriefSettlementDto {
    private String url;
    private List<FrontSettlementDto> objects;
}
