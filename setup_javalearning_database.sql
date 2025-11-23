-- UZIMA Borehole Drilling System Database Schema for javalearning
-- Execute this SQL to set up all required tables

-- Clients Table
CREATE TABLE IF NOT EXISTS clients (
    client_id INT PRIMARY KEY AUTO_INCREMENT,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(15) NOT NULL,
    address VARCHAR(255) NOT NULL,
    category ENUM('Industrial', 'Commercial', 'Domestic') NOT NULL,
    borehole_location VARCHAR(255),
    password VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Drilling Services Table
CREATE TABLE IF NOT EXISTS drilling_services (
    drilling_id INT PRIMARY KEY AUTO_INCREMENT,
    service_type ENUM('Symmetric', 'Core', 'Geo-Technical') NOT NULL,
    down_payment_fee DECIMAL(10, 2) NOT NULL,
    description VARCHAR(255)
);

-- Pump Services Table
CREATE TABLE IF NOT EXISTS pump_services (
    pump_id INT PRIMARY KEY AUTO_INCREMENT,
    pump_type ENUM('Submersible Electric', 'Solar', 'Hand') NOT NULL,
    cost DECIMAL(10, 2) NOT NULL,
    description VARCHAR(255)
);

-- Plumbing Services Table
CREATE TABLE IF NOT EXISTS plumbing_services (
    plumbing_id INT PRIMARY KEY AUTO_INCREMENT,
    pipe_type VARCHAR(100),
    pipe_diameter_inches INT,
    pipe_length_meters INT,
    number_of_outlets INT,
    cost_per_meter DECIMAL(10, 2),
    description VARCHAR(255)
);

-- Tank Installation Table
CREATE TABLE IF NOT EXISTS tank_installation (
    tank_id INT PRIMARY KEY AUTO_INCREMENT,
    capacity_litres INT NOT NULL,
    cost_per_litre DECIMAL(10, 2) NOT NULL,
    description VARCHAR(255)
);

-- Depth and Height Charges Table
CREATE TABLE IF NOT EXISTS depth_height_charges (
    charge_id INT PRIMARY KEY AUTO_INCREMENT,
    depth_min INT,
    depth_max INT,
    cost_per_meter DECIMAL(10, 2) NOT NULL
);

-- Fees Table (Survey and Local Authority)
CREATE TABLE IF NOT EXISTS fees (
    fee_id INT PRIMARY KEY AUTO_INCREMENT,
    client_category ENUM('Industrial', 'Commercial', 'Domestic') NOT NULL,
    survey_fee DECIMAL(10, 2) NOT NULL,
    local_authority_fee DECIMAL(10, 2) NOT NULL
);

-- Service Requests Table
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
    FOREIGN KEY (client_id) REFERENCES clients(client_id) ON DELETE CASCADE,
    FOREIGN KEY (drilling_id) REFERENCES drilling_services(drilling_id) ON DELETE SET NULL,
    FOREIGN KEY (pump_id) REFERENCES pump_services(pump_id) ON DELETE SET NULL,
    FOREIGN KEY (plumbing_id) REFERENCES plumbing_services(plumbing_id) ON DELETE SET NULL,
    FOREIGN KEY (tank_id) REFERENCES tank_installation(tank_id) ON DELETE SET NULL
);

-- Invoices Table
CREATE TABLE IF NOT EXISTS invoices (
    invoice_id INT PRIMARY KEY AUTO_INCREMENT,
    request_id INT NOT NULL,
    client_id INT NOT NULL,
    survey_fee DECIMAL(10, 2),
    local_authority_fee DECIMAL(10, 2),
    drilling_cost DECIMAL(10, 2),
    pump_cost DECIMAL(10, 2),
    plumbing_cost DECIMAL(10, 2),
    tank_cost DECIMAL(10, 2),
    depth_charges DECIMAL(10, 2),
    tax DECIMAL(10, 2),
    total_cost DECIMAL(10, 2),
    invoice_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    due_date DATE,
    payment_status ENUM('Unpaid', 'Paid', 'Overdue') DEFAULT 'Unpaid',
    FOREIGN KEY (request_id) REFERENCES service_requests(request_id) ON DELETE CASCADE,
    FOREIGN KEY (client_id) REFERENCES clients(client_id) ON DELETE CASCADE
);

-- Transactions Table
CREATE TABLE IF NOT EXISTS transactions (
    transaction_id INT PRIMARY KEY AUTO_INCREMENT,
    invoice_id INT NOT NULL,
    client_id INT NOT NULL,
    payment_amount DECIMAL(10, 2) NOT NULL,
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    payment_method VARCHAR(50),
    FOREIGN KEY (invoice_id) REFERENCES invoices(invoice_id) ON DELETE CASCADE,
    FOREIGN KEY (client_id) REFERENCES clients(client_id) ON DELETE CASCADE
);

-- Sample Admin Client (for testing admin panel)
INSERT INTO clients (full_name, email, phone, address, category, borehole_location, password) 
VALUES ('Admin User', 'admin@uzima.com', '0700000001', 'UZIMA Office', 'Industrial', 'Nairobi', 'f8a8a3e3e3e3e3e3e3e3e3e3e3e3e3e3')
ON DUPLICATE KEY UPDATE password='f8a8a3e3e3e3e3e3e3e3e3e3e3e3e3e3';

-- Sample Clients
INSERT INTO clients (full_name, email, phone, address, category, borehole_location, password) 
VALUES 
('John Doe', 'john@example.com', '0712345678', '123 Main St', 'Domestic', 'Nairobi', 'hashed_password_1'),
('Sarah Johnson', 'sarah@example.com', '0722345678', '456 Oak Ave', 'Commercial', 'Mombasa', 'hashed_password_2'),
('Mike Smith', 'mike@example.com', '0732345678', '789 Pine Rd', 'Industrial', 'Kisumu', 'hashed_password_3')
ON DUPLICATE KEY UPDATE full_name=VALUES(full_name);

-- Sample Drilling Services
INSERT INTO drilling_services (service_type, down_payment_fee, description) 
VALUES 
('Symmetric', 50000.00, 'Standard symmetric drilling'),
('Core', 75000.00, 'Core drilling service'),
('Geo-Technical', 100000.00, 'Geo-technical drilling')
ON DUPLICATE KEY UPDATE down_payment_fee=VALUES(down_payment_fee);

-- Sample Pump Services
INSERT INTO pump_services (pump_type, cost, description) 
VALUES 
('Submersible Electric', 35000.00, 'Electric submersible pump'),
('Solar', 50000.00, 'Solar-powered pump'),
('Hand', 5000.00, 'Manual hand pump')
ON DUPLICATE KEY UPDATE cost=VALUES(cost);

-- Sample Fees
INSERT INTO fees (client_category, survey_fee, local_authority_fee) 
VALUES 
('Domestic', 5000.00, 2000.00),
('Commercial', 10000.00, 5000.00),
('Industrial', 20000.00, 10000.00)
ON DUPLICATE KEY UPDATE survey_fee=VALUES(survey_fee);

-- Verification
SELECT 'Clients Table:' AS Status, COUNT(*) as Count FROM clients;
SELECT 'Drilling Services Table:' AS Status, COUNT(*) as Count FROM drilling_services;
SELECT 'Pump Services Table:' AS Status, COUNT(*) as Count FROM pump_services;
SELECT 'Fees Table:' AS Status, COUNT(*) as Count FROM fees;
