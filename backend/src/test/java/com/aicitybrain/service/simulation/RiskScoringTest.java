package com.aicitybrain.service.simulation;

import com.aicitybrain.domain.City;
import com.aicitybrain.domain.Zone;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RiskScoringTest {

    private Zone zoneOf(Zone.Kind kind) {
        City city = new City("TestCity", "desc", 100_000, 0, 0, "UTC");
        return new Zone(city, "Test Zone", kind, 0, 0, 10_000);
    }

    @Test
    void riverside_has_higher_flood_risk_than_suburban_for_same_rainfall() {
        double riverside = RiskScoring.computeFloodRisk(80, Zone.Kind.RIVERSIDE);
        double suburban = RiskScoring.computeFloodRisk(80, Zone.Kind.SUBURBAN);

        assertThat(riverside).isGreaterThan(suburban);
    }

    @Test
    void computeFloodRisk_is_clamped_between_0_and_100() {
        assertThat(RiskScoring.computeFloodRisk(0, Zone.Kind.SUBURBAN)).isBetween(0.0, 100.0);
        assertThat(RiskScoring.computeFloodRisk(500, Zone.Kind.RIVERSIDE)).isEqualTo(100.0);
    }

    @Test
    void riskLevel_maps_scores_to_expected_bands() {
        assertThat(RiskScoring.riskLevel(10)).isEqualTo("LOW");
        assertThat(RiskScoring.riskLevel(40)).isEqualTo("MODERATE");
        assertThat(RiskScoring.riskLevel(65)).isEqualTo("HIGH");
        assertThat(RiskScoring.riskLevel(90)).isEqualTo("CRITICAL");
    }

    @Test
    void computeZoneRisk_increases_as_traffic_and_flood_risk_increase() {
        Zone calm = zoneOf(Zone.Kind.SUBURBAN);
        calm.setTrafficLevel(10);
        calm.setFloodRiskScore(5);

        Zone stressed = zoneOf(Zone.Kind.SUBURBAN);
        stressed.setTrafficLevel(90);
        stressed.setFloodRiskScore(85);

        assertThat(RiskScoring.computeZoneRisk(stressed)).isGreaterThan(RiskScoring.computeZoneRisk(calm));
    }

    @Test
    void computeZoneRisk_treats_zero_power_supply_as_maximum_strain() {
        Zone zone = zoneOf(Zone.Kind.INDUSTRIAL);
        zone.setPowerSupplyMw(0);
        zone.setPowerDemandMw(50);

        assertThat(RiskScoring.computeZoneRisk(zone)).isGreaterThan(20);
    }
}
