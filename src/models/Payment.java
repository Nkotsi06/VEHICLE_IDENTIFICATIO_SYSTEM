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
 * Payment model representing fine payments for violations.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class Payment extends BaseEntity {

    // Core fields
    private int id;
    private int violationId;
    private double amount;
    private LocalDate paymentDate;
    private String paymentMethod;
    private String transactionId;
    private String receiptNumber;
    private int vehicleId;
    private String registrationNumber;
    private String status;

    // Payment method constants
    public static final String METHOD_CASH = "CASH";
    public static final String METHOD_CARD = "CARD";
    public static final String METHOD_BANK_TRANSFER = "BANK_TRANSFER";
    public static final String METHOD_DIGITAL_WALLET = "DIGITAL_WALLET";
    public static final String METHOD_MOBILE_MONEY = "MOBILE_MONEY";

    // Status constants
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_FAILED = "FAILED";
    public static final String STATUS_REFUNDED = "REFUNDED";

    // JavaFX Properties
    private final IntegerProperty violationIdProperty = new SimpleIntegerProperty();
    private final DoubleProperty amountProperty = new SimpleDoubleProperty();
    private final ObjectProperty<LocalDate> paymentDateProperty = new SimpleObjectProperty<>();
    private final StringProperty paymentMethodProperty = new SimpleStringProperty();
    private final StringProperty transactionIdProperty = new SimpleStringProperty();
    private final StringProperty receiptNumberProperty = new SimpleStringProperty();
    private final IntegerProperty vehicleIdProperty = new SimpleIntegerProperty();
    private final StringProperty registrationNumberProperty = new SimpleStringProperty();
    private final StringProperty statusProperty = new SimpleStringProperty();
    private final StringProperty formattedAmountProperty = new SimpleStringProperty();
    private final StringProperty paymentMethodDisplayProperty = new SimpleStringProperty();

    /**
     * Default constructor.
     */
    public Payment() {
        super();
        this.paymentDate = LocalDate.now();
        this.status = STATUS_COMPLETED;

        paymentDateProperty.set(paymentDate);
        statusProperty.set(STATUS_COMPLETED);
    }

    /**
     * Constructor for creating a new payment.
     *
     * @param violationId   the violation ID
     * @param amount        the payment amount
     * @param paymentDate   the payment date
     * @param paymentMethod the payment method
     * @param receiptNumber the receipt number
     */
    public Payment(int violationId, double amount, LocalDate paymentDate,
                   String paymentMethod, String receiptNumber) {
        this();
        this.violationId = violationId;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.paymentMethod = paymentMethod;
        this.receiptNumber = receiptNumber;

        violationIdProperty.set(violationId);
        amountProperty.set(amount);
        paymentDateProperty.set(paymentDate);
        paymentMethodProperty.set(paymentMethod);
        receiptNumberProperty.set(receiptNumber);
        updateDerivedProperties();
    }

    // ============================================
    // PRIVATE UPDATE METHODS
    // ============================================

    private void updateDerivedProperties() {
        formattedAmountProperty.set(String.format("M%,.2f", amount));

        switch (paymentMethod) {
            case METHOD_CASH:
                paymentMethodDisplayProperty.set("Cash");
                break;
            case METHOD_CARD:
                paymentMethodDisplayProperty.set("Card");
                break;
            case METHOD_BANK_TRANSFER:
                paymentMethodDisplayProperty.set("Bank Transfer");
                break;
            case METHOD_DIGITAL_WALLET:
                paymentMethodDisplayProperty.set("Digital Wallet");
                break;
            case METHOD_MOBILE_MONEY:
                paymentMethodDisplayProperty.set("Mobile Money");
                break;
            default:
                paymentMethodDisplayProperty.set(paymentMethod);
        }
    }

    // ============================================
    // GETTERS AND SETTERS WITH PROPERTY UPDATES
    // ============================================

    public int getViolationId() {
        return violationId;
    }

    public void setViolationId(int violationId) {
        this.violationId = violationId;
        violationIdProperty.set(violationId);
    }

    public IntegerProperty violationIdProperty() {
        return violationIdProperty;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
        amountProperty.set(amount);
        formattedAmountProperty.set(String.format("M%,.2f", amount));
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

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
        paymentMethodProperty.set(paymentMethod);
        updateDerivedProperties();
    }

    public StringProperty paymentMethodProperty() {
        return paymentMethodProperty;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
        transactionIdProperty.set(transactionId);
    }

    public StringProperty transactionIdProperty() {
        return transactionIdProperty;
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

    public String getFormattedAmount() {
        return formattedAmountProperty.get();
    }

    public StringProperty formattedAmountProperty() {
        return formattedAmountProperty;
    }

    public String getPaymentMethodDisplay() {
        return paymentMethodDisplayProperty.get();
    }

    public StringProperty paymentMethodDisplayProperty() {
        return paymentMethodDisplayProperty;
    }

    // ============================================
    // BUSINESS LOGIC METHODS
    // ============================================

    public String getFormattedPaymentDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return paymentDate != null ? paymentDate.format(formatter) : "";
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

    public void markAsCompleted() {
        this.status = STATUS_COMPLETED;
        statusProperty.set(STATUS_COMPLETED);
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
        return "Payment #" + id + " - " + getFormattedAmount() + " - " + getFormattedPaymentDate();
    }

    /**
     * Creates a copy of this payment.
     *
     * @return a new Payment instance
     */
    public Payment copy() {
        Payment copy = new Payment();
        copy.setId(this.id);
        copy.setViolationId(this.violationId);
        copy.setAmount(this.amount);
        copy.setPaymentDate(this.paymentDate);
        copy.setPaymentMethod(this.paymentMethod);
        copy.setTransactionId(this.transactionId);
        copy.setReceiptNumber(this.receiptNumber);
        copy.setVehicleId(this.vehicleId);
        copy.setRegistrationNumber(this.registrationNumber);
        copy.setStatus(this.status);
        copy.setCreatedAt(this.getCreatedAt());
        copy.setUpdatedAt(this.getUpdatedAt());
        return copy;
    }
}