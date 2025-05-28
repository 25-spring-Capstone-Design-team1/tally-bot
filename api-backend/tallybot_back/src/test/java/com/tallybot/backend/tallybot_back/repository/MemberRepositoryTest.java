//package com.tallybot.backend.tallybot_back.repository;
//
//import com.tallybot.backend.tallybot_back.domain.Group;
//import com.tallybot.backend.tallybot_back.domain.Member;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@DataJpaTest
//class MemberRepositoryTest {
//
//    @Autowired
//    private MemberRepository memberRepository;
//
//    @Autowired
//    private GroupRepository groupRepository;
//
//    @Test
//    @DisplayName("닉네임으로 멤버 조회")
//    void findByNickname() {
//        // given
//        Member member = new Member();
//        member.setNickname("철수");
//        memberRepository.save(member);
//
//        // when
//        Optional<Member> result = memberRepository.findByNickname("철수");
//
//        // then
//        assertThat(result).isPresent();
//        assertThat(result.get().getNickname()).isEqualTo("철수");
//    }
//
//    @Test
//    @DisplayName("닉네임 + 그룹으로 멤버 조회")
//    void findByNicknameAndGroup() {
//        // given
//        Group group = new Group();
//        group.setGroupName("여행팀");
//        groupRepository.save(group);
//
//        Member member = new Member();
//        member.setNickname("영희");
//        member.setGroup(group);
//        memberRepository.save(member);
//
//        // when
//        Optional<Member> result = memberRepository.findByNicknameAndGroup("영희", group);
//
//        // then
//        assertThat(result).isPresent();
//        assertThat(result.get().getGroup().getGroupName()).isEqualTo("여행팀");
//    }
//}
