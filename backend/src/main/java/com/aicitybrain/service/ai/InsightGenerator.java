package com.aicitybrain.service.ai;

import com.aicitybrain.dto.CityDtos;

import java.util.List;

/**
 * Turns raw simulation numbers into natural-language insight. Two implementations
 * exist: {@link RuleBasedInsightGenerator} (default, deterministic, zero cost, zero
 * external dependency) and {@link LlmInsightGenerator} (optional — activated only when
 * {@code app.ai.provider=llm} and an API key is configured). {@link ResilientInsightGenerator}
 * is the bean actually injected everywhere: it prefers the LLM when enabled and falls
 * back to the rule-based generator on any failure, so a missing/invalid key or a network
 * hiccup never breaks the app.
 */
public interface InsightGenerator {

    /** One or two sentence natural-language summary of the whole city's current condition. */
    String generateCityBriefing(CityDtos.CityStatusResponse status);

    /** Narrative + framing for a "what if" scenario result. */
    String generateWhatIfNarrative(WhatIfContext context);

    record WhatIfContext(
        Double rainfallDeltaPct,
        Double trafficDeltaPct,
        String powerOutageZoneName,
        Integer powerOutageDurationMinutes,
        String freeformQuery,
        String floodRiskLevel,
        double trafficImpactPct,
        int emergencyResponseDeltaMinutes,
        int hospitalsPotentiallyAffected,
        List<String> recommendedActions
    ) {}
}
