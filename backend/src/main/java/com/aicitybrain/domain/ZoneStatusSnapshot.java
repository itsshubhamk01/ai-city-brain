package com.aicitybrain.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Periodic snapshot of a Zone's live metrics, written by the simulation engine so the
 * analytics/trend charts have real historical data to render instead of only the
 * current instantaneous reading.
 */
@Entity
@Table(name = "zone_status_snapshots")
public class ZoneStatusSnapshot extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "zone_id", nullable = false)
    private Zone zone;

    @Column(name = "traffic_level", nullable = false)
    private double trafficLevel;
    @Column(name = "rainfall_mm", nullable = false)
    private double rainfallMm;
    @Column(name = "flood_risk_score", nullable = false)
    private double floodRiskScore;
    @Column(name = "power_demand_mw", nullable = false)
    private double powerDemandMw;
    @Column(name = "power_supply_mw", nullable = false)
    private double powerSupplyMw;
    @Column(name = "hospital_occupancy_pct", nullable = false)
    private double hospitalOccupancyPct;
    @Column(name = "waste_level_pct", nullable = false)
    private double wasteLevelPct;
    @Column(name = "aqi", nullable = false)
    private double aqi;
    @Column(name = "water_supply_pct", nullable = false)
    private double waterSupplyPct;
    @Column(name = "risk_score", nullable = false)
    private double riskScore;

    protected ZoneStatusSnapshot() {
    }

    public ZoneStatusSnapshot(Zone zone) {
        this.zone = zone;
        this.trafficLevel = zone.getTrafficLevel();
        this.rainfallMm = zone.getRainfallMm();
        this.floodRiskScore = zone.getFloodRiskScore();
        this.powerDemandMw = zone.getPowerDemandMw();
        this.powerSupplyMw = zone.getPowerSupplyMw();
        this.hospitalOccupancyPct = zone.getHospitalOccupancyPct();
        this.wasteLevelPct = zone.getWasteLevelPct();
        this.aqi = zone.getAqi();
        this.waterSupplyPct = zone.getWaterSupplyPct();
        this.riskScore = zone.getRiskScore();
    }

    public Zone getZone() { return zone; }
    public double getTrafficLevel() { return trafficLevel; }
    public double getRainfallMm() { return rainfallMm; }
    public double getFloodRiskScore() { return floodRiskScore; }
    public double getPowerDemandMw() { return powerDemandMw; }
    public double getPowerSupplyMw() { return powerSupplyMw; }
    public double getHospitalOccupancyPct() { return hospitalOccupancyPct; }
    public double getWasteLevelPct() { return wasteLevelPct; }
    public double getAqi() { return aqi; }
    public double getWaterSupplyPct() { return waterSupplyPct; }
    public double getRiskScore() { return riskScore; }
}
