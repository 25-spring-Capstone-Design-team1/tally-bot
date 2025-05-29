package com.tallybot.backend.tallybot_back.service;

import com.tallybot.backend.tallybot_back.domain.*;
import com.tallybot.backend.tallybot_back.dto.SettlementDto;
import com.tallybot.backend.tallybot_back.exception.NoSettlementResultException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;


@Service
public class GPTService {
    private final RestTemplate restTemplate;

    public GPTService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }


    public List<SettlementDto> returnResults(List<Chat> chats) {
        List<Map<String, String>> conversation = new ArrayList<>();

        // 시스템 메시지 - 멤버 목록 전송
        String systemMessage = createSystemMessage(chats);
        conversation.add(Map.of(
                "speaker", "system",
                "message_content", systemMessage
        ));

        for (Chat chat : chats) {
            conversation.add(Map.of(
                    "speaker", "user",
                    "message_content", chat.getMessage()
            ));
        }

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("conversation", conversation);
        requestBody.put("prompt_file", "resources/input_prompt.yaml");

        String url = "http://localhost:8000/api/process";

        try {
            ResponseEntity<SettlementDto[]> response = restTemplate.postForEntity(
                    url,
                    requestBody,
                    SettlementDto[].class
            );

            SettlementDto[] responseBody = response.getBody();

            // 응답이 null이거나 길이가 0인 경우
            if (responseBody == null || responseBody.length == 0) {
                throw new NoSettlementResultException("정산 결과가 존재하지 않습니다.");
            }

            return Arrays.asList(responseBody);

        } catch (NoSettlementResultException e) {
            throw e; // 예외 그대로 던지기
        } catch (Exception e) {
            System.err.println("GPT 요청 실패: " + e.getMessage());
            throw new RuntimeException("GPT 서버 응답 처리 중 오류가 발생했습니다.", e);
        }

    }


    private String createSystemMessage(List<Chat> chats) {
        Set<String> memberNames = chats.stream()
                .map(chat -> chat.getMember().getNickname())
                .collect(Collectors.toSet());

        return "members: " + memberNames + "\nmember_count: " + memberNames.size();
    }
}
