package com.tallybot.backend.tallybot_back.repository;

import com.tallybot.backend.tallybot_back.domain.UserGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepository extends JpaRepository<UserGroup, Long> {
}
