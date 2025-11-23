
CREATE DATABASE IF NOT EXISTS javalearning;

DROP TABLE IF EXISTS javalearning.users;


CREATE TABLE javalearning.users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(64),
    password VARCHAR(255) NOT NULL,
    country VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


SHOW TABLES FROM javalearning;

