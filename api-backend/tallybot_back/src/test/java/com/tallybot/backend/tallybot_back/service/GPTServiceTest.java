package com.tallybot.backend.tallybot_back.service;

import com.tallybot.backend.tallybot_back.domain.Chat;
import com.tallybot.backend.tallybot_back.domain.Member;
import com.tallybot.backend.tallybot_back.dto.SettlementDto;
import com.tallybot.backend.tallybot_back.exception.NoSettlementResultException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ActiveProfiles("mock-data")  // 이 프로파일 조합으로 별도 컨텍스트 생성
public class GPTServiceTest {
    private RestTemplate restTemplate;
    private GPTService gptService;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        gptService = new GPTService(restTemplate);
    }

    @Test
    void returnResults_success() {
        // given
        Member member = new Member();
        member.setNickname("준호");

        Chat chat = new Chat();
        chat.setMessage("숙소 예약했어 90000원");
        chat.setMember(member);

        List<Chat> chats = List.of(chat);

        SettlementDto mockDto = new SettlementDto();
        mockDto.setPlace("숙소");
        mockDto.setItem("숙소비");
        mockDto.setAmount(90000);
        mockDto.setPayerId(1L);
        mockDto.setParticipantIds(List.of(1L, 2L));
        mockDto.setConstants(Map.of("1", 0, "2", 0));
        mockDto.setRatios(Map.of("1", 1, "2", 1));

        SettlementDto[] mockResponse = new SettlementDto[]{mockDto};
        ResponseEntity<SettlementDto[]> responseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);

        when(restTemplate.postForEntity(anyString(), any(), eq(SettlementDto[].class)))
                .thenReturn(responseEntity);

        // when
        List<SettlementDto> result = gptService.returnResults(chats);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPlace()).isEqualTo("숙소");
        assertThat(result.get(0).getAmount()).isEqualTo(90000);

        // verify request body (optional)
        ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
        verify(restTemplate).postForEntity(eq("http://localhost:8000/api/process"), captor.capture(), eq(SettlementDto[].class));

        Map<String, Object> actualRequest = captor.getValue();
        assertThat(actualRequest).containsKeys("conversation", "prompt_file");
        List<Map<String, String>> conversation = (List<Map<String, String>>) actualRequest.get("conversation");
        assertThat(conversation.get(0).get("speaker")).isEqualTo("system");
        assertThat(conversation.get(1).get("message_content")).isEqualTo("숙소 예약했어 90000원");
    }

    @Test
    void returnResults_shouldThrowException_whenResponseBodyIsNull() {
        // given
        List<Chat> chats = mockChatList();

        given(restTemplate.postForEntity(
                anyString(),
                any(),
                eq(SettlementDto[].class)
        )).willReturn(ResponseEntity.ok(null)); // 응답 body = null

        // expect
        assertThatThrownBy(() -> gptService.returnResults(chats))
                .isInstanceOf(NoSettlementResultException.class)
                .hasMessageContaining("정산 결과가 존재하지 않습니다.");
    }

    @Test
    void returnResults_shouldThrowException_whenResponseBodyIsEmpty() {
        // given
        List<Chat> chats = mockChatList();

        given(restTemplate.postForEntity(
                anyString(),
                any(),
                eq(SettlementDto[].class)
        )).willReturn(ResponseEntity.ok(new SettlementDto[0])); // 빈 배열

        // expect
        assertThatThrownBy(() -> gptService.returnResults(chats))
                .isInstanceOf(NoSettlementResultException.class)
                .hasMessageContaining("정산 결과가 존재하지 않습니다.");
    }

    private List<Chat> mockChatList() {
        Chat chat = new Chat();
        Member member = new Member();
        member.setNickname("Alice");
        chat.setMember(member);
        chat.setMessage("밥 먹자~");
        return Collections.singletonList(chat);
    }
}
