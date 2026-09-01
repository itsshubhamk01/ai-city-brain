package com.aicitybrain.controller;

import com.aicitybrain.dto.GeoDtos;
import com.aicitybrain.service.geo.GeocodingService;
import com.aicitybrain.service.geo.IndianStates;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Real location data — India's states/UTs (static), free-text search, and reverse
 * geocoding, both backed by OpenStreetMap's Nominatim. Public (no login required) so
 * the landing page can let visitors explore before creating an account.
 */
@RestController
@RequestMapping("/api/v1/geo")
@Validated
@Tag(name = "Geo", description = "Real location search and reverse geocoding (OpenStreetMap/Nominatim) — no fake coordinates")
public class GeoController {

    private final GeocodingService geocodingService;

    public GeoController(GeocodingService geocodingService) {
        this.geocodingService = geocodingService;
    }

    @GetMapping("/states")
    @Operation(summary = "India's 28 states + 8 union territories")
    public ResponseEntity<List<GeoDtos.IndianState>> states() {
        return ResponseEntity.ok(IndianStates.ALL);
    }

    @GetMapping("/search")
    @Operation(summary = "Search for a place in India by name (city, town, landmark, address)")
    public ResponseEntity<GeoDtos.SearchResponse> search(@RequestParam @NotBlank String q) {
        return ResponseEntity.ok(geocodingService.search(q));
    }

    @GetMapping("/reverse")
    @Operation(summary = "Turn coordinates into a human place name (city/district/state)")
    public ResponseEntity<GeoDtos.ReverseGeocodeResponse> reverse(@RequestParam double lat, @RequestParam double lng) {
        return ResponseEntity.ok(geocodingService.reverse(lat, lng));
    }
}
