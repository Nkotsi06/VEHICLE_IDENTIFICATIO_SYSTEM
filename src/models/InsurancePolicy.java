package models;

import javafx.beans.property.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class InsurancePolicy {
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

    // Getters and Setters
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
}