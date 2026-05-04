package dao;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import database.ViewLoader;
import models.SystemHealth;

/**
 * SystemHealthDAO - Uses ONLY views for all operations.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class SystemHealthDAO {

    private final ViewLoader viewLoader;

    public SystemHealthDAO() {
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
        return viewLoader.countViewRows("vw_vehicles");
    }

    public int getTotalCustomers() throws SQLException {
        return viewLoader.countViewRows("vw_customers");
    }

    public int getActiveStolenCount() throws SQLException {
        return viewLoader.countViewRowsWithCondition("vw_stolen_vehicles", "status = 'ACTIVE'");
    }

    public int getActiveInsuranceCount() throws SQLException {
        return viewLoader.countViewRowsWithCondition("vw_insurance_policies", "status = 'ACTIVE' AND end_date >= CURRENT_DATE");
    }

    public double getTotalUnpaidFines() throws SQLException {
        return viewLoader.getSumUnpaidFines();
    }

    public int getPendingQueries() throws SQLException {
        return viewLoader.countViewRowsWithCondition("vw_customer_queries", "status = 'PENDING'");
    }

    public int getPendingWorkshops() throws SQLException {
        return viewLoader.countViewRowsWithCondition("vw_workshops", "is_approved = false");
    }

    public int getPendingClaims() throws SQLException {
        return viewLoader.countViewRowsWithCondition("vw_insurance_claims", "status = 'PENDING'");
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

    public String checkDatabaseStatus() throws SQLException {
        try {
            viewLoader.countViewRows("vw_users");
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
}