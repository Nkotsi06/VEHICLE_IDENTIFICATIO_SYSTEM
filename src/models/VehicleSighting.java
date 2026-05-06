package models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * VehicleSighting model representing a vehicle spotted by cameras or ANPR systems.
 * Used for vehicle tracking and movement reconstruction.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class VehicleSighting extends BaseEntity {

    // Core fields
    private int id;
    private Integer vehicleId;  // Can be null for vehicles not in system
    private String licensePlate;
    private String registrationNumber;
    private String make;
    private String model;
    private String color;
    private String sourceType;
    private String sourceDeviceId;
    private double latitude;
    private double longitude;
    private LocalDateTime timestamp;
    private double confidenceScore;
    private String direction;
    private String additionalData;
    private Integer sequenceNumber;
    private LocalDateTime previousTimestamp;
    private Double previousLatitude;
    private Double previousLongitude;
    private Double estimatedSpeed;
    private Double distanceFromPrevious;

    // ADDED FIELDS
    private String imagePath;
    private String alertStatus;

    // Source type constants
    public static final String SOURCE_TRAFFIC_CAMERA = "TRAFFIC_CAMERA";
    public static final String SOURCE_ANPR = "ANPR";
    public static final String SOURCE_TOLL_GATE = "TOLL_GATE";
    public static final String SOURCE_PARKING_LOT = "PARKING_LOT";
    public static final String SOURCE_GAS_STATION = "GAS_STATION";
    public static final String SOURCE_MOBILE_PATROL = "MOBILE_PATROL";

    // Direction constants
    public static final String DIRECTION_NORTH = "N";
    public static final String DIRECTION_SOUTH = "S";
    public static final String DIRECTION_EAST = "E";
    public static final String DIRECTION_WEST = "W";
    public static final String DIRECTION_NORTHEAST = "NE";
    public static final String DIRECTION_NORTHWEST = "NW";
    public static final String DIRECTION_SOUTHEAST = "SE";
    public static final String DIRECTION_SOUTHWEST = "SW";

    // JavaFX Properties for TableView binding
    private final ObjectProperty<LocalDateTime> timestampProperty = new SimpleObjectProperty<>();
    private final StringProperty licensePlateProperty = new SimpleStringProperty();
    private final StringProperty sourceDeviceIdProperty = new SimpleStringProperty();
    private final DoubleProperty confidenceScoreProperty = new SimpleDoubleProperty();
    private final StringProperty sourceTypeProperty = new SimpleStringProperty();
    private final StringProperty locationProperty = new SimpleStringProperty();
    private final StringProperty sourceIconProperty = new SimpleStringProperty();
    private final StringProperty formattedTimestampProperty = new SimpleStringProperty();
    private final StringProperty confidenceDisplayProperty = new SimpleStringProperty();

    // ADDED PROPERTIES
    private final StringProperty imagePathProperty = new SimpleStringProperty();
    private final StringProperty alertStatusProperty = new SimpleStringProperty();

    /**
     * Default constructor.
     */
    public VehicleSighting() {
        super();
        this.confidenceScore = 1.0;
        this.vehicleId = null;

        confidenceScoreProperty.set(1.0);
        updateDerivedProperties();

        confidenceScoreProperty.addListener((obs, oldVal, newVal) -> updateDerivedProperties());
        timestampProperty.addListener((obs, oldVal, newVal) -> updateDerivedProperties());
        latitudeProperty.addListener((obs, oldVal, newVal) -> updateDerivedProperties());
        longitudeProperty.addListener((obs, oldVal, newVal) -> updateDerivedProperties());
        sourceTypeProperty.addListener((obs, oldVal, newVal) -> updateDerivedProperties());
    }

    // Additional JavaFX properties not in original
    private final DoubleProperty latitudeProperty = new SimpleDoubleProperty();
    private final DoubleProperty longitudeProperty = new SimpleDoubleProperty();

    /**
     * Constructor with required fields.
     *
     * @param vehicleId    the vehicle ID (can be null)
     * @param licensePlate the license plate
     * @param sourceType   the source type
     * @param latitude     the latitude
     * @param longitude    the longitude
     * @param timestamp    the timestamp
     */
    public VehicleSighting(Integer vehicleId, String licensePlate, String sourceType,
                           double latitude, double longitude, LocalDateTime timestamp) {
        this();
        this.vehicleId = vehicleId;
        this.licensePlate = licensePlate;
        this.sourceType = sourceType;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;

        licensePlateProperty.set(licensePlate);
        timestampProperty.set(timestamp);
        sourceTypeProperty.set(sourceType);
        latitudeProperty.set(latitude);
        longitudeProperty.set(longitude);
    }

    // ============================================
    // PRIVATE UPDATE METHODS
    // ============================================

    private void updateDerivedProperties() {
        // Update formatted timestamp
        if (timestamp != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            formattedTimestampProperty.set(timestamp.format(formatter));
        } else {
            formattedTimestampProperty.set("");
        }

        // Update location display
        locationProperty.set(String.format("(%.6f, %.6f)", latitude, longitude));

        // Update confidence display
        confidenceDisplayProperty.set(String.format("%.0f%%", confidenceScore * 100));

        // Update source icon
        switch (sourceType) {
            case SOURCE_TRAFFIC_CAMERA:
                sourceIconProperty.set("📹 Camera");
                break;
            case SOURCE_ANPR:
                sourceIconProperty.set("🔍 ANPR");
                break;
            case SOURCE_TOLL_GATE:
                sourceIconProperty.set("🛣️ Toll");
                break;
            case SOURCE_PARKING_LOT:
                sourceIconProperty.set("🅿️ Parking");
                break;
            case SOURCE_GAS_STATION:
                sourceIconProperty.set("⛽ Gas");
                break;
            case SOURCE_MOBILE_PATROL:
                sourceIconProperty.set("🚓 Patrol");
                break;
            default:
                sourceIconProperty.set("📍 Sighting");
        }
    }

    // ============================================
    // GETTERS AND SETTERS WITH PROPERTY UPDATES
    // ============================================

    public Integer getVehicleId() { return vehicleId; }
    public void setVehicleId(Integer vehicleId) { this.vehicleId = vehicleId; }

    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
        licensePlateProperty.set(licensePlate);
    }
    public StringProperty licensePlateProperty() { return licensePlateProperty; }

    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }

    public String getMake() { return make; }
    public void setMake(String make) { this.make = make; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
        sourceTypeProperty.set(sourceType);
    }
    public StringProperty sourceTypeProperty() { return sourceTypeProperty; }

    public String getSourceDeviceId() { return sourceDeviceId; }
    public void setSourceDeviceId(String sourceDeviceId) {
        this.sourceDeviceId = sourceDeviceId;
        sourceDeviceIdProperty.set(sourceDeviceId);
    }
    public StringProperty sourceDeviceIdProperty() { return sourceDeviceIdProperty; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) {
        this.latitude = latitude;
        latitudeProperty.set(latitude);
    }
    public DoubleProperty latitudeProperty() { return latitudeProperty; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) {
        this.longitude = longitude;
        longitudeProperty.set(longitude);
    }
    public DoubleProperty longitudeProperty() { return longitudeProperty; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
        timestampProperty.set(timestamp);
    }
    public ObjectProperty<LocalDateTime> timestampProperty() { return timestampProperty; }

    public double getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(double confidenceScore) {
        this.confidenceScore = confidenceScore;
        confidenceScoreProperty.set(confidenceScore);
    }
    public DoubleProperty confidenceScoreProperty() { return confidenceScoreProperty; }

    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }

    public String getAdditionalData() { return additionalData; }
    public void setAdditionalData(String additionalData) { this.additionalData = additionalData; }

    public Integer getSequenceNumber() { return sequenceNumber; }
    public void setSequenceNumber(Integer sequenceNumber) { this.sequenceNumber = sequenceNumber; }

    public LocalDateTime getPreviousTimestamp() { return previousTimestamp; }
    public void setPreviousTimestamp(LocalDateTime previousTimestamp) { this.previousTimestamp = previousTimestamp; }

    public Double getPreviousLatitude() { return previousLatitude; }
    public void setPreviousLatitude(Double previousLatitude) { this.previousLatitude = previousLatitude; }

    public Double getPreviousLongitude() { return previousLongitude; }
    public void setPreviousLongitude(Double previousLongitude) { this.previousLongitude = previousLongitude; }

    public Double getEstimatedSpeed() { return estimatedSpeed; }
    public void setEstimatedSpeed(Double estimatedSpeed) { this.estimatedSpeed = estimatedSpeed; }

    public Double getDistanceFromPrevious() { return distanceFromPrevious; }
    public void setDistanceFromPrevious(Double distanceFromPrevious) { this.distanceFromPrevious = distanceFromPrevious; }

    // ============================================
    // ADDED GETTERS AND SETTERS
    // ============================================

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
        imagePathProperty.set(imagePath);
    }
    public StringProperty imagePathProperty() { return imagePathProperty; }

    public String getAlertStatus() { return alertStatus; }
    public void setAlertStatus(String alertStatus) {
        this.alertStatus = alertStatus;
        alertStatusProperty.set(alertStatus);
    }
    public StringProperty alertStatusProperty() { return alertStatusProperty; }

    // ============================================
    // DERIVED PROPERTY GETTERS
    // ============================================

    public String getSourceIcon() { return sourceIconProperty.get(); }
    public StringProperty sourceIconProperty() { return sourceIconProperty; }

    public String getLocation() { return locationProperty.get(); }
    public StringProperty locationProperty() { return locationProperty; }

    public String getFormattedTimestamp() { return formattedTimestampProperty.get(); }
    public StringProperty formattedTimestampProperty() { return formattedTimestampProperty; }

    public String getConfidenceDisplay() { return confidenceDisplayProperty.get(); }
    public StringProperty confidenceDisplayProperty() { return confidenceDisplayProperty; }

    // ============================================
    // BUSINESS LOGIC METHODS
    // ============================================

    public boolean isHighConfidence() {
        return confidenceScore >= 0.9;
    }

    public String getSourceTypeDisplay() {
        switch (sourceType) {
            case SOURCE_TRAFFIC_CAMERA: return "Traffic Camera";
            case SOURCE_ANPR: return "ANPR System";
            case SOURCE_TOLL_GATE: return "Toll Gate";
            case SOURCE_PARKING_LOT: return "Parking Lot";
            case SOURCE_GAS_STATION: return "Gas Station";
            case SOURCE_MOBILE_PATROL: return "Mobile Patrol";
            default: return sourceType != null ? sourceType.replace("_", " ") : "Unknown";
        }
    }

    public String getFormattedSpeed() {
        if (estimatedSpeed == null) return "N/A";
        return String.format("%.1f km/h", estimatedSpeed);
    }

    public String getFormattedDistance() {
        if (distanceFromPrevious == null) return "N/A";
        if (distanceFromPrevious < 1) {
            return String.format("%.0f meters", distanceFromPrevious * 1000);
        }
        return String.format("%.2f km", distanceFromPrevious);
    }

    public boolean isSpeeding() {
        return estimatedSpeed != null && estimatedSpeed > 120;
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
        return getSourceTypeDisplay() + " - " + getFormattedTimestamp() + " - " + getLocation();
    }

    /**
     * Creates a copy of this vehicle sighting.
     *
     * @return a new VehicleSighting instance
     */
    public VehicleSighting copy() {
        VehicleSighting copy = new VehicleSighting();
        copy.setId(this.id);
        copy.setVehicleId(this.vehicleId);
        copy.setLicensePlate(this.licensePlate);
        copy.setRegistrationNumber(this.registrationNumber);
        copy.setMake(this.make);
        copy.setModel(this.model);
        copy.setColor(this.color);
        copy.setSourceType(this.sourceType);
        copy.setSourceDeviceId(this.sourceDeviceId);
        copy.setLatitude(this.latitude);
        copy.setLongitude(this.longitude);
        copy.setTimestamp(this.timestamp);
        copy.setConfidenceScore(this.confidenceScore);
        copy.setDirection(this.direction);
        copy.setAdditionalData(this.additionalData);
        copy.setSequenceNumber(this.sequenceNumber);
        copy.setPreviousTimestamp(this.previousTimestamp);
        copy.setPreviousLatitude(this.previousLatitude);
        copy.setPreviousLongitude(this.previousLongitude);
        copy.setEstimatedSpeed(this.estimatedSpeed);
        copy.setDistanceFromPrevious(this.distanceFromPrevious);
        copy.setImagePath(this.imagePath);
        copy.setAlertStatus(this.alertStatus);
        copy.setCreatedAt(this.getCreatedAt());
        copy.setUpdatedAt(this.getUpdatedAt());
        return copy;
    }
}