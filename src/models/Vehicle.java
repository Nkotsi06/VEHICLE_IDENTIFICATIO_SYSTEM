package models;

import java.time.LocalDateTime;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Vehicle extends BaseEntity {
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

    public Vehicle() {
        super();
    }

    public Vehicle(String registrationNumber, String make, String model, int year, int ownerId, String color) {
        this();
        this.registrationNumber = registrationNumber;
        this.make = make;
        this.model = model;
        this.year = year;
        this.ownerId = ownerId;
        this.color = color;

        this.registrationNumberProperty.set(registrationNumber);
        this.makeProperty.set(make);
        this.modelProperty.set(model);
        this.yearProperty.set(year);
        this.colorProperty.set(color);
    }

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
    }
    public StringProperty statusNameProperty() { return statusNameProperty; }

    public String getStatusColorCode() { return statusColorCode; }
    public void setStatusColorCode(String statusColorCode) { this.statusColorCode = statusColorCode; }

    public String getColor() { return color; }
    public void setColor(String color) {
        this.color = color;
        colorProperty.set(color);
    }
    public StringProperty colorProperty() { return colorProperty; }

    public String getEngineNumber() { return engineNumber; }
    public void setEngineNumber(String engineNumber) { this.engineNumber = engineNumber; }

    public String getChassisNumber() { return chassisNumber; }
    public void setChassisNumber(String chassisNumber) { this.chassisNumber = chassisNumber; }

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

    public String getFullName() {
        return make + " " + model + " (" + year + ") - " + registrationNumber;
    }

    @Override
    public int getId() { return id; }
    @Override
    public void setId(int id) { this.id = id; }

    @Override
    public String toString() {
        return registrationNumber + " - " + make + " " + model;
    }
}