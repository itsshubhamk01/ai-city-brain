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
 * A concrete action an agent (or the CityBrain) took or recommended in response to
 * one or more AgentEvents — e.g. "Rerouted 842 vehicles via Road B", "Ambulance #17
 * dispatched to Zone 4". Drives the resume-worthy "AI Decisions" timeline.
 */
@Entity
@Table(name = "agent_actions")
public class AgentAction extends BaseEntity {

    public enum Status { PROPOSED, EXECUTED, FAILED }

    @Enumerated(EnumType.STRING)
    @Column(name = "agent_type", nullable = false, length = 20)
    private AgentType agentType;

    @Column(name = "action_type", nullable = false, length = 60)
    private String actionType;

    @Column(nullable = false, length = 1000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id")
    private Zone zone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_incident_id")
    private Incident relatedIncident;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.EXECUTED;

    protected AgentAction() {
    }

    public AgentAction(AgentType agentType, String actionType, String description, Zone zone, Incident relatedIncident) {
        this.agentType = agentType;
        this.actionType = actionType;
        this.description = description;
        this.zone = zone;
        this.relatedIncident = relatedIncident;
    }

    public AgentType getAgentType() { return agentType; }
    public String getActionType() { return actionType; }
    public String getDescription() { return description; }
    public Zone getZone() { return zone; }
    public Incident getRelatedIncident() { return relatedIncident; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}
