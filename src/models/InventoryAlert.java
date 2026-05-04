package models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * InventoryAlert model representing alerts for low stock or reorder needs.
 * Used by workshops to monitor parts inventory.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class InventoryAlert extends BaseEntity {

    // Core fields
    private int id;
    private int partInventoryId;
    private String partName;
    private String alertType;
    private String message;
    private boolean isResolved;
    private LocalDateTime resolvedAt;

    // Alert type constants
    public static final String ALERT_LOW_STOCK = "LOW_STOCK";
    public static final String ALERT_OUT_OF_STOCK = "OUT_OF_STOCK";
    public static final String ALERT_REORDER = "REORDER";
    public static final String ALERT_EXPIRING = "EXPIRING";

    // JavaFX Properties
    private final IntegerProperty partInventoryIdProperty = new SimpleIntegerProperty();
    private final StringProperty partNameProperty = new SimpleStringProperty();
    private final StringProperty alertTypeProperty = new SimpleStringProperty();
    private final StringProperty messageProperty = new SimpleStringProperty();
    private final BooleanProperty resolvedProperty = new SimpleBooleanProperty();
    private final ObjectProperty<LocalDateTime> resolvedAtProperty = new SimpleObjectProperty<>();
    private final StringProperty alertTypeDisplayProperty = new SimpleStringProperty();
    private final StringProperty alertColorProperty = new SimpleStringProperty();

    /**
     * Default constructor - initializes with unresolved status.
     */
    public InventoryAlert() {
        super();
        this.isResolved = false;

        resolvedProperty.set(false);
    }

    /**
     * Constructor for creating a new inventory alert.
     *
     * @param partInventoryId the part inventory ID
     * @param alertType       the alert type
     * @param message         the alert message
     */
    public InventoryAlert(int partInventoryId, String alertType, String message) {
        this();
        this.partInventoryId = partInventoryId;
        this.alertType = alertType;
        this.message = message;

        partInventoryIdProperty.set(partInventoryId);
        alertTypeProperty.set(alertType);
        messageProperty.set(message);
        updateAlertDisplay();
    }

    // ============================================
    // PRIVATE UPDATE METHODS
    // ============================================

    private void updateAlertDisplay() {
        switch (alertType) {
            case ALERT_LOW_STOCK:
                alertTypeDisplayProperty.set("Low Stock");
                alertColorProperty.set("#FF9800");
                break;
            case ALERT_OUT_OF_STOCK:
                alertTypeDisplayProperty.set("Out of Stock");
                alertColorProperty.set("#F44336");
                break;
            case ALERT_REORDER:
                alertTypeDisplayProperty.set("Reorder Needed");
                alertColorProperty.set("#2196F3");
                break;
            case ALERT_EXPIRING:
                alertTypeDisplayProperty.set("Expiring Soon");
                alertColorProperty.set("#9C27B0");
                break;
            default:
                alertTypeDisplayProperty.set(alertType);
                alertColorProperty.set("#9E9E9E");
        }
    }

    // ============================================
    // GETTERS AND SETTERS WITH PROPERTY UPDATES
    // ============================================

    public int getPartInventoryId() {
        return partInventoryId;
    }

    public void setPartInventoryId(int partInventoryId) {
        this.partInventoryId = partInventoryId;
        partInventoryIdProperty.set(partInventoryId);
    }

    public IntegerProperty partInventoryIdProperty() {
        return partInventoryIdProperty;
    }

    public String getPartName() {
        return partName;
    }

    public void setPartName(String partName) {
        this.partName = partName;
        partNameProperty.set(partName);
    }

    public StringProperty partNameProperty() {
        return partNameProperty;
    }

    public String getAlertType() {
        return alertType;
    }

    public void setAlertType(String alertType) {
        this.alertType = alertType;
        alertTypeProperty.set(alertType);
        updateAlertDisplay();
    }

    public StringProperty alertTypeProperty() {
        return alertTypeProperty;
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

    public boolean isResolved() {
        return isResolved;
    }

    public void setResolved(boolean resolved) {
        isResolved = resolved;
        resolvedProperty.set(resolved);
        if (resolved) {
            this.resolvedAt = LocalDateTime.now();
            resolvedAtProperty.set(this.resolvedAt);
        }
    }

    public BooleanProperty resolvedProperty() {
        return resolvedProperty;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
        resolvedAtProperty.set(resolvedAt);
    }

    public ObjectProperty<LocalDateTime> resolvedAtProperty() {
        return resolvedAtProperty;
    }

    public StringProperty alertTypeDisplayProperty() {
        return alertTypeDisplayProperty;
    }

    public StringProperty alertColorProperty() {
        return alertColorProperty;
    }

    // ============================================
    // BUSINESS LOGIC METHODS
    // ============================================

    public String getAlertTypeDisplay() {
        return alertTypeDisplayProperty.get();
    }

    public String getAlertColor() {
        return alertColorProperty.get();
    }

    public String getFormattedResolvedAt() {
        if (resolvedAt == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return resolvedAt.format(formatter);
    }

    public boolean isLowStock() {
        return ALERT_LOW_STOCK.equals(alertType);
    }

    public boolean isOutOfStock() {
        return ALERT_OUT_OF_STOCK.equals(alertType);
    }

    public boolean isReorderNeeded() {
        return ALERT_REORDER.equals(alertType);
    }

    /**
     * Resolves the alert.
     */
    public void resolve() {
        setResolved(true);
    }

    /**
     * Reopens a resolved alert.
     */
    public void reopen() {
        this.isResolved = false;
        this.resolvedAt = null;
        resolvedProperty.set(false);
        resolvedAtProperty.set(null);
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
        return getAlertTypeDisplay() + " - " + message;
    }

    /**
     * Creates a copy of this alert.
     *
     * @return a new InventoryAlert instance
     */
    public InventoryAlert copy() {
        InventoryAlert copy = new InventoryAlert();
        copy.setId(this.id);
        copy.setPartInventoryId(this.partInventoryId);
        copy.setPartName(this.partName);
        copy.setAlertType(this.alertType);
        copy.setMessage(this.message);
        copy.setResolved(this.isResolved);
        copy.setResolvedAt(this.resolvedAt);
        copy.setCreatedAt(this.getCreatedAt());
        copy.setUpdatedAt(this.getUpdatedAt());
        return copy;
    }
}