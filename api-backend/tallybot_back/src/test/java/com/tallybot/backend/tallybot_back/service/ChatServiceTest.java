package com.tallybot.backend.tallybot_back.service;

import com.tallybot.backend.tallybot_back.domain.Chat;
import com.tallybot.backend.tallybot_back.domain.Group;
import com.tallybot.backend.tallybot_back.domain.Member;
import com.tallybot.backend.tallybot_back.dto.ChatDto;
import com.tallybot.backend.tallybot_back.repository.ChatRepository;
import com.tallybot.backend.tallybot_back.repository.GroupRepository;
import com.tallybot.backend.tallybot_back.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class ChatServiceTest {

    @InjectMocks
    private ChatService chatService;

    @Mock private GroupRepository groupRepository;
    @Mock private MemberRepository memberRepository;
    @Mock private ChatRepository chatRepository;

    private Group group;
    private Member member;

    @BeforeEach
    void setup() {
        group = new Group();
        group.setGroupId(1L);
        group.setGroupName("여행방");

        member = new Member();
        member.setMemberId(100L);
        member.setNickname("철수");
        member.setGroup(group);
    }

    @Test
    @DisplayName("채팅 리스트 저장 테스트")
    void saveChatsTest() {
        // given
        ChatDto dto1 = new ChatDto(1L, LocalDateTime.parse("2024-05-01T12:00:00"), "철수", "안녕");
        List<ChatDto> dtoList = List.of(dto1);

        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(memberRepository.findByNicknameAndGroup("철수", group)).thenReturn(Optional.of(member));

        // when
        chatService.saveChats(dtoList);

        // then
        verify(groupRepository).findById(1L);
        verify(memberRepository).findByNicknameAndGroup("철수", group);
        verify(chatRepository).saveAll(any());
    }
}
