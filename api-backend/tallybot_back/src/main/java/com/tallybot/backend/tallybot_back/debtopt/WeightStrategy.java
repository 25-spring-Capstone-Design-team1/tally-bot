package com.tallybot.backend.tallybot_back.debtopt;

public interface WeightStrategy {
    int getWeight(FlattedGraph f);
    String toString();
}
