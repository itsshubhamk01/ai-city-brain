package com.aicitybrain.controller;

import com.aicitybrain.domain.Zone;
import com.aicitybrain.dto.CityDtos;
import com.aicitybrain.dto.MapDtos;
import com.aicitybrain.repository.ZoneRepository;
import com.aicitybrain.repository.ZoneStatusSnapshotRepository;
import com.aicitybrain.service.ai.InsightGenerator;
import com.aicitybrain.service.simulation.CityQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/city")
@Tag(name = "City", description = "Live city status, map data and AI briefings")
public class CityController {

    private final CityQueryService cityQueryService;
    private final ZoneRepository zoneRepository;
    private final ZoneStatusSnapshotRepository snapshotRepository;
    private final InsightGenerator insightGenerator;

    public CityController(CityQueryService cityQueryService, ZoneRepository zoneRepository,
                           ZoneStatusSnapshotRepository snapshotRepository, InsightGenerator insightGenerator) {
        this.cityQueryService = cityQueryService;
        this.zoneRepository = zoneRepository;
        this.snapshotRepository = snapshotRepository;
        this.insightGenerator = insightGenerator;
    }

    @GetMapping("/status")
    @Operation(summary = "Current, aggregated city-wide status (command-center headline numbers)")
    public ResponseEntity<CityDtos.CityStatusResponse> status() {
        return ResponseEntity.ok(cityQueryService.buildCityStatus());
    }

    @GetMapping("/map")
    @Operation(summary = "Everything the interactive map layer needs in one call")
    public ResponseEntity<MapDtos.MapDataResponse> map() {
        return ResponseEntity.ok(cityQueryService.buildMapData());
    }

    @GetMapping("/briefing")
    @Operation(summary = "On-demand AI natural-language city briefing (rule-based by default, real LLM if configured)")
    public ResponseEntity<CityDtos.BriefingResponse> briefing() {
        String text = insightGenerator.generateCityBriefing(cityQueryService.buildCityStatus());
        return ResponseEntity.ok(new CityDtos.BriefingResponse(text, Instant.now()));
    }

    @GetMapping("/zones/{zoneId}/history")
    @Operation(summary = "Recent historical snapshots for a zone, for trend charts")
    public ResponseEntity<List<CityDtos.ZoneHistoryPoint>> zoneHistory(@PathVariable UUID zoneId) {
        Zone zone = zoneRepository.findById(zoneId)
            .orElseThrow(() -> new NoSuchElementException("Zone not found: " + zoneId));
        List<CityDtos.ZoneHistoryPoint> points = snapshotRepository.findTop50ByZoneOrderByCreatedAtDesc(zone).stream()
            .map(s -> new CityDtos.ZoneHistoryPoint(s.getCreatedAt(), s.getTrafficLevel(), s.getFloodRiskScore(),
                s.getRiskScore(), s.getAqi(), s.getPowerDemandMw(), s.getPowerSupplyMw()))
            .toList();
        return ResponseEntity.ok(points);
    }
}
