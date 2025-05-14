//package com.tallybot.backend.tallybot_back.service;
//
//import com.tallybot.backend.tallybot_back.domain.*;
//import com.tallybot.backend.tallybot_back.dto.*;
//import com.tallybot.backend.tallybot_back.repository.*;
//import jakarta.transaction.Transactional;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import java.util.*;
//import java.util.stream.*;
//
//
//@Service
//@Transactional
//@RequiredArgsConstructor
//public class SettlementService {
//
//    private final SettlementRepository settlementRepository;
//    private final MemberRepository memberRepository;
//    private final CalculateRepository calculateRepository;
//
//    @Transactional
//    public void applySettlementUpdate(SettlementUpdateRequest request) {
//        Map<Long, Integer> constants = request.getConstants() != null ? request.getConstants() : new HashMap<>();
//        Map<Long, Integer> ratios = request.getRatios() != null ? request.getRatios() : new HashMap<>();
//        Integer sum = request.getSum() != null ? request.getSum() : 0;
//        ///settlement add
//        if ("add".equals(request.getField())) {
//            Map<String, Object> newValueMap = (Map<String, Object>) request.getNewValue();
//
//            /// new settlement 생성
//            Settlement newSettlement = new Settlement();
//
//            /// item 저장
//            newSettlement.setItem((String) newValueMap.get("item"));
//
//            /// amount 저장
//            newSettlement.setAmount((Integer) newValueMap.get("amount"));
//
//            /// payer 저장
//            // memberId로 결제자(Member) 조회
//            Long payerId = (Long) newValueMap.get("payerId");  // payerId를 받아옴
//            // payer를 Id로 조회
//            Member payer = memberRepository.findById(payerId)
//                    .orElseThrow(() -> new IllegalArgumentException("결제자 없음"));
//            // newSettlement에 payer 설정
//            newSettlement.setPayer(payer);
//
//
//            /// participants 저장
//            Object rawParticipantsIds = request.getNewValue();
//            List<Long> participantIds = new ArrayList<>();
//            if (rawParticipantsIds instanceof List<?>) {
//                for (Object o : (List<?>) rawParticipantsIds) {
//                    if (o instanceof Long id) {
//                        participantIds.add(id);
//                    } else {
//                        throw new IllegalArgumentException("participants 항목은 Long 타입의 memberId이어야 합니다.");
//                    }
//                }
//            } else {
//                throw new IllegalArgumentException("participants 값이 리스트가 아닙니다.");
//            }
//
//            List<Member> participants = memberRepository.findByIdIn(participantIds);
//            Set<Participant> participantSet = new HashSet<>();
//            for (Member member : participants) {
//                Participant.ParticipantKey participantKey = new Participant.ParticipantKey(newSettlement, member);
//
//                // participants 수정 시 constant와 ratio 값을 `newValue`에서 가져오기
//                Long memberId = member.getMemberId();  // memberId (Long 타입)
//
//                // constant와 ratio 값을 constants와 ratios에서 가져오기
//                Integer constant = request.getConstants().get(memberId);  // memberId를 기준으로 constant 조회
//                Integer ratioValue = request.getRatios().get(memberId);
//                Integer ratioSum = request.getSum();
//                Ratio ratio = new Ratio(ratioValue, ratioSum);
//
//                // Participant 객체에 추가
//                Participant participant = new Participant(participantKey, constant, ratio);
//                participantSet.add(participant);
//            }
//
//            newSettlement.setParticipants(participantSet);
//
//            ///group 저장
//            Group group = payer.getGroup();
//            newSettlement.setGroup(group);
//
//            Calculate calculate = calculateRepository.findById(request.getCalculateId())
//                    .orElseThrow(() -> new IllegalArgumentException("해당 정산 ID 없음"));
//            newSettlement.setCalculate(calculate);
//
//            settlementRepository.save(newSettlement);
//            return;
//        }
//
//        ///settlement 수정 및 삭제
//        Settlement settlement = settlementRepository.findById(request.getSettlementId())
//                .orElseThrow(() -> new IllegalArgumentException("해당 정산 내역이 존재하지 않음"));
//
//        switch (request.getField()) {
//            /// item 수정
//            case "item" -> {
//                Object raw = request.getNewValue();
//                if (raw instanceof String item) {
//                    settlement.setItem(item);
//                } else {
//                    throw new IllegalArgumentException("item 값은 문자열이어야 합니다.");
//                }
//            }
//            /// amount 수정
//            case "amount" -> {
//                Integer amount;
//                Object rawAmount = request.getNewValue();
//                if (rawAmount instanceof Integer i) {
//                    amount = i;
//                } else if (rawAmount instanceof String s) {
//                    amount = Integer.parseInt(s);
//                } else {
//                    throw new IllegalArgumentException("amount 값이 유효하지 않음");
//                }
//                settlement.setAmount(amount);
//            }
//            /// payer 수정
//            case "payer" -> {
//                Object rawPayerId = request.getNewValue();
//                if (rawPayerId instanceof Long payerId) {
//                    Member payer = memberRepository.findById(payerId)
//                            .orElseThrow(() -> new IllegalArgumentException("결제자가 존재하지 않습니다."));
//                    settlement.setPayer(payer);
//                } else {
//                    throw new IllegalArgumentException("payer 값은 Long 타입의 memberId여야 합니다.");
//                }
//            }
//            /// participants 수정
//            case "participants" -> {
//                Object rawParticipantIds = request.getNewValue();
//                List<Long> participantIds = new ArrayList<>();
//                if (rawParticipantIds instanceof List<?>) {
//                    for (Object o : (List<?>) rawParticipantIds) {
//                        if (o instanceof Long id) {
//                            participantIds.add(id);
//                        } else {
//                            throw new IllegalArgumentException("participants 항목은 Long 타입의 memberId이어야 합니다.");
//                        }
//                    }
//                } else {
//                    throw new IllegalArgumentException("participants 값이 리스트가 아닙니다.");
//                }
//
//                List<Member> participants = memberRepository.findByIdIn(participantIds);
//                Set<Participant> participantSet = new HashSet<>();
//                for (Member member : participants) {
//                    Participant.ParticipantKey participantKey = new Participant.ParticipantKey(settlement, member);
//
//                    // participants 수정 시 constant와 ratio 값을 `newValue`에서 가져오기
//                    Long memberId = member.getMemberId();  // memberId (Long 타입)
//
//                    // constant와 ratio 값을 constants와 ratios에서 가져오기
//                    Integer constant = request.getConstants().get(memberId);  // memberId를 기준으로 constant 조회
//                    Integer ratioValue = request.getRatios().get(memberId);
//                    Integer ratioSum = request.getSum();
//                    Ratio ratio = new Ratio(ratioValue, ratioSum);
//
//                    // Participant 객체에 추가
//                    Participant participant = new Participant(participantKey, constant, ratio);
//                    participantSet.add(participant);
//                }
//
//                settlement.setParticipants(participantSet);
//            }
//
//            case "delete" -> {
//                settlementRepository.delete(settlement);
//                return;
//            }
//            default -> throw new IllegalArgumentException("알 수 없는 수정항목");
//        }
//
//        settlementRepository.save(settlement);
//    }
//
//    /*
//     * 각 정산에 대하여 참여하는 사람의 Nickname을 가져온다.
//     */
//    public List<String> nicknamesInCalculate(List<Settlement> settlementList) {
//        Stream<Settlement> settlements = settlementList.stream();
//
//        // 각 정산의 참여자들을 Set을 이용해 겹치지 않게 합한다.
//        Stream<String> members = settlements.flatMap(settlement -> {
//            Long payer = settlement.getPayer().getMemberId();
//            Set<Long> payeeIds = settlement.getParticipants().stream().map(participant
//                    -> participant.getParticipantKey().member.getMemberId()).collect(Collectors.toSet());
//            payeeIds.add(payer);
//            return payeeIds.stream();
//        }).map(id -> {
//            return memberRepository.findById(id).orElseThrow(
//                    () -> new IllegalArgumentException("해당자 없음"));
//        }).map(Member::getNickname);
//
//        // List 형태로 변환하여 반환한다.
//        return members.collect(Collectors.toList());
//    }
//
//    /*
//     * GPT에서 넘어오는 settlement 관련 정보를
//     * settlement의 ID, Group 객체, calculate의 ID 등을 사용하여 DB의 Settlement 객체로 변환한다.
//     */
//    public Settlement toSettlement(SettlementDto settlementDto, Long settlementId, Group group, Long calculateId) {
//        // 정보 채우기
//        Settlement settlement = new Settlement();
//        settlement.setSettlementId(settlementId);
//
//        settlement.setPlace(settlementDto.getPlace());
//        settlement.setItem(settlementDto.getItem());
//        settlement.setAmount(settlementDto.getAmount());
//
//        settlement.setGroup(group);
//
//        // 닉네임으로 관여 멤버 찾기
//        var payer = memberRepository.findByNicknameAndGroup(settlementDto.getPayer(), group)
//                .orElseGet(() -> {
//                    Member member = new Member();
//                    member.setNickname(settlementDto.getPayer());
//                    member.setGroup(group);
//                    return memberRepository.save(member);
//                });
//
//        settlement.setPayer(payer);
//
//        // 비율의 분모를 만들기 위해 합한다.
//        int sum = 0;
//        for (Integer ratio : settlementDto.getRatios().values()) {
//            sum += ratio;
//        }
//
//        // 비율을 분수의 형태로, 고정금액과 함께 각 멤버로 저장, participant 테이블을 체운다.
//        Set<Participant> participants = new HashSet<>();
//        for (int i = 0; i < settlementDto.getParticipants().size(); i++) {
//            int finalI = i;
//            var mem = memberRepository.findByNicknameAndGroup(settlementDto.getParticipants().get(i), group)
//                    .orElseGet(() -> {
//                        Member member = new Member();
//                        member.setNickname(settlementDto.getParticipants().get(finalI));
//                        member.setGroup(group);
//                        return memberRepository.save(member);
//                    });
//            var constant = settlementDto.getConstants().get(settlementDto.getParticipants().get(i));
//            var ratio = settlementDto.getRatios().get(settlementDto.getParticipants().get(i));
//            participants.add(new Participant(new Participant.ParticipantKey(settlement, mem), constant, new Ratio(ratio, sum)));
//        }
//
//        settlement.setParticipants(participants);
//
//        // calculate ID에 해당하는 게 있는지 찾고 없으면 null을 채운다.
//        var calculate = calculateRepository.findById(calculateId)
//                .orElse(null);
//        settlement.setCalculate(calculate);
//
//        return settlement;
//    }
//
//    /*
//     * 여러 항목이 섞여있는 GPT로부터 오는 전체 정산 항목을 종합하여 DB의 Settlement의 List 형태로 변환한다.
//     */
////    public List<Settlement> toSettlements(List<SettlementDto> settlementsDto, Long calculateId) {
////        List<Settlement> settlementList = new ArrayList<>();
////        for (int i = 0; i < settlementsDto.getSettlementDtos().size(); i++) {
////            settlementList.add(toSettlement(settlementsDto.getSettlementDtos().get(i),
////                    null, settlementsDto.getGroup(), calculateId));
////        }
////        return settlementList;
////    }
//
//    public List<Settlement> toSettlements(List<SettlementDto> settlementDtos, Group group, Long calculateId) {
//        List<Settlement> settlementList = new ArrayList<>();
//        for (SettlementDto dto : settlementDtos) {
//            settlementList.add(toSettlement(dto, null, group, calculateId));
//        }
//        return settlementList;
//    }
//
//
//}