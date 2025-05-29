package com.tallybot.backend.tallybot_back.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tallybot.backend.tallybot_back.domain.Calculate;
import com.tallybot.backend.tallybot_back.domain.CalculateStatus;
import com.tallybot.backend.tallybot_back.domain.UserGroup;
import com.tallybot.backend.tallybot_back.domain.Member;
import com.tallybot.backend.tallybot_back.dto.GroupCreateRequest;
import com.tallybot.backend.tallybot_back.dto.GroupCreateResponse;
import com.tallybot.backend.tallybot_back.repository.CalculateRepository;
import com.tallybot.backend.tallybot_back.repository.GroupRepository;
import com.tallybot.backend.tallybot_back.repository.MemberRepository;
import com.tallybot.backend.tallybot_back.service.GroupService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GroupController.class)
@ActiveProfiles({"test", "mock-data"})  // 이 프로파일 조합으로 별도 컨텍스트 생성
class UserGroupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GroupService groupService;

    @MockitoBean
    private MemberRepository memberRepository;

    @MockitoBean
    private GroupRepository groupRepository;

    @MockitoBean
    private CalculateRepository calculateRepository;

    @Autowired
    private ObjectMapper objectMapper;


    @Test
    @DisplayName("200 OK : 그룹 생성 성공")
    void createGroupSuccess() throws Exception {
        GroupCreateRequest request = new GroupCreateRequest(
                2347917394801L,
                "정산방",
                "철수"
        );

        GroupCreateResponse response = new GroupCreateResponse(
                2347917394801L,
                List.of(
                        new GroupCreateResponse.MemberInfo("철수", 1L),
                        new GroupCreateResponse.MemberInfo("영희", 2L)
                )
        );

        given(groupService.createGroupWithMember(any())).willReturn(response);

        mockMvc.perform(post("/api/group/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()) //200 ok인지 검증
                .andExpect(jsonPath("$.groupId").value(2347917394801L))
                .andExpect(jsonPath("$.members[0].nickname").value("철수"))
                .andExpect(jsonPath("$.members[1].memberId").value(2));
    }

    @Test
    @DisplayName("400 Bad Request : 그룹 이름 누락")
    void createGroupWithoutName() throws Exception {
        GroupCreateRequest request = new GroupCreateRequest(
                2347917394801L,
                "", // 누락
                "철수"
        );

        mockMvc.perform(post("/api/group/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Group name must not be empty."));
    }

    @Test
    @DisplayName("400 Bad Request : 멤버 이름 누락")
    void createGroupWithoutMembers() throws Exception {
        GroupCreateRequest request = new GroupCreateRequest(
                2347917394801L,
                "정산방",
                "" // 빈 멤버
        );

        mockMvc.perform(post("/api/group/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Member nickname must not be empty."));
    }


    @Test
    @DisplayName("200 ok : 그룹 정보 조회 성공")
    void getGroupInfo_success() throws Exception {
        // given
        UserGroup userGroup = new UserGroup();
        userGroup.setGroupId(42L);
        userGroup.setGroupName("정산방");

        Mockito.when(groupRepository.findById(42L)).thenReturn(Optional.of(userGroup));
        Mockito.when(memberRepository.countByUserGroup(userGroup)).thenReturn(3);
        Mockito.when(calculateRepository.countByUserGroup(userGroup)).thenReturn(2);

        // when & then
        mockMvc.perform(get("/api/group/42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groupId").value(42))
                .andExpect(jsonPath("$.groupName").value("정산방"))
                .andExpect(jsonPath("$.memberCount").value(3))
                .andExpect(jsonPath("$.calculateCount").value(2));
    }

    @Test
    @DisplayName("400 Bad Request : 잘못된 groupId (0 이하)")
    void getGroupInfo_invalidId() throws Exception {
        mockMvc.perform(get("/api/group/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Group ID must be positive."));
    }

    @Test
    @DisplayName("404 Not Found : 존재하지 않는 그룹")
    void getGroupInfo_notFound() throws Exception {
        Mockito.when(groupRepository.findById(eq(999L))).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/group/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Group not found."));
    }

    @Test
    @DisplayName("200 ok : 그룹 멤버 조회 성공")
    void getGroupMembers_success() throws Exception {
        // given
        UserGroup userGroup = new UserGroup();
        userGroup.setGroupId(42L);
        userGroup.setGroupName("정산방");

        Member m1 = new Member();
        m1.setMemberId(1L);
        m1.setNickname("철수");
        m1.setUserGroup(userGroup);

        Member m2 = new Member();
        m2.setMemberId(2L);
        m2.setNickname("영희");
        m2.setUserGroup(userGroup);

        Mockito.when(groupRepository.findById(42L)).thenReturn(Optional.of(userGroup));
        Mockito.when(memberRepository.findByUserGroup(userGroup)).thenReturn(List.of(m1, m2));

        // when & then
        mockMvc.perform(get("/api/group/42/members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].memberId").value(1))
                .andExpect(jsonPath("$[0].nickname").value("철수"))
                .andExpect(jsonPath("$[1].memberId").value(2))
                .andExpect(jsonPath("$[1].nickname").value("영희"));
    }

    @Test
    @DisplayName("400 Bad Request : 잘못된 groupId (0 이하)")
    void getGroupMembers_invalidId() throws Exception {
        mockMvc.perform(get("/api/group/0/members"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Group ID must not be positive."));
    }

    @Test
    @DisplayName("404 Not Found : 존재하지 않는 그룹")
    void getGroupMembers_notFound() throws Exception {
        Mockito.when(groupRepository.findById(eq(999L))).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/group/999/members"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Group not found."));
    }

    @Test
    @DisplayName("200 OK : 정산 목록 조회 성공")
    void getGroupCalculates_success() throws Exception {
        // given
        UserGroup userGroup = new UserGroup();
        userGroup.setGroupId(42L);
        userGroup.setGroupName("정산방");

        Calculate c1 = new Calculate();
        c1.setCalculateId(101L);
        c1.setStartTime(LocalDateTime.of(2025, 5, 8, 11, 0));
        c1.setEndTime(LocalDateTime.of(2025, 5, 8, 13, 0));
        c1.setStatus(CalculateStatus.PENDING);

        Calculate c2 = new Calculate();
        c2.setCalculateId(102L);
        c2.setStartTime(LocalDateTime.of(2025, 5, 9, 10, 0));
        c2.setEndTime(LocalDateTime.of(2025, 5, 9, 12, 0));
        c2.setStatus(CalculateStatus.COMPLETED);

        // when
        Mockito.when(groupRepository.findById(42L)).thenReturn(Optional.of(userGroup));
        Mockito.when(calculateRepository.findByUserGroup(userGroup)).thenReturn(List.of(c1, c2));

        // then
        mockMvc.perform(get("/api/group/42/calculates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].calculateId").value(101))
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$[1].calculateId").value(102))
                .andExpect(jsonPath("$[1].status").value("COMPLETED"));
    }

    @Test
    @DisplayName("400 Bad Request : 잘못된 groupId")
    void getGroupCalculates_invalidId() throws Exception {
        mockMvc.perform(get("/api/group/0/calculates"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Group ID must be positive."));
    }

    @Test
    @DisplayName("404 Not Found : 그룹 없음")
    void getGroupCalculates_notFound() throws Exception {
        Mockito.when(groupRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/group/999/calculates"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Group not found."));
    }
}
