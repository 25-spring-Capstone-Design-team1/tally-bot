//package com.tallybot.backend.tallybot_back.service;
//
//import com.tallybot.backend.tallybot_back.domain.*;
//import com.tallybot.backend.tallybot_back.dto.*;
//import com.tallybot.backend.tallybot_back.repository.*;
//import jakarta.transaction.Transactional;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//public class FrontService {
//
//    private final CalculateRepository calculateRepository;
//    private final MemberRepository memberRepository;
//    private final SettlementRepository settlementRepository;
//    private final CalculateDetailRepository calculateDetailRepository;
//
//
////    public FrontResponseDto getFullResult(Long calculateId) {
////        Calculate calculate = calculateRepository.findById(calculateId)
////                .orElseThrow(() -> new IllegalArgumentException("정산 없음"));
////
////        Group group = calculate.getGroup();
////        List<Member> members = memberRepository.findByGroup(group);
////        List<Settlement> settlements = settlementRepository.findByGroup(group);
////        List<CalculateDetail> details = calculateDetailRepository.findByCalculate(calculate);
////
////        // DTO로 변환 - 프론트에 전송할 형식으로 변환
////        FrontGroupDto groupDto = new FrontGroupDto(group.getGroupId(), group.getGroupName());
////        List<FrontMemberDto> memberDtos = members.stream()
////                .map(m -> new FrontMemberDto(m.getMemberId(), m.getNickname()))
////                .toList();
////
////        FrontCalculateDto calculateDto = new FrontCalculateDto(
////                calculate.getCalculateId(),
////                calculate.getStartTime().toString(),
////                calculate.getEndTime().toString(),
////                calculate.getStatus()
////        );
////
////        List<FrontSettlementDto> settlementDtos = settlements.stream().map(s -> new FrontSettlementDto(
////                s.getSettlementId(),
////                s.getPlace(),
////                s.getItem(),
////                s.getAmount(),
////                new FrontMemberDto(s.getPayer().getMemberId(), s.getPayer().getNickname()),
////                s.getParticipants().stream()
////                        .map(p -> new FrontMemberDto(p.getMemberId(), p.getNickname()))
////                        .toList()
////        )).toList();
////
////        List<FrontCalculateDetailDto> detailDtos = details.stream().map(d -> new FrontCalculateDetailDto(
////                d.getPayer().getNickname(),
////                d.getPayee().getNickname(),
////                d.getAmount()
////        )).toList();
////
////        return new FrontResponseDto(groupDto, memberDtos, calculateDto, settlementDtos, detailDtos);
////    }
//
//    @Transactional
//    public void markAsCompleted(Long calculateId) {
//        Calculate calculate = calculateRepository.findById(calculateId)
//                .orElseThrow(() -> new IllegalArgumentException("정산 없음"));
//
//        calculate.setStatus(CalculateStatus.COMPLETED);
//        calculateRepository.save(calculate);
//    }
//
//}
