package com.tallybot.backend.tallybot_back.service;

import com.tallybot.backend.tallybot_back.domain.UserGroup;
import com.tallybot.backend.tallybot_back.domain.Member;
import com.tallybot.backend.tallybot_back.dto.ChatDto;
import com.tallybot.backend.tallybot_back.repository.ChatRepository;
import com.tallybot.backend.tallybot_back.repository.GroupRepository;
import com.tallybot.backend.tallybot_back.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
@ActiveProfiles("mock-data")  // 이 프로파일 조합으로 별도 컨텍스트 생성
class ChatServiceTest {

    private GroupRepository groupRepository;
    private MemberRepository memberRepository;
    private ChatRepository chatRepository;
    private ChatService chatService;

    @BeforeEach
    void setup() {
        groupRepository = mock(GroupRepository.class);
        memberRepository = mock(MemberRepository.class);
        chatRepository = mock(ChatRepository.class);
        chatService = new ChatService(groupRepository, memberRepository, chatRepository);
    }

    @Test
    void saveChats_success() {
        ChatDto dto = new ChatDto(1L, LocalDateTime.now(), 1001L, "테스트");

        UserGroup mockUserGroup = new UserGroup();
        Member mockMember = new Member();
        mockMember.setMemberId(1001L);

        when(groupRepository.findById(anyLong())).thenReturn(Optional.of(mockUserGroup));
        when(memberRepository.findByMemberIdAndUserGroup(anyLong(), eq(mockUserGroup)))
                .thenReturn(Optional.of(mockMember));

        chatService.saveChats(List.of(dto));

        verify(chatRepository, times(1)).saveAll(anyList());
    }

    @Test
    void saveChats_groupNotFound() {
        ChatDto dto = new ChatDto(999L, LocalDateTime.now(), 1001L, "하이");

        when(groupRepository.findById(999L)).thenReturn(Optional.empty());

        try {
            chatService.saveChats(List.of(dto));
            assert false : "예외가 발생해야 함";
        } catch (IllegalArgumentException e) {
            assert e.getMessage().contains("Group not found");
        }
    }

    @Test
    void groupAndMembersExist_returnsFalseIfMissing() {
        when(groupRepository.existsById(1L)).thenReturn(true);
        when(memberRepository.existsById(999L)).thenReturn(false);

        ChatDto dto = new ChatDto(1L, LocalDateTime.now(), 999L, "하이");

        boolean result = chatService.groupAndMembersExist(List.of(dto));
        assert !result;
    }
}
