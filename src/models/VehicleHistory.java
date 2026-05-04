package models;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * VehicleHistory model representing historical events for vehicles.
 * Tracks changes, ownership transfers, incidents, and other events.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class VehicleHistory extends BaseEntity {

    // Core fields
    private int id;
    private int vehicleId;
    private String registrationNumber;
    private String eventType;
    private LocalDate eventDate;
    private String description;
    private String details;

    // Event type constants
    public static final String EVENT_REGISTRATION = "REGISTRATION";
    public static final String EVENT_OWNERSHIP_TRANSFER = "OWNERSHIP_TRANSFER";
    public static final String EVENT_ACCIDENT = "ACCIDENT";
    public static final String EVENT_THEFT = "THEFT";
    public static final String EVENT_RECOVERY = "RECOVERY";
    public static final String EVENT_IMPOUND = "IMPOUND";
    public static final String EVENT_RELEASE = "RELEASE";
    public static final String EVENT_INSPECTION = "INSPECTION";
    public static final String EVENT_REPAIR = "REPAIR";
    public static final String EVENT_EXPIRY = "EXPIRY";
    public static final String EVENT_RENEWAL = "RENEWAL";

    // JavaFX Properties
    private final StringProperty eventTypeProperty = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> eventDateProperty = new SimpleObjectProperty<>();
    private final StringProperty descriptionProperty = new SimpleStringProperty();
    private final StringProperty detailsProperty = new SimpleStringProperty();
    private final StringProperty registrationNumberProperty = new SimpleStringProperty();
    private final StringProperty eventTypeDisplayProperty = new SimpleStringProperty();

    /**
     * Default constructor.
     */
    public VehicleHistory() {
        super();
        updateEventTypeDisplay();

        eventTypeProperty.addListener((obs, oldVal, newVal) -> updateEventTypeDisplay());
    }

    /**
     * Constructor for creating a new history entry.
     *
     * @param vehicleId       the vehicle ID
     * @param eventType       the event type
     * @param eventDate       the event date
     * @param description     the description
     * @param details         additional details
     */
    public VehicleHistory(int vehicleId, String eventType, LocalDate eventDate,
                          String description, String details) {
        this();
        this.vehicleId = vehicleId;
        this.eventType = eventType;
        this.eventDate = eventDate;
        this.description = description;
        this.details = details;

        eventTypeProperty.set(eventType);
        eventDateProperty.set(eventDate);
        descriptionProperty.set(description);
        detailsProperty.set(details);
    }

    // ============================================
    // PRIVATE UPDATE METHODS
    // ============================================

    private void updateEventTypeDisplay() {
        switch (eventType) {
            case EVENT_REGISTRATION:
                eventTypeDisplayProperty.set("Registration");
                break;
            case EVENT_OWNERSHIP_TRANSFER:
                eventTypeDisplayProperty.set("Ownership Transfer");
                break;
            case EVENT_ACCIDENT:
                eventTypeDisplayProperty.set("Accident");
                break;
            case EVENT_THEFT:
                eventTypeDisplayProperty.set("Theft");
                break;
            case EVENT_RECOVERY:
                eventTypeDisplayProperty.set("Recovery");
                break;
            case EVENT_IMPOUND:
                eventTypeDisplayProperty.set("Impounded");
                break;
            case EVENT_RELEASE:
                eventTypeDisplayProperty.set("Released");
                break;
            case EVENT_INSPECTION:
                eventTypeDisplayProperty.set("Inspection");
                break;
            case EVENT_REPAIR:
                eventTypeDisplayProperty.set("Repair");
                break;
            case EVENT_EXPIRY:
                eventTypeDisplayProperty.set("Document Expired");
                break;
            case EVENT_RENEWAL:
                eventTypeDisplayProperty.set("Document Renewed");
                break;
            default:
                eventTypeDisplayProperty.set(eventType != null ? eventType.replace("_", " ") : "Unknown");
        }
    }

    // ============================================
    // GETTERS AND SETTERS WITH PROPERTY UPDATES
    // ============================================

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

    public String getEventTypeDisplay() { return eventTypeDisplayProperty.get(); }
    public StringProperty eventTypeDisplayProperty() { return eventTypeDisplayProperty; }

    // ============================================
    // BUSINESS LOGIC METHODS
    // ============================================

    public String getFormattedEventDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return eventDate != null ? eventDate.format(formatter) : "";
    }

    public String getDescriptionPreview() {
        if (description == null) return "";
        if (description.length() <= 100) return description;
        return description.substring(0, 100) + "...";
    }

    public String getDetailsPreview() {
        if (details == null) return "";
        if (details.length() <= 100) return details;
        return details.substring(0, 100) + "...";
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
        return getEventTypeDisplay() + " - " + getFormattedEventDate() + " - " + getDescriptionPreview();
    }

    /**
     * Creates a copy of this vehicle history entry.
     *
     * @return a new VehicleHistory instance
     */
    public VehicleHistory copy() {
        VehicleHistory copy = new VehicleHistory();
        copy.setId(this.id);
        copy.setVehicleId(this.vehicleId);
        copy.setRegistrationNumber(this.registrationNumber);
        copy.setEventType(this.eventType);
        copy.setEventDate(this.eventDate);
        copy.setDescription(this.description);
        copy.setDetails(this.details);
        copy.setCreatedAt(this.getCreatedAt());
        copy.setUpdatedAt(this.getUpdatedAt());
        return copy;
    }
}