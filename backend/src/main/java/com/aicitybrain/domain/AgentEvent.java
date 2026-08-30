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
 * An immutable log record of something an agent observed and published onto the
 * event bus (e.g. "FLOOD_RISK_HIGH", "TRAFFIC_CONGESTION_CRITICAL"). This is the
 * audit trail behind every entry in the live "AI Decisions" feed.
 */
@Entity
@Table(name = "agent_events")
public class AgentEvent extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "agent_type", nullable = false, length = 20)
    private AgentType agentType;

    @Column(name = "event_type", nullable = false, length = 60)
    private String eventType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Severity severity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id")
    private Zone zone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_incident_id")
    private Incident relatedIncident;

    @Column(nullable = false, length = 1000)
    private String summary;

    @Column(columnDefinition = "text")
    private String payloadJson;

    protected AgentEvent() {
    }

    public AgentEvent(AgentType agentType, String eventType, Severity severity, Zone zone, String summary, String payloadJson) {
        this.agentType = agentType;
        this.eventType = eventType;
        this.severity = severity;
        this.zone = zone;
        this.summary = summary;
        this.payloadJson = payloadJson;
    }

    public AgentType getAgentType() { return agentType; }
    public String getEventType() { return eventType; }
    public Severity getSeverity() { return severity; }
    public Zone getZone() { return zone; }
    public Incident getRelatedIncident() { return relatedIncident; }
    public void setRelatedIncident(Incident relatedIncident) { this.relatedIncident = relatedIncident; }
    public String getSummary() { return summary; }
    public String getPayloadJson() { return payloadJson; }
}
