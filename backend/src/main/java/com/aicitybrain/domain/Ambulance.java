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
@Table(name = "ambulances")
public class Ambulance extends BaseEntity {

    public enum Status { AVAILABLE, DISPATCHED, EN_ROUTE, AT_HOSPITAL }

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "zone_id", nullable = false)
    private Zone zone;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false)
    private double lat;
    @Column(nullable = false)
    private double lng;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.AVAILABLE;

    protected Ambulance() {
    }

    public Ambulance(Zone zone, String code, double lat, double lng) {
        this.zone = zone;
        this.code = code;
        this.lat = lat;
        this.lng = lng;
    }

    public Zone getZone() { return zone; }
    public void setZone(Zone zone) { this.zone = zone; }
    public String getCode() { return code; }
    public double getLat() { return lat; }
    public double getLng() { return lng; }
    public void setLat(double lat) { this.lat = lat; }
    public void setLng(double lng) { this.lng = lng; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}
