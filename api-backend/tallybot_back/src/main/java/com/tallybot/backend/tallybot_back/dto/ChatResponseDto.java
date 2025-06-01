package com.tallybot.backend.tallybot_back.dto;

import com.tallybot.backend.tallybot_back.domain.Chat;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ChatResponseDto {
    private Long chatId;
    private Long groupId;
    private Long memberId;
    private String message;
    private LocalDateTime timestamp;

    public static ChatResponseDto from(Chat chat) {
        return new ChatResponseDto(
                chat.getChatId(),
                chat.getUserGroup().getGroupId(),
                chat.getMember().getMemberId(),
                chat.getMessage(),
                chat.getTimestamp()
        );
    }
}
