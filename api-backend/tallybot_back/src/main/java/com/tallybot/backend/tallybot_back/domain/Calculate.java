package com.tallybot.backend.tallybot_back.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Calculate{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long calculateId;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private CalculateStatus status; // PENDING, COMPLETED

    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;

    @OneToMany(mappedBy = "calculate")
    private Set<Settlement> settlements;
}



