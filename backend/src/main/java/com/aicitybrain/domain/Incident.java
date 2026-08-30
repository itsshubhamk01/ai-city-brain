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
 * A concrete, addressable event in the city: an accident, fire, flood, medical
 * emergency, power outage, waste overflow or infrastructure fault. Incidents are the
 * unit of work agents act on and the CityBrain prioritizes.
 */
@Entity
@Table(name = "incidents")
public class Incident extends BaseEntity {

    public enum Type { TRAFFIC_ACCIDENT, FIRE, FLOOD, MEDICAL_EMERGENCY, POWER_OUTAGE, WASTE_OVERFLOW, INFRASTRUCTURE }

    public enum Status { REPORTED, ACKNOWLEDGED, IN_PROGRESS, RESOLVED }

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "zone_id", nullable = false)
    private Zone zone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Type type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Severity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.REPORTED;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(nullable = false)
    private double lat;
    @Column(nullable = false)
    private double lng;

    @Enumerated(EnumType.STRING)
    @Column(name = "assigned_agent", length = 20)
    private AgentType assignedAgent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_by")
    private User reportedBy;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    protected Incident() {
    }

    public Incident(Zone zone, Type type, Severity severity, String description, double lat, double lng) {
        this.zone = zone;
        this.type = type;
        this.severity = severity;
        this.description = description;
        this.lat = lat;
        this.lng = lng;
    }

    public Zone getZone() { return zone; }
    public Type getType() { return type; }
    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) {
        this.status = status;
        if (status == Status.RESOLVED) {
            this.resolvedAt = Instant.now();
        }
    }
    public String getDescription() { return description; }
    public double getLat() { return lat; }
    public double getLng() { return lng; }
    public AgentType getAssignedAgent() { return assignedAgent; }
    public void setAssignedAgent(AgentType assignedAgent) { this.assignedAgent = assignedAgent; }
    public User getReportedBy() { return reportedBy; }
    public void setReportedBy(User reportedBy) { this.reportedBy = reportedBy; }
    public Instant getResolvedAt() { return resolvedAt; }
}
