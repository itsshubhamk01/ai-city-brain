package com.aicitybrain.service.brain;

import com.aicitybrain.domain.AgentAction;
import com.aicitybrain.domain.AgentType;
import com.aicitybrain.domain.Alert;
import com.aicitybrain.domain.City;
import com.aicitybrain.domain.Severity;
import com.aicitybrain.domain.Zone;
import com.aicitybrain.repository.AgentActionRepository;
import com.aicitybrain.repository.AgentEventRepository;
import com.aicitybrain.repository.AlertRepository;
import com.aicitybrain.repository.ZoneRepository;
import com.aicitybrain.service.events.CityEvent;
import com.aicitybrain.service.events.EventBus;
import com.aicitybrain.service.events.EventTypes;
import com.aicitybrain.service.simulation.CityQueryService;
import com.aicitybrain.websocket.WebSocketBroadcaster;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CityBrainServiceTest {

    private EventBus eventBus;
    private AlertRepository alertRepository;
    private ZoneRepository zoneRepository;
    private AgentEventRepository agentEventRepository;
    private AgentActionRepository agentActionRepository;
    private CityQueryService cityQueryService;
    private WebSocketBroadcaster broadcaster;
    private CityBrainService cityBrain;
    private Zone zone;

    @BeforeEach
    void setUp() {
        eventBus = mock(EventBus.class);
        alertRepository = mock(AlertRepository.class);
        zoneRepository = mock(ZoneRepository.class);
        agentEventRepository = mock(AgentEventRepository.class);
        agentActionRepository = mock(AgentActionRepository.class);
        cityQueryService = mock(CityQueryService.class);
        broadcaster = mock(WebSocketBroadcaster.class);

        cityBrain = new CityBrainService(eventBus, alertRepository, zoneRepository, agentEventRepository,
            agentActionRepository, cityQueryService, broadcaster);

        City city = new City("TestCity", "d", 1000, 0, 0, "UTC");
        zone = new Zone(city, "Test Zone", Zone.Kind.RIVERSIDE, 39.9, -105.5, 1000);

        when(alertRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(agentActionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(zoneRepository.findById(any())).thenReturn(Optional.of(zone));
    }

    @Test
    void low_severity_events_do_not_raise_an_alert() {
        cityBrain.onEvent(new CityEvent(EventTypes.WASTE_BIN_FULL, AgentType.WASTE, UUID.randomUUID(),
            Severity.LOW, Map.of(), null));

        verify(alertRepository, never()).save(any());
    }

    @Test
    void high_severity_event_raises_exactly_one_alert() {
        cityBrain.onEvent(new CityEvent(EventTypes.TRAFFIC_CONGESTION_HIGH, AgentType.TRAFFIC, UUID.randomUUID(),
            Severity.HIGH, Map.of(), null));

        verify(alertRepository, times(1)).save(any(Alert.class));
    }

    @Test
    void two_distinct_critical_signals_in_the_same_zone_raise_a_coordinated_response_action() {
        UUID zoneId = UUID.randomUUID();

        cityBrain.onEvent(new CityEvent(EventTypes.FLOOD_RISK_CRITICAL, AgentType.FLOOD, zoneId,
            Severity.CRITICAL, Map.of(), null));
        // One critical signal alone must not yet trigger the compound-crisis action.
        verify(agentActionRepository, never()).save(any());

        cityBrain.onEvent(new CityEvent(EventTypes.POWER_OUTAGE, AgentType.ENERGY, zoneId,
            Severity.CRITICAL, Map.of(), null));

        verify(agentActionRepository, times(1)).save(any(AgentAction.class));
    }
}
