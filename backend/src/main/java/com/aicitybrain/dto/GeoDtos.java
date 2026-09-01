package com.aicitybrain.dto;

import java.util.List;

/**
 * Location/geocoding DTOs. Everything here is backed by real, free OpenStreetMap data
 * (Nominatim) — no fake coordinates or invented place names.
 */
public final class GeoDtos {
    private GeoDtos() {}

    /** One candidate result from a location search (forward geocoding). */
    public record LocationSuggestion(
        String displayName,
        double lat,
        double lng,
        String city,
        String state,
        String country,
        String type
    ) {}

    public record SearchResponse(String query, List<LocationSuggestion> results, boolean available) {}

    /** Result of turning a lat/lng into a human place name (reverse geocoding). */
    public record ReverseGeocodeResponse(
        double lat,
        double lng,
        String displayName,
        String city,
        String district,
        String state,
        String country,
        boolean available
    ) {}

    public record IndianState(String name, String type) {}
}
