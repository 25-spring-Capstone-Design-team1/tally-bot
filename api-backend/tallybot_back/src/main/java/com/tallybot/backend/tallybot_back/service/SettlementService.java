package com.tallybot.backend.tallybot_back.service;

import com.tallybot.backend.tallybot_back.domain.*;
import com.tallybot.backend.tallybot_back.dto.*;
import com.tallybot.backend.tallybot_back.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Map;


import java.util.List;

@Service
@RequiredArgsConstructor
public class SettlementService {

    private final SettlementRepository settlementRepository;
    private final MemberRepository memberRepository;
    private final CalculateRepository calculateRepository;

    @Transactional
    public void applySettlementUpdate(SettlementUpdateRequest request) {
        //settlement add인 경우
        if ("add".equals(request.getField())) {
            Map<String, Object> newValueMap = (Map<String, Object>) request.getNewValue();

            Settlement newSettlement = new Settlement();
            newSettlement.setItem((String) newValueMap.get("item"));
            newSettlement.setAmount((Integer) newValueMap.get("amount"));

            String payerNickname = (String) newValueMap.get("payer");
            Member payer = memberRepository.findByNickname(payerNickname)
                    .orElseThrow(() -> new IllegalArgumentException("결제자 없음"));
            newSettlement.setPayer(payer);

            Object rawPayee = newValueMap.get("payee");
            List<String> participantNicknames = new ArrayList<>();
            if (rawPayee instanceof List<?>) {
                for (Object o : (List<?>) rawPayee) {
                    if (o instanceof String s) {
                        participantNicknames.add(s);
                    } else {
                        throw new IllegalArgumentException("payee 항목은 문자열이어야 합니다.");
                    }
                }
            } else {
                throw new IllegalArgumentException("payee 값이 리스트가 아닙니다.");
            }

            List<Member> participants = memberRepository.findByNicknameIn(participantNicknames);
            newSettlement.setParticipants(participants);

            Group group = payer.getGroup();
            newSettlement.setGroup(group);

            Calculate calculate = calculateRepository.findById(request.getCalculateId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 정산 ID 없음"));
            newSettlement.setCalculate(calculate);

            settlementRepository.save(newSettlement);
            return;
        }

        // settlement add가 아닌 경우
        Settlement settlement = settlementRepository.findById(request.getSettlementId())
                .orElseThrow(() -> new IllegalArgumentException("해당 정산 내역이 존재하지 않음"));

        switch (request.getField()) {
            case "item" -> {
                Object raw = request.getNewValue();
                if (raw instanceof String item) {
                    settlement.setItem(item);
                } else {
                    throw new IllegalArgumentException("item 값은 문자열이어야 합니다.");
                }
            }
            case "amount" -> {
                Integer amount;
                Object rawAmount = request.getNewValue();
                if (rawAmount instanceof Integer i) {
                    amount = i;
                } else if (rawAmount instanceof String s) {
                    amount = Integer.parseInt(s);
                } else {
                    throw new IllegalArgumentException("amount 값이 유효하지 않음");
                }
                settlement.setAmount(amount);
            }
            case "payer" -> {
                Object raw = request.getNewValue();
                if (raw instanceof String payer) {
                    settlement.setItem(payer);
                } else {
                    throw new IllegalArgumentException("payer 값은 문자열이어야 합니다.");
                }
            }
            case "payee" -> {
                Object rawList = request.getNewValue();
                List<String> nicknames = new ArrayList<>();
                if (rawList instanceof List<?>) {
                    for (Object o : (List<?>) rawList) {
                        if (o instanceof String s) {
                            nicknames.add(s);
                        }
                    }
                }
                List<Member> members = memberRepository.findByNicknameIn(nicknames);
                settlement.setParticipants(members);
            }
            case "delete" -> {
                settlementRepository.delete(settlement);
                return;
            }
            default -> throw new IllegalArgumentException("알 수 없는 수정항목");
        }

        settlementRepository.save(settlement);
    }


}
