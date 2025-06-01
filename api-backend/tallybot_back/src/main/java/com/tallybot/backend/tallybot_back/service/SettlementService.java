package com.tallybot.backend.tallybot_back.service;

import com.tallybot.backend.tallybot_back.domain.*;
import com.tallybot.backend.tallybot_back.dto.*;
import com.tallybot.backend.tallybot_back.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;


@Service
@Transactional
@RequiredArgsConstructor
public class SettlementService {

    private final SettlementRepository settlementRepository;
    private final MemberRepository memberRepository;
    private final CalculateRepository calculateRepository;
    private final GroupRepository groupRepository;
    private final OptimizationService optimizationService;
    private final CalculateDetailRepository calculateDetailRepository;


    public boolean fieldExists(String fieldName, SettlementUpdateRequest request)
    {
        Map<String, Object> newValue = request.getNewValue();
        Long calculateId = request.getCalculateId();
        Long settlementId = request.getSettlementId();
        Map<String, Integer> constants = request.getConstants();
        Map<String, Integer> ratios = request.getRatios();
        Integer sum = request.getSum();
        String field = request.getField();

        if (calculateId==null)
            return false;

        if ("add".equals(field))
        {
            if (newValue == null || newValue.get("amount") == null || newValue.get("payer") == null)
                return false;
            else if ((constants == null && ratios == null)||(sum == null))
                return false;
        }
        if ("delete".equals(field) && settlementId == null) {
            return false;
        }
        if ("update".equals(field))
        {
           if (settlementId == null)
               return false;
           if ((newValue.get("participants")!=null)&&(constants==null&&ratios==null))
               return false;
        }

        return true;
    }


    @Transactional
    public Long applySettlementUpdate(SettlementUpdateRequest request) {
        Long settlementId = request.getSettlementId();
        String field = request.getField();
        Map<String, Object> newValue = request.getNewValue()!= null ? request.getNewValue() : new HashMap<>();

        ///settlement add
        if ("add".equals(field)) {
            /// new settlement 생성
            Settlement newSettlement = new Settlement();

            /// place와 item: 기본값 처리
            newSettlement.setPlace((String) newValue.getOrDefault("place", "default"));
            newSettlement.setItem((String) newValue.getOrDefault("item", "default"));

            /// amount 저장
            Object rawAmount = newValue.get("amount");
            if (!(rawAmount instanceof Integer)) {
                throw new IllegalArgumentException("amount는 필수값입니다.");
            }
            newSettlement.setAmount((Integer) rawAmount);

            /// payer 저장
            // memberId로 결제자(Member) 조회
            Object rawPayer = newValue.get("payer");
            Long payerId = null;
            if (rawPayer instanceof Integer i) payerId = i.longValue();
            else if (rawPayer instanceof Long l) payerId = l;
            else throw new IllegalArgumentException("payerId 형식 오류");

            // payer를 Id로 조회
            Member payer = memberRepository.findById(payerId)
                    .orElseThrow(() -> new IllegalArgumentException("결제자 없음"));
            // newSettlement에 payer 설정
            newSettlement.setPayer(payer);


            /// participants 저장
            Set<Participant> participantSet = new HashSet<>();

            // "participants" 키만 명시적으로 처리
            Object rawParticipants = newValue.get("participants");

            List<Member> participants;
            Map<String, Integer> constants = request.getConstants();
            Map<String, Integer> ratios = request.getRatios();
            Integer sum = request.getSum();


            if (rawParticipants == null) {
                // constants 또는 ratios만 있고 participants가 누락된 경우 예외
                if (constants != null || ratios != null) {
                    throw new IllegalArgumentException("participants가 null일 경우 constants/ratios도 null이어야 합니다.");
                }

                participants = memberRepository.findByUserGroup(payer.getUserGroup());
                for (Member member : participants) {
                    Participant.ParticipantKey participantKey = new Participant.ParticipantKey(newSettlement, member);
                    Ratio ratio = new Ratio(1, 1); // 기본값 ratio 1/1
                    Participant participant = new Participant(participantKey, 0, ratio); // 기본값 constant 0
                    participantSet.add(participant);
                }
            }else if (rawParticipants instanceof List<?> rawList) {
                List<Long> participantIds = rawList.stream()
                        .map(obj -> {
                            if (obj instanceof Integer i) return i.longValue();
                            else if (obj instanceof Long l) return l;
                            else throw new IllegalArgumentException("participants 값이 유효하지 않습니다.");
                        })
                        .toList();

                participants = memberRepository.findAllById(participantIds);
                int ratioSum = (sum != null) ? sum : participants.size();  // 참여자 수 기반

                for (Member member : participants) {
                    Participant.ParticipantKey participantKey = new Participant.ParticipantKey(newSettlement, member);

                    Long memberId = member.getMemberId();
                    String memberIdStr = String.valueOf(memberId);

                    Integer constant = constants != null ? constants.get(memberIdStr) : 0;
                    Integer ratioValue = ratios != null ? ratios.get(memberIdStr) : 1;
                    Ratio ratio = new Ratio(ratioValue, ratioSum);

                    Participant participant = new Participant(participantKey, constant, ratio);
                    participantSet.add(participant);
                }
            }
            newSettlement.setParticipants(participantSet);
            newSettlement.setUserGroup(payer.getUserGroup());

            Calculate calculate = calculateRepository.findById(request.getCalculateId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 정산 ID 없음"));
            newSettlement.setCalculate(calculate);

            settlementRepository.save(newSettlement);
            return newSettlement.getSettlementId();
        }

        ///settlement 수정 및 삭제
        Settlement settlement = settlementRepository.findById(request.getSettlementId())
                .orElseThrow(() -> new IllegalArgumentException("해당 정산 내역이 존재하지 않음"));

        if ("delete".equals(field))
         {
             settlementRepository.delete(settlement);


             return settlementId;


         }else{
            for (Map.Entry<String, Object> entry : newValue.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                switch (key) {
                    /// place 수정
                    case "place" -> {
                        if (value instanceof String place) {
                            settlement.setPlace(place);
                        }
                    }
                    /// item 수정
                    case "item" -> {
                        if (value instanceof String item) {
                            settlement.setItem(item);
                        }
                    }
                    /// amount 수정
                    case "amount" -> {
                        Integer amount = null;
                        if (value instanceof Integer i) {
                            amount = i;
                            settlement.setAmount(amount);
                        }
                    }
                    /// payer 수정
                    case "payer" -> {
                        Long payerId = null;
                        if (value instanceof Integer) {
                            payerId = ((Integer) value).longValue();
                        } else if (value instanceof Long) {
                            payerId = (Long) value;
                        }
                        if (payerId != null) {
                            Member payer = memberRepository.findById(payerId)
                                    .orElseThrow(() -> new IllegalArgumentException("Member entity not found."));
                            settlement.setPayer(payer);
                        }
                    }
                    /// participants 수정
                    case "participants" -> {
                        if (value instanceof List<?> rawList) {
                            // 기존 Participant 명시적으로 제거
                            for (Participant old : new HashSet<>(settlement.getParticipants())) {
                                old.getParticipantKey().setSettlement(null);  // 혹시 모르니 관계 끊기
                                settlement.getParticipants().remove(old);     // 리스트에서 제거
                            }

                            // 새 participants 추가
                            List<Long> participantIds = rawList.stream()
                                    .map(obj -> {
                                        if (obj instanceof Integer i) return i.longValue();
                                        else if (obj instanceof Long l) return l;
                                        else throw new IllegalArgumentException("participants 값이 유효하지 않습니다.");
                                    })
                                    .toList();

                            List<Member> participants = memberRepository.findAllById(participantIds);
                            for (Member member : participants) {
                                Participant.ParticipantKey participantKey = new Participant.ParticipantKey(settlement, member);

                                Long memberId = member.getMemberId();
                                String memberIdStr = String.valueOf(memberId);

                                Integer constant = Optional.ofNullable(request.getConstants().get(memberIdStr)).orElse(0);
                                Integer ratioValue = Optional.ofNullable(request.getRatios().get(memberIdStr)).orElse(1);
                                Integer ratioSum = Optional.ofNullable(request.getSum()).orElse(participants.size());

                                Ratio ratio = new Ratio(ratioValue, ratioSum);
                                Participant participant = new Participant(participantKey, constant, ratio);
                                settlement.getParticipants().add(participant);
                            }
                        }
                    }

                    default -> throw new IllegalArgumentException("지원하지 않는 수정 필드: " + key);
                }
            }
            settlementRepository.save(settlement);
            return settlementId;
        }
    }

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

    public void applyAfterUpdate(Long calculateId) {
        Calculate calculate = calculateRepository.findById(calculateId)
                .orElseThrow(() -> new IllegalArgumentException("정산 없음"));
        List<Settlement> settlementList = settlementRepository.findByCalculate(calculate);

        calculateDetailRepository.deleteByCalculate(calculate);
        optimizationService.calculateAndOptimize(settlementList);

        calculate.setStatus(CalculateStatus.PENDING);
        calculateRepository.save(calculate);
    }

    /*
     * GPT에서 넘어오는 settlement 관련 정보를
     * settlement의 ID, Group 객체, calculate의 ID 등을 사용하여 DB의 Settlement 객체로 변환한다.
     */
    public Settlement toSettlement(SettlementDto settlementDto, Long calculateId) {
        // 정보 채우기
        Settlement settlement = new Settlement();

        settlement.setPlace(settlementDto.getPlace());
        settlement.setItem(settlementDto.getItem());
        settlement.setAmount(settlementDto.getAmount());

        // Calculate 및 Group 정보 설정
        Calculate calculate = calculateRepository.findById(calculateId)
                .orElseThrow(() -> new IllegalArgumentException("Calculate entity not found."));
        UserGroup userGroup = calculate.getUserGroup();
        settlement.setUserGroup(userGroup);

        // Payer 조회
        Long payerId = settlementDto.getPayerId();
        Member payer = memberRepository.findByMemberIdAndUserGroup(payerId, userGroup)
                .orElseThrow(() -> new IllegalArgumentException("Participant member not found in group. ID: " + payerId));
        settlement.setPayer(payer);


//         비율의 분모를 만들기 위해 합한다.
        int sum = 0;
        for (Integer ratio : settlementDto.getRatios().values()) {
            sum += ratio;
        }

        // 비율을 분수의 형태로, 고정금액과 함께 각 멤버로 저장, participant 테이블을 체운다.
        Set<Participant> participants = new HashSet<>();
        for (Long participantId : settlementDto.getParticipantIds()) {
            Member member = memberRepository.findByMemberIdAndUserGroup(participantId, userGroup)
                    .orElseThrow(() -> new IllegalArgumentException("Participant member not found in group. ID: " + participantId));

            String key = participantId.toString();
            Integer constant = settlementDto.getConstants().getOrDefault(key, 0);
            Integer ratio = settlementDto.getRatios().getOrDefault(key, 0);

            Participant.ParticipantKey pk = new Participant.ParticipantKey(settlement, member);
            participants.add(new Participant(pk, constant, new Ratio(ratio, sum)));
        }



        settlement.setParticipants(participants);
        settlement.setCalculate(calculate);

        return settlement;
    }

    /*
     * 여러 항목이 섞여있는 GPT로부터 오는 전체 정산 항목을 종합하여 DB의 Settlement의 List 형태로 변환한다.
     */
    public List<Settlement> toSettlements(List<SettlementDto> settlementDtos, Long calculateId) {
        List<Settlement> settlementList = new ArrayList<>();
        for (SettlementDto dto : settlementDtos) {
            settlementList.add(toSettlement(dto, calculateId));
        }
        return settlementList;
    }


//    public List<Settlement> toSettlements(List<SettlementDto> settlementDtos, Group group, Long calculateId) {
//        List<Settlement> settlementList = new ArrayList<>();
//        for (SettlementDto dto : settlementDtos) {
//            settlementList.add(toSettlement(dto, null, group, calculateId));
//        }
//        return settlementList;
//    }


}