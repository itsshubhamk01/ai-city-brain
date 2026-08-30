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

import java.util.Map;

/**
 * Monitors power demand against supply per zone. Escalates to a POWER_OUTAGE incident
 * once demand meets or exceeds available supply, mirroring a real grid-strain scenario.
 */
@Component
public class EnergyAgent extends AbstractAgent {

    private final ZoneRepository zoneRepository;
    private final IncidentRepository incidentRepository;

    public EnergyAgent(EventBus eventBus, AgentEventRepository agentEventRepository,
                        AgentActionRepository agentActionRepository, ZoneRepository zoneRepository,
                        IncidentRepository incidentRepository) {
        super(eventBus, agentEventRepository, agentActionRepository);
        this.zoneRepository = zoneRepository;
        this.incidentRepository = incidentRepository;
    }

    @Override
    public AgentType type() {
        return AgentType.ENERGY;
    }

    @Override
    @Transactional
    public void evaluate() {
        for (Zone zone : zoneRepository.findAll()) {
            double ratio = zone.getPowerSupplyMw() <= 0 ? 999
                : zone.getPowerDemandMw() / zone.getPowerSupplyMw();

            if (ratio >= RiskScoring.POWER_STRAIN_CRITICAL_RATIO) {
                handleOutage(zone, ratio);
            } else if (ratio >= RiskScoring.POWER_STRAIN_HIGH_RATIO) {
                handleHighDemand(zone, ratio);
            }
        }
    }

    private void handleHighDemand(Zone zone, double ratio) {
        String summary = "%s demand at %.0f MW against %.0f MW supply (%.0f%% of capacity) — approaching limit."
            .formatted(zone.getName(), zone.getPowerDemandMw(), zone.getPowerSupplyMw(), ratio * 100);
        logEvent(EventTypes.POWER_DEMAND_HIGH, Severity.HIGH, zone, summary);
        logAction("LOAD_BALANCING", "Requested load-balancing/demand response for " + zone.getName() + ".", zone, null);
        publish(EventTypes.POWER_DEMAND_HIGH, zone.getId(), Severity.HIGH,
            Map.of("demandMw", zone.getPowerDemandMw(), "supplyMw", zone.getPowerSupplyMw()));
    }

    private void handleOutage(Zone zone, double ratio) {
        String summary = "%s demand (%.0f MW) has met or exceeded available supply (%.0f MW) — outage risk CRITICAL."
            .formatted(zone.getName(), zone.getPowerDemandMw(), zone.getPowerSupplyMw());
        logEvent(EventTypes.POWER_OUTAGE, Severity.CRITICAL, zone, summary);

        Incident incident = null;
        if (!incidentRepository.existsByZoneAndTypeAndStatusNot(zone, Incident.Type.POWER_OUTAGE, Incident.Status.RESOLVED)) {
            incident = new Incident(zone, Incident.Type.POWER_OUTAGE, Severity.CRITICAL,
                "Power demand exceeds available supply — rolling outage risk.",
                zone.getCenterLat(), zone.getCenterLng());
            incident.setAssignedAgent(AgentType.ENERGY);
            incident = incidentRepository.save(incident);
        }

        logAction("GRID_REBALANCE", "Initiated emergency grid rebalancing for " + zone.getName()
            + "; prioritizing hospitals and emergency services.", zone, incident);
        publish(EventTypes.POWER_OUTAGE, zone.getId(), Severity.CRITICAL,
            Map.of("demandMw", zone.getPowerDemandMw(), "supplyMw", zone.getPowerSupplyMw()));
    }
}
