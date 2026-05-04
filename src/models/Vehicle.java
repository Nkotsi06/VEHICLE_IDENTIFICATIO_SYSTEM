package models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Vehicle model representing registered vehicles in the system.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class Vehicle extends BaseEntity {

    // Core fields
    private int id;
    private String registrationNumber;
    private String make;
    private String model;
    private int year;
    private int ownerId;
    private String ownerName;
    private int statusId;
    private String statusName;
    private String statusColorCode;
    private String color;
    private String engineNumber;
    private String chassisNumber;
    private Double currentLocationLat;
    private Double currentLocationLng;
    private LocalDateTime lastUpdatedLocation;
    private String description;

    // Status constants
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_STOLEN = "STOLEN";
    public static final String STATUS_IMPOUNDED = "IMPOUNDED";
    public static final String STATUS_SCRAPPED = "SCRAPPED";
    public static final String STATUS_SUSPENDED = "SUSPENDED";

    // JavaFX Properties for TableView binding
    private final StringProperty registrationNumberProperty = new SimpleStringProperty();
    private final StringProperty makeProperty = new SimpleStringProperty();
    private final StringProperty modelProperty = new SimpleStringProperty();
    private final IntegerProperty yearProperty = new SimpleIntegerProperty();
    private final StringProperty statusNameProperty = new SimpleStringProperty();
    private final StringProperty ownerNameProperty = new SimpleStringProperty();
    private final StringProperty colorProperty = new SimpleStringProperty();
    private final DoubleProperty currentLocationLatProperty = new SimpleDoubleProperty();
    private final DoubleProperty currentLocationLngProperty = new SimpleDoubleProperty();
    private final StringProperty engineNumberProperty = new SimpleStringProperty();
    private final StringProperty chassisNumberProperty = new SimpleStringProperty();
    private final StringProperty statusColorProperty = new SimpleStringProperty();
    private final StringProperty fullNameProperty = new SimpleStringProperty();

    /**
     * Default constructor.
     */
    public Vehicle() {
        super();
        updateFullName();

        makeProperty.addListener((obs, oldVal, newVal) -> updateFullName());
        modelProperty.addListener((obs, oldVal, newVal) -> updateFullName());
        yearProperty.addListener((obs, oldVal, newVal) -> updateFullName());
        registrationNumberProperty.addListener((obs, oldVal, newVal) -> updateFullName());
    }

    /**
     * Constructor for creating a new vehicle.
     *
     * @param registrationNumber the registration number
     * @param make               the make
     * @param model              the model
     * @param year               the year
     * @param ownerId            the owner ID
     * @param color              the color
     */
    public Vehicle(String registrationNumber, String make, String model, int year, int ownerId, String color) {
        this();
        this.registrationNumber = registrationNumber;
        this.make = make;
        this.model = model;
        this.year = year;
        this.ownerId = ownerId;
        this.color = color;

        registrationNumberProperty.set(registrationNumber);
        makeProperty.set(make);
        modelProperty.set(model);
        yearProperty.set(year);
        colorProperty.set(color);
    }

    // ============================================
    // PRIVATE UPDATE METHODS
    // ============================================

    private void updateFullName() {
        fullNameProperty.set(make + " " + model + " (" + year + ") - " + registrationNumber);
    }

    // ============================================
    // GETTERS AND SETTERS WITH PROPERTY UPDATES
    // ============================================

    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
        registrationNumberProperty.set(registrationNumber);
    }
    public StringProperty registrationNumberProperty() { return registrationNumberProperty; }

    public String getMake() { return make; }
    public void setMake(String make) {
        this.make = make;
        makeProperty.set(make);
    }
    public StringProperty makeProperty() { return makeProperty; }

    public String getModel() { return model; }
    public void setModel(String model) {
        this.model = model;
        modelProperty.set(model);
    }
    public StringProperty modelProperty() { return modelProperty; }

    public int getYear() { return year; }
    public void setYear(int year) {
        this.year = year;
        yearProperty.set(year);
    }
    public IntegerProperty yearProperty() { return yearProperty; }

    public int getOwnerId() { return ownerId; }
    public void setOwnerId(int ownerId) { this.ownerId = ownerId; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
        ownerNameProperty.set(ownerName);
    }
    public StringProperty ownerNameProperty() { return ownerNameProperty; }

    public int getStatusId() { return statusId; }
    public void setStatusId(int statusId) { this.statusId = statusId; }

    public String getStatusName() { return statusName; }
    public void setStatusName(String statusName) {
        this.statusName = statusName;
        statusNameProperty.set(statusName);
        updateStatusColor();
    }
    public StringProperty statusNameProperty() { return statusNameProperty; }

    private void updateStatusColor() {
        switch (statusName) {
            case STATUS_ACTIVE: statusColorProperty.set("#4CAF50"); break;
            case STATUS_STOLEN: statusColorProperty.set("#F44336"); break;
            case STATUS_IMPOUNDED: statusColorProperty.set("#FF9800"); break;
            case STATUS_SCRAPPED: statusColorProperty.set("#9E9E9E"); break;
            case STATUS_SUSPENDED: statusColorProperty.set("#FFC107"); break;
            default: statusColorProperty.set("#9E9E9E");
        }
    }

    public String getStatusColorCode() { return statusColorCode; }
    public void setStatusColorCode(String statusColorCode) { this.statusColorCode = statusColorCode; }
    public StringProperty statusColorProperty() { return statusColorProperty; }

    public String getColor() { return color; }
    public void setColor(String color) {
        this.color = color;
        colorProperty.set(color);
    }
    public StringProperty colorProperty() { return colorProperty; }

    public String getEngineNumber() { return engineNumber; }
    public void setEngineNumber(String engineNumber) {
        this.engineNumber = engineNumber;
        engineNumberProperty.set(engineNumber);
    }
    public StringProperty engineNumberProperty() { return engineNumberProperty; }

    public String getChassisNumber() { return chassisNumber; }
    public void setChassisNumber(String chassisNumber) {
        this.chassisNumber = chassisNumber;
        chassisNumberProperty.set(chassisNumber);
    }
    public StringProperty chassisNumberProperty() { return chassisNumberProperty; }

    public Double getCurrentLocationLat() { return currentLocationLat; }
    public void setCurrentLocationLat(Double currentLocationLat) {
        this.currentLocationLat = currentLocationLat;
        if (currentLocationLat != null) currentLocationLatProperty.set(currentLocationLat);
    }
    public DoubleProperty currentLocationLatProperty() { return currentLocationLatProperty; }

    public Double getCurrentLocationLng() { return currentLocationLng; }
    public void setCurrentLocationLng(Double currentLocationLng) {
        this.currentLocationLng = currentLocationLng;
        if (currentLocationLng != null) currentLocationLngProperty.set(currentLocationLng);
    }
    public DoubleProperty currentLocationLngProperty() { return currentLocationLngProperty; }

    public LocalDateTime getLastUpdatedLocation() { return lastUpdatedLocation; }
    public void setLastUpdatedLocation(LocalDateTime lastUpdatedLocation) { this.lastUpdatedLocation = lastUpdatedLocation; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getFullName() { return fullNameProperty.get(); }
    public StringProperty fullNameProperty() { return fullNameProperty; }

    // ============================================
    // BUSINESS LOGIC METHODS
    // ============================================

    public String getFormattedLastUpdatedLocation() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return lastUpdatedLocation != null ? lastUpdatedLocation.format(formatter) : "Never";
    }

    public boolean hasLocation() {
        return currentLocationLat != null && currentLocationLng != null;
    }

    public boolean isActive() {
        return STATUS_ACTIVE.equals(statusName);
    }

    public boolean isStolen() {
        return STATUS_STOLEN.equals(statusName);
    }

    public int getVehicleAge() {
        return LocalDateTime.now().getYear() - year;
    }

    public String getStatusDisplay() {
        return statusName != null ? statusName.replace("_", " ") : "Unknown";
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
        return registrationNumber + " - " + make + " " + model;
    }

    /**
     * Creates a copy of this vehicle.
     *
     * @return a new Vehicle instance
     */
    public Vehicle copy() {
        Vehicle copy = new Vehicle();
        copy.setId(this.id);
        copy.setRegistrationNumber(this.registrationNumber);
        copy.setMake(this.make);
        copy.setModel(this.model);
        copy.setYear(this.year);
        copy.setOwnerId(this.ownerId);
        copy.setOwnerName(this.ownerName);
        copy.setStatusId(this.statusId);
        copy.setStatusName(this.statusName);
        copy.setStatusColorCode(this.statusColorCode);
        copy.setColor(this.color);
        copy.setEngineNumber(this.engineNumber);
        copy.setChassisNumber(this.chassisNumber);
        copy.setCurrentLocationLat(this.currentLocationLat);
        copy.setCurrentLocationLng(this.currentLocationLng);
        copy.setLastUpdatedLocation(this.lastUpdatedLocation);
        copy.setDescription(this.description);
        copy.setCreatedAt(this.getCreatedAt());
        copy.setUpdatedAt(this.getUpdatedAt());
        return copy;
    }
}