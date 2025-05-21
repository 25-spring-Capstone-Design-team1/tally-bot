package com.tallybot.backend.tallybot_back.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.tallybot.backend.tallybot_back.domain.Chat;
import com.tallybot.backend.tallybot_back.domain.UserGroup;

import java.time.LocalDateTime;
import java.util.List;

// Chat 엔티티를 위한 Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {
    // 채팅 내역 db 저장 - 기본 CRUD 사용
    List<Chat> findByUserGroupAndTimestampBetween(UserGroup userGroup, LocalDateTime start, LocalDateTime end);

    default List<Chat> findByGroupAndNoTime(UserGroup userGroup) {
        return findByUserGroupAndTimestampBetween(userGroup, LocalDateTime.now().minusHours(24), LocalDateTime.now());
    }

    default List<Chat> findByGroupAndStartTime(UserGroup userGroup, LocalDateTime start) {
        return findByUserGroupAndTimestampBetween(userGroup, start, LocalDateTime.now());
    }

    default List<Chat> findByGroupAndTimeInterval(UserGroup userGroup, LocalDateTime start, LocalDateTime end) {
        return findByUserGroupAndTimestampBetween(userGroup, start, end);
    }

    default List<Chat> findByGroupAndTime(UserGroup userGroup, LocalDateTime start, LocalDateTime end) {
        if(start == null && end == null)
            return findByGroupAndNoTime(userGroup);
        else if(end == null)
            return findByGroupAndStartTime(userGroup, start);
        else
            return findByGroupAndTimeInterval(userGroup, start, end);
    }
}
