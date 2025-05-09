package com.tallybot.backend.tallybot_back.service;

import com.tallybot.backend.tallybot_back.domain.*;
import com.tallybot.backend.tallybot_back.dto.GroupCreateRequest;
import com.tallybot.backend.tallybot_back.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final MemberRepository memberRepository;

    public Long createGroupWithMembers(GroupCreateRequest request) {
        Group group = new Group();
        group.setGroupName(request.getGroupName());
        groupRepository.save(group);

        for (String nickname : request.getMembers()) {
            Member member = new Member();
            member.setNickname(nickname);
            member.setGroup(group);
            memberRepository.save(member);
        }

        return group.getGroupId();  // 생성된 groupId 반환
    }

}
