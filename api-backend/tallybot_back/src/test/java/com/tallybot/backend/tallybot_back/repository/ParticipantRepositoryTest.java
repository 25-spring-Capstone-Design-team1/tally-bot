package com.tallybot.backend.tallybot_back.repository;

import com.tallybot.backend.tallybot_back.domain.*;
import config.DatabaseTestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles({"test", "repository-test"})  // 다른 프로파일 조합으로 별도 컨텍스트 생성
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@Rollback
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ParticipantRepositoryTest extends DatabaseTestBase {
    @Autowired
    private CalculateRepository calculateRepository;

    @Autowired
    private SettlementRepository settlementRepository;

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("ParticipantRepositoryTest 검증")
    public void saveAndTestParticipant() {
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

        Settlement settlement0 = Settlement.builder()
                .settlementId(null)
                .place("스타벅스")
                .item("오늘의 커피")
                .amount(30000)
                .userGroup(userGroups.get(0))
                .payer(userGroupMembers.get(0).get(1))
                .calculate(cal0)
                .build();
        settlementRepository.save(settlement0);

        Settlement settlement1 = Settlement.builder()
                .settlementId(null)
                .place("숙소")
                .item("숙소")
                .amount(1160000)
                .userGroup(userGroups.get(1))
                .payer(userGroupMembers.get(1).get(2))
                .calculate(cal1)
                .build();
        settlementRepository.save(settlement1);


        Ratio half = new Ratio(1, 2);
        Ratio third = new Ratio(1, 3);
        Ratio sixth = new Ratio(1, 6);

        Participant part00 = Participant.builder()
                .participantKey(new Participant.ParticipantKey(settlement0, userGroupMembers.get(0).get(0)))
                .constant(18800)
                .ratio(half)
                .build();

        Participant part01 = Participant.builder()
                .participantKey(new Participant.ParticipantKey(settlement0, userGroupMembers.get(0).get(1)))
                .constant(0)
                .ratio(half)
                .build();

        Participant part10 = Participant.builder()
                .participantKey(new Participant.ParticipantKey(settlement1, userGroupMembers.get(1).get(0)))
                .constant(1800)
                .ratio(half)
                .build();

        Participant part11 = Participant.builder()
                .participantKey(new Participant.ParticipantKey(settlement1, userGroupMembers.get(1).get(1)))
                .constant(0)
                .ratio(third)
                .build();

        Participant part12 = Participant.builder()
                .participantKey(new Participant.ParticipantKey(settlement1, userGroupMembers.get(1).get(2)))
                .constant(0)
                .ratio(sixth)
                .build();

        System.out.println(part00);
        System.out.println(part01);
        System.out.println(part10);
        System.out.println(part11);
        System.out.println(part12);

        participantRepository.save(part00);
        participantRepository.save(part01);
        participantRepository.save(part10);
        participantRepository.save(part11);
        participantRepository.save(part12);

        // when
        List<Participant> participants = participantRepository.findAll();
        participantRepository.deleteAll();
        List<Participant> participants2 = participantRepository.findAll();

        // then
        System.out.println(part00);
        System.out.println(part01);
        System.out.println(part10);
        System.out.println(part11);
        System.out.println(part12);

        assertThat(participants).containsAll(List.of(part00, part01, part10, part11, part12))
                .hasSize(5);
        assertThat(participants2).isEmpty();
    }

}
