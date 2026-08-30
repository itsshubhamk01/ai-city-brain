package com.aicitybrain.service.agents;

import com.aicitybrain.domain.AgentType;
import com.aicitybrain.domain.Incident;
import com.aicitybrain.domain.Severity;
import com.aicitybrain.domain.Zone;
import com.aicitybrain.repository.AgentActionRepository;
import com.aicitybrain.repository.AgentEventRepository;
import com.aicitybrain.repository.IncidentRepository;
import com.aicitybrain.repository.ZoneRepository;
import com.aicitybrain.service.events.EventBus;
import com.aicitybrain.service.events.EventTypes;
import com.aicitybrain.service.simulation.RiskScoring;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Owns flood-risk assessment. Reads the raw rainfall figure the simulation engine
 * writes each tick, applies the zone's drainage-capacity model, and classifies the
 * result — mirroring the worked example in the spec: "Rainfall 82mm + drainage 60% →
 * Flood Risk HIGH". On HIGH/CRITICAL it publishes an event other agents (Traffic,
 * Emergency) react to, and CityBrain uses to raise a citizen alert.
 */
@Component
public class FloodAgent extends AbstractAgent {

    private final ZoneRepository zoneRepository;
    private final IncidentRepository incidentRepository;

    public FloodAgent(EventBus eventBus, AgentEventRepository agentEventRepository,
                       AgentActionRepository agentActionRepository, ZoneRepository zoneRepository,
                       IncidentRepository incidentRepository) {
        super(eventBus, agentEventRepository, agentActionRepository);
        this.zoneRepository = zoneRepository;
        this.incidentRepository = incidentRepository;
    }

    @Override
    public AgentType type() {
        return AgentType.FLOOD;
    }

    @Override
    @Transactional
    public void evaluate() {
        List<Zone> zones = zoneRepository.findAll();
        for (Zone zone : zones) {
            double risk = RiskScoring.computeFloodRisk(zone.getRainfallMm(), zone.getKind());
            zone.setFloodRiskScore(risk);
            zoneRepository.save(zone);

            if (risk >= RiskScoring.FLOOD_CRITICAL_THRESHOLD) {
                handleCritical(zone, risk);
            } else if (risk >= RiskScoring.FLOOD_HIGH_THRESHOLD) {
                handleHigh(zone, risk);
            }
        }
    }

    private void handleHigh(Zone zone, double risk) {
        String summary = "Rainfall %.0fmm in %s with %d%% drainage capacity → flood risk HIGH (%.0f/100)."
            .formatted(zone.getRainfallMm(), zone.getName(),
                (int) ((1 - RiskScoring.drainageFactor(zone.getKind())) * 100), risk);
        logEvent(EventTypes.FLOOD_RISK_HIGH, Severity.HIGH, zone, summary);
        logAction("DRAINAGE_ALERT", "Notified drainage/public-works teams for " + zone.getName()
            + "; monitoring rainfall trend for escalation.", zone, null);
        publish(EventTypes.FLOOD_RISK_HIGH, zone.getId(), Severity.HIGH,
            Map.of("riskScore", risk, "rainfallMm", zone.getRainfallMm(), "zoneName", zone.getName()));
    }

    private void handleCritical(Zone zone, double risk) {
        String summary = "Rainfall %.0fmm in %s exceeds safe drainage capacity → flood risk CRITICAL (%.0f/100)."
            .formatted(zone.getRainfallMm(), zone.getName(), risk);
        logEvent(EventTypes.FLOOD_RISK_CRITICAL, Severity.CRITICAL, zone, summary);

        Incident incident = null;
        if (!incidentRepository.existsByZoneAndTypeAndStatusNot(zone, Incident.Type.FLOOD, Incident.Status.RESOLVED)) {
            incident = new Incident(zone, Incident.Type.FLOOD, Severity.CRITICAL,
                "Critical flood risk detected — rainfall exceeding zone drainage capacity.",
                zone.getCenterLat(), zone.getCenterLng());
            incident.setAssignedAgent(AgentType.FLOOD);
            incident = incidentRepository.save(incident);
        }

        logAction("EVACUATION_RECOMMENDATION",
            "Recommended evacuation staging for low-lying areas of " + zone.getName()
                + "; flood risk score " + Math.round(risk) + "/100.", zone, incident);

        publish(EventTypes.FLOOD_RISK_CRITICAL, zone.getId(), Severity.CRITICAL,
            Map.of("riskScore", risk, "rainfallMm", zone.getRainfallMm(), "zoneName", zone.getName()));
        publish(EventTypes.ZONE_EVACUATION_RECOMMENDED, zone.getId(), Severity.CRITICAL,
            Map.of("zoneName", zone.getName(), "reason", "flood"));
    }
}
