package com.aicitybrain.service.brain;

import com.aicitybrain.domain.AgentAction;
import com.aicitybrain.domain.AgentType;
import com.aicitybrain.domain.Alert;
import com.aicitybrain.domain.Severity;
import com.aicitybrain.domain.Zone;
import com.aicitybrain.repository.AgentActionRepository;
import com.aicitybrain.repository.AgentEventRepository;
import com.aicitybrain.repository.AlertRepository;
import com.aicitybrain.repository.ZoneRepository;
import com.aicitybrain.service.events.CityEvent;
import com.aicitybrain.service.events.EventBus;
import com.aicitybrain.service.simulation.CityQueryService;
import com.aicitybrain.websocket.WebSocketBroadcaster;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The central orchestrator described in the spec: it doesn't replace the specialized
 * agents, it sits above them. It listens to every event on the bus (not just one
 * topic), decides which ones are significant enough to become a citizen/operator-facing
 * {@link Alert}, watches for multiple critical signals converging on the same zone
 * (flood + traffic + power all at once) and — only then — raises a single higher-level
 * "coordinated response" action that no individual agent could justify on its own.
 * After every simulation tick it also pushes the refreshed city status and decision
 * feed out over the WebSocket, which is what makes the dashboard feel live.
 */
@Service
public class CityBrainService {

    private static final Logger log = LoggerFactory.getLogger(CityBrainService.class);

    private final EventBus eventBus;
    private final AlertRepository alertRepository;
    private final ZoneRepository zoneRepository;
    private final AgentEventRepository agentEventRepository;
    private final AgentActionRepository agentActionRepository;
    private final CityQueryService cityQueryService;
    private final WebSocketBroadcaster broadcaster;

    /** Per-zone set of distinct CRITICAL event types seen since the last compound-crisis trigger. */
    private final Map<UUID, Set<String>> zoneCriticalSignals = new ConcurrentHashMap<>();

    public CityBrainService(EventBus eventBus, AlertRepository alertRepository, ZoneRepository zoneRepository,
                             AgentEventRepository agentEventRepository, AgentActionRepository agentActionRepository,
                             CityQueryService cityQueryService, WebSocketBroadcaster broadcaster) {
        this.eventBus = eventBus;
        this.alertRepository = alertRepository;
        this.zoneRepository = zoneRepository;
        this.agentEventRepository = agentEventRepository;
        this.agentActionRepository = agentActionRepository;
        this.cityQueryService = cityQueryService;
        this.broadcaster = broadcaster;
    }

    @PostConstruct
    void subscribe() {
        eventBus.subscribeAll(this::onEvent);
    }

    @Transactional
    void onEvent(CityEvent event) {
        if (event.severity() == null) {
            return;
        }
        if (event.severity() == Severity.HIGH || event.severity() == Severity.CRITICAL) {
            raiseAlert(event);
        }
        if (event.severity() == Severity.CRITICAL && event.zoneId() != null) {
            trackCompoundCrisis(event);
        }
    }

    private void raiseAlert(CityEvent event) {
        Zone zone = event.zoneId() == null ? null : zoneRepository.findById(event.zoneId()).orElse(null);
        String zoneName = zone != null ? zone.getName() : String.valueOf(event.data().getOrDefault("zoneName", "City-wide"));
        String title = humanize(event.type());
        String message = title + " — " + zoneName + ".";

        Alert alert = new Alert(event.severity(), title, message, zone, event.source().name());
        alertRepository.save(alert);
        broadcaster.send("alert", cityQueryService.toAlertResponse(alert));
    }

    private void trackCompoundCrisis(CityEvent event) {
        Set<String> signals = zoneCriticalSignals.computeIfAbsent(event.zoneId(), k -> ConcurrentHashMap.newKeySet());
        signals.add(event.type());

        if (signals.size() >= 2) {
            zoneRepository.findById(event.zoneId()).ifPresent(zone -> {
                String signalList = String.join(", ", signals.stream().map(this::humanize).toList());
                String description = ("%s is showing multiple critical signals at once (%s). "
                    + "Recommending a coordinated response across affected agents, including evacuation "
                    + "readiness for low-lying/high-traffic areas.").formatted(zone.getName(), signalList);

                AgentAction action = new AgentAction(AgentType.CITY_BRAIN, "COORDINATED_RESPONSE", description, zone, null);
                agentActionRepository.save(action);

                Alert alert = new Alert(Severity.CRITICAL, "Multi-hazard alert: " + zone.getName(), description, zone, "CITY_BRAIN");
                alertRepository.save(alert);
                broadcaster.send("alert", cityQueryService.toAlertResponse(alert));
                broadcaster.send("decision", cityQueryService.toDecisionFeedItem(action));

                log.info("CityBrain: compound crisis flagged for zone {} ({})", zone.getName(), signalList);
            });
            signals.clear();
        }
    }

    /** Called by the simulation engine once per tick, after every agent has evaluated. */
    @Transactional
    public void onTickCompleted() {
        broadcaster.send("city-status", cityQueryService.buildCityStatus());

        List<com.aicitybrain.domain.AgentEvent> recentEvents = agentEventRepository.findTop50ByOrderByCreatedAtDesc()
            .stream().limit(10).toList();
        List<AgentAction> recentActions = agentActionRepository.findTop50ByOrderByCreatedAtDesc()
            .stream().limit(10).toList();
        List<com.aicitybrain.dto.AgentDtos.DecisionFeedItem> feed =
            cityQueryService.buildRecentDecisionFeed(recentEvents, recentActions).stream().limit(12).toList();
        broadcaster.send("decisions", feed);
    }

    private String humanize(String eventType) {
        String[] parts = eventType.split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!sb.isEmpty()) sb.append(' ');
            sb.append(part.charAt(0)).append(part.substring(1).toLowerCase(Locale.US));
        }
        return sb.toString();
    }
}
