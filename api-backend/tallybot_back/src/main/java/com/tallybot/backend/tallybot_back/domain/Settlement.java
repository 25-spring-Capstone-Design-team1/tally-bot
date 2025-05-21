package com.tallybot.backend.tallybot_back.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

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
    private String item;      // 결제 항목
    private int amount;       // 결제 총액

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private UserGroup userGroup; // 소속 채팅방

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "payer_id")
    private Member payer; // 결제자

    @OneToMany(mappedBy = "participantKey.settlement")
    private Set<Participant> participants; // 정산 대상자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "calculate_id")
    private Calculate calculate;
}
