package com.aicitybrain.service.agents;

import com.aicitybrain.domain.AgentType;
import com.aicitybrain.domain.Hospital;
import com.aicitybrain.domain.Severity;
import com.aicitybrain.repository.AgentActionRepository;
import com.aicitybrain.repository.AgentEventRepository;
import com.aicitybrain.repository.HospitalRepository;
import com.aicitybrain.service.events.CityEvent;
import com.aicitybrain.service.events.EventBus;
import com.aicitybrain.service.events.EventTypes;
import com.aicitybrain.service.simulation.RiskScoring;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Monitors hospital bed occupancy citywide. Once a hospital crosses the HIGH/CRITICAL
 * threshold it recommends redirecting new patients to whichever hospital currently has
 * the most spare capacity — the exact "recommend where emergency patients should be
 * sent" behavior called out in the spec.
 */
@Component
public class HealthcareAgent extends AbstractAgent {

    private final HospitalRepository hospitalRepository;

    public HealthcareAgent(EventBus eventBus, AgentEventRepository agentEventRepository,
                            AgentActionRepository agentActionRepository, HospitalRepository hospitalRepository) {
        super(eventBus, agentEventRepository, agentActionRepository);
        this.hospitalRepository = hospitalRepository;
    }

    @PostConstruct
    void subscribeToMedicalEmergencies() {
        eventBus.subscribe(EventTypes.MEDICAL_EMERGENCY_REPORTED, this::onMedicalEmergency);
    }

    @Override
    public AgentType type() {
        return AgentType.HEALTHCARE;
    }

    @Override
    @Transactional
    public void evaluate() {
        List<Hospital> hospitals = hospitalRepository.findAll();

        for (Hospital hospital : hospitals) {
            double occupancy = hospital.getOccupancyPct();
            if (occupancy < RiskScoring.HOSPITAL_HIGH_THRESHOLD) continue;

            Severity severity = occupancy >= RiskScoring.HOSPITAL_CRITICAL_THRESHOLD ? Severity.CRITICAL : Severity.HIGH;
            String eventType = severity == Severity.CRITICAL
                ? EventTypes.HOSPITAL_CAPACITY_CRITICAL
                : EventTypes.HOSPITAL_CAPACITY_LOW;

            Optional<Hospital> altHospital = hospitals.stream()
                .filter(h -> !h.getId().equals(hospital.getId()))
                .min(Comparator.comparingDouble(Hospital::getOccupancyPct));

            String summary = "%s at %.0f%% bed occupancy (%d/%d beds)."
                .formatted(hospital.getName(), occupancy, hospital.getOccupiedBeds(), hospital.getTotalBeds());
            logEvent(eventType, severity, hospital.getZone(), summary);

            String recommendation = altHospital
                .map(h -> "Redirecting new patients to " + h.getName() + " (" + Math.round(h.getOccupancyPct()) + "% occupied).")
                .orElse("No lower-occupancy hospital currently available citywide.");
            logAction("PATIENT_REDIRECT_RECOMMENDATION", recommendation, hospital.getZone(), null);

            publish(eventType, hospital.getZone().getId(), severity,
                Map.of("hospitalName", hospital.getName(), "occupancyPct", occupancy));
        }
    }

    private void onMedicalEmergency(CityEvent event) {
        if (event.zoneId() == null) return;
        hospitalRepository.findAll().stream()
            .filter(h -> h.getZone().getId().equals(event.zoneId()))
            .min(Comparator.comparingDouble(Hospital::getOccupancyPct))
            .ifPresent(hospital -> {
                if (hospital.getOccupiedBeds() < hospital.getTotalBeds()) {
                    hospital.setOccupiedBeds(hospital.getOccupiedBeds() + 1);
                    hospitalRepository.save(hospital);
                }
            });
    }
}
