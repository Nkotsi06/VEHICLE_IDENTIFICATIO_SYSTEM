package models;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * GeofenceZone model representing a geographic boundary zone.
 * Used for vehicle tracking and alerting when vehicles enter/exit zones.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class GeofenceZone extends BaseEntity {

    // Core fields
    private int id;
    private String zoneName;
    private double centerLat;
    private double centerLng;
    private int radiusMeters;
    private String zoneType;
    private int priority;
    private boolean isActive;

    // Zone type constants
    public static final String TYPE_HIGH_CRIME = "HIGH_CRIME";
    public static final String TYPE_RESTRICTED = "RESTRICTED";
    public static final String TYPE_MONITORED = "MONITORED";
    public static final String TYPE_SCHOOL_ZONE = "SCHOOL_ZONE";
    public static final String TYPE_COMMERCIAL = "COMMERCIAL";
    public static final String TYPE_RESIDENTIAL = "RESIDENTIAL";

    // Priority constants
    public static final int PRIORITY_HIGH = 3;
    public static final int PRIORITY_MEDIUM = 2;
    public static final int PRIORITY_LOW = 1;

    // Default radius
    public static final int DEFAULT_RADIUS_METERS = 500;

    // JavaFX Properties for TableView binding
    private final StringProperty zoneNameProperty = new SimpleStringProperty();
    private final StringProperty zoneTypeProperty = new SimpleStringProperty();
    private final IntegerProperty radiusMetersProperty = new SimpleIntegerProperty();
    private final BooleanProperty activeProperty = new SimpleBooleanProperty();
    private final DoubleProperty centerLatProperty = new SimpleDoubleProperty();
    private final DoubleProperty centerLngProperty = new SimpleDoubleProperty();
    private final IntegerProperty priorityProperty = new SimpleIntegerProperty();

    /**
     * Default constructor - initializes with default values.
     */
    public GeofenceZone() {
        super();
        this.isActive = true;
        this.priority = PRIORITY_MEDIUM;
        this.radiusMeters = DEFAULT_RADIUS_METERS;

        activeProperty.set(true);
        priorityProperty.set(PRIORITY_MEDIUM);
        radiusMetersProperty.set(DEFAULT_RADIUS_METERS);
    }

    /**
     * Constructor for creating a new geofence zone.
     *
     * @param zoneName     the zone name
     * @param centerLat    center latitude
     * @param centerLng    center longitude
     * @param radiusMeters radius in meters
     * @param zoneType     the zone type
     */
    public GeofenceZone(String zoneName, double centerLat, double centerLng, int radiusMeters, String zoneType) {
        this();
        this.zoneName = zoneName;
        this.centerLat = centerLat;
        this.centerLng = centerLng;
        this.radiusMeters = radiusMeters;
        this.zoneType = zoneType;

        // Update properties
        zoneNameProperty.set(zoneName);
        centerLatProperty.set(centerLat);
        centerLngProperty.set(centerLng);
        radiusMetersProperty.set(radiusMeters);
        zoneTypeProperty.set(zoneType);
    }

    // ============================================
    // GETTERS AND SETTERS WITH PROPERTY UPDATES
    // ============================================

    public String getZoneName() {
        return zoneName;
    }

    public void setZoneName(String zoneName) {
        this.zoneName = zoneName;
        zoneNameProperty.set(zoneName);
    }

    public StringProperty zoneNameProperty() {
        return zoneNameProperty;
    }

    public double getCenterLat() {
        return centerLat;
    }

    public void setCenterLat(double centerLat) {
        this.centerLat = centerLat;
        centerLatProperty.set(centerLat);
    }

    public DoubleProperty centerLatProperty() {
        return centerLatProperty;
    }

    public double getCenterLng() {
        return centerLng;
    }

    public void setCenterLng(double centerLng) {
        this.centerLng = centerLng;
        centerLngProperty.set(centerLng);
    }

    public DoubleProperty centerLngProperty() {
        return centerLngProperty;
    }

    public int getRadiusMeters() {
        return radiusMeters;
    }

    public void setRadiusMeters(int radiusMeters) {
        this.radiusMeters = radiusMeters;
        radiusMetersProperty.set(radiusMeters);
    }

    public IntegerProperty radiusMetersProperty() {
        return radiusMetersProperty;
    }

    public String getZoneType() {
        return zoneType;
    }

    public void setZoneType(String zoneType) {
        this.zoneType = zoneType;
        zoneTypeProperty.set(zoneType);
    }

    public StringProperty zoneTypeProperty() {
        return zoneTypeProperty;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
        priorityProperty.set(priority);
    }

    public IntegerProperty priorityProperty() {
        return priorityProperty;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
        activeProperty.set(active);
    }

    public BooleanProperty activeProperty() {
        return activeProperty;
    }

    // ============================================
    // BUSINESS LOGIC METHODS
    // ============================================

    /**
     * Gets the CSS color for the zone type.
     *
     * @return hex color code
     */
    public String getZoneTypeColor() {
        switch (zoneType) {
            case TYPE_HIGH_CRIME: return "#F44336";
            case TYPE_RESTRICTED: return "#FF9800";
            case TYPE_MONITORED: return "#2196F3";
            case TYPE_SCHOOL_ZONE: return "#4CAF50";
            case TYPE_COMMERCIAL: return "#9C27B0";
            case TYPE_RESIDENTIAL: return "#00BCD4";
            default: return "#9E9E9E";
        }
    }

    /**
     * Gets the zone type display name.
     *
     * @return human-readable zone type
     */
    public String getZoneTypeDisplay() {
        switch (zoneType) {
            case TYPE_HIGH_CRIME: return "High Crime Area";
            case TYPE_RESTRICTED: return "Restricted Area";
            case TYPE_MONITORED: return "Monitored Area";
            case TYPE_SCHOOL_ZONE: return "School Zone";
            case TYPE_COMMERCIAL: return "Commercial Area";
            case TYPE_RESIDENTIAL: return "Residential Area";
            default: return zoneType != null ? zoneType.replace("_", " ") : "Unknown";
        }
    }

    /**
     * Gets the radius in kilometers.
     *
     * @return radius in kilometers
     */
    public double getRadiusKm() {
        return radiusMeters / 1000.0;
    }

    /**
     * Gets the formatted radius.
     *
     * @return formatted radius string
     */
    public String getFormattedRadius() {
        if (radiusMeters >= 1000) {
            return String.format("%.1f km", getRadiusKm());
        }
        return radiusMeters + " m";
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
            default: return "Medium";
        }
    }

    /**
     * Gets the priority color.
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
     * Activates the zone.
     */
    public void activate() {
        this.isActive = true;
        activeProperty.set(true);
    }

    /**
     * Deactivates the zone.
     */
    public void deactivate() {
        this.isActive = false;
        activeProperty.set(false);
    }

    /**
     * Validates the zone coordinates.
     *
     * @return true if coordinates are valid
     */
    public boolean hasValidCoordinates() {
        return centerLat >= -90 && centerLat <= 90 &&
                centerLng >= -180 && centerLng <= 180;
    }

    /**
     * Validates the zone radius.
     *
     * @return true if radius is valid
     */
    public boolean hasValidRadius() {
        return radiusMeters > 0 && radiusMeters <= 50000; // Max 50km
    }

    /**
     * Checks if the zone is valid (all required fields present).
     *
     * @return true if valid
     */
    public boolean isValid() {
        return zoneName != null && !zoneName.trim().isEmpty() &&
                zoneType != null && !zoneType.trim().isEmpty() &&
                hasValidCoordinates() &&
                hasValidRadius();
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
        return zoneName + " - " + getZoneTypeDisplay() + " (" + getFormattedRadius() + ")";
    }

    /**
     * Creates a copy of this geofence zone.
     *
     * @return a new GeofenceZone instance
     */
    public GeofenceZone copy() {
        GeofenceZone copy = new GeofenceZone();
        copy.setId(this.id);
        copy.setZoneName(this.zoneName);
        copy.setCenterLat(this.centerLat);
        copy.setCenterLng(this.centerLng);
        copy.setRadiusMeters(this.radiusMeters);
        copy.setZoneType(this.zoneType);
        copy.setPriority(this.priority);
        copy.setActive(this.isActive);
        copy.setCreatedAt(this.getCreatedAt());
        copy.setUpdatedAt(this.getUpdatedAt());
        return copy;
    }
}