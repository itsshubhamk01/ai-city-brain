-- ============================================================================
-- V1: Core schema for AI City Brain.
-- Runs identically against H2 (local, MODE=PostgreSQL) and real PostgreSQL
-- (docker profile) — this is the single source of truth for the schema;
-- Hibernate's ddl-auto is set to "none" (see application.yml) precisely so
-- Flyway always owns it.
-- ============================================================================

CREATE TABLE cities (
    id           UUID PRIMARY KEY,
    name         VARCHAR(120) NOT NULL UNIQUE,
    description  VARCHAR(500),
    population   BIGINT NOT NULL,
    center_lat   DOUBLE PRECISION NOT NULL,
    center_lng   DOUBLE PRECISION NOT NULL,
    timezone     VARCHAR(60),
    created_at   TIMESTAMP NOT NULL,
    updated_at   TIMESTAMP NOT NULL
);

CREATE TABLE users (
    id             UUID PRIMARY KEY,
    username       VARCHAR(80) NOT NULL UNIQUE,
    password_hash  VARCHAR(255) NOT NULL,
    full_name      VARCHAR(150) NOT NULL,
    email          VARCHAR(150) NOT NULL UNIQUE,
    role           VARCHAR(30) NOT NULL,
    enabled        BOOLEAN NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMP NOT NULL,
    updated_at     TIMESTAMP NOT NULL
);

CREATE TABLE zones (
    id                       UUID PRIMARY KEY,
    city_id                  UUID NOT NULL REFERENCES cities(id),
    name                     VARCHAR(120) NOT NULL,
    kind                     VARCHAR(30) NOT NULL,
    center_lat               DOUBLE PRECISION NOT NULL,
    center_lng               DOUBLE PRECISION NOT NULL,
    population               BIGINT NOT NULL,
    traffic_level            DOUBLE PRECISION NOT NULL DEFAULT 30,
    rainfall_mm              DOUBLE PRECISION NOT NULL DEFAULT 0,
    flood_risk_score         DOUBLE PRECISION NOT NULL DEFAULT 0,
    power_demand_mw          DOUBLE PRECISION NOT NULL DEFAULT 40,
    power_supply_mw          DOUBLE PRECISION NOT NULL DEFAULT 60,
    hospital_occupancy_pct   DOUBLE PRECISION NOT NULL DEFAULT 50,
    waste_level_pct          DOUBLE PRECISION NOT NULL DEFAULT 20,
    aqi                      DOUBLE PRECISION NOT NULL DEFAULT 40,
    water_supply_pct         DOUBLE PRECISION NOT NULL DEFAULT 95,
    risk_score               DOUBLE PRECISION NOT NULL DEFAULT 0,
    created_at               TIMESTAMP NOT NULL,
    updated_at               TIMESTAMP NOT NULL
);
CREATE INDEX idx_zones_city ON zones(city_id);

CREATE TABLE roads (
    id              UUID PRIMARY KEY,
    zone_id         UUID NOT NULL REFERENCES zones(id),
    name            VARCHAR(120) NOT NULL,
    start_lat       DOUBLE PRECISION NOT NULL,
    start_lng       DOUBLE PRECISION NOT NULL,
    end_lat         DOUBLE PRECISION NOT NULL,
    end_lng         DOUBLE PRECISION NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    congestion_pct  DOUBLE PRECISION NOT NULL DEFAULT 20,
    created_at      TIMESTAMP NOT NULL,
    updated_at      TIMESTAMP NOT NULL
);
CREATE INDEX idx_roads_zone ON roads(zone_id);

CREATE TABLE hospitals (
    id                   UUID PRIMARY KEY,
    zone_id              UUID NOT NULL REFERENCES zones(id),
    name                 VARCHAR(150) NOT NULL,
    lat                  DOUBLE PRECISION NOT NULL,
    lng                  DOUBLE PRECISION NOT NULL,
    total_beds           INTEGER NOT NULL,
    occupied_beds        INTEGER NOT NULL,
    emergency_capacity   INTEGER NOT NULL,
    created_at           TIMESTAMP NOT NULL,
    updated_at           TIMESTAMP NOT NULL
);
CREATE INDEX idx_hospitals_zone ON hospitals(zone_id);

CREATE TABLE fire_stations (
    id                UUID PRIMARY KEY,
    zone_id           UUID NOT NULL REFERENCES zones(id),
    name              VARCHAR(150) NOT NULL,
    lat               DOUBLE PRECISION NOT NULL,
    lng               DOUBLE PRECISION NOT NULL,
    total_units       INTEGER NOT NULL,
    available_units   INTEGER NOT NULL,
    created_at        TIMESTAMP NOT NULL,
    updated_at        TIMESTAMP NOT NULL
);
CREATE INDEX idx_fire_stations_zone ON fire_stations(zone_id);

CREATE TABLE ambulances (
    id           UUID PRIMARY KEY,
    zone_id      UUID NOT NULL REFERENCES zones(id),
    code         VARCHAR(20) NOT NULL UNIQUE,
    lat          DOUBLE PRECISION NOT NULL,
    lng          DOUBLE PRECISION NOT NULL,
    status       VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    created_at   TIMESTAMP NOT NULL,
    updated_at   TIMESTAMP NOT NULL
);
CREATE INDEX idx_ambulances_zone ON ambulances(zone_id);
CREATE INDEX idx_ambulances_status ON ambulances(status);

CREATE TABLE waste_bins (
    id             UUID PRIMARY KEY,
    zone_id        UUID NOT NULL REFERENCES zones(id),
    code           VARCHAR(20) NOT NULL UNIQUE,
    lat            DOUBLE PRECISION NOT NULL,
    lng            DOUBLE PRECISION NOT NULL,
    capacity_pct   DOUBLE PRECISION NOT NULL DEFAULT 20,
    created_at     TIMESTAMP NOT NULL,
    updated_at     TIMESTAMP NOT NULL
);
CREATE INDEX idx_waste_bins_zone ON waste_bins(zone_id);

CREATE TABLE power_stations (
    id                UUID PRIMARY KEY,
    zone_id           UUID NOT NULL REFERENCES zones(id),
    name              VARCHAR(150) NOT NULL,
    lat               DOUBLE PRECISION NOT NULL,
    lng               DOUBLE PRECISION NOT NULL,
    capacity_mw       DOUBLE PRECISION NOT NULL,
    current_load_mw   DOUBLE PRECISION NOT NULL,
    created_at        TIMESTAMP NOT NULL,
    updated_at        TIMESTAMP NOT NULL
);
CREATE INDEX idx_power_stations_zone ON power_stations(zone_id);

CREATE TABLE water_stations (
    id                    UUID PRIMARY KEY,
    zone_id               UUID NOT NULL REFERENCES zones(id),
    name                  VARCHAR(150) NOT NULL,
    lat                   DOUBLE PRECISION NOT NULL,
    lng                   DOUBLE PRECISION NOT NULL,
    reservoir_level_pct   DOUBLE PRECISION NOT NULL DEFAULT 90,
    created_at            TIMESTAMP NOT NULL,
    updated_at            TIMESTAMP NOT NULL
);
CREATE INDEX idx_water_stations_zone ON water_stations(zone_id);

CREATE TABLE incidents (
    id               UUID PRIMARY KEY,
    zone_id          UUID NOT NULL REFERENCES zones(id),
    type             VARCHAR(30) NOT NULL,
    severity         VARCHAR(20) NOT NULL,
    status           VARCHAR(20) NOT NULL DEFAULT 'REPORTED',
    description      VARCHAR(500) NOT NULL,
    lat              DOUBLE PRECISION NOT NULL,
    lng              DOUBLE PRECISION NOT NULL,
    assigned_agent   VARCHAR(20),
    reported_by      UUID REFERENCES users(id),
    resolved_at      TIMESTAMP,
    created_at       TIMESTAMP NOT NULL,
    updated_at       TIMESTAMP NOT NULL
);
CREATE INDEX idx_incidents_zone ON incidents(zone_id);
CREATE INDEX idx_incidents_status ON incidents(status);
CREATE INDEX idx_incidents_created_at ON incidents(created_at DESC);

CREATE TABLE agent_events (
    id                     UUID PRIMARY KEY,
    agent_type             VARCHAR(20) NOT NULL,
    event_type             VARCHAR(60) NOT NULL,
    severity               VARCHAR(20) NOT NULL,
    zone_id                UUID REFERENCES zones(id),
    related_incident_id    UUID REFERENCES incidents(id),
    summary                VARCHAR(1000) NOT NULL,
    payload_json           TEXT,
    created_at             TIMESTAMP NOT NULL,
    updated_at             TIMESTAMP NOT NULL
);
CREATE INDEX idx_agent_events_created_at ON agent_events(created_at DESC);
CREATE INDEX idx_agent_events_zone ON agent_events(zone_id);

CREATE TABLE agent_actions (
    id                     UUID PRIMARY KEY,
    agent_type             VARCHAR(20) NOT NULL,
    action_type            VARCHAR(60) NOT NULL,
    description            VARCHAR(1000) NOT NULL,
    zone_id                UUID REFERENCES zones(id),
    related_incident_id    UUID REFERENCES incidents(id),
    status                 VARCHAR(20) NOT NULL DEFAULT 'EXECUTED',
    created_at             TIMESTAMP NOT NULL,
    updated_at             TIMESTAMP NOT NULL
);
CREATE INDEX idx_agent_actions_created_at ON agent_actions(created_at DESC);
CREATE INDEX idx_agent_actions_zone ON agent_actions(zone_id);

CREATE TABLE alerts (
    id             UUID PRIMARY KEY,
    severity       VARCHAR(20) NOT NULL,
    title          VARCHAR(150) NOT NULL,
    message        VARCHAR(1000) NOT NULL,
    zone_id        UUID REFERENCES zones(id),
    source         VARCHAR(60) NOT NULL,
    acknowledged   BOOLEAN NOT NULL DEFAULT FALSE,
    created_at     TIMESTAMP NOT NULL,
    updated_at     TIMESTAMP NOT NULL
);
CREATE INDEX idx_alerts_created_at ON alerts(created_at DESC);
CREATE INDEX idx_alerts_acknowledged ON alerts(acknowledged);

CREATE TABLE simulation_runs (
    id                    UUID PRIMARY KEY,
    scenario_key          VARCHAR(60) NOT NULL,
    rainfall_input        DOUBLE PRECISION,
    traffic_input         DOUBLE PRECISION,
    population_input      DOUBLE PRECISION,
    power_demand_input    DOUBLE PRECISION,
    emergency_input       DOUBLE PRECISION,
    triggered_by          UUID REFERENCES users(id),
    status                VARCHAR(20) NOT NULL DEFAULT 'RUNNING',
    completed_at          TIMESTAMP,
    created_at            TIMESTAMP NOT NULL,
    updated_at            TIMESTAMP NOT NULL
);
CREATE INDEX idx_simulation_runs_created_at ON simulation_runs(created_at DESC);

CREATE TABLE zone_status_snapshots (
    id                       UUID PRIMARY KEY,
    zone_id                  UUID NOT NULL REFERENCES zones(id),
    traffic_level            DOUBLE PRECISION NOT NULL,
    rainfall_mm              DOUBLE PRECISION NOT NULL,
    flood_risk_score         DOUBLE PRECISION NOT NULL,
    power_demand_mw          DOUBLE PRECISION NOT NULL,
    power_supply_mw          DOUBLE PRECISION NOT NULL,
    hospital_occupancy_pct   DOUBLE PRECISION NOT NULL,
    waste_level_pct          DOUBLE PRECISION NOT NULL,
    aqi                      DOUBLE PRECISION NOT NULL,
    water_supply_pct         DOUBLE PRECISION NOT NULL,
    risk_score               DOUBLE PRECISION NOT NULL,
    created_at               TIMESTAMP NOT NULL,
    updated_at               TIMESTAMP NOT NULL
);
CREATE INDEX idx_snapshots_zone_created ON zone_status_snapshots(zone_id, created_at DESC);
