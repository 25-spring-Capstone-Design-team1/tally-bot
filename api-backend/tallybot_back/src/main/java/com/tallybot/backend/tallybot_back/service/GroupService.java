package com.tallybot.backend.tallybot_back.service;

import com.tallybot.backend.tallybot_back.domain.*;
import com.tallybot.backend.tallybot_back.dto.GroupCreateRequest;
import com.tallybot.backend.tallybot_back.dto.GroupCreateResponse;
import com.tallybot.backend.tallybot_back.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final MemberRepository memberRepository;


    public GroupCreateResponse createGroupWithMembers(GroupCreateRequest request) {
        Group group = groupRepository.save(new Group(null, request.getGroupName()));


        List<GroupCreateResponse.MemberInfo> memberInfos = new ArrayList<>();
        for (String nickname : request.getMembers()) {
            Member member = new Member();
            member.setNickname(nickname);
            member.setGroup(group);
            memberRepository.save(member);

            memberInfos.add(new GroupCreateResponse.MemberInfo(member.getNickname(), member.getMemberId()));
        }

        return new GroupCreateResponse(group.getGroupId(), memberInfos);
    }

}
