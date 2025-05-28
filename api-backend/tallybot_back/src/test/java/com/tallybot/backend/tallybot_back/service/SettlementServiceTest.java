package com.tallybot.backend.tallybot_back.service;

import com.tallybot.backend.tallybot_back.domain.*;
import com.tallybot.backend.tallybot_back.dto.*;
import com.tallybot.backend.tallybot_back.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

//import static jdk.internal.org.objectweb.asm.util.CheckClassAdapter.verify;
import static org.assertj.core.api.Assertions.assertThat;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SettlementServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private CalculateRepository calculateRepository;

    @Mock
    private SettlementRepository settlementRepository;

    @InjectMocks
    private SettlementService settlementService;


    @Test
    void addSettlement_success_withParticipants() {
        // given
        SettlementUpdateRequest request = new SettlementUpdateRequest();
        request.setCalculateId(1L);
        request.setField("add");

        Map<String, Object> newValue = new HashMap<>();
        newValue.put("place", "가츠벤또");
        newValue.put("item", "점심");
        newValue.put("amount", 30000);
        newValue.put("payer", 1001L);  // L 명시
        newValue.put("participants", List.of(1002L, 1003L));
        request.setNewValue(newValue);

        request.setConstants(Map.of("1001", 0, "1002", 0, "1003", 0));
        request.setRatios(Map.of("1001", 0, "1002", 1, "1003", 1));
        request.setSum(2);

        Member payer = new Member();
        payer.setMemberId(1001L);
        UserGroup userGroup = new UserGroup();
        payer.setUserGroup(userGroup);

        Member m2 = new Member();
        m2.setMemberId(1002L);
        Member m3 = new Member();
        m3.setMemberId(1003L);

        when(memberRepository.findById(1001L)).thenReturn(Optional.of(payer));
        when(memberRepository.findAllById(List.of(1002L, 1003L))).thenReturn(List.of(m2, m3));


        when(calculateRepository.findById(1L)).thenReturn(Optional.of(new Calculate()));
        when(settlementRepository.save(Mockito.<Settlement>any())).thenAnswer(invocation -> {
            Settlement s = invocation.getArgument(0);
            s.setSettlementId(123L); // 테스트용 ID
            return s;
        });


        // when
        Long result = settlementService.applySettlementUpdate(request);

        // then
        assertEquals(123L, result);
    }

    @Test
    void addSettlement_success_withoutParticipants_defaults() {
        Member payer = new Member();
        payer.setMemberId(1001L);
        UserGroup userGroup = new UserGroup();
        payer.setUserGroup(userGroup);

        Member m1 = new Member(); m1.setMemberId(1002L);
        Member m2 = new Member(); m2.setMemberId(1004L);

        when(memberRepository.findById(1001L)).thenReturn(Optional.of(payer));
        when(memberRepository.findByUserGroup(userGroup)).thenReturn(List.of(m1, m2));
        when(calculateRepository.findById(42L)).thenReturn(Optional.of(new Calculate()));
        when(settlementRepository.save(Mockito.<Settlement>any())).thenAnswer(invocation -> {
            Settlement s = invocation.getArgument(0);
            s.setSettlementId(456L); // 테스트용 ID
            return s;
        });

        SettlementUpdateRequest request = new SettlementUpdateRequest();
        request.setCalculateId(42L);
        request.setField("add");
        request.setNewValue(new HashMap<>(Map.of(
                "amount", 20000,
                "payer", 1001
        )));

        Long result = settlementService.applySettlementUpdate(request);

        assertThat(result).isEqualTo(456L);
    }

    @Test
    void addSettlement_fail_amountMissing() {
        SettlementUpdateRequest request = new SettlementUpdateRequest();
        request.setCalculateId(42L);
        request.setField("add");
        request.setNewValue(Map.of("payer", 1001));

        assertThatThrownBy(() -> settlementService.applySettlementUpdate(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("amount는 필수값입니다");
    }

    @Test
    void addSettlement_fail_participantsNull_butConstantsExist() {
        SettlementUpdateRequest request = new SettlementUpdateRequest();
        request.setCalculateId(42L);
        request.setField("add");
        request.setNewValue(Map.of("amount", 10000, "payer", 1001));
        request.setConstants(Map.of("1001", 5000));

        Member payer = new Member();
        payer.setMemberId(1001L);
        payer.setUserGroup(new UserGroup());  // 그룹도 필요
        when(memberRepository.findById(1001L)).thenReturn(Optional.of(payer));

        assertThatThrownBy(() -> settlementService.applySettlementUpdate(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("participants가 null일 경우 constants/ratios도 null이어야 합니다");
    }

    @Test
    void addSettlement_success_participantRatioDefaults() {
        Member payer = new Member();
        payer.setMemberId(1001L);
        UserGroup userGroup = new UserGroup();
        payer.setUserGroup(userGroup);

        Member participant = new Member();
        participant.setMemberId(1002L);

        when(memberRepository.findById(1001L)).thenReturn(Optional.of(payer));
        when(memberRepository.findAllById(List.of(1002L))).thenReturn(List.of(participant));
        when(calculateRepository.findById(42L)).thenReturn(Optional.of(new Calculate()));
        when(settlementRepository.save(Mockito.any())).thenAnswer(invocation -> {
            Settlement s = invocation.getArgument(0);
            s.setSettlementId(123L); // 테스트용 ID
            return s;
        });

        SettlementUpdateRequest request = new SettlementUpdateRequest();
        request.setCalculateId(42L);
        request.setField("add");
        request.setNewValue(Map.of(
                "amount", 10000,
                "payer", 1001,
                "participants", List.of(1002L)
        ));
        // constants, ratios, sum 모두 누락 (기본값 적용 예상)
        request.setConstants(null);
        request.setRatios(null);
        request.setSum(null);

        Long result = settlementService.applySettlementUpdate(request);
        assertEquals(123L, result);
    }

    @Test
    void deleteSettlement_success() {
        Long settlementId = 100L;
        Long calculateId = 1L;

        Settlement settlement = new Settlement();
        settlement.setSettlementId(settlementId);

        when(settlementRepository.findById(settlementId)).thenReturn(Optional.of(settlement));

        SettlementUpdateRequest request = new SettlementUpdateRequest();
        request.setField("delete");
        request.setSettlementId(settlementId);
        request.setCalculateId(calculateId);

        Long result = settlementService.applySettlementUpdate(request);
        assertEquals(settlementId, result);
        verify(settlementRepository).delete(settlement);
    }

    @Test
    void updateSettlement_success_modifyPlaceAndAmount() {
        Long settlementId = 101L;
        Long calculateId = 2L;

        Member payer = new Member();
        payer.setMemberId(2001L);
        UserGroup userGroup = new UserGroup();
        payer.setUserGroup(userGroup);

        Settlement settlement = new Settlement();
        settlement.setSettlementId(settlementId);
        settlement.setPayer(payer);
        settlement.setPlace("OldPlace");
        settlement.setAmount(10000);

        when(settlementRepository.findById(settlementId)).thenReturn(Optional.of(settlement));

        SettlementUpdateRequest request = new SettlementUpdateRequest();
        request.setField("update");
        request.setSettlementId(settlementId);
        request.setCalculateId(calculateId);
        request.setNewValue(Map.of(
                "place", "NewPlace",
                "amount", 20000
        ));

        Long result = settlementService.applySettlementUpdate(request);

        assertEquals(settlementId, result);
        assertEquals("NewPlace", settlement.getPlace());
        assertEquals(20000, settlement.getAmount());
    }

    @Test
    @DisplayName("✅ toSettlement(): SettlementDto를 Settlement로 정확히 변환한다")
    void toSettlement_success() {
        // given
        Long calculateId = 1L;

        UserGroup userGroup = new UserGroup();
        userGroup.setGroupId(10L);

        Member payer = new Member();
        payer.setMemberId(1001L);
        payer.setNickname("준호");
        payer.setUserGroup(userGroup);

        Member participant1 = new Member();
        participant1.setMemberId(1002L);
        participant1.setNickname("소연");
        participant1.setUserGroup(userGroup);

        Member participant2 = new Member();
        participant2.setMemberId(1003L);
        participant2.setNickname("민우");
        participant2.setUserGroup(userGroup);

        Calculate calculate = new Calculate();
        calculate.setCalculateId(calculateId);
        calculate.setUserGroup(userGroup);

        SettlementDto dto = new SettlementDto();
        dto.setPlace("호텔");
        dto.setItem("숙박");
        dto.setAmount(90000);
        dto.setPayerId(1001L);
        dto.setParticipantIds(List.of(1002L, 1003L));
        dto.setConstants(Map.of("1002", 0, "1003", 0));
        dto.setRatios(Map.of("1002", 1, "1003", 2));

        // when
        when(calculateRepository.findById(calculateId)).thenReturn(Optional.of(calculate));
        when(memberRepository.findByMemberIdAndGroup(1001L, userGroup)).thenReturn(Optional.of(payer));
        when(memberRepository.findByMemberIdAndGroup(1002L, userGroup)).thenReturn(Optional.of(participant1));
        when(memberRepository.findByMemberIdAndGroup(1003L, userGroup)).thenReturn(Optional.of(participant2));

        Settlement result = settlementService.toSettlement(dto, calculateId);

        // then
        assertThat(result.getPlace()).isEqualTo("호텔");
        assertThat(result.getItem()).isEqualTo("숙박");
        assertThat(result.getAmount()).isEqualTo(90000);
        assertThat(result.getPayer().getMemberId()).isEqualTo(1001L);
        assertThat(result.getParticipants()).hasSize(2);
        assertThat(result.getCalculate().getCalculateId()).isEqualTo(calculateId);
    }

    @Test
    @DisplayName("toSettlements(): SettlementDto 리스트를 Settlement 리스트로 변환한다")
    void toSettlements_success() {
        // given
        Long calculateId = 1L;
        SettlementDto dto1 = new SettlementDto();
        dto1.setPlace("식당");
        dto1.setItem("점심");
        dto1.setAmount(30000);
        dto1.setPayerId(1001L);
        dto1.setParticipantIds(List.of(1001L, 1002L));
        dto1.setConstants(Map.of("1001", 0, "1002", 0));
        dto1.setRatios(Map.of("1001", 1, "1002", 1));

        // 재사용을 위한 기본 데이터 설정
        UserGroup userGroup = new UserGroup();
        Calculate calculate = new Calculate();
        calculate.setCalculateId(calculateId);
        calculate.setUserGroup(userGroup);

        Member payer = new Member();
        payer.setMemberId(1001L);
        payer.setUserGroup(userGroup);

        Member participant = new Member();
        participant.setMemberId(1002L);
        participant.setUserGroup(userGroup);

        when(calculateRepository.findById(calculateId)).thenReturn(Optional.of(calculate));
        when(memberRepository.findByMemberIdAndGroup(1001L, userGroup)).thenReturn(Optional.of(payer));
        when(memberRepository.findByMemberIdAndGroup(1002L, userGroup)).thenReturn(Optional.of(participant));

        // when
        List<Settlement> result = settlementService.toSettlements(List.of(dto1), calculateId);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getParticipants()).hasSize(2);
    }


}
