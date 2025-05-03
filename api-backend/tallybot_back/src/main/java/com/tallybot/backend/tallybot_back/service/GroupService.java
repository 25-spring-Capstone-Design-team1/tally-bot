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

    public void createGroupWithMembers(GroupCreateRequest request) {
        // 그룹 생성
        Group group = new Group();
        group.setGroupName(request.getGroupName());
        groupRepository.save(group);

        // 멤버 리스트 저장
        for (String nickname : request.getMembers()) {
            Member member = new Member();
            member.setNickname(nickname);
            member.setGroup(group);
            memberRepository.save(member);
        }
    }
}
