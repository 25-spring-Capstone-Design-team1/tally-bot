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
    @Embeddable
    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParticipantKey implements Serializable {
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "settlement_id")
        public Settlement settlement;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "member_id")
        public Member member;
    }

    @EmbeddedId
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public ParticipantKey participantKey;
    public int constant;

    @Embedded
    public Ratio ratio;


}
