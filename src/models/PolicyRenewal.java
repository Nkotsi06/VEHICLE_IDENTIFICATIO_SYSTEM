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
 * PolicyRenewal model representing insurance policy renewals.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class PolicyRenewal extends BaseEntity {

    // Core fields
    private int id;
    private int insuranceId;
    private String policyNumber;
    private LocalDate renewalDate;
    private double premium;
    private String paymentStatus;
    private LocalDate paymentDate;
    private int vehicleId;
    private String registrationNumber;

    // Payment status constants
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_PAID = "PAID";
    public static final String STATUS_OVERDUE = "OVERDUE";
    public static final String STATUS_CANCELLED = "CANCELLED";

    // JavaFX Properties for TableView binding
    private final IntegerProperty insuranceIdProperty = new SimpleIntegerProperty();
    private final StringProperty policyNumberProperty = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> renewalDateProperty = new SimpleObjectProperty<>();
    private final DoubleProperty premiumProperty = new SimpleDoubleProperty();
    private final StringProperty paymentStatusProperty = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> paymentDateProperty = new SimpleObjectProperty<>();
    private final IntegerProperty vehicleIdProperty = new SimpleIntegerProperty();
    private final StringProperty registrationNumberProperty = new SimpleStringProperty();
    private final StringProperty statusDisplayProperty = new SimpleStringProperty();
    private final StringProperty statusColorProperty = new SimpleStringProperty();

    /**
     * Default constructor - initializes with PENDING status.
     */
    public PolicyRenewal() {
        super();
        this.paymentStatus = STATUS_PENDING;

        paymentStatusProperty.set(STATUS_PENDING);
        updateStatusDisplay();

        paymentStatusProperty.addListener((obs, oldVal, newVal) -> updateStatusDisplay());
    }

    /**
     * Constructor for creating a new renewal.
     *
     * @param insuranceId  the insurance ID
     * @param renewalDate  the renewal date
     * @param premium      the premium amount
     */
    public PolicyRenewal(int insuranceId, LocalDate renewalDate, double premium) {
        this();
        this.insuranceId = insuranceId;
        this.renewalDate = renewalDate;
        this.premium = premium;

        insuranceIdProperty.set(insuranceId);
        renewalDateProperty.set(renewalDate);
        premiumProperty.set(premium);
    }

    // ============================================
    // PRIVATE UPDATE METHODS
    // ============================================

    private void updateStatusDisplay() {
        switch (paymentStatus) {
            case STATUS_PENDING:
                statusDisplayProperty.set("Pending");
                statusColorProperty.set("#FFC107");
                break;
            case STATUS_PAID:
                statusDisplayProperty.set("Paid");
                statusColorProperty.set("#4CAF50");
                break;
            case STATUS_OVERDUE:
                statusDisplayProperty.set("Overdue");
                statusColorProperty.set("#F44336");
                break;
            case STATUS_CANCELLED:
                statusDisplayProperty.set("Cancelled");
                statusColorProperty.set("#9E9E9E");
                break;
            default:
                statusDisplayProperty.set(paymentStatus);
                statusColorProperty.set("#9E9E9E");
        }
    }

    // ============================================
    // GETTERS AND SETTERS WITH PROPERTY UPDATES
    // ============================================

    public int getInsuranceId() {
        return insuranceId;
    }

    public void setInsuranceId(int insuranceId) {
        this.insuranceId = insuranceId;
        insuranceIdProperty.set(insuranceId);
    }

    public IntegerProperty insuranceIdProperty() {
        return insuranceIdProperty;
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

    public LocalDate getRenewalDate() {
        return renewalDate;
    }

    public void setRenewalDate(LocalDate renewalDate) {
        this.renewalDate = renewalDate;
        renewalDateProperty.set(renewalDate);
    }

    public ObjectProperty<LocalDate> renewalDateProperty() {
        return renewalDateProperty;
    }

    public double getPremium() {
        return premium;
    }

    public void setPremium(double premium) {
        this.premium = premium;
        premiumProperty.set(premium);
    }

    public DoubleProperty premiumProperty() {
        return premiumProperty;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
        paymentStatusProperty.set(paymentStatus);
    }

    public StringProperty paymentStatusProperty() {
        return paymentStatusProperty;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
        paymentDateProperty.set(paymentDate);
    }

    public ObjectProperty<LocalDate> paymentDateProperty() {
        return paymentDateProperty;
    }

    public int getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
        vehicleIdProperty.set(vehicleId);
    }

    public IntegerProperty vehicleIdProperty() {
        return vehicleIdProperty;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
        registrationNumberProperty.set(registrationNumber);
    }

    public StringProperty registrationNumberProperty() {
        return registrationNumberProperty;
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

    public boolean isPaid() {
        return STATUS_PAID.equals(paymentStatus);
    }

    public boolean isPending() {
        return STATUS_PENDING.equals(paymentStatus);
    }

    public boolean isOverdue() {
        return STATUS_OVERDUE.equals(paymentStatus) ||
                (renewalDate != null && renewalDate.isBefore(LocalDate.now()) && !isPaid());
    }

    public String getFormattedRenewalDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return renewalDate != null ? renewalDate.format(formatter) : "";
    }

    public String getFormattedPaymentDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return paymentDate != null ? paymentDate.format(formatter) : "";
    }

    public String getFormattedPremium() {
        return String.format("M%,.2f", premium);
    }

    public void markAsPaid() {
        this.paymentStatus = STATUS_PAID;
        this.paymentDate = LocalDate.now();
        paymentStatusProperty.set(STATUS_PAID);
        paymentDateProperty.set(paymentDate);
    }

    public void markAsOverdue() {
        this.paymentStatus = STATUS_OVERDUE;
        paymentStatusProperty.set(STATUS_OVERDUE);
    }

    public void cancel() {
        this.paymentStatus = STATUS_CANCELLED;
        paymentStatusProperty.set(STATUS_CANCELLED);
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
        return "Renewal for " + policyNumber + " - " + getFormattedPremium() + " - " + getStatusDisplay();
    }

    /**
     * Creates a copy of this renewal.
     *
     * @return a new PolicyRenewal instance
     */
    public PolicyRenewal copy() {
        PolicyRenewal copy = new PolicyRenewal();
        copy.setId(this.id);
        copy.setInsuranceId(this.insuranceId);
        copy.setPolicyNumber(this.policyNumber);
        copy.setRenewalDate(this.renewalDate);
        copy.setPremium(this.premium);
        copy.setPaymentStatus(this.paymentStatus);
        copy.setPaymentDate(this.paymentDate);
        copy.setVehicleId(this.vehicleId);
        copy.setRegistrationNumber(this.registrationNumber);
        copy.setCreatedAt(this.getCreatedAt());
        copy.setUpdatedAt(this.getUpdatedAt());
        return copy;
    }
}