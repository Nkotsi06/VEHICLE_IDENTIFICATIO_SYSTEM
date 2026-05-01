package models;

import java.time.LocalDate;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class VehicleHistory extends BaseEntity {
    private int id;
    private int vehicleId;
    private String registrationNumber;
    private String eventType;
    private LocalDate eventDate;
    private String description;
    private String details;

    // JavaFX Properties
    private final StringProperty eventTypeProperty = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> eventDateProperty = new SimpleObjectProperty<>();
    private final StringProperty descriptionProperty = new SimpleStringProperty();
    private final StringProperty detailsProperty = new SimpleStringProperty();
    private final StringProperty registrationNumberProperty = new SimpleStringProperty();

    public VehicleHistory() {
        super();
    }

    public VehicleHistory(int vehicleId, String eventType, LocalDate eventDate, String description, String details) {
        this();
        this.vehicleId = vehicleId;
        this.eventType = eventType;
        this.eventDate = eventDate;
        this.description = description;
        this.details = details;

        this.eventTypeProperty.set(eventType);
        this.eventDateProperty.set(eventDate);
        this.descriptionProperty.set(description);
        this.detailsProperty.set(details);
    }

    public int getVehicleId() { return vehicleId; }
    public void setVehicleId(int vehicleId) { this.vehicleId = vehicleId; }

    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
        registrationNumberProperty.set(registrationNumber);
    }
    public StringProperty registrationNumberProperty() { return registrationNumberProperty; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) {
        this.eventType = eventType;
        eventTypeProperty.set(eventType);
    }
    public StringProperty eventTypeProperty() { return eventTypeProperty; }

    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
        eventDateProperty.set(eventDate);
    }
    public ObjectProperty<LocalDate> eventDateProperty() { return eventDateProperty; }

    public String getDescription() { return description; }
    public void setDescription(String description) {
        this.description = description;
        descriptionProperty.set(description);
    }
    public StringProperty descriptionProperty() { return descriptionProperty; }

    public String getDetails() { return details; }
    public void setDetails(String details) {
        this.details = details;
        detailsProperty.set(details);
    }
    public StringProperty detailsProperty() { return detailsProperty; }

    @Override
    public int getId() { return id; }
    @Override
    public void setId(int id) { this.id = id; }

    @Override
    public String toString() {
        return eventType + " - " + eventDate + " - " + description;
    }
}