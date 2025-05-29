package com.tallybot.backend.tallybot_back.repository;

import com.tallybot.backend.tallybot_back.domain.Chat;
import com.tallybot.backend.tallybot_back.domain.Member;
import com.tallybot.backend.tallybot_back.domain.UserGroup;
import com.tallybot.backend.tallybot_back.fixture.GenericBulkFactory;
import config.DatabaseTestBase;
import org.junit.jupiter.api.*;
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

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Order(1)
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@Rollback
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class ChatRepositoryTest extends DatabaseTestBase {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ChatRepository chatRepository;

    private GenericBulkFactory.Builder4<Chat, UserGroup, LocalDateTime, Member, String> chatFactory
            = new GenericBulkFactory.Builder4<>(
            (UserGroup a, LocalDateTime b, Member c, String d)
                    -> new Chat(null, a, b, c, d));

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("chat 범위 검사")
    void saveAndFindInterval() {
        // given
        List<UserGroup> userGroups = new ArrayList<>();
        List<List<Member>> userGroupMembers = new ArrayList<>();
        List<Chat> chats = new ArrayList<>();
        for(int i = 0; i < 3; i++) {
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

        group = UserGroup.builder()
                .groupId(1194L)
                .groupName("삼겹살 번개모임")
                .build();

        userGroups.set(2, groupRepository.save(group));

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
                .userGroup(userGroups.get(2))
                .nickname("민재")
                .build();

        userGroupMembers.get(2).add(memberRepository.save(member));

        member = Member.builder()
                .memberId(null)
                .userGroup(userGroups.get(1))
                .nickname("민경")
                .build();

        userGroupMembers.get(1).add(memberRepository.save(member));

        member = Member.builder()
                .memberId(null)
                .userGroup(userGroups.get(2))
                .nickname("현주")
                .build();

        userGroupMembers.get(2).add(memberRepository.save(member));

        member = Member.builder()
                .memberId(null)
                .userGroup(userGroups.get(1))
                .nickname("정현")
                .build();

        userGroupMembers.get(1).add(memberRepository.save(member));

        List<Chat> l = chatRepository.saveAll(List.of(new Chat(null, userGroups.get(2)
                , LocalDateTime.of(2024, 2, 8, 12, 40)
                , userGroupMembers.get(2).get(1), "야 내일 뭐하냐?")));

        List<Chat> l2 = chatFactory
                .add(userGroups.get(1), LocalDateTime.of(2024, 2, 8, 12, 40)
                        , userGroupMembers.get(1).get(1), "야 내일 뭐하냐?") // OK
                .add(userGroups.get(0), LocalDateTime.of(2024, 2, 8, 13, 48)
                        , userGroupMembers.get(0).get(0), "300만원만 생기면 좋겠다") // OK
                .add(userGroups.get(2), LocalDateTime.of(2024, 2, 9, 0, 2)
                        , userGroupMembers.get(2).get(1), "너는 나한테 왜 그러냐?")
                .add(userGroups.get(1), LocalDateTime.of(2024, 2, 9, 0, 3)
                        , userGroupMembers.get(1).get(0), "나는 배 안 고픈데?")
                .add(userGroups.get(2), LocalDateTime.of(2024, 2, 10, 0, 7)
                        , userGroupMembers.get(2).get(0), "아 아무것도 아니야...")
                .add(userGroups.get(0), LocalDateTime.of(2024, 2, 10, 0, 7)
                        , userGroupMembers.get(0).get(1), "글쎄 나는 모르겠네")
                .add(userGroups.get(1), LocalDateTime.of(2024, 2, 10, 0, 7)
                        , userGroupMembers.get(1).get(2), "AAAAAAAA")
                .saveAllWith(chatRepository);

        List<Chat> l3 = chatRepository.saveAll(List.of(new Chat(null, userGroups.get(2)
                , LocalDateTime.of(2024, 2, 10, 0, 8)
                , userGroupMembers.get(0).get(1), "야 내일 뭐하냐?")));

        l.addAll(l2);
        l.addAll(l3);

        // when
        // All OK.
        List<Chat> lTestAll0 = chatRepository.findByUserGroupAndTimestampBetween(
                userGroups.get(0)
                , LocalDateTime.of(2024, 2, 7, 10, 0)
                , LocalDateTime.of(2024, 2, 11, 0, 0));
        List<Chat> lTestAll1 = chatRepository.findByUserGroupAndTimestampBetween(
                userGroups.get(1)
                , LocalDateTime.of(2024, 2, 7, 10, 0)
                , LocalDateTime.of(2024, 2, 11, 0, 0));
        List<Chat> lTestAll2 = chatRepository.findByUserGroupAndTimestampBetween(
                userGroups.get(2)
                , LocalDateTime.of(2024, 2, 7, 10, 0)
                , LocalDateTime.of(2024, 2, 11, 0, 0));

        // Early Empty.
        List<Chat> lTestEarly0 = chatRepository.findByUserGroupAndTimestampBetween(
                userGroups.get(0)
                , LocalDateTime.of(2024, 2, 7, 10, 0)
                , LocalDateTime.of(2024, 2, 8, 13, 0));
        List<Chat> lTestEarly1 = chatRepository.findByUserGroupAndTimestampBetween(
                userGroups.get(1)
                , LocalDateTime.of(2024, 2, 7, 10, 0)
                , LocalDateTime.of(2024, 2, 8, 12, 0));
        List<Chat> lTestEarly2 = chatRepository.findByUserGroupAndTimestampBetween(
                userGroups.get(2)
                , LocalDateTime.of(2024, 2, 7, 10, 0)
                , LocalDateTime.of(2024, 2, 8, 11, 0));

        // Late Empty.
        List<Chat> lTestLate0 = chatRepository.findByUserGroupAndTimestampBetween(
                userGroups.get(0)
                , LocalDateTime.of(2024, 2, 10, 1, 0)
                , LocalDateTime.of(2024, 2, 11, 13, 0));
        List<Chat> lTestLate1 = chatRepository.findByUserGroupAndTimestampBetween(
                userGroups.get(1)
                , LocalDateTime.of(2024, 2, 10, 12, 0)
                , LocalDateTime.of(2024, 2, 11, 12, 0));
        List<Chat> lTestLate2 = chatRepository.findByUserGroupAndTimestampBetween(
                userGroups.get(2)
                , LocalDateTime.of(2024, 2, 10, 10, 0)
                , LocalDateTime.of(2024, 2, 11, 0, 0));

        // Partial OK.
        List<Chat> lTestPartial0 = chatRepository.findByUserGroupAndTimestampBetween(
                userGroups.get(0)
                , LocalDateTime.of(2024, 2, 8, 12, 0)
                , LocalDateTime.of(2024, 2, 10, 0, 0));
        List<Chat> lTestPartial1 = chatRepository.findByUserGroupAndTimestampBetween(
                userGroups.get(1)
                , LocalDateTime.of(2024, 2, 9, 0, 0)
                , LocalDateTime.of(2024, 2, 10, 12, 0));
        List<Chat> lTestPartial2 = chatRepository.findByUserGroupAndTimestampBetween(
                userGroups.get(2)
                , LocalDateTime.of(2024, 2, 9, 0, 0)
                , LocalDateTime.of(2024, 2, 9, 11, 0));

        // Reversed Interval Empty.
        List<Chat> lTestRevInterval0 = chatRepository.findByUserGroupAndTimestampBetween(
                userGroups.get(0)
                , LocalDateTime.of(2024, 2, 11, 0, 0)
                , LocalDateTime.of(2024, 2, 7, 10, 0));
        List<Chat> lTestRevInterval1 = chatRepository.findByUserGroupAndTimestampBetween(
                userGroups.get(1)
                , LocalDateTime.of(2024, 2, 10, 12, 0)
                , LocalDateTime.of(2024, 2, 9, 0, 0));
        List<Chat> lTestRevInterval2 = chatRepository.findByUserGroupAndTimestampBetween(
                userGroups.get(2)
                , LocalDateTime.of(2024, 2, 9, 11, 0)
                , LocalDateTime.of(2024, 2, 9, 0, 0));

        // then
        List<Chat> list0 = l.stream().filter(chat -> chat.getUserGroup().equals(userGroups.get(0))).toList();
        List<Chat> list1 = l.stream().filter(chat -> chat.getUserGroup().equals(userGroups.get(1))).toList();
        List<Chat> list2 = l.stream().filter(chat -> chat.getUserGroup().equals(userGroups.get(2))).toList();
        assertThat(lTestAll0).containsAll(list0);
        assertThat(list0).containsAll(lTestAll0);
        assertThat(lTestAll1).containsAll(list1);
        assertThat(list1).containsAll(lTestAll1);
        assertThat(lTestAll2).containsAll(list2);
        assertThat(list2).containsAll(lTestAll2);

        assertThat(lTestAll0).containsAll(lTestPartial0);
        assertThat(lTestAll1).containsAll(lTestPartial1);
        assertThat(lTestAll2).containsAll(lTestPartial2);

        assertThat(lTestEarly0).isEmpty();
        assertThat(lTestEarly1).isEmpty();
        assertThat(lTestEarly2).isEmpty();
        assertThat(lTestLate0).isEmpty();
        assertThat(lTestLate1).isEmpty();
        assertThat(lTestLate2).isEmpty();
        assertThat(lTestRevInterval0).isEmpty();
        assertThat(lTestRevInterval1).isEmpty();
        assertThat(lTestRevInterval2).isEmpty();
    }
}