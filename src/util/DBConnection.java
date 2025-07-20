package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Fixed Database Connection Utility Class
 * Provides centralized database connection management
 */
public class DBConnection {
    private static final Logger LOGGER = Logger.getLogger(DBConnection.class.getName());
    
    // Database configuration
    private static final String DB_HOST = "localhost";
    private static final String DB_PORT = "3306";
    private static final String DB_NAME = "aoopdatabase_payroll";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "admin";
    private static final String DB_DRIVER = "com.mysql.cj.jdbc.Driver";
    
    // Connection URL
    private static final String DB_URL = String.format(
        "jdbc:mysql://%s:%s/%s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=UTF-8",
        DB_HOST, DB_PORT, DB_NAME
    );
    
    // Static block to load the driver
    static {
        try {
            Class.forName(DB_DRIVER);
            LOGGER.info("✅ MySQL JDBC Driver loaded successfully");
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "❌ MySQL JDBC Driver not found", e);
            throw new RuntimeException("MySQL JDBC Driver not found. Please add mysql-connector-java to classpath.", e);
        }
    }
    
    /**
     * Get a database connection
     * @return Connection object
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        try {
            Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
            LOGGER.fine("🔗 Database connection established");
            return connection;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "❌ Failed to establish database connection", e);
            throw new SQLException("Database connection failed. Please check:\n" +
                    "1. MySQL server is running\n" +
                    "2. Database '" + DB_NAME + "' exists\n" +
                    "3. Username and password are correct\n" +
                    "4. MySQL service is accessible on " + DB_HOST + ":" + DB_PORT, e);
        }
    }
    
    /**
     * Test database connection
     * @return true if connection is successful, false otherwise
     */
    public static boolean testConnection() {
        try (Connection connection = getConnection()) {
            boolean isValid = connection != null && connection.isValid(5);
            if (isValid) {
                LOGGER.info("✅ Database connection test successful");
            } else {
                LOGGER.warning("⚠️ Database connection test failed - connection invalid");
            }
            return isValid;
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "⚠️ Database connection test failed", e);
            return false;
        }
    }
    
    /**
     * Close connection safely
     * @param connection Connection to close
     */
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
                LOGGER.fine("🔒 Database connection closed");
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "⚠️ Error closing database connection", e);
            }
        }
    }
    
    /**
     * Get database information
     * @return Database connection details (without password)
     */
    public static String getDatabaseInfo() {
        return String.format("Database: %s@%s:%s/%s", 
                DB_USERNAME, DB_HOST, DB_PORT, DB_NAME);
    }
    
    /**
     * Check if database and required tables exist
     * @return true if database is properly set up
     */
    public static boolean isDatabaseSetup() {
        try (Connection connection = getConnection()) {
            // Check if main tables exist
            String[] requiredTables = {
                "employees", "credentials", "attendance", 
                "payroll", "leave_request", "overtime"
            };
            
            for (String table : requiredTables) {
                try {
                    connection.prepareStatement("SELECT 1 FROM " + table + " LIMIT 1").executeQuery();
                } catch (SQLException e) {
                    LOGGER.warning("❌ Required table missing: " + table);
                    return false;
                }
            }
            
            LOGGER.info("✅ Database setup validation passed");
            return true;
            
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "❌ Database setup validation failed", e);
            return false;
        }
    }
    
    // Database configuration getters (for configuration management)
    public static String getHost() { return DB_HOST; }
    public static String getPort() { return DB_PORT; }
    public static String getDatabaseName() { return DB_NAME; }
    public static String getUsername() { return DB_USERNAME; }
    public static String getUrl() { return DB_URL; }
}