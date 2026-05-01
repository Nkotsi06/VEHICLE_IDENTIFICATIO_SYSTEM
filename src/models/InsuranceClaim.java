package models;

import java.time.LocalDate;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class InsuranceClaim extends BaseEntity {
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

    // JavaFX Properties
    private final IntegerProperty idProperty = new SimpleIntegerProperty();
    private final IntegerProperty policyIdProperty = new SimpleIntegerProperty();
    private final StringProperty policyNumberProperty = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> claimDateProperty = new SimpleObjectProperty<>();
    private final DoubleProperty claimAmountProperty = new SimpleDoubleProperty();
    private final StringProperty descriptionProperty = new SimpleStringProperty();
    private final StringProperty statusProperty = new SimpleStringProperty();
    private final DoubleProperty approvedAmountProperty = new SimpleDoubleProperty();
    private final StringProperty rejectionReasonProperty = new SimpleStringProperty();
    private final IntegerProperty vehicleIdProperty = new SimpleIntegerProperty();
    private final StringProperty registrationNumberProperty = new SimpleStringProperty();

    public InsuranceClaim() {
        super();
        this.status = "PENDING";
        this.claimDate = LocalDate.now();
    }

    public InsuranceClaim(int policyId, double claimAmount, String description) {
        this();
        this.policyId = policyId;
        this.claimAmount = claimAmount;
        this.description = description;

        this.policyIdProperty.set(policyId);
        this.claimAmountProperty.set(claimAmount);
        this.descriptionProperty.set(description);
        this.statusProperty.set("PENDING");
    }

    public int getPolicyId() { return policyId; }
    public void setPolicyId(int policyId) {
        this.policyId = policyId;
        policyIdProperty.set(policyId);
    }
    public IntegerProperty policyIdProperty() { return policyIdProperty; }

    public String getPolicyNumber() { return policyNumber; }
    public void setPolicyNumber(String policyNumber) {
        this.policyNumber = policyNumber;
        policyNumberProperty.set(policyNumber);
    }
    public StringProperty policyNumberProperty() { return policyNumberProperty; }

    public LocalDate getClaimDate() { return claimDate; }
    public void setClaimDate(LocalDate claimDate) {
        this.claimDate = claimDate;
        claimDateProperty.set(claimDate);
    }
    public ObjectProperty<LocalDate> claimDateProperty() { return claimDateProperty; }

    public double getClaimAmount() { return claimAmount; }
    public void setClaimAmount(double claimAmount) {
        this.claimAmount = claimAmount;
        claimAmountProperty.set(claimAmount);
    }
    public DoubleProperty claimAmountProperty() { return claimAmountProperty; }

    public String getDescription() { return description; }
    public void setDescription(String description) {
        this.description = description;
        descriptionProperty.set(description);
    }
    public StringProperty descriptionProperty() { return descriptionProperty; }

    public String getStatus() { return status; }
    public void setStatus(String status) {
        this.status = status;
        statusProperty.set(status);
    }
    public StringProperty statusProperty() { return statusProperty; }

    public Double getApprovedAmount() { return approvedAmount; }
    public void setApprovedAmount(Double approvedAmount) {
        this.approvedAmount = approvedAmount;
        if (approvedAmount != null) approvedAmountProperty.set(approvedAmount);
    }
    public DoubleProperty approvedAmountProperty() { return approvedAmountProperty; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
        rejectionReasonProperty.set(rejectionReason);
    }
    public StringProperty rejectionReasonProperty() { return rejectionReasonProperty; }

    public int getVehicleId() { return vehicleId; }
    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
        vehicleIdProperty.set(vehicleId);
    }
    public IntegerProperty vehicleIdProperty() { return vehicleIdProperty; }

    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
        registrationNumberProperty.set(registrationNumber);
    }
    public StringProperty registrationNumberProperty() { return registrationNumberProperty; }

    public String getMake() { return make; }
    public void setMake(String make) { this.make = make; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public IntegerProperty idProperty() { return idProperty; }

    public boolean isPending() { return "PENDING".equals(status); }
    public boolean isApproved() { return "APPROVED".equals(status); }

    @Override
    public int getId() { return id; }
    @Override
    public void setId(int id) {
        this.id = id;
        idProperty.set(id);
    }

    @Override
    public String toString() {
        return "Claim #" + id + " - " + claimDate + " - $" + claimAmount + " - " + status;
    }
}