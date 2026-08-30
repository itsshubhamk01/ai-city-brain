package com.aicitybrain.service.agents;

import com.aicitybrain.domain.AgentType;

/**
 * Contract every specialized agent implements. Deliberately minimal — a single
 * per-tick evaluation hook. Cross-agent coordination happens through the
 * {@link com.aicitybrain.service.events.EventBus}, not through agents calling each
 * other directly, matching the project's event-driven design goal.
 */
public interface CityAgent {
    AgentType type();

    /** Invoked once per simulation tick, after raw metrics for the tick have been written. */
    void evaluate();
}
