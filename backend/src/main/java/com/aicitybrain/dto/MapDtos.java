package com.aicitybrain.dto;

import java.util.List;
import java.util.UUID;

public final class MapDtos {
    private MapDtos() {}

    public record RoadDto(UUID id, String name, double startLat, double startLng, double endLat, double endLng,
                           String status, double congestionPct) {}

    public record HospitalDto(UUID id, String name, double lat, double lng, int totalBeds, int occupiedBeds,
                               double occupancyPct) {}

    public record AmbulanceDto(UUID id, String code, double lat, double lng, String status) {}

    public record FireStationDto(UUID id, String name, double lat, double lng, int totalUnits, int availableUnits) {}

    public record WasteBinDto(UUID id, String code, double lat, double lng, double capacityPct) {}

    public record PowerStationDto(UUID id, String name, double lat, double lng, double capacityMw, double currentLoadMw) {}

    public record WaterStationDto(UUID id, String name, double lat, double lng, double reservoirLevelPct) {}

    public record MapDataResponse(
        List<CityDtos.ZoneStatusResponse> zones,
        List<RoadDto> roads,
        List<HospitalDto> hospitals,
        List<AmbulanceDto> ambulances,
        List<FireStationDto> fireStations,
        List<WasteBinDto> wasteBins,
        List<PowerStationDto> powerStations,
        List<WaterStationDto> waterStations,
        List<IncidentDtos.IncidentResponse> incidents
    ) {}
}
