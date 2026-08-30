package com.aicitybrain.service.agents;

import com.aicitybrain.domain.AgentType;
import com.aicitybrain.domain.Road;
import com.aicitybrain.domain.Severity;
import com.aicitybrain.domain.Zone;
import com.aicitybrain.repository.AgentActionRepository;
import com.aicitybrain.repository.AgentEventRepository;
import com.aicitybrain.repository.RoadRepository;
import com.aicitybrain.repository.ZoneRepository;
import com.aicitybrain.service.events.CityEvent;
import com.aicitybrain.service.events.EventBus;
import com.aicitybrain.service.events.EventTypes;
import com.aicitybrain.service.simulation.RiskScoring;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Owns traffic-congestion assessment and road status. Reacts to its own thresholds
 * every tick, and also reacts live to {@link FloodAgent}'s output — when a zone is
 * flagged HIGH/CRITICAL flood risk, TrafficAgent closes the roads in that zone and
 * logs a rerouting decision: the "agents talk through the event bus, not to each
 * other directly" coordination pattern described in the spec.
 */
@Component
public class TrafficAgent extends AbstractAgent {

    private final ZoneRepository zoneRepository;
    private final RoadRepository roadRepository;

    public TrafficAgent(EventBus eventBus, AgentEventRepository agentEventRepository,
                         AgentActionRepository agentActionRepository, ZoneRepository zoneRepository,
                         RoadRepository roadRepository) {
        super(eventBus, agentEventRepository, agentActionRepository);
        this.zoneRepository = zoneRepository;
        this.roadRepository = roadRepository;
    }

    @PostConstruct
    void subscribeToFloodEvents() {
        eventBus.subscribe(EventTypes.FLOOD_RISK_HIGH, this::onFloodRisk);
        eventBus.subscribe(EventTypes.FLOOD_RISK_CRITICAL, this::onFloodRisk);
    }

    @Override
    public AgentType type() {
        return AgentType.TRAFFIC;
    }

    @Override
    @Transactional
    public void evaluate() {
        for (Zone zone : zoneRepository.findAll()) {
            double traffic = zone.getTrafficLevel();
            List<Road> roads = roadRepository.findByZone(zone);

            if (traffic >= RiskScoring.TRAFFIC_CRITICAL_THRESHOLD) {
                markCongestion(zone, roads, traffic, Severity.CRITICAL, EventTypes.TRAFFIC_CONGESTION_CRITICAL);
            } else if (traffic >= RiskScoring.TRAFFIC_HIGH_THRESHOLD) {
                markCongestion(zone, roads, traffic, Severity.HIGH, EventTypes.TRAFFIC_CONGESTION_HIGH);
            } else {
                roads.stream()
                    .filter(r -> r.getStatus() == Road.Status.CONGESTED)
                    .forEach(r -> {
                        r.setStatus(Road.Status.OPEN);
                        r.setCongestionPct(traffic);
                        roadRepository.save(r);
                    });
            }
        }
    }

    private void markCongestion(Zone zone, List<Road> roads, double traffic, Severity severity, String eventType) {
        roads.forEach(r -> {
            if (r.getStatus() != Road.Status.CLOSED) {
                r.setStatus(Road.Status.CONGESTED);
                r.setCongestionPct(traffic);
                roadRepository.save(r);
            }
        });

        String summary = "%s congestion at %.0f%% (%d road%s affected) → recommending signal retiming and diversion."
            .formatted(zone.getName(), traffic, roads.size(), roads.size() == 1 ? "" : "s");
        logEvent(eventType, severity, zone, summary);
        logAction("SIGNAL_RETIME_AND_DIVERSION",
            "Retimed traffic signals and issued diversion guidance for " + zone.getName()
                + " (" + Math.round(traffic) + "% congestion).", zone, null);
        publish(eventType, zone.getId(), severity, Map.of("trafficLevel", traffic, "zoneName", zone.getName()));
    }

    private void onFloodRisk(CityEvent event) {
        UUID zoneId = event.zoneId();
        if (zoneId == null) return;
        zoneRepository.findById(zoneId).ifPresent(zone -> {
            List<Road> roads = roadRepository.findByZone(zone);
            long closedCount = roads.stream()
                .filter(r -> r.getStatus() != Road.Status.CLOSED)
                .peek(r -> {
                    r.setStatus(Road.Status.CLOSED);
                    roadRepository.save(r);
                })
                .count();

            if (closedCount > 0) {
                String zoneName = (String) event.data().getOrDefault("zoneName", zone.getName());
                logAction("FLOOD_ROAD_CLOSURE",
                    "Closed " + closedCount + " road(s) in " + zoneName + " due to flood risk and rerouted traffic.",
                    zone, null);
                publish(EventTypes.ROAD_STATUS_CHANGED, zone.getId(), event.severity(),
                    Map.of("reason", "flood", "roadsClosed", closedCount, "zoneName", zoneName));
            }
        });
    }
}
