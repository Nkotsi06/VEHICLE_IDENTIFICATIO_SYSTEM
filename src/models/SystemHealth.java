package models;

import java.time.LocalDateTime;

public class SystemHealth extends BaseEntity {
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

    public SystemHealth() {
        super();
        this.databaseStatus = "HEALTHY";
        this.overallHealthStatus = "GOOD";
    }

    public int getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(int totalUsers) {
        this.totalUsers = totalUsers;
    }

    public int getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(int activeUsers) {
        this.activeUsers = activeUsers;
    }

    public int getInactiveUsers() {
        return inactiveUsers;
    }

    public void setInactiveUsers(int inactiveUsers) {
        this.inactiveUsers = inactiveUsers;
    }

    public int getNewUsersLast30Days() {
        return newUsersLast30Days;
    }

    public void setNewUsersLast30Days(int newUsersLast30Days) {
        this.newUsersLast30Days = newUsersLast30Days;
    }

    public int getTotalVehicles() {
        return totalVehicles;
    }

    public void setTotalVehicles(int totalVehicles) {
        this.totalVehicles = totalVehicles;
    }

    public int getNewVehiclesLast30Days() {
        return newVehiclesLast30Days;
    }

    public void setNewVehiclesLast30Days(int newVehiclesLast30Days) {
        this.newVehiclesLast30Days = newVehiclesLast30Days;
    }

    public int getServicesLast30Days() {
        return servicesLast30Days;
    }

    public void setServicesLast30Days(int servicesLast30Days) {
        this.servicesLast30Days = servicesLast30Days;
    }

    public int getViolationsLast30Days() {
        return violationsLast30Days;
    }

    public void setViolationsLast30Days(int violationsLast30Days) {
        this.violationsLast30Days = violationsLast30Days;
    }

    public int getAuditEventsLast7Days() {
        return auditEventsLast7Days;
    }

    public void setAuditEventsLast7Days(int auditEventsLast7Days) {
        this.auditEventsLast7Days = auditEventsLast7Days;
    }

    public int getUnreadNotifications() {
        return unreadNotifications;
    }

    public void setUnreadNotifications(int unreadNotifications) {
        this.unreadNotifications = unreadNotifications;
    }

    public int getPoliciesExpiringSoon() {
        return policiesExpiringSoon;
    }

    public void setPoliciesExpiringSoon(int policiesExpiringSoon) {
        this.policiesExpiringSoon = policiesExpiringSoon;
    }

    public int getDatabaseConnectionsActive() {
        return databaseConnectionsActive;
    }

    public void setDatabaseConnectionsActive(int databaseConnectionsActive) {
        this.databaseConnectionsActive = databaseConnectionsActive;
    }

    public long getDatabaseQueryTimeMs() {
        return databaseQueryTimeMs;
    }

    public void setDatabaseQueryTimeMs(long databaseQueryTimeMs) {
        this.databaseQueryTimeMs = databaseQueryTimeMs;
    }

    public double getDatabaseUsagePercent() {
        return databaseUsagePercent;
    }

    public void setDatabaseUsagePercent(double databaseUsagePercent) {
        this.databaseUsagePercent = databaseUsagePercent;
    }

    public String getDatabaseStatus() {
        return databaseStatus;
    }

    public void setDatabaseStatus(String databaseStatus) {
        this.databaseStatus = databaseStatus;
    }

    public LocalDateTime getLastBackupTime() {
        return lastBackupTime;
    }

    public void setLastBackupTime(LocalDateTime lastBackupTime) {
        this.lastBackupTime = lastBackupTime;
    }

    public long getBackupSizeBytes() {
        return backupSizeBytes;
    }

    public void setBackupSizeBytes(long backupSizeBytes) {
        this.backupSizeBytes = backupSizeBytes;
    }

    public String getApplicationVersion() {
        return applicationVersion;
    }

    public void setApplicationVersion(String applicationVersion) {
        this.applicationVersion = applicationVersion;
    }

    public LocalDateTime getServerStartTime() {
        return serverStartTime;
    }

    public void setServerStartTime(LocalDateTime serverStartTime) {
        this.serverStartTime = serverStartTime;
    }

    public long getUptimeSeconds() {
        return uptimeSeconds;
    }

    public void setUptimeSeconds(long uptimeSeconds) {
        this.uptimeSeconds = uptimeSeconds;
    }

    public double getCpuUsagePercent() {
        return cpuUsagePercent;
    }

    public void setCpuUsagePercent(double cpuUsagePercent) {
        this.cpuUsagePercent = cpuUsagePercent;
    }

    public double getMemoryUsagePercent() {
        return memoryUsagePercent;
    }

    public void setMemoryUsagePercent(double memoryUsagePercent) {
        this.memoryUsagePercent = memoryUsagePercent;
    }

    public String getOverallHealthStatus() {
        return overallHealthStatus;
    }

    public void setOverallHealthStatus(String overallHealthStatus) {
        this.overallHealthStatus = overallHealthStatus;
    }

    public void calculateOverallHealth() {
        int issueCount = 0;

        if (databaseUsagePercent > 85) issueCount++;
        if (databaseQueryTimeMs > 1000) issueCount++;
        if (!"HEALTHY".equals(databaseStatus)) issueCount++;
        if (cpuUsagePercent > 80) issueCount++;
        if (memoryUsagePercent > 85) issueCount++;

        if (issueCount == 0) {
            this.overallHealthStatus = "EXCELLENT";
        } else if (issueCount <= 2) {
            this.overallHealthStatus = "GOOD";
        } else if (issueCount <= 4) {
            this.overallHealthStatus = "WARNING";
        } else {
            this.overallHealthStatus = "CRITICAL";
        }
    }

    public String getHealthColor() {
        switch (overallHealthStatus) {
            case "EXCELLENT": return "#4CAF50";
            case "GOOD": return "#8BC34A";
            case "WARNING": return "#FFC107";
            case "CRITICAL": return "#F44336";
            default: return "#9E9E9E";
        }
    }

    public String getFormattedUptime() {
        long days = uptimeSeconds / 86400;
        long hours = (uptimeSeconds % 86400) / 3600;
        long minutes = (uptimeSeconds % 3600) / 60;
        long seconds = uptimeSeconds % 60;

        return String.format("%dd %dh %dm %ds", days, hours, minutes, seconds);
    }

    public String getFormattedBackupSize() {
        if (backupSizeBytes < 1024) {
            return backupSizeBytes + " B";
        } else if (backupSizeBytes < 1024 * 1024) {
            return String.format("%.2f KB", backupSizeBytes / 1024.0);
        } else if (backupSizeBytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", backupSizeBytes / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", backupSizeBytes / (1024.0 * 1024 * 1024));
        }
    }

    @Override
    public String toString() {
        return "System Health: " + overallHealthStatus + " - " + totalVehicles + " vehicles, " + activeUsers + " active users";
    }
}