package com.tallybot.backend.tallybot_back.dto;
import com.tallybot.backend.tallybot_back.domain.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SettlementsDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SettlementDto {
        private String place;     // 결제 장소
        private String payer;     // 결제자
        private String item;      // 결제 항목
        private int amount;       // 결제 총액

        private List<String> participants;
        private Map<String, Integer> constants;
        private Map<String, Integer> ratios;
    }

    private Group group;

    private List<SettlementDto> settlementDtos;

    public static List<Participant> toParticipants(Group group, SettlementDto settlementDto) {
        List<Participant> participants = new ArrayList<>();
        var constants = settlementDto.getConstants();
        var ratios = settlementDto.getRatios();

        int sum = 0;
        for(Integer i : constants.values()) {
            sum += i;
        }

        for(int i = 0; i < settlementDto.getParticipants().size(); i++) {
            String name = settlementDto.getParticipants().get(i);
            Participant participant = new Participant();
            participant.setParticipantKey(new Participant.ParticipantKey(null, new Member(null, name, group)));
            participant.setConstant(constants.get(name));
            participant.setRatio(new Ratio(ratios.get(name), sum));
            participants.add(participant);
        }

        return participants;
    }
}
