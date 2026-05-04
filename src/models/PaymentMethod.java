package models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * PaymentMethod model representing saved payment methods for digital wallet.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class PaymentMethod extends BaseEntity {

    // Core fields
    private int id;
    private int walletId;
    private String cardLastFour;
    private String cardType;
    private int expiryMonth;
    private int expiryYear;
    private boolean isDefault;
    private String cardHolderName;

    // Card type constants
    public static final String CARD_VISA = "VISA";
    public static final String CARD_MASTERCARD = "MASTERCARD";
    public static final String CARD_AMEX = "AMEX";
    public static final String CARD_DISCOVER = "DISCOVER";

    // JavaFX Properties
    private final IntegerProperty walletIdProperty = new SimpleIntegerProperty();
    private final StringProperty cardLastFourProperty = new SimpleStringProperty();
    private final StringProperty cardTypeProperty = new SimpleStringProperty();
    private final IntegerProperty expiryMonthProperty = new SimpleIntegerProperty();
    private final IntegerProperty expiryYearProperty = new SimpleIntegerProperty();
    private final BooleanProperty defaultProperty = new SimpleBooleanProperty();
    private final StringProperty cardHolderNameProperty = new SimpleStringProperty();
    private final StringProperty maskedCardNumberProperty = new SimpleStringProperty();
    private final StringProperty expiryDisplayProperty = new SimpleStringProperty();
    private final StringProperty cardTypeDisplayProperty = new SimpleStringProperty();

    /**
     * Default constructor.
     */
    public PaymentMethod() {
        super();
        this.isDefault = false;
        defaultProperty.set(false);
    }

    /**
     * Constructor for creating a new payment method.
     *
     * @param walletId      the wallet ID
     * @param cardLastFour  the last 4 digits of the card
     * @param cardType      the card type (VISA, MASTERCARD, etc.)
     * @param expiryMonth   the expiry month (1-12)
     * @param expiryYear    the expiry year
     */
    public PaymentMethod(int walletId, String cardLastFour, String cardType, int expiryMonth, int expiryYear) {
        this();
        this.walletId = walletId;
        this.cardLastFour = cardLastFour;
        this.cardType = cardType;
        this.expiryMonth = expiryMonth;
        this.expiryYear = expiryYear;

        walletIdProperty.set(walletId);
        cardLastFourProperty.set(cardLastFour);
        cardTypeProperty.set(cardType);
        expiryMonthProperty.set(expiryMonth);
        expiryYearProperty.set(expiryYear);
        updateDerivedProperties();
    }

    // ============================================
    // PRIVATE UPDATE METHODS
    // ============================================

    private void updateDerivedProperties() {
        // Update masked card number
        if (cardLastFour != null && !cardLastFour.isEmpty()) {
            maskedCardNumberProperty.set("**** **** **** " + cardLastFour);
        } else {
            maskedCardNumberProperty.set("****");
        }

        // Update expiry display
        expiryDisplayProperty.set(String.format("%02d/%d", expiryMonth, expiryYear));

        // Update card type display
        switch (cardType) {
            case CARD_VISA:
                cardTypeDisplayProperty.set("Visa");
                break;
            case CARD_MASTERCARD:
                cardTypeDisplayProperty.set("Mastercard");
                break;
            case CARD_AMEX:
                cardTypeDisplayProperty.set("American Express");
                break;
            case CARD_DISCOVER:
                cardTypeDisplayProperty.set("Discover");
                break;
            default:
                cardTypeDisplayProperty.set(cardType);
        }
    }

    // ============================================
    // GETTERS AND SETTERS WITH PROPERTY UPDATES
    // ============================================

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

    public String getCardLastFour() {
        return cardLastFour;
    }

    public void setCardLastFour(String cardLastFour) {
        this.cardLastFour = cardLastFour;
        cardLastFourProperty.set(cardLastFour);
        updateDerivedProperties();
    }

    public StringProperty cardLastFourProperty() {
        return cardLastFourProperty;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
        cardTypeProperty.set(cardType);
        updateDerivedProperties();
    }

    public StringProperty cardTypeProperty() {
        return cardTypeProperty;
    }

    public int getExpiryMonth() {
        return expiryMonth;
    }

    public void setExpiryMonth(int expiryMonth) {
        this.expiryMonth = expiryMonth;
        expiryMonthProperty.set(expiryMonth);
        updateDerivedProperties();
    }

    public IntegerProperty expiryMonthProperty() {
        return expiryMonthProperty;
    }

    public int getExpiryYear() {
        return expiryYear;
    }

    public void setExpiryYear(int expiryYear) {
        this.expiryYear = expiryYear;
        expiryYearProperty.set(expiryYear);
        updateDerivedProperties();
    }

    public IntegerProperty expiryYearProperty() {
        return expiryYearProperty;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
        defaultProperty.set(isDefault);
    }

    public BooleanProperty defaultProperty() {
        return defaultProperty;
    }

    public String getCardHolderName() {
        return cardHolderName;
    }

    public void setCardHolderName(String cardHolderName) {
        this.cardHolderName = cardHolderName;
        cardHolderNameProperty.set(cardHolderName);
    }

    public StringProperty cardHolderNameProperty() {
        return cardHolderNameProperty;
    }

    public String getMaskedCardNumber() {
        return maskedCardNumberProperty.get();
    }

    public StringProperty maskedCardNumberProperty() {
        return maskedCardNumberProperty;
    }

    public String getExpiryDisplay() {
        return expiryDisplayProperty.get();
    }

    public StringProperty expiryDisplayProperty() {
        return expiryDisplayProperty;
    }

    public String getCardTypeDisplay() {
        return cardTypeDisplayProperty.get();
    }

    public StringProperty cardTypeDisplayProperty() {
        return cardTypeDisplayProperty;
    }

    // ============================================
    // BUSINESS LOGIC METHODS
    // ============================================

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

    public String getExpiryStatus() {
        if (isExpired()) return "EXPIRED";
        if (expiryYear == LocalDateTime.now().getYear() && expiryMonth <= LocalDateTime.now().getMonthValue() + 3) {
            return "EXPIRING_SOON";
        }
        return "VALID";
    }

    public String getExpiryColor() {
        String status = getExpiryStatus();
        switch (status) {
            case "EXPIRED": return "#F44336";
            case "EXPIRING_SOON": return "#FF9800";
            default: return "#4CAF50";
        }
    }

    public String getCardIcon() {
        switch (cardType) {
            case CARD_VISA: return "💳 Visa";
            case CARD_MASTERCARD: return "💳 Mastercard";
            case CARD_AMEX: return "💳 Amex";
            default: return "💳 Card";
        }
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
        return getCardTypeDisplay() + " - " + getMaskedCardNumber() + (isDefault ? " (Default)" : "");
    }

    /**
     * Creates a copy of this payment method.
     *
     * @return a new PaymentMethod instance
     */
    public PaymentMethod copy() {
        PaymentMethod copy = new PaymentMethod();
        copy.setId(this.id);
        copy.setWalletId(this.walletId);
        copy.setCardLastFour(this.cardLastFour);
        copy.setCardType(this.cardType);
        copy.setExpiryMonth(this.expiryMonth);
        copy.setExpiryYear(this.expiryYear);
        copy.setDefault(this.isDefault);
        copy.setCardHolderName(this.cardHolderName);
        copy.setCreatedAt(this.getCreatedAt());
        copy.setUpdatedAt(this.getUpdatedAt());
        return copy;
    }
}