package models;

import java.time.LocalDate;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class BOLOAlert extends BaseEntity {
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

    // JavaFX Properties
    private final StringProperty registrationNumberProperty = new SimpleStringProperty();
    private final StringProperty messageProperty = new SimpleStringProperty();
    private final StringProperty priorityProperty = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> expiryDateProperty = new SimpleObjectProperty<>();
    private final StringProperty statusProperty = new SimpleStringProperty();

    public BOLOAlert() {
        super();
        this.status = "ACTIVE";
        this.priority = "MEDIUM";
    }

    public BOLOAlert(int vehicleId, String message, String priority, LocalDate expiryDate) {
        this();
        this.vehicleId = vehicleId;
        this.message = message;
        this.priority = priority;
        this.expiryDate = expiryDate;
        this.alertDate = LocalDate.now();

        this.messageProperty.set(message);
        this.priorityProperty.set(priority);
        this.expiryDateProperty.set(expiryDate);
        this.statusProperty.set("ACTIVE");
    }

    public int getVehicleId() { return vehicleId; }
    public void setVehicleId(int vehicleId) { this.vehicleId = vehicleId; }

    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
        registrationNumberProperty.set(registrationNumber);
    }
    public StringProperty registrationNumberProperty() { return registrationNumberProperty; }

    public String getMake() { return make; }
    public void setMake(String make) { this.make = make; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public int getStolenVehicleId() { return stolenVehicleId; }
    public void setStolenVehicleId(int stolenVehicleId) { this.stolenVehicleId = stolenVehicleId; }

    public String getStolenCaseNumber() { return stolenCaseNumber; }
    public void setStolenCaseNumber(String stolenCaseNumber) { this.stolenCaseNumber = stolenCaseNumber; }

    public LocalDate getAlertDate() { return alertDate; }
    public void setAlertDate(LocalDate alertDate) { this.alertDate = alertDate; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
        expiryDateProperty.set(expiryDate);
    }
    public ObjectProperty<LocalDate> expiryDateProperty() { return expiryDateProperty; }

    public String getMessage() { return message; }
    public void setMessage(String message) {
        this.message = message;
        messageProperty.set(message);
    }
    public StringProperty messageProperty() { return messageProperty; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) {
        this.priority = priority;
        priorityProperty.set(priority);
    }
    public StringProperty priorityProperty() { return priorityProperty; }

    public String getStatus() { return status; }
    public void setStatus(String status) {
        this.status = status;
        statusProperty.set(status);
    }
    public StringProperty statusProperty() { return statusProperty; }

    public boolean isDistributedToAll() { return distributedToAll; }
    public void setDistributedToAll(boolean distributedToAll) { this.distributedToAll = distributedToAll; }

    public boolean isActive() {
        return "ACTIVE".equals(status) && expiryDate != null && expiryDate.isAfter(LocalDate.now());
    }

    public String getPriorityColor() {
        switch (priority) {
            case "HIGH": return "#F44336";
            case "MEDIUM": return "#FF9800";
            case "LOW": return "#4CAF50";
            default: return "#9E9E9E";
        }
    }

    @Override
    public int getId() { return id; }
    @Override
    public void setId(int id) { this.id = id; }

    @Override
    public String toString() {
        return "BOLO: " + registrationNumber + " - " + priority + " Priority";
    }
}