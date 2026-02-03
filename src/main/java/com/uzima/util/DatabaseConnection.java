package com.uzima.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Database Connection Manager using HikariCP connection pooling
 * Provides thread-safe database connections for the application
 * Uses lazy initialization - database connection is only attempted when needed
 */
public class DatabaseConnection {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnection.class);
    private static HikariDataSource dataSource;
    private static boolean initialized = false;
    private static String lastError = null;
    
    // Database configuration
    private static final String DB_URL = "jdbc:mysql://localhost:3306/uzima_borehole_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "teddy"; // Set your MySQL password here
    
    // No static block - lazy initialization only when needed
    
    /**
     * Initialize HikariCP connection pool (lazy - called on first use)
     * @return true if initialization successful, false otherwise
     */
    private static synchronized boolean initializeDataSource() {
        if (initialized && dataSource != null) {
            return true;
        }
        
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(DB_URL);
            config.setUsername(DB_USER);
            config.setPassword(DB_PASSWORD);
            
            // Pool configuration
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setConnectionTimeout(10000); // Reduced to 10 seconds for faster feedback
            config.setIdleTimeout(600000);
            config.setMaxLifetime(1800000);
            
            // Performance optimizations
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            
            dataSource = new HikariDataSource(config);
            initialized = true;
            lastError = null;
            logger.info("Database connection pool initialized successfully");
            return true;
        } catch (Exception e) {
            lastError = e.getMessage();
            logger.error("Failed to initialize database connection pool", e);
            return false;
        }
    }
    
    /**
     * Get the last database error message
     * @return error message or null if no error
     */
    public static String getLastError() {
        return lastError;
    }
    
    /**
     * Get a connection from the pool (initializes pool on first call)
     * @return Database connection
     * @throws SQLException if connection cannot be established
     */
    public static Connection getConnection() throws SQLException {
        if (!initialized) {
            if (!initializeDataSource()) {
                throw new SQLException("Database connection failed: " + (lastError != null ? lastError : "Unknown error"));
            }
        }
        return dataSource.getConnection();
    }
    
    /**
     * Close the data source (call on application shutdown)
     */
    public static void closeDataSource() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            initialized = false;
            logger.info("Database connection pool closed");
        }
    }
    
    /**
     * Test database connection (initializes pool on first call)
     * @return true if connection is successful
     */
    public static boolean testConnection() {
        try {
            if (!initialized) {
                if (!initializeDataSource()) {
                    return false;
                }
            }
            try (Connection conn = dataSource.getConnection()) {
                return conn != null && !conn.isClosed();
            }
        } catch (SQLException e) {
            lastError = e.getMessage();
            logger.error("Database connection test failed", e);
            return false;
        }
    }
}
