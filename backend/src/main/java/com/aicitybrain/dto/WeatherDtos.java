package com.aicitybrain.dto;

import java.time.Instant;
import java.util.List;

/**
 * Weather DTOs backed by Open-Meteo (free, no API key, non-commercial use).
 * {@code available=false} means the UI must show "Live weather data unavailable
 * for this location" rather than any fabricated numbers — see WeatherService.
 */
public final class WeatherDtos {
    private WeatherDtos() {}

    public record CurrentConditions(
        Double temperatureC,
        Double feelsLikeC,
        Integer humidityPct,
        Double windSpeedKmh,
        Integer windDirectionDeg,
        Double pressureHpa,
        Double precipitationMm,
        String condition,
        Boolean isDay
    ) {}

    public record DailyForecastDay(
        String date,
        Double tempMinC,
        Double tempMaxC,
        String condition,
        String sunrise,
        String sunset,
        Integer uvIndexMax,
        Integer precipitationProbabilityMaxPct
    ) {}

    public record HourlyForecastPoint(
        String time,
        Double temperatureC,
        Integer precipitationProbabilityPct,
        String condition
    ) {}

    public record WeatherResponse(
        double lat,
        double lng,
        boolean available,
        String unavailableReason,
        CurrentConditions current,
        List<DailyForecastDay> daily,
        List<HourlyForecastPoint> hourly,
        String source,
        Instant fetchedAt
    ) {}
}
