package com.tallybot.backend.tallybot_back.domain;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Table(name = "participant")
public class Participant {

    @EmbeddedId
    private ParticipantKey participantKey;

    @Column(name = "constant", nullable = false)
    private Integer constant;

    @Embedded
    private Ratio ratio;

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class ParticipantKey implements Serializable {

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "settlement_id", nullable = false)
        private Settlement settlement;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "member_id", nullable = false)
        private Member member;
    }
}
