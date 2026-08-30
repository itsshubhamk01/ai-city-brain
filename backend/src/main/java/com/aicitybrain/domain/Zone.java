package com.aicitybrain.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * A district of the simulated city. Zone rows carry the *live* metrics the simulation
 * engine mutates every tick (traffic, rainfall, power, hospital load, waste, AQI, water
 * supply) plus a derived overall {@code riskScore}. This is the single source of truth
 * the command-center dashboard, the map layer and every agent read from.
 */
@Entity
@Table(name = "zones")
public class Zone extends BaseEntity {

    public enum Kind {
        DOWNTOWN, RESIDENTIAL, INDUSTRIAL, AIRPORT, SUBURBAN, RIVERSIDE
    }

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "city_id", nullable = false)
    private City city;

    @Column(nullable = false, length = 120)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Kind kind;

    @Column(name = "center_lat", nullable = false)
    private double centerLat;

    @Column(name = "center_lng", nullable = false)
    private double centerLng;

    @Column(nullable = false)
    private long population;

    // --- Live, mutable metrics (0-100 unless noted) — updated every simulation tick ---
    @Column(name = "traffic_level", nullable = false)
    private double trafficLevel = 30;

    @Column(name = "rainfall_mm", nullable = false)
    private double rainfallMm = 0;

    @Column(name = "flood_risk_score", nullable = false)
    private double floodRiskScore = 0;

    @Column(name = "power_demand_mw", nullable = false)
    private double powerDemandMw = 40;

    @Column(name = "power_supply_mw", nullable = false)
    private double powerSupplyMw = 60;

    @Column(name = "hospital_occupancy_pct", nullable = false)
    private double hospitalOccupancyPct = 50;

    @Column(name = "waste_level_pct", nullable = false)
    private double wasteLevelPct = 20;

    @Column(name = "aqi", nullable = false)
    private double aqi = 40;

    @Column(name = "water_supply_pct", nullable = false)
    private double waterSupplyPct = 95;

    @Column(name = "risk_score", nullable = false)
    private double riskScore = 0;

    protected Zone() {
    }

    public Zone(City city, String name, Kind kind, double centerLat, double centerLng, long population) {
        this.city = city;
        this.name = name;
        this.kind = kind;
        this.centerLat = centerLat;
        this.centerLng = centerLng;
        this.population = population;
    }

    public City getCity() {
        return city;
    }

    public String getName() {
        return name;
    }

    public Kind getKind() {
        return kind;
    }

    public double getCenterLat() {
        return centerLat;
    }

    public double getCenterLng() {
        return centerLng;
    }

    public long getPopulation() {
        return population;
    }

    public double getTrafficLevel() {
        return trafficLevel;
    }

    public void setTrafficLevel(double trafficLevel) {
        this.trafficLevel = clamp(trafficLevel);
    }

    public double getRainfallMm() {
        return rainfallMm;
    }

    public void setRainfallMm(double rainfallMm) {
        this.rainfallMm = Math.max(0, rainfallMm);
    }

    public double getFloodRiskScore() {
        return floodRiskScore;
    }

    public void setFloodRiskScore(double floodRiskScore) {
        this.floodRiskScore = clamp(floodRiskScore);
    }

    public double getPowerDemandMw() {
        return powerDemandMw;
    }

    public void setPowerDemandMw(double powerDemandMw) {
        this.powerDemandMw = Math.max(0, powerDemandMw);
    }

    public double getPowerSupplyMw() {
        return powerSupplyMw;
    }

    public void setPowerSupplyMw(double powerSupplyMw) {
        this.powerSupplyMw = Math.max(0, powerSupplyMw);
    }

    public double getHospitalOccupancyPct() {
        return hospitalOccupancyPct;
    }

    public void setHospitalOccupancyPct(double hospitalOccupancyPct) {
        this.hospitalOccupancyPct = clamp(hospitalOccupancyPct);
    }

    public double getWasteLevelPct() {
        return wasteLevelPct;
    }

    public void setWasteLevelPct(double wasteLevelPct) {
        this.wasteLevelPct = clamp(wasteLevelPct);
    }

    public double getAqi() {
        return aqi;
    }

    public void setAqi(double aqi) {
        this.aqi = Math.max(0, aqi);
    }

    public double getWaterSupplyPct() {
        return waterSupplyPct;
    }

    public void setWaterSupplyPct(double waterSupplyPct) {
        this.waterSupplyPct = clamp(waterSupplyPct);
    }

    public double getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(double riskScore) {
        this.riskScore = clamp(riskScore);
    }

    private static double clamp(double v) {
        return Math.max(0, Math.min(100, v));
    }
}
