package models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * GeofenceAlertEvent model representing real-time alerts when vehicles enter/exit geofenced zones.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class GeofenceAlertEvent extends BaseEntity {

    // Core fields
    private int id;
    private int geofenceZoneId;
    private String zoneName;
    private String zoneType;
    private int vehicleId;
    private String registrationNumber;
    private String make;
    private String model;
    private String alertType;
    private LocalDateTime alertTimestamp;
    private Double vehicleLocationLat;
    private Double vehicleLocationLng;
    private boolean isNotified;
    private String notificationSentTo;
    private String priority;

    // Alert type constants
    public static final String ALERT_ENTER = "ENTER";
    public static final String ALERT_EXIT = "EXIT";

    // Priority constants
    public static final String PRIORITY_HIGH = "HIGH";
    public static final String PRIORITY_MEDIUM = "MEDIUM";
    public static final String PRIORITY_LOW = "LOW";

    // JavaFX Properties for TableView binding
    private final StringProperty zoneNameProperty = new SimpleStringProperty();
    private final StringProperty zoneTypeProperty = new SimpleStringProperty();
    private final StringProperty registrationNumberProperty = new SimpleStringProperty();
    private final StringProperty alertTypeProperty = new SimpleStringProperty();
    private final ObjectProperty<LocalDateTime> alertTimestampProperty = new SimpleObjectProperty<>();
    private final BooleanProperty notifiedProperty = new SimpleBooleanProperty();
    private final StringProperty priorityProperty = new SimpleStringProperty();

    /**
     * Default constructor - initializes with current timestamp.
     */
    public GeofenceAlertEvent() {
        super();
        this.alertTimestamp = LocalDateTime.now();
        this.isNotified = false;
        this.priority = PRIORITY_MEDIUM;

        alertTimestampProperty.set(this.alertTimestamp);
        notifiedProperty.set(false);
        priorityProperty.set(PRIORITY_MEDIUM);
    }

    /**
     * Constructor for creating a new alert event.
     *
     * @param geofenceZoneId the geofence zone ID
     * @param vehicleId      the vehicle ID
     * @param alertType      the alert type (ENTER/EXIT)
     */
    public GeofenceAlertEvent(int geofenceZoneId, int vehicleId, String alertType) {
        this();
        this.geofenceZoneId = geofenceZoneId;
        this.vehicleId = vehicleId;
        this.alertType = alertType;

        alertTypeProperty.set(alertType);
    }

    // ============================================
    // GETTERS AND SETTERS WITH PROPERTY UPDATES
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
        zoneNameProperty.set(zoneName);
    }

    public StringProperty zoneNameProperty() {
        return zoneNameProperty;
    }

    public String getZoneType() {
        return zoneType;
    }

    public void setZoneType(String zoneType) {
        this.zoneType = zoneType;
        zoneTypeProperty.set(zoneType);
    }

    public StringProperty zoneTypeProperty() {
        return zoneTypeProperty;
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
        registrationNumberProperty.set(registrationNumber);
    }

    public StringProperty registrationNumberProperty() {
        return registrationNumberProperty;
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

    public String getAlertType() {
        return alertType;
    }

    public void setAlertType(String alertType) {
        this.alertType = alertType;
        alertTypeProperty.set(alertType);
    }

    public StringProperty alertTypeProperty() {
        return alertTypeProperty;
    }

    public LocalDateTime getAlertTimestamp() {
        return alertTimestamp;
    }

    public void setAlertTimestamp(LocalDateTime alertTimestamp) {
        this.alertTimestamp = alertTimestamp;
        alertTimestampProperty.set(alertTimestamp);
    }

    public ObjectProperty<LocalDateTime> alertTimestampProperty() {
        return alertTimestampProperty;
    }

    public Double getVehicleLocationLat() {
        return vehicleLocationLat;
    }

    public void setVehicleLocationLat(Double vehicleLocationLat) {
        this.vehicleLocationLat = vehicleLocationLat;
    }

    public Double getVehicleLocationLng() {
        return vehicleLocationLng;
    }

    public void setVehicleLocationLng(Double vehicleLocationLng) {
        this.vehicleLocationLng = vehicleLocationLng;
    }

    public boolean isNotified() {
        return isNotified;
    }

    public void setNotified(boolean notified) {
        isNotified = notified;
        notifiedProperty.set(notified);
    }

    public BooleanProperty notifiedProperty() {
        return notifiedProperty;
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
        priorityProperty.set(priority);
    }

    public StringProperty priorityProperty() {
        return priorityProperty;
    }

    // ============================================
    // BUSINESS LOGIC METHODS
    // ============================================

    /**
     * Gets the event icon for display.
     *
     * @return "ENTER" or "EXIT"
     */
    public String getEventIcon() {
        return ALERT_ENTER.equals(alertType) ? "ENTER" : "EXIT";
    }

    /**
     * Gets the event color code.
     *
     * @return hex color for the event type
     */
    public String getEventColor() {
        if (ALERT_ENTER.equals(alertType)) {
            return "#F44336"; // Red for entry
        } else {
            return "#4CAF50"; // Green for exit
        }
    }

    /**
     * Gets the alert type display name.
     *
     * @return human-readable alert type
     */
    public String getAlertTypeDisplay() {
        return ALERT_ENTER.equals(alertType) ? "Entered Zone" : "Exited Zone";
    }

    /**
     * Calculates the priority based on zone type and alert type.
     */
    public void calculatePriority() {
        if (("HIGH_CRIME".equals(zoneType) || "SCHOOL_ZONE".equals(zoneType) || "RESTRICTED".equals(zoneType))
                && ALERT_ENTER.equals(alertType)) {
            this.priority = PRIORITY_HIGH;
        } else {
            this.priority = PRIORITY_MEDIUM;
        }
        priorityProperty.set(this.priority);
    }

    /**
     * Gets the formatted alert timestamp.
     *
     * @return formatted date-time string
     */
    public String getFormattedAlertTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return alertTimestamp != null ? alertTimestamp.format(formatter) : "";
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
     * Gets the priority color.
     *
     * @return hex color code
     */
    public String getPriorityColor() {
        switch (priority) {
            case PRIORITY_HIGH: return "#F44336";
            case PRIORITY_MEDIUM: return "#FF9800";
            case PRIORITY_LOW: return "#4CAF50";
            default: return "#9E9E9E";
        }
    }

    /**
     * Marks the alert as notified.
     *
     * @param recipient the recipient of the notification
     */
    public void markNotified(String recipient) {
        this.isNotified = true;
        this.notificationSentTo = recipient;
        notifiedProperty.set(true);
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
        return alertType + " - " + zoneName + " - " + registrationNumber + " at " + getFormattedAlertTimestamp();
    }

    /**
     * Creates a copy of this alert event.
     *
     * @return a new GeofenceAlertEvent instance
     */
    public GeofenceAlertEvent copy() {
        GeofenceAlertEvent copy = new GeofenceAlertEvent();
        copy.setId(this.id);
        copy.setGeofenceZoneId(this.geofenceZoneId);
        copy.setZoneName(this.zoneName);
        copy.setZoneType(this.zoneType);
        copy.setVehicleId(this.vehicleId);
        copy.setRegistrationNumber(this.registrationNumber);
        copy.setMake(this.make);
        copy.setModel(this.model);
        copy.setAlertType(this.alertType);
        copy.setAlertTimestamp(this.alertTimestamp);
        copy.setVehicleLocationLat(this.vehicleLocationLat);
        copy.setVehicleLocationLng(this.vehicleLocationLng);
        copy.setNotified(this.isNotified);
        copy.setNotificationSentTo(this.notificationSentTo);
        copy.setPriority(this.priority);
        copy.setCreatedAt(this.getCreatedAt());
        copy.setUpdatedAt(this.getUpdatedAt());
        return copy;
    }
}