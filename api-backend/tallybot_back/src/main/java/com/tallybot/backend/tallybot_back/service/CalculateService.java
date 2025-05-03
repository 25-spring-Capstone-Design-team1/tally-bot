package com.tallybot.backend.tallybot_back.service;

import com.tallybot.backend.tallybot_back.domain.*;
import com.tallybot.backend.tallybot_back.dto.CalculateDto;
import com.tallybot.backend.tallybot_back.dto.ResponseDetail;
import com.tallybot.backend.tallybot_back.dto.ResponseDto;
import com.tallybot.backend.tallybot_back.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CalculateService {

    private final GroupRepository groupRepository;
    private final CalculateRepository calculateRepository;
    private final ChatRepository chatRepository;
    private final GPTService gptService;
    private final CalculateDetailRepository calculateDetailRepository;

    public Long startCalculate(CalculateDto request) {
        Group group = groupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new IllegalArgumentException("그룹 없음"));

        Calculate calculate = new Calculate();
        calculate.setGroup(group);
        calculate.setStartTime(request.getStartTime());
        calculate.setEndTime(request.getEndTime());
        calculate.setStatus(CalculateStatus.PENDING);
        calculateRepository.save(calculate);

        List<Chat> chats = chatRepository.findByGroupAndTimestampBetween(
                group,
                request.getStartTime(),
                request.getEndTime()
        );

        //gpt 다루는 메소드에 데이터 넘기기
        List<CalculateDetail> results = gptService.returnResults(chats, calculate);

        // 정산 결과 저장
        calculateDetailRepository.saveAll(results);

        // 상태 완료 처리
        calculate.setStatus(CalculateStatus.COMPLETED);
        calculateRepository.save(calculate);

        return calculate.getCalculateId();
    }

    public ResponseDto resultReturn(Long calculateId) {
        Calculate calculate = calculateRepository.findById(calculateId)
                .orElseThrow(() -> new IllegalArgumentException("정산 없음"));

        List<CalculateDetail> details = calculateDetailRepository.findByCalculate(calculate);

        List<ResponseDetail> detailResponses = details.stream().map(detail ->
                new ResponseDetail(
                        detail.getPayer().getNickname(),
                        detail.getPayee().getNickname(),
                        detail.getAmount()
                )
        ).toList();

        String url = "https://tallybot.me/calculate/" + calculateId;

        return new ResponseDto(url, detailResponses);
    }
}





