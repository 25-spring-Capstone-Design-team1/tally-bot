package com.tallybot.backend.tallybot_back.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.tallybot.backend.tallybot_back.domain.Chat;
import com.tallybot.backend.tallybot_back.domain.Group;

import java.time.LocalDateTime;
import java.util.List;

// Chat 엔티티를 위한 Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {
    // 채팅 내역 db 저장 - 기본 CRUD 사용
    List<Chat> findByGroupAndTimestampBetween(Group group, LocalDateTime start, LocalDateTime end);

    default List<Chat> findByGroupAndNoTime(Group group) {
        return findByGroupAndTimestampBetween(group, LocalDateTime.now().minusHours(24), LocalDateTime.now());
    }

    default List<Chat> findByGroupAndStartTime(Group group, LocalDateTime start) {
        return findByGroupAndTimestampBetween(group, start, LocalDateTime.now());
    }

    default List<Chat> findByGroupAndTimeInterval(Group group, LocalDateTime start, LocalDateTime end) {
        return findByGroupAndTimestampBetween(group, start, end);
    }

    default List<Chat> findByGroupAndTime(Group group, LocalDateTime start, LocalDateTime end) {
        if(start == null && end == null)
            return findByGroupAndNoTime(group);
        else if(end == null)
            return findByGroupAndStartTime(group, start);
        else
            return findByGroupAndTimeInterval(group, start, end);
    }
}
