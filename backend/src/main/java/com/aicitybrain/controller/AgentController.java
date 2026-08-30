package com.aicitybrain.controller;

import com.aicitybrain.dto.AgentDtos;
import com.aicitybrain.repository.AgentActionRepository;
import com.aicitybrain.repository.AgentEventRepository;
import com.aicitybrain.service.simulation.CityQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/agents")
@Tag(name = "Agents", description = "The live 'AI Decisions' feed — what every agent has observed and done")
public class AgentController {

    private final AgentEventRepository agentEventRepository;
    private final AgentActionRepository agentActionRepository;
    private final CityQueryService cityQueryService;

    public AgentController(AgentEventRepository agentEventRepository, AgentActionRepository agentActionRepository,
                            CityQueryService cityQueryService) {
        this.agentEventRepository = agentEventRepository;
        this.agentActionRepository = agentActionRepository;
        this.cityQueryService = cityQueryService;
    }

    @GetMapping("/decisions")
    @Operation(summary = "Merged, time-ordered feed of every agent event + action")
    public ResponseEntity<List<AgentDtos.DecisionFeedItem>> decisions() {
        List<AgentDtos.DecisionFeedItem> feed = cityQueryService.buildRecentDecisionFeed(
            agentEventRepository.findTop50ByOrderByCreatedAtDesc(),
            agentActionRepository.findTop50ByOrderByCreatedAtDesc()
        );
        return ResponseEntity.ok(feed.stream().limit(50).toList());
    }
}
