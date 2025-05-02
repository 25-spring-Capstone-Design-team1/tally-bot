package com.tallybot.backend.tallybot_back.domain;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long groupId; // 채팅방 구별용 ID

    private String groupName; // 채팅방명
}
