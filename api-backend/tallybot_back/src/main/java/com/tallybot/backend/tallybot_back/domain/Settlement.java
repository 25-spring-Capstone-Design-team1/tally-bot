package com.tallybot.backend.tallybot_back.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "settlement")
public class Settlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "settlement_id", nullable = false)
    private Long settlementId; // 결제내역 식별 ID

    @Column(name = "place", nullable = false, length = 63)
    private String place;     // 결제 장소

    @Column(name = "item", nullable = false, length = 63)
    private String item;      // 결제 항목

    @Column(name = "amount", nullable = false)
    private int amount;       // 결제 총액

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private UserGroup userGroup; // 소속 채팅방

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payer_id", nullable = false)
    private Member payer; // 결제자

    // Participant 관계는 필요시에만 조회하도록 수정
    @OneToMany(mappedBy = "participantKey.settlement",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true)
    private Set<Participant> participants; // 정산 대상자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "calculate_id", nullable = false)
    private Calculate calculate;
}