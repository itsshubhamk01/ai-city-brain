package com.aicitybrain.service.agents;

import com.aicitybrain.domain.Ambulance;
import com.aicitybrain.domain.AgentType;
import com.aicitybrain.domain.FireStation;
import com.aicitybrain.domain.Incident;
import com.aicitybrain.domain.Severity;
import com.aicitybrain.domain.Zone;
import com.aicitybrain.repository.AgentActionRepository;
import com.aicitybrain.repository.AgentEventRepository;
import com.aicitybrain.repository.AmbulanceRepository;
import com.aicitybrain.repository.FireStationRepository;
import com.aicitybrain.repository.IncidentRepository;
import com.aicitybrain.service.events.CityEvent;
import com.aicitybrain.service.events.EventBus;
import com.aicitybrain.service.events.EventTypes;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Dispatches available emergency resources (ambulances, fire units) to unresolved
 * incidents, nearest-zone-first, and reacts to flood events by rerouting ambulances
 * that are currently inside a flooded zone.
 */
@Component
public class EmergencyAgent extends AbstractAgent {

    private final IncidentRepository incidentRepository;
    private final AmbulanceRepository ambulanceRepository;
    private final FireStationRepository fireStationRepository;

    public EmergencyAgent(EventBus eventBus, AgentEventRepository agentEventRepository,
                           AgentActionRepository agentActionRepository, IncidentRepository incidentRepository,
                           AmbulanceRepository ambulanceRepository, FireStationRepository fireStationRepository) {
        super(eventBus, agentEventRepository, agentActionRepository);
        this.incidentRepository = incidentRepository;
        this.ambulanceRepository = ambulanceRepository;
        this.fireStationRepository = fireStationRepository;
    }

    @PostConstruct
    void subscribeToFloodEvents() {
        eventBus.subscribe(EventTypes.FLOOD_RISK_CRITICAL, this::onFloodRisk);
    }

    @Override
    public AgentType type() {
        return AgentType.EMERGENCY;
    }

    @Override
    @Transactional
    public void evaluate() {
        List<Incident> unassigned = incidentRepository.findByStatusNotAndAssignedAgentIsNull(Incident.Status.RESOLVED);

        for (Incident incident : unassigned) {
            switch (incident.getType()) {
                case TRAFFIC_ACCIDENT, MEDICAL_EMERGENCY -> dispatchAmbulance(incident);
                case FIRE -> dispatchFireUnit(incident);
                default -> { /* handled by other agents (flood/waste/power) */ }
            }
        }
    }

    private void dispatchAmbulance(Incident incident) {
        Zone zone = incident.getZone();
        List<Ambulance> available = ambulanceRepository.findByStatus(Ambulance.Status.AVAILABLE);
        Optional<Ambulance> chosen = available.stream()
            .filter(a -> a.getZone().getId().equals(zone.getId()))
            .findFirst()
            .or(() -> available.stream().findFirst());

        if (chosen.isEmpty()) {
            logEvent(EventTypes.MEDICAL_EMERGENCY_REPORTED, Severity.HIGH, zone,
                "No ambulances available for " + incident.getType() + " in " + zone.getName() + " — queued.");
            return;
        }

        Ambulance ambulance = chosen.get();
        ambulance.setStatus(Ambulance.Status.DISPATCHED);
        ambulanceRepository.save(ambulance);

        incident.setAssignedAgent(AgentType.EMERGENCY);
        incident.setStatus(Incident.Status.IN_PROGRESS);
        incidentRepository.save(incident);

        String desc = "Ambulance " + ambulance.getCode() + " dispatched to " + incident.getType()
            + " in " + zone.getName() + ".";
        logAction("AMBULANCE_DISPATCH", desc, zone, incident);
        publish(EventTypes.AMBULANCE_DISPATCHED, zone.getId(), incident.getSeverity(),
            Map.of("ambulanceCode", ambulance.getCode(), "incidentId", incident.getId().toString()));
    }

    private void dispatchFireUnit(Incident incident) {
        Zone zone = incident.getZone();
        List<FireStation> stations = fireStationRepository.findAll();
        Optional<FireStation> chosen = stations.stream()
            .filter(s -> s.getZone().getId().equals(zone.getId()) && s.getAvailableUnits() > 0)
            .findFirst()
            .or(() -> stations.stream().filter(s -> s.getAvailableUnits() > 0)
                .max(Comparator.comparingInt(FireStation::getAvailableUnits)));

        if (chosen.isEmpty()) {
            logEvent(EventTypes.FIRE_REPORTED, Severity.CRITICAL, zone,
                "Fire reported in " + zone.getName() + " but no fire units currently available.");
            return;
        }

        FireStation station = chosen.get();
        station.setAvailableUnits(station.getAvailableUnits() - 1);
        fireStationRepository.save(station);

        incident.setAssignedAgent(AgentType.EMERGENCY);
        incident.setStatus(Incident.Status.IN_PROGRESS);
        incidentRepository.save(incident);

        String desc = "Fire unit dispatched from " + station.getName() + " to incident in " + zone.getName() + ".";
        logAction("FIRE_UNIT_DISPATCH", desc, zone, incident);
    }

    private void onFloodRisk(CityEvent event) {
        UUID zoneId = event.zoneId();
        if (zoneId == null) return;
        List<Ambulance> inZone = ambulanceRepository.findAll().stream()
            .filter(a -> a.getZone().getId().equals(zoneId) && a.getStatus() != Ambulance.Status.AVAILABLE)
            .toList();
        if (!inZone.isEmpty()) {
            String zoneName = (String) event.data().getOrDefault("zoneName", "affected zone");
            logAction("AMBULANCE_REROUTE",
                inZone.size() + " ambulance(s) rerouted around flood risk in " + zoneName + ".",
                inZone.get(0).getZone(), null);
        }
    }
}
