package com.tallybot.backend.tallybot_back.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tallybot.backend.tallybot_back.dto.GroupCreateRequest;
import com.tallybot.backend.tallybot_back.dto.GroupCreateResponse;
import com.tallybot.backend.tallybot_back.service.GroupService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GroupController.class)
class GroupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GroupService groupService;

    @Autowired
    private ObjectMapper objectMapper;


    @Test
    @DisplayName("200 OK : 그룹 생성 성공")
    void createGroupSuccess() throws Exception {
        GroupCreateRequest request = new GroupCreateRequest("정산방", List.of("철수", "영희", "민수"));
        GroupCreateResponse response = new GroupCreateResponse(42L, List.of(
                new GroupCreateResponse.MemberInfo("철수", 1L),
                new GroupCreateResponse.MemberInfo("영희", 2L),
                new GroupCreateResponse.MemberInfo("민수", 3L)
        ));

        given(groupService.createGroupWithMembers(any())).willReturn(response);

        mockMvc.perform(post("/api/group/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()) //200 ok인지 검증
                .andExpect(jsonPath("$.groupId").value(42))
                .andExpect(jsonPath("$.members[0].nickname").value("철수"))
                .andExpect(jsonPath("$.members[1].memberId").value(2));
    }

    @Test
    @DisplayName("400 Bad Request : 그룹 이름 누락")
    void createGroupWithoutName() throws Exception {
        String json = """
            {
              "groupName": "",
              "members": ["철수", "영희"]
            }
            """;

        mockMvc.perform(post("/api/group/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Group name must not be empty."));
    }

    @Test
    @DisplayName("400 Bad Request : 멤버 리스트 누락")
    void createGroupWithoutMembers() throws Exception {
        String json = """
            {
              "groupName": "정산방",
              "members": []
            }
            """;

        mockMvc.perform(post("/api/group/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Members list must not be empty."));
    }

    @Test
    @DisplayName("400 Bad Request : 멤버 중 빈 문자열 존재")
    void createGroupWithEmptyNickname() throws Exception {
        String json = """
            {
              "groupName": "정산방",
              "members": ["철수", ""]
            }
            """;

        mockMvc.perform(post("/api/group/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("All member nicknames must be non-empty strings."));
    }
}
