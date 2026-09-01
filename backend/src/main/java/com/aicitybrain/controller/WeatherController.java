package com.aicitybrain.controller;

import com.aicitybrain.dto.WeatherDtos;
import com.aicitybrain.service.geo.WeatherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Real, live weather for any coordinate — backed by Open-Meteo (free, no key).
 * Public endpoint, same reasoning as {@link GeoController}.
 */
@RestController
@RequestMapping("/api/v1/weather")
@Tag(name = "Weather", description = "Real live weather (Open-Meteo) — returns available=false rather than fake data on failure")
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping
    @Operation(summary = "Current conditions + hourly/daily forecast for a coordinate")
    public ResponseEntity<WeatherDtos.WeatherResponse> weather(@RequestParam double lat, @RequestParam double lng) {
        return ResponseEntity.ok(weatherService.fetchWeather(lat, lng));
    }
}
