//package com.tallybot.backend.tallybot_back.repository;
//
//import com.tallybot.backend.tallybot_back.domain.*;
//import config.DatabaseTestBase;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.annotation.DirtiesContext;
//import org.springframework.test.annotation.Rollback;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@SpringBootTest
//@ActiveProfiles("test")
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
//@Transactional
//@Rollback
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
//public class SettlementRepositoryTest extends DatabaseTestBase {
//
//    @Autowired
//    private GroupRepository groupRepository;
//
//    @Autowired
//    private MemberRepository memberRepository;
//
//    @Autowired
//    private CalculateRepository calculateRepository;
//
//    @Autowired
//    private SettlementRepository settlementRepository;
//
//    @BeforeEach
//    void setUp() {
//    }
//
//    @Test
//    @DisplayName("Settlement 검증")
//    public void saveAndTestSettlement() {
//        // given
//
//        List<UserGroup> userGroups = new ArrayList<>();
//        List<List<Member>> userGroupMembers = new ArrayList<>();
//
//        for(int i = 0; i < 2; i++) {
//            userGroups.add(null);
//            userGroupMembers.add(new ArrayList<>());
//        }
//
//        UserGroup group = UserGroup.builder()
//                .groupId(8810L)
//                .groupName("새로운 톡방")
//                .build();
//
//        userGroups.set(0, groupRepository.save(group));
//
//        group = UserGroup.builder()
//                .groupId(4042L)
//                .groupName("2019 17기 Withme")
//                .build();
//
//        userGroups.set(1, groupRepository.save(group));
//
//        Member member = Member.builder()
//                .memberId(null)
//                .userGroup(userGroups.get(0))
//                .nickname("철수")
//                .build();
//
//        userGroupMembers.get(0).add(memberRepository.save(member));
//
//        member = Member.builder()
//                .memberId(null)
//                .userGroup(userGroups.get(0))
//                .nickname("영희")
//                .build();
//
//        userGroupMembers.get(0).add(memberRepository.save(member));
//
//        member = Member.builder()
//                .memberId(null)
//                .userGroup(userGroups.get(1))
//                .nickname("철수")
//                .build();
//
//        userGroupMembers.get(1).add(memberRepository.save(member));
//
//        member = Member.builder()
//                .memberId(null)
//                .userGroup(userGroups.get(1))
//                .nickname("민경")
//                .build();
//
//        userGroupMembers.get(1).add(memberRepository.save(member));
//
//        member = Member.builder()
//                .memberId(null)
//                .userGroup(userGroups.get(1))
//                .nickname("정현")
//                .build();
//
//        userGroupMembers.get(1).add(memberRepository.save(member));
//
//        LocalDateTime startTime = LocalDateTime.of(2024, 11, 8, 13, 0);
//        LocalDateTime endTime = LocalDateTime.of(2024, 11, 9, 13, 0);
//
//        Calculate cal0 = Calculate.builder()
//                .calculateId(null)
//                .startTime(startTime)
//                .endTime(endTime)
//                .status(CalculateStatus.CALCULATING)
//                .userGroup(userGroups.get(0))
//                .build();
//        calculateRepository.save(cal0);
//
//        Calculate cal1 = Calculate.builder()
//                .calculateId(null)
//                .startTime(startTime)
//                .endTime(endTime)
//                .status(CalculateStatus.PENDING)
//                .userGroup(userGroups.get(1))
//                .build();
//        calculateRepository.save(cal1);
//
//        Settlement settlement0 = Settlement.builder()
//                .settlementId(null)
//                .place("스타벅스")
//                .item("오늘의 커피")
//                .amount(30000)
//                .userGroup(userGroups.get(0))
//                .payer(userGroupMembers.get(0).get(1))
//                .calculate(cal0)
//                .build();
//        settlementRepository.save(settlement0);
//
//        Settlement settlement1 = Settlement.builder()
//                .settlementId(null)
//                .place("교보문고")
//                .item("잡지")
//                .amount(16000)
//                .userGroup(userGroups.get(0))
//                .payer(userGroupMembers.get(0).get(0))
//                .calculate(cal0)
//                .build();
//        settlementRepository.save(settlement1);
//
//        Settlement settlement2 = Settlement.builder()
//                .settlementId(null)
//                .place("창화당")
//                .item("만두")
//                .amount(20000)
//                .userGroup(userGroups.get(0))
//                .payer(userGroupMembers.get(0).get(0))
//                .calculate(cal0)
//                .build();
//        settlementRepository.save(settlement2);
//
//        Settlement settlement3 = Settlement.builder()
//                .settlementId(null)
//                .place("숙소")
//                .item("숙소")
//                .amount(1160000)
//                .userGroup(userGroups.get(1))
//                .payer(userGroupMembers.get(1).get(2))
//                .calculate(cal1)
//                .build();
//        settlementRepository.save(settlement3);
//
//        Settlement settlement4 = Settlement.builder()
//                .settlementId(null)
//                .place("식재료")
//                .item("식재료")
//                .amount(55000)
//                .userGroup(userGroups.get(1))
//                .payer(userGroupMembers.get(1).get(1))
//                .calculate(cal1)
//                .build();
//        settlementRepository.save(settlement4);
//
//        // when
//        List<Settlement> listCal00 = settlementRepository.findByCalculate(cal0);
//        List<Settlement> listCal01 = settlementRepository.findByCalculate(cal1);
//
//        Optional<Settlement> set00 = settlementRepository.findById(settlement0.getSettlementId());
//        Optional<Settlement> set01 = settlementRepository.findById(settlement1.getSettlementId());
//        Optional<Settlement> set02 = settlementRepository.findById(settlement2.getSettlementId());
//        Optional<Settlement> set03 = settlementRepository.findById(settlement3.getSettlementId());
//        Optional<Settlement> set04 = settlementRepository.findById(settlement4.getSettlementId());
//
//        settlementRepository.delete(settlement0);
//        settlementRepository.delete(settlement1);
//
//        List<Settlement> listCal10 = settlementRepository.findByCalculate(cal0);
//        List<Settlement> listCal11 = settlementRepository.findByCalculate(cal1);
//
//        Optional<Settlement> set10 = settlementRepository.findById(settlement0.getSettlementId());
//        Optional<Settlement> set11 = settlementRepository.findById(settlement1.getSettlementId());
//        Optional<Settlement> set12 = settlementRepository.findById(settlement2.getSettlementId());
//        Optional<Settlement> set13 = settlementRepository.findById(settlement3.getSettlementId());
//        Optional<Settlement> set14 = settlementRepository.findById(settlement4.getSettlementId());
//
//        // then
//        assertThat(listCal00).isEqualTo(List.of(settlement0, settlement1, settlement2));
//        assertThat(listCal01).isEqualTo(List.of(settlement3, settlement4));
//
//        assertThat(set00).isNotEmpty().get().isEqualTo(settlement0);
//        assertThat(set01).isNotEmpty().get().isEqualTo(settlement1);
//        assertThat(set02).isNotEmpty().get().isEqualTo(settlement2);
//        assertThat(set03).isNotEmpty().get().isEqualTo(settlement3);
//        assertThat(set04).isNotEmpty().get().isEqualTo(settlement4);
//
//        assertThat(listCal10).isEqualTo(List.of(settlement2));
//        assertThat(listCal11).isEqualTo(List.of(settlement3, settlement4));
//
//        assertThat(set10).isEmpty();
//        assertThat(set11).isEmpty();
//        assertThat(set12).isNotEmpty().get().isEqualTo(settlement2);
//        assertThat(set13).isNotEmpty().get().isEqualTo(settlement3);
//        assertThat(set14).isNotEmpty().get().isEqualTo(settlement4);
//    }
//
//}
