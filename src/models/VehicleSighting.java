package models;

import java.time.LocalDateTime;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class VehicleSighting extends BaseEntity {
    private int id;
    private int vehicleId;
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

    // JavaFX Properties for TableView binding
    private final ObjectProperty<LocalDateTime> timestampProperty = new SimpleObjectProperty<>();
    private final StringProperty licensePlateProperty = new SimpleStringProperty();
    private final StringProperty sourceDeviceIdProperty = new SimpleStringProperty();
    private final DoubleProperty confidenceScoreProperty = new SimpleDoubleProperty();
    private final StringProperty sourceTypeProperty = new SimpleStringProperty();

    public VehicleSighting() {
        super();
        this.confidenceScore = 1.0;
    }

    public VehicleSighting(int vehicleId, String licensePlate, String sourceType,
                           double latitude, double longitude, LocalDateTime timestamp) {
        this();
        this.vehicleId = vehicleId;
        this.licensePlate = licensePlate;
        this.sourceType = sourceType;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;

        // Update properties
        licensePlateProperty.set(licensePlate);
        timestampProperty.set(timestamp);
        sourceTypeProperty.set(sourceType);
    }

    public int getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
        licensePlateProperty.set(licensePlate);
    }

    public StringProperty licensePlateProperty() {
        return licensePlateProperty;
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

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
        sourceTypeProperty.set(sourceType);
    }

    public StringProperty sourceTypeProperty() {
        return sourceTypeProperty;
    }

    public String getSourceDeviceId() {
        return sourceDeviceId;
    }

    public void setSourceDeviceId(String sourceDeviceId) {
        this.sourceDeviceId = sourceDeviceId;
        sourceDeviceIdProperty.set(sourceDeviceId);
    }

    public StringProperty sourceDeviceIdProperty() {
        return sourceDeviceIdProperty;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
        timestampProperty.set(timestamp);
    }

    public ObjectProperty<LocalDateTime> timestampProperty() {
        return timestampProperty;
    }

    public double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(double confidenceScore) {
        this.confidenceScore = confidenceScore;
        confidenceScoreProperty.set(confidenceScore);
    }

    public DoubleProperty confidenceScoreProperty() {
        return confidenceScoreProperty;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getAdditionalData() {
        return additionalData;
    }

    public void setAdditionalData(String additionalData) {
        this.additionalData = additionalData;
    }

    public Integer getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(Integer sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public LocalDateTime getPreviousTimestamp() {
        return previousTimestamp;
    }

    public void setPreviousTimestamp(LocalDateTime previousTimestamp) {
        this.previousTimestamp = previousTimestamp;
    }

    public Double getPreviousLatitude() {
        return previousLatitude;
    }

    public void setPreviousLatitude(Double previousLatitude) {
        this.previousLatitude = previousLatitude;
    }

    public Double getPreviousLongitude() {
        return previousLongitude;
    }

    public void setPreviousLongitude(Double previousLongitude) {
        this.previousLongitude = previousLongitude;
    }

    public Double getEstimatedSpeed() {
        return estimatedSpeed;
    }

    public void setEstimatedSpeed(Double estimatedSpeed) {
        this.estimatedSpeed = estimatedSpeed;
    }

    public Double getDistanceFromPrevious() {
        return distanceFromPrevious;
    }

    public void setDistanceFromPrevious(Double distanceFromPrevious) {
        this.distanceFromPrevious = distanceFromPrevious;
    }

    public String getSourceIcon() {
        switch (sourceType) {
            case "traffic_camera": return "CAM";
            case "toll_gate": return "TOLL";
            case "parking_lot": return "PARK";
            case "gas_station": return "GAS";
            case "anpr_system": return "ANPR";
            default: return "UNK";
        }
    }

    public boolean isHighConfidence() {
        return confidenceScore >= 0.9;
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
        return sourceType + " - " + timestamp + " - (" + latitude + ", " + longitude + ")";
    }
}