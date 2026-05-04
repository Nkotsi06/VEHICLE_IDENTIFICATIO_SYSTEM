package models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.IntegerProperty;

/**
 * CustomerComplaint model representing complaints filed by customers against workshops.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class CustomerComplaint extends BaseEntity {

    // Core fields
    private int id;
    private int customerId;
    private String customerName;
    private int workshopId;
    private String workshopName;
    private LocalDateTime complaintDate;
    private String complaintText;
    private String resolutionStatus;
    private String resolutionNotes;
    private LocalDateTime resolvedDate;

    // Status constants
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    public static final String STATUS_RESOLVED = "RESOLVED";
    public static final String STATUS_REJECTED = "REJECTED";
    public static final String STATUS_ESCALATED = "ESCALATED";

    // JavaFX Properties for TableView binding
    private final IntegerProperty customerIdProperty = new SimpleIntegerProperty();
    private final StringProperty customerNameProperty = new SimpleStringProperty();
    private final IntegerProperty workshopIdProperty = new SimpleIntegerProperty();
    private final StringProperty workshopNameProperty = new SimpleStringProperty();
    private final ObjectProperty<LocalDateTime> complaintDateProperty = new SimpleObjectProperty<>();
    private final StringProperty complaintTextProperty = new SimpleStringProperty();
    private final StringProperty resolutionStatusProperty = new SimpleStringProperty();
    private final StringProperty resolutionNotesProperty = new SimpleStringProperty();

    /**
     * Default constructor - initializes with PENDING status.
     */
    public CustomerComplaint() {
        super();
        this.resolutionStatus = STATUS_PENDING;
        this.complaintDate = LocalDateTime.now();

        resolutionStatusProperty.set(STATUS_PENDING);
        complaintDateProperty.set(this.complaintDate);
    }

    /**
     * Constructor for creating a new complaint.
     *
     * @param customerId   the customer ID
     * @param workshopId   the workshop ID
     * @param complaintText the complaint text
     */
    public CustomerComplaint(int customerId, int workshopId, String complaintText) {
        this();
        this.customerId = customerId;
        this.workshopId = workshopId;
        this.complaintText = complaintText;
        this.complaintDate = LocalDateTime.now();

        customerIdProperty.set(customerId);
        workshopIdProperty.set(workshopId);
        complaintTextProperty.set(complaintText);
    }

    // ============================================
    // GETTERS AND SETTERS WITH PROPERTY UPDATES
    // ============================================

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
        customerIdProperty.set(customerId);
    }

    public IntegerProperty customerIdProperty() {
        return customerIdProperty;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
        customerNameProperty.set(customerName);
    }

    public StringProperty customerNameProperty() {
        return customerNameProperty;
    }

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

        if (STATUS_RESOLVED.equals(resolutionStatus) || STATUS_REJECTED.equals(resolutionStatus)) {
            this.resolvedDate = LocalDateTime.now();
        }
    }

    public StringProperty resolutionStatusProperty() {
        return resolutionStatusProperty;
    }

    public String getResolutionNotes() {
        return resolutionNotes;
    }

    public void setResolutionNotes(String resolutionNotes) {
        this.resolutionNotes = resolutionNotes;
        resolutionNotesProperty.set(resolutionNotes);
    }

    public StringProperty resolutionNotesProperty() {
        return resolutionNotesProperty;
    }

    public LocalDateTime getResolvedDate() {
        return resolvedDate;
    }

    public void setResolvedDate(LocalDateTime resolvedDate) {
        this.resolvedDate = resolvedDate;
    }

    // ============================================
    // BUSINESS LOGIC METHODS
    // ============================================

    /**
     * Checks if the complaint is pending.
     *
     * @return true if status is PENDING
     */
    public boolean isPending() {
        return STATUS_PENDING.equals(resolutionStatus);
    }

    /**
     * Checks if the complaint is resolved.
     *
     * @return true if status is RESOLVED
     */
    public boolean isResolved() {
        return STATUS_RESOLVED.equals(resolutionStatus);
    }

    /**
     * Gets the status display name.
     *
     * @return human-readable status
     */
    public String getStatusDisplay() {
        switch (resolutionStatus) {
            case STATUS_PENDING: return "Pending";
            case STATUS_IN_PROGRESS: return "In Progress";
            case STATUS_RESOLVED: return "Resolved";
            case STATUS_REJECTED: return "Rejected";
            case STATUS_ESCALATED: return "Escalated";
            default: return resolutionStatus;
        }
    }

    /**
     * Gets the CSS color for the status.
     *
     * @return hex color code
     */
    public String getStatusColor() {
        switch (resolutionStatus) {
            case STATUS_PENDING: return "#FF9800";
            case STATUS_IN_PROGRESS: return "#2196F3";
            case STATUS_RESOLVED: return "#4CAF50";
            case STATUS_REJECTED: return "#F44336";
            case STATUS_ESCALATED: return "#9C27B0";
            default: return "#9E9E9E";
        }
    }

    /**
     * Gets the formatted complaint date.
     *
     * @return formatted date-time string
     */
    public String getFormattedComplaintDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return complaintDate != null ? complaintDate.format(formatter) : "";
    }

    /**
     * Gets the complaint preview (first 100 characters).
     *
     * @return preview text
     */
    public String getComplaintPreview() {
        if (complaintText == null) return "";
        if (complaintText.length() <= 100) return complaintText;
        return complaintText.substring(0, 100) + "...";
    }

    /**
     * Escalates the complaint to admin.
     */
    public void escalate() {
        this.resolutionStatus = STATUS_ESCALATED;
        resolutionStatusProperty.set(STATUS_ESCALATED);
    }

    /**
     * Resolves the complaint with notes.
     *
     * @param notes resolution notes
     */
    public void resolve(String notes) {
        this.resolutionStatus = STATUS_RESOLVED;
        this.resolutionNotes = notes;
        this.resolvedDate = LocalDateTime.now();
        resolutionStatusProperty.set(STATUS_RESOLVED);
        resolutionNotesProperty.set(notes);
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
        return getComplaintPreview() + " - " + getStatusDisplay();
    }

    /**
     * Creates a copy of this complaint.
     *
     * @return a new CustomerComplaint instance
     */
    public CustomerComplaint copy() {
        CustomerComplaint copy = new CustomerComplaint();
        copy.setId(this.id);
        copy.setCustomerId(this.customerId);
        copy.setCustomerName(this.customerName);
        copy.setWorkshopId(this.workshopId);
        copy.setWorkshopName(this.workshopName);
        copy.setComplaintDate(this.complaintDate);
        copy.setComplaintText(this.complaintText);
        copy.setResolutionStatus(this.resolutionStatus);
        copy.setResolutionNotes(this.resolutionNotes);
        copy.setResolvedDate(this.resolvedDate);
        copy.setCreatedAt(this.getCreatedAt());
        copy.setUpdatedAt(this.getUpdatedAt());
        return copy;
    }
}