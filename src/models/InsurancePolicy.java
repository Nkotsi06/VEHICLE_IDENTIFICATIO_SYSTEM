package models;

import javafx.beans.property.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * InsurancePolicy model representing an insurance policy for a vehicle.
 * Uses JavaFX properties for TableView binding.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class InsurancePolicy {

    // Status constants
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_EXPIRED = "EXPIRED";
    public static final String STATUS_CANCELLED = "CANCELLED";
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_SUSPENDED = "SUSPENDED";

    // Core properties
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final IntegerProperty vehicleId = new SimpleIntegerProperty();
    private final IntegerProperty providerId = new SimpleIntegerProperty();
    private final StringProperty policyNumber = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> startDate = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> endDate = new SimpleObjectProperty<>();
    private final DoubleProperty premium = new SimpleDoubleProperty();
    private final DoubleProperty coverageAmount = new SimpleDoubleProperty();
    private final StringProperty status = new SimpleStringProperty();
    private final ObjectProperty<LocalDateTime> createdAt = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDateTime> updatedAt = new SimpleObjectProperty<>();

    // Vehicle related fields (from joined queries)
    private final StringProperty registrationNumber = new SimpleStringProperty();
    private final StringProperty make = new SimpleStringProperty();
    private final StringProperty model = new SimpleStringProperty();
    private final StringProperty vehicleMake = new SimpleStringProperty();
    private final StringProperty vehicleModel = new SimpleStringProperty();
    private final StringProperty ownerName = new SimpleStringProperty();
    private final IntegerProperty ownerId = new SimpleIntegerProperty();

    // Provider related fields (from joined queries)
    private final StringProperty providerName = new SimpleStringProperty();

    // Additional computed properties
    private final StringProperty formattedPremiumProperty = new SimpleStringProperty();
    private final StringProperty formattedCoverageProperty = new SimpleStringProperty();
    private final StringProperty statusDisplayProperty = new SimpleStringProperty();
    private final StringProperty statusColorProperty = new SimpleStringProperty();

    /**
     * Default constructor.
     */
    public InsurancePolicy() {
        // Initialize with default values
        status.set(STATUS_PENDING);
        createdAt.set(LocalDateTime.now());
        updatedAt.set(LocalDateTime.now());

        // Setup listeners for derived properties
        premium.addListener((obs, oldVal, newVal) -> updateFormattedPremium());
        coverageAmount.addListener((obs, oldVal, newVal) -> updateFormattedCoverage());
        status.addListener((obs, oldVal, newVal) -> updateStatusDisplay());

        updateFormattedPremium();
        updateFormattedCoverage();
        updateStatusDisplay();
    }

    // ============================================
    // PRIVATE UPDATE METHODS
    // ============================================

    private void updateFormattedPremium() {
        formattedPremiumProperty.set(String.format("M%,.2f", premium.get()));
    }

    private void updateFormattedCoverage() {
        formattedCoverageProperty.set(String.format("M%,.2f", coverageAmount.get()));
    }

    private void updateStatusDisplay() {
        String currentStatus = status.get();
        switch (currentStatus) {
            case STATUS_ACTIVE:
                statusDisplayProperty.set("Active");
                statusColorProperty.set("#4CAF50");
                break;
            case STATUS_EXPIRED:
                statusDisplayProperty.set("Expired");
                statusColorProperty.set("#9E9E9E");
                break;
            case STATUS_CANCELLED:
                statusDisplayProperty.set("Cancelled");
                statusColorProperty.set("#F44336");
                break;
            case STATUS_PENDING:
                statusDisplayProperty.set("Pending");
                statusColorProperty.set("#FFC107");
                break;
            case STATUS_SUSPENDED:
                statusDisplayProperty.set("Suspended");
                statusColorProperty.set("#FF9800");
                break;
            default:
                statusDisplayProperty.set(currentStatus);
                statusColorProperty.set("#9E9E9E");
        }
    }

    // ============================================
    // BUSINESS LOGIC METHODS
    // ============================================

    public boolean isActive() {
        LocalDate now = LocalDate.now();
        return STATUS_ACTIVE.equals(status.get()) &&
                startDate.get() != null && endDate.get() != null &&
                !startDate.get().isAfter(now) && !endDate.get().isBefore(now);
    }

    public boolean isExpired() {
        return endDate.get() != null && endDate.get().isBefore(LocalDate.now());
    }

    public boolean isExpiringSoon(int daysThreshold) {
        if (endDate.get() == null) return false;
        LocalDate threshold = LocalDate.now().plusDays(daysThreshold);
        return endDate.get().isBefore(threshold) && endDate.get().isAfter(LocalDate.now());
    }

    public int getDaysRemaining() {
        if (endDate.get() == null) return -1;
        return (int) java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), endDate.get());
    }

    public String getFormattedStartDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return startDate.get() != null ? startDate.get().format(formatter) : "";
    }

    public String getFormattedEndDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return endDate.get() != null ? endDate.get().format(formatter) : "";
    }

    public String getVehicleInfo() {
        String info = registrationNumber.get() != null ? registrationNumber.get() : "";
        String makeModel = "";
        if (make.get() != null && model.get() != null) {
            makeModel = make.get() + " " + model.get();
        } else if (vehicleMake.get() != null && vehicleModel.get() != null) {
            makeModel = vehicleMake.get() + " " + vehicleModel.get();
        }
        if (!makeModel.isEmpty()) {
            info += " (" + makeModel + ")";
        }
        return info;
    }

    // ============================================
    // GETTERS AND SETTERS
    // ============================================

    public int getId() { return id.get(); }
    public void setId(int value) { id.set(value); }
    public IntegerProperty idProperty() { return id; }

    public int getVehicleId() { return vehicleId.get(); }
    public void setVehicleId(int value) { vehicleId.set(value); }
    public IntegerProperty vehicleIdProperty() { return vehicleId; }

    public int getProviderId() { return providerId.get(); }
    public void setProviderId(int value) { providerId.set(value); }
    public IntegerProperty providerIdProperty() { return providerId; }

    public String getPolicyNumber() { return policyNumber.get(); }
    public void setPolicyNumber(String value) { policyNumber.set(value); }
    public StringProperty policyNumberProperty() { return policyNumber; }

    public LocalDate getStartDate() { return startDate.get(); }
    public void setStartDate(LocalDate value) { startDate.set(value); }
    public ObjectProperty<LocalDate> startDateProperty() { return startDate; }

    public LocalDate getEndDate() { return endDate.get(); }
    public void setEndDate(LocalDate value) { endDate.set(value); }
    public ObjectProperty<LocalDate> endDateProperty() { return endDate; }

    public double getPremium() { return premium.get(); }
    public void setPremium(double value) { premium.set(value); }
    public DoubleProperty premiumProperty() { return premium; }

    public double getCoverageAmount() { return coverageAmount.get(); }
    public void setCoverageAmount(double value) { coverageAmount.set(value); }
    public DoubleProperty coverageAmountProperty() { return coverageAmount; }

    public String getStatus() { return status.get(); }
    public void setStatus(String value) { status.set(value); }
    public StringProperty statusProperty() { return status; }

    public LocalDateTime getCreatedAt() { return createdAt.get(); }
    public void setCreatedAt(LocalDateTime value) { createdAt.set(value); }
    public ObjectProperty<LocalDateTime> createdAtProperty() { return createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt.get(); }
    public void setUpdatedAt(LocalDateTime value) { updatedAt.set(value); }
    public ObjectProperty<LocalDateTime> updatedAtProperty() { return updatedAt; }

    // Vehicle related getters and setters
    public String getRegistrationNumber() { return registrationNumber.get(); }
    public void setRegistrationNumber(String value) { registrationNumber.set(value); }
    public StringProperty registrationNumberProperty() { return registrationNumber; }

    public String getMake() { return make.get(); }
    public void setMake(String value) { make.set(value); }
    public StringProperty makeProperty() { return make; }

    public String getModel() { return model.get(); }
    public void setModel(String value) { model.set(value); }
    public StringProperty modelProperty() { return model; }

    public String getVehicleMake() { return vehicleMake.get(); }
    public void setVehicleMake(String value) { vehicleMake.set(value); }
    public StringProperty vehicleMakeProperty() { return vehicleMake; }

    public String getVehicleModel() { return vehicleModel.get(); }
    public void setVehicleModel(String value) { vehicleModel.set(value); }
    public StringProperty vehicleModelProperty() { return vehicleModel; }

    public String getOwnerName() { return ownerName.get(); }
    public void setOwnerName(String value) { ownerName.set(value); }
    public StringProperty ownerNameProperty() { return ownerName; }

    public int getOwnerId() { return ownerId.get(); }
    public void setOwnerId(int value) { ownerId.set(value); }
    public IntegerProperty ownerIdProperty() { return ownerId; }

    // Provider related getters and setters
    public String getProviderName() { return providerName.get(); }
    public void setProviderName(String value) { providerName.set(value); }
    public StringProperty providerNameProperty() { return providerName; }

    // Computed property getters
    public String getFormattedPremium() { return formattedPremiumProperty.get(); }
    public StringProperty formattedPremiumProperty() { return formattedPremiumProperty; }

    public String getFormattedCoverage() { return formattedCoverageProperty.get(); }
    public StringProperty formattedCoverageProperty() { return formattedCoverageProperty; }

    public String getStatusDisplay() { return statusDisplayProperty.get(); }
    public StringProperty statusDisplayProperty() { return statusDisplayProperty; }

    public String getStatusColor() { return statusColorProperty.get(); }
    public StringProperty statusColorProperty() { return statusColorProperty; }

    // ============================================
    // OVERRIDE METHODS
    // ============================================

    @Override
    public String toString() {
        return policyNumber.get() + " - " + providerName.get() + " - " + getStatusDisplay();
    }

    /**
     * Creates a copy of this policy.
     *
     * @return a new InsurancePolicy instance
     */
    public InsurancePolicy copy() {
        InsurancePolicy copy = new InsurancePolicy();
        copy.setId(this.getId());
        copy.setVehicleId(this.getVehicleId());
        copy.setProviderId(this.getProviderId());
        copy.setPolicyNumber(this.getPolicyNumber());
        copy.setStartDate(this.getStartDate());
        copy.setEndDate(this.getEndDate());
        copy.setPremium(this.getPremium());
        copy.setCoverageAmount(this.getCoverageAmount());
        copy.setStatus(this.getStatus());
        copy.setRegistrationNumber(this.getRegistrationNumber());
        copy.setMake(this.getMake());
        copy.setModel(this.getModel());
        copy.setOwnerName(this.getOwnerName());
        copy.setOwnerId(this.getOwnerId());
        copy.setProviderName(this.getProviderName());
        copy.setCreatedAt(this.getCreatedAt());
        copy.setUpdatedAt(this.getUpdatedAt());
        return copy;
    }
}