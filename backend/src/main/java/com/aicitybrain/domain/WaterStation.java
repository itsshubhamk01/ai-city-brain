package com.aicitybrain.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "water_stations")
public class WaterStation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "zone_id", nullable = false)
    private Zone zone;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false)
    private double lat;
    @Column(nullable = false)
    private double lng;

    @Column(name = "reservoir_level_pct", nullable = false)
    private double reservoirLevelPct = 90;

    protected WaterStation() {
    }

    public WaterStation(Zone zone, String name, double lat, double lng, double reservoirLevelPct) {
        this.zone = zone;
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.reservoirLevelPct = reservoirLevelPct;
    }

    public Zone getZone() { return zone; }
    public String getName() { return name; }
    public double getLat() { return lat; }
    public double getLng() { return lng; }
    public double getReservoirLevelPct() { return reservoirLevelPct; }
    public void setReservoirLevelPct(double reservoirLevelPct) { this.reservoirLevelPct = reservoirLevelPct; }
}
