package com.aicitybrain.dto;

import java.util.List;
import java.util.UUID;

public final class WhatIfDtos {
    private WhatIfDtos() {}

    /**
     * A hypothetical scenario to evaluate against the CURRENT city state without
     * mutating it — the "What if rainfall increases by 50%?" feature.
     * All fields optional; the service applies whichever deltas are present.
     */
    public record WhatIfRequest(
        Double rainfallDeltaPct,
        Double trafficDeltaPct,
        UUID powerOutageZoneId,
        Integer powerOutageDurationMinutes,
        String freeformQuery
    ) {}

    public record WhatIfResponse(
        String floodRiskLevel,
        double trafficImpactPct,
        int emergencyResponseDeltaMinutes,
        int hospitalsPotentiallyAffected,
        List<String> recommendedActions,
        String narrative
    ) {}
}
