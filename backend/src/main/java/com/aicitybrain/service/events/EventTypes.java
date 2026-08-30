package com.aicitybrain.service.events;

/**
 * Well-known event "topics" published onto the {@link EventBus}. Centralizing these as
 * constants (instead of scattering string literals across agents) gives us compile-time
 * safety while keeping the bus itself topic-agnostic, exactly like a Kafka topic name.
 */
public final class EventTypes {
    private EventTypes() {}

    public static final String TICK_COMPLETED = "TICK_COMPLETED";

    public static final String TRAFFIC_CONGESTION_HIGH = "TRAFFIC_CONGESTION_HIGH";
    public static final String TRAFFIC_CONGESTION_CRITICAL = "TRAFFIC_CONGESTION_CRITICAL";

    public static final String FLOOD_RISK_ELEVATED = "FLOOD_RISK_ELEVATED";
    public static final String FLOOD_RISK_HIGH = "FLOOD_RISK_HIGH";
    public static final String FLOOD_RISK_CRITICAL = "FLOOD_RISK_CRITICAL";

    public static final String ACCIDENT_REPORTED = "ACCIDENT_REPORTED";
    public static final String FIRE_REPORTED = "FIRE_REPORTED";
    public static final String MEDICAL_EMERGENCY_REPORTED = "MEDICAL_EMERGENCY_REPORTED";
    public static final String INCIDENT_RESOLVED = "INCIDENT_RESOLVED";

    public static final String POWER_DEMAND_HIGH = "POWER_DEMAND_HIGH";
    public static final String POWER_OUTAGE = "POWER_OUTAGE";

    public static final String WASTE_BIN_FULL = "WASTE_BIN_FULL";

    public static final String HOSPITAL_CAPACITY_LOW = "HOSPITAL_CAPACITY_LOW";
    public static final String HOSPITAL_CAPACITY_CRITICAL = "HOSPITAL_CAPACITY_CRITICAL";

    public static final String AQI_UNHEALTHY = "AQI_UNHEALTHY";

    public static final String SCENARIO_TRIGGERED = "SCENARIO_TRIGGERED";

    public static final String ROAD_STATUS_CHANGED = "ROAD_STATUS_CHANGED";
    public static final String AMBULANCE_DISPATCHED = "AMBULANCE_DISPATCHED";

    public static final String ZONE_EVACUATION_RECOMMENDED = "ZONE_EVACUATION_RECOMMENDED";
}
