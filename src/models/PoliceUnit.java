package models;

import java.time.LocalDateTime;

public class PoliceUnit extends BaseEntity {
    private String unitId;
    private String officerName;
    private String badgeNumber;
    private Double currentLocationLat;
    private Double currentLocationLng;
    private LocalDateTime lastLocationUpdate;
    private String status;
    private String deviceId;

    public PoliceUnit() {
        super();
        this.status = "AVAILABLE";
    }

    public PoliceUnit(String unitId, String officerName, String badgeNumber) {
        this();
        this.unitId = unitId;
        this.officerName = officerName;
        this.badgeNumber = badgeNumber;
    }

    public String getUnitId() {
        return unitId;
    }

    public void setUnitId(String unitId) {
        this.unitId = unitId;
    }

    public String getOfficerName() {
        return officerName;
    }

    public void setOfficerName(String officerName) {
        this.officerName = officerName;
    }

    public String getBadgeNumber() {
        return badgeNumber;
    }

    public void setBadgeNumber(String badgeNumber) {
        this.badgeNumber = badgeNumber;
    }

    public Double getCurrentLocationLat() {
        return currentLocationLat;
    }

    public void setCurrentLocationLat(Double currentLocationLat) {
        this.currentLocationLat = currentLocationLat;
    }

    public Double getCurrentLocationLng() {
        return currentLocationLng;
    }

    public void setCurrentLocationLng(Double currentLocationLng) {
        this.currentLocationLng = currentLocationLng;
    }

    public LocalDateTime getLastLocationUpdate() {
        return lastLocationUpdate;
    }

    public void setLastLocationUpdate(LocalDateTime lastLocationUpdate) {
        this.lastLocationUpdate = lastLocationUpdate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public boolean isOnPatrol() {
        return "ON_PATROL".equals(status);
    }

    public boolean isAvailable() {
        return "AVAILABLE".equals(status);
    }

    public String getStatusColor() {
        switch (status) {
            case "ON_PATROL": return "#4CAF50";
            case "AVAILABLE": return "#2196F3";
            case "BUSY": return "#FF9800";
            case "OFF_DUTY": return "#9E9E9E";
            default: return "#9E9E9E";
        }
    }

    @Override
    public String toString() {
        return unitId + " - " + officerName + " (" + status + ")";
    }
}