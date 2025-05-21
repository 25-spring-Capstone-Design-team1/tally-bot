package com.tallybot.backend.tallybot_back.service;

import com.tallybot.backend.tallybot_back.domain.*;
import com.tallybot.backend.tallybot_back.dto.SettlementDto;
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
        // Chat → GPT에 전달할 포맷으로 변환
        List<Map<String, String>> conversation = new ArrayList<>();

        // 시스템 메시지 - 멤버 목록 전송
        String systemMessage = createSystemMessage(chats);  // 아래 설명 있음
        conversation.add(Map.of(
                "speaker", "system",
                "message_content", systemMessage
        ));

        // 일반 유저 메시지
        for (Chat chat : chats) {
            conversation.add(Map.of(
                    "speaker", "user",
                    "message_content", chat.getMessage()
            ));
        }

        //  요청 바디 구성
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("conversation", conversation);
        requestBody.put("prompt_file", "resources/input_prompt.yaml");

        // FastAPI 서버로 요청
        String url = "http://localhost:8000/api/process";
        ResponseEntity<SettlementDto[]> response = restTemplate.postForEntity(
                url,
                requestBody,
                SettlementDto[].class
        );

        return Arrays.asList(response.getBody());
    }

    private String createSystemMessage(List<Chat> chats) {
        Set<String> memberNames = chats.stream()
                .map(chat -> chat.getMember().getNickname())
                .collect(Collectors.toSet());

        return "members: " + memberNames + "\nmember_count: " + memberNames.size();
    }
}
