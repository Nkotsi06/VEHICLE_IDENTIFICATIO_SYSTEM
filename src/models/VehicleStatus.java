package models;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import static models.ServiceRecord.STATUS_PENDING;

/**
 * VehicleStatus model representing possible statuses for vehicles.
 * Used for dropdown menus and status display.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class VehicleStatus extends BaseEntity {

    // Core fields
    private int id;
    private String statusName;
    private String description;
    private String colorCode;

    // Status constants
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_STOLEN = "STOLEN";
    public static final String STATUS_IMPOUNDED = "IMPOUNDED";
    public static final String STATUS_SCRAPPED = "SCRAPPED";
    public static final String STATUS_SUSPENDED = "SUSPENDED";
    public static final String STATUS_PENDING = "PENDING";  // FIXED: removed underscore after String

    // Color codes for status
    public static final String COLOR_ACTIVE = "#4CAF50";
    public static final String COLOR_STOLEN = "#F44336";
    public static final String COLOR_IMPOUNDED = "#FF9800";
    public static final String COLOR_SCRAPPED = "#9E9E9E";
    public static final String COLOR_SUSPENDED = "#FFC107";
    public static final String COLOR_PENDING = "#2196F3";

    // JavaFX Properties
    private final StringProperty statusNameProperty = new SimpleStringProperty();
    private final StringProperty descriptionProperty = new SimpleStringProperty();
    private final StringProperty colorCodeProperty = new SimpleStringProperty();
    private final StringProperty statusDisplayProperty = new SimpleStringProperty();

    /**
     * Default constructor.
     */
    public VehicleStatus() {
        super();
    }

    /**
     * Constructor for creating a new vehicle status.
     *
     * @param statusName  the status name
     * @param description the description
     * @param colorCode   the color code for UI
     */
    public VehicleStatus(String statusName, String description, String colorCode) {
        this();
        this.statusName = statusName;
        this.description = description;
        this.colorCode = colorCode;

        statusNameProperty.set(statusName);
        descriptionProperty.set(description);
        colorCodeProperty.set(colorCode);
        updateStatusDisplay();

        statusNameProperty.addListener((obs, oldVal, newVal) -> updateStatusDisplay());
    }

    // ============================================
    // PRIVATE UPDATE METHODS
    // ============================================

    private void updateStatusDisplay() {
        switch (statusName) {
            case STATUS_ACTIVE:
                statusDisplayProperty.set("Active");
                break;
            case STATUS_STOLEN:
                statusDisplayProperty.set("Stolen");
                break;
            case STATUS_IMPOUNDED:
                statusDisplayProperty.set("Impounded");
                break;
            case STATUS_SCRAPPED:
                statusDisplayProperty.set("Scrapped");
                break;
            case STATUS_SUSPENDED:
                statusDisplayProperty.set("Suspended");
                break;
            case STATUS_PENDING:
                statusDisplayProperty.set("Pending");
                break;
            default:
                statusDisplayProperty.set(statusName != null ? statusName : "Unknown");
        }
    }

    // ============================================
    // GETTERS AND SETTERS WITH PROPERTY UPDATES
    // ============================================

    public String getStatusName() { return statusName; }
    public void setStatusName(String statusName) {
        this.statusName = statusName;
        statusNameProperty.set(statusName);
    }
    public StringProperty statusNameProperty() { return statusNameProperty; }

    public String getDescription() { return description; }
    public void setDescription(String description) {
        this.description = description;
        descriptionProperty.set(description);
    }
    public StringProperty descriptionProperty() { return descriptionProperty; }

    public String getColorCode() { return colorCode; }
    public void setColorCode(String colorCode) {
        this.colorCode = colorCode;
        colorCodeProperty.set(colorCode);
    }
    public StringProperty colorCodeProperty() { return colorCodeProperty; }

    public String getStatusDisplay() { return statusDisplayProperty.get(); }
    public StringProperty statusDisplayProperty() { return statusDisplayProperty; }

    public static String getColorForStatus(String status) {
        switch (status) {
            case STATUS_ACTIVE: return COLOR_ACTIVE;
            case STATUS_STOLEN: return COLOR_STOLEN;
            case STATUS_IMPOUNDED: return COLOR_IMPOUNDED;
            case STATUS_SCRAPPED: return COLOR_SCRAPPED;
            case STATUS_SUSPENDED: return COLOR_SUSPENDED;
            case STATUS_PENDING: return COLOR_PENDING;
            default: return "#9E9E9E";
        }
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
        return getStatusDisplay();
    }

    /**
     * Creates a copy of this vehicle status.
     *
     * @return a new VehicleStatus instance
     */
    public VehicleStatus copy() {
        VehicleStatus copy = new VehicleStatus();
        copy.setId(this.id);
        copy.setStatusName(this.statusName);
        copy.setDescription(this.description);
        copy.setColorCode(this.colorCode);
        copy.setCreatedAt(this.getCreatedAt());
        copy.setUpdatedAt(this.getUpdatedAt());
        return copy;
    }
}