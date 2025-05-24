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


//    @Override
//    @Transactional
//    public void run(String... args) {
//        if (groupRepository.count() > 0) return; // 이미 데이터가 있다면 skip
//
//        // 1. 그룹 생성
//        UserGroup userGroup = new UserGroup();
//        userGroup.setGroupName("치킨모임");
//        groupRepository.save(userGroup);
//
//        // 2. 멤버 생성
//        Member member1 = new Member(null, "지우", userGroup);
//        Member member2 = new Member(null, "현우", userGroup);
//        Member member3 = new Member(null, "은비", userGroup);
//        memberRepository.saveAll(Set.of(member1, member2, member3));
//
//        // 3. 정산 생성
//        Calculate calculate = new Calculate();
//        calculate.setUserGroup(userGroup);
//        calculate.setStartTime(LocalDateTime.now().minusHours(1));
//        calculate.setEndTime(LocalDateTime.now());
//        calculate.setStatus(CalculateStatus.COMPLETED);
//        calculateRepository.save(calculate);
//
//        // 4. 결제내역 생성
//        Settlement settlement = new Settlement();
//        settlement.setUserGroup(userGroup);
//        settlement.setPayer(member1);
//        settlement.setPlace("BBQ");
//        settlement.setItem("황금올리브");
//        settlement.setAmount(30000);
//        settlement.setCalculate(calculate);
//        settlementRepository.save(settlement);
//
//        // 5. 참여자 생성
//        Participant p1 = new Participant();
//        Participant.ParticipantKey pk1 = new Participant.ParticipantKey(settlement, member1);
//        p1.setParticipantKey(pk1);
//        p1.setConstant(10000);
//        p1.setRatio(new Ratio(1));
//        participantRepository.save(p1);
//
//        Participant p2 = new Participant();
//        Participant.ParticipantKey pk2 = new Participant.ParticipantKey(settlement, member2);
//        p2.setParticipantKey(pk2);
//        p2.setConstant(10000);
//        p2.setRatio(new Ratio(1));
//        participantRepository.save(p2);
//
//        Participant p3 = new Participant();
//        Participant.ParticipantKey pk3 = new Participant.ParticipantKey(settlement, member3);
//        p3.setParticipantKey(pk3);
//        p3.setConstant(10000);
//        p3.setRatio(new Ratio(1));
//        participantRepository.save(p3);
//
//        // CalculateDetail mock data
//        CalculateDetail calculateDetail = new CalculateDetail();
//        calculateDetail.setCalculate(calculate); // 위에서 만든 calculate 객체 사용
//        calculateDetail.setPayer(member2);       // 현우가
//        calculateDetail.setPayee(member1);       // 지우에게
//        calculateDetail.setAmount(10000);        // 10,000원 정산
//        calculateDetailRepository.save(calculateDetail);
//
//        System.out.println("✅ Mock 데이터가 성공적으로 생성되었습니다.");
//    }

//    @Override
//    @Transactional
//    public void run(String... args) {
//        if (groupRepository.count() > 0) return; // 이미 데이터가 있다면 skip
//
//        createMockData("치킨모임", "지우", "현우", "은비", "BBQ", "황금올리브", 30000, member1 -> List.of(10000, 10000, 10000), 10000);
//        createMockData("피자파티", "민수", "정아", "태현", "피자스쿨", "불고기피자", 27000, member1 -> List.of(9000, 9000, 9000), 9000);
//        createMockData("삼겹살회식", "영희", "철수", "미나", "고기천국", "삼겹살세트", 45000, member1 -> List.of(15000, 15000, 15000), 15000);
//        createMockData("분식왕국", "하늘", "주연", "동현", "분식나라", "떡볶이세트", 24000, member1 -> List.of(8000, 8000, 8000), 8000);
//
//        System.out.println("✅ 여러 개의 Mock 데이터가 성공적으로 생성되었습니다.");
//    }
//
//    private void createMockData(
//            String groupName,
//            String memberName1,
//            String memberName2,
//            String memberName3,
//            String place,
//            String item,
//            int totalAmount,
//            java.util.function.Function<Member, List<Integer>> constantsFunction,
//            int detailAmount
//    ) {
//        // 1. 그룹 생성
//        UserGroup group = new UserGroup();
//        group.setGroupName(groupName);
//        groupRepository.save(group);
//
//        // 2. 멤버 생성
//        Member member1 = new Member(null, memberName1, group);
//        Member member2 = new Member(null, memberName2, group);
//        Member member3 = new Member(null, memberName3, group);
//        memberRepository.saveAll(Set.of(member1, member2, member3));
//
//        // 3. 정산 생성
//        Calculate calculate = new Calculate();
//        calculate.setUserGroup(group);
//        calculate.setStartTime(LocalDateTime.now().minusHours(2));
//        calculate.setEndTime(LocalDateTime.now());
//        calculate.setStatus(CalculateStatus.COMPLETED);
//        calculateRepository.save(calculate);
//
//        // 4. 결제내역 생성
//        Settlement settlement = new Settlement();
//        settlement.setUserGroup(group);
//        settlement.setPayer(member1);
//        settlement.setPlace(place);
//        settlement.setItem(item);
//        settlement.setAmount(totalAmount);
//        settlement.setCalculate(calculate);
//        settlementRepository.save(settlement);
//
//        // 5. 참여자 생성
//        List<Member> members = List.of(member1, member2, member3);
//        List<Integer> constants = constantsFunction.apply(member1);
//
//        for (int i = 0; i < members.size(); i++) {
//            Participant p = new Participant();
//            p.setParticipantKey(new Participant.ParticipantKey(settlement, members.get(i)));
//            p.setConstant(constants.get(i));
//            p.setRatio(new Ratio(1));
//            participantRepository.save(p);
//        }
//
//        // 6. 정산 내역 생성
//        CalculateDetail calculateDetail = new CalculateDetail();
//        calculateDetail.setCalculate(calculate);
//        calculateDetail.setPayer(member2); // 임의로 member2가 지불
//        calculateDetail.setPayee(member1);
//        calculateDetail.setAmount(detailAmount);
//        calculateDetailRepository.save(calculateDetail);
//    }

    @Override
    @Transactional
    public void run(String... args) {
        if (groupRepository.count() > 0) return; // 이미 데이터가 있다면 skip

        // 1. 그룹 생성
        UserGroup group = new UserGroup();
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

        System.out.println("✅ 1개의 그룹에 3개의 정산(Calculate)와 각 정산에 3개의 Settlement가 생성되었습니다.");
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
            settlement.setPayer(members.get(i % 3));
            settlement.setPlace(label + "_Place" + (i + 1));
            settlement.setItem(label + "_Item" + (i + 1));
            settlement.setAmount(30000 + i * 5000);
            settlement.setCalculate(calculate);
            settlementRepository.save(settlement);

            // Participant 3명 참여 (상수값 고정, ratio 1)
            for (Member member : members) {
                Participant participant = new Participant();
                Participant.ParticipantKey key = new Participant.ParticipantKey(settlement, member);
                participant.setParticipantKey(key);
                participant.setConstant(10000);
                participant.setRatio(new Ratio(1));
                participantRepository.save(participant);
            }
        }

        // CalculateDetail 1개 생성
        CalculateDetail detail = new CalculateDetail();
        detail.setCalculate(calculate);
        detail.setPayer(members.get(1)); // 현우가
        detail.setPayee(members.get(0)); // 지우에게
        detail.setAmount(10000);
        calculateDetailRepository.save(detail);
    }


}
