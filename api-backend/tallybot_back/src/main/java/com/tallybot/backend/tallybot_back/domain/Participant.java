package com.tallybot.backend.tallybot_back.domain;

import jakarta.persistence.*;
import lombok.*;
import org.apache.tomcat.util.modeler.ParameterInfo;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Participant {

    @EmbeddedId
    private ParticipantKey participantKey;

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
        @JoinColumn(name = "settlement_id")
        private Settlement settlement;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "member_id")
        private Member member;
    }
}
