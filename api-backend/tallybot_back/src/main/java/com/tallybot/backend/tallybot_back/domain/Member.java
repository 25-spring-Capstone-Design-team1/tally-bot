package com.tallybot.backend.tallybot_back.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId; // 멤버 식별용 ID

    private String nickname; // 사용자 닉네임

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id") // db 컬럼명으로 수정하기
    private UserGroup userGroup;
}