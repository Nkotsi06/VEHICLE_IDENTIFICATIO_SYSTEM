package models;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class InspectionChecklistItem extends BaseEntity {
    private int id;
    private int inspectionId;
    private String itemName;
    private String status;
    private String notes;
    private String photoPath;

    // JavaFX Properties
    private final StringProperty itemNameProperty = new SimpleStringProperty();
    private final StringProperty statusProperty = new SimpleStringProperty();
    private final StringProperty notesProperty = new SimpleStringProperty();

    public InspectionChecklistItem() {
        super();
        this.status = "NOT_CHECKED";
    }

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

    public int getInspectionId() { return inspectionId; }
    public void setInspectionId(int inspectionId) { this.inspectionId = inspectionId; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) {
        this.itemName = itemName;
        itemNameProperty.set(itemName);
    }
    public StringProperty itemNameProperty() { return itemNameProperty; }

    public String getStatus() { return status; }
    public void setStatus(String status) {
        this.status = status;
        statusProperty.set(status);
    }
    public StringProperty statusProperty() { return statusProperty; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) {
        this.notes = notes;
        notesProperty.set(notes);
    }
    public StringProperty notesProperty() { return notesProperty; }

    public String getPhotoPath() { return photoPath; }
    public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }

    public boolean isPassed() { return "PASS".equals(status); }
    public boolean isFailed() { return "FAIL".equals(status); }

    @Override
    public int getId() { return id; }
    @Override
    public void setId(int id) { this.id = id; }

    @Override
    public String toString() {
        return itemName + " - " + status;
    }
}