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

    private List<CalculateDetail> calculateShare(List<Settlement> sm) {
        Map<Pair<Member, Member>, Integer> m = new HashMap<>();

        for (Settlement s : sm) {
            int amount = s.getAmount();
            for (Participant pc : s.getParticipants()) {
                amount -= pc.getConstant();
            }

            for (Participant pc : s.getParticipants()) {
                Pair<Member, Member> p = Pair.of(s.getPayer(), pc.getParticipantKey().getMember());
                m.put(p, m.getOrDefault(p, 0) + pc.getConstant() + pc.getRatio().mul(new Ratio(amount)).toInt());
            }
        }

        Calculate calculate = sm.get(0).getCalculate();

        List<CalculateDetail> lcd = new ArrayList<>();
        for (Pair<Member, Member> mem2Mem : m.keySet()) {
            lcd.add(new CalculateDetail(null, calculate, mem2Mem.getFirst(), mem2Mem.getSecond(), m.get(mem2Mem)));
        }

        return lcd;
    }

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
