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

public class PolicyRenewal extends BaseEntity {
    private int id;
    private int insuranceId;
    private String policyNumber;
    private LocalDate renewalDate;
    private double premium;
    private String paymentStatus;
    private LocalDate paymentDate;
    private int vehicleId;
    private String registrationNumber;

    // JavaFX Properties for TableView binding
    private final IntegerProperty insuranceIdProperty = new SimpleIntegerProperty();
    private final StringProperty policyNumberProperty = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> renewalDateProperty = new SimpleObjectProperty<>();
    private final DoubleProperty premiumProperty = new SimpleDoubleProperty();
    private final StringProperty paymentStatusProperty = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> paymentDateProperty = new SimpleObjectProperty<>();
    private final IntegerProperty vehicleIdProperty = new SimpleIntegerProperty();
    private final StringProperty registrationNumberProperty = new SimpleStringProperty();

    public PolicyRenewal() {
        super();
        this.paymentStatus = "PENDING";
    }

    public PolicyRenewal(int insuranceId, LocalDate renewalDate, double premium) {
        this();
        this.insuranceId = insuranceId;
        this.renewalDate = renewalDate;
        this.premium = premium;

        // Update properties
        insuranceIdProperty.set(insuranceId);
        renewalDateProperty.set(renewalDate);
        premiumProperty.set(premium);
        paymentStatusProperty.set("PENDING");
    }

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

    public boolean isPaid() {
        return "PAID".equals(paymentStatus);
    }

    public boolean isPending() {
        return "PENDING".equals(paymentStatus);
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
        return "Renewal for " + policyNumber + " - " + premium + " - " + paymentStatus;
    }
}