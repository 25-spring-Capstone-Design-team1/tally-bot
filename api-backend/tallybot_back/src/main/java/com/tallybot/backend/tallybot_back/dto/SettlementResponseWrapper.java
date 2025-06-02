package com.tallybot.backend.tallybot_back.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class SettlementResponseWrapper {

    @JsonProperty("final_result")
    private List<SettlementDto> finalResult;

    public List<SettlementDto> getFinalResult() {
        return finalResult;
    }

    public void setFinalResult(List<SettlementDto> finalResult) {
        this.finalResult = finalResult;
    }
}
