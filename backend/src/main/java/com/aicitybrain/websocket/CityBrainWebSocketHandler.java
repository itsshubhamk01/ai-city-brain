package com.aicitybrain.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Raw WebSocket endpoint (no STOMP broker required — a native browser WebSocket client
 * is enough) that fans server-pushed JSON envelopes out to every connected dashboard.
 * This is what makes the command center feel "live" instead of poll-and-refresh.
 */
@Component
public class CityBrainWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(CityBrainWebSocketHandler.class);

    private final Set<WebSocketSession> sessions = new CopyOnWriteArraySet<>();

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
        sessions.add(session);
        log.info("WebSocket client connected: {} (total={})", session.getId(), sessions.size());
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
        sessions.remove(session);
        log.info("WebSocket client disconnected: {} (total={})", session.getId(), sessions.size());
    }

    public void broadcast(String json) {
        for (WebSocketSession session : sessions) {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(json));
                }
            } catch (IOException e) {
                log.warn("Failed to send WS message to session {}: {}", session.getId(), e.getMessage());
            }
        }
    }

    public int connectedClients() {
        return sessions.size();
    }
}
