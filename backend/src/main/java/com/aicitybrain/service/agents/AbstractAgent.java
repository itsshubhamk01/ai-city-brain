package com.aicitybrain.service.agents;

import com.aicitybrain.domain.AgentAction;
import com.aicitybrain.domain.AgentEvent;
import com.aicitybrain.domain.Incident;
import com.aicitybrain.domain.Severity;
import com.aicitybrain.domain.Zone;
import com.aicitybrain.repository.AgentActionRepository;
import com.aicitybrain.repository.AgentEventRepository;
import com.aicitybrain.service.events.CityEvent;
import com.aicitybrain.service.events.EventBus;

import java.util.Map;
import java.util.UUID;

/**
 * Shared plumbing for every agent: publishing to the event bus and writing the audit
 * trail (AgentEvent = "what I observed", AgentAction = "what I did about it") that
 * backs the live "AI Decisions" feed. Concrete agents focus purely on their domain
 * logic and call these two helpers instead of repeating boilerplate.
 */
public abstract class AbstractAgent implements CityAgent {

    protected final EventBus eventBus;
    protected final AgentEventRepository agentEventRepository;
    protected final AgentActionRepository agentActionRepository;

    protected AbstractAgent(EventBus eventBus,
                             AgentEventRepository agentEventRepository,
                             AgentActionRepository agentActionRepository) {
        this.eventBus = eventBus;
        this.agentEventRepository = agentEventRepository;
        this.agentActionRepository = agentActionRepository;
    }

    protected void publish(String eventType, UUID zoneId, Severity severity, Map<String, Object> data) {
        eventBus.publish(CityEvent.of(eventType, type(), zoneId, severity, data));
    }

    protected AgentEvent logEvent(String eventType, Severity severity, Zone zone, String summary) {
        return agentEventRepository.save(new AgentEvent(type(), eventType, severity, zone, summary, null));
    }

    protected AgentAction logAction(String actionType, String description, Zone zone, Incident incident) {
        return agentActionRepository.save(new AgentAction(type(), actionType, description, zone, incident));
    }
}
