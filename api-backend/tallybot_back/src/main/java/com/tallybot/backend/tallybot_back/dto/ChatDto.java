package com.tallybot.backend.tallybot_back.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
@Setter
public class ChatDto {

    @JsonProperty("방id")
    private Long groupId;

    @JsonProperty("시간")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    @JsonProperty("닉네임")
    private String nickname;

    @JsonProperty("말")
    private String message;
}
