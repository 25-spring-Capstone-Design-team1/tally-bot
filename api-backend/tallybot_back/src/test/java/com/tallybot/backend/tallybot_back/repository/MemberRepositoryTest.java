package com.tallybot.backend.tallybot_back.repository;

import com.tallybot.backend.tallybot_back.domain.Member;
import com.tallybot.backend.tallybot_back.domain.UserGroup;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;


// 실제 테스트
@SpringBootTest
@ActiveProfiles({"test", "repository-test"})  // 다른 프로파일 조합으로 별도 컨텍스트 생성
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@Rollback
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class MemberRepositoryTest extends DatabaseTestBase {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("group 추가 -> member 추가 및 조회")
    void saveAndFindUserGroupAndMember() {
        // given
        List<UserGroup> userGroups = new ArrayList<>();
        List<List<Member>> userGroupMembers = new ArrayList<>();
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

        // when
        List<List<Boolean>> exists = new ArrayList<>();
        List<Integer> countByUserGroup = new ArrayList<>();
        List<List<Member>> findsByUserGroup = new ArrayList();
        List<List<Optional<Member>>> findByMemberIdAndUserGroup = new ArrayList();
        List<List<Boolean>> existsByUserGroupAndNickname = new ArrayList<>();
        List<List<Optional<Member>>> findById = new ArrayList();

        for(int i = 0; i < 3; i++) {
            exists.add(new ArrayList<>());
            countByUserGroup.add(i, memberRepository.countByUserGroup(userGroups.get(i)));
            findsByUserGroup.add(i, memberRepository.findByUserGroup(userGroups.get(i)));
            findByMemberIdAndUserGroup.add(new ArrayList<>());
            existsByUserGroupAndNickname.add(new ArrayList<>());
            findById.add(new ArrayList<>());

            for(int j = 0; j < userGroupMembers.get(i).size(); j++) {
                exists.get(i).add(j, memberRepository.existsById(
                        userGroupMembers.get(i).get(j).getMemberId()
                ));
                findByMemberIdAndUserGroup.get(i).add(j, memberRepository.findByMemberIdAndUserGroup(
                        userGroupMembers.get(i).get(j).getMemberId(), userGroups.get(i)
                ));
                existsByUserGroupAndNickname.get(i).add(j, memberRepository.existsByUserGroupAndNickname(
                        userGroups.get(i), userGroupMembers.get(i).get(j).getNickname()
                ));
                findById.get(i).add(j, memberRepository.findById(
                        userGroupMembers.get(i).get(j).getMemberId()
                ));

            }
        }

        List<Member> findAllById1 = memberRepository.findAllById(Set.of(userGroupMembers.get(1).get(1).getMemberId(), userGroupMembers.get(1).get(2).getMemberId()));
        List<Member> findAllById0 = memberRepository.findAllById(Set.of(userGroupMembers.get(0).get(0).getMemberId(), userGroupMembers.get(0).get(1).getMemberId()));
        List<Member> findAllById2 = memberRepository.findAllById(Set.of(userGroupMembers.get(2).get(0).getMemberId()));

        List<List<Boolean>> existsFalse = new ArrayList<>();
        List<Integer> countByUserGroupFalse = new ArrayList<>();
        List<List<Member>> findsByUserGroupFalse = new ArrayList();
        List<List<Optional<Member>>> findByMemberIdAndUserGroupFalse1 = new ArrayList();
        List<List<Optional<Member>>> findByMemberIdAndUserGroupFalse2 = new ArrayList();
        List<List<Boolean>> existsByUserGroupAndNicknameFalse1 = new ArrayList<>();
        List<List<Boolean>> existsByUserGroupAndNicknameFalse2 = new ArrayList<>();
        List<List<Optional<Member>>> findByIdFalse = new ArrayList();

        for(int i = 0; i < 3; i++) {
            existsFalse.add(new ArrayList<>());
            var cbugFalse = UserGroup.builder()
                    .groupId(9203L)
                    .groupName("가짜 그룹")
                    .build();
            countByUserGroupFalse.add(i, memberRepository.countByUserGroup(cbugFalse));
            findsByUserGroupFalse.add(i, memberRepository.findByUserGroup(cbugFalse));
            findByMemberIdAndUserGroupFalse1.add(new ArrayList<>());
            findByMemberIdAndUserGroupFalse2.add(new ArrayList<>());
            existsByUserGroupAndNicknameFalse1.add(new ArrayList<>());
            existsByUserGroupAndNicknameFalse2.add(new ArrayList<>());
            findByIdFalse.add(new ArrayList<>());

            for(int j = 0; j < userGroupMembers.get(i).size(); j++) {
                existsFalse.get(i).add(j, memberRepository.existsById(
                        userGroupMembers.get(i).get(j).getMemberId() + 603037L
                ));
                findByMemberIdAndUserGroupFalse1.get(i).add(j, memberRepository.findByMemberIdAndUserGroup(
                        userGroupMembers.get(i).get(j).getMemberId() + 603037L, userGroups.get(i)
                ));
                findByMemberIdAndUserGroupFalse2.get(i).add(j, memberRepository.findByMemberIdAndUserGroup(
                        userGroupMembers.get(i).get(j).getMemberId(), cbugFalse
                ));
                existsByUserGroupAndNicknameFalse1.get(i).add(j, memberRepository.existsByUserGroupAndNickname(
                        cbugFalse, userGroupMembers.get(i).get(j).getNickname()
                ));
                existsByUserGroupAndNicknameFalse2.get(i).add(j, memberRepository.existsByUserGroupAndNickname(
                        userGroups.get(i), userGroupMembers.get(i).get(j).getNickname() + "2"
                ));
                findByIdFalse.get(i).add(j, memberRepository.findById(
                        userGroupMembers.get(i).get(j).getMemberId() + 603037L
                ));

            }
        }

        List<Member> findAllByIdFalse1 = memberRepository.findAllById(Set.of(userGroupMembers.get(1).get(1).getMemberId() + 603037L, userGroupMembers.get(1).get(2).getMemberId() + 603037L));
        List<Member> findAllByIdFalse0 = memberRepository.findAllById(Set.of(userGroupMembers.get(0).get(0).getMemberId() + 603037L, userGroupMembers.get(0).get(1).getMemberId() + 603037L));
        List<Member> findAllByIdFalse2 = memberRepository.findAllById(Set.of(userGroupMembers.get(2).get(0).getMemberId() + 603037L));

        // then
        assertThat(findAllById0).containsAll(Set.of(userGroupMembers.get(0).get(0), userGroupMembers.get(0).get(1))).hasSize(2);
        assertThat(findAllById1).containsAll(Set.of(userGroupMembers.get(1).get(1), userGroupMembers.get(1).get(2))).hasSize(2);
        assertThat(findAllById2).contains(userGroupMembers.get(2).get(0)).hasSize(1);
        assertThat(findAllByIdFalse0).isEmpty();
        assertThat(findAllByIdFalse1).isEmpty();
        assertThat(findAllByIdFalse2).isEmpty();

        for(int i = 0; i < 3; i++) {
            assertThat(countByUserGroup.get(i)).isEqualTo(userGroupMembers.get(i).size());
            assertThat(countByUserGroupFalse.get(i)).isEqualTo(0);
            assertThat(findsByUserGroup.get(i)).containsAll(userGroupMembers.get(i));
            assertThat(userGroupMembers.get(i)).containsAll(findsByUserGroup.get(i));
            assertThat(findsByUserGroupFalse.get(i).size()).isEqualTo(0);
            for(int j = 0; j < userGroupMembers.get(i).size(); j++) {
                assertThat(exists.get(i).get(j)).isTrue();
                assertThat(existsFalse.get(i).get(j)).isFalse();
                assertThat(findByMemberIdAndUserGroup.get(i).get(j)).isNotEmpty()
                        .get().isEqualTo(userGroupMembers.get(i).get(j));
                assertThat(findByMemberIdAndUserGroupFalse1.get(i).get(j)).isEmpty();
                assertThat(findByMemberIdAndUserGroupFalse2.get(i).get(j)).isEmpty();
                assertThat(existsByUserGroupAndNickname.get(i).get(j)).isTrue();
                assertThat(existsByUserGroupAndNicknameFalse1.get(i).get(j)).isFalse();
                assertThat(existsByUserGroupAndNicknameFalse2.get(i).get(j)).isFalse();
                assertThat(findById.get(i).get(j)).isNotEmpty()
                        .get().isEqualTo(userGroupMembers.get(i).get(j));
                assertThat(findByIdFalse.get(i).get(j)).isEmpty();
            }
        }
    }
}