package com.tallybot.backend.tallybot_back.repository;

import com.tallybot.backend.tallybot_back.domain.Group;
import com.tallybot.backend.tallybot_back.domain.Member;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByMemberId(Long memberId);

    Optional<Member> findByMemberIdAndGroup(Long memberId, Group group);

    int countByGroup(Group group);

    List<Member> findByGroup(Group group);

    boolean existsByGroupAndNickname(Group group, String nickname);

    List<Member> findByMemberIdIn(List<Long> ids);

}
