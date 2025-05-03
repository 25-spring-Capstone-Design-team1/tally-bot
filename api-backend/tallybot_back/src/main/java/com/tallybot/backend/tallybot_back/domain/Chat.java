package com.tallybot.backend.tallybot_back.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long chatId;

    @ManyToOne
    @JsonProperty("방id")
    @JoinColumn(name = "group_id") // 외래 키로 연결, db 컬럼명으로 수정하기
    private Group group;

    @JsonProperty("시간")
    private LocalDateTime timestamp;

    @ManyToOne
    @JsonProperty("닉네임")
    @JoinColumn(name = "member_id")
    private Member member;

    @JsonProperty("말")
    private String message;
}
