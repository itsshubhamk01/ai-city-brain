package com.aicitybrain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public final class IncidentDtos {
    private IncidentDtos() {}

    public record IncidentResponse(
        UUID id,
        String type,
        String severity,
        String status,
        UUID zoneId,
        String zoneName,
        String description,
        double lat,
        double lng,
        String assignedAgent,
        Instant createdAt,
        Instant resolvedAt
    ) {}

    public record IncidentCreateRequest(
        @NotNull UUID zoneId,
        @NotBlank String type,
        @NotBlank String severity,
        @NotBlank String description,
        double lat,
        double lng
    ) {}

    public record IncidentStatusUpdateRequest(
        @NotBlank String status
    ) {}
}
