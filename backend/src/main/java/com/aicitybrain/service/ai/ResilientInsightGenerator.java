package com.aicitybrain.service.ai;

import com.aicitybrain.dto.CityDtos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * The only {@link InsightGenerator} bean actually injected into services/controllers.
 * Tries the real LLM when it's configured and enabled; on ANY failure (missing key,
 * network error, bad response, timeout) it transparently falls back to the rule-based
 * generator so the "AI Decisions" and "What-If" features never break the demo.
 */
@Service
@Primary
public class ResilientInsightGenerator implements InsightGenerator {

    private static final Logger log = LoggerFactory.getLogger(ResilientInsightGenerator.class);

    private final RuleBasedInsightGenerator ruleBased;
    private final LlmInsightGenerator llm;

    public ResilientInsightGenerator(RuleBasedInsightGenerator ruleBased, LlmInsightGenerator llm) {
        this.ruleBased = ruleBased;
        this.llm = llm;
    }

    @Override
    public String generateCityBriefing(CityDtos.CityStatusResponse status) {
        if (llm.isEnabled()) {
            try {
                return llm.complete(llm.cityBriefingPrompt(status));
            } catch (Exception e) {
                log.warn("LLM insight generation failed, falling back to rule-based briefing: {}", e.getMessage());
            }
        }
        return ruleBased.generateCityBriefing(status);
    }

    @Override
    public String generateWhatIfNarrative(WhatIfContext context) {
        if (llm.isEnabled()) {
            try {
                return llm.complete(llm.whatIfPrompt(context));
            } catch (Exception e) {
                log.warn("LLM what-if narrative failed, falling back to rule-based narrative: {}", e.getMessage());
            }
        }
        return ruleBased.generateWhatIfNarrative(context);
    }
}
