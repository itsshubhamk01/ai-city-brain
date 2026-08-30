package com.aicitybrain.service.agents;

import com.aicitybrain.domain.AgentType;
import com.aicitybrain.domain.Severity;
import com.aicitybrain.domain.WasteBin;
import com.aicitybrain.repository.AgentActionRepository;
import com.aicitybrain.repository.AgentEventRepository;
import com.aicitybrain.repository.WasteBinRepository;
import com.aicitybrain.service.events.EventBus;
import com.aicitybrain.service.events.EventTypes;
import com.aicitybrain.service.simulation.RiskScoring;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Monitors waste-bin fill levels and "dispatches" a collection truck once a bin
 * crosses the full threshold — a small, satisfying closed loop: the bin fills up over
 * several ticks, the agent notices, takes action, and the bin visibly empties again.
 */
@Component
public class WasteAgent extends AbstractAgent {

    private final WasteBinRepository wasteBinRepository;

    public WasteAgent(EventBus eventBus, AgentEventRepository agentEventRepository,
                       AgentActionRepository agentActionRepository, WasteBinRepository wasteBinRepository) {
        super(eventBus, agentEventRepository, agentActionRepository);
        this.wasteBinRepository = wasteBinRepository;
    }

    @Override
    public AgentType type() {
        return AgentType.WASTE;
    }

    @Override
    @Transactional
    public void evaluate() {
        for (WasteBin bin : wasteBinRepository.findByCapacityPctGreaterThanEqual(RiskScoring.WASTE_FULL_THRESHOLD)) {
            double before = bin.getCapacityPct();
            bin.setCapacityPct(ThreadLocalRandom.current().nextDouble(5, 15));
            wasteBinRepository.save(bin);

            String summary = "Bin %s in %s reached %.0f%% capacity → collection truck dispatched."
                .formatted(bin.getCode(), bin.getZone().getName(), before);
            logEvent(EventTypes.WASTE_BIN_FULL, Severity.MODERATE, bin.getZone(), summary);
            logAction("WASTE_COLLECTION_DISPATCH",
                "Collection truck dispatched to bin " + bin.getCode() + " in " + bin.getZone().getName()
                    + " (was " + Math.round(before) + "% full).", bin.getZone(), null);
            publish(EventTypes.WASTE_BIN_FULL, bin.getZone().getId(), Severity.MODERATE,
                Map.of("binCode", bin.getCode(), "capacityBefore", before));
        }
    }
}
