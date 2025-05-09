package com.tallybot.backend.tallybot_back.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tallybot.backend.tallybot_back.dto.GroupCreateRequest;
import com.tallybot.backend.tallybot_back.service.GroupService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

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
    @DisplayName("그룹 생성 요청 테스트")
    void createGroupTest() throws Exception {
        // given
        GroupCreateRequest request = new GroupCreateRequest();
        request.setGroupName("여행팀");
        request.setMembers(List.of("철수", "영희", "민수"));

        // service 가짜 설정
        doNothing().when(groupService).createGroupWithMembers(request);

        // when + then
        mockMvc.perform(post("/group/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("그룹 생성 완료"));
    }
}
