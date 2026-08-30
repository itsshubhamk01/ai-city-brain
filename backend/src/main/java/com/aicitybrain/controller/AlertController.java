package com.aicitybrain.controller;

import com.aicitybrain.domain.Alert;
import com.aicitybrain.dto.AlertDtos;
import com.aicitybrain.repository.AlertRepository;
import com.aicitybrain.service.simulation.CityQueryService;
import com.aicitybrain.websocket.WebSocketBroadcaster;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/alerts")
@Tag(name = "Alerts", description = "Citizen/operator-facing alerts raised by the CityBrain")
public class AlertController {

    private final AlertRepository alertRepository;
    private final CityQueryService cityQueryService;
    private final WebSocketBroadcaster broadcaster;

    public AlertController(AlertRepository alertRepository, CityQueryService cityQueryService, WebSocketBroadcaster broadcaster) {
        this.alertRepository = alertRepository;
        this.cityQueryService = cityQueryService;
        this.broadcaster = broadcaster;
    }

    @GetMapping
    @Operation(summary = "Recent alerts, most recent first")
    public ResponseEntity<List<AlertDtos.AlertResponse>> list() {
        return ResponseEntity.ok(alertRepository.findTop50ByOrderByCreatedAtDesc().stream()
            .map(cityQueryService::toAlertResponse).toList());
    }

    @PatchMapping("/{id}/acknowledge")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATIONS_MANAGER','EMERGENCY_RESPONDER','TRAFFIC_MANAGER')")
    @Transactional
    @Operation(summary = "Acknowledge an alert")
    public ResponseEntity<AlertDtos.AlertResponse> acknowledge(@PathVariable UUID id) {
        Alert alert = alertRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Alert not found: " + id));
        alert.setAcknowledged(true);
        alert = alertRepository.save(alert);
        AlertDtos.AlertResponse response = cityQueryService.toAlertResponse(alert);
        broadcaster.send("alert-acknowledged", response);
        return ResponseEntity.ok(response);
    }
}
