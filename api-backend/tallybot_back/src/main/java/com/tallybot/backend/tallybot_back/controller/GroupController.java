package com.tallybot.backend.tallybot_back.controller;

import com.tallybot.backend.tallybot_back.domain.Calculate;
import com.tallybot.backend.tallybot_back.domain.Group;
import com.tallybot.backend.tallybot_back.domain.Member;
import com.tallybot.backend.tallybot_back.dto.*;
import com.tallybot.backend.tallybot_back.repository.CalculateRepository;
import com.tallybot.backend.tallybot_back.repository.GroupRepository;
import com.tallybot.backend.tallybot_back.repository.MemberRepository;
import com.tallybot.backend.tallybot_back.service.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/group")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;
    private final GroupRepository groupRepository;
    private final MemberRepository memberRepository;
    private final CalculateRepository calculateRepository;

    @PostMapping("/create")
    public ResponseEntity<GroupCreateResponse> createGroup(@Valid @RequestBody GroupCreateRequest request) {
        GroupCreateResponse response = groupService.createGroupWithMember(request);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/{groupId}")
    public ResponseEntity<?> getGroupInfo(@PathVariable Long groupId) {
        if (groupId == null || groupId <= 0) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Group ID must be positive."));
        }

        Optional<Group> optionalGroup = groupRepository.findById(groupId);
        if (optionalGroup.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Group not found."));
        }

        Group group = optionalGroup.get();

        int memberCount = memberRepository.countByGroup(group);
        int calculateCount = calculateRepository.countByGroup(group);

        FrontGroupDto response = new FrontGroupDto(
                group.getGroupId(),
                group.getGroupName(),
                memberCount,
                calculateCount
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{groupId}/members")
    public ResponseEntity<?> getGroupMembers(@PathVariable Long groupId) {
        // ID 유효성 체크
        if (groupId == null || groupId <= 0) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Group ID must not be positive."));
        }

        // 그룹 존재 여부 확인
        Optional<Group> optionalGroup = groupRepository.findById(groupId);
        if (optionalGroup.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Group not found."));
        }

        Group group = optionalGroup.get();

        // 해당 그룹에 속한 멤버 목록 조회
        List<Member> members = memberRepository.findByGroup(group);
        List<FrontMemberDto> result = members.stream()
                .map(m -> new FrontMemberDto(m.getMemberId(), m.getNickname()))
                .toList();

        return ResponseEntity.ok(result);
    }


    @GetMapping("/{groupId}/calculates")
    public ResponseEntity<?> getGroupCalculates(@PathVariable Long groupId) {
        // 유효성 검사
        if (groupId == null || groupId <= 0) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Group ID must be positive."));
        }

        Optional<Group> optionalGroup = groupRepository.findById(groupId);
        if (optionalGroup.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Group not found."));
        }

        Group group = optionalGroup.get();

        List<Calculate> calculates = calculateRepository.findByGroup(group);
        List<FrontCalculateDto> result = calculates.stream()
                .map(c -> new FrontCalculateDto(
                        c.getCalculateId(),
                        c.getStartTime(),
                        c.getEndTime(),
                        c.getStatus()
                ))
                .toList();

        return ResponseEntity.ok(result);
    }
}
