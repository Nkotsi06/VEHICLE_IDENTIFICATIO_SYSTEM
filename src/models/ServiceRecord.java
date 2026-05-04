package models;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * ServiceRecord model representing vehicle service history.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class ServiceRecord extends BaseEntity {

    // Core fields
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

    // Service type constants
    public static final String TYPE_OIL_CHANGE = "OIL_CHANGE";
    public static final String TYPE_TUNE_UP = "TUNE_UP";
    public static final String TYPE_BRAKE_REPAIR = "BRAKE_REPAIR";
    public static final String TYPE_ENGINE_REPAIR = "ENGINE_REPAIR";
    public static final String TYPE_TRANSMISSION = "TRANSMISSION";
    public static final String TYPE_TIRE_REPLACEMENT = "TIRE_REPLACEMENT";
    public static final String TYPE_BATTERY_REPLACEMENT = "BATTERY_REPLACEMENT";
    public static final String TYPE_GENERAL_INSPECTION = "GENERAL_INSPECTION";

    // Status constants
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_CANCELLED = "CANCELLED";

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
    private final StringProperty formattedCostProperty = new SimpleStringProperty();
    private final StringProperty statusDisplayProperty = new SimpleStringProperty();
    private final StringProperty statusColorProperty = new SimpleStringProperty();

    /**
     * Default constructor.
     */
    public ServiceRecord() {
        super();
        this.status = STATUS_COMPLETED;

        statusProperty.set(STATUS_COMPLETED);
        updateStatusDisplay();

        statusProperty.addListener((obs, oldVal, newVal) -> updateStatusDisplay());
        costProperty.addListener((obs, oldVal, newVal) -> updateFormattedCost());

        updateFormattedCost();
    }

    /**
     * Constructor for creating a new service record.
     *
     * @param vehicleId     the vehicle ID
     * @param workshopId    the workshop ID
     * @param serviceDate   the service date
     * @param serviceType   the service type
     * @param description   the service description
     * @param cost          the service cost
     */
    public ServiceRecord(int vehicleId, int workshopId, LocalDate serviceDate,
                         String serviceType, String description, double cost) {
        this();
        this.vehicleId = vehicleId;
        this.workshopId = workshopId;
        this.serviceDate = serviceDate;
        this.serviceType = serviceType;
        this.description = description;
        this.cost = cost;

        serviceDateProperty.set(serviceDate);
        serviceTypeProperty.set(serviceType);
        costProperty.set(cost);
    }

    // ============================================
    // PRIVATE UPDATE METHODS
    // ============================================

    private void updateFormattedCost() {
        formattedCostProperty.set(String.format("M%,.2f", cost));
    }

    private void updateStatusDisplay() {
        switch (status) {
            case STATUS_PENDING:
                statusDisplayProperty.set("Pending");
                statusColorProperty.set("#FFC107");
                break;
            case STATUS_IN_PROGRESS:
                statusDisplayProperty.set("In Progress");
                statusColorProperty.set("#2196F3");
                break;
            case STATUS_COMPLETED:
                statusDisplayProperty.set("Completed");
                statusColorProperty.set("#4CAF50");
                break;
            case STATUS_CANCELLED:
                statusDisplayProperty.set("Cancelled");
                statusColorProperty.set("#9E9E9E");
                break;
            default:
                statusDisplayProperty.set(status);
                statusColorProperty.set("#9E9E9E");
        }
    }

    // ============================================
    // GETTERS AND SETTERS WITH PROPERTY UPDATES
    // ============================================

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

    public String getFormattedCost() {
        return formattedCostProperty.get();
    }

    public StringProperty formattedCostProperty() {
        return formattedCostProperty;
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

    // ============================================
    // BUSINESS LOGIC METHODS
    // ============================================

    public String getFormattedServiceDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return serviceDate != null ? serviceDate.format(formatter) : "";
    }

    public String getServiceTypeDisplay() {
        switch (serviceType) {
            case TYPE_OIL_CHANGE: return "Oil Change";
            case TYPE_TUNE_UP: return "Tune Up";
            case TYPE_BRAKE_REPAIR: return "Brake Repair";
            case TYPE_ENGINE_REPAIR: return "Engine Repair";
            case TYPE_TRANSMISSION: return "Transmission";
            case TYPE_TIRE_REPLACEMENT: return "Tire Replacement";
            case TYPE_BATTERY_REPLACEMENT: return "Battery Replacement";
            case TYPE_GENERAL_INSPECTION: return "General Inspection";
            default: return serviceType != null ? serviceType.replace("_", " ") : "Unknown";
        }
    }

    public void calculateStatus() {
        if (serviceDate != null) {
            LocalDate today = LocalDate.now();
            if (serviceDate.equals(today)) {
                status = STATUS_IN_PROGRESS;
            } else if (serviceDate.isAfter(today)) {
                status = STATUS_PENDING;
            } else {
                status = STATUS_COMPLETED;
            }
        } else {
            status = STATUS_PENDING;
        }
        statusProperty.set(status);
    }

    public boolean isCompleted() {
        return STATUS_COMPLETED.equals(status);
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
        return getServiceTypeDisplay() + " - " + getFormattedServiceDate() + " - " + getFormattedCost();
    }

    /**
     * Creates a copy of this service record.
     *
     * @return a new ServiceRecord instance
     */
    public ServiceRecord copy() {
        ServiceRecord copy = new ServiceRecord();
        copy.setId(this.id);
        copy.setVehicleId(this.vehicleId);
        copy.setRegistrationNumber(this.registrationNumber);
        copy.setMake(this.make);
        copy.setModel(this.model);
        copy.setWorkshopId(this.workshopId);
        copy.setWorkshopName(this.workshopName);
        copy.setMechanicId(this.mechanicId);
        copy.setMechanicName(this.mechanicName);
        copy.setServiceDate(this.serviceDate);
        copy.setServiceType(this.serviceType);
        copy.setDescription(this.description);
        copy.setCost(this.cost);
        copy.setOdometerReading(this.odometerReading);
        copy.setStatus(this.status);
        copy.setCreatedAt(this.getCreatedAt());
        copy.setUpdatedAt(this.getUpdatedAt());
        return copy;
    }
}