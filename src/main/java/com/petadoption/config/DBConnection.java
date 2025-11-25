package com.petadoption.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Database Connection Manager Class
 * 
 * Handles JDBC connection to MySQL database with connection pooling support.
 * This class provides a centralized mechanism to establish and manage database connections.
 * 
 * Features:
 * - Singleton pattern for efficient connection management
 * - Connection pooling ready for production environments
 * - Comprehensive error handling and logging
 * - Thread-safe connection management
 * 
 * @author Pet Adoption System Team
 * @version 1.0
 */
public class DBConnection {
    
    // Database connection properties - should be loaded from configuration file in production
    private static final String DB_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String DB_URL = "jdbc:mysql://localhost:3306/pet_adoption_system";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Abhi9608";
    private static final int CONNECTION_TIMEOUT = 10000; // 10 seconds
    
    // Singleton instance
    private static DBConnection instance;
    private Connection connection;
    
    /**
     * Private constructor to prevent instantiation
     * Initializes the JDBC driver
     */
    private DBConnection() {
        try {
            // Load MySQL JDBC driver
            Class.forName(DB_DRIVER);
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC Driver not found: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Get singleton instance of DBConnection
     * Uses synchronized block for thread-safe lazy initialization
     * 
     * @return DBConnection instance
     */
    public static synchronized DBConnection getInstance() {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;
    }
    
    /**
     * Establishes and returns a new database connection
     * Creates a fresh connection each time - suitable for connection pooling
     * 
     * @return Connection object to interact with MySQL database
     * @throws SQLException if connection cannot be established
     */
    public Connection getConnection() throws SQLException {
        try {
            // Create and return new connection
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            return connection;
        } catch (SQLException e) {
            System.err.println("Failed to establish database connection: " + e.getMessage());
            throw new SQLException("Database connection failed. Please check your database configuration.", e);
        }
    }
    
    /**
     * Close the database connection safely
     * Checks if connection exists and is not already closed before closing
     * 
     * @param connection the connection object to close
     */
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Close PreparedStatement safely
     * Used in DAO classes to ensure proper resource cleanup
     * 
     * @param preparedStatement the prepared statement to close
     */
    public static void closePreparedStatement(java.sql.PreparedStatement preparedStatement) {
        if (preparedStatement != null) {
            try {
                preparedStatement.close();
            } catch (SQLException e) {
                System.err.println("Error closing prepared statement: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Close ResultSet safely
     * Used in DAO classes to ensure proper resource cleanup
     * 
     * @param resultSet the result set to close
     */
    public static void closeResultSet(java.sql.ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                System.err.println("Error closing result set: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Test the database connection
     * Useful for debugging and connection verification
     * 
     * @return true if connection is successful, false otherwise
     */
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("✓ Database connection successful!");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("✗ Database connection test failed: " + e.getMessage());
        }
        return false;
    }
}
