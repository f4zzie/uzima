-- ============================================================================
-- UZIMA BOREHOLE DRILLING SYSTEM - DATABASE SCHEMA
-- ============================================================================
-- Complete database setup script for the UZIMA Borehole Drilling System
-- This script creates all tables, relationships, and initial data
-- 
-- Database: javalearning
-- Version: 1.0
-- Last Updated: November 23, 2025
-- ============================================================================

-- Create and use database
CREATE DATABASE IF NOT EXISTS javalearning;
USE javalearning;

-- ============================================================================
-- TABLE DEFINITIONS
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Clients Table
-- Stores customer/client information
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS clients (
    client_id INT PRIMARY KEY AUTO_INCREMENT,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(15) NOT NULL,
    address VARCHAR(255) NOT NULL,
    category ENUM('Individual', 'Commercial', 'NGO', 'Government') NOT NULL,
    borehole_location VARCHAR(255),
    password VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------------------------------------------------------
-- Drilling Services Table
-- Available drilling service types with pricing
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS drilling_services (
    drilling_id INT PRIMARY KEY AUTO_INCREMENT,
    service_type ENUM('Symmetric', 'Core', 'Geo-Technical') NOT NULL,
    down_payment_fee DECIMAL(10, 2) NOT NULL,
    description VARCHAR(255),
    UNIQUE KEY uk_service_type (service_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------------------------------------------------------
-- Pump Services Table
-- Available pump installation options with costs
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS pump_services (
    pump_id INT PRIMARY KEY AUTO_INCREMENT,
    pump_type ENUM('Submersible Electric', 'Solar', 'Hand') NOT NULL,
    cost DECIMAL(10, 2) NOT NULL,
    description VARCHAR(255),
    UNIQUE KEY uk_pump_type (pump_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------------------------------------------------------
-- Plumbing Services Table
-- Plumbing service configurations and pricing
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS plumbing_services (
    plumbing_id INT PRIMARY KEY AUTO_INCREMENT,
    pipe_type VARCHAR(100),
    pipe_diameter_inches INT,
    pipe_length_meters INT,
    number_of_outlets INT,
    cost_per_meter DECIMAL(10, 2),
    description VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------------------------------------------------------
-- Tank Installation Table
-- Water tank specifications and pricing
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS tank_installation (
    tank_id INT PRIMARY KEY AUTO_INCREMENT,
    capacity_litres INT NOT NULL,
    cost_per_litre DECIMAL(10, 2) NOT NULL,
    description VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------------------------------------------------------
-- Depth and Height Charges Table
-- Tiered pricing based on borehole depth ranges
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS depth_height_charges (
    charge_id INT PRIMARY KEY AUTO_INCREMENT,
    depth_min INT NOT NULL,
    depth_max INT NOT NULL,
    cost_per_meter DECIMAL(10, 2) NOT NULL,
    INDEX idx_depth_range (depth_min, depth_max)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------------------------------------------------------
-- Fees Table
-- Survey and local authority fees by client category
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS fees (
    fee_id INT PRIMARY KEY AUTO_INCREMENT,
    client_category ENUM('Individual', 'Commercial', 'NGO', 'Government') NOT NULL,
    survey_fee DECIMAL(10, 2) NOT NULL,
    local_authority_fee DECIMAL(10, 2) NOT NULL,
    UNIQUE KEY uk_category (client_category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------------------------------------------------------
-- Service Requests Table
-- Customer service requests with all selections
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS service_requests (
    request_id INT PRIMARY KEY AUTO_INCREMENT,
    client_id INT NOT NULL,
    drilling_id INT,
    pump_id INT,
    plumbing_id INT,
    tank_id INT,
    depth_meters INT,
    height_meters INT,
    request_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        status ENUM('Pending', 'Approved', 'In Progress', 'Completed', 'Cancelled') DEFAULT 'Pending',
        scheduled_date TIMESTAMP NULL DEFAULT NULL,
    FOREIGN KEY (client_id) REFERENCES clients(client_id) ON DELETE CASCADE,
    FOREIGN KEY (drilling_id) REFERENCES drilling_services(drilling_id) ON DELETE SET NULL,
    FOREIGN KEY (pump_id) REFERENCES pump_services(pump_id) ON DELETE SET NULL,
    FOREIGN KEY (plumbing_id) REFERENCES plumbing_services(plumbing_id) ON DELETE SET NULL,
    FOREIGN KEY (tank_id) REFERENCES tank_installation(tank_id) ON DELETE SET NULL,
    INDEX idx_client (client_id),
    INDEX idx_status (status),
    INDEX idx_date (request_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------------------------------------------------------
-- Invoices Table
-- Billing information with complete cost breakdown
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS invoices (
    invoice_id INT PRIMARY KEY AUTO_INCREMENT,
    request_id INT NOT NULL,
    client_id INT NOT NULL,
    survey_fee DECIMAL(10, 2) DEFAULT 0.00,
    local_authority_fee DECIMAL(10, 2) DEFAULT 0.00,
    drilling_cost DECIMAL(10, 2) DEFAULT 0.00,
    pump_cost DECIMAL(10, 2) DEFAULT 0.00,
    plumbing_cost DECIMAL(10, 2) DEFAULT 0.00,
    tank_cost DECIMAL(10, 2) DEFAULT 0.00,
    depth_height_charge DECIMAL(10, 2) DEFAULT 0.00,
    subtotal DECIMAL(10, 2) NOT NULL,
    tax_amount DECIMAL(10, 2) NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    invoice_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    payment_status ENUM('Pending', 'Partial', 'Paid', 'Overdue') DEFAULT 'Pending',
    FOREIGN KEY (request_id) REFERENCES service_requests(request_id) ON DELETE CASCADE,
    FOREIGN KEY (client_id) REFERENCES clients(client_id) ON DELETE CASCADE,
    INDEX idx_client (client_id),
    INDEX idx_payment_status (payment_status),
    INDEX idx_invoice_date (invoice_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------------------------------------------------------
-- Users Table
-- System users for authentication and access control
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(100) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('Admin', 'Staff', 'Manager', 'Viewer') DEFAULT 'Staff',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================================
-- INITIAL DATA POPULATION
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Drilling Services - 3 types
-- ----------------------------------------------------------------------------
INSERT INTO drilling_services (service_type, down_payment_fee, description) VALUES
('Symmetric', 130000.00, 'Standard symmetric rotary drilling suitable for most soil conditions'),
('Core', 225000.00, 'Core drilling for precise sampling and geological analysis'),
('Geo-Technical', 335000.00, 'Advanced geo-technical drilling for complex soil investigations')
ON DUPLICATE KEY UPDATE 
    down_payment_fee = VALUES(down_payment_fee),
    description = VALUES(description);

-- ----------------------------------------------------------------------------
-- Pump Services - 3 types
-- ----------------------------------------------------------------------------
INSERT INTO pump_services (pump_type, cost, description) VALUES
('Submersible Electric', 90000.00, 'High-efficiency submersible electric pump with automatic controls'),
('Solar', 65000.00, 'Solar-powered pump system with battery backup'),
('Hand', 30000.00, 'Manual hand pump for basic water extraction')
ON DUPLICATE KEY UPDATE 
    cost = VALUES(cost),
    description = VALUES(description);

-- ----------------------------------------------------------------------------
-- Depth and Height Charges - 4 tiers
-- Pricing per meter based on depth ranges
-- ----------------------------------------------------------------------------
INSERT INTO depth_height_charges (depth_min, depth_max, cost_per_meter) VALUES
(1, 100, 1000.00),      -- 1-100 meters: KES 1,000 per meter
(101, 200, 1500.00),    -- 101-200 meters: KES 1,500 per meter
(201, 300, 2000.00),    -- 201-300 meters: KES 2,000 per meter
(301, 999999, 2500.00)  -- 300+ meters: KES 2,500 per meter
ON DUPLICATE KEY UPDATE 
    cost_per_meter = VALUES(cost_per_meter);

-- ----------------------------------------------------------------------------
-- Category Fees - Survey and Local Authority fees by client category
-- ----------------------------------------------------------------------------
INSERT INTO fees (client_category, survey_fee, local_authority_fee) VALUES
('Individual', 7000.00, 10000.00),      -- Individual: KES 7K + 10K
('Commercial', 15000.00, 30000.00),     -- Commercial: KES 15K + 30K
('NGO', 10000.00, 15000.00),            -- NGO: KES 10K + 15K
('Government', 20000.00, 50000.00)      -- Government: KES 20K + 50K
ON DUPLICATE KEY UPDATE 
    survey_fee = VALUES(survey_fee),
    local_authority_fee = VALUES(local_authority_fee);

-- ============================================================================
-- TEST DATA (For development and testing)
-- ============================================================================

-- Test Clients with passwords
-- Password for all test accounts: 'password123'
INSERT INTO clients (full_name, email, phone, address, category, borehole_location, password) VALUES
('Admin User', 'admin@uzima.com', '+254700000001', 'Nairobi CBD, Nairobi', 'Government', 'Nairobi County', 'password123'),
('John Kamau', 'john.kamau@test.com', '+254701000001', 'Karen, Nairobi', 'Individual', 'Nairobi County', 'password123'),
('Mary Wanjiku', 'mary.wanjiku@test.com', '+254702000001', 'Kiambu Town', 'Commercial', 'Kiambu County', 'password123'),
('Peter Ochieng', 'peter.ochieng@test.com', '+254703000001', 'Kisumu City', 'NGO', 'Kisumu County', 'password123')
ON DUPLICATE KEY UPDATE full_name = VALUES(full_name);

-- Test Admin User for system access
INSERT INTO users (username, email, password, role) VALUES
('admin', 'admin@uzima.com', 'password123', 'Admin'),
('staff', 'staff@uzima.com', 'password123', 'Staff'),
('manager', 'manager@uzima.com', 'password123', 'Manager')
ON DUPLICATE KEY UPDATE username = VALUES(username);

-- ============================================================================
-- DATABASE VIEWS (Optional - for reporting)
-- ============================================================================

-- View for invoice summary with client information
CREATE OR REPLACE VIEW invoice_summary AS
SELECT 
    i.invoice_id,
    i.invoice_date,
    c.full_name AS client_name,
    c.email AS client_email,
    c.category AS client_category,
    i.subtotal,
    i.tax_amount,
    i.total_amount,
    i.payment_status
FROM invoices i
JOIN clients c ON i.client_id = c.client_id
ORDER BY i.invoice_date DESC;

-- View for service request details
CREATE OR REPLACE VIEW service_request_details AS
SELECT 
    sr.request_id,
    sr.request_date,
    c.full_name AS client_name,
    c.category AS client_category,
    ds.service_type AS drilling_type,
    ps.pump_type,
    sr.depth_meters,
    sr.height_meters,
    sr.status
FROM service_requests sr
JOIN clients c ON sr.client_id = c.client_id
LEFT JOIN drilling_services ds ON sr.drilling_id = ds.drilling_id
LEFT JOIN pump_services ps ON sr.pump_id = ps.pump_id
ORDER BY sr.request_date DESC;

-- ============================================================================
-- VERIFICATION QUERIES
-- ============================================================================

-- Verify table creation
SELECT 
    'Tables Created Successfully' AS Status,
    COUNT(*) AS TableCount 
FROM information_schema.tables 
WHERE table_schema = 'javalearning';

-- Verify initial data
SELECT 'Drilling Services' AS DataType, COUNT(*) AS RecordCount FROM drilling_services
UNION ALL
SELECT 'Pump Services', COUNT(*) FROM pump_services
UNION ALL
SELECT 'Depth Charges', COUNT(*) FROM depth_height_charges
UNION ALL
SELECT 'Category Fees', COUNT(*) FROM fees;

-- ============================================================================
-- SETUP COMPLETE
-- ============================================================================
SELECT '✓ UZIMA Database Setup Complete!' AS Message;
SELECT 'Database: javalearning' AS Info;
SELECT 'Tables: 11 created' AS Info;
SELECT 'Initial Data: Loaded' AS Info;
