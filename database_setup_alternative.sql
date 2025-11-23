-- Alternative SQL Script - More Reliable for MySQL Workbench
-- This version doesn't rely on USE statement

-- Create database if it doesn't exist
CREATE DATABASE IF NOT EXISTS javalearning;

-- Drop table if it exists (optional - remove this line if you want to keep existing data)
DROP TABLE IF EXISTS javalearning.users;

-- Create users table (explicitly specifying database name)
CREATE TABLE javalearning.users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(64),
    password VARCHAR(255) NOT NULL,
    country VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Verify table was created
SHOW TABLES FROM javalearning;

