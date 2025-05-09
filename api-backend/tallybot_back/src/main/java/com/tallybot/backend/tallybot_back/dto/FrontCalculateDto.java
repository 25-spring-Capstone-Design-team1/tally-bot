package com.tallybot.backend.tallybot_back.dto;

import com.tallybot.backend.tallybot_back.domain.CalculateStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FrontCalculateDto {
    private Long calculateId;
    private String startTime;
    private String endTime;
    private CalculateStatus status;
}
