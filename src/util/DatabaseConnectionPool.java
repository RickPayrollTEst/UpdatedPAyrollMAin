package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Enhanced Database Connection Pool for better performance
 * Addresses mentor feedback about database optimization
 */
public class DatabaseConnectionPool {
    private static final Logger LOGGER = Logger.getLogger(DatabaseConnectionPool.class.getName());
    
    private static DatabaseConnectionPool instance;
    private BlockingQueue<Connection> connectionPool;
    private final int INITIAL_POOL_SIZE = 5;
    private final int MAX_POOL_SIZE = 20;
    private final int CONNECTION_TIMEOUT = 30; // seconds
    
    private final String DB_URL;
    private final String DB_USERNAME;
    private final String DB_PASSWORD;
    
    private DatabaseConnectionPool() {
        ConfigurationManager config = ConfigurationManager.getInstance();
        this.DB_URL = config.getDatabaseUrl();
        this.DB_USERNAME = config.getDatabaseUsername();
        this.DB_PASSWORD = config.getDatabasePassword();
        
        initializePool();
    }
    
    public static synchronized DatabaseConnectionPool getInstance() {
        if (instance == null) {
            instance = new DatabaseConnectionPool();
        }
        return instance;
    }
    
    private void initializePool() {
        connectionPool = new LinkedBlockingQueue<>(MAX_POOL_SIZE);
        
        try {
            // Load MySQL driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Create initial connections
            for (int i = 0; i < INITIAL_POOL_SIZE; i++) {
                Connection connection = createNewConnection();
                if (connection != null) {
                    connectionPool.offer(connection);
                }
            }
            
            LOGGER.info("✅ Database connection pool initialized with " + connectionPool.size() + " connections");
            
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "❌ MySQL driver not found", e);
            throw new RuntimeException("MySQL driver not found", e);
        }
    }
    
    private Connection createNewConnection() {
        try {
            Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
            
            // Configure connection
            connection.setAutoCommit(true);
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            
            return connection;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "❌ Failed to create database connection", e);
            return null;
        }
    }
    
    public Connection getConnection() throws SQLException {
        try {
            Connection connection = connectionPool.poll(CONNECTION_TIMEOUT, TimeUnit.SECONDS);
            
            if (connection == null) {
                // Pool is empty, create new connection if under max limit
                if (getTotalConnections() < MAX_POOL_SIZE) {
                    connection = createNewConnection();
                    if (connection == null) {
                        throw new SQLException("Unable to create new database connection");
                    }
                } else {
                    throw new SQLException("Connection pool exhausted. Maximum connections: " + MAX_POOL_SIZE);
                }
            }
            
            // Validate connection
            if (!isConnectionValid(connection)) {
                connection = createNewConnection();
                if (connection == null) {
                    throw new SQLException("Unable to create valid database connection");
                }
            }
            
            return connection;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("Interrupted while waiting for database connection", e);
        }
    }
    
    public void returnConnection(Connection connection) {
        if (connection != null) {
            try {
                if (isConnectionValid(connection)) {
                    // Reset connection state
                    connection.setAutoCommit(true);
                    connection.clearWarnings();
                    
                    // Return to pool
                    if (!connectionPool.offer(connection)) {
                        // Pool is full, close the connection
                        connection.close();
                    }
                } else {
                    // Connection is invalid, close it
                    connection.close();
                }
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "⚠️ Error returning connection to pool", e);
                try {
                    connection.close();
                } catch (SQLException closeException) {
                    LOGGER.log(Level.WARNING, "⚠️ Error closing invalid connection", closeException);
                }
            }
        }
    }
    
    private boolean isConnectionValid(Connection connection) {
        try {
            return connection != null && 
                   !connection.isClosed() && 
                   connection.isValid(5);
        } catch (SQLException e) {
            return false;
        }
    }
    
    public int getAvailableConnections() {
        return connectionPool.size();
    }
    
    public int getTotalConnections() {
        // This is a simplified implementation
        // In a real pool, you'd track all connections (available + in use)
        return connectionPool.size();
    }
    
    public void closeAllConnections() {
        LOGGER.info("🔒 Closing all database connections...");
        
        Connection connection;
        while ((connection = connectionPool.poll()) != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "⚠️ Error closing connection", e);
            }
        }
        
        LOGGER.info("✅ All database connections closed");
    }
    
    public String getPoolStatus() {
        return String.format("Connection Pool Status: Available=%d, Total=%d, Max=%d",
                getAvailableConnections(), getTotalConnections(), MAX_POOL_SIZE);
    }
    
    // Shutdown hook to clean up connections
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (instance != null) {
                instance.closeAllConnections();
            }
        }));
    }
}