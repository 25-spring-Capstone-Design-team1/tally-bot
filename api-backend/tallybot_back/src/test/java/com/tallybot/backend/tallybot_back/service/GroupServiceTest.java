package com.tallybot.backend.tallybot_back.service;

import com.tallybot.backend.tallybot_back.domain.Group;
import com.tallybot.backend.tallybot_back.domain.Member;
import com.tallybot.backend.tallybot_back.dto.GroupCreateRequest;
import com.tallybot.backend.tallybot_back.dto.GroupCreateResponse;
import com.tallybot.backend.tallybot_back.repository.GroupRepository;
import com.tallybot.backend.tallybot_back.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class GroupServiceTest {

    private GroupRepository groupRepository;
    private MemberRepository memberRepository;
    private GroupService groupService;

    @BeforeEach
    void setUp() {
        groupRepository = mock(GroupRepository.class);
        memberRepository = mock(MemberRepository.class);
        groupService = new GroupService(groupRepository, memberRepository);
    }

    @Test
    @DisplayName("그룹과 멤버가 정상적으로 생성됨")
    void createGroupWithMembers_success() {
        // given
        GroupCreateRequest request = new GroupCreateRequest("정산방", List.of("철수", "영희"));

        Group savedGroup = new Group();
        savedGroup.setGroupId(42L);
        savedGroup.setGroupName("정산방");

        when(groupRepository.save(any(Group.class))).thenReturn(savedGroup);
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> {
            Member m = invocation.getArgument(0);
            m.setMemberId((long) (Math.random() * 100)); // 임의로 ID 부여
            return m;
        });

        // when
        GroupCreateResponse response = groupService.createGroupWithMembers(request);

        // then
        assertThat(response.getGroupId()).isEqualTo(42L);
        assertThat(response.getMembers()).hasSize(2);
        assertThat(response.getMembers()).extracting("nickname")
                .containsExactlyInAnyOrder("철수", "영희");

        // Repository 호출 여부 확인
        verify(groupRepository, times(1)).save(any(Group.class));
        verify(memberRepository, times(2)).save(any(Member.class));
    }
}
