package com.tallybot.backend.tallybot_back.service;

import com.tallybot.backend.tallybot_back.domain.*;
import com.tallybot.backend.tallybot_back.dto.ResponseBriefSettlement;
import com.tallybot.backend.tallybot_back.dto.ResponseBriefSettlementDto;
import com.tallybot.backend.tallybot_back.dto.SettlementDto;
import com.tallybot.backend.tallybot_back.dto.SettlementsDto;
import com.tallybot.backend.tallybot_back.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional
@RequiredArgsConstructor
public class SettlementService {
    private final SettlementRepository settlementRepository;
    private final GroupRepository groupRepository;
    private final MemberRepository memberRepository;
    private final ParticipantRepository participantRepository;
    private final CalculateRepository calculateRepository;

    public List<String> nicknamesInCalculate(List<Settlement> settlementList) {
        Stream<Settlement> settlements = settlementList.stream();

        Stream<String> members = settlements.flatMap(settlement -> {
            Long payer = settlement.getPayer().getMemberId();
            Set<Long> payeeIds = settlement.getParticipants().stream().map(participant
                    -> participant.getParticipantKey().member.getMemberId()).collect(Collectors.toSet());
            payeeIds.add(payer);
            return payeeIds.stream();
        }).map(id -> {
            return memberRepository.findById(id).orElseThrow(
                    () -> new IllegalArgumentException("해당자 없음"));
        }).map(Member::getNickname);

        return members.collect(Collectors.toList());
    }

    public Settlement toSettlement(SettlementDto settlementDto, Long settlementId, Group group, Long calculateId) {
        Settlement settlement = new Settlement();
        settlement.setSettlementId(settlementId);

        settlement.setPlace(settlementDto.getPlace());
        settlement.setItem(settlementDto.getItem());
        settlement.setAmount(settlementDto.getAmount());

        settlement.setGroup(group);

        var payer = memberRepository.findByNicknameAndGroup(settlementDto.getPayer(), group)
                .orElseGet(() -> {
                    Member member = new Member();
                    member.setNickname(settlementDto.getPayer());
                    member.setGroup(group);
                    return memberRepository.save(member);
                });

        settlement.setPayer(payer);

        int sum = 0;
        for (Integer ratio : settlementDto.getRatios().values()) {
            sum += ratio;
        }

        Set<Participant> participants = new HashSet<>();
        for (int i = 0; i < settlementDto.getParticipants().size(); i++) {
            int finalI = i;
            var mem = memberRepository.findByNicknameAndGroup(settlementDto.getParticipants().get(i), group)
                    .orElseGet(() -> {
                        Member member = new Member();
                        member.setNickname(settlementDto.getParticipants().get(finalI));
                        member.setGroup(group);
                        return memberRepository.save(member);
                    });
            var constant = settlementDto.getConstants().get(settlementDto.getParticipants().get(i));
            var ratio = settlementDto.getRatios().get(settlementDto.getParticipants().get(i));
            participants.add(new Participant(new Participant.ParticipantKey(settlement, mem), constant, new Ratio(ratio, sum)));
        }

        settlement.setParticipants(participants);

        var calculate = calculateRepository.findById(calculateId)
                .orElse(null);
        settlement.setCalculate(calculate);

        return settlement;
    }

    public List<Settlement> toSettlements(SettlementsDto settlementsDto, Long calculateId) {
        List<Settlement> settlementList = new ArrayList<>();
        for(int i = 0; i < settlementsDto.getSettlementDtos().size(); i++) {
            settlementList.add(toSettlement(settlementsDto.getSettlementDtos().get(i),
                    null, settlementsDto.getGroup(), calculateId));
        }
        return settlementList;
    }
}
