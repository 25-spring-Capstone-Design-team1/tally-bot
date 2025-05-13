package com.tallybot.backend.tallybot_back.service;

import com.tallybot.backend.tallybot_back.domain.Chat;
import com.tallybot.backend.tallybot_back.domain.Group;
import com.tallybot.backend.tallybot_back.domain.Member;
import com.tallybot.backend.tallybot_back.dto.ChatDto;
import com.tallybot.backend.tallybot_back.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class ChatService {

    private final GroupRepository groupRepository;
    private final MemberRepository memberRepository;
    private final ChatRepository chatRepository;

    public void saveChats(List<ChatDto> dtoList) {
        List<Chat> chatList = new ArrayList<>();

        for (ChatDto dto : dtoList) {
            Group group = groupRepository.findById(dto.getGroupId())
                    .orElseThrow(() -> new IllegalArgumentException("Group not found"));

            Member member = memberRepository.findByNicknameAndGroup(dto.getNickname(), group)
                    .orElseThrow(() -> new IllegalArgumentException("Member not found"));

            Chat chat = new Chat();
            chat.setGroup(group);
            chat.setMember(member);
            chat.setTimestamp(dto.getTimestamp());
            chat.setMessage(dto.getMessage());

            chatList.add(chat);
        }

        chatRepository.saveAll(chatList);
    }
}
