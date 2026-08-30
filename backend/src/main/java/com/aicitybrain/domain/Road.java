package com.aicitybrain.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "roads")
public class Road extends BaseEntity {

    public enum Status { OPEN, CONGESTED, CLOSED }

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "zone_id", nullable = false)
    private Zone zone;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "start_lat", nullable = false)
    private double startLat;
    @Column(name = "start_lng", nullable = false)
    private double startLng;
    @Column(name = "end_lat", nullable = false)
    private double endLat;
    @Column(name = "end_lng", nullable = false)
    private double endLng;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.OPEN;

    @Column(name = "congestion_pct", nullable = false)
    private double congestionPct = 20;

    protected Road() {
    }

    public Road(Zone zone, String name, double startLat, double startLng, double endLat, double endLng) {
        this.zone = zone;
        this.name = name;
        this.startLat = startLat;
        this.startLng = startLng;
        this.endLat = endLat;
        this.endLng = endLng;
    }

    public Zone getZone() { return zone; }
    public String getName() { return name; }
    public double getStartLat() { return startLat; }
    public double getStartLng() { return startLng; }
    public double getEndLat() { return endLat; }
    public double getEndLng() { return endLng; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public double getCongestionPct() { return congestionPct; }
    public void setCongestionPct(double congestionPct) { this.congestionPct = Math.max(0, Math.min(100, congestionPct)); }
}
