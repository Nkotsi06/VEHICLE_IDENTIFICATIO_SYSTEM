package models;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * InsurancePayment model representing premium payments for insurance policies.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class InsurancePayment extends BaseEntity {

    // Core fields
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

    // Status constants
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_FAILED = "FAILED";
    public static final String STATUS_REFUNDED = "REFUNDED";

    // Payment method constants
    public static final String METHOD_CREDIT_CARD = "CREDIT_CARD";
    public static final String METHOD_DEBIT_CARD = "DEBIT_CARD";
    public static final String METHOD_BANK_TRANSFER = "BANK_TRANSFER";
    public static final String METHOD_DIGITAL_WALLET = "DIGITAL_WALLET";
    public static final String METHOD_CASH = "CASH";

    private static final java.util.Map<String, String> METHOD_DISPLAY = new java.util.HashMap<>();
    static {
        METHOD_DISPLAY.put(METHOD_CREDIT_CARD, "Credit Card");
        METHOD_DISPLAY.put(METHOD_DEBIT_CARD, "Debit Card");
        METHOD_DISPLAY.put(METHOD_BANK_TRANSFER, "Bank Transfer");
        METHOD_DISPLAY.put(METHOD_DIGITAL_WALLET, "Digital Wallet");
        METHOD_DISPLAY.put(METHOD_CASH, "Cash");
    }

    // JavaFX Properties
    private final StringProperty policyNumberProperty = new SimpleStringProperty();
    private final StringProperty registrationNumberProperty = new SimpleStringProperty();
    private final DoubleProperty amountProperty = new SimpleDoubleProperty();
    private final ObjectProperty<LocalDate> paymentDateProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> dueDateProperty = new SimpleObjectProperty<>();
    private final StringProperty statusProperty = new SimpleStringProperty();
    private final StringProperty paymentMethodProperty = new SimpleStringProperty();
    private final StringProperty receiptNumberProperty = new SimpleStringProperty();
    private final StringProperty formattedAmountProperty = new SimpleStringProperty();

    /**
     * Default constructor - initializes with PENDING status.
     */
    public InsurancePayment() {
        super();
        this.status = STATUS_PENDING;
        this.lateFee = 0;

        statusProperty.set(STATUS_PENDING);
    }

    /**
     * Constructor for creating a new payment.
     *
     * @param insuranceId   the insurance ID
     * @param amount        the payment amount
     * @param dueDate       the due date
     * @param paymentMethod the payment method
     */
    public InsurancePayment(int insuranceId, double amount, LocalDate dueDate, String paymentMethod) {
        this();
        this.insuranceId = insuranceId;
        this.amount = amount;
        this.dueDate = dueDate;
        this.paymentMethod = paymentMethod;

        amountProperty.set(amount);
        dueDateProperty.set(dueDate);
        paymentMethodProperty.set(paymentMethod);
        formattedAmountProperty.set(formatAmount(amount));
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

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
        amountProperty.set(amount);
        formattedAmountProperty.set(formatAmount(amount));
    }

    public DoubleProperty amountProperty() {
        return amountProperty;
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

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
        dueDateProperty.set(dueDate);
    }

    public ObjectProperty<LocalDate> dueDateProperty() {
        return dueDateProperty;
    }

    public double getLateFee() {
        return lateFee;
    }

    public void setLateFee(double lateFee) {
        this.lateFee = lateFee;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
        paymentMethodProperty.set(paymentMethod);
    }

    public StringProperty paymentMethodProperty() {
        return paymentMethodProperty;
    }

    public String getReceiptNumber() {
        return receiptNumber;
    }

    public void setReceiptNumber(String receiptNumber) {
        this.receiptNumber = receiptNumber;
        receiptNumberProperty.set(receiptNumber);
    }

    public StringProperty receiptNumberProperty() {
        return receiptNumberProperty;
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

    public StringProperty formattedAmountProperty() {
        return formattedAmountProperty;
    }

    // ============================================
    // BUSINESS LOGIC METHODS
    // ============================================

    private String formatAmount(double amount) {
        return String.format("M%,.2f", amount);
    }

    public String getFormattedAmount() {
        return formattedAmountProperty.get();
    }

    public String getFormattedLateFee() {
        return String.format("M%,.2f", lateFee);
    }

    public double getTotalAmount() {
        return amount + lateFee;
    }

    public String getFormattedTotalAmount() {
        return String.format("M%,.2f", getTotalAmount());
    }

    public boolean isCompleted() {
        return STATUS_COMPLETED.equals(status);
    }

    public boolean isPending() {
        return STATUS_PENDING.equals(status);
    }

    public boolean isFailed() {
        return STATUS_FAILED.equals(status);
    }

    public boolean isRefunded() {
        return STATUS_REFUNDED.equals(status);
    }

    public boolean isOverdue() {
        return dueDate != null && dueDate.isBefore(LocalDate.now()) && !STATUS_COMPLETED.equals(status);
    }

    public String getStatusDisplay() {
        switch (status) {
            case STATUS_PENDING: return "Pending";
            case STATUS_COMPLETED: return "Completed";
            case STATUS_FAILED: return "Failed";
            case STATUS_REFUNDED: return "Refunded";
            default: return status;
        }
    }

    public String getStatusColor() {
        switch (status) {
            case STATUS_PENDING: return "#FFC107";
            case STATUS_COMPLETED: return "#4CAF50";
            case STATUS_FAILED: return "#F44336";
            case STATUS_REFUNDED: return "#9E9E9E";
            default: return "#9E9E9E";
        }
    }

    public String getPaymentMethodDisplay() {
        return METHOD_DISPLAY.getOrDefault(paymentMethod, paymentMethod != null ? paymentMethod : "Unknown");
    }

    public String getFormattedDueDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return dueDate != null ? dueDate.format(formatter) : "";
    }

    public String getFormattedPaymentDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return paymentDate != null ? paymentDate.format(formatter) : "";
    }

    public int getDaysOverdue() {
        if (dueDate == null || isCompleted()) return 0;
        if (dueDate.isBefore(LocalDate.now())) {
            return (int) java.time.temporal.ChronoUnit.DAYS.between(dueDate, LocalDate.now());
        }
        return 0;
    }

    public void calculateLateFee(double dailyRate) {
        int daysOverdue = getDaysOverdue();
        if (daysOverdue > 0) {
            this.lateFee = amount * dailyRate * daysOverdue;
        } else {
            this.lateFee = 0;
        }
    }

    public void markAsCompleted(String receiptNumber) {
        this.status = STATUS_COMPLETED;
        this.paymentDate = LocalDate.now();
        this.receiptNumber = receiptNumber;
        statusProperty.set(STATUS_COMPLETED);
        paymentDateProperty.set(this.paymentDate);
        receiptNumberProperty.set(receiptNumber);
    }

    public void markAsFailed() {
        this.status = STATUS_FAILED;
        statusProperty.set(STATUS_FAILED);
    }

    public void refund() {
        this.status = STATUS_REFUNDED;
        statusProperty.set(STATUS_REFUNDED);
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
        return "Insurance Payment - " + getFormattedAmount() + " - Due: " + getFormattedDueDate() + " - " + getStatusDisplay();
    }

    /**
     * Creates a copy of this payment.
     *
     * @return a new InsurancePayment instance
     */
    public InsurancePayment copy() {
        InsurancePayment copy = new InsurancePayment();
        copy.setId(this.id);
        copy.setInsuranceId(this.insuranceId);
        copy.setPolicyNumber(this.policyNumber);
        copy.setVehicleId(this.vehicleId);
        copy.setRegistrationNumber(this.registrationNumber);
        copy.setAmount(this.amount);
        copy.setPaymentDate(this.paymentDate);
        copy.setDueDate(this.dueDate);
        copy.setLateFee(this.lateFee);
        copy.setPaymentMethod(this.paymentMethod);
        copy.setReceiptNumber(this.receiptNumber);
        copy.setStatus(this.status);
        copy.setCreatedAt(this.getCreatedAt());
        copy.setUpdatedAt(this.getUpdatedAt());
        return copy;
    }
}