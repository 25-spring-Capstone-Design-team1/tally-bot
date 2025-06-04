package com.tallybot.backend.tallybot_back.service;

import com.tallybot.backend.tallybot_back.domain.*;
import com.tallybot.backend.tallybot_back.dto.GroupCreateRequest;
import com.tallybot.backend.tallybot_back.dto.GroupCreateResponse;
import com.tallybot.backend.tallybot_back.repository.*;
import com.tallybot.backend.tallybot_back.service.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final MemberRepository memberRepository;
    private final MemberService memberService;
    private static final Logger logger = LoggerFactory.getLogger(GroupService.class);


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
//        Long customId=0L;
//        Long oldId=0L;
//        boolean exists = memberRepository.existsByUserGroupAndNickname(userGroup, request.getMember());
//        if (!exists) {
//            Member member = new Member();
//            member.setUserGroup(userGroup);
//            member.setNickname(request.getMember());
//            if ("이다빈".equals(request.getMember())) {
//                member.setMemberId(101L); // ID 직접 지정
//                logger.info("🍀 {} id 설정 : 101L",
//                        request.getMember());
//            }
//            if ("임재민".equals(request.getMember())) {
//                member.setMemberId(102L); // ID 직접 지정
//                logger.info("🍀 {} id 설정 : 102L",
//                        request.getMember());
//            }
//            if ("정혜윤".equals(request.getMember())) {
//                member.setMemberId(103L); // ID 직접 지정
//                logger.info("🍀 {} id 설정 : 103L",
//                        request.getMember());
//            }
//            if ("허원혁".equals(request.getMember())) {
//                member.setMemberId(104L); // ID 직접 지정
//                logger.info("🍀 {} id 설정 : 104L",
//                        request.getMember());
//            }
//
//            memberRepository.save(member);
//        }
//        else{
//
//            Member existingMember = memberRepository.findByUserGroupAndNickname(userGroup, request.getMember())
//                    .orElseThrow(() -> new RuntimeException("기존 멤버를 찾을 수 없습니다."));
//            oldId = existingMember.getMemberId();
//
//            if ("이다빈".equals(request.getMember())) {
//                customId = 101L; // ID 직접 지정
//            }
//            if ("임재민".equals(request.getMember())) {
//                customId = 102L; // ID 직접 지정
//            }
//            if ("정혜윤".equals(request.getMember())) {
//                customId = 103L; // ID 직접 지정
//            }
//            if ("허원혁".equals(request.getMember())) {
//                customId = 104L; // ID 직접 지정
//            }
//            memberService.recreateMemberWithCustomId(oldId, customId);
//            logger.info("🍀 {} id 수정 : {}",
//                    request.getMember(), customId);
//        }


        // 모든 멤버 조회 후 응답 반환
        List<Member> members = memberRepository.findByUserGroup(userGroup);
        List<GroupCreateResponse.MemberInfo> memberInfos = members.stream()
                .map(m -> new GroupCreateResponse.MemberInfo(m.getNickname(), m.getMemberId()))
                .toList();

        return new GroupCreateResponse(userGroup.getGroupId(), memberInfos);
    }

}
