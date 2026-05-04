package models;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * InsuranceVerification model representing verification records for insurance policies.
 * Used by police to verify insurance status of vehicles.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class InsuranceVerification extends BaseEntity {

    // Core fields
    private int id;
    private int insuranceId;
    private String policyNumber;
    private int vehicleId;
    private String registrationNumber;
    private String make;
    private String model;
    private int verifiedBy;
    private String verifiedByName;
    private LocalDate verificationDate;
    private String verificationStatus;
    private String notes;
    private LocalDate policyStartDate;
    private LocalDate policyEndDate;
    private double premium;
    private double coverageAmount;
    private String providerName;
    private boolean isVerified;
    private LocalDate expiryDate;

    // Status constants
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_VERIFIED = "VERIFIED";
    public static final String STATUS_REJECTED = "REJECTED";
    public static final String STATUS_EXPIRED = "EXPIRED";

    // JavaFX Properties for TableView binding
    private final StringProperty policyNumberProperty = new SimpleStringProperty();
    private final StringProperty registrationNumberProperty = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> verificationDateProperty = new SimpleObjectProperty<>();
    private final StringProperty verificationStatusProperty = new SimpleStringProperty();
    private final StringProperty verifiedByNameProperty = new SimpleStringProperty();
    private final StringProperty providerNameProperty = new SimpleStringProperty();
    private final StringProperty policyPeriodProperty = new SimpleStringProperty();
    private final BooleanProperty isValidProperty = new SimpleBooleanProperty();

    /**
     * Default constructor - initializes with PENDING status.
     */
    public InsuranceVerification() {
        super();
        this.verificationStatus = STATUS_PENDING;
        this.isVerified = false;
        this.verificationDate = LocalDate.now();

        verificationStatusProperty.set(STATUS_PENDING);
        verificationDateProperty.set(verificationDate);
        isValidProperty.set(false);
    }

    /**
     * Constructor for creating a verification record.
     *
     * @param insuranceId        the insurance ID
     * @param verifiedBy         the ID of the verifying officer
     * @param verificationStatus the verification status
     * @param notes              verification notes
     */
    public InsuranceVerification(int insuranceId, int verifiedBy, String verificationStatus, String notes) {
        this();
        this.insuranceId = insuranceId;
        this.verifiedBy = verifiedBy;
        this.verificationStatus = verificationStatus;
        this.notes = notes;
        this.isVerified = STATUS_VERIFIED.equals(verificationStatus);

        verificationStatusProperty.set(verificationStatus);
        isValidProperty.set(this.isVerified);
    }

    // ============================================
    // GETTERS AND SETTERS WITH PROPERTY UPDATES
    // ============================================

    public int getInsuranceId() {
        return insuranceId;
    }

    public void setInsuranceId(int insuranceId) {
        this.insuranceId = insuranceId;
    }

    public String getPolicyNumber() {
        return policyNumber;
    }

    public void setPolicyNumber(String policyNumber) {
        this.policyNumber = policyNumber;
        policyNumberProperty.set(policyNumber);
        updatePolicyPeriod();
    }

    public StringProperty policyNumberProperty() {
        return policyNumberProperty;
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
    }

    public StringProperty registrationNumberProperty() {
        return registrationNumberProperty;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getVerifiedBy() {
        return verifiedBy;
    }

    public void setVerifiedBy(int verifiedBy) {
        this.verifiedBy = verifiedBy;
    }

    public String getVerifiedByName() {
        return verifiedByName;
    }

    public void setVerifiedByName(String verifiedByName) {
        this.verifiedByName = verifiedByName;
        verifiedByNameProperty.set(verifiedByName);
    }

    public StringProperty verifiedByNameProperty() {
        return verifiedByNameProperty;
    }

    public LocalDate getVerificationDate() {
        return verificationDate;
    }

    public void setVerificationDate(LocalDate verificationDate) {
        this.verificationDate = verificationDate;
        verificationDateProperty.set(verificationDate);
    }

    public ObjectProperty<LocalDate> verificationDateProperty() {
        return verificationDateProperty;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
        verificationStatusProperty.set(verificationStatus);
        this.isVerified = STATUS_VERIFIED.equals(verificationStatus);
        isValidProperty.set(this.isVerified);
    }

    public StringProperty verificationStatusProperty() {
        return verificationStatusProperty;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDate getPolicyStartDate() {
        return policyStartDate;
    }

    public void setPolicyStartDate(LocalDate policyStartDate) {
        this.policyStartDate = policyStartDate;
        updatePolicyPeriod();
    }

    public LocalDate getPolicyEndDate() {
        return policyEndDate;
    }

    public void setPolicyEndDate(LocalDate policyEndDate) {
        this.policyEndDate = policyEndDate;
        if (policyEndDate != null) {
            this.expiryDate = policyEndDate;
        }
        updatePolicyPeriod();
        isValidProperty.set(isPolicyValid());
    }

    public double getPremium() {
        return premium;
    }

    public void setPremium(double premium) {
        this.premium = premium;
    }

    public double getCoverageAmount() {
        return coverageAmount;
    }

    public void setCoverageAmount(double coverageAmount) {
        this.coverageAmount = coverageAmount;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
        providerNameProperty.set(providerName);
    }

    public StringProperty providerNameProperty() {
        return providerNameProperty;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
        if (verified) {
            this.verificationStatus = STATUS_VERIFIED;
            verificationStatusProperty.set(STATUS_VERIFIED);
        }
        isValidProperty.set(verified && isPolicyValid());
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public StringProperty policyPeriodProperty() {
        return policyPeriodProperty;
    }

    public BooleanProperty isValidProperty() {
        return isValidProperty;
    }

    private void updatePolicyPeriod() {
        if (policyStartDate != null && policyEndDate != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            policyPeriodProperty.set(policyStartDate.format(formatter) + " - " + policyEndDate.format(formatter));
        } else {
            policyPeriodProperty.set("");
        }
    }

    // ============================================
    // BUSINESS LOGIC METHODS
    // ============================================

    public boolean isPending() {
        return STATUS_PENDING.equals(verificationStatus);
    }

    public boolean isRejected() {
        return STATUS_REJECTED.equals(verificationStatus);
    }

    public boolean isPolicyValid() {
        return policyEndDate != null && policyEndDate.isAfter(LocalDate.now());
    }

    public boolean isPolicyExpired() {
        return policyEndDate != null && policyEndDate.isBefore(LocalDate.now());
    }

    public boolean isPolicyExpiringSoon() {
        if (policyEndDate == null) return false;
        LocalDate thirtyDaysFromNow = LocalDate.now().plusDays(30);
        return policyEndDate.isBefore(thirtyDaysFromNow) && policyEndDate.isAfter(LocalDate.now());
    }

    public String getVerificationStatusColor() {
        switch (verificationStatus) {
            case STATUS_VERIFIED: return "#4CAF50";
            case STATUS_REJECTED: return "#F44336";
            case STATUS_PENDING: return "#FFC107";
            case STATUS_EXPIRED: return "#9E9E9E";
            default: return "#9E9E9E";
        }
    }

    public String getVerificationStatusLabel() {
        switch (verificationStatus) {
            case STATUS_VERIFIED: return "Verified";
            case STATUS_REJECTED: return "Rejected";
            case STATUS_PENDING: return "Pending";
            case STATUS_EXPIRED: return "Expired";
            default: return "Unknown";
        }
    }

    public String getPolicyValidityStatus() {
        if (policyEndDate == null) return "UNKNOWN";
        if (policyEndDate.isBefore(LocalDate.now())) return "EXPIRED";
        if (policyEndDate.minusDays(30).isBefore(LocalDate.now())) return "EXPIRING_SOON";
        return "VALID";
    }

    public String getPolicyValidityColor() {
        String status = getPolicyValidityStatus();
        switch (status) {
            case "EXPIRED": return "#F44336";
            case "EXPIRING_SOON": return "#FF9800";
            case "VALID": return "#4CAF50";
            default: return "#9E9E9E";
        }
    }

    public String getFormattedVerificationDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return verificationDate != null ? verificationDate.format(formatter) : "";
    }

    public String getFormattedPolicyPeriod() {
        return policyPeriodProperty.get();
    }

    public String getFormattedPremium() {
        return String.format("M%,.2f", premium);
    }

    public String getFormattedCoverage() {
        return String.format("M%,.2f", coverageAmount);
    }

    /**
     * Verifies the insurance policy.
     *
     * @param verifierName name of the verifying officer
     * @param notes        verification notes
     */
    public void verify(String verifierName, String notes) {
        this.verificationStatus = STATUS_VERIFIED;
        this.verifiedByName = verifierName;
        this.notes = notes;
        this.isVerified = true;
        this.verificationDate = LocalDate.now();

        verificationStatusProperty.set(STATUS_VERIFIED);
        verifiedByNameProperty.set(verifierName);
        verificationDateProperty.set(this.verificationDate);
        isValidProperty.set(isPolicyValid());
    }

    /**
     * Rejects the verification.
     *
     * @param verifierName name of the rejecting officer
     * @param reason       rejection reason
     */
    public void reject(String verifierName, String reason) {
        this.verificationStatus = STATUS_REJECTED;
        this.verifiedByName = verifierName;
        this.notes = reason;
        this.isVerified = false;
        this.verificationDate = LocalDate.now();

        verificationStatusProperty.set(STATUS_REJECTED);
        verifiedByNameProperty.set(verifierName);
        verificationDateProperty.set(this.verificationDate);
        isValidProperty.set(false);
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
        return "Insurance Verification: " + policyNumber + " - " + getVerificationStatusLabel() + " by " + verifiedByName;
    }

    /**
     * Creates a copy of this verification.
     *
     * @return a new InsuranceVerification instance
     */
    public InsuranceVerification copy() {
        InsuranceVerification copy = new InsuranceVerification();
        copy.setId(this.id);
        copy.setInsuranceId(this.insuranceId);
        copy.setPolicyNumber(this.policyNumber);
        copy.setVehicleId(this.vehicleId);
        copy.setRegistrationNumber(this.registrationNumber);
        copy.setMake(this.make);
        copy.setModel(this.model);
        copy.setVerifiedBy(this.verifiedBy);
        copy.setVerifiedByName(this.verifiedByName);
        copy.setVerificationDate(this.verificationDate);
        copy.setVerificationStatus(this.verificationStatus);
        copy.setNotes(this.notes);
        copy.setPolicyStartDate(this.policyStartDate);
        copy.setPolicyEndDate(this.policyEndDate);
        copy.setPremium(this.premium);
        copy.setCoverageAmount(this.coverageAmount);
        copy.setProviderName(this.providerName);
        copy.setVerified(this.isVerified);
        copy.setExpiryDate(this.expiryDate);
        copy.setCreatedAt(this.getCreatedAt());
        copy.setUpdatedAt(this.getUpdatedAt());
        return copy;
    }
}