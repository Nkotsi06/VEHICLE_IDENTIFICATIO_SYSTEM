package database;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DatabaseConnection {

    private static DatabaseConnection instance;
    private Connection connection;
    private String url;
    private String username;
    private String password;

    private DatabaseConnection() {
        loadProperties();
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    private void loadProperties() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config/database.properties")) {
            Properties props = new Properties();
            if (input != null) {
                props.load(input);
                this.url = props.getProperty("db.url", "jdbc:postgresql://localhost:5432/vehicle_db");
                this.username = props.getProperty("db.user", "postgres");
                this.password = props.getProperty("db.password", "1234567");
            } else {
                this.url = "jdbc:postgresql://localhost:5432/vehicle_db";
                this.username = "postgres";
                this.password = "1234567";
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.url = "jdbc:postgresql://localhost:5432/vehicle_db";
            this.username = "postgres";
            this.password = "1234567";
        }
    }

    public Connection getConnection() throws SQLException {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("org.postgresql.Driver");
                connection = DriverManager.getConnection(url, username, password);
                connection.setAutoCommit(true);
            }
            return connection;
        } catch (ClassNotFoundException e) {
            throw new SQLException("PostgreSQL JDBC Driver not found", e);
        }
    }

    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void beginTransaction() throws SQLException {
        Connection conn = getConnection();
        conn.setAutoCommit(false);
    }

    public void commitTransaction() throws SQLException {
        Connection conn = getConnection();
        conn.commit();
        conn.setAutoCommit(true);
    }

    public void rollbackTransaction() throws SQLException {
        Connection conn = getConnection();
        conn.rollback();
        conn.setAutoCommit(true);
    }

    public void closeResources(ResultSet rs, Statement stmt, Connection conn) {
        try {
            if (rs != null) rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (stmt != null) stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (conn != null && conn != this.connection) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void closeResources(PreparedStatement ps, Connection conn) {
        closeResources(null, ps, conn);
    }
}