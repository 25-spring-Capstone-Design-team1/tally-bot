package com.tallybot.backend.tallybot_back.service;

import com.tallybot.backend.tallybot_back.domain.Chat;
import com.tallybot.backend.tallybot_back.domain.UserGroup;
import com.tallybot.backend.tallybot_back.domain.Member;
import com.tallybot.backend.tallybot_back.dto.ChatDto;
import com.tallybot.backend.tallybot_back.dto.ChatForGptDto;
import com.tallybot.backend.tallybot_back.dto.ChatResponseDto;
import com.tallybot.backend.tallybot_back.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ChatService {

    private final GroupRepository groupRepository;
    private final MemberRepository memberRepository;
    private final ChatRepository chatRepository;

    public boolean groupAndMembersExist(List<ChatDto> chatDtos) {
        for (ChatDto chat : chatDtos) {
            if (!groupRepository.existsById(chat.getGroupId()) ||
                    !memberRepository.existsById(chat.getMemberId())) {
                return false;
            }
        }
        return true;
    }

    public List<ChatResponseDto> getChatsByGroup(Long groupId) {
        List<Chat> chats = chatRepository.findByUserGroup_GroupIdOrderByTimestampAsc(groupId);
        return chats.stream()
                .map(ChatResponseDto::from)
                .collect(Collectors.toList());
    }


    public void saveChats(List<ChatDto> dtoList) {
        List<Chat> chatList = new ArrayList<>();

        for (ChatDto dto : dtoList) {
            UserGroup userGroup = groupRepository.findById(dto.getGroupId())
                    .orElseThrow(() -> new IllegalArgumentException("Group not found"));

            Member member = memberRepository.findByMemberIdAndUserGroup(dto.getMemberId(), userGroup)
                    .orElseThrow(() -> new IllegalArgumentException("Member not found"));

            Chat chat = new Chat();
            chat.setUserGroup(userGroup);
            chat.setMember(member);
            chat.setTimestamp(dto.getTimestamp());
            chat.setMessage(dto.getMessage());

            chatList.add(chat);
        }

        chatRepository.saveAll(chatList);
    }
}
