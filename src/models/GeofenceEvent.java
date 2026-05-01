package models;

import java.time.LocalDateTime;

public class GeofenceEvent extends BaseEntity {
    private int geofenceZoneId;
    private String zoneName;
    private String zoneType;
    private int vehicleId;
    private String registrationNumber;
    private String make;
    private String model;
    private String eventType;
    private LocalDateTime eventTimestamp;
    private Double vehicleLatitude;
    private Double vehicleLongitude;
    private Double distanceToZoneCenter;
    private boolean isNotified;
    private String notificationSentTo;
    private String priority;

    public GeofenceEvent() {
        super();
        this.isNotified = false;
        this.priority = "MEDIUM";
    }

    public GeofenceEvent(int geofenceZoneId, int vehicleId, String eventType, LocalDateTime eventTimestamp) {
        this();
        this.geofenceZoneId = geofenceZoneId;
        this.vehicleId = vehicleId;
        this.eventType = eventType;
        this.eventTimestamp = eventTimestamp;
    }

    public int getGeofenceZoneId() {
        return geofenceZoneId;
    }

    public void setGeofenceZoneId(int geofenceZoneId) {
        this.geofenceZoneId = geofenceZoneId;
    }

    public String getZoneName() {
        return zoneName;
    }

    public void setZoneName(String zoneName) {
        this.zoneName = zoneName;
    }

    public String getZoneType() {
        return zoneType;
    }

    public void setZoneType(String zoneType) {
        this.zoneType = zoneType;
    }

    public int getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public LocalDateTime getEventTimestamp() {
        return eventTimestamp;
    }

    public void setEventTimestamp(LocalDateTime eventTimestamp) {
        this.eventTimestamp = eventTimestamp;
    }

    public Double getVehicleLatitude() {
        return vehicleLatitude;
    }

    public void setVehicleLatitude(Double vehicleLatitude) {
        this.vehicleLatitude = vehicleLatitude;
    }

    public Double getVehicleLongitude() {
        return vehicleLongitude;
    }

    public void setVehicleLongitude(Double vehicleLongitude) {
        this.vehicleLongitude = vehicleLongitude;
    }

    public Double getDistanceToZoneCenter() {
        return distanceToZoneCenter;
    }

    public void setDistanceToZoneCenter(Double distanceToZoneCenter) {
        this.distanceToZoneCenter = distanceToZoneCenter;
    }

    public boolean isNotified() {
        return isNotified;
    }

    public void setNotified(boolean notified) {
        isNotified = notified;
    }

    public String getNotificationSentTo() {
        return notificationSentTo;
    }

    public void setNotificationSentTo(String notificationSentTo) {
        this.notificationSentTo = notificationSentTo;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getEventIcon() {
        return "ENTER".equals(eventType) ? "ENTER" : "EXIT";
    }

    public String getEventColor() {
        if ("ENTER".equals(eventType)) {
            return "#F44336";
        } else {
            return "#4CAF50";
        }
    }

    public void calculatePriority() {
        if ("HIGH_CRIME".equals(zoneType) && "ENTER".equals(eventType)) {
            this.priority = "HIGH";
        } else if ("SCHOOL_ZONE".equals(zoneType) && "ENTER".equals(eventType)) {
            this.priority = "HIGH";
        } else if ("RESTRICTED".equals(zoneType)) {
            this.priority = "HIGH";
        } else {
            this.priority = "MEDIUM";
        }
    }

    @Override
    public String toString() {
        return eventType + " - " + zoneName + " - " + registrationNumber + " at " + eventTimestamp;
    }
}