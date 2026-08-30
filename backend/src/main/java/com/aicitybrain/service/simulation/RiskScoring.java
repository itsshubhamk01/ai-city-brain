package com.aicitybrain.service.simulation;

import com.aicitybrain.domain.Zone;

/**
 * Central, documented place for every threshold and formula the agents and the
 * dashboard use to turn raw metrics into risk classifications. Keeping this in one
 * class (rather than scattering magic numbers across six agents) is what makes the
 * "why did the AI decide that" question answerable — every number here is explainable.
 */
public final class RiskScoring {
    private RiskScoring() {}

    public static final double FLOOD_HIGH_THRESHOLD = 65;
    public static final double FLOOD_CRITICAL_THRESHOLD = 85;

    public static final double TRAFFIC_HIGH_THRESHOLD = 75;
    public static final double TRAFFIC_CRITICAL_THRESHOLD = 90;

    public static final double POWER_STRAIN_HIGH_RATIO = 0.85;
    public static final double POWER_STRAIN_CRITICAL_RATIO = 1.0;

    public static final double HOSPITAL_HIGH_THRESHOLD = 85;
    public static final double HOSPITAL_CRITICAL_THRESHOLD = 95;

    public static final double WASTE_FULL_THRESHOLD = 90;

    public static final double AQI_UNHEALTHY_THRESHOLD = 100;

    /** 0 (excellent) .. 1 (poor) drainage capacity per zone type — a simulated city-planning fact. */
    public static double drainageFactor(Zone.Kind kind) {
        return switch (kind) {
            case RIVERSIDE -> 0.75;    // lowest elevation, closest to the river — worst case
            case DOWNTOWN -> 0.55;     // dense pavement, aging storm drains
            case INDUSTRIAL -> 0.45;
            case AIRPORT -> 0.40;      // flat, well-engineered drainage
            case RESIDENTIAL -> 0.35;
            case SUBURBAN -> 0.25;     // best natural absorption
        };
    }

    /** Flood Agent's core formula: rainfall intensity moderated by zone drainage capacity. */
    public static double computeFloodRisk(double rainfallMm, Zone.Kind kind) {
        double poorDrainage = drainageFactor(kind); // 0..1, higher = worse
        double raw = (rainfallMm / 1.4) * (0.55 + poorDrainage);
        return clamp(raw);
    }

    /** Aggregate 0-100 risk score for a single zone, blending every live metric. */
    public static double computeZoneRisk(Zone zone) {
        double powerStrain = zone.getPowerSupplyMw() <= 0
            ? 100
            : clamp(((zone.getPowerDemandMw() / zone.getPowerSupplyMw()) - 0.5) * 100);
        double aqiNormalized = clamp(zone.getAqi() / 2.0);

        return clamp(
            zone.getTrafficLevel() * 0.28
                + zone.getFloodRiskScore() * 0.28
                + zone.getHospitalOccupancyPct() * 0.16
                + powerStrain * 0.16
                + aqiNormalized * 0.12
        );
    }

    public static String riskLevel(double score) {
        if (score >= 80) return "CRITICAL";
        if (score >= 60) return "HIGH";
        if (score >= 35) return "MODERATE";
        return "LOW";
    }

    public static double clamp(double v) {
        return Math.max(0, Math.min(100, v));
    }
}
