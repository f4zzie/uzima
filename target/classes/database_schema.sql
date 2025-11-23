-- UZIMA Borehole Drilling System Database Schema
-- Created for managing clients, services, and billing

-- Create database if not exists
CREATE DATABASE IF NOT EXISTS uzima_borehole;
USE uzima_borehole;

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
    depth_height_charge DECIMAL(10, 2),
    subtotal DECIMAL(10, 2) NOT NULL,
    tax_amount DECIMAL(10, 2) NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    invoice_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    payment_status ENUM('Pending', 'Partial', 'Paid', 'Overdue') DEFAULT 'Pending',
    FOREIGN KEY (request_id) REFERENCES service_requests(request_id) ON DELETE CASCADE,
    FOREIGN KEY (client_id) REFERENCES clients(client_id) ON DELETE CASCADE
);

-- Users Table (for login/authentication)
CREATE TABLE IF NOT EXISTS users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(100) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('Admin', 'Staff', 'Manager', 'Viewer') DEFAULT 'Staff',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert Default Drilling Services
INSERT INTO drilling_services (service_type, down_payment_fee, description) VALUES
('Symmetric', 130000, 'Symmetric drilling service'),
('Core', 225000, 'Core drilling service'),
('Geo-Technical', 335000, 'Geo-Technical drilling service');

-- Insert Default Pump Services
INSERT INTO pump_services (pump_type, cost, description) VALUES
('Submersible Electric', 90000, 'Submersible electric pump installation'),
('Solar', 65000, 'Solar pump installation'),
('Hand', 30000, 'Hand pump installation');

-- Insert Depth and Height Charges
INSERT INTO depth_height_charges (depth_min, depth_max, cost_per_meter) VALUES
(1, 100, 1000),
(101, 200, 1500),
(201, 300, 2000),
(301, 999999, 2500);

-- Insert Fees Table
INSERT INTO fees (client_category, survey_fee, local_authority_fee) VALUES
('Industrial', 20000, 50000),
('Commercial', 15000, 30000),
('Domestic', 7000, 10000);
