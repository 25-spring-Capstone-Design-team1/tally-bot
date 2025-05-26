package com.tallybot.backend.tallybot_back.service;

import com.tallybot.backend.tallybot_back.domain.*;
import com.tallybot.backend.tallybot_back.dto.*;
import com.tallybot.backend.tallybot_back.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalculateServiceTest {


    @InjectMocks
    private CalculateService calculateService;

    @Mock private GroupRepository groupRepository;
    @Mock private CalculateRepository calculateRepository;
    @Mock private ChatRepository chatRepository;
    @Mock private GPTService gptService;
    @Mock private CalculateDetailRepository calculateDetailRepository;
    @Mock private SettlementRepository settlementRepository;
    @Mock private SettlementService settlementService;


    @Test
    void groupExists_true() {
        when(groupRepository.existsById(1L)).thenReturn(true);
        boolean result = calculateService.groupExists(1L);
        assertThat(result).isTrue();
    }

    @Test
    void groupExists_false() {
        when(groupRepository.existsById(999L)).thenReturn(false);
        boolean result = calculateService.groupExists(999L);
        assertThat(result).isFalse();
    }

//    @Test
//    @DisplayName("startCalculate(): 정산 생성 및 GPT 비동기 호출")
//    void startCalculate_success() {
//        // given
//        Group group = new Group();
//        group.setGroupId(1L);
//
//        Calculate calculate = new Calculate();
//        calculate.setCalculateId(100L);
//        calculate.setGroup(group);
//
//        List<Chat> chats = List.of();
//
//        CalculateRequestDto request = new CalculateRequestDto(
//                1L,
//                LocalDateTime.of(2025, 1, 1, 12, 0),
//                LocalDateTime.of(2025, 1, 1, 14, 0)
//        );
//
//        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
//        when(calculateRepository.save(any())).thenReturn(calculate);
//        when(calculateRepository.findById(100L)).thenReturn(Optional.of(calculate));
//        when(chatRepository.findByGroupAndTimestampBetween(eq(group), any(), any())).thenReturn(chats);
//        when(gptService.returnResults(chats)).thenReturn(List.of());
//        when(settlementService.toSettlements(any(), eq(100L))).thenReturn(List.of());
//
//        // when
//        Long result = calculateService.startCalculate(request);
//
//        // then
//        assertThat(result).isEqualTo(100L);
//
//        // save()는 startCalculate + pendingCalculate 두 번 호출됨
//        verify(calculateRepository, times(2)).save(any());
//        verify(chatRepository).findByGroupAndTimestampBetween(eq(group), any(), any());
//        verify(gptService).returnResults(chats);
//
//    }



    @Test
    @DisplayName("recalculate(): 기존 Settlement로 재정산 처리")
    void recalculate_success() {
        // given
        Long calculateId = 200L;
        Calculate calculate = new Calculate();
        calculate.setCalculateId(calculateId);

        Member payer = new Member(); payer.setMemberId(1001L);
        Member payee = new Member(); payee.setMemberId(1002L);

        Settlement settlement = new Settlement();
        settlement.setCalculate(calculate);
        settlement.setPayer(payer);
        settlement.setAmount(10000);

        Participant.ParticipantKey pk = new Participant.ParticipantKey(settlement, payee);
        Participant participant = new Participant(pk, 0, new Ratio(1, 1));
        settlement.setParticipants(Set.of(participant));

        // 👉 calculateRepository는 2번 호출되므로 둘 다 처리
        when(calculateRepository.findByCalculateId(calculateId)).thenReturn(Optional.of(calculate));
        when(calculateRepository.findById(calculateId)).thenReturn(Optional.of(calculate));
        when(settlementRepository.findByCalculate(calculate)).thenReturn(List.of(settlement));

        // when
        calculateService.recalculate(calculateId);

        // then
        verify(calculateDetailRepository).saveAll(any());
    }


    @Test
    @DisplayName("calculateAndOptimize(): 정산 → 최적화 → 저장까지 정상 수행")
    void calculateAndOptimize_success() {
        // given
        UserGroup userGroup = new UserGroup();
        Member m1 = new Member(); m1.setMemberId(1L); m1.setUserGroup(userGroup);
        Member m2 = new Member(); m2.setMemberId(2L); m2.setUserGroup(userGroup);

        Calculate calculate = new Calculate();
        calculate.setCalculateId(1L);
        calculate.setUserGroup(userGroup);

        // 정산 1건
        Settlement s = new Settlement();
        s.setCalculate(calculate);
        s.setAmount(10000);
        s.setPayer(m1);

        Participant.ParticipantKey pk = new Participant.ParticipantKey(s, m2);
        Participant participant = new Participant(pk, 0, new Ratio(1, 1));
        s.setParticipants(Set.of(participant));

        // when
        calculateService.calculateAndOptimize(List.of(s));

        // then
        verify(calculateDetailRepository).saveAll(any());
    }



    @Test
    @DisplayName("✅ botResultReturn(): 정산 결과 DTO 정상 반환")
    void botResultReturn_success() {
        // given
        UserGroup userGroup = new UserGroup();
        userGroup.setGroupId(42L);

        Calculate calculate = new Calculate();
        calculate.setCalculateId(101L);
        calculate.setUserGroup(userGroup);

        Member payer1 = new Member();
        payer1.setMemberId(1001L);
        Member payee1 = new Member();
        payee1.setMemberId(1002L);

        Member payer2 = new Member();
        payer2.setMemberId(1004L);
        Member payee2 = new Member();
        payee2.setMemberId(1002L);

        CalculateDetail detail1 = new CalculateDetail();
        detail1.setCalculate(calculate);
        detail1.setPayer(payer1);
        detail1.setPayee(payee1);
        detail1.setAmount(12000);

        CalculateDetail detail2 = new CalculateDetail();
        detail2.setCalculate(calculate);
        detail2.setPayer(payer2);
        detail2.setPayee(payee2);
        detail2.setAmount(8000);

        when(calculateDetailRepository.findAllByCalculate(calculate))
                .thenReturn(List.of(detail1, detail2));

        // when
        BotResponseDto result = calculateService.botResultReturn(calculate);

        // then
        assertThat(result.getGroupUrl()).isEqualTo("https://tallybot.me/42");
        assertThat(result.getCalculateUrl()).isEqualTo("https://tallybot.me/42/101");

        List<TransferDto> transfers = result.getTransfers();
        assertThat(transfers).hasSize(2);
        assertThat(transfers.get(0).getPayerId()).isEqualTo(1001L);
        assertThat(transfers.get(0).getPayeeId()).isEqualTo(1002L);
        assertThat(transfers.get(0).getAmount()).isEqualTo(12000);
    }

}
