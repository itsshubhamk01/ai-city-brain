-- ============================================================================
-- V4: A handful of starter incidents/alerts/decisions so the command center has
-- something to show in the first seconds after startup, before the simulation
-- engine has generated its own organic activity.
-- ============================================================================

INSERT INTO incidents (id, zone_id, type, severity, status, description, lat, lng, assigned_agent, created_at, updated_at) VALUES
('72c6d3be-240c-4889-a167-a17308d31e8e', 'bb24430c-ec6d-4127-9f16-d2c71bf7a788', 'TRAFFIC_ACCIDENT', 'MODERATE', 'IN_PROGRESS',
 'Two-vehicle collision near Main Street and Capitol Avenue.', 39.9000, -105.5005, 'EMERGENCY', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('f8b566e4-e7a4-4e25-857c-3fdf58987710', 'cefceb6e-282d-4e7c-a436-eec0a2f85606', 'WASTE_OVERFLOW', 'LOW', 'REPORTED',
 'Overflowing waste bin reported near Foundry Boulevard.', 39.9143, -105.5293, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO alerts (id, severity, title, message, zone_id, source, acknowledged, created_at, updated_at) VALUES
('67452671-edb6-4b2e-a4d8-faef931ce8ec', 'MODERATE', 'Traffic Accident Reported', 'Two-vehicle collision near Main Street and Capitol Avenue in Downtown Core — emergency response dispatched.',
 'bb24430c-ec6d-4127-9f16-d2c71bf7a788', 'EMERGENCY', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('5378813e-9011-4a14-8222-2222e112130c', 'LOW', 'Welcome to AI City Brain', 'NovaCity''s digital twin is live. Live metrics, incidents and AI agent decisions will begin streaming immediately.',
 NULL, 'CITY_BRAIN', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO agent_events (id, agent_type, event_type, severity, zone_id, summary, created_at, updated_at) VALUES
('439a0137-e81c-407c-a814-ae6577d51076', 'TRAFFIC', 'TRAFFIC_CONGESTION_HIGH', 'MODERATE', 'bb24430c-ec6d-4127-9f16-d2c71bf7a788',
 'Downtown Core morning congestion at 45% — within normal range, monitoring.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('7e3bcfc7-63b7-417d-9b8b-2a63887f8374', 'WASTE', 'WASTE_BIN_FULL', 'MODERATE', 'cefceb6e-282d-4e7c-a436-eec0a2f85606',
 'Bin BIN-I1 in Industrial Park reported at 70% capacity.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('c7356f5a-9d84-46bf-8d70-ed2f8cde320a', 'CITY_BRAIN', 'SYSTEM_STARTED', 'LOW', NULL,
 'AI City Brain initialized for NovaCity — 6 zones, 6 agents, and the CityBrain orchestrator are online.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO agent_actions (id, agent_type, action_type, description, zone_id, status, created_at, updated_at) VALUES
('780cb663-6c56-4aad-8c0d-6c4d9312db57', 'EMERGENCY', 'AMBULANCE_DISPATCH', 'Ambulance AMB-01 dispatched to two-vehicle collision in Downtown Core.',
 'bb24430c-ec6d-4127-9f16-d2c71bf7a788', 'EXECUTED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('9debd077-c29d-4bdb-8b73-a9c05f33c76b', 'CITY_BRAIN', 'SYSTEM_STARTED', 'Simulation engine started — ticking every few seconds with live synthetic city data.',
 NULL, 'EXECUTED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
