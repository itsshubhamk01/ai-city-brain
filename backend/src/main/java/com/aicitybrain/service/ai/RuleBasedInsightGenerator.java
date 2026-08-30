package com.aicitybrain.service.ai;

import com.aicitybrain.dto.CityDtos;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

/**
 * Deterministic, template-based natural-language generation. No network call, no API
 * key, no cost, no latency — this is the generator that runs by default and the one
 * every demo path is guaranteed to work with.
 */
@Component
public class RuleBasedInsightGenerator implements InsightGenerator {

    @Override
    public String generateCityBriefing(CityDtos.CityStatusResponse status) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(Locale.US, "%s is currently at %s overall risk (score %.0f/100).",
            status.name(), status.overallRiskLevel().toLowerCase(Locale.US), status.overallRiskScore()));

        if (status.activeIncidents() > 0) {
            sb.append(String.format(Locale.US, " %d active incident%s being tracked.",
                status.activeIncidents(), status.activeIncidents() == 1 ? " is" : "s are"));
        } else {
            sb.append(" No active incidents.");
        }

        if (status.criticalAlerts() > 0) {
            sb.append(String.format(Locale.US, " %d critical alert%s require attention.",
                status.criticalAlerts(), status.criticalAlerts() == 1 ? "" : "s"));
        }

        status.zones().stream()
            .max((a, b) -> Double.compare(a.riskScore(), b.riskScore()))
            .filter(z -> z.riskScore() >= 60)
            .ifPresent(z -> sb.append(String.format(Locale.US,
                " Highest risk zone: %s (%.0f/100) — watch traffic (%.0f%%) and flood risk (%.0f%%).",
                z.name(), z.riskScore(), z.trafficLevel(), z.floodRiskScore())));

        return sb.toString();
    }

    @Override
    public String generateWhatIfNarrative(WhatIfContext c) {
        StringBuilder sb = new StringBuilder("Simulated scenario");
        if (c.freeformQuery() != null && !c.freeformQuery().isBlank()) {
            sb.append(": \"").append(c.freeformQuery()).append("\"");
        }
        sb.append(". ");

        if (c.rainfallDeltaPct() != null && c.rainfallDeltaPct() != 0) {
            sb.append(String.format(Locale.US, "Rainfall %+.0f%% pushes flood risk to %s. ",
                c.rainfallDeltaPct(), c.floodRiskLevel()));
        }
        if (c.trafficDeltaPct() != null && c.trafficDeltaPct() != 0) {
            sb.append(String.format(Locale.US, "Traffic load shifts by %+.0f%%, an estimated %.0f%% network impact. ",
                c.trafficDeltaPct(), c.trafficImpactPct()));
        }
        if (c.powerOutageZoneName() != null) {
            sb.append(String.format(Locale.US, "A %d-minute outage in %s would add roughly %d minute%s to emergency response times and could affect %d hospital%s. ",
                c.powerOutageDurationMinutes() == null ? 0 : c.powerOutageDurationMinutes(),
                c.powerOutageZoneName(),
                c.emergencyResponseDeltaMinutes(),
                c.emergencyResponseDeltaMinutes() == 1 ? "" : "s",
                c.hospitalsPotentiallyAffected(),
                c.hospitalsPotentiallyAffected() == 1 ? "" : "s"));
        }

        if (!c.recommendedActions().isEmpty()) {
            sb.append("Recommended: ").append(String.join("; ", c.recommendedActions())).append(".");
        } else {
            sb.append("No immediate action recommended at this magnitude.");
        }

        return sb.toString();
    }

    /** Kept for completeness / potential reuse by templates that want a plain bullet list. */
    static List<String> defaultActionsIfNone() {
        return List.of("Continue monitoring — no threshold breached yet.");
    }
}
