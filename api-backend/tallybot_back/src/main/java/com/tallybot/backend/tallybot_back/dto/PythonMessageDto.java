package com.tallybot.backend.tallybot_back.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PythonMessageDto {
    private String unique_chat_id;
    private String speaker;  // Member ID as String
    private String message_content;
    private String timestamp;
}
