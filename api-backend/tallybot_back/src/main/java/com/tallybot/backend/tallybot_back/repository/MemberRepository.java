package com.tallybot.backend.tallybot_back.repository;

import com.tallybot.backend.tallybot_back.domain.UserGroup;
import com.tallybot.backend.tallybot_back.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByMemberId(Long memberId);

    Optional<Member> findByMemberIdAndUserGroup(Long memberId, UserGroup userGroup);

    int countByUserGroup(UserGroup userGroup);

    List<Member> findByUserGroup(UserGroup userGroup);

    boolean existsByUserGroupAndNickname(UserGroup userGroup, String nickname);

    List<Member> findByMemberIdIn(List<Long> ids);

}
