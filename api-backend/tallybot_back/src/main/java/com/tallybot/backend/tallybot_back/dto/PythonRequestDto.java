package com.tallybot.backend.tallybot_back.dto;

import lombok.*;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PythonRequestDto {
    private Long chatroom_id;

    @JsonProperty("chatroom_name")
    private String chatroomName;
    private List<Map<String, String>> members; // ex: [{ "1": "지훈" }, { "2": "준호" }]
    private List<PythonMessageDto> messages;
}
