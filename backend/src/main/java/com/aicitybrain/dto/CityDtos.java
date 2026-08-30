package com.aicitybrain.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class CityDtos {
    private CityDtos() {}

    public record ZoneStatusResponse(
        UUID id,
        String name,
        String kind,
        double centerLat,
        double centerLng,
        long population,
        double trafficLevel,
        double rainfallMm,
        double floodRiskScore,
        double powerDemandMw,
        double powerSupplyMw,
        double hospitalOccupancyPct,
        double wasteLevelPct,
        double aqi,
        double waterSupplyPct,
        double riskScore
    ) {}

    public record ZoneHistoryPoint(
        Instant recordedAt,
        double trafficLevel,
        double floodRiskScore,
        double riskScore,
        double aqi,
        double powerDemandMw,
        double powerSupplyMw
    ) {}

    public record CityStatusResponse(
        UUID cityId,
        String name,
        long population,
        List<ZoneStatusResponse> zones,
        String overallRiskLevel,
        double overallRiskScore,
        long activeIncidents,
        long criticalAlerts,
        double trafficAvg,
        double aqiAvg,
        double waterSupplyAvg,
        double powerAvg,
        double hospitalOccupancyAvg,
        Instant timestamp
    ) {}

    public record BriefingResponse(String briefing, Instant generatedAt) {}
}
