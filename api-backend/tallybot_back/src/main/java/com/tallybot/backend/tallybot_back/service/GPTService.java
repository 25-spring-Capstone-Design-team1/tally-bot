package com.tallybot.backend.tallybot_back.service;

import com.tallybot.backend.tallybot_back.domain.*;
import com.tallybot.backend.tallybot_back.dto.SettlementDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GPTService {
    public List<SettlementDto> returnResults(List<Chat> chats) {
        List<SettlementDto> result = new ArrayList<>();

        // GPT API 호출 또는 처리 로직 작성 필요

        return result;
    }
}
