package com.aicitybrain.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Typed façade over {@link CityBrainWebSocketHandler}: every push is wrapped in a
 * {@code { channel, payload } } envelope so the frontend can route messages by channel
 * without parsing heuristics.
 */
@Service
public class WebSocketBroadcaster {

    private static final Logger log = LoggerFactory.getLogger(WebSocketBroadcaster.class);

    private final CityBrainWebSocketHandler handler;
    private final ObjectMapper objectMapper;

    public WebSocketBroadcaster(CityBrainWebSocketHandler handler, ObjectMapper objectMapper) {
        this.handler = handler;
        this.objectMapper = objectMapper;
    }

    public void send(String channel, Object payload) {
        try {
            String json = objectMapper.writeValueAsString(Map.of("channel", channel, "payload", payload));
            handler.broadcast(json);
        } catch (Exception e) {
            log.warn("Failed to serialize/broadcast channel={}: {}", channel, e.getMessage());
        }
    }
}
