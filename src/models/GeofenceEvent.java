package models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * GeofenceEvent model representing vehicle entry/exit events in geofenced zones.
 * This is a simplified version of GeofenceAlertEvent for historical tracking.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class GeofenceEvent extends BaseEntity {

    // Core fields
    private int id;
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

    // Event type constants
    public static final String EVENT_ENTER = "ENTER";
    public static final String EVENT_EXIT = "EXIT";

    // Priority constants
    public static final String PRIORITY_HIGH = "HIGH";
    public static final String PRIORITY_MEDIUM = "MEDIUM";
    public static final String PRIORITY_LOW = "LOW";

    /**
     * Default constructor - initializes with default values.
     */
    public GeofenceEvent() {
        super();
        this.isNotified = false;
        this.priority = PRIORITY_MEDIUM;
        this.eventType = EVENT_ENTER;
    }

    /**
     * Constructor for creating a new geofence event.
     *
     * @param geofenceZoneId the geofence zone ID
     * @param vehicleId      the vehicle ID
     * @param eventType      the event type (ENTER/EXIT)
     * @param eventTimestamp the event timestamp
     */
    public GeofenceEvent(int geofenceZoneId, int vehicleId, String eventType, LocalDateTime eventTimestamp) {
        this();
        this.geofenceZoneId = geofenceZoneId;
        this.vehicleId = vehicleId;
        this.eventType = eventType;
        this.eventTimestamp = eventTimestamp;
    }

    // ============================================
    // GETTERS AND SETTERS
    // ============================================

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

    // ============================================
    // BUSINESS LOGIC METHODS
    // ============================================

    /**
     * Gets the event icon.
     *
     * @return "ENTER" or "EXIT"
     */
    public String getEventIcon() {
        return EVENT_ENTER.equals(eventType) ? "ENTER" : "EXIT";
    }

    /**
     * Gets the event color code.
     *
     * @return hex color for the event
     */
    public String getEventColor() {
        if (EVENT_ENTER.equals(eventType)) {
            return "#F44336";
        } else {
            return "#4CAF50";
        }
    }

    /**
     * Gets the event type display name.
     *
     * @return human-readable event type
     */
    public String getEventTypeDisplay() {
        return EVENT_ENTER.equals(eventType) ? "Entered Zone" : "Exited Zone";
    }

    /**
     * Gets the formatted event timestamp.
     *
     * @return formatted date-time string
     */
    public String getFormattedEventTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return eventTimestamp != null ? eventTimestamp.format(formatter) : "";
    }

    /**
     * Gets the formatted distance to zone center.
     *
     * @return formatted distance string
     */
    public String getFormattedDistance() {
        if (distanceToZoneCenter == null) return "N/A";
        if (distanceToZoneCenter < 1) {
            return String.format("%.0f meters", distanceToZoneCenter * 1000);
        }
        return String.format("%.2f km", distanceToZoneCenter);
    }

    /**
     * Gets the priority display name.
     *
     * @return human-readable priority
     */
    public String getPriorityDisplay() {
        switch (priority) {
            case PRIORITY_HIGH: return "High";
            case PRIORITY_MEDIUM: return "Medium";
            case PRIORITY_LOW: return "Low";
            default: return priority;
        }
    }

    /**
     * Calculates the priority based on zone type and event type.
     */
    public void calculatePriority() {
        if (("HIGH_CRIME".equals(zoneType) || "SCHOOL_ZONE".equals(zoneType) || "RESTRICTED".equals(zoneType))
                && EVENT_ENTER.equals(eventType)) {
            this.priority = PRIORITY_HIGH;
        } else {
            this.priority = PRIORITY_MEDIUM;
        }
    }

    /**
     * Marks the event as notified.
     *
     * @param recipient the recipient of the notification
     */
    public void markNotified(String recipient) {
        this.isNotified = true;
        this.notificationSentTo = recipient;
    }

    // ============================================
    // OVERRIDE METHODS
    // ============================================

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return eventType + " - " + zoneName + " - " + registrationNumber + " at " + getFormattedEventTimestamp();
    }

    /**
     * Converts this GeofenceEvent to a GeofenceAlertEvent.
     *
     * @return a new GeofenceAlertEvent
     */
    public GeofenceAlertEvent toAlertEvent() {
        GeofenceAlertEvent alert = new GeofenceAlertEvent();
        alert.setGeofenceZoneId(this.geofenceZoneId);
        alert.setZoneName(this.zoneName);
        alert.setZoneType(this.zoneType);
        alert.setVehicleId(this.vehicleId);
        alert.setRegistrationNumber(this.registrationNumber);
        alert.setMake(this.make);
        alert.setModel(this.model);
        alert.setAlertType(this.eventType);
        alert.setAlertTimestamp(this.eventTimestamp);
        alert.setVehicleLocationLat(this.vehicleLatitude);
        alert.setVehicleLocationLng(this.vehicleLongitude);
        alert.setPriority(this.priority);
        alert.setNotified(this.isNotified);
        alert.setNotificationSentTo(this.notificationSentTo);
        return alert;
    }
}