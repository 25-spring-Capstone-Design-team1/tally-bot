package com.tallybot.backend.tallybot_back.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "calculate_detail")
public class CalculateDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "calculate_detail_id", nullable = false)
    private Long calculateDetailId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "calculate_id", nullable = false)
    private Calculate calculate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payer_id", nullable = false)
    private Member payer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payee_id", nullable = false)
    private Member payee;

    @Column(name = "amount", nullable = false)
    private int amount;
}
