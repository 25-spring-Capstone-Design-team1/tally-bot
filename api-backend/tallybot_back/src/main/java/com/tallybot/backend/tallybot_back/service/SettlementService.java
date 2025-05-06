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

    /*
     * 각 정산에 대하여 참여하는 사람의 Nickname을 가져온다.
     */
    public List<String> nicknamesInCalculate(List<Settlement> settlementList) {
        Stream<Settlement> settlements = settlementList.stream();

        // 각 정산의 참여자들을 Set을 이용해 겹치지 않게 합한다.
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

        // List 형태로 변환하여 반환한다.
        return members.collect(Collectors.toList());
    }

    /*
     * GPT에서 넘어오는 settlement 관련 정보를
     * settlement의 ID, Group 객체, calculate의 ID 등을 사용하여 DB의 Settlement 객체로 변환한다.
     */
    public Settlement toSettlement(SettlementDto settlementDto, Long settlementId, Group group, Long calculateId) {
        // 정보 채우기
        Settlement settlement = new Settlement();
        settlement.setSettlementId(settlementId);

        settlement.setPlace(settlementDto.getPlace());
        settlement.setItem(settlementDto.getItem());
        settlement.setAmount(settlementDto.getAmount());

        settlement.setGroup(group);

        // 닉네임으로 관여 멤버 찾기
        var payer = memberRepository.findByNicknameAndGroup(settlementDto.getPayer(), group)
                .orElseGet(() -> {
                    Member member = new Member();
                    member.setNickname(settlementDto.getPayer());
                    member.setGroup(group);
                    return memberRepository.save(member);
                });

        settlement.setPayer(payer);

        // 비율의 분모를 만들기 위해 합한다.
        int sum = 0;
        for (Integer ratio : settlementDto.getRatios().values()) {
            sum += ratio;
        }

        // 비율을 분수의 형태로, 고정금액과 함께 각 멤버로 저장, participant 테이블을 체운다.
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

        // calculate ID에 해당하는 게 있는지 찾고 없으면 null을 채운다.
        var calculate = calculateRepository.findById(calculateId)
                .orElse(null);
        settlement.setCalculate(calculate);

        return settlement;
    }

    /*
     * 여러 항목이 섞여있는 GPT로부터 오는 전체 정산 항목을 종합하여 DB의 Settlement의 List 형태로 변환한다.
     */
    public List<Settlement> toSettlements(SettlementsDto settlementsDto, Long calculateId) {
        List<Settlement> settlementList = new ArrayList<>();
        for(int i = 0; i < settlementsDto.getSettlementDtos().size(); i++) {
            settlementList.add(toSettlement(settlementsDto.getSettlementDtos().get(i),
                    null, settlementsDto.getGroup(), calculateId));
        }
        return settlementList;
    }
}
