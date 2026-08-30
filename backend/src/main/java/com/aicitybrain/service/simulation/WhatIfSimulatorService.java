package com.aicitybrain.service.simulation;

import com.aicitybrain.domain.Zone;
import com.aicitybrain.dto.WhatIfDtos;
import com.aicitybrain.repository.HospitalRepository;
import com.aicitybrain.repository.ZoneRepository;
import com.aicitybrain.service.ai.InsightGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Answers "what if…?" questions by projecting the CURRENT live city state forward
 * under a hypothetical delta — without writing anything back to the database. This is
 * intentionally a pure read + compute path so operators can safely explore scenarios
 * without ever risking the real simulation state.
 */
@Service
@Transactional(readOnly = true)
public class WhatIfSimulatorService {

    private final ZoneRepository zoneRepository;
    private final HospitalRepository hospitalRepository;
    private final InsightGenerator insightGenerator;

    public WhatIfSimulatorService(ZoneRepository zoneRepository, HospitalRepository hospitalRepository,
                                   InsightGenerator insightGenerator) {
        this.zoneRepository = zoneRepository;
        this.hospitalRepository = hospitalRepository;
        this.insightGenerator = insightGenerator;
    }

    public WhatIfDtos.WhatIfResponse evaluate(WhatIfDtos.WhatIfRequest request) {
        List<Zone> zones = zoneRepository.findAll();
        double rainfallDeltaPct = request.rainfallDeltaPct() == null ? 0 : request.rainfallDeltaPct();
        double trafficDeltaPct = request.trafficDeltaPct() == null ? 0 : request.trafficDeltaPct();

        double worstFloodRisk = zones.stream()
            .mapToDouble(z -> RiskScoring.computeFloodRisk(z.getRainfallMm() * (1 + rainfallDeltaPct / 100.0), z.getKind()))
            .max().orElse(0);
        String floodRiskLevel = RiskScoring.riskLevel(worstFloodRisk);

        double trafficImpactPct = RiskScoring.clamp(Math.abs(trafficDeltaPct) * 0.8) * Math.signum(trafficDeltaPct);

        String powerOutageZoneName = null;
        int emergencyResponseDeltaMinutes = 0;
        int hospitalsPotentiallyAffected = 0;

        if (request.powerOutageZoneId() != null) {
            Zone zone = zoneRepository.findById(request.powerOutageZoneId()).orElse(null);
            if (zone != null) {
                powerOutageZoneName = zone.getName();
                int minutes = request.powerOutageDurationMinutes() == null ? 30 : request.powerOutageDurationMinutes();
                // Heuristic: signal outages + rerouting add roughly 15% of the outage duration to average response time.
                emergencyResponseDeltaMinutes = (int) Math.round(minutes * 0.15);
                hospitalsPotentiallyAffected = hospitalRepository.findByZone(zone).size();
            }
        }

        List<String> actions = buildRecommendations(floodRiskLevel, trafficDeltaPct, powerOutageZoneName, hospitalsPotentiallyAffected);

        InsightGenerator.WhatIfContext context = new InsightGenerator.WhatIfContext(
            request.rainfallDeltaPct(), request.trafficDeltaPct(), powerOutageZoneName,
            request.powerOutageDurationMinutes(), request.freeformQuery(),
            floodRiskLevel, trafficImpactPct, emergencyResponseDeltaMinutes, hospitalsPotentiallyAffected, actions
        );
        String narrative = insightGenerator.generateWhatIfNarrative(context);

        return new WhatIfDtos.WhatIfResponse(floodRiskLevel, trafficImpactPct, emergencyResponseDeltaMinutes,
            hospitalsPotentiallyAffected, actions, narrative);
    }

    private List<String> buildRecommendations(String floodRiskLevel, double trafficDeltaPct,
                                               String powerOutageZoneName, int hospitalsAffected) {
        List<String> actions = new ArrayList<>();
        if ("CRITICAL".equals(floodRiskLevel)) {
            actions.add("Pre-position evacuation resources and sandbags in low-lying zones");
        } else if ("HIGH".equals(floodRiskLevel)) {
            actions.add("Put drainage and public-works crews on standby");
        }
        if (trafficDeltaPct > 20) {
            actions.add("Activate alternate signal-timing plans and pre-stage traffic officers");
        }
        if (powerOutageZoneName != null) {
            actions.add("Confirm backup generators are ready at " + hospitalsAffected + " hospital(s) in " + powerOutageZoneName);
        }
        if (actions.isEmpty()) {
            actions.add("No immediate action required at this magnitude — continue routine monitoring");
        }
        return actions;
    }
}
