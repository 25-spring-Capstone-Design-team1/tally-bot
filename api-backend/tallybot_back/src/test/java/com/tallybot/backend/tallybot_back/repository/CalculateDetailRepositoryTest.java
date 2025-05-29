package com.tallybot.backend.tallybot_back.repository;

import com.tallybot.backend.tallybot_back.domain.*;
import config.DatabaseTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// 실제 테스트
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@Rollback
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CalculateDetailRepositoryTest extends DatabaseTestBase {

    @Autowired
    private CalculateDetailRepository calculateDetailRepository;

    @Autowired
    private CalculateRepository calculateRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private GroupRepository groupRepository;

    @BeforeEach
    void setUp() {
        calculateDetailRepository.deleteAll();
        calculateRepository.deleteAll();
        memberRepository.deleteAll();
        groupRepository.deleteAll();
    }

    @Test
    @DisplayName("calculateDetail 조회")
    void saveAndCaculateDetailQuery() {
        // given
        List<UserGroup> userGroups = new ArrayList<>();
        List<List<Member>> userGroupMembers = new ArrayList<>();
        for(int i = 0; i < 2; i++) {
            userGroups.add(null);
            userGroupMembers.add(new ArrayList<>());
        }

        UserGroup group = UserGroup.builder()
                        .groupId(8810L)
                        .groupName("새로운 톡방")
                        .build();

        userGroups.set(0, groupRepository.save(group));

        group = UserGroup.builder()
                .groupId(4042L)
                .groupName("2019 17기 Withme")
                .build();

        userGroups.set(1, groupRepository.save(group));

        Member member = Member.builder()
                        .memberId(null)
                        .userGroup(userGroups.get(0))
                        .nickname("철수")
                        .build();

        userGroupMembers.get(0).add(memberRepository.save(member));

        member = Member.builder()
                .memberId(null)
                .userGroup(userGroups.get(0))
                .nickname("영희")
                .build();

        userGroupMembers.get(0).add(memberRepository.save(member));

        member = Member.builder()
                .memberId(null)
                .userGroup(userGroups.get(1))
                .nickname("철수")
                .build();

        userGroupMembers.get(1).add(memberRepository.save(member));

        member = Member.builder()
                .memberId(null)
                .userGroup(userGroups.get(1))
                .nickname("민경")
                .build();

        userGroupMembers.get(1).add(memberRepository.save(member));

        member = Member.builder()
                .memberId(null)
                .userGroup(userGroups.get(1))
                .nickname("정현")
                .build();

        userGroupMembers.get(1).add(memberRepository.save(member));

        LocalDateTime startTime = LocalDateTime.of(2024, 11, 8, 13, 0);
        LocalDateTime endTime = LocalDateTime.of(2024, 11, 9, 13, 0);

        Calculate cal0 = Calculate.builder()
                .calculateId(null)
                .startTime(startTime)
                .endTime(endTime)
                .status(CalculateStatus.CALCULATING)
                .userGroup(userGroups.get(0))
                .build();
        calculateRepository.save(cal0);

        Calculate cal1 = Calculate.builder()
                .calculateId(null)
                .startTime(startTime)
                .endTime(endTime)
                .status(CalculateStatus.PENDING)
                .userGroup(userGroups.get(1))
                .build();
        calculateRepository.save(cal1);

        CalculateDetail cd0 = CalculateDetail.builder()
                .calculateDetailId(null)
                .amount(64050)
                .calculate(cal0)
                .payer(userGroupMembers.get(0).get(0))
                .payee(userGroupMembers.get(0).get(1))
                .build();

        calculateDetailRepository.save(cd0);

        CalculateDetail cd1 = CalculateDetail.builder()
                .calculateDetailId(null)
                .amount(34050)
                .calculate(cal1)
                .payer(userGroupMembers.get(1).get(1))
                .payee(userGroupMembers.get(1).get(0))
                .build();

        CalculateDetail cd2 = CalculateDetail.builder()
                .calculateDetailId(null)
                .amount(4940)
                .calculate(cal1)
                .payer(userGroupMembers.get(1).get(1))
                .payee(userGroupMembers.get(1).get(2))
                .build();
        calculateDetailRepository.saveAll(List.of(cd1, cd2));

        // when
        List<CalculateDetail> cds00 = calculateDetailRepository.findAllByCalculate(cal0);
        List<CalculateDetail> cds01 = calculateDetailRepository.findAllByCalculate(cal1);
        List<CalculateDetail> cds02 = calculateDetailRepository.findAllByCalculate(Calculate.builder().calculateId(cal0.getCalculateId() + 8081).build());

        calculateDetailRepository.deleteByCalculate(cal1);
        calculateDetailRepository.deleteByCalculate(Calculate.builder().calculateId(cal0.getCalculateId() + 8081).build());

        List<CalculateDetail> cds10 = calculateDetailRepository.findAllByCalculate(cal0);
        List<CalculateDetail> cds11 = calculateDetailRepository.findAllByCalculate(cal1);
        List<CalculateDetail> cds12 = calculateDetailRepository.findAllByCalculate(Calculate.builder().calculateId(cal0.getCalculateId() + 8081).build());

        // then
        assertThat(cds00).contains(cd0).hasSize(1);
        assertThat(cds01).contains(cd1).contains(cd2).hasSize(2);
        assertThat(cds02).isEmpty();

        assertThat(cds10).contains(cd0).hasSize(1);
        assertThat(cds11).isEmpty();
        assertThat(cds12).isEmpty();
    }
}