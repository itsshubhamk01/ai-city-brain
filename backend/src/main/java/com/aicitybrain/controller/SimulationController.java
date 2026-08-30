package com.aicitybrain.controller;

import com.aicitybrain.domain.Incident;
import com.aicitybrain.domain.Severity;
import com.aicitybrain.domain.SimulationRun;
import com.aicitybrain.domain.User;
import com.aicitybrain.domain.Zone;
import com.aicitybrain.dto.SimulationDtos;
import com.aicitybrain.exception.ApiException;
import com.aicitybrain.repository.SimulationRunRepository;
import com.aicitybrain.repository.UserRepository;
import com.aicitybrain.repository.ZoneRepository;
import com.aicitybrain.service.simulation.SimulationEngineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * The "Simulation Control" + scenario-preset surface. Every write here is recorded as
 * a {@link SimulationRun} so there is a full audit trail of who changed what, when.
 */
@RestController
@RequestMapping("/api/v1/simulation")
@Tag(name = "Simulation", description = "Live simulation control panel and named scenario presets")
public class SimulationController {

    private final SimulationEngineService engine;
    private final SimulationRunRepository simulationRunRepository;
    private final ZoneRepository zoneRepository;
    private final UserRepository userRepository;

    public SimulationController(SimulationEngineService engine, SimulationRunRepository simulationRunRepository,
                                 ZoneRepository zoneRepository, UserRepository userRepository) {
        this.engine = engine;
        this.simulationRunRepository = simulationRunRepository;
        this.zoneRepository = zoneRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/state")
    @Operation(summary = "Current simulation-control slider values and running status")
    public ResponseEntity<SimulationDtos.SimulationStateResponse> state() {
        return ResponseEntity.ok(engine.currentState());
    }

    @PostMapping("/start")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATIONS_MANAGER')")
    @Operation(summary = "Start (or resume) the live simulation")
    public ResponseEntity<SimulationDtos.SimulationStateResponse> start() {
        engine.start();
        return ResponseEntity.ok(engine.currentState());
    }

    @PostMapping("/stop")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATIONS_MANAGER')")
    @Operation(summary = "Pause the live simulation")
    public ResponseEntity<SimulationDtos.SimulationStateResponse> stop() {
        engine.stop();
        return ResponseEntity.ok(engine.currentState());
    }

    @PatchMapping("/control")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATIONS_MANAGER')")
    @Transactional
    @Operation(summary = "Adjust one or more simulation sliders (rainfall, traffic, population, power demand, emergency level)")
    public ResponseEntity<SimulationDtos.SimulationStateResponse> control(@Valid @RequestBody SimulationDtos.SimulationControlRequest request,
                                                                           Authentication authentication) {
        engine.applyControl(request.rainfall(), request.trafficIntensity(), request.population(),
            request.powerDemand(), request.emergencyLevel());
        recordRun("MANUAL_CONTROL", request.rainfall(), request.trafficIntensity(), request.population(),
            request.powerDemand(), request.emergencyLevel(), authentication);
        return ResponseEntity.ok(engine.currentState());
    }

    @PostMapping("/scenario")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATIONS_MANAGER')")
    @Transactional
    @Operation(summary = "Trigger a named scenario preset: HEAVY_RAIN, MAJOR_ACCIDENT, POWER_OUTAGE, MASS_EMERGENCY, or NORMAL")
    public ResponseEntity<SimulationDtos.SimulationStateResponse> scenario(@Valid @RequestBody SimulationDtos.ScenarioRequest request,
                                                                            Authentication authentication) {
        String key = request.scenarioKey().toUpperCase();
        switch (key) {
            case "HEAVY_RAIN" -> {
                engine.applyControl(88.0, 55.0, null, null, null);
                recordRun(key, 88.0, 55.0, null, null, null, authentication);
            }
            case "MAJOR_ACCIDENT" -> {
                Zone zone = zoneOrFirst("Downtown Core");
                engine.spawnIncident(zone.getId(), Incident.Type.TRAFFIC_ACCIDENT, Severity.HIGH,
                    "Major multi-vehicle accident reported — emergency scenario triggered.");
                engine.applyControl(null, 70.0, null, null, null);
                recordRun(key, null, 70.0, null, null, null, authentication);
            }
            case "POWER_OUTAGE" -> {
                Zone zone = zoneOrFirst("Industrial Park");
                engine.forceOutage(zone.getId(), 8.0);
                recordRun(key, null, null, null, 90.0, null, authentication);
            }
            case "MASS_EMERGENCY" -> {
                Zone zoneA = zoneOrFirst("Downtown Core");
                Zone zoneB = zoneOrFirst("Green Meadows");
                engine.spawnIncident(zoneA.getId(), Incident.Type.MEDICAL_EMERGENCY, Severity.HIGH, "Mass casualty event reported.");
                engine.spawnIncident(zoneB.getId(), Incident.Type.MEDICAL_EMERGENCY, Severity.HIGH, "Mass casualty event reported.");
                engine.applyControl(null, null, null, null, 90.0);
                recordRun(key, null, null, null, null, 90.0, authentication);
            }
            case "NORMAL" -> {
                engine.applyControl(20.0, 35.0, 50.0, 40.0, 10.0);
                recordRun(key, 20.0, 35.0, 50.0, 40.0, 10.0, authentication);
            }
            default -> throw ApiException.badRequest("Unknown scenario key: " + request.scenarioKey());
        }
        return ResponseEntity.ok(engine.currentState());
    }

    private Zone zoneOrFirst(String preferredName) {
        return zoneRepository.findByNameIgnoreCase(preferredName)
            .orElseGet(() -> zoneRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new java.util.NoSuchElementException("No zones seeded")));
    }

    private void recordRun(String key, Double rainfall, Double traffic, Double population, Double powerDemand,
                            Double emergency, Authentication authentication) {
        // A new control/scenario change supersedes whatever was previously "in effect".
        List<SimulationRun> running = simulationRunRepository.findTop20ByOrderByCreatedAtDesc().stream()
            .filter(r -> r.getStatus() == SimulationRun.Status.RUNNING).toList();
        running.forEach(r -> {
            r.complete();
            simulationRunRepository.save(r);
        });

        User user = authentication == null ? null : userRepository.findByUsername(authentication.getName()).orElse(null);
        simulationRunRepository.save(new SimulationRun(key, rainfall, traffic, population, powerDemand, emergency, user));
    }
}
