package com.aicitybrain.controller;

import com.aicitybrain.dto.InfrastructureDtos;
import com.aicitybrain.service.geo.MumbaiInfrastructureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/mumbai")
@Tag(name = "Mumbai", description = "Real Mumbai-specific data — hospitals, police, and fire stations from OpenStreetMap")
public class MumbaiController {

    private final MumbaiInfrastructureService infrastructureService;

    public MumbaiController(MumbaiInfrastructureService infrastructureService) {
        this.infrastructureService = infrastructureService;
    }

    @GetMapping("/infrastructure")
    @Operation(summary = "Real hospitals, police stations, and fire stations across Greater Mumbai (OpenStreetMap)")
    public ResponseEntity<InfrastructureDtos.InfrastructureResponse> infrastructure() {
        return ResponseEntity.ok(infrastructureService.getInfrastructure());
    }
}
