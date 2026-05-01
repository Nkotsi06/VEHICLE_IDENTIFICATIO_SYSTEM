package models;

import java.time.LocalDate;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ServiceRecord extends BaseEntity {
    private int id;
    private int vehicleId;
    private String registrationNumber;
    private String make;
    private String model;
    private int workshopId;
    private String workshopName;
    private int mechanicId;
    private String mechanicName;
    private LocalDate serviceDate;
    private String serviceType;
    private String description;
    private double cost;
    private int odometerReading;
    private String status;

    // JavaFX Properties for TableView binding
    private final StringProperty registrationNumberProperty = new SimpleStringProperty();
    private final StringProperty makeProperty = new SimpleStringProperty();
    private final StringProperty modelProperty = new SimpleStringProperty();
    private final StringProperty workshopNameProperty = new SimpleStringProperty();
    private final StringProperty mechanicNameProperty = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> serviceDateProperty = new SimpleObjectProperty<>();
    private final StringProperty serviceTypeProperty = new SimpleStringProperty();
    private final DoubleProperty costProperty = new SimpleDoubleProperty();
    private final IntegerProperty odometerReadingProperty = new SimpleIntegerProperty();
    private final StringProperty statusProperty = new SimpleStringProperty();

    public ServiceRecord() {
        super();
    }

    public ServiceRecord(int vehicleId, int workshopId, LocalDate serviceDate,
                         String serviceType, String description, double cost) {
        this();
        this.vehicleId = vehicleId;
        this.workshopId = workshopId;
        this.serviceDate = serviceDate;
        this.serviceType = serviceType;
        this.description = description;
        this.cost = cost;

        // Update properties
        serviceDateProperty.set(serviceDate);
        serviceTypeProperty.set(serviceType);
        costProperty.set(cost);
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
        makeProperty.set(make);
    }

    public StringProperty makeProperty() {
        return makeProperty;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
        modelProperty.set(model);
    }

    public StringProperty modelProperty() {
        return modelProperty;
    }

    public int getWorkshopId() {
        return workshopId;
    }

    public void setWorkshopId(int workshopId) {
        this.workshopId = workshopId;
    }

    public String getWorkshopName() {
        return workshopName;
    }

    public void setWorkshopName(String workshopName) {
        this.workshopName = workshopName;
        workshopNameProperty.set(workshopName);
    }

    public StringProperty workshopNameProperty() {
        return workshopNameProperty;
    }

    public int getMechanicId() {
        return mechanicId;
    }

    public void setMechanicId(int mechanicId) {
        this.mechanicId = mechanicId;
    }

    public String getMechanicName() {
        return mechanicName;
    }

    public void setMechanicName(String mechanicName) {
        this.mechanicName = mechanicName;
        mechanicNameProperty.set(mechanicName);
    }

    public StringProperty mechanicNameProperty() {
        return mechanicNameProperty;
    }

    public LocalDate getServiceDate() {
        return serviceDate;
    }

    public void setServiceDate(LocalDate serviceDate) {
        this.serviceDate = serviceDate;
        serviceDateProperty.set(serviceDate);
    }

    public ObjectProperty<LocalDate> serviceDateProperty() {
        return serviceDateProperty;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
        serviceTypeProperty.set(serviceType);
    }

    public StringProperty serviceTypeProperty() {
        return serviceTypeProperty;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
        costProperty.set(cost);
    }

    public DoubleProperty costProperty() {
        return costProperty;
    }

    public int getOdometerReading() {
        return odometerReading;
    }

    public void setOdometerReading(int odometerReading) {
        this.odometerReading = odometerReading;
        odometerReadingProperty.set(odometerReading);
    }

    public IntegerProperty odometerReadingProperty() {
        return odometerReadingProperty;
    }

    // Status field - for workshop dashboard
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

    // Helper method to calculate status based on service date
    public void calculateStatus() {
        if (serviceDate != null) {
            LocalDate today = LocalDate.now();
            if (serviceDate.equals(today)) {
                status = "TODAY";
            } else if (serviceDate.isAfter(today)) {
                status = "SCHEDULED";
            } else {
                status = "COMPLETED";
            }
        } else {
            status = "PENDING";
        }
        statusProperty.set(status);
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
        return serviceType + " - " + serviceDate + " - $" + cost;
    }
}