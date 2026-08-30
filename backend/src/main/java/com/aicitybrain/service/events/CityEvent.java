package com.aicitybrain.service.events;

import com.aicitybrain.domain.AgentType;
import com.aicitybrain.domain.Severity;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * A single message on the event bus. Deliberately shaped like a Kafka record
 * (topic + key + value + headers) so that swapping {@link InMemoryEventBus} for a real
 * Kafka-backed implementation later is a drop-in change — see {@link EventBus} javadoc.
 */
public record CityEvent(
    String type,
    AgentType source,
    UUID zoneId,
    Severity severity,
    Map<String, Object> data,
    Instant timestamp
) {
    public CityEvent {
        if (data == null) {
            data = Map.of();
        }
        if (timestamp == null) {
            timestamp = Instant.now();
        }
    }

    public static CityEvent of(String type, AgentType source, UUID zoneId, Severity severity, Map<String, Object> data) {
        return new CityEvent(type, source, zoneId, severity, data, Instant.now());
    }
}
