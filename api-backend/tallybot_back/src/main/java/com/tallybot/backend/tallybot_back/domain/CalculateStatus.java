package com.tallybot.backend.tallybot_back.domain;

import lombok.Getter;

@Getter
public enum CalculateStatus {
    CALCULATING,
    PENDING,
    COMPLETED
}