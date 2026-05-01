package models;

import java.time.LocalDate;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class InsuranceVerification extends BaseEntity {
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

    // JavaFX Properties for TableView binding
    private final StringProperty policyNumberProperty = new SimpleStringProperty();
    private final StringProperty registrationNumberProperty = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> verificationDateProperty = new SimpleObjectProperty<>();
    private final StringProperty verificationStatusProperty = new SimpleStringProperty();
    private final StringProperty verifiedByNameProperty = new SimpleStringProperty();

    public InsuranceVerification() {
        super();
        this.verificationStatus = "PENDING";
        this.isVerified = false;
        this.verificationDate = LocalDate.now();
    }

    public InsuranceVerification(int insuranceId, int verifiedBy, String verificationStatus, String notes) {
        this();
        this.insuranceId = insuranceId;
        this.verifiedBy = verifiedBy;
        this.verificationStatus = verificationStatus;
        this.notes = notes;
        this.isVerified = "VERIFIED".equals(verificationStatus);
    }

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
        this.isVerified = "VERIFIED".equals(verificationStatus);
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
    }

    public LocalDate getPolicyEndDate() {
        return policyEndDate;
    }

    public void setPolicyEndDate(LocalDate policyEndDate) {
        this.policyEndDate = policyEndDate;
        if (policyEndDate != null) {
            this.expiryDate = policyEndDate;
        }
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
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
        if (verified) {
            this.verificationStatus = "VERIFIED";
            verificationStatusProperty.set("VERIFIED");
        }
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public boolean isPending() {
        return "PENDING".equals(verificationStatus);
    }

    public boolean isRejected() {
        return "REJECTED".equals(verificationStatus);
    }

    public boolean isPolicyValid() {
        return policyEndDate != null && policyEndDate.isAfter(LocalDate.now());
    }

    public boolean isPolicyExpiringSoon() {
        if (policyEndDate == null) return false;
        LocalDate thirtyDaysFromNow = LocalDate.now().plusDays(30);
        return policyEndDate.isBefore(thirtyDaysFromNow) && policyEndDate.isAfter(LocalDate.now());
    }

    public String getVerificationStatusColor() {
        switch (verificationStatus) {
            case "VERIFIED": return "#4CAF50";
            case "REJECTED": return "#F44336";
            case "PENDING": return "#FFC107";
            default: return "#9E9E9E";
        }
    }

    public String getVerificationStatusLabel() {
        switch (verificationStatus) {
            case "VERIFIED": return "VERIFIED";
            case "REJECTED": return "REJECTED";
            case "PENDING": return "PENDING";
            default: return "UNKNOWN";
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
        return "Insurance Verification: " + policyNumber + " - " + verificationStatus + " by " + verifiedByName;
    }
}