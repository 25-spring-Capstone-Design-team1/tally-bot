package com.tallybot.backend.tallybot_back.repository;

import com.tallybot.backend.tallybot_back.domain.Calculate;
import com.tallybot.backend.tallybot_back.domain.Group;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CalculateRepository extends JpaRepository<Calculate, Long> {
    int countByGroup(Group group);
    List<Calculate> findByGroup(Group group);
    Optional<Calculate> findByCalculateId(Long calculateId);

}
