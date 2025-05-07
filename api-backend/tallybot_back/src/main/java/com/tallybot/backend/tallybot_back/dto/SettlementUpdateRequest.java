package com.tallybot.backend.tallybot_back.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.Map;


@Getter
@Setter
public class SettlementUpdateRequest {
    private Long calculateId;
    private Long settlementId;
    private String field;
    private Map<String, Object> newValue; // 'item', 'amount', 'payer', 'payee' 등 담는 Map
}

