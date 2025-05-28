package com.tallybot.backend.tallybot_back.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.tallybot.backend.tallybot_back.domain.Chat;
import com.tallybot.backend.tallybot_back.domain.UserGroup;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

// Chat 엔티티를 위한 Repository
@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {
    // 채팅 내역 db 저장 - 기본 CRUD 사용
    List<Chat> findByUserGroupAndTimestampBetween(UserGroup userGroup, LocalDateTime start, LocalDateTime end);
}
