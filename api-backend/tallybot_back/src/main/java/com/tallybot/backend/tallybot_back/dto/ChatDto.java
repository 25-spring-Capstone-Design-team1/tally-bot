package com.tallybot.backend.tallybot_back.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
@Setter
public class ChatDto {
    @NotNull(message = "Essential field must not be null")
    private Long groupId;

    @NotNull(message = "Essential field must not be null")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    @NotNull(message = "Essential field must not be null")
    private Long memberId;

    @NotNull(message = "Essential field must not be null")
    private String message;
}
