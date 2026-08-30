package com.aicitybrain.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "fire_stations")
public class FireStation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "zone_id", nullable = false)
    private Zone zone;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false)
    private double lat;
    @Column(nullable = false)
    private double lng;

    @Column(name = "total_units", nullable = false)
    private int totalUnits;

    @Column(name = "available_units", nullable = false)
    private int availableUnits;

    protected FireStation() {
    }

    public FireStation(Zone zone, String name, double lat, double lng, int totalUnits, int availableUnits) {
        this.zone = zone;
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.totalUnits = totalUnits;
        this.availableUnits = availableUnits;
    }

    public Zone getZone() { return zone; }
    public String getName() { return name; }
    public double getLat() { return lat; }
    public double getLng() { return lng; }
    public int getTotalUnits() { return totalUnits; }
    public int getAvailableUnits() { return availableUnits; }
    public void setAvailableUnits(int availableUnits) { this.availableUnits = availableUnits; }
}
