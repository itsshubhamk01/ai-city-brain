package com.aicitybrain.domain;

/**
 * Shared severity scale used consistently across incidents, alerts and agent events
 * so the CityBrain orchestrator can compare and prioritize across all sources with a
 * single ordinal scale.
 */
public enum Severity {
    LOW(1),
    MODERATE(2),
    HIGH(3),
    CRITICAL(4);

    private final int weight;

    Severity(int weight) {
        this.weight = weight;
    }

    /** Numeric weight used by CityBrain's prioritization/risk-scoring logic. */
    public int weight() {
        return weight;
    }
}
