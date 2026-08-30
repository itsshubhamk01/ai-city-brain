package com.aicitybrain.domain;

/**
 * Identifies which specialized AI agent (or the central orchestrator itself)
 * produced a given event, action, alert or decision.
 */
public enum AgentType {
    TRAFFIC,
    EMERGENCY,
    FLOOD,
    WASTE,
    ENERGY,
    HEALTHCARE,
    CITY_BRAIN
}
