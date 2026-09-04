package com.aicitybrain.dto;

import java.time.Instant;
import java.util.List;

/**
 * Real hospital/police/fire-station locations for Mumbai, sourced from
 * OpenStreetMap's Overpass API — never invented. {@code available=false} means the
 * UI should say so honestly rather than showing an empty map silently.
 */
public final class InfrastructureDtos {
    private InfrastructureDtos() {}

    public record InfrastructurePoi(
        String id,
        String type,   // HOSPITAL | POLICE | FIRE_STATION
        String name,
        double lat,
        double lng
    ) {}

    public record InfrastructureResponse(
        boolean available,
        String unavailableReason,
        Instant fetchedAt,
        List<InfrastructurePoi> hospitals,
        List<InfrastructurePoi> policeStations,
        List<InfrastructurePoi> fireStations
    ) {}
}
