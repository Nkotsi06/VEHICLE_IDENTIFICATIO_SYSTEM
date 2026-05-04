package database;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Database connection manager with connection pooling and transaction support.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class DatabaseConnection {

    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());
    private static DatabaseConnection instance;

    // Connection pool settings
    private static final int MAX_POOL_SIZE = 10;
    private static final AtomicInteger activeConnections = new AtomicInteger(0);

    private Connection connection;
    private String url;
    private String username;
    private String password;
    private boolean isTransactionActive = false;

    // Default values (should be overridden by properties file)
    private static final String DEFAULT_URL = "jdbc:postgresql://localhost:5432/vehicle_db";
    private static final String DEFAULT_USER = "postgres";
    private static final String DEFAULT_PASSWORD = "1234567"; // Empty - user must set

    private DatabaseConnection() {
        loadProperties();
        initializeConnectionPool();
    }

    /**
     * Gets the singleton instance.
     *
     * @return DatabaseConnection instance
     */
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /**
     * Loads database properties from config file.
     */
    private void loadProperties() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config/database.properties")) {
            Properties props = new Properties();
            if (input != null) {
                props.load(input);
                this.url = props.getProperty("db.url", DEFAULT_URL);
                this.username = props.getProperty("db.user", DEFAULT_USER);
                this.password = props.getProperty("db.password", DEFAULT_PASSWORD);
                LOGGER.info("Database properties loaded from config file");
            } else {
                LOGGER.warning("database.properties not found, using defaults. Please create config/database.properties");
                this.url = DEFAULT_URL;
                this.username = DEFAULT_USER;
                this.password = DEFAULT_PASSWORD;
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to load database properties, using defaults", e);
            this.url = DEFAULT_URL;
            this.username = DEFAULT_USER;
            this.password = DEFAULT_PASSWORD;
        }
    }

    /**
     * Initializes the connection pool.
     */
    private void initializeConnectionPool() {
        // Pre-warm connections
        for (int i = 0; i < 3; i++) {
            try {
                Connection testConn = createNewConnection();
                if (testConn != null && !testConn.isClosed()) {
                    testConn.close();
                }
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Failed to pre-warm connection", e);
            }
        }
        LOGGER.info("Database connection pool initialized");
    }

    /**
     * Creates a new database connection.
     *
     * @return new Connection
     * @throws SQLException if connection fails
     */
    private Connection createNewConnection() throws SQLException {
        try {
            // Ensure driver is loaded
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(url, username, password);
            conn.setAutoCommit(true);
            return conn;
        } catch (ClassNotFoundException e) {
            throw new SQLException("PostgreSQL JDBC Driver not found. Please add postgresql jar to classpath.", e);
        }
    }

    /**
     * Gets a database connection.
     *
     * @return database connection
     * @throws SQLException if connection fails
     */
    public Connection getConnection() throws SQLException {
        // Check if in transaction - return the same connection
        if (isTransactionActive && connection != null && !connection.isClosed()) {
            return connection;
        }

        // Create new connection if needed
        if (connection == null || connection.isClosed()) {
            connection = createNewConnection();
            activeConnections.incrementAndGet();
            LOGGER.fine("New connection created. Active: " + activeConnections.get());
        }

        return connection;
    }

    /**
     * Tests the database connection.
     *
     * @return true if connection is successful
     */
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            boolean isValid = conn != null && !conn.isClosed() && conn.isValid(5);
            if (isValid) {
                LOGGER.info("Database connection test successful");
            } else {
                LOGGER.warning("Database connection test failed");
            }
            return isValid;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database connection test failed", e);
            return false;
        }
    }

    /**
     * Closes the current connection.
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                activeConnections.decrementAndGet();
                LOGGER.fine("Connection closed. Active: " + activeConnections.get());
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error closing connection", e);
        } finally {
            connection = null;
        }
    }

    /**
     * Begins a transaction.
     *
     * @throws SQLException if transaction cannot be started
     */
    public void beginTransaction() throws SQLException {
        if (isTransactionActive) {
            throw new SQLException("Transaction already active. Commit or rollback first.");
        }

        Connection conn = getConnection();
        conn.setAutoCommit(false);
        isTransactionActive = true;
        LOGGER.fine("Transaction started");
    }

    /**
     * Commits the current transaction.
     *
     * @throws SQLException if commit fails
     */
    public void commitTransaction() throws SQLException {
        if (!isTransactionActive) {
            throw new SQLException("No active transaction to commit");
        }

        Connection conn = getConnection();
        conn.commit();
        conn.setAutoCommit(true);
        isTransactionActive = false;
        LOGGER.fine("Transaction committed");
    }

    /**
     * Rolls back the current transaction.
     *
     * @throws SQLException if rollback fails
     */
    public void rollbackTransaction() throws SQLException {
        if (!isTransactionActive) {
            throw new SQLException("No active transaction to rollback");
        }

        Connection conn = getConnection();
        conn.rollback();
        conn.setAutoCommit(true);
        isTransactionActive = false;
        LOGGER.fine("Transaction rolled back");
    }

    /**
     * Checks if a transaction is active.
     *
     * @return true if transaction active
     */
    public boolean isTransactionActive() {
        return isTransactionActive;
    }

    /**
     * Closes ResultSet, Statement, and optionally Connection (if not the main one).
     *
     * @param rs   ResultSet to close
     * @param stmt Statement to close
     * @param conn Connection to close (if not the main connection)
     */
    public void closeResources(ResultSet rs, Statement stmt, Connection conn) {
        try {
            if (rs != null) rs.close();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error closing ResultSet", e);
        }
        try {
            if (stmt != null) stmt.close();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error closing Statement", e);
        }
        try {
            if (conn != null && conn != this.connection) {
                conn.close();
                activeConnections.decrementAndGet();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error closing Connection", e);
        }
    }

    /**
     * Convenience method for closing PreparedStatement and Connection.
     *
     * @param ps   PreparedStatement to close
     * @param conn Connection to close (if not the main connection)
     */
    public void closeResources(PreparedStatement ps, Connection conn) {
        closeResources(null, ps, conn);
    }

    /**
     * Gets the number of active connections.
     *
     * @return active connection count
     */
    public int getActiveConnectionCount() {
        return activeConnections.get();
    }

    /**
     * Shuts down all database connections.
     */
    public void shutdown() {
        closeConnection();
        LOGGER.info("Database connection manager shut down");
    }
}