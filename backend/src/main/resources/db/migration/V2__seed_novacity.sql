-- ============================================================================
-- V2: Seed the demo city — "NovaCity" — with 6 zones and their infrastructure.
-- All IDs are fixed literals so this file is fully reproducible and so later
-- migrations / documentation can reference specific rows by name in comments.
-- ============================================================================

INSERT INTO cities (id, name, description, population, center_lat, center_lng, timezone, created_at, updated_at) VALUES
('6604a61d-6191-4f48-8fe2-251cab7e6e66', 'NovaCity', 'A mid-sized fictional smart city used as the AI City Brain demo environment.',
 1250000, 39.9000, -105.5000, 'America/Denver', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO zones (id, city_id, name, kind, center_lat, center_lng, population,
                    traffic_level, rainfall_mm, flood_risk_score, power_demand_mw, power_supply_mw,
                    hospital_occupancy_pct, waste_level_pct, aqi, water_supply_pct, risk_score,
                    created_at, updated_at) VALUES
('bb24430c-ec6d-4127-9f16-d2c71bf7a788', '6604a61d-6191-4f48-8fe2-251cab7e6e66', 'Downtown Core', 'DOWNTOWN',
 39.9000, -105.5000, 320000, 45, 5, 3, 60, 75, 55, 25, 55, 96, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('780d23ae-b2bc-4cf7-bab2-3bb7cc92168a', '6604a61d-6191-4f48-8fe2-251cab7e6e66', 'Riverside District', 'RIVERSIDE',
 39.8850, -105.4850, 180000, 30, 8, 6, 45, 70, 48, 20, 38, 94, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('cefceb6e-282d-4e7c-a436-eec0a2f85606', '6604a61d-6191-4f48-8fe2-251cab7e6e66', 'Industrial Park', 'INDUSTRIAL',
 39.9150, -105.5300, 95000, 25, 4, 2, 70, 78, 40, 22, 72, 95, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('d902ee5e-6d50-40a1-88d3-fd9a55837382', '6604a61d-6191-4f48-8fe2-251cab7e6e66', 'Green Meadows', 'RESIDENTIAL',
 39.9200, -105.4700, 260000, 22, 5, 2, 40, 65, 45, 18, 35, 97, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('98c74a2d-bb16-493a-a643-6b1c61ba24e0', '6604a61d-6191-4f48-8fe2-251cab7e6e66', 'Oakwood Suburbs', 'SUBURBAN',
 39.8700, -105.5350, 210000, 16, 4, 1, 35, 60, 42, 15, 30, 98, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('b0095715-a272-465d-93c8-09adf28e989c', '6604a61d-6191-4f48-8fe2-251cab7e6e66', 'Airport District', 'AIRPORT',
 39.9400, -105.5100, 45000, 28, 4, 2, 50, 68, 38, 20, 48, 95, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Roads (a couple of named arterials per zone) --------------------------------------------
INSERT INTO roads (id, zone_id, name, start_lat, start_lng, end_lat, end_lng, status, congestion_pct, created_at, updated_at) VALUES
('33f5a24e-880f-4a6a-b2b0-c063870ca49a', 'bb24430c-ec6d-4127-9f16-d2c71bf7a788', 'Main Street', 39.8970, -105.5040, 39.9030, -105.4960, 'OPEN', 40, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('2dd86fe0-e955-436e-a994-ee372af07bf7', 'bb24430c-ec6d-4127-9f16-d2c71bf7a788', 'Capitol Avenue', 39.8980, -105.4970, 39.9040, -105.5050, 'OPEN', 38, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('84df0cf4-f924-4a39-a581-551cbbd77919', '780d23ae-b2bc-4cf7-bab2-3bb7cc92168a', 'Riverbank Road', 39.8820, -105.4900, 39.8880, -105.4800, 'OPEN', 25, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('fe609457-03aa-407f-ad48-6aba0a0c9370', '780d23ae-b2bc-4cf7-bab2-3bb7cc92168a', 'Levee Drive', 39.8830, -105.4870, 39.8870, -105.4830, 'OPEN', 22, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('2bbfecd7-6594-4e3a-86e0-98a814d37ed2', 'cefceb6e-282d-4e7c-a436-eec0a2f85606', 'Foundry Boulevard', 39.9120, -105.5340, 39.9180, -105.5260, 'OPEN', 30, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('98a26dbd-ffae-4865-9fa1-6c4b1288a76f', 'd902ee5e-6d50-40a1-88d3-fd9a55837382', 'Meadow Lane', 39.9170, -105.4740, 39.9230, -105.4660, 'OPEN', 18, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('e72463aa-43e7-4519-8a7f-db53f30f9398', '98c74a2d-bb16-493a-a643-6b1c61ba24e0', 'Oakwood Parkway', 39.8670, -105.5390, 39.8730, -105.5310, 'OPEN', 15, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('18ea7c11-14e8-4f84-8788-099e8b59d751', 'b0095715-a272-465d-93c8-09adf28e989c', 'Terminal Access Road', 39.9370, -105.5140, 39.9430, -105.5060, 'OPEN', 20, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Hospitals ---------------------------------------------------------------------------------
INSERT INTO hospitals (id, zone_id, name, lat, lng, total_beds, occupied_beds, emergency_capacity, created_at, updated_at) VALUES
('5fe5bd6d-25b9-4762-a213-35b1ca016e98', 'bb24430c-ec6d-4127-9f16-d2c71bf7a788', 'NovaCity General Hospital', 39.8990, -105.4990, 420, 231, 40, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('2ee32421-81e2-4076-b31b-8cf2ef78b78c', '780d23ae-b2bc-4cf7-bab2-3bb7cc92168a', 'Riverside Medical Center', 39.8845, -105.4845, 210, 100, 25, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('28fc4ddd-1d8c-4d4d-a33f-4e090384adb1', 'd902ee5e-6d50-40a1-88d3-fd9a55837382', 'Green Meadows Community Hospital', 39.9195, -105.4695, 180, 81, 20, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('f9352021-ae9e-48d0-8d13-e5baf1fdf64c', 'cefceb6e-282d-4e7c-a436-eec0a2f85606', 'Industrial Park Urgent Care', 39.9145, -105.5295, 90, 41, 15, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Fire stations -------------------------------------------------------------------------------
INSERT INTO fire_stations (id, zone_id, name, lat, lng, total_units, available_units, created_at, updated_at) VALUES
('1a060566-057e-48bb-8e03-3d028ea444cd', 'bb24430c-ec6d-4127-9f16-d2c71bf7a788', 'Downtown Fire Station 1', 39.9005, -105.5010, 6, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('9ce30fc1-1ae2-4628-92b4-cd50e9bb75bf', 'cefceb6e-282d-4e7c-a436-eec0a2f85606', 'Industrial Fire Station 4', 39.9155, -105.5305, 5, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('42f9a7c0-c003-4a79-bdad-1353312f21bd', '98c74a2d-bb16-493a-a643-6b1c61ba24e0', 'Oakwood Fire Station 7', 39.8705, -105.5355, 4, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Ambulances ------------------------------------------------------------------------------------
INSERT INTO ambulances (id, zone_id, code, lat, lng, status, created_at, updated_at) VALUES
('bf0ba989-f004-4285-8a6f-afeed2696a9a', 'bb24430c-ec6d-4127-9f16-d2c71bf7a788', 'AMB-01', 39.8995, -105.4995, 'DISPATCHED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('5d512514-3800-4c19-9b63-720ba7fa64a2', 'bb24430c-ec6d-4127-9f16-d2c71bf7a788', 'AMB-02', 39.9010, -105.5020, 'AVAILABLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('92019eb5-f245-429b-a213-4f170de3588e', '780d23ae-b2bc-4cf7-bab2-3bb7cc92168a', 'AMB-03', 39.8848, -105.4848, 'AVAILABLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('518ec01c-3b0f-4b3a-a7e4-6bee7ac992d1', 'cefceb6e-282d-4e7c-a436-eec0a2f85606', 'AMB-04', 39.9148, -105.5298, 'AVAILABLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('44643333-6379-4911-bb6c-4607c5d7cde4', 'd902ee5e-6d50-40a1-88d3-fd9a55837382', 'AMB-05', 39.9198, -105.4698, 'AVAILABLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('3172994c-fb2b-4023-a33f-bf5d0c4d9fc9', '98c74a2d-bb16-493a-a643-6b1c61ba24e0', 'AMB-06', 39.8703, -105.5348, 'AVAILABLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Waste bins -------------------------------------------------------------------------------------
INSERT INTO waste_bins (id, zone_id, code, lat, lng, capacity_pct, created_at, updated_at) VALUES
('b77646ad-04ad-4562-a908-cfce8e9904d4', 'bb24430c-ec6d-4127-9f16-d2c71bf7a788', 'BIN-D1', 39.8985, -105.4985, 62, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('e6179f0c-f2ce-4848-906f-acb5db96f9e2', '780d23ae-b2bc-4cf7-bab2-3bb7cc92168a', 'BIN-R1', 39.8843, -105.4843, 48, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('dc3dbb63-fa43-45b9-9bf9-b091c8a74a0a', 'cefceb6e-282d-4e7c-a436-eec0a2f85606', 'BIN-I1', 39.9143, -105.5293, 70, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('623313cd-129a-4df0-86c9-bf5d22f9b2cc', 'd902ee5e-6d50-40a1-88d3-fd9a55837382', 'BIN-G1', 39.9193, -105.4693, 35, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('dc9f54b2-8a12-47ef-ba11-98dccdad65fb', '98c74a2d-bb16-493a-a643-6b1c61ba24e0', 'BIN-O1', 39.8698, -105.5343, 28, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('12174a7b-fe13-4af9-8204-03aeb34e468a', 'b0095715-a272-465d-93c8-09adf28e989c', 'BIN-A1', 39.9393, -105.5093, 40, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Power stations ----------------------------------------------------------------------------------
INSERT INTO power_stations (id, zone_id, name, lat, lng, capacity_mw, current_load_mw, created_at, updated_at) VALUES
('a13ecbc4-9fb4-45b6-bede-33ac8ee4a8cc', 'bb24430c-ec6d-4127-9f16-d2c71bf7a788', 'Downtown Substation A', 39.9020, -105.5030, 90, 60, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('251ab6fe-87dc-4f4e-a826-c7c4464298a5', 'cefceb6e-282d-4e7c-a436-eec0a2f85606', 'Industrial Grid Station', 39.9160, -105.5320, 110, 70, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('d2e69c92-b1ec-4684-bc86-7bdcd698aab8', '98c74a2d-bb16-493a-a643-6b1c61ba24e0', 'Oakwood Substation C', 39.8690, -105.5370, 55, 35, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Water stations -------------------------------------------------------------------------------------
INSERT INTO water_stations (id, zone_id, name, lat, lng, reservoir_level_pct, created_at, updated_at) VALUES
('9f5461d1-c51f-4088-a0a6-51faf27408ec', '780d23ae-b2bc-4cf7-bab2-3bb7cc92168a', 'Riverside Reservoir & Treatment Plant', 39.8810, -105.4810, 94, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('f20d111f-5a1f-4c5c-85eb-98d5c011c9aa', 'd902ee5e-6d50-40a1-88d3-fd9a55837382', 'Green Meadows Pumping Station', 39.9230, -105.4730, 97, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
