package com.aicitybrain.controller;

import com.aicitybrain.domain.Incident;
import com.aicitybrain.domain.Severity;
import com.aicitybrain.domain.User;
import com.aicitybrain.domain.Zone;
import com.aicitybrain.dto.IncidentDtos;
import com.aicitybrain.exception.ApiException;
import com.aicitybrain.repository.IncidentRepository;
import com.aicitybrain.repository.UserRepository;
import com.aicitybrain.repository.ZoneRepository;
import com.aicitybrain.service.events.CityEvent;
import com.aicitybrain.service.events.EventBus;
import com.aicitybrain.service.events.EventTypes;
import com.aicitybrain.service.simulation.CityQueryService;
import com.aicitybrain.websocket.WebSocketBroadcaster;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/incidents")
@Tag(name = "Incidents", description = "City incidents — traffic accidents, fires, floods, medical emergencies, outages")
public class IncidentController {

    private final IncidentRepository incidentRepository;
    private final ZoneRepository zoneRepository;
    private final UserRepository userRepository;
    private final CityQueryService cityQueryService;
    private final EventBus eventBus;
    private final WebSocketBroadcaster broadcaster;

    public IncidentController(IncidentRepository incidentRepository, ZoneRepository zoneRepository,
                               UserRepository userRepository, CityQueryService cityQueryService,
                               EventBus eventBus, WebSocketBroadcaster broadcaster) {
        this.incidentRepository = incidentRepository;
        this.zoneRepository = zoneRepository;
        this.userRepository = userRepository;
        this.cityQueryService = cityQueryService;
        this.eventBus = eventBus;
        this.broadcaster = broadcaster;
    }

    @GetMapping
    @Operation(summary = "List active incidents, prioritized by severity then age")
    public ResponseEntity<List<IncidentDtos.IncidentResponse>> list() {
        List<Incident> incidents = incidentRepository.findByStatusNotOrderByCreatedAtDesc(Incident.Status.RESOLVED);
        List<IncidentDtos.IncidentResponse> prioritized = incidents.stream()
            .sorted(Comparator.<Incident>comparingInt(i -> -i.getSeverity().weight())
                .thenComparing(Incident::getCreatedAt))
            .map(cityQueryService::toIncidentResponse)
            .toList();
        return ResponseEntity.ok(prioritized);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','OPERATIONS_MANAGER','EMERGENCY_RESPONDER','TRAFFIC_MANAGER')")
    @Transactional
    @Operation(summary = "Manually report a new incident")
    public ResponseEntity<IncidentDtos.IncidentResponse> create(@Valid @RequestBody IncidentDtos.IncidentCreateRequest request,
                                                                 Authentication authentication) {
        Zone zone = zoneRepository.findById(request.zoneId())
            .orElseThrow(() -> new NoSuchElementException("Zone not found: " + request.zoneId()));

        Incident.Type type = parseEnum(Incident.Type.class, request.type(), "type");
        Severity severity = parseEnum(Severity.class, request.severity(), "severity");

        Incident incident = new Incident(zone, type, severity, request.description(), request.lat(), request.lng());
        User reporter = userRepository.findByUsername(authentication.getName()).orElse(null);
        incident.setReportedBy(reporter);
        incident = incidentRepository.save(incident);

        IncidentDtos.IncidentResponse response = cityQueryService.toIncidentResponse(incident);
        broadcaster.send("incident", response);

        String eventType = switch (type) {
            case TRAFFIC_ACCIDENT -> EventTypes.ACCIDENT_REPORTED;
            case FIRE -> EventTypes.FIRE_REPORTED;
            case MEDICAL_EMERGENCY -> EventTypes.MEDICAL_EMERGENCY_REPORTED;
            default -> null;
        };
        if (eventType != null) {
            eventBus.publish(new CityEvent(eventType, com.aicitybrain.domain.AgentType.CITY_BRAIN, zone.getId(),
                severity, Map.of("incidentId", incident.getId().toString(), "zoneName", zone.getName()), Instant.now()));
        }

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATIONS_MANAGER','EMERGENCY_RESPONDER','TRAFFIC_MANAGER')")
    @Transactional
    @Operation(summary = "Update an incident's status (acknowledge / in progress / resolve)")
    public ResponseEntity<IncidentDtos.IncidentResponse> updateStatus(@PathVariable UUID id,
                                                                       @Valid @RequestBody IncidentDtos.IncidentStatusUpdateRequest request) {
        Incident incident = incidentRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Incident not found: " + id));
        Incident.Status status = parseEnum(Incident.Status.class, request.status(), "status");
        incident.setStatus(status);
        incident = incidentRepository.save(incident);

        IncidentDtos.IncidentResponse response = cityQueryService.toIncidentResponse(incident);
        broadcaster.send("incident", response);
        return ResponseEntity.ok(response);
    }

    private <E extends Enum<E>> E parseEnum(Class<E> enumClass, String value, String fieldName) {
        try {
            return Enum.valueOf(enumClass, value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw ApiException.badRequest("Invalid " + fieldName + ": " + value);
        }
    }
}
