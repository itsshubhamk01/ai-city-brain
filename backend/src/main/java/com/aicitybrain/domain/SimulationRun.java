package com.aicitybrain.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * A record of a simulation-control adjustment or named scenario (e.g. "HEAVY_RAIN")
 * that was applied to the live city — the audit trail behind the "Simulation Control"
 * and "What-If" features.
 */
@Entity
@Table(name = "simulation_runs")
public class SimulationRun extends BaseEntity {

    public enum Status { RUNNING, COMPLETED }

    @Column(name = "scenario_key", nullable = false, length = 60)
    private String scenarioKey;

    @Column(name = "rainfall_input")
    private Double rainfallInput;
    @Column(name = "traffic_input")
    private Double trafficInput;
    @Column(name = "population_input")
    private Double populationInput;
    @Column(name = "power_demand_input")
    private Double powerDemandInput;
    @Column(name = "emergency_input")
    private Double emergencyInput;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "triggered_by")
    private User triggeredBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.RUNNING;

    @Column(name = "completed_at")
    private Instant completedAt;

    protected SimulationRun() {
    }

    public SimulationRun(String scenarioKey, Double rainfallInput, Double trafficInput, Double populationInput,
                          Double powerDemandInput, Double emergencyInput, User triggeredBy) {
        this.scenarioKey = scenarioKey;
        this.rainfallInput = rainfallInput;
        this.trafficInput = trafficInput;
        this.populationInput = populationInput;
        this.powerDemandInput = powerDemandInput;
        this.emergencyInput = emergencyInput;
        this.triggeredBy = triggeredBy;
    }

    public String getScenarioKey() { return scenarioKey; }
    public Double getRainfallInput() { return rainfallInput; }
    public Double getTrafficInput() { return trafficInput; }
    public Double getPopulationInput() { return populationInput; }
    public Double getPowerDemandInput() { return powerDemandInput; }
    public Double getEmergencyInput() { return emergencyInput; }
    public User getTriggeredBy() { return triggeredBy; }
    public Status getStatus() { return status; }
    public Instant getCompletedAt() { return completedAt; }

    public void complete() {
        this.status = Status.COMPLETED;
        this.completedAt = Instant.now();
    }
}
