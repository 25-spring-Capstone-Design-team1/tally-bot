package com.tallybot.backend.tallybot_back.dto;

import com.tallybot.backend.tallybot_back.domain.Ratio;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Map;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SettlementUpdateRequest {
    private Long calculateId;
    private Long settlementId;
    private String field;
    private Map<String, Object> newValue; // 'place', 'item', 'amount', 'payer', 'participants' 등 담는 Map
    private Map<String, Integer> constants;  // key: memberId, value: constant
    private Map<String, Integer> ratios;  // key: memberId, value: ratio
    private Integer sum;
}

