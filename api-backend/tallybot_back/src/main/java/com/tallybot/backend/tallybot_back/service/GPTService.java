package com.tallybot.backend.tallybot_back.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tallybot.backend.tallybot_back.domain.*;
import com.tallybot.backend.tallybot_back.dto.*;
import com.tallybot.backend.tallybot_back.exception.NoSettlementResultException;
import com.tallybot.backend.tallybot_back.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


import java.util.List;

import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class GPTService {
    private final RestTemplate restTemplate;
    private final GroupRepository groupRepository;
    private static final Logger logger = LoggerFactory.getLogger(GPTService.class);


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


        String chatroomName = groupRepository.findById(groupId)
                .map(UserGroup::getGroupName)
                .orElse("Unnamed Group");

        PythonRequestDto requestDto = new PythonRequestDto(groupId, chatroomName, members, messages);

        //실제 gpt 서버 주소
        String url = "http://tally-bot-ai-backend-alb-2092930451.ap-northeast-2.elb.amazonaws.com/api/process";

        //테스트용 mock 주소
//        String url = "http://localhost:8080/api/process";

        logger.info("🍀 gptservice에서 조회된 채팅 수: {}", chatDtos.size());
        try {
            ObjectMapper mapper = new ObjectMapper();
            String requestJson = mapper.writeValueAsString(requestDto);
            System.out.println("📤 GPT 요청 JSON:\n" + requestJson);
            logger.info("🍀 GPT 요청 JSON:\n" + requestJson);

//            ResponseEntity<SettlementDto[]> response = restTemplate.postForEntity(
//                    url,
//                    requestDto,
//                    SettlementDto[].class
//            );
//
//            SettlementDto[] responseBody = response.getBody();
//            if (responseBody == null || responseBody.length == 0) {
//                throw new NoSettlementResultException("정산 결과가 존재하지 않습니다.");
//            }
//
//            return Arrays.asList(responseBody);


            // 디버깅 시 임시로 응답 확인
//            ResponseEntity<String> rawResponse = restTemplate.postForEntity(url, requestDto, String.class);
//            System.out.println("❤️GPT 응답 (raw):\n" + rawResponse.getBody());
//            logger.info("🍀GPT 응답 (raw):\n" + rawResponse.getBody());

            ResponseEntity<SettlementResponseWrapper> response = restTemplate.postForEntity(
                    url,
                    requestDto,
                    SettlementResponseWrapper.class
            );

            SettlementResponseWrapper wrapper = response.getBody();

            if (wrapper == null || wrapper.getFinalResult() == null || wrapper.getFinalResult().isEmpty()) {
                throw new NoSettlementResultException("정산 결과가 존재하지 않습니다.");
            }

            return wrapper.getFinalResult();


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
