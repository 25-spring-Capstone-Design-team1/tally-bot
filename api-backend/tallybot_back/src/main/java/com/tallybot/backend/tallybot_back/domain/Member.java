package com.tallybot.backend.tallybot_back.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId; // 멤버 식별용 ID

    private String nickname; // 사용자 닉네임

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id") // 외래키 컬럼 이름 지정
    private Group group; // 어떤 채팅방에 속해있는지
}