package com.tallybot.backend.tallybot_back.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 메시지 식별 ID

    private String room;       // 채팅방 이름
    private String nickname;   // 발신자 닉네임
    private String message;    // 메시지 내용
    private String timestamp;  // 전송 시간 (문자열로 간단히 저장)
}
