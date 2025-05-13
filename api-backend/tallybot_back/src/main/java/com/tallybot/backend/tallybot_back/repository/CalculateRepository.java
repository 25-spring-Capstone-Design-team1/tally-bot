package com.tallybot.backend.tallybot_back.repository;

import com.tallybot.backend.tallybot_back.domain.Calculate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CalculateRepository extends JpaRepository<Calculate, Long> {
}
