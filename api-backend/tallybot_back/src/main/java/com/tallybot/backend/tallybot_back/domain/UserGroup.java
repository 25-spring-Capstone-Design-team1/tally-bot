package com.tallybot.backend.tallybot_back.domain;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_group")
public class UserGroup {
    @Id
    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "group_name", nullable = false, columnDefinition = "VARCHAR(50)")
    private String groupName;
}
