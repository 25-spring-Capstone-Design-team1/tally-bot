package com.tallybot.backend.tallybot_back.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tallybot.backend.tallybot_back.domain.*;
import com.tallybot.backend.tallybot_back.dto.SettlementsDto;
import com.tallybot.backend.tallybot_back.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class GPTService {
    private final SettlementRepository settlementRepository;
    private final GroupRepository groupRepository;
    private final MemberRepository memberRepository;
    private final ParticipantRepository participantRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public GPTService(SettlementRepository settlementRepository, GroupRepository groupRepository,
                      MemberRepository memberRepository, ParticipantRepository participantRepository, ObjectMapper objectMapper) {
        this.settlementRepository = settlementRepository;
        this.groupRepository = groupRepository;
        this.memberRepository = memberRepository;
        this.participantRepository = participantRepository;
        this.objectMapper = objectMapper;
    }

    public void saveSettlementsFromJson(String json) {
        try {
            SettlementsDto settlementsDto = objectMapper.readValue(json, SettlementsDto.class);
            saveSettlements(settlementsDto);
        } catch (JsonProcessingException e) {
            System.err.println(e.getMessage());
        }
    }

    public void saveSettlements(SettlementsDto settlementsDto) {
        Group group = groupRepository.save(settlementsDto.getGroup());

        for(SettlementsDto.SettlementDto settlementDto: settlementsDto.getSettlementDtos()) {
            Settlement settlement = new Settlement();
            settlement.setSettlementId(null);

            settlement.setPlace(settlementDto.getPlace());
            settlement.setItem(settlementDto.getItem());
            settlement.setAmount(settlementDto.getAmount());

            settlement.setGroup(group);

            var payer = memberRepository.findFirstByGroupAndNickname(group, settlementDto.getPayer())
                    .orElseGet(() -> {
                        Member member = new Member();
                        member.setNickname(settlementDto.getPayer());
                        member.setGroup(group);
                        return memberRepository.save(member);
                    });

            settlement.setPayer(payer);

            settlement = settlementRepository.save(settlement);

            int sum = 0;
            for(Integer ratio: settlementDto.getRatios().values()) {
                sum += ratio;
            }

            for(int i = 0; i < settlementDto.getParticipants().size(); i++) {
                int finalI = i;
                var mem = memberRepository.findFirstByGroupAndNickname(group, settlementDto.getParticipants().get(i))
                        .orElseGet(() -> {
                            Member member = new Member();
                            member.setNickname(settlementDto.getParticipants().get(finalI));
                            member.setGroup(group);
                            return memberRepository.save(member);
                        });
                var constant = settlementDto.getConstants().get(settlementDto.getParticipants().get(i));
                var ratio = settlementDto.getRatios().get(settlementDto.getParticipants().get(i));
                participantRepository.save(new Participant(new Participant.ParticipantKey(settlement, mem), constant, new Ratio(ratio, sum)));
            }
        }


    }
}
