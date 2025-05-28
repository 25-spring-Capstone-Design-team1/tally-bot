package com.tallybot.backend.tallybot_back.exception;

public class NoSettlementResultException extends RuntimeException {
    public NoSettlementResultException(String message) {
        super(message);
    }
}