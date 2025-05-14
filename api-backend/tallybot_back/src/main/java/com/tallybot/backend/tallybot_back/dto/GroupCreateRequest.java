package com.tallybot.backend.tallybot_back.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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

    @NotBlank(message = "Group name must not be empty.")
    private String groupName;

    @NotEmpty(message = "Members list must not be empty.")
    private List<@NotBlank(message = "All member nicknames must be non-empty strings.") String> members;

}
