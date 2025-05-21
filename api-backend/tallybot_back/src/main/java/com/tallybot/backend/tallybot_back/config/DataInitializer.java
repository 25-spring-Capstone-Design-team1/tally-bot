package com.tallybot.backend.tallybot_back.config;

import com.tallybot.backend.tallybot_back.domain.*;
import com.tallybot.backend.tallybot_back.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final GroupRepository groupRepository;
    private final MemberRepository memberRepository;
    private final CalculateRepository calculateRepository;
    private final SettlementRepository settlementRepository;
    private final ParticipantRepository participantRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (groupRepository.count() > 0) return; // 이미 데이터가 있다면 skip

        // 1. 그룹 생성
        UserGroup userGroup = new UserGroup();
        userGroup.setGroupName("치킨모임");
        groupRepository.save(userGroup);

        // 2. 멤버 생성
        Member member1 = new Member(null, "지우", userGroup);
        Member member2 = new Member(null, "현우", userGroup);
        Member member3 = new Member(null, "은비", userGroup);
        memberRepository.saveAll(Set.of(member1, member2, member3));

        // 3. 정산 생성
        Calculate calculate = new Calculate();
        calculate.setUserGroup(userGroup);
        calculate.setStartTime(LocalDateTime.now().minusHours(1));
        calculate.setEndTime(LocalDateTime.now());
        calculate.setStatus(CalculateStatus.COMPLETED);
        calculateRepository.save(calculate);

        // 4. 결제내역 생성
        Settlement settlement = new Settlement();
        settlement.setUserGroup(userGroup);
        settlement.setPayer(member1);
        settlement.setPlace("BBQ");
        settlement.setItem("황금올리브");
        settlement.setAmount(30000);
        settlement.setCalculate(calculate);
        settlementRepository.save(settlement);

        // 5. 참여자 생성
        Participant p1 = new Participant();
        Participant.ParticipantKey pk1 = new Participant.ParticipantKey(settlement, member1);
        p1.setParticipantKey(pk1);
        p1.setConstant(10000);
        p1.setRatio(new Ratio(1));
        participantRepository.save(p1);

        Participant p2 = new Participant();
        Participant.ParticipantKey pk2 = new Participant.ParticipantKey(settlement, member2);
        p2.setParticipantKey(pk2);
        p2.setConstant(10000);
        p2.setRatio(new Ratio(1));
        participantRepository.save(p2);

        Participant p3 = new Participant();
        Participant.ParticipantKey pk3 = new Participant.ParticipantKey(settlement, member3);
        p3.setParticipantKey(pk3);
        p3.setConstant(10000);
        p3.setRatio(new Ratio(1));
        participantRepository.save(p3);

        System.out.println("✅ Mock 데이터가 성공적으로 생성되었습니다.");
    }
}
