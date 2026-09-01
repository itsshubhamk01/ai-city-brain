package com.aicitybrain.service.geo;

import com.aicitybrain.dto.WeatherDtos;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Real weather via <a href="https://open-meteo.com">Open-Meteo</a> — free, no API key,
 * no signup, no card, global coverage including every Indian location. On ANY failure
 * (network error, timeout, malformed response) this returns {@code available=false}
 * with a human-readable reason instead of ever fabricating a number, per the
 * "never invent values" requirement.
 */
@Service
public class WeatherService {

    private static final Logger log = LoggerFactory.getLogger(WeatherService.class);
    private static final String BASE_URL = "https://api.open-meteo.com/v1/forecast";
    private static final Duration CACHE_TTL = Duration.ofMinutes(10);

    private final HttpClient httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .build();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public WeatherDtos.WeatherResponse fetchWeather(double lat, double lng) {
        String key = "%.2f,%.2f".formatted(lat, lng);
        CacheEntry cached = cache.get(key);
        if (cached != null && cached.expiresAt.isAfter(Instant.now())) {
            return cached.response;
        }

        WeatherDtos.WeatherResponse response = doFetch(lat, lng);
        if (response.available()) {
            cache.put(key, new CacheEntry(response, Instant.now().plus(CACHE_TTL)));
        }
        return response;
    }

    private WeatherDtos.WeatherResponse doFetch(double lat, double lng) {
        try {
            String url = BASE_URL
                + "?latitude=" + lat + "&longitude=" + lng
                + "&current=temperature_2m,apparent_temperature,relative_humidity_2m,wind_speed_10m,"
                + "wind_direction_10m,surface_pressure,precipitation,weather_code,is_day"
                + "&hourly=temperature_2m,precipitation_probability,weather_code"
                + "&daily=weather_code,temperature_2m_max,temperature_2m_min,sunrise,sunset,"
                + "uv_index_max,precipitation_probability_max"
                + "&forecast_days=7&timezone=auto";

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(8))
                .GET()
                .build();

            HttpResponse<String> httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (httpResponse.statusCode() != 200) {
                return unavailable(lat, lng, "Weather provider returned HTTP " + httpResponse.statusCode());
            }

            JsonNode root = objectMapper.readTree(httpResponse.body());
            return parse(lat, lng, root);
        } catch (Exception e) {
            log.warn("Weather fetch failed for ({}, {}): {}", lat, lng, e.getMessage());
            return unavailable(lat, lng, "Could not reach the weather provider — please try again shortly.");
        }
    }

    private WeatherDtos.WeatherResponse parse(double lat, double lng, JsonNode root) {
        JsonNode current = root.path("current");
        WeatherDtos.CurrentConditions currentConditions = new WeatherDtos.CurrentConditions(
            numOrNull(current, "temperature_2m"),
            numOrNull(current, "apparent_temperature"),
            intOrNull(current, "relative_humidity_2m"),
            numOrNull(current, "wind_speed_10m"),
            intOrNull(current, "wind_direction_10m"),
            numOrNull(current, "surface_pressure"),
            numOrNull(current, "precipitation"),
            conditionOf(intOrNull(current, "weather_code")),
            current.path("is_day").isMissingNode() ? null : current.path("is_day").asInt() == 1
        );

        List<WeatherDtos.DailyForecastDay> daily = new ArrayList<>();
        JsonNode dailyNode = root.path("daily");
        JsonNode dailyDates = dailyNode.path("time");
        for (int i = 0; i < dailyDates.size(); i++) {
            daily.add(new WeatherDtos.DailyForecastDay(
                dailyDates.get(i).asText(),
                numOrNull(dailyNode.path("temperature_2m_min"), i),
                numOrNull(dailyNode.path("temperature_2m_max"), i),
                conditionOf(intOrNull(dailyNode.path("weather_code"), i)),
                textOrNull(dailyNode.path("sunrise"), i),
                textOrNull(dailyNode.path("sunset"), i),
                intOrNull(dailyNode.path("uv_index_max"), i),
                intOrNull(dailyNode.path("precipitation_probability_max"), i)
            ));
        }

        List<WeatherDtos.HourlyForecastPoint> hourly = new ArrayList<>();
        JsonNode hourlyNode = root.path("hourly");
        JsonNode hourlyTimes = hourlyNode.path("time");
        // Only the next 24 hours — the raw response includes the full 7-day hourly series.
        int hourlyLimit = Math.min(24, hourlyTimes.size());
        for (int i = 0; i < hourlyLimit; i++) {
            hourly.add(new WeatherDtos.HourlyForecastPoint(
                hourlyTimes.get(i).asText(),
                numOrNull(hourlyNode.path("temperature_2m"), i),
                intOrNull(hourlyNode.path("precipitation_probability"), i),
                conditionOf(intOrNull(hourlyNode.path("weather_code"), i))
            ));
        }

        return new WeatherDtos.WeatherResponse(lat, lng, true, null, currentConditions, daily, hourly,
            "Open-Meteo", Instant.now());
    }

    private WeatherDtos.WeatherResponse unavailable(double lat, double lng, String reason) {
        return new WeatherDtos.WeatherResponse(lat, lng, false, reason, null, List.of(), List.of(), "Open-Meteo", Instant.now());
    }

    private Double numOrNull(JsonNode parent, String field) {
        JsonNode node = parent.path(field);
        return node.isMissingNode() || node.isNull() ? null : node.asDouble();
    }

    private Double numOrNull(JsonNode arrayNode, int index) {
        if (index >= arrayNode.size()) return null;
        JsonNode node = arrayNode.get(index);
        return node == null || node.isNull() ? null : node.asDouble();
    }

    private Integer intOrNull(JsonNode parent, String field) {
        JsonNode node = parent.path(field);
        return node.isMissingNode() || node.isNull() ? null : node.asInt();
    }

    private Integer intOrNull(JsonNode arrayNode, int index) {
        if (index >= arrayNode.size()) return null;
        JsonNode node = arrayNode.get(index);
        return node == null || node.isNull() ? null : node.asInt();
    }

    private String textOrNull(JsonNode arrayNode, int index) {
        if (index >= arrayNode.size()) return null;
        JsonNode node = arrayNode.get(index);
        return node == null || node.isNull() ? null : node.asText();
    }

    /** WMO weather interpretation codes — the same standard table Open-Meteo, and most other providers, use. */
    private String conditionOf(Integer code) {
        if (code == null) return null;
        return switch (code) {
            case 0 -> "Clear sky";
            case 1 -> "Mainly clear";
            case 2 -> "Partly cloudy";
            case 3 -> "Overcast";
            case 45, 48 -> "Fog";
            case 51, 53, 55 -> "Drizzle";
            case 56, 57 -> "Freezing drizzle";
            case 61, 63, 65 -> "Rain";
            case 66, 67 -> "Freezing rain";
            case 71, 73, 75, 77 -> "Snow";
            case 80, 81, 82 -> "Rain showers";
            case 85, 86 -> "Snow showers";
            case 95 -> "Thunderstorm";
            case 96, 99 -> "Thunderstorm with hail";
            default -> "Unknown (code " + code + ")";
        };
    }

    private record CacheEntry(WeatherDtos.WeatherResponse response, Instant expiresAt) {}
}
