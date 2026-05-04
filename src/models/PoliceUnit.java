package models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * PoliceUnit model representing mobile police units for patrol and tracking.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class PoliceUnit extends BaseEntity {

    // Core fields
    private int id;
    private String unitId;
    private String officerName;
    private String badgeNumber;
    private Double currentLocationLat;
    private Double currentLocationLng;
    private LocalDateTime lastLocationUpdate;
    private String status;
    private String deviceId;
    private Double batteryLevel;
    private String vehicleRegistration;

    // Status constants
    public static final String STATUS_AVAILABLE = "AVAILABLE";
    public static final String STATUS_ON_PATROL = "ON_PATROL";
    public static final String STATUS_BUSY = "BUSY";
    public static final String STATUS_OFF_DUTY = "OFF_DUTY";
    public static final String STATUS_OUT_OF_SERVICE = "OUT_OF_SERVICE";

    // Status display mappings
    private static final java.util.Map<String, String> STATUS_DISPLAY = new java.util.HashMap<>();
    private static final java.util.Map<String, String> STATUS_COLOR = new java.util.HashMap<>();
    static {
        STATUS_DISPLAY.put(STATUS_AVAILABLE, "Available");
        STATUS_DISPLAY.put(STATUS_ON_PATROL, "On Patrol");
        STATUS_DISPLAY.put(STATUS_BUSY, "Busy");
        STATUS_DISPLAY.put(STATUS_OFF_DUTY, "Off Duty");
        STATUS_DISPLAY.put(STATUS_OUT_OF_SERVICE, "Out of Service");

        STATUS_COLOR.put(STATUS_AVAILABLE, "#4CAF50");
        STATUS_COLOR.put(STATUS_ON_PATROL, "#2196F3");
        STATUS_COLOR.put(STATUS_BUSY, "#FF9800");
        STATUS_COLOR.put(STATUS_OFF_DUTY, "#9E9E9E");
        STATUS_COLOR.put(STATUS_OUT_OF_SERVICE, "#F44336");
    }

    // JavaFX Properties
    private final StringProperty unitIdProperty = new SimpleStringProperty();
    private final StringProperty officerNameProperty = new SimpleStringProperty();
    private final StringProperty badgeNumberProperty = new SimpleStringProperty();
    private final DoubleProperty currentLocationLatProperty = new SimpleDoubleProperty();
    private final DoubleProperty currentLocationLngProperty = new SimpleDoubleProperty();
    private final ObjectProperty<LocalDateTime> lastLocationUpdateProperty = new SimpleObjectProperty<>();
    private final StringProperty statusProperty = new SimpleStringProperty();
    private final StringProperty deviceIdProperty = new SimpleStringProperty();
    private final DoubleProperty batteryLevelProperty = new SimpleDoubleProperty();
    private final StringProperty vehicleRegistrationProperty = new SimpleStringProperty();
    private final StringProperty statusDisplayProperty = new SimpleStringProperty();
    private final StringProperty statusColorProperty = new SimpleStringProperty();
    private final StringProperty locationDisplayProperty = new SimpleStringProperty();
    private final StringProperty batteryDisplayProperty = new SimpleStringProperty();

    /**
     * Default constructor - initializes with AVAILABLE status.
     */
    public PoliceUnit() {
        super();
        this.status = STATUS_AVAILABLE;
        this.batteryLevel = 100.0;
        this.lastLocationUpdate = LocalDateTime.now();

        statusProperty.set(STATUS_AVAILABLE);
        batteryLevelProperty.set(100.0);
        lastLocationUpdateProperty.set(lastLocationUpdate);
        updateStatusDisplay();
        updateLocationDisplay();
        updateBatteryDisplay();

        statusProperty.addListener((obs, oldVal, newVal) -> updateStatusDisplay());
        currentLocationLatProperty.addListener((obs, oldVal, newVal) -> updateLocationDisplay());
        currentLocationLngProperty.addListener((obs, oldVal, newVal) -> updateLocationDisplay());
        batteryLevelProperty.addListener((obs, oldVal, newVal) -> updateBatteryDisplay());
    }

    /**
     * Constructor for creating a new police unit.
     *
     * @param unitId       the unit ID
     * @param officerName  the officer name
     * @param badgeNumber  the badge number
     */
    public PoliceUnit(String unitId, String officerName, String badgeNumber) {
        this();
        this.unitId = unitId;
        this.officerName = officerName;
        this.badgeNumber = badgeNumber;

        unitIdProperty.set(unitId);
        officerNameProperty.set(officerName);
        badgeNumberProperty.set(badgeNumber);
    }

    // ============================================
    // PRIVATE UPDATE METHODS
    // ============================================

    private void updateStatusDisplay() {
        statusDisplayProperty.set(STATUS_DISPLAY.getOrDefault(status, status));
        statusColorProperty.set(STATUS_COLOR.getOrDefault(status, "#9E9E9E"));
    }

    private void updateLocationDisplay() {
        Double lat = currentLocationLatProperty.get();
        Double lng = currentLocationLngProperty.get();
        if (lat != null && lng != null) {
            locationDisplayProperty.set(String.format("(%.6f, %.6f)", lat, lng));
        } else {
            locationDisplayProperty.set("Not available");
        }
    }

    private void updateBatteryDisplay() {
        Double battery = batteryLevelProperty.get();
        if (battery != null) {
            batteryDisplayProperty.set(String.format("%.0f%%", battery));
        } else {
            batteryDisplayProperty.set("N/A");
        }
    }

    // ============================================
    // GETTERS AND SETTERS WITH PROPERTY UPDATES
    // ============================================

    public String getUnitId() {
        return unitId;
    }

    public void setUnitId(String unitId) {
        this.unitId = unitId;
        unitIdProperty.set(unitId);
    }

    public StringProperty unitIdProperty() {
        return unitIdProperty;
    }

    public String getOfficerName() {
        return officerName;
    }

    public void setOfficerName(String officerName) {
        this.officerName = officerName;
        officerNameProperty.set(officerName);
    }

    public StringProperty officerNameProperty() {
        return officerNameProperty;
    }

    public String getBadgeNumber() {
        return badgeNumber;
    }

    public void setBadgeNumber(String badgeNumber) {
        this.badgeNumber = badgeNumber;
        badgeNumberProperty.set(badgeNumber);
    }

    public StringProperty badgeNumberProperty() {
        return badgeNumberProperty;
    }

    public Double getCurrentLocationLat() {
        return currentLocationLat;
    }

    public void setCurrentLocationLat(Double currentLocationLat) {
        this.currentLocationLat = currentLocationLat;
        currentLocationLatProperty.set(currentLocationLat);
    }

    public DoubleProperty currentLocationLatProperty() {
        return currentLocationLatProperty;
    }

    public Double getCurrentLocationLng() {
        return currentLocationLng;
    }

    public void setCurrentLocationLng(Double currentLocationLng) {
        this.currentLocationLng = currentLocationLng;
        currentLocationLngProperty.set(currentLocationLng);
    }

    public DoubleProperty currentLocationLngProperty() {
        return currentLocationLngProperty;
    }

    public LocalDateTime getLastLocationUpdate() {
        return lastLocationUpdate;
    }

    public void setLastLocationUpdate(LocalDateTime lastLocationUpdate) {
        this.lastLocationUpdate = lastLocationUpdate;
        lastLocationUpdateProperty.set(lastLocationUpdate);
    }

    public ObjectProperty<LocalDateTime> lastLocationUpdateProperty() {
        return lastLocationUpdateProperty;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        statusProperty.set(status);
    }

    public StringProperty statusProperty() {
        return statusProperty;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
        deviceIdProperty.set(deviceId);
    }

    public StringProperty deviceIdProperty() {
        return deviceIdProperty;
    }

    public Double getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(Double batteryLevel) {
        this.batteryLevel = batteryLevel;
        batteryLevelProperty.set(batteryLevel);
    }

    public DoubleProperty batteryLevelProperty() {
        return batteryLevelProperty;
    }

    public String getVehicleRegistration() {
        return vehicleRegistration;
    }

    public void setVehicleRegistration(String vehicleRegistration) {
        this.vehicleRegistration = vehicleRegistration;
        vehicleRegistrationProperty.set(vehicleRegistration);
    }

    public StringProperty vehicleRegistrationProperty() {
        return vehicleRegistrationProperty;
    }

    public String getStatusDisplay() {
        return statusDisplayProperty.get();
    }

    public StringProperty statusDisplayProperty() {
        return statusDisplayProperty;
    }

    public String getStatusColor() {
        return statusColorProperty.get();
    }

    public StringProperty statusColorProperty() {
        return statusColorProperty;
    }

    public String getLocationDisplay() {
        return locationDisplayProperty.get();
    }

    public StringProperty locationDisplayProperty() {
        return locationDisplayProperty;
    }

    public String getBatteryDisplay() {
        return batteryDisplayProperty.get();
    }

    public StringProperty batteryDisplayProperty() {
        return batteryDisplayProperty;
    }

    // ============================================
    // BUSINESS LOGIC METHODS
    // ============================================

    public boolean isOnPatrol() {
        return STATUS_ON_PATROL.equals(status);
    }

    public boolean isAvailable() {
        return STATUS_AVAILABLE.equals(status);
    }

    public boolean isBusy() {
        return STATUS_BUSY.equals(status);
    }

    public boolean isOffDuty() {
        return STATUS_OFF_DUTY.equals(status);
    }

    public boolean isLowBattery() {
        return batteryLevel != null && batteryLevel < 20.0;
    }

    public boolean isCriticalBattery() {
        return batteryLevel != null && batteryLevel < 10.0;
    }

    public String getFormattedLastLocationUpdate() {
        if (lastLocationUpdate == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return lastLocationUpdate.format(formatter);
    }

    public void setOnPatrol() {
        setStatus(STATUS_ON_PATROL);
    }

    public void setAvailable() {
        setStatus(STATUS_AVAILABLE);
    }

    public void setBusy() {
        setStatus(STATUS_BUSY);
    }

    public void setOffDuty() {
        setStatus(STATUS_OFF_DUTY);
    }

    public void updateLocation(double latitude, double longitude) {
        this.currentLocationLat = latitude;
        this.currentLocationLng = longitude;
        this.lastLocationUpdate = LocalDateTime.now();
        currentLocationLatProperty.set(latitude);
        currentLocationLngProperty.set(longitude);
        lastLocationUpdateProperty.set(lastLocationUpdate);
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
        return unitId + " - " + officerName + " (" + getStatusDisplay() + ")";
    }

    /**
     * Creates a copy of this police unit.
     *
     * @return a new PoliceUnit instance
     */
    public PoliceUnit copy() {
        PoliceUnit copy = new PoliceUnit();
        copy.setId(this.id);
        copy.setUnitId(this.unitId);
        copy.setOfficerName(this.officerName);
        copy.setBadgeNumber(this.badgeNumber);
        copy.setCurrentLocationLat(this.currentLocationLat);
        copy.setCurrentLocationLng(this.currentLocationLng);
        copy.setLastLocationUpdate(this.lastLocationUpdate);
        copy.setStatus(this.status);
        copy.setDeviceId(this.deviceId);
        copy.setBatteryLevel(this.batteryLevel);
        copy.setVehicleRegistration(this.vehicleRegistration);
        copy.setCreatedAt(this.getCreatedAt());
        copy.setUpdatedAt(this.getUpdatedAt());
        return copy;
    }
}