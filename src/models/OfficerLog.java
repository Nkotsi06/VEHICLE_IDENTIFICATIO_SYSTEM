package models;

import java.time.LocalDateTime;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class OfficerLog extends BaseEntity {
    private int id;
    private String officerName;
    private String badgeNumber;
    private String action;
    private int vehicleId;
    private String registrationNumber;
    private LocalDateTime timestamp;

    // JavaFX Properties for TableView binding
    private final StringProperty officerNameProperty = new SimpleStringProperty();
    private final StringProperty badgeNumberProperty = new SimpleStringProperty();
    private final StringProperty actionProperty = new SimpleStringProperty();
    private final IntegerProperty vehicleIdProperty = new SimpleIntegerProperty();
    private final StringProperty registrationNumberProperty = new SimpleStringProperty();
    private final ObjectProperty<LocalDateTime> timestampProperty = new SimpleObjectProperty<>();

    public OfficerLog() {
        super();
    }

    public OfficerLog(String officerName, String badgeNumber, String action, int vehicleId) {
        this();
        this.officerName = officerName;
        this.badgeNumber = badgeNumber;
        this.action = action;
        this.vehicleId = vehicleId;
        this.timestamp = LocalDateTime.now();

        // Update properties
        officerNameProperty.set(officerName);
        badgeNumberProperty.set(badgeNumber);
        actionProperty.set(action);
        vehicleIdProperty.set(vehicleId);
        timestampProperty.set(timestamp);
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

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
        actionProperty.set(action);
    }

    public StringProperty actionProperty() {
        return actionProperty;
    }

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
        return officerName + " - " + action + " - " + timestamp;
    }
}