package com.aicitybrain.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.aicitybrain.dto.CityDtos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

/**
 * Real, pluggable AI backend: calls the Anthropic Messages API. Disabled by default —
 * only used when {@code app.ai.provider=llm} AND {@code ANTHROPIC_API_KEY} is set (see
 * application.yml). No key is ever hard-coded here or anywhere else in this project.
 *
 * <p>Never called directly — {@link ResilientInsightGenerator} decides whether to use
 * this or fall back to {@link RuleBasedInsightGenerator}, so a missing key, network
 * failure, or bad response never breaks the app.</p>
 */
@Component
public class LlmInsightGenerator {

    private static final Logger log = LoggerFactory.getLogger(LlmInsightGenerator.class);
    private static final String ANTHROPIC_ENDPOINT = "https://api.anthropic.com/v1/messages";
    private static final String ANTHROPIC_VERSION = "2023-06-01";

    private final HttpClient httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .build();
    private final ObjectMapper objectMapper;

    @Value("${app.ai.provider}")
    private String provider;

    @Value("${app.ai.anthropic-api-key}")
    private String apiKey;

    @Value("${app.ai.anthropic-model}")
    private String model;

    public LlmInsightGenerator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public boolean isEnabled() {
        return "llm".equalsIgnoreCase(provider) && apiKey != null && !apiKey.isBlank();
    }

    public String complete(String prompt) throws Exception {
        Map<String, Object> body = Map.of(
            "model", model,
            "max_tokens", 300,
            "messages", new Object[]{ Map.of("role", "user", "content", prompt) }
        );

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(ANTHROPIC_ENDPOINT))
            .header("content-type", "application/json")
            .header("x-api-key", apiKey)
            .header("anthropic-version", ANTHROPIC_VERSION)
            .timeout(Duration.ofSeconds(8))
            .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IllegalStateException("Anthropic API returned HTTP " + response.statusCode() + ": " + response.body());
        }

        JsonNode root = objectMapper.readTree(response.body());
        JsonNode content = root.path("content");
        if (content.isArray() && !content.isEmpty()) {
            return content.get(0).path("text").asText("").trim();
        }
        throw new IllegalStateException("Unexpected Anthropic response shape: " + response.body());
    }

    public String cityBriefingPrompt(CityDtos.CityStatusResponse status) {
        return """
            You are the AI City Brain for %s, a smart-city operations platform.
            In at most 2 sentences, give operators a plain-language briefing.
            Overall risk: %s (%.0f/100). Active incidents: %d. Critical alerts: %d.
            Be concrete and calm, no filler, no markdown.
            """.formatted(status.name(), status.overallRiskLevel(), status.overallRiskScore(),
                status.activeIncidents(), status.criticalAlerts());
    }

    public String whatIfPrompt(InsightGenerator.WhatIfContext c) {
        return """
            You are the AI City Brain's "what-if" scenario engine.
            Scenario query: %s
            Computed impact — flood risk: %s, traffic impact: %.0f%%, extra emergency response time: %d min, hospitals potentially affected: %d.
            Recommended actions already computed: %s
            In at most 3 sentences, explain the situation and the reasoning in plain language for a city operations manager. No markdown, no headers.
            """.formatted(
                c.freeformQuery() == null || c.freeformQuery().isBlank() ? "(none provided — parameter sliders only)" : c.freeformQuery(),
                c.floodRiskLevel(), c.trafficImpactPct(), c.emergencyResponseDeltaMinutes(), c.hospitalsPotentiallyAffected(),
                String.join(", ", c.recommendedActions()));
    }
}
