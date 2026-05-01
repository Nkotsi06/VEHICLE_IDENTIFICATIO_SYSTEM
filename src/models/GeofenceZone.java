package models;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class GeofenceZone extends BaseEntity {
    private int id;
    private String zoneName;
    private double centerLat;
    private double centerLng;
    private int radiusMeters;
    private String zoneType;
    private int priority;
    private boolean isActive;

    // JavaFX Properties for TableView binding
    private final StringProperty zoneNameProperty = new SimpleStringProperty();
    private final StringProperty zoneTypeProperty = new SimpleStringProperty();
    private final IntegerProperty radiusMetersProperty = new SimpleIntegerProperty();
    private final BooleanProperty activeProperty = new SimpleBooleanProperty();

    public GeofenceZone() {
        super();
        this.isActive = true;
        this.priority = 1;
    }

    public GeofenceZone(String zoneName, double centerLat, double centerLng, int radiusMeters, String zoneType) {
        this();
        this.zoneName = zoneName;
        this.centerLat = centerLat;
        this.centerLng = centerLng;
        this.radiusMeters = radiusMeters;
        this.zoneType = zoneType;

        // Update properties
        zoneNameProperty.set(zoneName);
        zoneTypeProperty.set(zoneType);
        radiusMetersProperty.set(radiusMeters);
        activeProperty.set(true);
    }

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
    }

    public double getCenterLng() {
        return centerLng;
    }

    public void setCenterLng(double centerLng) {
        this.centerLng = centerLng;
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

    public String getZoneTypeColor() {
        switch (zoneType) {
            case "HIGH_CRIME": return "#F44336";
            case "RESTRICTED": return "#FF9800";
            case "MONITORED": return "#2196F3";
            case "SCHOOL_ZONE": return "#4CAF50";
            default: return "#9E9E9E";
        }
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
        return zoneName + " - " + zoneType + " (" + radiusMeters + "m)";
    }
}