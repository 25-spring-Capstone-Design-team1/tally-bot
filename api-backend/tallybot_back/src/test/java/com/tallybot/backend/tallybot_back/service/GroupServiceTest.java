package com.tallybot.backend.tallybot_back.service;

import com.tallybot.backend.tallybot_back.domain.Group;
import com.tallybot.backend.tallybot_back.domain.Member;
import com.tallybot.backend.tallybot_back.dto.GroupCreateRequest;
import com.tallybot.backend.tallybot_back.repository.GroupRepository;
import com.tallybot.backend.tallybot_back.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class GroupServiceTest {

    @InjectMocks
    private GroupService groupService;

    @Mock private GroupRepository groupRepository;
    @Mock private MemberRepository memberRepository;

    private GroupCreateRequest request;

    @BeforeEach
    void setUp() {
        request = new GroupCreateRequest();
        request.setGroupName("여행팀");
        request.setMembers(List.of("철수", "영희", "민수"));
    }

    @Test
    @DisplayName("그룹 및 멤버 생성 테스트")
    void createGroupWithMembersTest() {
        // given
        Group savedGroup = new Group();
        savedGroup.setGroupId(1L);
        savedGroup.setGroupName("여행팀");

        when(groupRepository.save(any(Group.class))).thenReturn(savedGroup);

        // when
        groupService.createGroupWithMembers(request);

        // then
        verify(groupRepository).save(any(Group.class));
        verify(memberRepository, times(3)).save(any(Member.class)); // 멤버 3명 저장 확인
    }
}
