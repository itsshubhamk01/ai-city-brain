package com.aicitybrain.service.simulation;

import com.aicitybrain.domain.Alert;
import com.aicitybrain.domain.AgentAction;
import com.aicitybrain.domain.AgentEvent;
import com.aicitybrain.domain.City;
import com.aicitybrain.domain.Incident;
import com.aicitybrain.domain.Severity;
import com.aicitybrain.domain.Zone;
import com.aicitybrain.dto.AgentDtos;
import com.aicitybrain.dto.AlertDtos;
import com.aicitybrain.dto.CityDtos;
import com.aicitybrain.dto.IncidentDtos;
import com.aicitybrain.dto.MapDtos;
import com.aicitybrain.repository.AlertRepository;
import com.aicitybrain.repository.AmbulanceRepository;
import com.aicitybrain.repository.CityRepository;
import com.aicitybrain.repository.FireStationRepository;
import com.aicitybrain.repository.HospitalRepository;
import com.aicitybrain.repository.IncidentRepository;
import com.aicitybrain.repository.PowerStationRepository;
import com.aicitybrain.repository.RoadRepository;
import com.aicitybrain.repository.WasteBinRepository;
import com.aicitybrain.repository.WaterStationRepository;
import com.aicitybrain.repository.ZoneRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Single, shared place that turns JPA entities into API DTOs. Used by REST controllers
 * (on-demand reads) and by {@link com.aicitybrain.service.brain.CityBrainService}
 * (periodic WebSocket broadcasts) so the two never drift out of sync.
 */
@Service
@Transactional(readOnly = true)
public class CityQueryService {

    private final CityRepository cityRepository;
    private final ZoneRepository zoneRepository;
    private final IncidentRepository incidentRepository;
    private final AlertRepository alertRepository;
    private final RoadRepository roadRepository;
    private final HospitalRepository hospitalRepository;
    private final AmbulanceRepository ambulanceRepository;
    private final FireStationRepository fireStationRepository;
    private final WasteBinRepository wasteBinRepository;
    private final PowerStationRepository powerStationRepository;
    private final WaterStationRepository waterStationRepository;

    public CityQueryService(CityRepository cityRepository, ZoneRepository zoneRepository,
                             IncidentRepository incidentRepository, AlertRepository alertRepository,
                             RoadRepository roadRepository, HospitalRepository hospitalRepository,
                             AmbulanceRepository ambulanceRepository, FireStationRepository fireStationRepository,
                             WasteBinRepository wasteBinRepository, PowerStationRepository powerStationRepository,
                             WaterStationRepository waterStationRepository) {
        this.cityRepository = cityRepository;
        this.zoneRepository = zoneRepository;
        this.incidentRepository = incidentRepository;
        this.alertRepository = alertRepository;
        this.roadRepository = roadRepository;
        this.hospitalRepository = hospitalRepository;
        this.ambulanceRepository = ambulanceRepository;
        this.fireStationRepository = fireStationRepository;
        this.wasteBinRepository = wasteBinRepository;
        this.powerStationRepository = powerStationRepository;
        this.waterStationRepository = waterStationRepository;
    }

    public City requireCity() {
        return cityRepository.findFirstByOrderByCreatedAtAsc()
            .orElseThrow(() -> new NoSuchElementException("No city has been seeded yet"));
    }

    public CityDtos.ZoneStatusResponse toZoneResponse(Zone z) {
        return new CityDtos.ZoneStatusResponse(
            z.getId(), z.getName(), z.getKind().name(), z.getCenterLat(), z.getCenterLng(), z.getPopulation(),
            z.getTrafficLevel(), z.getRainfallMm(), z.getFloodRiskScore(), z.getPowerDemandMw(), z.getPowerSupplyMw(),
            z.getHospitalOccupancyPct(), z.getWasteLevelPct(), z.getAqi(), z.getWaterSupplyPct(), z.getRiskScore()
        );
    }

    public IncidentDtos.IncidentResponse toIncidentResponse(Incident i) {
        return new IncidentDtos.IncidentResponse(
            i.getId(), i.getType().name(), i.getSeverity().name(), i.getStatus().name(),
            i.getZone().getId(), i.getZone().getName(), i.getDescription(), i.getLat(), i.getLng(),
            i.getAssignedAgent() == null ? null : i.getAssignedAgent().name(),
            i.getCreatedAt(), i.getResolvedAt()
        );
    }

    public CityDtos.CityStatusResponse buildCityStatus() {
        City city = requireCity();
        List<Zone> zones = zoneRepository.findByCityOrderByNameAsc(city);

        List<CityDtos.ZoneStatusResponse> zoneResponses = zones.stream().map(this::toZoneResponse).toList();

        double trafficAvg = average(zones.stream().mapToDouble(Zone::getTrafficLevel));
        double aqiAvg = average(zones.stream().mapToDouble(Zone::getAqi));
        double waterAvg = average(zones.stream().mapToDouble(Zone::getWaterSupplyPct));
        double powerAvg = average(zones.stream().mapToDouble(z -> z.getPowerSupplyMw() <= 0 ? 100
            : Math.min(100, (z.getPowerDemandMw() / z.getPowerSupplyMw()) * 100)));
        double hospitalAvg = average(zones.stream().mapToDouble(Zone::getHospitalOccupancyPct));
        double overallRisk = average(zones.stream().mapToDouble(Zone::getRiskScore));

        long activeIncidents = incidentRepository.countByStatusNot(Incident.Status.RESOLVED);
        long criticalAlerts = alertRepository.countByAcknowledgedFalseAndSeverityIn(List.of(Severity.HIGH, Severity.CRITICAL));

        return new CityDtos.CityStatusResponse(
            city.getId(), city.getName(), city.getPopulation(), zoneResponses,
            RiskScoring.riskLevel(overallRisk), overallRisk,
            activeIncidents, criticalAlerts,
            trafficAvg, aqiAvg, waterAvg, powerAvg, hospitalAvg,
            Instant.now()
        );
    }

    public MapDtos.MapDataResponse buildMapData() {
        City city = requireCity();
        List<Zone> zones = zoneRepository.findByCityOrderByNameAsc(city);

        List<MapDtos.RoadDto> roads = roadRepository.findAll().stream()
            .map(r -> new MapDtos.RoadDto(r.getId(), r.getName(), r.getStartLat(), r.getStartLng(),
                r.getEndLat(), r.getEndLng(), r.getStatus().name(), r.getCongestionPct()))
            .toList();

        List<MapDtos.HospitalDto> hospitals = hospitalRepository.findAll().stream()
            .map(h -> new MapDtos.HospitalDto(h.getId(), h.getName(), h.getLat(), h.getLng(),
                h.getTotalBeds(), h.getOccupiedBeds(), h.getOccupancyPct()))
            .toList();

        List<MapDtos.AmbulanceDto> ambulances = ambulanceRepository.findAll().stream()
            .map(a -> new MapDtos.AmbulanceDto(a.getId(), a.getCode(), a.getLat(), a.getLng(), a.getStatus().name()))
            .toList();

        List<MapDtos.FireStationDto> fireStations = fireStationRepository.findAll().stream()
            .map(f -> new MapDtos.FireStationDto(f.getId(), f.getName(), f.getLat(), f.getLng(),
                f.getTotalUnits(), f.getAvailableUnits()))
            .toList();

        List<MapDtos.WasteBinDto> wasteBins = wasteBinRepository.findAll().stream()
            .map(w -> new MapDtos.WasteBinDto(w.getId(), w.getCode(), w.getLat(), w.getLng(), w.getCapacityPct()))
            .toList();

        List<MapDtos.PowerStationDto> powerStations = powerStationRepository.findAll().stream()
            .map(p -> new MapDtos.PowerStationDto(p.getId(), p.getName(), p.getLat(), p.getLng(),
                p.getCapacityMw(), p.getCurrentLoadMw()))
            .toList();

        List<MapDtos.WaterStationDto> waterStations = waterStationRepository.findAll().stream()
            .map(w -> new MapDtos.WaterStationDto(w.getId(), w.getName(), w.getLat(), w.getLng(), w.getReservoirLevelPct()))
            .toList();

        List<IncidentDtos.IncidentResponse> incidents = incidentRepository.findByStatusNotOrderByCreatedAtDesc(Incident.Status.RESOLVED)
            .stream().map(this::toIncidentResponse).toList();

        return new MapDtos.MapDataResponse(
            zones.stream().map(this::toZoneResponse).toList(),
            roads, hospitals, ambulances, fireStations, wasteBins, powerStations, waterStations, incidents
        );
    }

    public AlertDtos.AlertResponse toAlertResponse(Alert a) {
        return new AlertDtos.AlertResponse(
            a.getId(), a.getSeverity().name(), a.getTitle(), a.getMessage(),
            a.getZone() == null ? "City-wide" : a.getZone().getName(),
            a.getSource(), a.isAcknowledged(), a.getCreatedAt()
        );
    }

    public AgentDtos.DecisionFeedItem toDecisionFeedItem(AgentEvent e) {
        return new AgentDtos.DecisionFeedItem(
            e.getId(), e.getAgentType().name(), "EVENT", e.getEventType(), e.getSummary(),
            e.getSeverity().name(), e.getZone() == null ? "City-wide" : e.getZone().getName(), e.getCreatedAt()
        );
    }

    public AgentDtos.DecisionFeedItem toDecisionFeedItem(AgentAction a) {
        return new AgentDtos.DecisionFeedItem(
            a.getId(), a.getAgentType().name(), "ACTION", a.getActionType(), a.getDescription(),
            null, a.getZone() == null ? "City-wide" : a.getZone().getName(), a.getCreatedAt()
        );
    }

    public List<AgentDtos.DecisionFeedItem> buildRecentDecisionFeed(List<AgentEvent> events, List<AgentAction> actions) {
        List<AgentDtos.DecisionFeedItem> merged = new java.util.ArrayList<>();
        events.forEach(e -> merged.add(toDecisionFeedItem(e)));
        actions.forEach(a -> merged.add(toDecisionFeedItem(a)));
        merged.sort((x, y) -> y.createdAt().compareTo(x.createdAt()));
        return merged;
    }

    private double average(java.util.stream.DoubleStream stream) {
        double[] values = stream.toArray();
        if (values.length == 0) return 0;
        return java.util.Arrays.stream(values).average().orElse(0);
    }
}
