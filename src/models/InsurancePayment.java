package models;

import java.time.LocalDate;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class InsurancePayment extends BaseEntity {
    private int id;
    private int insuranceId;
    private String policyNumber;
    private int vehicleId;
    private String registrationNumber;
    private double amount;
    private LocalDate paymentDate;
    private LocalDate dueDate;
    private double lateFee;
    private String paymentMethod;
    private String receiptNumber;
    private String status;

    // JavaFX Properties
    private final StringProperty policyNumberProperty = new SimpleStringProperty();
    private final StringProperty registrationNumberProperty = new SimpleStringProperty();
    private final DoubleProperty amountProperty = new SimpleDoubleProperty();
    private final ObjectProperty<LocalDate> paymentDateProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> dueDateProperty = new SimpleObjectProperty<>();
    private final StringProperty statusProperty = new SimpleStringProperty();

    public InsurancePayment() {
        super();
        this.status = "PENDING";
        this.lateFee = 0;
    }

    public InsurancePayment(int insuranceId, double amount, LocalDate dueDate, String paymentMethod) {
        this();
        this.insuranceId = insuranceId;
        this.amount = amount;
        this.dueDate = dueDate;
        this.paymentMethod = paymentMethod;
    }

    public int getInsuranceId() { return insuranceId; }
    public void setInsuranceId(int insuranceId) { this.insuranceId = insuranceId; }

    public String getPolicyNumber() { return policyNumber; }
    public void setPolicyNumber(String policyNumber) {
        this.policyNumber = policyNumber;
        policyNumberProperty.set(policyNumber);
    }
    public StringProperty policyNumberProperty() { return policyNumberProperty; }

    public int getVehicleId() { return vehicleId; }
    public void setVehicleId(int vehicleId) { this.vehicleId = vehicleId; }

    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
        registrationNumberProperty.set(registrationNumber);
    }
    public StringProperty registrationNumberProperty() { return registrationNumberProperty; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) {
        this.amount = amount;
        amountProperty.set(amount);
    }
    public DoubleProperty amountProperty() { return amountProperty; }

    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
        paymentDateProperty.set(paymentDate);
    }
    public ObjectProperty<LocalDate> paymentDateProperty() { return paymentDateProperty; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
        dueDateProperty.set(dueDate);
    }
    public ObjectProperty<LocalDate> dueDateProperty() { return dueDateProperty; }

    public double getLateFee() { return lateFee; }
    public void setLateFee(double lateFee) { this.lateFee = lateFee; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getReceiptNumber() { return receiptNumber; }
    public void setReceiptNumber(String receiptNumber) { this.receiptNumber = receiptNumber; }

    public String getStatus() { return status; }
    public void setStatus(String status) {
        this.status = status;
        statusProperty.set(status);
    }
    public StringProperty statusProperty() { return statusProperty; }

    public boolean isCompleted() { return "COMPLETED".equals(status); }
    public boolean isPending() { return "PENDING".equals(status); }
    public boolean isOverdue() {
        return dueDate != null && dueDate.isBefore(LocalDate.now()) && !"COMPLETED".equals(status);
    }
    public double getTotalAmount() { return amount + lateFee; }

    @Override
    public int getId() { return id; }
    @Override
    public void setId(int id) { this.id = id; }

    @Override
    public String toString() {
        return "Insurance Payment - " + utils.CurrencyUtil.format(amount) + " - Due: " + dueDate + " - " + status;
    }
}