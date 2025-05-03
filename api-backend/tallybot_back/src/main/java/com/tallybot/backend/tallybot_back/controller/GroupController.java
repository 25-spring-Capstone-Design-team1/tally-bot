package com.tallybot.backend.tallybot_back.controller;

import com.tallybot.backend.tallybot_back.dto.GroupCreateRequest;
import com.tallybot.backend.tallybot_back.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/group")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @PostMapping("/create")
    public ResponseEntity<String> createGroup(@RequestBody GroupCreateRequest request) {
        groupService.createGroupWithMembers(request);
        return ResponseEntity.ok("그룹 생성 완료");
    }
}
