package models;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * InsuranceClaim model representing insurance claims filed by policyholders.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class InsuranceClaim extends BaseEntity {

    // Core fields
    private int id;
    private int policyId;
    private String policyNumber;
    private LocalDate claimDate;
    private double claimAmount;
    private String description;
    private String status;
    private Double approvedAmount;
    private String rejectionReason;
    private int vehicleId;
    private String registrationNumber;
    private String make;
    private String model;
    private LocalDate incidentDate;
    private String incidentLocation;

    // Status constants
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_UNDER_REVIEW = "UNDER_REVIEW";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";
    public static final String STATUS_PAID = "PAID";
    public static final String STATUS_CLOSED = "CLOSED";

    // Status display names
    private static final java.util.Map<String, String> STATUS_DISPLAY = new java.util.HashMap<>();
    static {
        STATUS_DISPLAY.put(STATUS_PENDING, "Pending");
        STATUS_DISPLAY.put(STATUS_UNDER_REVIEW, "Under Review");
        STATUS_DISPLAY.put(STATUS_APPROVED, "Approved");
        STATUS_DISPLAY.put(STATUS_REJECTED, "Rejected");
        STATUS_DISPLAY.put(STATUS_PAID, "Paid");
        STATUS_DISPLAY.put(STATUS_CLOSED, "Closed");
    }

    // JavaFX Properties for TableView binding
    private final IntegerProperty policyIdProperty = new SimpleIntegerProperty();
    private final StringProperty policyNumberProperty = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> claimDateProperty = new SimpleObjectProperty<>();
    private final DoubleProperty claimAmountProperty = new SimpleDoubleProperty();
    private final StringProperty descriptionProperty = new SimpleStringProperty();
    private final StringProperty statusProperty = new SimpleStringProperty();
    private final DoubleProperty approvedAmountProperty = new SimpleDoubleProperty();
    private final StringProperty rejectionReasonProperty = new SimpleStringProperty();
    private final StringProperty registrationNumberProperty = new SimpleStringProperty();
    private final StringProperty vehicleInfoProperty = new SimpleStringProperty();

    /**
     * Default constructor - initializes with PENDING status and current date.
     */
    public InsuranceClaim() {
        super();
        this.status = STATUS_PENDING;
        this.claimDate = LocalDate.now();

        statusProperty.set(STATUS_PENDING);
        claimDateProperty.set(claimDate);
    }

    /**
     * Constructor for creating a new claim.
     *
     * @param policyId     the policy ID
     * @param claimAmount  the claim amount
     * @param description  the claim description
     */
    public InsuranceClaim(int policyId, double claimAmount, String description) {
        this();
        this.policyId = policyId;
        this.claimAmount = claimAmount;
        this.description = description;

        policyIdProperty.set(policyId);
        claimAmountProperty.set(claimAmount);
        descriptionProperty.set(description);
    }

    // ============================================
    // GETTERS AND SETTERS WITH PROPERTY UPDATES
    // ============================================

    public int getPolicyId() {
        return policyId;
    }

    public void setPolicyId(int policyId) {
        this.policyId = policyId;
        policyIdProperty.set(policyId);
    }

    public IntegerProperty policyIdProperty() {
        return policyIdProperty;
    }

    public String getPolicyNumber() {
        return policyNumber;
    }

    public void setPolicyNumber(String policyNumber) {
        this.policyNumber = policyNumber;
        policyNumberProperty.set(policyNumber);
    }

    public StringProperty policyNumberProperty() {
        return policyNumberProperty;
    }

    public LocalDate getClaimDate() {
        return claimDate;
    }

    public void setClaimDate(LocalDate claimDate) {
        this.claimDate = claimDate;
        claimDateProperty.set(claimDate);
    }

    public ObjectProperty<LocalDate> claimDateProperty() {
        return claimDateProperty;
    }

    public double getClaimAmount() {
        return claimAmount;
    }

    public void setClaimAmount(double claimAmount) {
        this.claimAmount = claimAmount;
        claimAmountProperty.set(claimAmount);
    }

    public DoubleProperty claimAmountProperty() {
        return claimAmountProperty;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        descriptionProperty.set(description);
    }

    public StringProperty descriptionProperty() {
        return descriptionProperty;
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

    public Double getApprovedAmount() {
        return approvedAmount;
    }

    public void setApprovedAmount(Double approvedAmount) {
        this.approvedAmount = approvedAmount;
        if (approvedAmount != null) {
            approvedAmountProperty.set(approvedAmount);
        }
    }

    public DoubleProperty approvedAmountProperty() {
        return approvedAmountProperty;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
        rejectionReasonProperty.set(rejectionReason);
    }

    public StringProperty rejectionReasonProperty() {
        return rejectionReasonProperty;
    }

    public int getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
        registrationNumberProperty.set(registrationNumber);
        updateVehicleInfo();
    }

    public StringProperty registrationNumberProperty() {
        return registrationNumberProperty;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
        updateVehicleInfo();
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
        updateVehicleInfo();
    }

    public LocalDate getIncidentDate() {
        return incidentDate;
    }

    public void setIncidentDate(LocalDate incidentDate) {
        this.incidentDate = incidentDate;
    }

    public String getIncidentLocation() {
        return incidentLocation;
    }

    public void setIncidentLocation(String incidentLocation) {
        this.incidentLocation = incidentLocation;
    }

    public StringProperty vehicleInfoProperty() {
        return vehicleInfoProperty;
    }

    private void updateVehicleInfo() {
        String info = registrationNumber != null ? registrationNumber : "";
        if (make != null && model != null) {
            info += " (" + make + " " + model + ")";
        }
        vehicleInfoProperty.set(info);
    }

    // ============================================
    // BUSINESS LOGIC METHODS
    // ============================================

    /**
     * Checks if the claim is pending.
     *
     * @return true if status is PENDING
     */
    public boolean isPending() {
        return STATUS_PENDING.equals(status);
    }

    /**
     * Checks if the claim is approved.
     *
     * @return true if status is APPROVED
     */
    public boolean isApproved() {
        return STATUS_APPROVED.equals(status);
    }

    /**
     * Checks if the claim is rejected.
     *
     * @return true if status is REJECTED
     */
    public boolean isRejected() {
        return STATUS_REJECTED.equals(status);
    }

    /**
     * Checks if the claim is under review.
     *
     * @return true if status is UNDER_REVIEW
     */
    public boolean isUnderReview() {
        return STATUS_UNDER_REVIEW.equals(status);
    }

    /**
     * Checks if the claim is paid.
     *
     * @return true if status is PAID
     */
    public boolean isPaid() {
        return STATUS_PAID.equals(status);
    }

    /**
     * Gets the status display name.
     *
     * @return human-readable status
     */
    public String getStatusDisplay() {
        return STATUS_DISPLAY.getOrDefault(status, status);
    }

    /**
     * Gets the status color for UI.
     *
     * @return hex color code
     */
    public String getStatusColor() {
        switch (status) {
            case STATUS_PENDING: return "#FFC107";
            case STATUS_UNDER_REVIEW: return "#2196F3";
            case STATUS_APPROVED: return "#4CAF50";
            case STATUS_REJECTED: return "#F44336";
            case STATUS_PAID: return "#9C27B0";
            case STATUS_CLOSED: return "#9E9E9E";
            default: return "#9E9E9E";
        }
    }

    /**
     * Gets the formatted claim date.
     *
     * @return formatted date string
     */
    public String getFormattedClaimDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return claimDate != null ? claimDate.format(formatter) : "";
    }

    /**
     * Gets the formatted claim amount.
     *
     * @return formatted currency string
     */
    public String getFormattedClaimAmount() {
        return String.format("M%,.2f", claimAmount);
    }

    /**
     * Gets the formatted approved amount.
     *
     * @return formatted currency string
     */
    public String getFormattedApprovedAmount() {
        if (approvedAmount == null) return "—";
        return String.format("M%,.2f", approvedAmount);
    }

    /**
     * Gets the claim preview.
     *
     * @return preview text
     */
    public String getDescriptionPreview() {
        if (description == null) return "";
        if (description.length() <= 100) return description;
        return description.substring(0, 100) + "...";
    }

    /**
     * Approves the claim.
     *
     * @param amount the approved amount
     */
    public void approve(double amount) {
        this.status = STATUS_APPROVED;
        this.approvedAmount = amount;
        statusProperty.set(STATUS_APPROVED);
        approvedAmountProperty.set(amount);
    }

    /**
     * Rejects the claim.
     *
     * @param reason the rejection reason
     */
    public void reject(String reason) {
        this.status = STATUS_REJECTED;
        this.rejectionReason = reason;
        statusProperty.set(STATUS_REJECTED);
        rejectionReasonProperty.set(reason);
    }

    /**
     * Marks the claim as paid.
     */
    public void markAsPaid() {
        this.status = STATUS_PAID;
        statusProperty.set(STATUS_PAID);
    }

    /**
     * Sends for review.
     */
    public void sendForReview() {
        this.status = STATUS_UNDER_REVIEW;
        statusProperty.set(STATUS_UNDER_REVIEW);
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
        return "Claim #" + id + " - " + getFormattedClaimDate() + " - " + getFormattedClaimAmount() + " - " + getStatusDisplay();
    }

    /**
     * Creates a copy of this claim.
     *
     * @return a new InsuranceClaim instance
     */
    public InsuranceClaim copy() {
        InsuranceClaim copy = new InsuranceClaim();
        copy.setId(this.id);
        copy.setPolicyId(this.policyId);
        copy.setPolicyNumber(this.policyNumber);
        copy.setClaimDate(this.claimDate);
        copy.setClaimAmount(this.claimAmount);
        copy.setDescription(this.description);
        copy.setStatus(this.status);
        copy.setApprovedAmount(this.approvedAmount);
        copy.setRejectionReason(this.rejectionReason);
        copy.setVehicleId(this.vehicleId);
        copy.setRegistrationNumber(this.registrationNumber);
        copy.setMake(this.make);
        copy.setModel(this.model);
        copy.setIncidentDate(this.incidentDate);
        copy.setIncidentLocation(this.incidentLocation);
        copy.setCreatedAt(this.getCreatedAt());
        copy.setUpdatedAt(this.getUpdatedAt());
        return copy;
    }
}