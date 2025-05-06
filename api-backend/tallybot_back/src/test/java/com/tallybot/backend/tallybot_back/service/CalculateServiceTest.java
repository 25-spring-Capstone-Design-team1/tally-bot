package com.tallybot.backend.tallybot_back.service;

import com.tallybot.backend.tallybot_back.domain.*;
import com.tallybot.backend.tallybot_back.dto.CalculateDto;
import com.tallybot.backend.tallybot_back.dto.ResponseDetail;
import com.tallybot.backend.tallybot_back.dto.ResponseDetailDto;
import com.tallybot.backend.tallybot_back.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class CalculateServiceTest {

    @InjectMocks
    private CalculateService calculateService;

    @Mock private GroupRepository groupRepository;
    @Mock private CalculateRepository calculateRepository;
    @Mock private ChatRepository chatRepository;
    @Mock private GPTService gptService;
    @Mock private CalculateDetailRepository calculateDetailRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("정산 시작 로직 테스트")
    void startCalculateTest() {
        // given
        Group group = new Group();
        group.setGroupId(1L);
        group.setGroupName("여행");

        CalculateDto dto = new CalculateDto();
        dto.setGroupId(1L);
        dto.setStartTime(LocalDateTime.of(2024, 5, 1, 10, 0));
        dto.setEndTime(LocalDateTime.of(2024, 5, 1, 11, 0));

        Calculate savedCalculate = new Calculate();
        savedCalculate.setCalculateId(42L);
        savedCalculate.setGroup(group);

        List<Chat> dummyChats = List.of(new Chat(), new Chat());
        List<CalculateDetail> dummyResults = List.of(new CalculateDetail());

        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(chatRepository.findByGroupAndTimestampBetween(group, dto.getStartTime(), dto.getEndTime()))
                .thenReturn(dummyChats);
        when(gptService.returnResults(dummyChats, savedCalculate)).thenReturn(dummyResults);
        when(calculateRepository.save(any(Calculate.class))).thenAnswer(invocation -> {
            Calculate c = invocation.getArgument(0);
            c.setCalculateId(42L);
            return c;
        });


        // when
        Long resultId = calculateService.startCalculate(dto);

        // then
        assertThat(resultId).isEqualTo(42L);
        verify(groupRepository).findById(1L);
        verify(calculateDetailRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("정산 결과 반환 테스트")
    void resultReturnTest() {
        // given
        Group group = new Group();
        group.setGroupName("테스트");

        Member payer = new Member();
        payer.setNickname("철수");

        Member payee = new Member();
        payee.setNickname("영희");

        Calculate calculate = new Calculate();
        calculate.setCalculateId(100L);
        calculate.setGroup(group);

        CalculateDetail detail = new CalculateDetail();
        detail.setCalculate(calculate);
        detail.setPayer(payer);
        detail.setPayee(payee);
        detail.setAmount(3000);

        when(calculateRepository.findById(100L)).thenReturn(Optional.of(calculate));
        when(calculateDetailRepository.findByCalculate(calculate))
                .thenReturn(List.of(detail));

        // when
        ResponseDetailDto response = calculateService.resultReturn(100L);

        // then
        assertThat(response.getUrl()).isEqualTo("https://tallybot.me/calculate/100");
        assertThat(response.getDetails()).hasSize(1);
        ResponseDetail d = response.getDetails().get(0);
        assertThat(d.getPayerNickname()).isEqualTo("철수");
        assertThat(d.getPayeeNickname()).isEqualTo("영희");
        assertThat(d.getAmount()).isEqualTo(3000);
    }
}
