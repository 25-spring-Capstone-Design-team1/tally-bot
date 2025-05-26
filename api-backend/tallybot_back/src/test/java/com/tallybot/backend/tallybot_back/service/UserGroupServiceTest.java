package com.tallybot.backend.tallybot_back.service;

import com.tallybot.backend.tallybot_back.domain.UserGroup;
import com.tallybot.backend.tallybot_back.domain.Member;
import com.tallybot.backend.tallybot_back.dto.GroupCreateRequest;
import com.tallybot.backend.tallybot_back.dto.GroupCreateResponse;
import com.tallybot.backend.tallybot_back.repository.GroupRepository;
import com.tallybot.backend.tallybot_back.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class UserGroupServiceTest {

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
    @DisplayName("신규 그룹과 신규 멤버를 등록한다")
    void createGroupWithNewGroupAndMember() {
        // given
        Long groupId = 1234L;
        String groupName = "정산방";
        String memberName = "철수";

        GroupCreateRequest request = new GroupCreateRequest(groupId, groupName, memberName);
        UserGroup userGroup = new UserGroup(groupId, groupName);

        when(groupRepository.findById(groupId)).thenReturn(Optional.empty());
        when(groupRepository.save(any(UserGroup.class))).thenReturn(userGroup);
        when(memberRepository.existsByUserGroupAndNickname(userGroup, memberName)).thenReturn(false);

        Member member = new Member();
        member.setUserGroup(userGroup);
        member.setNickname(memberName);
        member.setMemberId(1L);

        when(memberRepository.save(any(Member.class))).thenReturn(member);
        when(memberRepository.findByUserGroup(userGroup)).thenReturn(List.of(member));

        // when
        GroupCreateResponse response = groupService.createGroupWithMember(request);

        // then
        assertThat(response.getGroupId()).isEqualTo(groupId);
        assertThat(response.getMembers()).hasSize(1);
        assertThat(response.getMembers().get(0).getNickname()).isEqualTo("철수");
        assertThat(response.getMembers().get(0).getMemberId()).isEqualTo(1L);

        verify(groupRepository).save(any(UserGroup.class));
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    @DisplayName("기존 그룹에 중복 멤버가 존재하면 추가하지 않는다")
    void createGroupWithExistingGroupAndDuplicateMember() {
        // given
        Long groupId = 1234L;
        String groupName = "정산방";
        String memberName = "철수";

        GroupCreateRequest request = new GroupCreateRequest(groupId, groupName, memberName);
        UserGroup userGroup = new UserGroup(groupId, groupName);

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(userGroup));
        when(memberRepository.existsByUserGroupAndNickname(userGroup, memberName)).thenReturn(true);

        Member existingMember = new Member();
        existingMember.setUserGroup(userGroup);
        existingMember.setNickname(memberName);
        existingMember.setMemberId(1L);

        when(memberRepository.findByUserGroup(userGroup)).thenReturn(List.of(existingMember));

        // when
        GroupCreateResponse response = groupService.createGroupWithMember(request);

        // then
        assertThat(response.getGroupId()).isEqualTo(groupId);
        assertThat(response.getMembers()).hasSize(1);
        assertThat(response.getMembers().get(0).getNickname()).isEqualTo("철수");

        verify(memberRepository, never()).save(any(Member.class)); // 저장 안 함
    }

}
