package com.tallybot.backend.tallybot_back.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SettlementDto {
    private String place;

    private String payer;

    private String item;

    private int amount;

    @JsonProperty("participants")
    private List<String> participants;

    private Map<String, Integer> constants;
    private Map<String, Integer> ratios;
}
