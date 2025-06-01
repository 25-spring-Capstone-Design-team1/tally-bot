package com.tallybot.backend.tallybot_back.service;

import com.tallybot.backend.tallybot_back.domain.*;
import com.tallybot.backend.tallybot_back.dto.ChatForGptDto;
import com.tallybot.backend.tallybot_back.dto.PythonMessageDto;
import com.tallybot.backend.tallybot_back.dto.PythonRequestDto;
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



    public List<SettlementDto> returnResults(Long groupId, List<ChatForGptDto> chatDtos) {
        List<Map<String, String>> members = chatDtos.stream()
                .map(dto -> Map.of(String.valueOf(dto.getMemberId()), dto.getNickname()))
                .distinct()
                .collect(Collectors.toList());

        List<PythonMessageDto> messages = chatDtos.stream()
                .map(dto -> new PythonMessageDto(
                        String.valueOf(dto.getChatId()),
                        String.valueOf(dto.getMemberId()),
                        dto.getMessage(),
                        dto.getTimestamp().toString()
                ))
                .collect(Collectors.toList());


        PythonRequestDto requestDto = new PythonRequestDto(groupId, members, messages);

        //실제 gpt 서버 주소
//        String url = "http://localhost:8000/api/process";

        //테스트용 mock 주소
        String url = "http://localhost:8080/api/process";

        try {
            ResponseEntity<SettlementDto[]> response = restTemplate.postForEntity(
                    url,
                    requestDto,
                    SettlementDto[].class
            );

            SettlementDto[] responseBody = response.getBody();
            if (responseBody == null || responseBody.length == 0) {
                throw new NoSettlementResultException("정산 결과가 존재하지 않습니다.");
            }

            return Arrays.asList(responseBody);

        } catch (NoSettlementResultException e) {
            throw e;
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
