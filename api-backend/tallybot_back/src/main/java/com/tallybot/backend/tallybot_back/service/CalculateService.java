package com.tallybot.backend.tallybot_back.service;

import com.tallybot.backend.tallybot_back.domain.*;
import com.tallybot.backend.tallybot_back.dto.*;
import com.tallybot.backend.tallybot_back.helper.Triple;
import com.tallybot.backend.tallybot_back.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class CalculateService {

    private final GroupRepository groupRepository;
    private final CalculateRepository calculateRepository;
    private final ChatRepository chatRepository;
    private final GPTService gptService;
    private final CalculateDetailRepository calculateDetailRepository;
    private final MemberRepository memberRepository;
    private final SettlementRepository settlementRepository;
    private final SettlementService settlementService;
    private final ParticipantRepository participantRepository;

    public Long startCalculate(CalculateDto request) {
        Group group = groupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new IllegalArgumentException("그룹 없음"));

        Calculate calculate = new Calculate();
        calculate.setGroup(group);
        calculate.setStartTime(request.getStartTime());
        calculate.setEndTime(request.getEndTime());
        calculate.setStatus(CalculateStatus.PENDING);
        calculate = calculateRepository.save(calculate);

        List<Chat> chats = chatRepository.findByGroupAndTimestampBetween(
                group,
                request.getStartTime(),
                request.getEndTime()
        );

        //gpt 다루는 메소드에 데이터 넘기기
        SettlementsDto results
                = gptService.returnResults(chats, calculate);

        // 해독 결과 저장
        List<Settlement> res = settlementService.toSettlements(results, calculate.getCalculateId());
        for(Settlement sm: res) {
            sm = settlementRepository.save(sm);
            for(Participant pc: sm.getParticipants()) {
                pc.getParticipantKey().settlement = sm;
                participantRepository.save(pc);
            }
        }

        // 상태 완료 처리
        /*calculate.setStatus(CalculateStatus.COMPLETED);
        calculateRepository.save(calculate);*/

        return calculate.getCalculateId();
    }

    public List<CalculateDetail> calculateShare(List<Settlement> sm) {
        Map<Pair<Member, Member>, Integer> m = new HashMap<>();
        for (Settlement s : sm) {
            int amount = s.getAmount();
            for(Participant pc: s.getParticipants()) {
                amount -= pc.getConstant();
            }

            final int finalAmount = amount;
            for(Participant pc: s.getParticipants()) {
                Pair<Member, Member> p = Pair.of(pc.getParticipantKey().member, s.getPayer());
                m.put(p, m.getOrDefault(p, 0) + pc.getConstant() + pc.getRatio().mul(new Ratio(amount)).toInt());
            }
        }

        Calculate calculate = sm.get(0).getCalculate();

        List<CalculateDetail> lcd = new ArrayList<>();
        for(Pair<Member, Member> mem2Mem: m.keySet()) {
            lcd.add(new CalculateDetail(null, calculate, mem2Mem.getFirst(), mem2Mem.getSecond(), m.get(mem2Mem)));
        }

        return lcd;
    }

    public ResponseDetailDto resultReturn(Long calculateId) {
        Calculate calculate = calculateRepository.findById(calculateId)
                .orElseThrow(() -> new IllegalArgumentException("정산 없음"));

        List<CalculateDetail> details = calculateDetailRepository.findByCalculate(calculate);

        List<ResponseDetail> detailResponses = details.stream().map(detail ->
                new ResponseDetail(
                        detail.getPayer().getNickname(),
                        detail.getPayee().getNickname(),
                        detail.getAmount()
                )
        ).toList();

        String url = "https://tallybot.me/calculate/" + calculateId;

        return new ResponseDetailDto(url, detailResponses);
    }


    public ResponseBriefSettlementDto resultBriefSettlementReturn(Long calculateId) {
        Calculate calculate = calculateRepository.findById(calculateId)
                .orElseThrow(() -> new IllegalArgumentException("정산 없음"));

        List<Settlement> settlements = settlementRepository.findByCalculate(calculate);

        List<ResponseBriefSettlement> settlementResponses = settlements.stream().map(settlement -> {
            List<String> nicks = settlementService.nicknamesInCalculate(settlements);
            return new ResponseBriefSettlement(
                    settlement.getSettlementId(),
                    settlement.getPlace(),
                    settlement.getPayer().getNickname(),
                    nicks.size(),
                    settlement.getAmount(),
                    nicks
            );
        }).toList();

        String url = "https://tallybot.me/settlement/brief/" + calculateId;

        return new ResponseBriefSettlementDto(url, settlementResponses);
    }

    public ResponseSettlementDto resultSettlementReturn(Long calculateId) {
        Calculate calculate = calculateRepository.findById(calculateId)
                .orElseThrow(() -> new IllegalArgumentException("정산 없음"));

        List<Settlement> settlements = settlementRepository.findByCalculate(calculate);

        List<SettlementDto> settlementDtoResponses = settlements.stream().map(settlement -> {
            int denom = settlement.getParticipants().stream().map(Participant::getRatio).map(Ratio::getDenominator).reduce(1,
                    (a, b) -> a * b / Ratio.gcd(a, b));
            return new SettlementDto(
                    settlement.getPlace(),
                    settlement.getPayer().getNickname(),
                    settlement.getItem(),
                    settlement.getAmount(),
                    settlement.getParticipants().stream().map(participant -> {
                        return participant.getParticipantKey().member.getNickname();
                    }).toList(),
                    settlement.getParticipants().stream().collect(Collectors.toMap(
                            participant -> participant.getParticipantKey().member.getNickname(),
                            Participant::getConstant)),
                    settlement.getParticipants().stream().collect(Collectors.toMap(
                            participant -> ((Participant) participant).getParticipantKey().member.getNickname(),
                            participant -> Ratio.mul(((Participant) participant).getRatio(), new Ratio(denom, 1)).toInt()))
            );
        }).toList();

        String url = "https://tallybot.me/settlement/detail" + calculateId;

        return new ResponseSettlementDto(url, settlementDtoResponses);
    }
}





