package dao;

import database.DatabaseConnection;
import database.ViewLoader;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DummyDataDAO - Uses ONLY views for fetching real data from database.
 * Returns actual database records (not fake data).
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class DummyDataDAO {

    private final ViewLoader viewLoader;

    public DummyDataDAO() {
        this.viewLoader = new ViewLoader();
    }

    /**
     * Fetch all users from the database using view.
     * Minimum 20 records if available.
     */
    public List<Map<String, Object>> getUsers() throws SQLException {
        List<Map<String, Object>> users = new ArrayList<>();

        // Use view - NO direct SQL
        List<Map<String, Object>> results = viewLoader.loadViewWithPagination("vw_users", 100, 0);

        for (Map<String, Object> row : results) {
            Map<String, Object> user = new HashMap<>();
            user.put("id", row.get("id"));
            user.put("username", row.get("username"));
            user.put("full_name", row.get("full_name"));
            user.put("email", row.get("email"));
            user.put("role", row.get("role"));
            user.put("is_active", row.get("is_active"));
            user.put("created_at", row.get("created_at"));
            users.add(user);
        }
        return users;
    }

    /**
     * Fetch all vehicles from the database using view.
     * Minimum 20 records if available.
     */
    public List<Map<String, Object>> getVehicles() throws SQLException {
        List<Map<String, Object>> vehicles = new ArrayList<>();

        // Use view - NO direct SQL
        List<Map<String, Object>> results = viewLoader.loadViewWithPagination("vw_vehicles", 100, 0);

        for (Map<String, Object> row : results) {
            Map<String, Object> vehicle = new HashMap<>();
            vehicle.put("id", row.get("id"));
            vehicle.put("registration_number", row.get("registration_number"));
            vehicle.put("make", row.get("make"));
            vehicle.put("model", row.get("model"));
            vehicle.put("year", row.get("year"));
            vehicle.put("owner_name", row.get("owner_name") != null ? row.get("owner_name") : "N/A");
            vehicle.put("status_name", row.get("status_name") != null ? row.get("status_name") : "Unknown");
            vehicle.put("created_at", row.get("created_at"));
            vehicles.add(vehicle);
        }
        return vehicles;
    }

    /**
     * Fetch all customers from the database using view.
     * Minimum 20 records if available.
     */
    public List<Map<String, Object>> getCustomers() throws SQLException {
        List<Map<String, Object>> customers = new ArrayList<>();

        // Use view - NO direct SQL
        List<Map<String, Object>> results = viewLoader.loadViewWithPagination("vw_customers", 100, 0);

        for (Map<String, Object> row : results) {
            Map<String, Object> customer = new HashMap<>();
            customer.put("id", row.get("id"));
            customer.put("name", row.get("name"));
            customer.put("email", row.get("email") != null ? row.get("email") : "");
            customer.put("phone", row.get("phone") != null ? row.get("phone") : "");
            customer.put("address", row.get("address") != null ? row.get("address") : "");
            customer.put("vehicle_count", row.get("vehicle_count") != null ? row.get("vehicle_count") : 0);
            customer.put("created_at", row.get("created_at"));
            customers.add(customer);
        }
        return customers;
    }

    /**
     * Fetch all service records from the database using views.
     * Minimum 20 records if available.
     */
    public List<Map<String, Object>> getServiceRecords() throws SQLException {
        List<Map<String, Object>> services = new ArrayList<>();

        // Use view - NO direct SQL
        List<Map<String, Object>> results = viewLoader.loadViewWithPagination("vw_service_records", 100, 0);

        for (Map<String, Object> row : results) {
            Map<String, Object> service = new HashMap<>();
            service.put("id", row.get("id"));
            service.put("registration_number", row.get("registration_number") != null ? row.get("registration_number") : "N/A");
            service.put("workshop_name", row.get("workshop_name") != null ? row.get("workshop_name") : "N/A");
            service.put("service_type", row.get("service_type"));
            service.put("service_date", row.get("service_date"));
            service.put("cost", row.get("cost"));
            service.put("odometer_reading", row.get("odometer_reading"));
            service.put("status", row.get("status"));
            services.add(service);
        }
        return services;
    }

    /**
     * Get total count of users using view.
     */
    public int getUserCount() throws SQLException {
        return viewLoader.countViewRows("vw_users");
    }

    /**
     * Get total count of vehicles using view.
     */
    public int getVehicleCount() throws SQLException {
        return viewLoader.countViewRows("vw_vehicles");
    }

    /**
     * Get total count of customers using view.
     */
    public int getCustomerCount() throws SQLException {
        return viewLoader.countViewRows("vw_customers");
    }

    /**
     * Get total count of service records using view.
     */
    public int getServiceRecordCount() throws SQLException {
        return viewLoader.countViewRows("vw_service_records");
    }
}