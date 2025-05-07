package com.tallybot.backend.tallybot_back.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ResponseDetailDto {
    private String url;
    private List<ResponseDetail> details;
}
