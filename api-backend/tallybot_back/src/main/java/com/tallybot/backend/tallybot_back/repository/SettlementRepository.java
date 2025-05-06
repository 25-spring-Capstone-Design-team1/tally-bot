package com.tallybot.backend.tallybot_back.repository;

import com.tallybot.backend.tallybot_back.domain.Calculate;
import com.tallybot.backend.tallybot_back.domain.CalculateDetail;
import com.tallybot.backend.tallybot_back.domain.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface SettlementRepository extends JpaRepository<Settlement, Long> {
    List<Settlement> findByCalculate(Calculate calculate);
}
