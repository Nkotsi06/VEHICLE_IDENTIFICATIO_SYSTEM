package models;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * BOLOAlert model representing "Be On the LookOut" alerts for stolen vehicles.
 * Used by police to broadcast vehicle alerts to all officers.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class BOLOAlert extends BaseEntity {

    // Core fields
    private int id;
    private int vehicleId;
    private String registrationNumber;
    private String make;
    private String model;
    private int stolenVehicleId;
    private String stolenCaseNumber;
    private LocalDate alertDate;
    private LocalDate expiryDate;
    private String message;
    private String priority;
    private String status;
    private boolean distributedToAll;

    // Priority constants
    public static final String PRIORITY_HIGH = "HIGH";
    public static final String PRIORITY_MEDIUM = "MEDIUM";
    public static final String PRIORITY_LOW = "LOW";

    // Status constants
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_EXPIRED = "EXPIRED";
    public static final String STATUS_CANCELLED = "CANCELLED";
    public static final String STATUS_RESOLVED = "RESOLVED";

    // Default values
    private static final int DEFAULT_ALERT_DAYS = 30;

    // JavaFX Properties for TableView binding
    private final IntegerProperty vehicleIdProperty = new SimpleIntegerProperty();
    private final StringProperty registrationNumberProperty = new SimpleStringProperty();
    private final StringProperty makeProperty = new SimpleStringProperty();
    private final StringProperty modelProperty = new SimpleStringProperty();
    private final StringProperty messageProperty = new SimpleStringProperty();
    private final StringProperty priorityProperty = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> expiryDateProperty = new SimpleObjectProperty<>();
    private final StringProperty statusProperty = new SimpleStringProperty();
    private final BooleanProperty distributedProperty = new SimpleBooleanProperty();

    /**
     * Default constructor - initializes with default values.
     */
    public BOLOAlert() {
        super();
        this.status = STATUS_ACTIVE;
        this.priority = PRIORITY_MEDIUM;
        this.alertDate = LocalDate.now();
        this.expiryDate = LocalDate.now().plusDays(DEFAULT_ALERT_DAYS);
        this.distributedToAll = false;

        // Initialize properties
        statusProperty.set(STATUS_ACTIVE);
        priorityProperty.set(PRIORITY_MEDIUM);
        expiryDateProperty.set(this.expiryDate);
        distributedProperty.set(false);
    }

    /**
     * Constructor for creating a new BOLO alert.
     *
     * @param vehicleId   the ID of the stolen vehicle
     * @param message     the alert message
     * @param priority    the priority level (HIGH/MEDIUM/LOW)
     * @param expiryDate  the expiry date
     */
    public BOLOAlert(int vehicleId, String message, String priority, LocalDate expiryDate) {
        this();
        this.vehicleId = vehicleId;
        this.message = message;
        this.priority = priority;
        this.expiryDate = expiryDate;
        this.alertDate = LocalDate.now();

        vehicleIdProperty.set(vehicleId);
        messageProperty.set(message);
        priorityProperty.set(priority);
        expiryDateProperty.set(expiryDate);
    }

    /**
     * Constructor with all fields.
     *
     * @param vehicleId        the vehicle ID
     * @param registrationNumber the registration number
     * @param make             the vehicle make
     * @param model            the vehicle model
     * @param message          the alert message
     * @param priority         the priority level
     * @param expiryDate       the expiry date
     */
    public BOLOAlert(int vehicleId, String registrationNumber, String make, String model,
                     String message, String priority, LocalDate expiryDate) {
        this(vehicleId, message, priority, expiryDate);
        this.registrationNumber = registrationNumber;
        this.make = make;
        this.model = model;

        registrationNumberProperty.set(registrationNumber);
        makeProperty.set(make);
        modelProperty.set(model);
    }

    // ============================================
    // GETTERS AND SETTERS WITH PROPERTY UPDATES
    // ============================================

    public int getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
        vehicleIdProperty.set(vehicleId);
    }

    public IntegerProperty vehicleIdProperty() {
        return vehicleIdProperty;
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

    public int getStolenVehicleId() {
        return stolenVehicleId;
    }

    public void setStolenVehicleId(int stolenVehicleId) {
        this.stolenVehicleId = stolenVehicleId;
    }

    public String getStolenCaseNumber() {
        return stolenCaseNumber;
    }

    public void setStolenCaseNumber(String stolenCaseNumber) {
        this.stolenCaseNumber = stolenCaseNumber;
    }

    public LocalDate getAlertDate() {
        return alertDate;
    }

    public void setAlertDate(LocalDate alertDate) {
        this.alertDate = alertDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
        expiryDateProperty.set(expiryDate);
    }

    public ObjectProperty<LocalDate> expiryDateProperty() {
        return expiryDateProperty;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
        messageProperty.set(message);
    }

    public StringProperty messageProperty() {
        return messageProperty;
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

    public boolean isDistributedToAll() {
        return distributedToAll;
    }

    public void setDistributedToAll(boolean distributedToAll) {
        this.distributedToAll = distributedToAll;
        distributedProperty.set(distributedToAll);
    }

    public BooleanProperty distributedProperty() {
        return distributedProperty;
    }

    // ============================================
    // BUSINESS LOGIC METHODS
    // ============================================

    /**
     * Checks if the alert is currently active.
     *
     * @return true if status is ACTIVE and expiry date is in the future
     */
    public boolean isActive() {
        return STATUS_ACTIVE.equals(status) &&
                expiryDate != null &&
                !expiryDate.isBefore(LocalDate.now());
    }

    /**
     * Checks if the alert has expired.
     *
     * @return true if expiry date has passed
     */
    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }

    /**
     * Gets the number of days remaining until expiry.
     *
     * @return days remaining, or -1 if no expiry date
     */
    public int getDaysRemaining() {
        if (expiryDate == null) return -1;
        return (int) java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
    }

    /**
     * Gets the CSS color for the priority.
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
     * Gets the CSS color for the status.
     *
     * @return hex color code
     */
    public String getStatusColor() {
        switch (status) {
            case STATUS_ACTIVE: return "#4CAF50";
            case STATUS_EXPIRED: return "#9E9E9E";
            case STATUS_CANCELLED: return "#F44336";
            case STATUS_RESOLVED: return "#2196F3";
            default: return "#9E9E9E";
        }
    }

    /**
     * Gets the formatted expiry date for display.
     *
     * @return formatted date string
     */
    public String getFormattedExpiryDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return expiryDate != null ? expiryDate.format(formatter) : "";
    }

    /**
     * Gets the formatted alert date for display.
     *
     * @return formatted date string
     */
    public String getFormattedAlertDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return alertDate != null ? alertDate.format(formatter) : "";
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
     * Gets the status display name.
     *
     * @return human-readable status
     */
    public String getStatusDisplay() {
        switch (status) {
            case STATUS_ACTIVE: return "Active";
            case STATUS_EXPIRED: return "Expired";
            case STATUS_CANCELLED: return "Cancelled";
            case STATUS_RESOLVED: return "Resolved";
            default: return status;
        }
    }

    /**
     * Extends the alert by a number of days.
     *
     * @param days number of days to extend
     */
    public void extendAlert(int days) {
        if (expiryDate != null && days > 0) {
            this.expiryDate = expiryDate.plusDays(days);
            expiryDateProperty.set(this.expiryDate);
        }
    }

    /**
     * Cancels the alert.
     */
    public void cancel() {
        this.status = STATUS_CANCELLED;
        statusProperty.set(STATUS_CANCELLED);
    }

    /**
     * Marks the alert as resolved.
     */
    public void resolve() {
        this.status = STATUS_RESOLVED;
        statusProperty.set(STATUS_RESOLVED);
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
        return "BOLO: " + registrationNumber + " - " + getPriorityDisplay() + " Priority";
    }

    /**
     * Creates a copy of this BOLO alert.
     *
     * @return a new BOLOAlert instance with the same values
     */
    public BOLOAlert copy() {
        BOLOAlert copy = new BOLOAlert();
        copy.setId(this.id);
        copy.setVehicleId(this.vehicleId);
        copy.setRegistrationNumber(this.registrationNumber);
        copy.setMake(this.make);
        copy.setModel(this.model);
        copy.setStolenVehicleId(this.stolenVehicleId);
        copy.setStolenCaseNumber(this.stolenCaseNumber);
        copy.setAlertDate(this.alertDate);
        copy.setExpiryDate(this.expiryDate);
        copy.setMessage(this.message);
        copy.setPriority(this.priority);
        copy.setStatus(this.status);
        copy.setDistributedToAll(this.distributedToAll);
        copy.setCreatedAt(this.getCreatedAt());
        copy.setUpdatedAt(this.getUpdatedAt());
        return copy;
    }
}