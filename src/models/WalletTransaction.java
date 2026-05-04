package models;

import java.time.LocalDateTime;
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
 * WalletTransaction model representing transactions in a digital wallet.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class WalletTransaction extends BaseEntity {

    // Core fields
    private int id;
    private int walletId;
    private double amount;
    private String transactionType;
    private String referenceId;
    private String description;
    private String status;
    private LocalDateTime transactionDate;

    // Transaction type constants
    public static final String TYPE_DEPOSIT = "DEPOSIT";
    public static final String TYPE_WITHDRAWAL = "WITHDRAWAL";
    public static final String TYPE_PAYMENT = "PAYMENT";
    public static final String TYPE_REFUND = "REFUND";
    public static final String TYPE_TRANSFER = "TRANSFER";

    // Status constants
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_FAILED = "FAILED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    // JavaFX Properties for TableView binding
    private final IntegerProperty walletIdProperty = new SimpleIntegerProperty();
    private final DoubleProperty amountProperty = new SimpleDoubleProperty();
    private final StringProperty transactionTypeProperty = new SimpleStringProperty();
    private final StringProperty referenceIdProperty = new SimpleStringProperty();
    private final StringProperty descriptionProperty = new SimpleStringProperty();
    private final StringProperty statusProperty = new SimpleStringProperty();
    private final ObjectProperty<LocalDateTime> transactionDateProperty = new SimpleObjectProperty<>();
    private final StringProperty typeDisplayProperty = new SimpleStringProperty();
    private final StringProperty statusDisplayProperty = new SimpleStringProperty();
    private final StringProperty statusColorProperty = new SimpleStringProperty();
    private final StringProperty formattedAmountProperty = new SimpleStringProperty();
    private final StringProperty formattedDateProperty = new SimpleStringProperty();

    /**
     * Default constructor - initializes with PENDING status and current date.
     */
    public WalletTransaction() {
        super();
        this.status = STATUS_PENDING;
        this.transactionDate = LocalDateTime.now();

        statusProperty.set(STATUS_PENDING);
        transactionDateProperty.set(transactionDate);
        updateDerivedProperties();

        transactionTypeProperty.addListener((obs, oldVal, newVal) -> updateDerivedProperties());
        statusProperty.addListener((obs, oldVal, newVal) -> updateDerivedProperties());
        amountProperty.addListener((obs, oldVal, newVal) -> updateDerivedProperties());
        transactionDateProperty.addListener((obs, oldVal, newVal) -> updateDerivedProperties());
    }

    /**
     * Constructor for creating a new transaction.
     *
     * @param walletId        the wallet ID
     * @param amount          the transaction amount
     * @param transactionType the transaction type
     * @param referenceId     the reference ID
     * @param description     the description
     */
    public WalletTransaction(int walletId, double amount, String transactionType, String referenceId, String description) {
        this();
        this.walletId = walletId;
        this.amount = amount;
        this.transactionType = transactionType;
        this.referenceId = referenceId;
        this.description = description;

        walletIdProperty.set(walletId);
        amountProperty.set(amount);
        transactionTypeProperty.set(transactionType);
        referenceIdProperty.set(referenceId);
        descriptionProperty.set(description);
    }

    // ============================================
    // PRIVATE UPDATE METHODS
    // ============================================

    private void updateDerivedProperties() {
        // Update type display
        switch (transactionType) {
            case TYPE_DEPOSIT:
                typeDisplayProperty.set("Deposit");
                break;
            case TYPE_WITHDRAWAL:
                typeDisplayProperty.set("Withdrawal");
                break;
            case TYPE_PAYMENT:
                typeDisplayProperty.set("Payment");
                break;
            case TYPE_REFUND:
                typeDisplayProperty.set("Refund");
                break;
            case TYPE_TRANSFER:
                typeDisplayProperty.set("Transfer");
                break;
            default:
                typeDisplayProperty.set(transactionType);
        }

        // Update status display
        switch (status) {
            case STATUS_PENDING:
                statusDisplayProperty.set("Pending");
                statusColorProperty.set("#FFC107");
                break;
            case STATUS_COMPLETED:
                statusDisplayProperty.set("Completed");
                statusColorProperty.set("#4CAF50");
                break;
            case STATUS_FAILED:
                statusDisplayProperty.set("Failed");
                statusColorProperty.set("#F44336");
                break;
            case STATUS_CANCELLED:
                statusDisplayProperty.set("Cancelled");
                statusColorProperty.set("#9E9E9E");
                break;
            default:
                statusDisplayProperty.set(status);
                statusColorProperty.set("#9E9E9E");
        }

        // Update formatted amount
        String symbol = TYPE_DEPOSIT.equals(transactionType) || TYPE_REFUND.equals(transactionType) ? "+" : "-";
        formattedAmountProperty.set(String.format("%sM%,.2f", symbol, amount));

        // Update formatted date
        if (transactionDate != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            formattedDateProperty.set(transactionDate.format(formatter));
        } else {
            formattedDateProperty.set("");
        }
    }

    // ============================================
    // GETTERS AND SETTERS WITH PROPERTY UPDATES
    // ============================================

    public int getWalletId() { return walletId; }
    public void setWalletId(int walletId) {
        this.walletId = walletId;
        walletIdProperty.set(walletId);
    }
    public IntegerProperty walletIdProperty() { return walletIdProperty; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) {
        this.amount = amount;
        amountProperty.set(amount);
    }
    public DoubleProperty amountProperty() { return amountProperty; }

    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
        transactionTypeProperty.set(transactionType);
    }
    public StringProperty transactionTypeProperty() { return transactionTypeProperty; }

    public String getReferenceId() { return referenceId; }
    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
        referenceIdProperty.set(referenceId);
    }
    public StringProperty referenceIdProperty() { return referenceIdProperty; }

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

    public LocalDateTime getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
        transactionDateProperty.set(transactionDate);
    }
    public ObjectProperty<LocalDateTime> transactionDateProperty() { return transactionDateProperty; }

    public String getTypeDisplay() { return typeDisplayProperty.get(); }
    public StringProperty typeDisplayProperty() { return typeDisplayProperty; }

    public String getStatusDisplay() { return statusDisplayProperty.get(); }
    public StringProperty statusDisplayProperty() { return statusDisplayProperty; }

    public String getStatusColor() { return statusColorProperty.get(); }
    public StringProperty statusColorProperty() { return statusColorProperty; }

    public String getFormattedAmount() { return formattedAmountProperty.get(); }
    public StringProperty formattedAmountProperty() { return formattedAmountProperty; }

    public String getFormattedDate() { return formattedDateProperty.get(); }
    public StringProperty formattedDateProperty() { return formattedDateProperty; }

    // ============================================
    // BUSINESS LOGIC METHODS
    // ============================================

    public boolean isCompleted() {
        return STATUS_COMPLETED.equals(status);
    }

    public boolean isPending() {
        return STATUS_PENDING.equals(status);
    }

    public boolean isDeposit() {
        return TYPE_DEPOSIT.equals(transactionType);
    }

    public boolean isPayment() {
        return TYPE_PAYMENT.equals(transactionType);
    }

    public void complete() {
        this.status = STATUS_COMPLETED;
        statusProperty.set(STATUS_COMPLETED);
    }

    public void fail() {
        this.status = STATUS_FAILED;
        statusProperty.set(STATUS_FAILED);
    }

    public void cancel() {
        this.status = STATUS_CANCELLED;
        statusProperty.set(STATUS_CANCELLED);
    }

    // ============================================
    // OVERRIDE METHODS
    // ============================================

    @Override
    public int getId() { return id; }
    @Override
    public void setId(int id) { this.id = id; }

    @Override
    public String toString() {
        return getTypeDisplay() + " - " + getFormattedAmount() + " - " + getFormattedDate();
    }

    /**
     * Creates a copy of this transaction.
     *
     * @return a new WalletTransaction instance
     */
    public WalletTransaction copy() {
        WalletTransaction copy = new WalletTransaction();
        copy.setId(this.id);
        copy.setWalletId(this.walletId);
        copy.setAmount(this.amount);
        copy.setTransactionType(this.transactionType);
        copy.setReferenceId(this.referenceId);
        copy.setDescription(this.description);
        copy.setStatus(this.status);
        copy.setTransactionDate(this.transactionDate);
        copy.setCreatedAt(this.getCreatedAt());
        copy.setUpdatedAt(this.getUpdatedAt());
        return copy;
    }
}