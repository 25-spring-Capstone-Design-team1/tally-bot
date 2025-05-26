package com.tallybot.backend.tallybot_back.service;

import com.tallybot.backend.tallybot_back.domain.*;
import com.tallybot.backend.tallybot_back.dto.GroupCreateRequest;
import com.tallybot.backend.tallybot_back.dto.GroupCreateResponse;
import com.tallybot.backend.tallybot_back.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final MemberRepository memberRepository;

    public GroupCreateResponse createGroupWithMember(GroupCreateRequest request) {
        UserGroup userGroup = groupRepository.findById(request.getGroupId())
                .orElseGet(() -> groupRepository.save(new UserGroup(request.getGroupId(), request.getGroupName())));

        // 중복 멤버 확인
        boolean exists = memberRepository.existsByUserGroupAndNickname(userGroup, request.getMember());
        if (!exists) {
            Member member = new Member();
            member.setUserGroup(userGroup);
            member.setNickname(request.getMember());
            memberRepository.save(member);
        }

        // 모든 멤버 조회 후 응답 반환
        List<Member> members = memberRepository.findByUserGroup(userGroup);
        List<GroupCreateResponse.MemberInfo> memberInfos = members.stream()
                .map(m -> new GroupCreateResponse.MemberInfo(m.getNickname(), m.getMemberId()))
                .toList();

        return new GroupCreateResponse(userGroup.getGroupId(), memberInfos);
    }

}
