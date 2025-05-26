package com.tallybot.backend.tallybot_back.repository;

import com.tallybot.backend.tallybot_back.domain.Calculate;
import com.tallybot.backend.tallybot_back.domain.UserGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CalculateRepository extends JpaRepository<Calculate, Long> {
    int countByUserGroup(UserGroup userGroup);
    List<Calculate> findByUserGroup(UserGroup userGroup);
    Optional<Calculate> findByCalculateId(Long calculateId);

}
