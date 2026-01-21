-- ===============================================
-- ADD TEST CUSTOMER FOR LOGIN TESTING
-- Run this if you already have the database set up
-- ===============================================

USE uzima_borehole_db;

-- First add password column if it doesn't exist
ALTER TABLE clients ADD COLUMN IF NOT EXISTS password VARCHAR(255);

-- If the above fails (older MySQL), try this:
-- ALTER TABLE clients ADD password VARCHAR(255);

-- Insert test customers (delete first if they exist)
DELETE FROM clients WHERE client_id IN ('DOM-2025-0001', 'COM-2025-0001');

INSERT INTO clients (client_id, full_name, physical_address, phone_number, email, 
    client_category, borehole_location, county, registration_date, password, is_active) VALUES
('DOM-2025-0001', 'John Kamau', '123 Kenyatta Avenue, Nairobi', '0722123456', 'john.kamau@email.com',
    'DOMESTIC', 'Kiambu Road, Near Total Petrol Station', 'Nairobi', CURDATE(), 'test123', TRUE),
('COM-2025-0001', 'Acme Industries Ltd', '45 Industrial Area, Mombasa', '0733456789', 'info@acme.co.ke',
    'COMMERCIAL', 'Industrial Zone B, Plot 78', 'Mombasa', CURDATE(), 'test123', TRUE);

SELECT '✅ Test customers added successfully!' AS Status;
SELECT client_id, full_name, password FROM clients WHERE client_id LIKE '%-2025-0001';

-- ===============================================
-- CUSTOMER LOGIN CREDENTIALS:
-- ===============================================
-- Customer 1:
--   Client ID: DOM-2025-0001
--   Password:  test123
--
-- Customer 2:
--   Client ID: COM-2025-0001
--   Password:  test123
-- ===============================================
