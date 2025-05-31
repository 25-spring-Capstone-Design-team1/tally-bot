package com.tallybot.backend.tallybot_back.service;

//import com.tallybot.backend.tallybot_back.debtopt.Graph;
import com.tallybot.backend.tallybot_back.debtopt.Graph;
import com.tallybot.backend.tallybot_back.domain.*;
import com.tallybot.backend.tallybot_back.dto.*;
import com.tallybot.backend.tallybot_back.exception.NoSettlementResultException;
import com.tallybot.backend.tallybot_back.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.h2.value.Transfer;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import java.util.List;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CalculateService {

    private final GroupRepository groupRepository;
    private final CalculateRepository calculateRepository;
    private final ChatRepository chatRepository;
    private final GPTService gptService;
    private final CalculateDetailRepository calculateDetailRepository;
    private final SettlementRepository settlementRepository;
    private final SettlementService settlementService;
    private final ParticipantRepository participantRepository;
    private final OptimizationService optimizationService;


    public boolean groupExists(Long groupId) {
        return groupRepository.existsById(groupId);
    }

    /*
     * group ID와 시작 시간, 종료 시간을 담은 query를 받아
     * 해당 시간대 Chat을 받아와 Settlement를 생성한다.
     * 그 후, 각 Settlement에서 정산해야 할 금액을 산정하여,
     * 최적화 후 Calculate ID를 반환한다.
     * 이 때, 대화 분석이 GPTService를 통해 이루어지며,
     * 각자의 정산 몫 분배와 최적화는 백에서 이루어진다.
     */
    public Long startCalculate(CalculateRequestDto request) {
        UserGroup userGroup = groupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));

        Calculate calculate = new Calculate();
        calculate.setUserGroup(userGroup);
        calculate.setStartTime(request.getStartTime());
        calculate.setEndTime(request.getEndTime());
        calculate.setStatus(CalculateStatus.CALCULATING);
        calculate = calculateRepository.save(calculate);
        Long calculateId = calculate.getCalculateId();

        List<Chat> chats = chatRepository.findByUserGroupAndTimestampBetween(
                userGroup,
                request.getStartTime(),
                request.getEndTime()
        );

        // 나머지 GPT 처리 로직은 비동기로 실행
        Long finalCalculateId = calculateId; // 비동기에서 접근 가능하도록 final 변수로 복사

//        CompletableFuture.runAsync(() -> {
//            try {
//                List<SettlementDto> results = gptService.returnResults(request.getGroupId(), chats);
//
//
//                List<Settlement> settlements = settlementService.toSettlements(results, finalCalculateId);
//
//                for (Settlement settlement : settlements) {
//                    Settlement savedSettlement = settlementRepository.save(settlement);
//                    for (Participant participant : settlement.getParticipants()) {
//                        participant.getParticipantKey().setSettlement(savedSettlement);
//                        participantRepository.save(participant);
//                    }
//                }
//
//                calculateAndOptimize(settlements);
//                pendingCalculate(calculateId);
//
//            } catch (NoSettlementResultException ex) {
//                // GPT가 정산 결과를 반환하지 않은 경우 → calculate 삭제
////                calculateRepository.deleteById(finalCalculateId);
//                System.err.println("정산 결과 없음 - calculate 삭제됨: " + ex.getMessage());
//                return;
//            } catch (Exception ex) {
//                // 기타 오류 → calculate 삭제
////                calculateRepository.deleteById(finalCalculateId);
//                System.err.println("GPT 처리 중 오류 발생 - calculate 삭제됨: " + ex.getMessage());
//                return;
//            }
//        });


        List<ChatForGptDto> chatDtos = chats.stream()
                .map(chat -> new ChatForGptDto(
                        chat.getChatId(),
                        chat.getMember().getMemberId(),
                        chat.getMember().getNickname(),
                        chat.getMessage(),
                        chat.getTimestamp()
                ))
                .toList();


        CompletableFuture.runAsync(() -> {
            try {
                List<SettlementDto> results = gptService.returnResults(request.getGroupId(), chatDtos);

                List<Settlement> settlements = settlementService.toSettlements(results, finalCalculateId);


                for (Settlement settlement : settlements) {
                    Settlement savedSettlement = settlementRepository.save(settlement);
                    for (Participant participant : settlement.getParticipants()) {
                        participant.getParticipantKey().setSettlement(savedSettlement);
                        participantRepository.save(participant);
                    }
                }

                calculateAndOptimize(settlements);
                pendingCalculate(calculateId);

            } catch (NoSettlementResultException ex) {
                calculateRepository.deleteById(finalCalculateId);
                System.err.println("정산 결과 없음 - calculate 삭제됨: " + ex.getMessage());
                return;
            } catch (Exception ex) {
                calculateRepository.deleteById(finalCalculateId);
                System.err.println("GPT 처리 중 오류 발생 - calculate 삭제됨: " + ex.getMessage());
//                ex.printStackTrace();
                return;
            }
        });


        return calculateId;
    }


//    public void calculateAndOptimize(List<Settlement> settlementList)
//    {
//        // Calculate Detail 생성
//        List<CalculateDetail> lcd = calculateShare(settlementList);
//        lcd = optimizationService.optimize(lcd);
//        calculateDetailRepository.saveAll(lcd);
//
//    }


    public void calculateAndOptimize(List<Settlement> settlementList) {
        optimizationService.calculateAndOptimize(settlementList);
    }


    /// 로직 테스트 필요
//    public void recalculate(Long calculateId) {
//        Calculate calculate = calculateRepository.findByCalculateId(calculateId)
//                .orElseThrow(() -> new IllegalArgumentException("해당 Calculate ID가 존재하지 않습니다."));
//
//        calculateDetailRepository.deleteByCalculate(calculate);
//        List<Settlement> settlementList = settlementRepository.findByCalculate(calculate);
//
//        calculateAndOptimize(settlementList);
//        pendingCalculate(calculateId);
//    }

    @Transactional
    public void recalculate(Long calculateId) {
        Calculate calculate = calculateRepository.findById(calculateId)
                .orElseThrow(() -> new IllegalArgumentException("계산 ID 존재하지 않음"));

        // 1. 기존 CalculateDetail 삭제
        calculateDetailRepository.deleteByCalculate(calculate);

//        // 2. 기존 Settlement 삭제
//        settlementRepository.deleteByCalculate(calculate);
//
//        // 3. 새 정산 계산 → Settlement 생성
//        List<Settlement> settlements = calculateSettlementFromChat(calculate);
//        settlementRepository.saveAll(settlements);


//        // 4. 새 송금관계(CalculateDetail) 생성
//        List<CalculateDetail> details = optimizationService.summarizeTransfers(settlements, calculate);
//        calculateDetailRepository.saveAll(details);

        List<Settlement> settlementList = settlementRepository.findByCalculate(calculate);
        calculateAndOptimize(settlementList);
        // 5. 상태 초기화
        calculate.setStatus(CalculateStatus.PENDING);
        calculateRepository.save(calculate);
    }





    /*
     * 정산 계산 완료를 표시하여 저장한다.
     */
    public void pendingCalculate(Long calculateId) {
        Calculate calculate = calculateRepository.findById(calculateId)
                .orElseThrow(() -> new IllegalArgumentException("정산 없음"));

        calculate.setStatus(CalculateStatus.PENDING);
        calculateRepository.save(calculate);
    }

    /*
     * 각자의 정산 몫을 산정한다.
     */
    public List<CalculateDetail> calculateShare(List<Settlement> sm) {
        Map<Pair<Member, Member>, Integer> m = new HashMap<>();


        for (Settlement s : sm) {
            // 미리 고정 금액으로 정산하는 금액을 뺀다.
            int amount = s.getAmount();
            for(Participant pc: s.getParticipants()) {
                amount -= pc.getConstant();
            }

            // 남은 금액을 각 비율로 나눈다.
            final int finalAmount = amount;
            for(Participant pc: s.getParticipants()) {
                Pair<Member, Member> p = Pair.of(s.getPayer(), pc.getParticipantKey().getMember());

//                Pair<Member, Member> p = Pair.of(pc.getParticipantKey().getMember(), s.getPayer());
                m.put(p, m.getOrDefault(p, 0) + pc.getConstant() + pc.getRatio().mul(new Ratio(amount)).toInt());
            }
        }

        Calculate calculate = sm.get(0).getCalculate();

        List<CalculateDetail> lcd = new ArrayList<>();
        for(Pair<Member, Member> mem2Mem: m.keySet()) {
            lcd.add(new CalculateDetail(null, calculate, mem2Mem.getFirst(), mem2Mem.getSecond(), m.get(mem2Mem)));
        }

        return lcd;
    }

    /*
     * 그래프 간소화를 이용하여 산정된 정산 몫을 최적화한다.
     */
//    public List<CalculateDetail> optimize(List<CalculateDetail> lcd) {
//        // 그래프 형태로 만든다.
//        Set<Member> members = new HashSet<>();
//        Calculate calculate = lcd.get(0).getCalculate();
//        for(CalculateDetail cd: lcd) {
//            members.add(cd.getPayer());
//            members.add(cd.getPayee());
//        }
//
//        // index compression
//        List<Member> memberList = members.stream().toList();
//        Graph graph = new Graph(memberList.size());
//        for(CalculateDetail cd: lcd) {
//            int payerNum = memberList.indexOf(cd.getPayer());
//            int payeeNum = memberList.indexOf(cd.getPayee());
//            graph.addEdge(payerNum, payeeNum, cd.getAmount());
//        }
//
//        // 그래프 간소화를 실현하되, 그것이 오히려 간선을 늘리는 경우 원복한다.
//        Graph graph2 = Graph.summarize(graph);
//        if(graph2.getEdgeCount() < graph.getEdgeCount()) {
//            graph = graph2;
//        }
//
//        // 그래프 형태를 되돌린다.
//        List<CalculateDetail> lcd2 = new ArrayList<>();
//        for(int i = 0; i < graph.getVertexCount(); i++) {
//            for(Integer j: graph.getAdjacencyList().get(i).keySet()) {
//                if(graph.getAdjacencyList().get(i).get(j) < 0) continue;
//                lcd2.add(new CalculateDetail(null, calculate, memberList.get(i), memberList.get(j),
//                        graph.getAdjacencyList().get(i).get(j)));
//            }
//        }
//
//        return lcd2;
//    }


    public BotResponseDto botResultReturn(Calculate calculate) {
        Long groupId = calculate.getUserGroup().getGroupId();
        Long calculateId = calculate.getCalculateId();

        String groupUrl = "https://tallybot.me/" + groupId;
        String calculateUrl = groupUrl + "/" + calculateId;

        // 실제 정산 결과 리스트 생성
        List<TransferDto> transfers = calculateDetailRepository.findAllByCalculate(calculate)
                .stream()
                .map(detail -> new TransferDto(
                        detail.getPayer().getMemberId(),
                        detail.getPayee().getMemberId(),
                        detail.getAmount()
                ))
                .collect(Collectors.toList());

        return new BotResponseDto(groupUrl, calculateUrl, transfers);
    }

    /*
     * Calculate ID에 따라 정산 계산된 payer-payee 간
     * 금전 거래 관계를 CalculateDetail울 Response 형태로 반환한다.
     */
//    public ResponseDetailDto resultReturn(Long calculateId) {
//        Calculate calculate = calculateRepository.findById(calculateId)
//                .orElseThrow(() -> new IllegalArgumentException("정산 없음"));
//
//        List<CalculateDetail> details = calculateDetailRepository.findByCalculate(calculate);
//
//        List<ResponseDetail> detailResponses = details.stream().map(detail ->
//                new ResponseDetail(
//                        detail.getPayer().getNickname(),
//                        detail.getPayee().getNickname(),
//                        detail.getAmount()
//                )
//        ).toList();
//
//        String url = "https://tallybot.me/calculate/" + calculateId;
//
//        return new ResponseDetailDto(url, detailResponses);
//    }
//
//    /*
//     * Calculate ID에 따라 정산 계산된 각 item을 Response 형태로 반환한다.
//     */
//    public ResponseBriefSettlementDto resultBriefSettlementReturn(Long calculateId) {
//        Calculate calculate = calculateRepository.findById(calculateId)
//                .orElseThrow(() -> new IllegalArgumentException("정산 없음"));
//
//        List<Settlement> settlements = settlementRepository.findByCalculate(calculate);
//
//        List<ResponseBriefSettlement> settlementResponses = settlements.stream().map(settlement -> {
//            List<String> nicks = settlementService.nicknamesInCalculate(settlements);
//            return new ResponseBriefSettlement(
//                    settlement.getSettlementId(),
//                    settlement.getPlace(),
//                    settlement.getPayer().getNickname(),
//                    nicks.size(),
//                    settlement.getAmount(),
//                    nicks
//            );
//        }).toList();
//
//        String url = "https://tallybot.me/settlement/brief/" + calculateId;
//
//        return new ResponseBriefSettlementDto(url, settlementResponses);
//    }
//
//    /*
//     * Calculate ID에 따라 각자의 정산 비율 등을 포함한 결제의 자세한 정보를 Response 형태로 반환한다.
//     */
//    public ResponseSettlementDto resultSettlementReturn(Long calculateId) {
//        Calculate calculate = calculateRepository.findById(calculateId)
//                .orElseThrow(() -> new IllegalArgumentException("정산 없음"));
//
//        List<Settlement> settlements = settlementRepository.findByCalculate(calculate);
//
//        List<SettlementDto> settlementDtoResponses = settlements.stream().map(settlement -> {
//            int denom = settlement.getParticipants().stream().map(Participant::getRatio).map(Ratio::getDenominator).reduce(1,
//                    (a, b) -> a * b / Ratio.gcd(a, b));
//            return new SettlementDto(
//                    settlement.getPlace(),
//                    settlement.getPayer().getNickname(),
//                    settlement.getItem(),
//                    settlement.getAmount(),
//                    settlement.getParticipants().stream().map(participant -> {
//                        return participant.getParticipantKey().member.getNickname();
//                    }).toList(),
//                    settlement.getParticipants().stream().collect(Collectors.toMap(
//                            participant -> participant.getParticipantKey().member.getNickname(),
//                            Participant::getConstant)),
//                    settlement.getParticipants().stream().collect(Collectors.toMap(
//                            participant -> ((Participant) participant).getParticipantKey().member.getNickname(),
//                            participant -> Ratio.mul(((Participant) participant).getRatio(), new Ratio(denom, 1)).toInt()))
//            );
//        }).toList();
//
//        String url = "https://tallybot.me/settlement/detail" + calculateId;
//
//        return new ResponseSettlementDto(url, settlementDtoResponses);
//    }
//
//    @Transactional
//    public void recalculate(Long calculateId) {
//        Calculate calculate = calculateRepository.findById(calculateId)
//                .orElseThrow(() -> new IllegalArgumentException("정산 없음"));
//
//        // 기존 결과 삭제
//        calculateDetailRepository.deleteByCalculate(calculate);
//
//        // 재분석 없이 프론트에서 전달된 수정 사항만 settlement에 업데이트 (예: 정산 대상자, 금액 등)
//
//        // 웹에서 수정 시 item: value, amount: value 형식으로 들어오니 GPT 추가로 돌리지 않고
//        // 내부 함수만 돌리는 코드 작성 필요
//
//        // 상태는 그대로 유지 (PENDING or COMPLETED)
//    }


}





