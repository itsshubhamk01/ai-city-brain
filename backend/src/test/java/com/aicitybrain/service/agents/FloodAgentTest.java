package com.aicitybrain.service.agents;

import com.aicitybrain.domain.City;
import com.aicitybrain.domain.Incident;
import com.aicitybrain.domain.Severity;
import com.aicitybrain.domain.Zone;
import com.aicitybrain.repository.AgentActionRepository;
import com.aicitybrain.repository.AgentEventRepository;
import com.aicitybrain.repository.IncidentRepository;
import com.aicitybrain.repository.ZoneRepository;
import com.aicitybrain.service.events.CityEvent;
import com.aicitybrain.service.events.EventBus;
import com.aicitybrain.service.events.EventTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FloodAgentTest {

    private EventBus eventBus;
    private AgentEventRepository agentEventRepository;
    private AgentActionRepository agentActionRepository;
    private ZoneRepository zoneRepository;
    private IncidentRepository incidentRepository;
    private FloodAgent floodAgent;

    @BeforeEach
    void setUp() {
        eventBus = mock(EventBus.class);
        agentEventRepository = mock(AgentEventRepository.class);
        agentActionRepository = mock(AgentActionRepository.class);
        zoneRepository = mock(ZoneRepository.class);
        incidentRepository = mock(IncidentRepository.class);
        floodAgent = new FloodAgent(eventBus, agentEventRepository, agentActionRepository, zoneRepository, incidentRepository);

        when(agentEventRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(agentActionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(zoneRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(incidentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private Zone riversideZoneWithRainfall(double rainfallMm) {
        City city = new City("TestCity", "d", 1000, 0, 0, "UTC");
        Zone zone = new Zone(city, "Flood Test Zone", Zone.Kind.RIVERSIDE, 39.9, -105.5, 1000);
        zone.setRainfallMm(rainfallMm);
        return zone;
    }

    @Test
    void low_rainfall_triggers_no_flood_event() {
        Zone zone = riversideZoneWithRainfall(20); // well under the HIGH threshold
        when(zoneRepository.findAll()).thenReturn(List.of(zone));

        floodAgent.evaluate();

        verify(agentEventRepository, never()).save(any());
        verify(eventBus, never()).publish(any());
    }

    @Test
    void high_rainfall_publishes_high_flood_risk_without_creating_an_incident() {
        Zone zone = riversideZoneWithRainfall(75); // ~69.6 risk -> HIGH band
        when(zoneRepository.findAll()).thenReturn(List.of(zone));

        floodAgent.evaluate();

        ArgumentCaptor<CityEvent> captor = ArgumentCaptor.forClass(CityEvent.class);
        verify(eventBus).publish(captor.capture());
        assertThat(captor.getValue().type()).isEqualTo(EventTypes.FLOOD_RISK_HIGH);
        assertThat(captor.getValue().severity()).isEqualTo(Severity.HIGH);
        verify(incidentRepository, never()).save(any());
    }

    @Test
    void critical_rainfall_creates_a_flood_incident_and_recommends_evacuation() {
        Zone zone = riversideZoneWithRainfall(100); // ~92.8 risk -> CRITICAL band
        when(zoneRepository.findAll()).thenReturn(List.of(zone));
        when(incidentRepository.existsByZoneAndTypeAndStatusNot(any(), any(), any())).thenReturn(false);

        floodAgent.evaluate();

        verify(incidentRepository).save(any(Incident.class));

        ArgumentCaptor<CityEvent> captor = ArgumentCaptor.forClass(CityEvent.class);
        verify(eventBus, atLeastOnce()).publish(captor.capture());
        assertThat(captor.getAllValues())
            .extracting(CityEvent::type)
            .contains(EventTypes.FLOOD_RISK_CRITICAL, EventTypes.ZONE_EVACUATION_RECOMMENDED);
    }

    @Test
    void critical_rainfall_does_not_duplicate_an_already_open_flood_incident() {
        Zone zone = riversideZoneWithRainfall(100);
        when(zoneRepository.findAll()).thenReturn(List.of(zone));
        when(incidentRepository.existsByZoneAndTypeAndStatusNot(any(), any(), any())).thenReturn(true);

        floodAgent.evaluate();

        verify(incidentRepository, never()).save(any(Incident.class));
    }
}
