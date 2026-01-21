-- ===============================================
-- FIX DATABASE AND ADD TEST CUSTOMER
-- ===============================================
-- Run this in MySQL: mysql -u root -p < database/fix_and_add_customer.sql
-- Or copy and paste into MySQL Workbench/terminal

USE uzima_borehole_db;

-- Step 1: Add password column (ignore error if already exists)
ALTER TABLE clients ADD COLUMN password VARCHAR(255);

-- Step 2: Delete existing test customers if any
DELETE FROM clients WHERE client_id IN ('DOM-2025-0001', 'COM-2025-0001');

-- Step 3: Insert test customers
INSERT INTO clients (client_id, full_name, physical_address, phone_number, email, 
    client_category, borehole_location, county, registration_date, password, is_active) VALUES
('DOM-2025-0001', 'John Kamau', '123 Kenyatta Avenue, Nairobi', '0722123456', 'john.kamau@email.com',
    'DOMESTIC', 'Kiambu Road, Near Total Petrol Station', 'Nairobi', CURDATE(), 'test123', TRUE);

INSERT INTO clients (client_id, full_name, physical_address, phone_number, email, 
    client_category, borehole_location, county, registration_date, password, is_active) VALUES
('COM-2025-0001', 'Acme Industries Ltd', '45 Industrial Area, Mombasa', '0733456789', 'info@acme.co.ke',
    'COMMERCIAL', 'Industrial Zone B, Plot 78', 'Mombasa', CURDATE(), 'test123', TRUE);

SELECT '✅ Done! Test customers added.' AS Status;
SELECT client_id AS 'Customer ID', full_name AS 'Name', password AS 'Password' FROM clients WHERE client_id LIKE '%-2025-0001';
