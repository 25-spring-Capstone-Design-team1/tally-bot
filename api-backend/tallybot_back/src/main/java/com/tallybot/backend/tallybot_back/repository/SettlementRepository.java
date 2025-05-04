package com.tallybot.backend.tallybot_back.repository;

import com.tallybot.backend.tallybot_back.domain.Chat;
import com.tallybot.backend.tallybot_back.domain.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {
    List<Settlement> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    default List<Settlement> findAllIn24hr(LocalDateTime start) {
        if(start == null) return findByTimestampBetween(LocalDateTime.now().minusHours(24), LocalDateTime.now());
        return findByTimestampBetween(start, start.plusHours(24));
    }
}
