-- ============================================================================
-- V5: Rename seed accounts to real Indian names (removing the "(Role)" suffix
-- style that read as demo/placeholder data). The Administrator account is
-- named after the project's creator.
-- ============================================================================

UPDATE users SET full_name = 'Shubham Kadam' WHERE username = 'admin';
UPDATE users SET full_name = 'Priya Deshmukh' WHERE username = 'ops_manager';
UPDATE users SET full_name = 'Rahul Sharma' WHERE username = 'responder';
UPDATE users SET full_name = 'Anjali Patil' WHERE username = 'traffic_mgr';
UPDATE users SET full_name = 'Vikram Joshi' WHERE username = 'analyst';
UPDATE users SET full_name = 'Sneha Iyer' WHERE username = 'citizen';
