package com.tallybot.backend.tallybot_back.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.tallybot.backend.tallybot_back.domain.Chat;

import java.time.LocalDateTime;
import java.util.List;

// Chat 엔티티를 위한 Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {
    // 채팅 내역 db 저장 - 기본 CRUD 사용
    List<Chat> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    default List<Chat> findAllIn24hr(LocalDateTime start) {
        if(start == null) return findByTimestampBetween(LocalDateTime.now().minusHours(24), LocalDateTime.now());
        return findByTimestampBetween(start, start.plusHours(24));
    }
}
