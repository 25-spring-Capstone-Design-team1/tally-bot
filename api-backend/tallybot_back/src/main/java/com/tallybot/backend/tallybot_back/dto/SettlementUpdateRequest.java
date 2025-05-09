package com.tallybot.backend.tallybot_back.dto;

import com.tallybot.backend.tallybot_back.domain.Ratio;
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
    private Map<Long, Integer> constants;  // key: memberId, value: constant
    private Map<Long, Integer> ratios;  // key: memberId, value: ratio
    private Integer sum;
}

