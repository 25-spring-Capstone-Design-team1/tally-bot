package com.tallybot.backend.tallybot_back.domain;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

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

//    @Embeddable
//    @Getter
//    @Setter
//    @NoArgsConstructor
//    @AllArgsConstructor
//    @EqualsAndHashCode
//    public static class ParticipantKey implements Serializable {
//
//        @ManyToOne(fetch = FetchType.LAZY)
//        @JoinColumn(name = "settlement_id", nullable = false)
//        private Settlement settlement;
//
//        @ManyToOne(fetch = FetchType.LAZY)
//        @JoinColumn(name = "member_id", nullable = false)
//        private Member member;
//    }
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public static class ParticipantKey implements Serializable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "settlement_id", nullable = false)
    private Settlement settlement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ParticipantKey that)) return false;

        return Objects.equals(
                this.settlement != null ? this.settlement.getSettlementId() : null,
                that.settlement != null ? that.settlement.getSettlementId() : null
        ) &&
                Objects.equals(
                        this.member != null ? this.member.getMemberId() : null,
                        that.member != null ? that.member.getMemberId() : null
                );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                settlement != null ? settlement.getSettlementId() : null,
                member != null ? member.getMemberId() : null
        );
    }
}
}


