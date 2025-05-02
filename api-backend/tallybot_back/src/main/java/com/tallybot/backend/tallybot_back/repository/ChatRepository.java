package com.tallybot.backend.tallybot_back.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.tallybot.backend.tallybot_back.domain.Chat;

// Chat 엔티티를 위한 Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {
    // 기본 CRUD 사용
}
