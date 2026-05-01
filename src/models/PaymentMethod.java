package models;

import java.time.LocalDateTime;

public class PaymentMethod extends BaseEntity {
    private int walletId;
    private String cardLastFour;
    private String cardType;
    private int expiryMonth;
    private int expiryYear;
    private boolean isDefault;

    public PaymentMethod() {
        super();
        this.isDefault = false;
    }

    public PaymentMethod(int walletId, String cardLastFour, String cardType, int expiryMonth, int expiryYear) {
        this();
        this.walletId = walletId;
        this.cardLastFour = cardLastFour;
        this.cardType = cardType;
        this.expiryMonth = expiryMonth;
        this.expiryYear = expiryYear;
    }

    public int getWalletId() {
        return walletId;
    }

    public void setWalletId(int walletId) {
        this.walletId = walletId;
    }

    public String getCardLastFour() {
        return cardLastFour;
    }

    public void setCardLastFour(String cardLastFour) {
        this.cardLastFour = cardLastFour;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public int getExpiryMonth() {
        return expiryMonth;
    }

    public void setExpiryMonth(int expiryMonth) {
        this.expiryMonth = expiryMonth;
    }

    public int getExpiryYear() {
        return expiryYear;
    }

    public void setExpiryYear(int expiryYear) {
        this.expiryYear = expiryYear;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public String getMaskedCardNumber() {
        if (cardLastFour == null || cardLastFour.isEmpty()) {
            return "****";
        }
        return "**** **** **** " + cardLastFour;
    }

    public boolean isExpired() {
        if (expiryYear == 0 || expiryMonth == 0) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();

        if (expiryYear < currentYear) {
            return true;
        }
        if (expiryYear == currentYear && expiryMonth < currentMonth) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return getCardType() + " - " + getMaskedCardNumber() + (isDefault ? " (Default)" : "");
    }
}