package com.tallybot.backend.tallybot_back.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GroupCreateRequest {
    @NotNull(message = "Group ID must not be null.")
    private Long groupId;

    @NotBlank(message = "Group name must not be empty.")
    private String groupName;

    @NotBlank(message = "Member nickname must not be empty.")
    private String member;
}
