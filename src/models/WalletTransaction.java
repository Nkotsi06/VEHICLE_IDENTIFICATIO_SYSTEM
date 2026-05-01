package models;

import java.time.LocalDateTime;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class WalletTransaction extends BaseEntity {
    private int id;
    private int walletId;
    private double amount;
    private String transactionType;
    private String referenceId;
    private String description;
    private String status;
    private LocalDateTime transactionDate;

    // JavaFX Properties for TableView binding
    private final IntegerProperty walletIdProperty = new SimpleIntegerProperty();
    private final DoubleProperty amountProperty = new SimpleDoubleProperty();
    private final StringProperty transactionTypeProperty = new SimpleStringProperty();
    private final StringProperty referenceIdProperty = new SimpleStringProperty();
    private final StringProperty descriptionProperty = new SimpleStringProperty();
    private final StringProperty statusProperty = new SimpleStringProperty();
    private final ObjectProperty<LocalDateTime> transactionDateProperty = new SimpleObjectProperty<>();

    public WalletTransaction() {
        super();
        this.status = "PENDING";
        this.transactionDate = LocalDateTime.now();
    }

    public WalletTransaction(int walletId, double amount, String transactionType, String referenceId, String description) {
        this();
        this.walletId = walletId;
        this.amount = amount;
        this.transactionType = transactionType;
        this.referenceId = referenceId;
        this.description = description;

        // Update properties
        walletIdProperty.set(walletId);
        amountProperty.set(amount);
        transactionTypeProperty.set(transactionType);
        referenceIdProperty.set(referenceId);
        descriptionProperty.set(description);
        statusProperty.set("PENDING");
        transactionDateProperty.set(transactionDate);
    }

    public int getWalletId() {
        return walletId;
    }

    public void setWalletId(int walletId) {
        this.walletId = walletId;
        walletIdProperty.set(walletId);
    }

    public IntegerProperty walletIdProperty() {
        return walletIdProperty;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
        amountProperty.set(amount);
    }

    public DoubleProperty amountProperty() {
        return amountProperty;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
        transactionTypeProperty.set(transactionType);
    }

    public StringProperty transactionTypeProperty() {
        return transactionTypeProperty;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
        referenceIdProperty.set(referenceId);
    }

    public StringProperty referenceIdProperty() {
        return referenceIdProperty;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        descriptionProperty.set(description);
    }

    public StringProperty descriptionProperty() {
        return descriptionProperty;
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

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
        transactionDateProperty.set(transactionDate);
    }

    public ObjectProperty<LocalDateTime> transactionDateProperty() {
        return transactionDateProperty;
    }

    public boolean isCompleted() {
        return "COMPLETED".equals(status);
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
        return transactionType + " - " + utils.CurrencyUtil.format(amount) + " - " + transactionDate;
    }
}