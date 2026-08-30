package com.aicitybrain.service.simulation;

import com.aicitybrain.domain.Ambulance;
import com.aicitybrain.domain.FireStation;
import com.aicitybrain.domain.Incident;
import com.aicitybrain.domain.Severity;
import com.aicitybrain.domain.Zone;
import com.aicitybrain.domain.ZoneStatusSnapshot;
import com.aicitybrain.dto.SimulationDtos;
import com.aicitybrain.repository.AmbulanceRepository;
import com.aicitybrain.repository.FireStationRepository;
import com.aicitybrain.repository.IncidentRepository;
import com.aicitybrain.repository.ZoneRepository;
import com.aicitybrain.repository.ZoneStatusSnapshotRepository;
import com.aicitybrain.service.agents.EmergencyAgent;
import com.aicitybrain.service.agents.EnergyAgent;
import com.aicitybrain.service.agents.FloodAgent;
import com.aicitybrain.service.agents.HealthcareAgent;
import com.aicitybrain.service.agents.TrafficAgent;
import com.aicitybrain.service.agents.WasteAgent;
import com.aicitybrain.service.brain.CityBrainService;
import com.aicitybrain.service.events.CityEvent;
import com.aicitybrain.service.events.EventBus;
import com.aicitybrain.service.events.EventTypes;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The "world simulator". Every tick it:
 * <ol>
 *   <li>writes fresh raw sensor readings (traffic, rainfall, AQI, power demand, hospital
 *       pressure, waste drift, water supply) onto every zone, driven by the operator's
 *       simulation-control sliders plus bounded random noise;</li>
 *   <li>occasionally spawns an organic incident so the city feels alive even with no
 *       operator input;</li>
 *   <li>runs every specialized agent in a fixed, sensible order (Flood before Traffic,
 *       so Traffic can react to fresh flood data in the same tick; Emergency last, so
 *       it can dispatch to anything spawned earlier in the same tick);</li>
 *   <li>recomputes each zone's aggregate risk score;</li>
 *   <li>auto-resolves incidents that have been in progress long enough, freeing the
 *       responding resources back up; and</li>
 *   <li>hands off to {@link CityBrainService} to push the live dashboard update.</li>
 * </ol>
 */
@Service
public class SimulationEngineService {

    private static final Logger log = LoggerFactory.getLogger(SimulationEngineService.class);
    private static final Duration IN_PROGRESS_AUTO_RESOLVE_AFTER = Duration.ofSeconds(24);
    private static final int SNAPSHOT_EVERY_N_TICKS = 5;

    private final ZoneRepository zoneRepository;
    private final IncidentRepository incidentRepository;
    private final ZoneStatusSnapshotRepository snapshotRepository;
    private final AmbulanceRepository ambulanceRepository;
    private final FireStationRepository fireStationRepository;
    private final EventBus eventBus;
    private final CityBrainService cityBrainService;

    private final FloodAgent floodAgent;
    private final TrafficAgent trafficAgent;
    private final EnergyAgent energyAgent;
    private final WasteAgent wasteAgent;
    private final HealthcareAgent healthcareAgent;
    private final EmergencyAgent emergencyAgent;

    @Value("${app.simulation.autostart}")
    private boolean autostart;

    private volatile boolean running;
    private volatile Instant lastTick;
    private int tickCount = 0;

    // --- Simulation-control sliders (0-100), mutated only through applyControl()/applyScenario() ---
    private volatile double rainfallSlider = 20;
    private volatile double trafficSlider = 35;
    private volatile double populationSlider = 50;
    private volatile double powerDemandSlider = 40;
    private volatile double emergencySlider = 10;

    public SimulationEngineService(ZoneRepository zoneRepository, IncidentRepository incidentRepository,
                                    ZoneStatusSnapshotRepository snapshotRepository, AmbulanceRepository ambulanceRepository,
                                    FireStationRepository fireStationRepository, EventBus eventBus,
                                    CityBrainService cityBrainService, FloodAgent floodAgent, TrafficAgent trafficAgent,
                                    EnergyAgent energyAgent, WasteAgent wasteAgent, HealthcareAgent healthcareAgent,
                                    EmergencyAgent emergencyAgent) {
        this.zoneRepository = zoneRepository;
        this.incidentRepository = incidentRepository;
        this.snapshotRepository = snapshotRepository;
        this.ambulanceRepository = ambulanceRepository;
        this.fireStationRepository = fireStationRepository;
        this.eventBus = eventBus;
        this.cityBrainService = cityBrainService;
        this.floodAgent = floodAgent;
        this.trafficAgent = trafficAgent;
        this.energyAgent = energyAgent;
        this.wasteAgent = wasteAgent;
        this.healthcareAgent = healthcareAgent;
        this.emergencyAgent = emergencyAgent;
    }

    @PostConstruct
    void init() {
        this.running = autostart;
        log.info("Simulation engine initialized (autostart={})", autostart);
    }

    /**
     * NOTE on transactionality: this method is called directly by Spring's scheduler
     * (never via {@code this.tick()} from within the class), so the {@code @Transactional}
     * proxy applies correctly — a common pitfall would be splitting this into a plain
     * "tick()" that internally calls a separate "@Transactional runTick()", since that
     * self-invocation bypasses the proxy and silently runs without a transaction.
     * Spring's scheduler already logs and continues past exceptions thrown by a
     * {@code @Scheduled} method (fixed-delay tasks are simply rescheduled), so no extra
     * try/catch is needed here to keep future ticks running.
     */
    @Scheduled(fixedDelayString = "${app.simulation.tick-interval-ms}")
    @Transactional
    public void tick() {
        if (!running) return;

        List<Zone> zones = zoneRepository.findAll();
        zones.forEach(this::applyRawMetrics);
        zoneRepository.saveAll(zones);

        maybeSpawnOrganicIncident(zones);

        floodAgent.evaluate();
        trafficAgent.evaluate();
        energyAgent.evaluate();
        wasteAgent.evaluate();
        healthcareAgent.evaluate();
        emergencyAgent.evaluate();

        List<Zone> refreshed = zoneRepository.findAll();
        refreshed.forEach(z -> z.setRiskScore(RiskScoring.computeZoneRisk(z)));
        zoneRepository.saveAll(refreshed);

        autoResolveStaleIncidents();

        tickCount++;
        if (tickCount % SNAPSHOT_EVERY_N_TICKS == 0) {
            refreshed.forEach(z -> snapshotRepository.save(new ZoneStatusSnapshot(z)));
        }

        lastTick = Instant.now();
        cityBrainService.onTickCompleted();
    }

    private void applyRawMetrics(Zone zone) {
        ThreadLocalRandom r = ThreadLocalRandom.current();

        double trafficKindBias = switch (zone.getKind()) {
            case DOWNTOWN -> 18;
            case AIRPORT -> 10;
            case INDUSTRIAL -> 4;
            case RIVERSIDE -> 0;
            case RESIDENTIAL -> -6;
            case SUBURBAN -> -12;
        };
        double targetTraffic = RiskScoring.clamp(18 + trafficSlider * 0.55 + trafficKindBias + r.nextDouble(-6, 6));
        zone.setTrafficLevel(smooth(zone.getTrafficLevel(), targetTraffic, 0.35));

        double targetRainfall = Math.max(0, rainfallSlider * 1.3 + r.nextDouble(-5, 5));
        zone.setRainfallMm(smooth(zone.getRainfallMm(), targetRainfall, 0.3));

        double aqiKindBias = switch (zone.getKind()) {
            case INDUSTRIAL -> 30;
            case AIRPORT -> 18;
            case DOWNTOWN -> 12;
            default -> 0;
        };
        double targetAqi = Math.max(5, 30 + trafficSlider * 0.5 + aqiKindBias + r.nextDouble(-8, 8));
        zone.setAqi(smooth(zone.getAqi(), targetAqi, 0.25));

        double targetDemand = 25 + populationSlider * 0.3 + powerDemandSlider * 0.5 + r.nextDouble(-3, 3);
        zone.setPowerDemandMw(Math.max(5, smooth(zone.getPowerDemandMw(), targetDemand, 0.3)));

        double baselineSupply = 75;
        if (zone.getPowerSupplyMw() < baselineSupply * 0.5) {
            // Recovering from a simulated outage — restore gradually rather than snapping back.
            zone.setPowerSupplyMw(Math.min(baselineSupply, zone.getPowerSupplyMw() + 3));
        } else {
            zone.setPowerSupplyMw(smooth(zone.getPowerSupplyMw(), baselineSupply + r.nextDouble(-3, 3), 0.2));
        }

        double targetHospitalOcc = RiskScoring.clamp(45 + emergencySlider * 0.4 + r.nextDouble(-4, 4));
        zone.setHospitalOccupancyPct(smooth(zone.getHospitalOccupancyPct(), targetHospitalOcc, 0.1));

        double wasteTarget = RiskScoring.clamp(zone.getWasteLevelPct() + r.nextDouble(-2.5, 4));
        zone.setWasteLevelPct(wasteTarget);

        double targetWater = RiskScoring.clamp(94 + r.nextDouble(-3, 3) - zone.getFloodRiskScore() * 0.06);
        zone.setWaterSupplyPct(smooth(zone.getWaterSupplyPct(), targetWater, 0.2));
    }

    private double smooth(double current, double target, double alpha) {
        return current + (target - current) * alpha;
    }

    private void maybeSpawnOrganicIncident(List<Zone> zones) {
        if (ThreadLocalRandom.current().nextDouble() > 0.14) return;

        Zone zone = zones.get(ThreadLocalRandom.current().nextInt(zones.size()));
        double roll = ThreadLocalRandom.current().nextDouble();
        Incident.Type type = roll < 0.55 ? Incident.Type.TRAFFIC_ACCIDENT
            : roll < 0.85 ? Incident.Type.MEDICAL_EMERGENCY
            : Incident.Type.FIRE;
        Severity severity = ThreadLocalRandom.current().nextDouble() < 0.3 ? Severity.HIGH : Severity.MODERATE;

        String description = switch (type) {
            case TRAFFIC_ACCIDENT -> "Multi-vehicle collision reported on a road in " + zone.getName() + ".";
            case MEDICAL_EMERGENCY -> "Medical emergency call received in " + zone.getName() + ".";
            case FIRE -> "Fire reported at a structure in " + zone.getName() + ".";
            default -> "Incident reported in " + zone.getName() + ".";
        };

        double jitter = 0.004;
        Incident incident = new Incident(zone, type, severity, description,
            zone.getCenterLat() + ThreadLocalRandom.current().nextDouble(-jitter, jitter),
            zone.getCenterLng() + ThreadLocalRandom.current().nextDouble(-jitter, jitter));
        incident = incidentRepository.save(incident);

        String eventType = switch (type) {
            case TRAFFIC_ACCIDENT -> EventTypes.ACCIDENT_REPORTED;
            case FIRE -> EventTypes.FIRE_REPORTED;
            case MEDICAL_EMERGENCY -> EventTypes.MEDICAL_EMERGENCY_REPORTED;
            default -> null;
        };
        if (eventType != null) {
            eventBus.publish(new CityEvent(eventType, com.aicitybrain.domain.AgentType.CITY_BRAIN, zone.getId(),
                severity, java.util.Map.of("incidentId", incident.getId().toString(), "zoneName", zone.getName()), Instant.now()));
        }
    }

    private void autoResolveStaleIncidents() {
        Instant cutoff = Instant.now().minus(IN_PROGRESS_AUTO_RESOLVE_AFTER);
        for (Incident incident : incidentRepository.findByStatusNotOrderByCreatedAtDesc(Incident.Status.RESOLVED)) {
            if (incident.getStatus() == Incident.Status.IN_PROGRESS && incident.getUpdatedAt().isBefore(cutoff)) {
                incident.setStatus(Incident.Status.RESOLVED);
                incidentRepository.save(incident);
                freeRespondingResources(incident);
            }
        }
    }

    private void freeRespondingResources(Incident incident) {
        if (incident.getType() == Incident.Type.FIRE) {
            fireStationRepository.findAll().stream()
                .filter(s -> s.getZone().getId().equals(incident.getZone().getId()) && s.getAvailableUnits() < s.getTotalUnits())
                .findFirst()
                .ifPresent(s -> {
                    s.setAvailableUnits(s.getAvailableUnits() + 1);
                    fireStationRepository.save(s);
                });
        } else {
            ambulanceRepository.findByStatus(Ambulance.Status.DISPATCHED).stream()
                .filter(a -> a.getZone().getId().equals(incident.getZone().getId()))
                .findFirst()
                .ifPresent(a -> {
                    a.setStatus(Ambulance.Status.AVAILABLE);
                    ambulanceRepository.save(a);
                });
        }
    }

    // ------------------------------------------------------------------
    // Public control surface, used by SimulationController
    // ------------------------------------------------------------------

    public void start() {
        running = true;
    }

    public void stop() {
        running = false;
    }

    public void applyControl(Double rainfall, Double traffic, Double population, Double powerDemand, Double emergency) {
        if (rainfall != null) this.rainfallSlider = RiskScoring.clamp(rainfall);
        if (traffic != null) this.trafficSlider = RiskScoring.clamp(traffic);
        if (population != null) this.populationSlider = RiskScoring.clamp(population);
        if (powerDemand != null) this.powerDemandSlider = RiskScoring.clamp(powerDemand);
        if (emergency != null) this.emergencySlider = RiskScoring.clamp(emergency);
    }

    /** Directly force a zone's power supply down (used by the POWER_OUTAGE scenario preset). */
    @Transactional
    public void forceOutage(java.util.UUID zoneId, double remainingSupplyMw) {
        zoneRepository.findById(zoneId).ifPresent(z -> {
            z.setPowerSupplyMw(remainingSupplyMw);
            zoneRepository.save(z);
        });
    }

    @Transactional
    public Zone spawnIncident(java.util.UUID zoneId, Incident.Type type, Severity severity, String description) {
        Zone zone = zoneRepository.findById(zoneId).orElseThrow();
        Incident incident = incidentRepository.save(new Incident(zone, type, severity, description, zone.getCenterLat(), zone.getCenterLng()));
        String eventType = switch (type) {
            case TRAFFIC_ACCIDENT -> EventTypes.ACCIDENT_REPORTED;
            case FIRE -> EventTypes.FIRE_REPORTED;
            case MEDICAL_EMERGENCY -> EventTypes.MEDICAL_EMERGENCY_REPORTED;
            default -> null;
        };
        if (eventType != null) {
            eventBus.publish(new CityEvent(eventType, com.aicitybrain.domain.AgentType.CITY_BRAIN, zoneId, severity,
                java.util.Map.of("incidentId", incident.getId().toString(), "zoneName", zone.getName()), Instant.now()));
        }
        return zone;
    }

    public SimulationDtos.SimulationStateResponse currentState() {
        return new SimulationDtos.SimulationStateResponse(running, rainfallSlider, trafficSlider,
            populationSlider, powerDemandSlider, emergencySlider, lastTick);
    }

    public List<Zone> allZones() {
        return zoneRepository.findAll();
    }
}
