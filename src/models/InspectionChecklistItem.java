package models;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * InspectionChecklistItem model representing an item on a digital inspection checklist.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class InspectionChecklistItem extends BaseEntity {

    // Core fields
    private int id;
    private int inspectionId;
    private String itemName;
    private String status;
    private String notes;
    private String photoPath;

    // Status constants
    public static final String STATUS_PASS = "PASS";
    public static final String STATUS_FAIL = "FAIL";
    public static final String STATUS_NOT_CHECKED = "NOT_CHECKED";
    public static final String STATUS_NA = "N/A";

    // JavaFX Properties
    private final StringProperty itemNameProperty = new SimpleStringProperty();
    private final StringProperty statusProperty = new SimpleStringProperty();
    private final StringProperty notesProperty = new SimpleStringProperty();

    /**
     * Default constructor - initializes with NOT_CHECKED status.
     */
    public InspectionChecklistItem() {
        super();
        this.status = STATUS_NOT_CHECKED;

        statusProperty.set(STATUS_NOT_CHECKED);
    }

    /**
     * Constructor for creating a checklist item.
     *
     * @param inspectionId the inspection ID
     * @param itemName     the item name
     * @param status       the status (PASS/FAIL/NOT_CHECKED)
     * @param notes        additional notes
     */
    public InspectionChecklistItem(int inspectionId, String itemName, String status, String notes) {
        this();
        this.inspectionId = inspectionId;
        this.itemName = itemName;
        this.status = status;
        this.notes = notes;

        this.itemNameProperty.set(itemName);
        this.statusProperty.set(status);
        this.notesProperty.set(notes);
    }

    // ============================================
    // GETTERS AND SETTERS WITH PROPERTY UPDATES
    // ============================================

    public int getInspectionId() {
        return inspectionId;
    }

    public void setInspectionId(int inspectionId) {
        this.inspectionId = inspectionId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
        itemNameProperty.set(itemName);
    }

    public StringProperty itemNameProperty() {
        return itemNameProperty;
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
        notesProperty.set(notes);
    }

    public StringProperty notesProperty() {
        return notesProperty;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    // ============================================
    // BUSINESS LOGIC METHODS
    // ============================================

    /**
     * Checks if the item passed inspection.
     *
     * @return true if status is PASS
     */
    public boolean isPassed() {
        return STATUS_PASS.equals(status);
    }

    /**
     * Checks if the item failed inspection.
     *
     * @return true if status is FAIL
     */
    public boolean isFailed() {
        return STATUS_FAIL.equals(status);
    }

    /**
     * Checks if the item has been checked.
     *
     * @return true if status is PASS or FAIL
     */
    public boolean isChecked() {
        return STATUS_PASS.equals(status) || STATUS_FAIL.equals(status);
    }

    /**
     * Gets the status display name.
     *
     * @return human-readable status
     */
    public String getStatusDisplay() {
        switch (status) {
            case STATUS_PASS: return "Pass";
            case STATUS_FAIL: return "Fail";
            case STATUS_NOT_CHECKED: return "Not Checked";
            case STATUS_NA: return "N/A";
            default: return status;
        }
    }

    /**
     * Gets the CSS color for the status.
     *
     * @return hex color code
     */
    public String getStatusColor() {
        switch (status) {
            case STATUS_PASS: return "#4CAF50";
            case STATUS_FAIL: return "#F44336";
            case STATUS_NOT_CHECKED: return "#FFC107";
            default: return "#9E9E9E";
        }
    }

    /**
     * Marks the item as passed.
     *
     * @param notes optional notes
     */
    public void markPass(String notes) {
        this.status = STATUS_PASS;
        if (notes != null) {
            this.notes = notes;
            notesProperty.set(notes);
        }
        statusProperty.set(STATUS_PASS);
    }

    /**
     * Marks the item as failed.
     *
     * @param notes the failure notes
     */
    public void markFail(String notes) {
        this.status = STATUS_FAIL;
        this.notes = notes;
        statusProperty.set(STATUS_FAIL);
        notesProperty.set(notes);
    }

    /**
     * Resets the item to not checked.
     */
    public void reset() {
        this.status = STATUS_NOT_CHECKED;
        this.notes = null;
        statusProperty.set(STATUS_NOT_CHECKED);
        notesProperty.set(null);
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
        return itemName + " - " + getStatusDisplay();
    }

    /**
     * Creates a copy of this checklist item.
     *
     * @return a new InspectionChecklistItem instance
     */
    public InspectionChecklistItem copy() {
        InspectionChecklistItem copy = new InspectionChecklistItem();
        copy.setId(this.id);
        copy.setInspectionId(this.inspectionId);
        copy.setItemName(this.itemName);
        copy.setStatus(this.status);
        copy.setNotes(this.notes);
        copy.setPhotoPath(this.photoPath);
        copy.setCreatedAt(this.getCreatedAt());
        copy.setUpdatedAt(this.getUpdatedAt());
        return copy;
    }
}