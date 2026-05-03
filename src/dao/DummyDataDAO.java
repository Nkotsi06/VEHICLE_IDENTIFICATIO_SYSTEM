package dao;

import database.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO for fetching real data from database for Dummy Data display
 * Returns actual database records (not fake data)
 */
public class DummyDataDAO {

    private DatabaseConnection dbConnection;

    public DummyDataDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    /**
     * Fetch all users from the database
     * Minimum 20 records if available
     */
    public List<Map<String, Object>> getUsers() throws SQLException {
        List<Map<String, Object>> users = new ArrayList<>();
        String sql = "SELECT id, username, full_name, email, role, is_active, created_at " +
                "FROM users ORDER BY created_at DESC LIMIT 100";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> user = new HashMap<>();
                user.put("id", rs.getInt("id"));
                user.put("username", rs.getString("username"));
                user.put("full_name", rs.getString("full_name"));
                user.put("email", rs.getString("email"));
                user.put("role", rs.getString("role"));
                user.put("is_active", rs.getBoolean("is_active"));
                user.put("created_at", rs.getTimestamp("created_at"));
                users.add(user);
            }
        }
        return users;
    }

    /**
     * Fetch all vehicles from the database with owner and status information
     * Minimum 20 records if available
     */
    public List<Map<String, Object>> getVehicles() throws SQLException {
        List<Map<String, Object>> vehicles = new ArrayList<>();
        String sql = "SELECT v.id, v.registration_number, v.make, v.model, v.year, " +
                "c.name as owner_name, vs.status_name, v.created_at " +
                "FROM vehicles v " +
                "LEFT JOIN customers c ON v.owner_id = c.id " +
                "LEFT JOIN vehicle_status vs ON v.status_id = vs.id " +
                "ORDER BY v.created_at DESC LIMIT 100";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> vehicle = new HashMap<>();
                vehicle.put("id", rs.getInt("id"));
                vehicle.put("registration_number", rs.getString("registration_number"));
                vehicle.put("make", rs.getString("make"));
                vehicle.put("model", rs.getString("model"));
                vehicle.put("year", rs.getInt("year"));
                vehicle.put("owner_name", rs.getString("owner_name") != null ? rs.getString("owner_name") : "N/A");
                vehicle.put("status_name", rs.getString("status_name") != null ? rs.getString("status_name") : "Unknown");
                vehicle.put("created_at", rs.getTimestamp("created_at"));
                vehicles.add(vehicle);
            }
        }
        return vehicles;
    }

    /**
     * Fetch all customers from the database with vehicle count
     * Minimum 20 records if available
     */
    public List<Map<String, Object>> getCustomers() throws SQLException {
        List<Map<String, Object>> customers = new ArrayList<>();
        String sql = "SELECT c.id, c.name, u.email, c.phone, c.address, " +
                "(SELECT COUNT(*) FROM vehicles WHERE owner_id = c.id) as vehicle_count, " +
                "c.created_at " +
                "FROM customers c " +
                "LEFT JOIN users u ON c.user_id = u.id " +
                "ORDER BY c.created_at DESC LIMIT 100";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> customer = new HashMap<>();
                customer.put("id", rs.getInt("id"));
                customer.put("name", rs.getString("name"));
                customer.put("email", rs.getString("email") != null ? rs.getString("email") : "");
                customer.put("phone", rs.getString("phone") != null ? rs.getString("phone") : "");
                customer.put("address", rs.getString("address") != null ? rs.getString("address") : "");
                customer.put("vehicle_count", rs.getInt("vehicle_count"));
                customer.put("created_at", rs.getTimestamp("created_at"));
                customers.add(customer);
            }
        }
        return customers;
    }

    /**
     * Fetch all service records from the database with vehicle and workshop information
     * Minimum 20 records if available
     */
    public List<Map<String, Object>> getServiceRecords() throws SQLException {
        List<Map<String, Object>> services = new ArrayList<>();
        String sql = "SELECT sr.id, v.registration_number, w.workshop_name, " +
                "sr.service_type, sr.service_date, sr.cost, sr.odometer_reading, sr.status " +
                "FROM service_records sr " +
                "LEFT JOIN vehicles v ON sr.vehicle_id = v.id " +
                "LEFT JOIN workshops w ON sr.workshop_id = w.id " +
                "ORDER BY sr.service_date DESC LIMIT 100";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> service = new HashMap<>();
                service.put("id", rs.getInt("id"));
                service.put("registration_number", rs.getString("registration_number") != null ? rs.getString("registration_number") : "N/A");
                service.put("workshop_name", rs.getString("workshop_name") != null ? rs.getString("workshop_name") : "N/A");
                service.put("service_type", rs.getString("service_type"));
                service.put("service_date", rs.getDate("service_date"));
                service.put("cost", rs.getDouble("cost"));
                service.put("odometer_reading", rs.getInt("odometer_reading"));
                service.put("status", rs.getString("status"));
                services.add(service);
            }
        }
        return services;
    }

    /**
     * Get total count of users
     */
    public int getUserCount() throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM users";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt("count");
            return 0;
        }
    }

    /**
     * Get total count of vehicles
     */
    public int getVehicleCount() throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM vehicles";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt("count");
            return 0;
        }
    }

    /**
     * Get total count of customers
     */
    public int getCustomerCount() throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM customers";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt("count");
            return 0;
        }
    }

    /**
     * Get total count of service records
     */
    public int getServiceRecordCount() throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM service_records";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt("count");
            return 0;
        }
    }
}