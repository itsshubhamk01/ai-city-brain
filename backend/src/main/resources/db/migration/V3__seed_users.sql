-- ============================================================================
-- V3: Demo accounts — one per role, so every dashboard/permission level can be
-- demonstrated immediately after a fresh clone. Passwords are intentionally
-- simple DEMO-ONLY credentials; change or remove these before any real
-- deployment (see docs/SETUP.md "Demo accounts" section).
-- Hashes below are real BCrypt (cost factor 10), compatible with Spring
-- Security's BCryptPasswordEncoder used by AuthService.
-- ============================================================================

INSERT INTO users (id, username, password_hash, full_name, email, role, enabled, created_at, updated_at) VALUES
('5d3ee35e-076b-4226-9337-e8a847ee41e0', 'admin', '$2b$10$3p.LVFtLOlJ7Y.27xzMOduP9zbK9pMbhquHzB8veJ89ToytCnv18K', 'Ava Martinez (Admin)', 'admin@novacity.gov', 'ADMIN', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('0dc020d5-7290-4f81-8edf-de5aa39d28f4', 'ops_manager', '$2b$10$KAFFNKReSH2e.BHQ5xsYMeRAMMDsbR6wgu4zae4ZXSdYOSsGLuw0y', 'Marcus Chen (Operations Manager)', 'ops@novacity.gov', 'OPERATIONS_MANAGER', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('4ac505ee-571c-479f-9da2-59bf0e3360cc', 'responder', '$2b$10$/WWhRsoQhf7RbE9/2/KkYuNSh5Ag8AuLeUutsFFrR5CDhfpX4ZNT6', 'Priya Nair (Emergency Responder)', 'responder@novacity.gov', 'EMERGENCY_RESPONDER', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('65b1fb29-49cb-4042-9670-d197f78b2acf', 'traffic_mgr', '$2b$10$vz/M5s5//Q.VPAzMYCLUzedufeNhW4se8PPr.ZiFG44aPq5MquJqK', 'Diego Alvarez (Traffic Manager)', 'traffic@novacity.gov', 'TRAFFIC_MANAGER', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('f6b93919-b172-485f-b5f0-e302e703dc52', 'analyst', '$2b$10$vZrCJWrqD6.BHtxQyGbylen4L81GBYRzrbuwSacWpd6MPVAX07kl6', 'Hana Suzuki (Analyst)', 'analyst@novacity.gov', 'ANALYST', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('c9402603-10ea-4334-bee1-a10bf67a60e6', 'citizen', '$2b$10$MldPKrXEetq1GT1VP.hKae0dI.qUiqhfkv.hOO9kr1mbL35ylILZu', 'Sam Whitfield (Citizen)', 'citizen@novacity.gov', 'CITIZEN', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
