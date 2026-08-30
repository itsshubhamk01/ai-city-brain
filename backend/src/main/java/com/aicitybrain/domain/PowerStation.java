package com.aicitybrain.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "power_stations")
public class PowerStation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "zone_id", nullable = false)
    private Zone zone;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false)
    private double lat;
    @Column(nullable = false)
    private double lng;

    @Column(name = "capacity_mw", nullable = false)
    private double capacityMw;

    @Column(name = "current_load_mw", nullable = false)
    private double currentLoadMw;

    protected PowerStation() {
    }

    public PowerStation(Zone zone, String name, double lat, double lng, double capacityMw, double currentLoadMw) {
        this.zone = zone;
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.capacityMw = capacityMw;
        this.currentLoadMw = currentLoadMw;
    }

    public Zone getZone() { return zone; }
    public String getName() { return name; }
    public double getLat() { return lat; }
    public double getLng() { return lng; }
    public double getCapacityMw() { return capacityMw; }
    public double getCurrentLoadMw() { return currentLoadMw; }
    public void setCurrentLoadMw(double currentLoadMw) { this.currentLoadMw = currentLoadMw; }
}
