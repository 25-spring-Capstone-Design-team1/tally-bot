package com.tallybot.backend.tallybot_back.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GroupCreateRequest {

    private String groupName;
    private List<String> members;

}
