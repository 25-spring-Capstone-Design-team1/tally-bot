package com.tallybot.backend.tallybot_back.service;

import com.tallybot.backend.tallybot_back.domain.*;
import com.tallybot.backend.tallybot_back.dto.SettlementsDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GPTService {
    public SettlementsDto returnResults(List<Chat> chats, Calculate calculate) {
        SettlementsDto settlementsDto = new SettlementsDto();

        // GPT API 호출 또는 처리 로직 작성 필요

        return settlementsDto;
    }
}
