package com.tallybot.backend.tallybot_back.service;

import org.springframework.data.util.Pair;
import com.tallybot.backend.tallybot_back.debtopt.Graph;
import com.tallybot.backend.tallybot_back.domain.*;
import com.tallybot.backend.tallybot_back.repository.CalculateDetailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class OptimizationService {

    private final CalculateDetailRepository calculateDetailRepository;

    public void calculateAndOptimize(List<Settlement> settlementList) {
        List<CalculateDetail> lcd = calculateShare(settlementList);
        lcd = optimize(settlementList, lcd);
        calculateDetailRepository.saveAll(lcd);
    }

//    private List<CalculateDetail> calculateShare(List<Settlement> sm) {
//        Map<Pair<Member, Member>, Integer> m = new HashMap<>();
//
//        for (Settlement s : sm) {
//            int amount = s.getAmount();
//            for (Participant pc : s.getParticipants()) {
//                amount -= pc.getConstant();
//            }
//
//            for (Participant pc : s.getParticipants()) {
//                Pair<Member, Member> p = Pair.of(s.getPayer(), pc.getParticipantKey().getMember());
//                m.put(p, m.getOrDefault(p, 0) + pc.getConstant() + pc.getRatio().mul(new Ratio(amount)).toInt());
//            }
//        }
//
//        Calculate calculate = sm.get(0).getCalculate();
//
//        List<CalculateDetail> lcd = new ArrayList<>();
//        for (Pair<Member, Member> mem2Mem : m.keySet()) {
//            lcd.add(new CalculateDetail(null, calculate, mem2Mem.getFirst(), mem2Mem.getSecond(), m.get(mem2Mem)));
//        }
//
//        return lcd;
//    }

//    private List<CalculateDetail> calculateShare(List<Settlement> sm) {
//        Map<Pair<Member, Member>, Integer> m = new HashMap<>();
//
//        for (Settlement s : sm) {
//            int totalAmount = s.getAmount();
//            int fixedAmount = s.getParticipants().stream().mapToInt(Participant::getConstant).sum();
//            int remainingAmount = totalAmount - fixedAmount;
//
//            // 전체 비율 분모 (분수들 통합)
//            int totalNumerator = s.getParticipants().stream()
//                    .mapToInt(p -> p.getRatio().getNumerator())
//                    .sum();
//
//            for (Participant pc : s.getParticipants()) {
//                Member payer = s.getPayer();
//                Member payee = pc.getParticipantKey().getMember();
//                Pair<Member, Member> p = Pair.of(payer, payee);
//
//                int constant = pc.getConstant();
//                int ratioNumerator = pc.getRatio().getNumerator();
//
//                // 비율 기반 금액 분배
//                int ratioShare = (totalNumerator == 0) ? 0 : (int) Math.round((double) ratioNumerator / totalNumerator * remainingAmount);
//
//                int totalShare = constant + ratioShare;
//
//                m.put(p, m.getOrDefault(p, 0) + totalShare);
//            }
//        }
//
//        Calculate calculate = sm.get(0).getCalculate();
//        List<CalculateDetail> lcd = new ArrayList<>();
//        for (Map.Entry<Pair<Member, Member>, Integer> entry : m.entrySet()) {
//            lcd.add(new CalculateDetail(
//                    null, calculate,
//                    entry.getKey().getFirst(),
//                    entry.getKey().getSecond(),
//                    entry.getValue()));
//        }
//
//        return lcd;
//    }

    ///ratios 잘됨, 재정산 시 tranfer 이상함
    private List<CalculateDetail> calculateShare(List<Settlement> sm) {
        Map<Pair<Member, Member>, Integer> m = new HashMap<>();

        for (Settlement s : sm) {
            int totalAmount = s.getAmount();
            int fixedAmount = s.getParticipants().stream().mapToInt(Participant::getConstant).sum();
            int remainingAmount = totalAmount - fixedAmount;

            int totalNumerator = s.getParticipants().stream()
                    .mapToInt(p -> p.getRatio().getNumerator())
                    .sum();

            for (Participant pc : s.getParticipants()) {
                Member payer = s.getPayer(); // 돈 낸 사람 (받을 사람)
                Member payee = pc.getParticipantKey().getMember(); // 참여자 (줄 사람)

                // ✅ 돈을 줘야 하는 사람 → 받은 사람
                Pair<Member, Member> p = Pair.of(payee, payer);

                int constant = pc.getConstant();
                int ratioNumerator = pc.getRatio().getNumerator();

                int ratioShare = (totalNumerator == 0) ? 0 : (int) Math.round((double) ratioNumerator / totalNumerator * remainingAmount);
                int totalShare = constant + ratioShare;

                m.put(p, m.getOrDefault(p, 0) + totalShare);
            }
        }

        Calculate calculate = sm.get(0).getCalculate();
        List<CalculateDetail> lcd = new ArrayList<>();
        for (Map.Entry<Pair<Member, Member>, Integer> entry : m.entrySet()) {
            lcd.add(new CalculateDetail(
                    null, calculate,
                    entry.getKey().getFirst(),  // payer → 돈을 주는 사람
                    entry.getKey().getSecond(), // payee → 돈을 받는 사람
                    entry.getValue()));
        }

        return lcd;
    }


//    private List<CalculateDetail> calculateShare(List<Settlement> settlementList) {
//        Map<Pair<Member, Member>, Integer> paymentMap = new HashMap<>();
//
//        for (Settlement settlement : settlementList) {
//            int totalAmount = settlement.getAmount();
//
//            // 모든 참여자 정보
//            Set<Participant> participants = settlement.getParticipants();
//
//            // 고정 금액 합
//            int totalConstant = participants.stream()
//                    .mapToInt(Participant::getConstant)
//                    .sum();
//
//            // 비율로 분배할 금액
//            int remainingAmount = totalAmount - totalConstant;
//
//            // 비율 총합 (정수 기준)
//            int totalRatioNumerator = participants.stream()
//                    .mapToInt(p -> p.getRatio().getNumerator())
//                    .sum();
//
//            for (Participant participant : participants) {
//                Member payer = settlement.getPayer();
//                Member payee = participant.getParticipantKey().getMember();
//                Pair<Member, Member> key = Pair.of(payee, payer);  // 주는 사람이 payee
//
//                int constant = participant.getConstant();
//                int numerator = participant.getRatio().getNumerator();
//
//                // 비율 기반 금액 계산 (소수점 반올림)
//                int ratioShare = (totalRatioNumerator == 0) ? 0 :
//                        (int) Math.round(((double) numerator / totalRatioNumerator) * remainingAmount);
//
//                int totalShare = constant + ratioShare;
//
//                // payer → payee 구조 (payee가 payer에게 줄 돈)
//                if (!payer.equals(payee)) {
//                    paymentMap.put(key, paymentMap.getOrDefault(key, 0) + totalShare);
//                }
//            }
//        }
//
//        Calculate calculate = settlementList.get(0).getCalculate();
//        List<CalculateDetail> result = new ArrayList<>();
//
//        for (Map.Entry<Pair<Member, Member>, Integer> entry : paymentMap.entrySet()) {
//            Member from = entry.getKey().getFirst();   // payee
//            Member to = entry.getKey().getSecond();    // payer
//            int amount = entry.getValue();
//
//            result.add(new CalculateDetail(null, calculate, from, to, amount));
//        }
//
//        return result;
//    }





    private List<CalculateDetail> optimize(List<Settlement> sm, List<CalculateDetail> lcd) {
        Set<Member> members = new HashSet<>();
        Calculate calculate = sm.get(0).getCalculate();
        for (CalculateDetail cd : lcd) {
            members.add(cd.getPayer());
            members.add(cd.getPayee());
        }

        List<Member> memberList = new ArrayList<>(members);
        Graph graph = new Graph(memberList.size());

        for (CalculateDetail cd : lcd) {
            int payerNum = memberList.indexOf(cd.getPayer());
            int payeeNum = memberList.indexOf(cd.getPayee());
            graph.addEdge(payerNum, payeeNum, cd.getAmount());
        }

        Graph graph2 = Graph.summarize(graph);
        if (graph2.getEdgeCount() < graph.getEdgeCount()) {
            graph = graph2;
        }

        List<CalculateDetail> lcd2 = new ArrayList<>();
        for (int i = 0; i < graph.getVertexCount(); i++) {
            for (Integer j : graph.getAdjacencyList().get(i).keySet()) {
                if (graph.getAdjacencyList().get(i).get(j) < 0) continue;
                lcd2.add(new CalculateDetail(null, calculate, memberList.get(i), memberList.get(j),
                        graph.getAdjacencyList().get(i).get(j)));
            }
        }

        return lcd2;
    }
}
