-- ===============================================
-- UZIMA BOREHOLE DRILLING SYSTEM - COMPLETE DATABASE SETUP
-- ===============================================
-- This script creates the database, tables, and adds test data
-- Run with: mysql -u root -p < database/setup_database.sql

DROP DATABASE IF EXISTS uzima_borehole_db;
CREATE DATABASE uzima_borehole_db;
USE uzima_borehole_db;

-- ===============================================
-- 1. USERS TABLE
-- ===============================================
CREATE TABLE users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role ENUM('ADMIN', 'MANAGER', 'CLERK') DEFAULT 'CLERK',
    email VARCHAR(100),
    phone VARCHAR(20),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL,
    INDEX idx_username (username)
);

-- ===============================================
-- 2. CLIENTS TABLE
-- ===============================================
CREATE TABLE clients (
    client_id VARCHAR(20) PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    physical_address TEXT NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    phone_number_2 VARCHAR(20),
    email VARCHAR(100),
    client_category ENUM('INDUSTRIAL', 'COMMERCIAL', 'DOMESTIC') NOT NULL,
    borehole_location TEXT NOT NULL,
    county VARCHAR(50),
    gps_coordinates VARCHAR(100),
    id_number VARCHAR(20),
    registration_date DATE NOT NULL,
    password VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    created_by INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_category (client_category),
    INDEX idx_phone (phone_number),
    FOREIGN KEY (created_by) REFERENCES users(user_id)
);

-- ===============================================
-- 3. DRILLING SERVICES
-- ===============================================
CREATE TABLE drilling_types (
    drilling_type_id INT PRIMARY KEY AUTO_INCREMENT,
    drilling_name VARCHAR(50) UNIQUE NOT NULL,
    down_payment DECIMAL(10,2) NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE
);

-- ===============================================
-- 4. PUMP TYPES
-- ===============================================
CREATE TABLE pump_types (
    pump_type_id INT PRIMARY KEY AUTO_INCREMENT,
    pump_name VARCHAR(50) UNIQUE NOT NULL,
    base_cost DECIMAL(10,2) NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE
);

-- ===============================================
-- 5. DEPTH AND HEIGHT CHARGES
-- ===============================================
CREATE TABLE depth_height_charges (
    range_id INT PRIMARY KEY AUTO_INCREMENT,
    range_start INT NOT NULL,
    range_end INT,
    cost_per_meter DECIMAL(10,2) NOT NULL,
    description VARCHAR(100)
);

-- ===============================================
-- 6. SURVEY AND AUTHORITY FEES
-- ===============================================
CREATE TABLE survey_authority_fees (
    fee_id INT PRIMARY KEY AUTO_INCREMENT,
    client_category ENUM('INDUSTRIAL', 'COMMERCIAL', 'DOMESTIC') UNIQUE NOT NULL,
    survey_fee DECIMAL(10,2) NOT NULL,
    authority_fee DECIMAL(10,2) NOT NULL
);

-- ===============================================
-- 7. TANK TYPES
-- ===============================================
CREATE TABLE tank_types (
    tank_type_id INT PRIMARY KEY AUTO_INCREMENT,
    capacity_liters INT NOT NULL,
    base_cost DECIMAL(10,2) NOT NULL,
    description VARCHAR(100),
    is_active BOOLEAN DEFAULT TRUE
);

-- ===============================================
-- 8. PLUMBING PIPE TYPES
-- ===============================================
CREATE TABLE pipe_types (
    pipe_type_id INT PRIMARY KEY AUTO_INCREMENT,
    pipe_name VARCHAR(50) NOT NULL,
    cost_per_meter DECIMAL(10,2) NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE
);

-- ===============================================
-- 9. MAIN TRANSACTIONS/ORDERS TABLE
-- ===============================================
CREATE TABLE transactions (
    transaction_id INT PRIMARY KEY AUTO_INCREMENT,
    transaction_no VARCHAR(30) UNIQUE NOT NULL,
    client_id VARCHAR(20) NOT NULL,
    transaction_date DATE NOT NULL,
    
    -- Drilling Details
    drilling_type_id INT,
    borehole_depth DECIMAL(10,2),
    drilling_cost DECIMAL(10,2) DEFAULT 0,
    
    -- Pump Details
    pump_type_id INT,
    tank_height DECIMAL(10,2),
    pump_cost DECIMAL(10,2) DEFAULT 0,
    
    -- Tank Details
    tank_type_id INT,
    tank_cost DECIMAL(10,2) DEFAULT 0,
    
    -- Plumbing Details
    has_plumbing BOOLEAN DEFAULT FALSE,
    plumbing_cost DECIMAL(10,2) DEFAULT 0,
    
    -- Fees
    survey_fee DECIMAL(10,2) DEFAULT 0,
    authority_fee DECIMAL(10,2) DEFAULT 0,
    
    -- Totals
    subtotal DECIMAL(10,2) NOT NULL,
    tax_amount DECIMAL(10,2) NOT NULL,
    grand_total DECIMAL(10,2) NOT NULL,
    
    -- Payment Status
    payment_status ENUM('NOT_PAID', 'PARTIALLY_PAID', 'PAID') DEFAULT 'NOT_PAID',
    amount_paid DECIMAL(10,2) DEFAULT 0,
    balance DECIMAL(10,2),
    
    -- Job Status
    job_status ENUM('PENDING_SURVEY', 'APPROVED', 'IN_PROGRESS', 'COMPLETED') DEFAULT 'PENDING_SURVEY',
    
    notes TEXT,
    created_by INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (client_id) REFERENCES clients(client_id),
    FOREIGN KEY (drilling_type_id) REFERENCES drilling_types(drilling_type_id),
    FOREIGN KEY (pump_type_id) REFERENCES pump_types(pump_type_id),
    FOREIGN KEY (tank_type_id) REFERENCES tank_types(tank_type_id),
    FOREIGN KEY (created_by) REFERENCES users(user_id),
    
    INDEX idx_client (client_id),
    INDEX idx_date (transaction_date),
    INDEX idx_payment_status (payment_status),
    INDEX idx_job_status (job_status)
);

-- ===============================================
-- 10. PLUMBING DETAILS TABLE
-- ===============================================
CREATE TABLE plumbing_details (
    plumbing_id INT PRIMARY KEY AUTO_INCREMENT,
    transaction_id INT NOT NULL,
    pipe_type_id INT NOT NULL,
    pipe_diameter DECIMAL(5,2) NOT NULL,
    pipe_length DECIMAL(10,2) NOT NULL,
    number_of_outlets INT NOT NULL,
    cost DECIMAL(10,2) NOT NULL,
    
    FOREIGN KEY (transaction_id) REFERENCES transactions(transaction_id) ON DELETE CASCADE,
    FOREIGN KEY (pipe_type_id) REFERENCES pipe_types(pipe_type_id)
);

-- ===============================================
-- 11. PAYMENTS TABLE
-- ===============================================
CREATE TABLE payments (
    payment_id INT PRIMARY KEY AUTO_INCREMENT,
    transaction_id INT NOT NULL,
    payment_date DATE NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    payment_method ENUM('CASH', 'MPESA', 'BANK_TRANSFER', 'CHEQUE') NOT NULL,
    reference_number VARCHAR(50),
    received_by INT,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (transaction_id) REFERENCES transactions(transaction_id),
    FOREIGN KEY (received_by) REFERENCES users(user_id),
    INDEX idx_transaction (transaction_id),
    INDEX idx_date (payment_date)
);

-- ===============================================
-- 12. AUDIT LOG TABLE
-- ===============================================
CREATE TABLE audit_log (
    log_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT,
    action VARCHAR(50) NOT NULL,
    table_name VARCHAR(50),
    record_id VARCHAR(50),
    description TEXT,
    ip_address VARCHAR(45),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    INDEX idx_user (user_id),
    INDEX idx_action (action),
    INDEX idx_date (created_at)
);

-- ===============================================
-- 13. SYSTEM SETTINGS TABLE
-- ===============================================
CREATE TABLE system_settings (
    setting_id INT PRIMARY KEY AUTO_INCREMENT,
    setting_key VARCHAR(50) UNIQUE NOT NULL,
    setting_value TEXT,
    description TEXT,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- ===============================================
-- INSERT DEFAULT DATA
-- ===============================================

-- Default Admin User (username: user1, password: user1234)
INSERT INTO users (username, password, full_name, role, email, phone, is_active) VALUES
('user1', 'user1234', 'System Administrator', 'ADMIN', 'admin@uzima.co.ke', '0700000000', TRUE);

-- Drilling Types (Table 2)
INSERT INTO drilling_types (drilling_name, down_payment, description) VALUES
('SYMMETRIC', 130000.00, 'Symmetric drilling service'),
('CORE', 225000.00, 'Core drilling service'),
('GEO_TECHNICAL', 335000.00, 'Geo-technical drilling service');

-- Pump Types (Table 3)
INSERT INTO pump_types (pump_name, base_cost, description) VALUES
('SUBMERSIBLE_ELECTRIC', 90000.00, 'Submersible electric pump'),
('SOLAR', 65000.00, 'Solar powered pump'),
('HAND', 30000.00, 'Hand pump');

-- Depth and Height Charges (Table 4)
INSERT INTO depth_height_charges (range_start, range_end, cost_per_meter, description) VALUES
(1, 100, 1000.00, '1-100 meters'),
(101, 200, 1500.00, '101-200 meters'),
(201, 300, 2000.00, '201-300 meters'),
(301, NULL, 2500.00, 'Over 300 meters');

-- Survey and Authority Fees (Table 1)
INSERT INTO survey_authority_fees (client_category, survey_fee, authority_fee) VALUES
('INDUSTRIAL', 20000.00, 50000.00),
('COMMERCIAL', 15000.00, 30000.00),
('DOMESTIC', 7000.00, 10000.00);

-- Tank Types
INSERT INTO tank_types (capacity_liters, base_cost, description) VALUES
(1000, 15000.00, '1000 liters tank'),
(2000, 28000.00, '2000 liters tank'),
(5000, 65000.00, '5000 liters tank'),
(10000, 125000.00, '10000 liters tank');

-- Pipe Types
INSERT INTO pipe_types (pipe_name, cost_per_meter, description) VALUES
('PVC', 500.00, 'PVC pipes'),
('HDPE', 800.00, 'High-Density Polyethylene pipes'),
('GALVANIZED', 1200.00, 'Galvanized steel pipes');

-- System Settings
INSERT INTO system_settings (setting_key, setting_value, description) VALUES
('TAX_RATE', '0.16', 'Tax rate (16%)'),
('COMPANY_NAME', 'Uzima Borehole Drilling Company', 'Company name'),
('COMPANY_ADDRESS', 'P.O. Box 12345, Nairobi, Kenya', 'Company address'),
('COMPANY_PHONE', '+254 700 000 000', 'Company phone'),
('COMPANY_EMAIL', 'info@uzimaborehole.co.ke', 'Company email');

-- Test Customers
INSERT INTO clients (client_id, full_name, physical_address, phone_number, email, 
    client_category, borehole_location, county, registration_date, password, is_active, created_by) VALUES
('DOM-2025-0001', 'John Kamau', '123 Kenyatta Avenue, Nairobi', '0722123456', 'john.kamau@email.com',
    'DOMESTIC', 'Kiambu Road, Near Total Petrol Station', 'Nairobi', CURDATE(), 'test123', TRUE, 1),
('COM-2025-0001', 'Jane Wanjiku', '456 Moi Avenue, Mombasa', '0733987654', 'jane.wanjiku@company.co.ke',
    'COMMERCIAL', 'Industrial Area, Plot 789', 'Mombasa', CURDATE(), 'test123', TRUE, 1),
('UZM-IND-001', 'ABC Manufacturing Ltd', 'Industrial Area, Nairobi', '+254712345678', 'info@abc.co.ke', 
    'INDUSTRIAL', 'Plot 45, Industrial Area', 'Nairobi', '2026-01-15', NULL, TRUE, 1);

-- ===============================================
-- VIEWS FOR REPORTING
-- ===============================================

-- Client Invoice View
CREATE VIEW vw_client_invoices AS
SELECT 
    t.transaction_no,
    t.transaction_date,
    c.client_id,
    c.full_name AS client_name,
    c.client_category,
    c.phone_number,
    dt.drilling_name,
    t.borehole_depth,
    pt.pump_name,
    t.tank_height,
    tt.capacity_liters AS tank_capacity,
    t.drilling_cost,
    t.pump_cost,
    t.tank_cost,
    t.plumbing_cost,
    t.survey_fee,
    t.authority_fee,
    t.subtotal,
    t.tax_amount,
    t.grand_total,
    t.payment_status,
    t.amount_paid,
    t.balance,
    t.job_status
FROM transactions t
JOIN clients c ON t.client_id = c.client_id
LEFT JOIN drilling_types dt ON t.drilling_type_id = dt.drilling_type_id
LEFT JOIN pump_types pt ON t.pump_type_id = pt.pump_type_id
LEFT JOIN tank_types tt ON t.tank_type_id = tt.tank_type_id;

-- Revenue Summary View
CREATE VIEW vw_revenue_summary AS
SELECT 
    COUNT(DISTINCT client_id) AS total_clients,
    COUNT(*) AS total_transactions,
    SUM(drilling_cost) AS total_drilling_revenue,
    SUM(pump_cost) AS total_pump_revenue,
    SUM(tank_cost) AS total_tank_revenue,
    SUM(plumbing_cost) AS total_plumbing_revenue,
    SUM(survey_fee) AS total_survey_fees,
    SUM(authority_fee) AS total_authority_fees,
    SUM(subtotal) AS total_subtotal,
    SUM(tax_amount) AS total_tax_collected,
    SUM(grand_total) AS total_revenue,
    SUM(amount_paid) AS total_paid,
    SUM(balance) AS total_outstanding
FROM transactions;

-- ===============================================
-- STORED PROCEDURES
-- ===============================================

DELIMITER //

-- Calculate Depth/Height Cost
CREATE FUNCTION calculate_depth_cost(depth DECIMAL(10,2))
RETURNS DECIMAL(10,2)
DETERMINISTIC
BEGIN
    DECLARE total_cost DECIMAL(10,2) DEFAULT 0;
    
    IF depth <= 100 THEN
        SET total_cost = depth * 1000;
    ELSEIF depth <= 200 THEN
        SET total_cost = (100 * 1000) + ((depth - 100) * 1500);
    ELSEIF depth <= 300 THEN
        SET total_cost = (100 * 1000) + (100 * 1500) + ((depth - 200) * 2000);
    ELSE
        SET total_cost = (100 * 1000) + (100 * 1500) + (100 * 2000) + ((depth - 300) * 2500);
    END IF;
    
    RETURN total_cost;
END//

DELIMITER ;

-- ===============================================
-- INDEXES FOR PERFORMANCE
-- ===============================================
CREATE INDEX idx_transaction_client_date ON transactions(client_id, transaction_date);
CREATE INDEX idx_payment_transaction_date ON payments(transaction_id, payment_date);

-- ===============================================
-- COMPLETION MESSAGE
-- ===============================================
SELECT 'Database setup completed successfully!' AS Status;
SELECT 'Login credentials:' AS Info;
SELECT 'Staff: username=user1, password=user1234' AS Staff_Login;
SELECT 'Test Customer: client_id=DOM-2025-0001, password=test123' AS Customer_Login;
