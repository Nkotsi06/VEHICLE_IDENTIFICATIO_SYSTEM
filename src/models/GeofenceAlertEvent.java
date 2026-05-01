package models;

import java.time.LocalDateTime;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class GeofenceAlertEvent extends BaseEntity {
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

    // JavaFX Properties for TableView binding
    private final StringProperty zoneNameProperty = new SimpleStringProperty();
    private final StringProperty registrationNumberProperty = new SimpleStringProperty();
    private final StringProperty alertTypeProperty = new SimpleStringProperty();
    private final ObjectProperty<LocalDateTime> alertTimestampProperty = new SimpleObjectProperty<>();
    private final BooleanProperty notifiedProperty = new SimpleBooleanProperty();

    public GeofenceAlertEvent() {
        super();
        this.alertTimestamp = LocalDateTime.now();
        this.isNotified = false;
    }

    public GeofenceAlertEvent(int geofenceZoneId, int vehicleId, String alertType) {
        this();
        this.geofenceZoneId = geofenceZoneId;
        this.vehicleId = vehicleId;
        this.alertType = alertType;

        // Update properties
        alertTypeProperty.set(alertType);
        alertTimestampProperty.set(alertTimestamp);
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
    }

    public String getEventIcon() {
        return "ENTER".equals(alertType) ? "ENTER" : "EXIT";
    }

    public String getEventColor() {
        if ("ENTER".equals(alertType)) {
            return "#F44336";
        } else {
            return "#4CAF50";
        }
    }

    public void calculatePriority() {
        if ("HIGH_CRIME".equals(zoneType) && "ENTER".equals(alertType)) {
            this.priority = "HIGH";
        } else if ("SCHOOL_ZONE".equals(zoneType) && "ENTER".equals(alertType)) {
            this.priority = "HIGH";
        } else if ("RESTRICTED".equals(zoneType)) {
            this.priority = "HIGH";
        } else {
            this.priority = "MEDIUM";
        }
    }

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
        return alertType + " - " + zoneName + " - " + registrationNumber + " at " + alertTimestamp;
    }
}