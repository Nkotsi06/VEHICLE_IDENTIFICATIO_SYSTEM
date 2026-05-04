package models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * SystemHealth model representing system health metrics and monitoring data.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class SystemHealth extends BaseEntity {

    // Core fields
    private int totalUsers;
    private int activeUsers;
    private int inactiveUsers;
    private int newUsersLast30Days;
    private int totalVehicles;
    private int newVehiclesLast30Days;
    private int servicesLast30Days;
    private int violationsLast30Days;
    private int auditEventsLast7Days;
    private int unreadNotifications;
    private int policiesExpiringSoon;
    private int databaseConnectionsActive;
    private long databaseQueryTimeMs;
    private double databaseUsagePercent;
    private String databaseStatus;
    private LocalDateTime lastBackupTime;
    private long backupSizeBytes;
    private String applicationVersion;
    private LocalDateTime serverStartTime;
    private long uptimeSeconds;
    private double cpuUsagePercent;
    private double memoryUsagePercent;
    private String overallHealthStatus;

    // Status constants
    public static final String HEALTH_EXCELLENT = "EXCELLENT";
    public static final String HEALTH_GOOD = "GOOD";
    public static final String HEALTH_WARNING = "WARNING";
    public static final String HEALTH_CRITICAL = "CRITICAL";

    // Database status constants
    public static final String DB_HEALTHY = "HEALTHY";
    public static final String DB_DEGRADED = "DEGRADED";
    public static final String DB_OFFLINE = "OFFLINE";

    // JavaFX Properties
    private final IntegerProperty totalUsersProperty = new SimpleIntegerProperty();
    private final IntegerProperty activeUsersProperty = new SimpleIntegerProperty();
    private final IntegerProperty totalVehiclesProperty = new SimpleIntegerProperty();
    private final IntegerProperty servicesLast30DaysProperty = new SimpleIntegerProperty();
    private final DoubleProperty cpuUsageProperty = new SimpleDoubleProperty();
    private final DoubleProperty memoryUsageProperty = new SimpleDoubleProperty();
    private final DoubleProperty databaseUsageProperty = new SimpleDoubleProperty();
    private final StringProperty databaseStatusProperty = new SimpleStringProperty();
    private final StringProperty overallHealthProperty = new SimpleStringProperty();
    private final StringProperty healthColorProperty = new SimpleStringProperty();
    private final StringProperty formattedUptimeProperty = new SimpleStringProperty();
    private final StringProperty formattedBackupSizeProperty = new SimpleStringProperty();

    /**
     * Default constructor.
     */
    public SystemHealth() {
        super();
        this.databaseStatus = DB_HEALTHY;
        this.overallHealthStatus = HEALTH_GOOD;
        this.applicationVersion = "1.0.0";
        this.serverStartTime = LocalDateTime.now();

        databaseStatusProperty.set(DB_HEALTHY);
        overallHealthProperty.set(HEALTH_GOOD);
        healthColorProperty.set("#8BC34A");
        updateDerivedProperties();
    }

    // ============================================
    // PRIVATE UPDATE METHODS
    // ============================================

    private void updateDerivedProperties() {
        // Update formatted uptime
        long days = uptimeSeconds / 86400;
        long hours = (uptimeSeconds % 86400) / 3600;
        long minutes = (uptimeSeconds % 3600) / 60;
        long seconds = uptimeSeconds % 60;
        formattedUptimeProperty.set(String.format("%dd %dh %dm %ds", days, hours, minutes, seconds));

        // Update formatted backup size
        if (backupSizeBytes < 1024) {
            formattedBackupSizeProperty.set(backupSizeBytes + " B");
        } else if (backupSizeBytes < 1024 * 1024) {
            formattedBackupSizeProperty.set(String.format("%.2f KB", backupSizeBytes / 1024.0));
        } else if (backupSizeBytes < 1024 * 1024 * 1024) {
            formattedBackupSizeProperty.set(String.format("%.2f MB", backupSizeBytes / (1024.0 * 1024)));
        } else {
            formattedBackupSizeProperty.set(String.format("%.2f GB", backupSizeBytes / (1024.0 * 1024 * 1024)));
        }
    }

    // ============================================
    // GETTERS AND SETTERS
    // ============================================

    public int getTotalUsers() { return totalUsers; }
    public void setTotalUsers(int totalUsers) {
        this.totalUsers = totalUsers;
        totalUsersProperty.set(totalUsers);
    }
    public IntegerProperty totalUsersProperty() { return totalUsersProperty; }

    public int getActiveUsers() { return activeUsers; }
    public void setActiveUsers(int activeUsers) {
        this.activeUsers = activeUsers;
        activeUsersProperty.set(activeUsers);
    }
    public IntegerProperty activeUsersProperty() { return activeUsersProperty; }

    public int getInactiveUsers() { return inactiveUsers; }
    public void setInactiveUsers(int inactiveUsers) { this.inactiveUsers = inactiveUsers; }

    public int getNewUsersLast30Days() { return newUsersLast30Days; }
    public void setNewUsersLast30Days(int newUsersLast30Days) { this.newUsersLast30Days = newUsersLast30Days; }

    public int getTotalVehicles() { return totalVehicles; }
    public void setTotalVehicles(int totalVehicles) {
        this.totalVehicles = totalVehicles;
        totalVehiclesProperty.set(totalVehicles);
    }
    public IntegerProperty totalVehiclesProperty() { return totalVehiclesProperty; }

    public int getNewVehiclesLast30Days() { return newVehiclesLast30Days; }
    public void setNewVehiclesLast30Days(int newVehiclesLast30Days) { this.newVehiclesLast30Days = newVehiclesLast30Days; }

    public int getServicesLast30Days() { return servicesLast30Days; }
    public void setServicesLast30Days(int servicesLast30Days) {
        this.servicesLast30Days = servicesLast30Days;
        servicesLast30DaysProperty.set(servicesLast30Days);
    }
    public IntegerProperty servicesLast30DaysProperty() { return servicesLast30DaysProperty; }

    public int getViolationsLast30Days() { return violationsLast30Days; }
    public void setViolationsLast30Days(int violationsLast30Days) { this.violationsLast30Days = violationsLast30Days; }

    public int getAuditEventsLast7Days() { return auditEventsLast7Days; }
    public void setAuditEventsLast7Days(int auditEventsLast7Days) { this.auditEventsLast7Days = auditEventsLast7Days; }

    public int getUnreadNotifications() { return unreadNotifications; }
    public void setUnreadNotifications(int unreadNotifications) { this.unreadNotifications = unreadNotifications; }

    public int getPoliciesExpiringSoon() { return policiesExpiringSoon; }
    public void setPoliciesExpiringSoon(int policiesExpiringSoon) { this.policiesExpiringSoon = policiesExpiringSoon; }

    public int getDatabaseConnectionsActive() { return databaseConnectionsActive; }
    public void setDatabaseConnectionsActive(int databaseConnectionsActive) { this.databaseConnectionsActive = databaseConnectionsActive; }

    public long getDatabaseQueryTimeMs() { return databaseQueryTimeMs; }
    public void setDatabaseQueryTimeMs(long databaseQueryTimeMs) { this.databaseQueryTimeMs = databaseQueryTimeMs; }

    public double getDatabaseUsagePercent() { return databaseUsagePercent; }
    public void setDatabaseUsagePercent(double databaseUsagePercent) {
        this.databaseUsagePercent = databaseUsagePercent;
        databaseUsageProperty.set(databaseUsagePercent);
    }
    public DoubleProperty databaseUsageProperty() { return databaseUsageProperty; }

    public String getDatabaseStatus() { return databaseStatus; }
    public void setDatabaseStatus(String databaseStatus) {
        this.databaseStatus = databaseStatus;
        databaseStatusProperty.set(databaseStatus);
    }
    public StringProperty databaseStatusProperty() { return databaseStatusProperty; }

    public LocalDateTime getLastBackupTime() { return lastBackupTime; }
    public void setLastBackupTime(LocalDateTime lastBackupTime) { this.lastBackupTime = lastBackupTime; }

    public long getBackupSizeBytes() { return backupSizeBytes; }
    public void setBackupSizeBytes(long backupSizeBytes) {
        this.backupSizeBytes = backupSizeBytes;
        updateDerivedProperties();
    }

    public String getApplicationVersion() { return applicationVersion; }
    public void setApplicationVersion(String applicationVersion) { this.applicationVersion = applicationVersion; }

    public LocalDateTime getServerStartTime() { return serverStartTime; }
    public void setServerStartTime(LocalDateTime serverStartTime) { this.serverStartTime = serverStartTime; }

    public long getUptimeSeconds() { return uptimeSeconds; }
    public void setUptimeSeconds(long uptimeSeconds) {
        this.uptimeSeconds = uptimeSeconds;
        updateDerivedProperties();
    }

    public double getCpuUsagePercent() { return cpuUsagePercent; }
    public void setCpuUsagePercent(double cpuUsagePercent) {
        this.cpuUsagePercent = cpuUsagePercent;
        cpuUsageProperty.set(cpuUsagePercent);
    }
    public DoubleProperty cpuUsageProperty() { return cpuUsageProperty; }

    public double getMemoryUsagePercent() { return memoryUsagePercent; }
    public void setMemoryUsagePercent(double memoryUsagePercent) {
        this.memoryUsagePercent = memoryUsagePercent;
        memoryUsageProperty.set(memoryUsagePercent);
    }
    public DoubleProperty memoryUsageProperty() { return memoryUsageProperty; }

    public String getOverallHealthStatus() { return overallHealthStatus; }
    public void setOverallHealthStatus(String overallHealthStatus) {
        this.overallHealthStatus = overallHealthStatus;
        overallHealthProperty.set(overallHealthStatus);
        updateHealthColor();
    }
    public StringProperty overallHealthProperty() { return overallHealthProperty; }

    public String getHealthColor() { return healthColorProperty.get(); }
    public StringProperty healthColorProperty() { return healthColorProperty; }

    public String getFormattedUptime() { return formattedUptimeProperty.get(); }
    public StringProperty formattedUptimeProperty() { return formattedUptimeProperty; }

    public String getFormattedBackupSize() { return formattedBackupSizeProperty.get(); }
    public StringProperty formattedBackupSizeProperty() { return formattedBackupSizeProperty; }

    private void updateHealthColor() {
        switch (overallHealthStatus) {
            case HEALTH_EXCELLENT: healthColorProperty.set("#4CAF50"); break;
            case HEALTH_GOOD: healthColorProperty.set("#8BC34A"); break;
            case HEALTH_WARNING: healthColorProperty.set("#FFC107"); break;
            case HEALTH_CRITICAL: healthColorProperty.set("#F44336"); break;
            default: healthColorProperty.set("#9E9E9E");
        }
    }

    // ============================================
    // BUSINESS LOGIC METHODS
    // ============================================

    public void calculateOverallHealth() {
        int issueCount = 0;

        if (databaseUsagePercent > 85) issueCount++;
        if (databaseQueryTimeMs > 1000) issueCount++;
        if (!DB_HEALTHY.equals(databaseStatus)) issueCount++;
        if (cpuUsagePercent > 80) issueCount++;
        if (memoryUsagePercent > 85) issueCount++;

        if (issueCount == 0) {
            this.overallHealthStatus = HEALTH_EXCELLENT;
        } else if (issueCount <= 2) {
            this.overallHealthStatus = HEALTH_GOOD;
        } else if (issueCount <= 4) {
            this.overallHealthStatus = HEALTH_WARNING;
        } else {
            this.overallHealthStatus = HEALTH_CRITICAL;
        }
        overallHealthProperty.set(this.overallHealthStatus);
        updateHealthColor();
    }

    public String getFormattedLastBackupTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return lastBackupTime != null ? lastBackupTime.format(formatter) : "Never";
    }

    public String getFormattedServerStartTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return serverStartTime != null ? serverStartTime.format(formatter) : "";
    }

    public String getDatabaseStatusDisplay() {
        switch (databaseStatus) {
            case DB_HEALTHY: return "Healthy";
            case DB_DEGRADED: return "Degraded";
            case DB_OFFLINE: return "Offline";
            default: return databaseStatus;
        }
    }

    public String getDatabaseStatusColor() {
        switch (databaseStatus) {
            case DB_HEALTHY: return "#4CAF50";
            case DB_DEGRADED: return "#FF9800";
            case DB_OFFLINE: return "#F44336";
            default: return "#9E9E9E";
        }
    }

    public double getActiveUserPercentage() {
        if (totalUsers <= 0) return 0;
        return (double) activeUsers / totalUsers * 100;
    }

    // ============================================
    // OVERRIDE METHODS
    // ============================================

    @Override
    public int getId() { return id; }
    @Override
    public void setId(int id) { this.id = id; }

    @Override
    public String toString() {
        return "System Health: " + overallHealthStatus + " - " + totalVehicles + " vehicles, " + activeUsers + " active users";
    }
}