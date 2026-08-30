package com.aicitybrain.dto;

import java.time.Instant;
import java.util.UUID;

public final class AlertDtos {
    private AlertDtos() {}

    public record AlertResponse(
        UUID id,
        String severity,
        String title,
        String message,
        String zoneName,
        String source,
        boolean acknowledged,
        Instant createdAt
    ) {}
}
