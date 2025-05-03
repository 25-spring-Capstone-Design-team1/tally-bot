package com.tallybot.backend.tallybot_back.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CalculateDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long calculateDetailId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "calculate_id")
    private Calculate calculate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payer_id")
    private Member payer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payee_id")
    private Member payee;

    private int amount;
}
