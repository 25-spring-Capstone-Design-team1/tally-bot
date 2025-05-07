package com.tallybot.backend.tallybot_back.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class FrontResponseDto {
    private FrontGroupDto group;
    private List<FrontMemberDto> members;
    private FrontCalculateDto calculate;
    private List<FrontSettlementDto> settlements;
    private List<FrontCalculateDetailDto> calculateDetails;
}
