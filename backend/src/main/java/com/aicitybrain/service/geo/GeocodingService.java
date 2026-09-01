package com.aicitybrain.service.geo;

import com.aicitybrain.dto.GeoDtos;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Real geocoding via OpenStreetMap's public <a href="https://nominatim.org">Nominatim</a>
 * service — free, no API key. Their usage policy requires: (1) a descriptive
 * User-Agent identifying the application, and (2) no more than one request per
 * second. Both are enforced here so this app is a good citizen of a free public
 * service, not just "free until they block us".
 */
@Service
public class GeocodingService {

    private static final Logger log = LoggerFactory.getLogger(GeocodingService.class);
    private static final String BASE_URL = "https://nominatim.openstreetmap.org";
    private static final String USER_AGENT = "AICityBrain/1.0 (student smart-city portfolio project)";
    private static final Duration MIN_INTERVAL = Duration.ofMillis(1100);
    private static final Duration CACHE_TTL = Duration.ofHours(6);

    private final HttpClient httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .build();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, Object> cache = new ConcurrentHashMap<>();
    private final Map<String, Instant> cacheExpiry = new ConcurrentHashMap<>();

    private volatile Instant lastRequestAt = Instant.EPOCH;
    private final Object rateLimitLock = new Object();

    public GeoDtos.SearchResponse search(String query) {
        String cacheKey = "search:" + query.toLowerCase();
        GeoDtos.SearchResponse cached = getCached(cacheKey);
        if (cached != null) return cached;

        try {
            throttle();
            String url = BASE_URL + "/search?format=jsonv2&addressdetails=1&limit=8&countrycodes=in&q="
                + URLEncoder.encode(query, StandardCharsets.UTF_8);
            JsonNode results = fetch(url);

            List<GeoDtos.LocationSuggestion> suggestions = new ArrayList<>();
            if (results.isArray()) {
                for (JsonNode item : results) {
                    suggestions.add(toSuggestion(item));
                }
            }
            GeoDtos.SearchResponse response = new GeoDtos.SearchResponse(query, suggestions, true);
            putCache(cacheKey, response);
            return response;
        } catch (Exception e) {
            log.warn("Geocoding search failed for '{}': {}", query, e.getMessage());
            return new GeoDtos.SearchResponse(query, List.of(), false);
        }
    }

    public GeoDtos.ReverseGeocodeResponse reverse(double lat, double lng) {
        String cacheKey = "reverse:%.4f,%.4f".formatted(lat, lng);
        GeoDtos.ReverseGeocodeResponse cached = getCached(cacheKey);
        if (cached != null) return cached;

        try {
            throttle();
            String url = BASE_URL + "/reverse?format=jsonv2&addressdetails=1&zoom=12&lat=" + lat + "&lon=" + lng;
            JsonNode result = fetch(url);

            if (result.has("error")) {
                return unavailableReverse(lat, lng);
            }

            JsonNode address = result.path("address");
            String city = firstNonBlank(address, "city", "town", "village", "county");
            String district = firstNonBlank(address, "state_district", "county");
            String state = textOrNull(address, "state");

            GeoDtos.ReverseGeocodeResponse response = new GeoDtos.ReverseGeocodeResponse(
                lat, lng,
                textOrNull(result, "display_name"),
                city, district, state,
                textOrNull(address, "country"),
                true
            );
            putCache(cacheKey, response);
            return response;
        } catch (Exception e) {
            log.warn("Reverse geocoding failed for ({}, {}): {}", lat, lng, e.getMessage());
            return unavailableReverse(lat, lng);
        }
    }

    /** Enforces "no more than 1 request/second" across every call this backend makes to Nominatim. */
    private void throttle() throws InterruptedException {
        synchronized (rateLimitLock) {
            Duration sinceLast = Duration.between(lastRequestAt, Instant.now());
            if (sinceLast.compareTo(MIN_INTERVAL) < 0) {
                Thread.sleep(MIN_INTERVAL.minus(sinceLast).toMillis());
            }
            lastRequestAt = Instant.now();
        }
    }

    private JsonNode fetch(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("User-Agent", USER_AGENT)
            .timeout(Duration.ofSeconds(8))
            .GET()
            .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IllegalStateException("Nominatim returned HTTP " + response.statusCode());
        }
        return objectMapper.readTree(response.body());
    }

    private GeoDtos.LocationSuggestion toSuggestion(JsonNode item) {
        JsonNode address = item.path("address");
        return new GeoDtos.LocationSuggestion(
            textOrNull(item, "display_name"),
            item.path("lat").asDouble(),
            item.path("lon").asDouble(),
            firstNonBlank(address, "city", "town", "village"),
            textOrNull(address, "state"),
            textOrNull(address, "country"),
            textOrNull(item, "type")
        );
    }

    private GeoDtos.ReverseGeocodeResponse unavailableReverse(double lat, double lng) {
        return new GeoDtos.ReverseGeocodeResponse(lat, lng, null, null, null, null, null, false);
    }

    @SuppressWarnings("unchecked")
    private <T> T getCached(String key) {
        Instant expiry = cacheExpiry.get(key);
        if (expiry == null || expiry.isBefore(Instant.now())) {
            cache.remove(key);
            cacheExpiry.remove(key);
            return null;
        }
        return (T) cache.get(key);
    }

    private void putCache(String key, Object value) {
        cache.put(key, value);
        cacheExpiry.put(key, Instant.now().plus(CACHE_TTL));
    }

    private String textOrNull(JsonNode parent, String field) {
        JsonNode node = parent.path(field);
        return node.isMissingNode() || node.isNull() ? null : node.asText();
    }

    private String firstNonBlank(JsonNode parent, String... fields) {
        for (String field : fields) {
            String value = textOrNull(parent, field);
            if (value != null && !value.isBlank()) return value;
        }
        return null;
    }
}
