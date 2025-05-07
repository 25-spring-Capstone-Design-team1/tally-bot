package com.tallybot.backend.tallybot_back.dto;
import com.tallybot.backend.tallybot_back.domain.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SettlementsDto {
    private Group group;

    private List<SettlementDto> settlementDtos;
}
