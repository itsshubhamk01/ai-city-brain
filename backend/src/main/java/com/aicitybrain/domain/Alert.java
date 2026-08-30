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
 * A citizen/operator-facing alert (distinct from the lower-level AgentEvent audit
 * log). Alerts are what the dashboard's "Critical Alerts" panel and the citizen-role
 * view actually render.
 */
@Entity
@Table(name = "alerts")
public class Alert extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Severity severity;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, length = 1000)
    private String message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id")
    private Zone zone;

    @Column(nullable = false, length = 60)
    private String source;

    @Column(nullable = false)
    private boolean acknowledged = false;

    protected Alert() {
    }

    public Alert(Severity severity, String title, String message, Zone zone, String source) {
        this.severity = severity;
        this.title = title;
        this.message = message;
        this.zone = zone;
        this.source = source;
    }

    public Severity getSeverity() { return severity; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public Zone getZone() { return zone; }
    public String getSource() { return source; }
    public boolean isAcknowledged() { return acknowledged; }
    public void setAcknowledged(boolean acknowledged) { this.acknowledged = acknowledged; }
}
