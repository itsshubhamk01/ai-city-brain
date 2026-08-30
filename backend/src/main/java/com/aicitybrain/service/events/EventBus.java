package com.aicitybrain.service.events;

/**
 * Publish/subscribe event bus that decouples agents from each other — mirrors the
 * "agents should not call each other directly, they should go through an event bus"
 * requirement from the project spec.
 *
 * <p>The default implementation ({@link InMemoryEventBus}) is a synchronous, in-process
 * bus: zero infrastructure, zero cost, and fully deterministic (which also makes agent
 * logic easy to unit test). This interface is the seam a real broker would plug into —
 * a {@code KafkaEventBus implements EventBus} could replace {@link InMemoryEventBus} via
 * a single {@code @Primary} bean swap, without touching a single agent class. That
 * upgrade is documented but intentionally NOT implemented here, in line with the
 * project's zero-cost/local-laptop requirement — see docs/ARCHITECTURE.md.</p>
 */
public interface EventBus {

    /** Publish an event; every listener subscribed to {@code event.type()} is invoked. */
    void publish(CityEvent event);

    /** Subscribe to a single topic/event type. */
    void subscribe(String eventType, CityEventListener listener);

    /** Subscribe to every event regardless of type — used by the CityBrain orchestrator. */
    void subscribeAll(CityEventListener listener);
}
