package com.tallybot.backend.tallybot_back.dto;

import com.tallybot.backend.tallybot_back.domain.CalculateStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class FrontCalculateDto {
    private Long calculateId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private CalculateStatus status;
}
