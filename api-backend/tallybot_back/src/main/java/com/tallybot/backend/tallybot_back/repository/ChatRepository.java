package com.tallybot.backend.tallybot_back.repository;
import com.tallybot.backend.tallybot_back.domain.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import com.tallybot.backend.tallybot_back.domain.Chat;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatRepository extends JpaRepository<Chat, Long> {

    List<Chat> findByGroupAndTimestampBetween(Group group, LocalDateTime start, LocalDateTime end);
}
