package com.tallybot.backend.tallybot_back.config;

import com.tallybot.backend.tallybot_back.domain.*;
import com.tallybot.backend.tallybot_back.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final GroupRepository groupRepository;
    private final MemberRepository memberRepository;
    private final CalculateRepository calculateRepository;
    private final SettlementRepository settlementRepository;
    private final ParticipantRepository participantRepository;
    private final CalculateDetailRepository calculateDetailRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (groupRepository.count() > 0) return;

        // 1. 그룹 생성 (groupId 수동 지정)
        UserGroup group = new UserGroup();
        group.setGroupId(1L);
        group.setGroupName("치킨모임");
        groupRepository.save(group);

        // 2. 멤버 생성
        Member m1 = new Member(null, "지우", group);
        Member m2 = new Member(null, "현우", group);
        Member m3 = new Member(null, "은비", group);
        memberRepository.saveAll(Set.of(m1, m2, m3));

        List<Member> members = List.of(m1, m2, m3);

        // 3개의 Calculate 생성
        createCalculateWithSettlements(group, members, CalculateStatus.COMPLETED, "COMPLETED");
        createCalculateWithSettlements(group, members, CalculateStatus.PENDING, "PENDING");
        createCalculateWithSettlements(group, members, CalculateStatus.CALCULATING, "CALCULATING");

        System.out.println("✅ Mock 데이터가 성공적으로 생성되었습니다.");
    }

    private void createCalculateWithSettlements(UserGroup group, List<Member> members, CalculateStatus status, String label) {
        // Calculate 생성
        Calculate calculate = new Calculate();
        calculate.setUserGroup(group);
        calculate.setStartTime(LocalDateTime.now().minusHours(2));
        calculate.setEndTime(LocalDateTime.now());
        calculate.setStatus(status);
        calculateRepository.save(calculate);

        // Settlement 3개 생성
        for (int i = 0; i < 3; i++) {
            Settlement settlement = new Settlement();
            settlement.setUserGroup(group);
            settlement.setPayer(members.get(i % members.size()));
            settlement.setPlace(label + "_Place" + (i + 1));
            settlement.setItem(label + "_Item" + (i + 1));
            settlement.setAmount(30000 + i * 5000);
            settlement.setCalculate(calculate);
            settlementRepository.save(settlement);

            // Participant 3명 등록
            for (Member member : members) {
                Participant participant = new Participant();
                Participant.ParticipantKey key = new Participant.ParticipantKey(settlement, member);
                participant.setParticipantKey(key);
                participant.setConstant(10000);
                participant.setRatio(new Ratio(1));
                participantRepository.save(participant);
            }
        }

        // CalculateDetail 생성
        CalculateDetail detail = new CalculateDetail();
        detail.setCalculate(calculate);
        detail.setPayer(members.get(1)); // 현우
        detail.setPayee(members.get(0)); // 지우
        detail.setAmount(10000);
        calculateDetailRepository.save(detail);
    }
}
