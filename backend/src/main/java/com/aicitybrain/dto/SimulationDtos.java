package com.aicitybrain.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

public final class SimulationDtos {
    private SimulationDtos() {}

    /**
     * Every field is optional — only sliders the operator actually moved are sent,
     * the rest of the live simulation state is left untouched.
     */
    public record SimulationControlRequest(
        @DecimalMin("0") @DecimalMax("100") Double rainfall,
        @DecimalMin("0") @DecimalMax("100") Double trafficIntensity,
        @DecimalMin("0") @DecimalMax("100") Double population,
        @DecimalMin("0") @DecimalMax("100") Double powerDemand,
        @DecimalMin("0") @DecimalMax("100") Double emergencyLevel
    ) {}

    public record ScenarioRequest(
        @NotBlank String scenarioKey
    ) {}

    public record SimulationStateResponse(
        boolean running,
        double rainfall,
        double trafficIntensity,
        double population,
        double powerDemand,
        double emergencyLevel,
        Instant lastTick
    ) {}
}
