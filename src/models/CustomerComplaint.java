package models;

import java.time.LocalDateTime;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class CustomerComplaint extends BaseEntity {
    private int id;
    private int customerId;
    private String customerName;
    private int workshopId;
    private String workshopName;
    private LocalDateTime complaintDate;
    private String complaintText;
    private String resolutionStatus;
    private String resolutionNotes;

    // JavaFX Properties for TableView binding
    private final StringProperty workshopNameProperty = new SimpleStringProperty();
    private final ObjectProperty<LocalDateTime> complaintDateProperty = new SimpleObjectProperty<>();
    private final StringProperty complaintTextProperty = new SimpleStringProperty();
    private final StringProperty resolutionStatusProperty = new SimpleStringProperty();

    public CustomerComplaint() {
        super();
        this.resolutionStatus = "PENDING";
    }

    public CustomerComplaint(int customerId, int workshopId, String complaintText) {
        this();
        this.customerId = customerId;
        this.workshopId = workshopId;
        this.complaintText = complaintText;
        this.complaintDate = LocalDateTime.now();
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public int getWorkshopId() {
        return workshopId;
    }

    public void setWorkshopId(int workshopId) {
        this.workshopId = workshopId;
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

    public LocalDateTime getComplaintDate() {
        return complaintDate;
    }

    public void setComplaintDate(LocalDateTime complaintDate) {
        this.complaintDate = complaintDate;
        complaintDateProperty.set(complaintDate);
    }

    public ObjectProperty<LocalDateTime> complaintDateProperty() {
        return complaintDateProperty;
    }

    public String getComplaintText() {
        return complaintText;
    }

    public void setComplaintText(String complaintText) {
        this.complaintText = complaintText;
        complaintTextProperty.set(complaintText);
    }

    public StringProperty complaintTextProperty() {
        return complaintTextProperty;
    }

    public String getResolutionStatus() {
        return resolutionStatus;
    }

    public void setResolutionStatus(String resolutionStatus) {
        this.resolutionStatus = resolutionStatus;
        resolutionStatusProperty.set(resolutionStatus);
    }

    public StringProperty resolutionStatusProperty() {
        return resolutionStatusProperty;
    }

    public String getResolutionNotes() {
        return resolutionNotes;
    }

    public void setResolutionNotes(String resolutionNotes) {
        this.resolutionNotes = resolutionNotes;
    }

    public boolean isPending() {
        return "PENDING".equals(resolutionStatus);
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
        return complaintText.substring(0, Math.min(50, complaintText.length())) + "... - " + resolutionStatus;
    }
}