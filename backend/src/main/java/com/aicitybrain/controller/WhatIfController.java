package com.aicitybrain.controller;

import com.aicitybrain.dto.WhatIfDtos;
import com.aicitybrain.service.simulation.WhatIfSimulatorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/whatif")
@Tag(name = "What-If", description = "Hypothetical scenario projection — never mutates live city state")
public class WhatIfController {

    private final WhatIfSimulatorService whatIfSimulatorService;

    public WhatIfController(WhatIfSimulatorService whatIfSimulatorService) {
        this.whatIfSimulatorService = whatIfSimulatorService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','OPERATIONS_MANAGER','ANALYST','TRAFFIC_MANAGER','EMERGENCY_RESPONDER')")
    @Operation(summary = "Ask 'what if...?' and get a projected impact + AI-generated recommendation")
    public ResponseEntity<WhatIfDtos.WhatIfResponse> evaluate(@RequestBody WhatIfDtos.WhatIfRequest request) {
        return ResponseEntity.ok(whatIfSimulatorService.evaluate(request));
    }
}
