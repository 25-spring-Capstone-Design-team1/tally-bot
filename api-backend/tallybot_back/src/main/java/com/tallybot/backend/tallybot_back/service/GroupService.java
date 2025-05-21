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

    public GroupCreateResponse createGroupWithMember(GroupCreateRequest request) {
        Group group = groupRepository.findById(request.getGroupId())
                .orElseGet(() -> groupRepository.save(new Group(request.getGroupId(), request.getGroupName())));

        // 중복 멤버 확인
        boolean exists = memberRepository.existsByGroupAndNickname(group, request.getMember());
        if (!exists) {
            Member member = new Member();
            member.setGroup(group);
            member.setNickname(request.getMember());
            memberRepository.save(member);
        }

        // 모든 멤버 조회 후 응답 반환
        List<Member> members = memberRepository.findByGroup(group);
        List<GroupCreateResponse.MemberInfo> memberInfos = members.stream()
                .map(m -> new GroupCreateResponse.MemberInfo(m.getNickname(), m.getMemberId()))
                .toList();

        return new GroupCreateResponse(group.getGroupId(), memberInfos);
    }

}
