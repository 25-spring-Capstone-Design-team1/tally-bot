package com.tallybot.backend.tallybot_back.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Settlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long settlementId; // 결제내역 식별 ID

    private String place;     // 결제 장소
    private String item;      // 정산 항목
    private int amount;       // 정산 금액

    private Double ratio;     // 비율 정산
    private Integer constant; // 고정 금액 정산

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group; // 소속 채팅방

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payer_id")
    private Member payer; // 결제자

    @ManyToMany
    @JoinTable(
            name = "settlement_participants",
            joinColumns = @JoinColumn(name = "settlement_id"),
            inverseJoinColumns = @JoinColumn(name = "member_id")
    )
    private List<Member> participants; // 정산 대상자
}
