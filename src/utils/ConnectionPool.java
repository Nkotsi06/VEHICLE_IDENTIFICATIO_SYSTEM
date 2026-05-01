package utils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import database.DatabaseConnection;

public class ConnectionPool {

    private static ConnectionPool instance;
    private BlockingQueue<Connection> connectionPool;
    private int poolSize = 10;

    private ConnectionPool() {
        connectionPool = new LinkedBlockingQueue<>(poolSize);
        initializePool();
    }

    public static synchronized ConnectionPool getInstance() {
        if (instance == null) {
            instance = new ConnectionPool();
        }
        return instance;
    }

    private void initializePool() {
        for (int i = 0; i < poolSize; i++) {
            try {
                Connection conn = DatabaseConnection.getInstance().getConnection();
                connectionPool.offer(conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public Connection getConnection() throws InterruptedException {
        return connectionPool.take();
    }

    public void releaseConnection(Connection connection) {
        if (connection != null) {
            connectionPool.offer(connection);
        }
    }

    public int getAvailableConnections() {
        return connectionPool.size();
    }

    public void closeAllConnections() {
        for (Connection conn : connectionPool) {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        connectionPool.clear();
    }
}