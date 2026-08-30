package com.aicitybrain.service.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class InMemoryEventBus implements EventBus {

    private static final Logger log = LoggerFactory.getLogger(InMemoryEventBus.class);

    private final Map<String, List<CityEventListener>> topicListeners = new ConcurrentHashMap<>();
    private final List<CityEventListener> wildcardListeners = new CopyOnWriteArrayList<>();

    @Override
    public void publish(CityEvent event) {
        log.debug("Event published: type={} source={} zoneId={} severity={}",
            event.type(), event.source(), event.zoneId(), event.severity());

        topicListeners.getOrDefault(event.type(), List.of()).forEach(listener -> safeDispatch(listener, event));
        wildcardListeners.forEach(listener -> safeDispatch(listener, event));
    }

    @Override
    public void subscribe(String eventType, CityEventListener listener) {
        topicListeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(listener);
    }

    @Override
    public void subscribeAll(CityEventListener listener) {
        wildcardListeners.add(listener);
    }

    private void safeDispatch(CityEventListener listener, CityEvent event) {
        try {
            listener.onEvent(event);
        } catch (Exception e) {
            // One misbehaving agent must never take down the tick or the rest of the bus.
            log.error("Listener threw while handling event type={}: {}", event.type(), e.getMessage(), e);
        }
    }
}
