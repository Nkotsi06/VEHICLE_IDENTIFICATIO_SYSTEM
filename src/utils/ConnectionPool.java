package utils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import database.DatabaseConnection;

/**
 * Connection pool for managing database connections efficiently.
 * Implements a blocking queue pattern for connection reuse.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class ConnectionPool {

    private static ConnectionPool instance;
    private BlockingQueue<Connection> connectionPool;
    private int poolSize = 10;
    private AtomicInteger activeConnections = new AtomicInteger(0);
    private boolean isShutdown = false;

    /**
     * Private constructor for singleton pattern.
     * Initializes the connection pool.
     */
    private ConnectionPool() {
        connectionPool = new LinkedBlockingQueue<>(poolSize);
        initializePool();
    }

    /**
     * Gets the singleton instance of the connection pool.
     *
     * @return the connection pool instance
     */
    public static synchronized ConnectionPool getInstance() {
        if (instance == null) {
            instance = new ConnectionPool();
        }
        return instance;
    }

    /**
     * Initializes the pool with database connections.
     */
    private void initializePool() {
        for (int i = 0; i < poolSize; i++) {
            try {
                Connection conn = createConnection();
                if (conn != null) {
                    connectionPool.offer(conn);
                }
            } catch (SQLException e) {
                System.err.println("Failed to create connection " + (i + 1) + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        System.out.println("Connection pool initialized with " + connectionPool.size() + " connections");
    }

    /**
     * Creates a new database connection.
     *
     * @return a new Connection object
     * @throws SQLException if connection creation fails
     */
    private Connection createConnection() throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        if (conn != null) {
            // Set auto-commit to false for transaction control
            conn.setAutoCommit(true);
        }
        return conn;
    }

    /**
     * Gets a connection from the pool (blocking if none available).
     *
     * @return a database connection
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    public Connection getConnection() throws InterruptedException {
        if (isShutdown) {
            throw new IllegalStateException("Connection pool is shutdown");
        }

        Connection conn = connectionPool.poll(30, TimeUnit.SECONDS);
        if (conn == null) {
            throw new IllegalStateException("Timeout waiting for available connection");
        }

        // Validate connection before returning
        if (!isConnectionValid(conn)) {
            // Replace invalid connection
            try {
                conn.close();
                conn = createConnection();
            } catch (SQLException e) {
                System.err.println("Failed to replace invalid connection: " + e.getMessage());
                conn = createConnection();
            }
        }

        activeConnections.incrementAndGet();
        return conn;
    }

    /**
     * Gets a connection with a custom timeout.
     *
     * @param timeout the timeout duration
     * @param unit    the timeout unit
     * @return a database connection, or null if timeout occurs
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    public Connection getConnection(long timeout, TimeUnit unit) throws InterruptedException {
        if (isShutdown) {
            throw new IllegalStateException("Connection pool is shutdown");
        }

        Connection conn = connectionPool.poll(timeout, unit);
        if (conn != null) {
            if (!isConnectionValid(conn)) {
                try {
                    conn.close();
                    conn = createConnection();
                } catch (SQLException e) {
                    System.err.println("Failed to replace invalid connection: " + e.getMessage());
                    return null;
                }
            }
            activeConnections.incrementAndGet();
        }
        return conn;
    }

    /**
     * Releases a connection back to the pool.
     *
     * @param connection the connection to release
     */
    public void releaseConnection(Connection connection) {
        if (connection != null && !isShutdown) {
            try {
                // Reset connection state before returning to pool
                if (!connection.getAutoCommit()) {
                    connection.setAutoCommit(true);
                }
                connection.clearWarnings();
            } catch (SQLException e) {
                System.err.println("Error resetting connection: " + e.getMessage());
                // If we can't reset, close it and create a new one
                try {
                    connection.close();
                    connection = createConnection();
                } catch (SQLException ex) {
                    System.err.println("Failed to create replacement connection: " + ex.getMessage());
                }
            }

            if (connectionPool.offer(connection)) {
                activeConnections.decrementAndGet();
            } else {
                // Pool is full, close the connection
                try {
                    connection.close();
                    activeConnections.decrementAndGet();
                } catch (SQLException e) {
                    System.err.println("Error closing excess connection: " + e.getMessage());
                }
            }
        } else if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Error closing connection during shutdown: " + e.getMessage());
            }
        }
    }

    /**
     * Validates that a connection is still usable.
     *
     * @param conn the connection to validate
     * @return true if valid, false otherwise
     */
    private boolean isConnectionValid(Connection conn) {
        if (conn == null) return false;
        try {
            return conn.isValid(5); // 5 second timeout
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Gets the number of available connections in the pool.
     *
     * @return number of available connections
     */
    public int getAvailableConnections() {
        return connectionPool.size();
    }

    /**
     * Gets the number of active connections currently in use.
     *
     * @return number of active connections
     */
    public int getActiveConnections() {
        return activeConnections.get();
    }

    /**
     * Gets the maximum pool size.
     *
     * @return the pool size
     */
    public int getPoolSize() {
        return poolSize;
    }

    /**
     * Closes all connections in the pool and shuts it down.
     */
    public synchronized void shutdown() {
        if (isShutdown) return;

        isShutdown = true;

        for (Connection conn : connectionPool) {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
        connectionPool.clear();
        activeConnections.set(0);
        System.out.println("Connection pool shutdown complete");
    }

    /**
     * Checks if the pool is shutdown.
     *
     * @return true if shutdown, false otherwise
     */
    public boolean isShutdown() {
        return isShutdown;
    }
}