package com.aicitybrain.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "hospitals")
public class Hospital extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "zone_id", nullable = false)
    private Zone zone;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false)
    private double lat;
    @Column(nullable = false)
    private double lng;

    @Column(name = "total_beds", nullable = false)
    private int totalBeds;

    @Column(name = "occupied_beds", nullable = false)
    private int occupiedBeds;

    @Column(name = "emergency_capacity", nullable = false)
    private int emergencyCapacity;

    protected Hospital() {
    }

    public Hospital(Zone zone, String name, double lat, double lng, int totalBeds, int occupiedBeds, int emergencyCapacity) {
        this.zone = zone;
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.totalBeds = totalBeds;
        this.occupiedBeds = occupiedBeds;
        this.emergencyCapacity = emergencyCapacity;
    }

    public Zone getZone() { return zone; }
    public String getName() { return name; }
    public double getLat() { return lat; }
    public double getLng() { return lng; }
    public int getTotalBeds() { return totalBeds; }
    public int getOccupiedBeds() { return occupiedBeds; }
    public void setOccupiedBeds(int occupiedBeds) { this.occupiedBeds = occupiedBeds; }
    public int getEmergencyCapacity() { return emergencyCapacity; }

    public double getOccupancyPct() {
        return totalBeds == 0 ? 0 : (occupiedBeds * 100.0) / totalBeds;
    }
}
