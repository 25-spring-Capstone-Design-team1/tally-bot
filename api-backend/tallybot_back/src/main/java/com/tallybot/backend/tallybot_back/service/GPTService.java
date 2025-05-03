package com.tallybot.backend.tallybot_back.service;

import com.tallybot.backend.tallybot_back.domain.Calculate;
import com.tallybot.backend.tallybot_back.domain.CalculateDetail;
import com.tallybot.backend.tallybot_back.domain.Chat;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GPTService {
    public List<CalculateDetail> returnResults(List<Chat> chats, Calculate calculate) {
        List<CalculateDetail> result = new ArrayList<>();

        // GPT API 호출 또는 처리 로직 작성 필요

        return result;
    }
}
