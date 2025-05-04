package com.tallybot.backend.tallybot_back.repository;

import com.tallybot.backend.tallybot_back.domain.Group;
import com.tallybot.backend.tallybot_back.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findFirstByGroupAndNickname(Group group, String nickname);
}
