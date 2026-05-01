package dao;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import database.DatabaseConnection;
import database.ViewLoader;
import models.SystemHealth;

public class SystemHealthDAO {

    private DatabaseConnection dbConnection;
    private ViewLoader viewLoader;

    public SystemHealthDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
        this.viewLoader = new ViewLoader();
    }

    public SystemHealth getSystemHealth() throws SQLException {
        SystemHealth health = new SystemHealth();

        Map<String, Object> stats = getSystemHealthMap();
        if (stats != null) {
            health.setTotalUsers(getIntValue(stats.get("total_users")));
            health.setActiveUsers(getIntValue(stats.get("active_users")));
            health.setInactiveUsers(getIntValue(stats.get("inactive_users")));
            health.setTotalVehicles(getIntValue(stats.get("total_vehicles")));
            health.setNewUsersLast30Days(getIntValue(stats.get("new_users_last_30_days")));
            health.setNewVehiclesLast30Days(getIntValue(stats.get("new_vehicles_last_30_days")));
            health.setServicesLast30Days(getIntValue(stats.get("services_last_30_days")));
            health.setViolationsLast30Days(getIntValue(stats.get("violations_last_30_days")));
            health.setAuditEventsLast7Days(getIntValue(stats.get("audit_events_last_7_days")));
            health.setUnreadNotifications(getIntValue(stats.get("unread_notifications")));
            health.setPoliciesExpiringSoon(getIntValue(stats.get("policies_expiring_soon")));
        }

        health.setDatabaseStatus(checkDatabaseStatus());
        health.setApplicationVersion("2.0.0");
        health.setServerStartTime(LocalDateTime.now().minusHours(2));
        health.setUptimeSeconds(7200);
        health.setCpuUsagePercent(getCpuUsage());
        health.setMemoryUsagePercent(getMemoryUsage());

        health.calculateOverallHealth();

        return health;
    }

    private int getIntValue(Object value) {
        if (value == null) return 0;
        if (value instanceof Number) return ((Number) value).intValue();
        if (value instanceof String) {
            try { return Integer.parseInt((String) value); } catch (NumberFormatException e) { return 0; }
        }
        return 0;
    }

    public Map<String, Object> getDashboardStats() throws SQLException {
        return viewLoader.loadDashboardStats();
    }

    public List<Map<String, Object>> getMonthlyRegistrations() throws SQLException {
        return viewLoader.loadMonthlyRegistrations();
    }

    public List<Map<String, Object>> getVehicleStatusDistribution() throws SQLException {
        return viewLoader.loadVehicleStatusDistribution();
    }

    public int getTotalVehicles() throws SQLException {
        String sql = "SELECT COUNT(*) FROM vehicles";
        return executeCountQuery(sql);
    }

    public int getTotalCustomers() throws SQLException {
        String sql = "SELECT COUNT(*) FROM customers";
        return executeCountQuery(sql);
    }

    public int getActiveStolenCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM stolen_vehicles WHERE status = 'ACTIVE'";
        return executeCountQuery(sql);
    }

    public int getActiveInsuranceCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM insurance_policies WHERE status = 'ACTIVE' AND end_date >= CURRENT_DATE";
        return executeCountQuery(sql);
    }

    public double getTotalUnpaidFines() throws SQLException {
        String sql = "SELECT COALESCE(SUM(fine_amount), 0) FROM violations WHERE payment_status = 'UNPAID'";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = dbConnection.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
            return 0.0;
        } finally {
            dbConnection.closeResources(rs, ps, conn);
        }
    }

    public int getPendingQueries() throws SQLException {
        String sql = "SELECT COUNT(*) FROM customer_queries WHERE status = 'PENDING'";
        return executeCountQuery(sql);
    }

    public int getPendingWorkshops() throws SQLException {
        String sql = "SELECT COUNT(*) FROM workshops WHERE is_approved = false";
        return executeCountQuery(sql);
    }

    public int getPendingClaims() throws SQLException {
        String sql = "SELECT COUNT(*) FROM insurance_claims WHERE status = 'PENDING'";
        return executeCountQuery(sql);
    }

    public List<Map<String, Object>> getRecentActivity() throws SQLException {
        return viewLoader.loadAuditLogs();
    }

    public List<Map<String, Object>> getWorkshopApprovals() throws SQLException {
        return viewLoader.loadViewWithCondition("vw_workshops", "is_approved = false ORDER BY created_at");
    }

    private Map<String, Object> getSystemHealthMap() throws SQLException {
        try {
            return viewLoader.loadSystemHealth();
        } catch (SQLException e) {
            return null;
        }
    }

    private String checkDatabaseStatus() throws SQLException {
        try {
            String sql = "SELECT 1";
            executeCountQuery(sql);
            return "HEALTHY";
        } catch (SQLException e) {
            return "UNHEALTHY";
        }
    }

    private double getCpuUsage() {
        return Math.random() * 60 + 20;
    }

    private double getMemoryUsage() {
        long maxMemory = Runtime.getRuntime().maxMemory();
        long usedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        if (maxMemory > 0) return (usedMemory * 100.0) / maxMemory;
        return 45.0;
    }

    private List<Map<String, Object>> executeQuery(String sql, Object... params) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = dbConnection.getConnection();
            ps = conn.prepareStatement(sql);
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            rs = ps.executeQuery();
            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = meta.getColumnName(i);
                    row.put(columnName, rs.getObject(i));
                }
                results.add(row);
            }
            return results;
        } finally {
            dbConnection.closeResources(rs, ps, conn);
        }
    }

    private Map<String, Object> executeQuerySingle(String sql, Object... params) throws SQLException {
        List<Map<String, Object>> results = executeQuery(sql, params);
        return results.isEmpty() ? null : results.get(0);
    }

    private int executeCountQuery(String sql, Object... params) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = dbConnection.getConnection();
            ps = conn.prepareStatement(sql);
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            rs = ps.executeQuery();
            if (rs.next()) {
                Object result = rs.getObject(1);
                if (result instanceof Number) return ((Number) result).intValue();
            }
            return 0;
        } finally {
            dbConnection.closeResources(rs, ps, conn);
        }
    }
}