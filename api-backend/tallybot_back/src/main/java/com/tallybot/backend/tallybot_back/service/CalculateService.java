package com.tallybot.backend.tallybot_back.service;

import com.tallybot.backend.tallybot_back.domain.*;
import com.tallybot.backend.tallybot_back.dto.BotCalculateDto;
import com.tallybot.backend.tallybot_back.dto.BotResponseDetail;
import com.tallybot.backend.tallybot_back.dto.BotResponseDto;
import com.tallybot.backend.tallybot_back.repository.*;
import jakarta.transaction.Transactional;
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
    private final SettlementRepository settlementRepository;

    public Long startCalculate(BotCalculateDto request) {
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

    public BotResponseDto resultReturn(Long calculateId) {
        Calculate calculate = calculateRepository.findById(calculateId)
                .orElseThrow(() -> new IllegalArgumentException("정산 없음"));

        List<CalculateDetail> details = calculateDetailRepository.findByCalculate(calculate);

        List<BotResponseDetail> detailResponses = details.stream().map(detail ->
                new BotResponseDetail(
                        detail.getPayer().getNickname(),
                        detail.getPayee().getNickname(),
                        detail.getAmount()
                )
        ).toList();

        String url = "https://tallybot.me/calculate/" + calculateId;

        return new BotResponseDto(url, detailResponses);
    }

    @Transactional
    public void recalculate(Long calculateId) {
        Calculate calculate = calculateRepository.findById(calculateId)
                .orElseThrow(() -> new IllegalArgumentException("정산 없음"));

        // 기존 결과 삭제
        calculateDetailRepository.deleteByCalculate(calculate);

        // 재분석 없이 프론트에서 전달된 수정 사항만 settlement에 업데이트 (예: 정산 대상자, 금액 등)

        // 웹에서 수정 시 item: value, amount: value 형식으로 들어오니 GPT 추가로 돌리지 않고
        // 내부 함수만 돌리는 코드 작성 필요

        // 상태는 그대로 유지 (PENDING or COMPLETED)
    }




}





