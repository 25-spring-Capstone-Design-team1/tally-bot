package com.tallybot.backend.tallybot_back.service;

import com.tallybot.backend.tallybot_back.domain.*;
import com.tallybot.backend.tallybot_back.dto.*;
import com.tallybot.backend.tallybot_back.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.time.LocalDateTime;
import java.util.Collections;
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

    @BeforeEach
    void setUp() {
        groupRepository = mock(GroupRepository.class);
        calculateRepository = mock(CalculateRepository.class);
        chatRepository = mock(ChatRepository.class);
        gptService = mock(GPTService.class);

        calculateService = new CalculateService(
                groupRepository, calculateRepository, chatRepository, gptService
        );
    }

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

    @Test
    void startCalculate_success() {
        Group mockGroup = new Group();
        mockGroup.setGroupId(1L);

        Calculate mockCalculate = new Calculate();
        mockCalculate.setCalculateId(42L);

        List<Chat> chats = Collections.emptyList();
        List<SettlementDto> settlements = Collections.emptyList();

        CalculateRequestDto dto = new CalculateRequestDto(
                1L,
                LocalDateTime.of(2024, 5, 1, 14, 0),
                LocalDateTime.of(2024, 5, 1, 15, 0)
        );

        when(groupRepository.findById(1L)).thenReturn(Optional.of(mockGroup));
        when(chatRepository.findByGroupAndTimestampBetween(eq(mockGroup), any(), any())).thenReturn(chats);
        when(gptService.returnResults(chats)).thenReturn(settlements);
        when(calculateRepository.save(any())).thenReturn(mockCalculate);

        Long resultId = calculateService.startCalculate(dto);

        assertThat(resultId).isEqualTo(42L);
    }

}
