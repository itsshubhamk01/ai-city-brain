package com.aicitybrain.dto;

import java.time.Instant;
import java.util.UUID;

public final class AgentDtos {
    private AgentDtos() {}

    /** A single item in the unified, time-ordered "AI Decisions" live feed. */
    public record DecisionFeedItem(
        UUID id,
        String agentType,
        String category,   // "EVENT" | "ACTION"
        String label,
        String summary,
        String severity,   // nullable for pure actions
        String zoneName,
        Instant createdAt
    ) {}
}
