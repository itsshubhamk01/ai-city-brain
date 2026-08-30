package com.aicitybrain.domain;

/**
 * Real-world operator roles for the City Brain platform (Section "Real-World Roles").
 * Each maps to a distinct dashboard experience and a distinct set of permitted actions,
 * enforced centrally in {@code SecurityConfig} and re-checked with method security
 * annotations on sensitive controller endpoints.
 */
public enum Role {
    /** Full platform control: user management, city configuration, all modules. */
    ADMIN,
    /** Day-to-day command-center operator: acknowledges alerts, runs scenarios. */
    OPERATIONS_MANAGER,
    /** Emergency services: incident + ambulance dispatch views, action logging. */
    EMERGENCY_RESPONDER,
    /** Traffic control room: road/traffic-signal status, reroute recommendations. */
    TRAFFIC_MANAGER,
    /** Read + analytics: historical trends, exports, no write access to live state. */
    ANALYST,
    /** Public-facing citizen account: city status + active alerts only. */
    CITIZEN
}
