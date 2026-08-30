package com.aicitybrain.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "waste_bins")
public class WasteBin extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "zone_id", nullable = false)
    private Zone zone;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false)
    private double lat;
    @Column(nullable = false)
    private double lng;

    @Column(name = "capacity_pct", nullable = false)
    private double capacityPct = 20;

    protected WasteBin() {
    }

    public WasteBin(Zone zone, String code, double lat, double lng, double capacityPct) {
        this.zone = zone;
        this.code = code;
        this.lat = lat;
        this.lng = lng;
        this.capacityPct = capacityPct;
    }

    public Zone getZone() { return zone; }
    public String getCode() { return code; }
    public double getLat() { return lat; }
    public double getLng() { return lng; }
    public double getCapacityPct() { return capacityPct; }
    public void setCapacityPct(double capacityPct) { this.capacityPct = Math.max(0, Math.min(100, capacityPct)); }
}
