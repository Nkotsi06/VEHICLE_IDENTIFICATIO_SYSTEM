package models;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Mechanic model representing workshop mechanics.
 * Contains personal information and specialization details.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class Mechanic extends BaseEntity {

    // Core fields
    private int id;
    private int workshopId;
    private String workshopName;
    private String name;
    private String specialization;
    private String phone;
    private String email;
    private String status;

    // Status constants
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_INACTIVE = "INACTIVE";
    public static final String STATUS_ON_LEAVE = "ON_LEAVE";
    public static final String STATUS_TERMINATED = "TERMINATED";

    // JavaFX Properties for TableView binding
    private final IntegerProperty workshopIdProperty = new SimpleIntegerProperty();
    private final StringProperty workshopNameProperty = new SimpleStringProperty();
    private final StringProperty nameProperty = new SimpleStringProperty();
    private final StringProperty specializationProperty = new SimpleStringProperty();
    private final StringProperty phoneProperty = new SimpleStringProperty();
    private final StringProperty emailProperty = new SimpleStringProperty();
    private final StringProperty statusProperty = new SimpleStringProperty();
    private final StringProperty statusDisplayProperty = new SimpleStringProperty();
    private final StringProperty statusColorProperty = new SimpleStringProperty();

    /**
     * Default constructor.
     */
    public Mechanic() {
        super();
        this.status = STATUS_ACTIVE;

        statusProperty.set(STATUS_ACTIVE);
        updateStatusDisplay();

        statusProperty.addListener((obs, oldVal, newVal) -> updateStatusDisplay());
    }

    /**
     * Constructor for creating a new mechanic.
     *
     * @param workshopId     the workshop ID
     * @param name           the mechanic's name
     * @param specialization the mechanic's specialization
     * @param phone          the mechanic's phone number
     */
    public Mechanic(int workshopId, String name, String specialization, String phone) {
        this();
        this.workshopId = workshopId;
        this.name = name;
        this.specialization = specialization;
        this.phone = phone;

        workshopIdProperty.set(workshopId);
        nameProperty.set(name);
        specializationProperty.set(specialization);
        phoneProperty.set(phone);
    }

    // ============================================
    // PRIVATE UPDATE METHODS
    // ============================================

    private void updateStatusDisplay() {
        switch (status) {
            case STATUS_ACTIVE:
                statusDisplayProperty.set("Active");
                statusColorProperty.set("#4CAF50");
                break;
            case STATUS_INACTIVE:
                statusDisplayProperty.set("Inactive");
                statusColorProperty.set("#9E9E9E");
                break;
            case STATUS_ON_LEAVE:
                statusDisplayProperty.set("On Leave");
                statusColorProperty.set("#FF9800");
                break;
            case STATUS_TERMINATED:
                statusDisplayProperty.set("Terminated");
                statusColorProperty.set("#F44336");
                break;
            default:
                statusDisplayProperty.set(status);
                statusColorProperty.set("#9E9E9E");
        }
    }

    // ============================================
    // GETTERS AND SETTERS WITH PROPERTY UPDATES
    // ============================================

    public int getWorkshopId() {
        return workshopId;
    }

    public void setWorkshopId(int workshopId) {
        this.workshopId = workshopId;
        workshopIdProperty.set(workshopId);
    }

    public IntegerProperty workshopIdProperty() {
        return workshopIdProperty;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        nameProperty.set(name);
    }

    public StringProperty nameProperty() {
        return nameProperty;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
        specializationProperty.set(specialization);
    }

    public StringProperty specializationProperty() {
        return specializationProperty;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
        phoneProperty.set(phone);
    }

    public StringProperty phoneProperty() {
        return phoneProperty;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
        emailProperty.set(email);
    }

    public StringProperty emailProperty() {
        return emailProperty;
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

    public boolean isActive() {
        return STATUS_ACTIVE.equals(status);
    }

    public void activate() {
        setStatus(STATUS_ACTIVE);
    }

    public void deactivate() {
        setStatus(STATUS_INACTIVE);
    }

    public void setOnLeave() {
        setStatus(STATUS_ON_LEAVE);
    }

    public void terminate() {
        setStatus(STATUS_TERMINATED);
    }

    public String getDisplayName() {
        return name + " (" + specialization + ")";
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
        return name + " - " + specialization;
    }

    /**
     * Creates a copy of this mechanic.
     *
     * @return a new Mechanic instance
     */
    public Mechanic copy() {
        Mechanic copy = new Mechanic();
        copy.setId(this.id);
        copy.setWorkshopId(this.workshopId);
        copy.setWorkshopName(this.workshopName);
        copy.setName(this.name);
        copy.setSpecialization(this.specialization);
        copy.setPhone(this.phone);
        copy.setEmail(this.email);
        copy.setStatus(this.status);
        copy.setCreatedAt(this.getCreatedAt());
        copy.setUpdatedAt(this.getUpdatedAt());
        return copy;
    }
}