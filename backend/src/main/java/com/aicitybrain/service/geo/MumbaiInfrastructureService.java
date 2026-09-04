package com.aicitybrain.service.geo;

import com.aicitybrain.dto.InfrastructureDtos;
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

/**
 * Real hospitals, police stations, and fire stations across Greater Mumbai, sourced
 * from OpenStreetMap's free, public Overpass API. This is genuinely open data — real
 * facilities that real people have mapped — not a synthetic list.
 *
 * <p>Overpass's public instance has a modest daily query budget, and this query
 * covers the whole city rather than a single user, so results are cached for 24
 * hours: at most one real network call per day serves every visitor, regardless of
 * how many people use the app.</p>
 */
@Service
public class MumbaiInfrastructureService {

    private static final Logger log = LoggerFactory.getLogger(MumbaiInfrastructureService.class);
    private static final String OVERPASS_URL = "https://overpass-api.de/api/interpreter";
    private static final Duration CACHE_TTL = Duration.ofHours(24);

    // Greater Mumbai bounding box: south, west, north, east
    private static final String BBOX = "18.89,72.75,19.30,73.05";

    private final HttpClient httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(8))
        .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private volatile InfrastructureDtos.InfrastructureResponse cached;
    private volatile Instant cachedAt = Instant.EPOCH;

    public synchronized InfrastructureDtos.InfrastructureResponse getInfrastructure() {
        if (cached != null && Duration.between(cachedAt, Instant.now()).compareTo(CACHE_TTL) < 0) {
            return cached;
        }
        InfrastructureDtos.InfrastructureResponse fresh = fetch();
        if (fresh.available()) {
            cached = fresh;
            cachedAt = Instant.now();
        }
        // If the fetch failed but we still have a (stale) previous successful result, prefer
        // serving that over an empty "unavailable" map — it's still real data, just not brand new.
        return fresh.available() || cached == null ? fresh : cached;
    }

    private InfrastructureDtos.InfrastructureResponse fetch() {
        try {
            String query = """
                [out:json][timeout:25];
                (
                  node["amenity"="hospital"](%s);
                  way["amenity"="hospital"](%s);
                  node["amenity"="police"](%s);
                  way["amenity"="police"](%s);
                  node["amenity"="fire_station"](%s);
                  way["amenity"="fire_station"](%s);
                );
                out center tags;
                """.formatted(BBOX, BBOX, BBOX, BBOX, BBOX, BBOX);

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(OVERPASS_URL))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString("data=" + URLEncoder.encode(query, StandardCharsets.UTF_8)))
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                return unavailable("The map data provider returned HTTP " + response.statusCode());
            }

            return parse(objectMapper.readTree(response.body()));
        } catch (Exception e) {
            log.warn("Mumbai infrastructure fetch failed: {}", e.getMessage());
            return unavailable("Could not reach the map data provider — please try again shortly.");
        }
    }

    private InfrastructureDtos.InfrastructureResponse parse(JsonNode root) {
        List<InfrastructureDtos.InfrastructurePoi> hospitals = new ArrayList<>();
        List<InfrastructureDtos.InfrastructurePoi> police = new ArrayList<>();
        List<InfrastructureDtos.InfrastructurePoi> fire = new ArrayList<>();

        for (JsonNode el : root.path("elements")) {
            double lat = el.has("lat") ? el.get("lat").asDouble() : el.path("center").path("lat").asDouble(Double.NaN);
            double lng = el.has("lon") ? el.get("lon").asDouble() : el.path("center").path("lon").asDouble(Double.NaN);
            if (Double.isNaN(lat) || Double.isNaN(lng)) continue;

            JsonNode tags = el.path("tags");
            String amenity = tags.path("amenity").asText("");
            String name = tags.path("name").asText(null);
            String id = el.path("type").asText("") + "/" + el.path("id").asLong();

            switch (amenity) {
                case "hospital" -> hospitals.add(new InfrastructureDtos.InfrastructurePoi(id, "HOSPITAL", name != null ? name : "Hospital", lat, lng));
                case "police" -> police.add(new InfrastructureDtos.InfrastructurePoi(id, "POLICE", name != null ? name : "Police Station", lat, lng));
                case "fire_station" -> fire.add(new InfrastructureDtos.InfrastructurePoi(id, "FIRE_STATION", name != null ? name : "Fire Station", lat, lng));
                default -> { /* ignore anything outside the three amenity types we queried */ }
            }
        }

        return new InfrastructureDtos.InfrastructureResponse(true, null, Instant.now(), hospitals, police, fire);
    }

    private InfrastructureDtos.InfrastructureResponse unavailable(String reason) {
        return new InfrastructureDtos.InfrastructureResponse(false, reason, Instant.now(), List.of(), List.of(), List.of());
    }
}
