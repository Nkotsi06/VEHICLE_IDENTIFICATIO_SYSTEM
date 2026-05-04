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
 * Warrant model representing arrest warrants issued for vehicle-related violations.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class Warrant extends BaseEntity {

    // Core fields
    private int id;
    private int violationId;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String judgeName;
    private String status;
    private int vehicleId;
    private String registrationNumber;
    private double fineAmount;

    // Status constants
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_EXECUTED = "EXECUTED";
    public static final String STATUS_EXPIRED = "EXPIRED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    // Default expiry period (days)
    public static final int DEFAULT_EXPIRY_DAYS = 90;

    // JavaFX Properties for TableView binding
    private final IntegerProperty violationIdProperty = new SimpleIntegerProperty();
    private final StringProperty registrationNumberProperty = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> issueDateProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> expiryDateProperty = new SimpleObjectProperty<>();
    private final StringProperty judgeNameProperty = new SimpleStringProperty();
    private final StringProperty statusProperty = new SimpleStringProperty();
    private final DoubleProperty fineAmountProperty = new SimpleDoubleProperty();
    private final StringProperty statusDisplayProperty = new SimpleStringProperty();
    private final StringProperty statusColorProperty = new SimpleStringProperty();
    private final StringProperty formattedIssueDateProperty = new SimpleStringProperty();
    private final StringProperty formattedExpiryDateProperty = new SimpleStringProperty();
    private final StringProperty daysRemainingProperty = new SimpleStringProperty();

    /**
     * Default constructor.
     */
    public Warrant() {
        super();
        this.status = STATUS_ACTIVE;

        statusProperty.set(STATUS_ACTIVE);
        updateDerivedProperties();

        statusProperty.addListener((obs, oldVal, newVal) -> updateDerivedProperties());
        issueDateProperty.addListener((obs, oldVal, newVal) -> updateDerivedProperties());
        expiryDateProperty.addListener((obs, oldVal, newVal) -> updateDerivedProperties());
    }

    /**
     * Constructor for creating a new warrant.
     *
     * @param violationId the violation ID
     * @param issueDate   the issue date
     * @param expiryDate  the expiry date
     * @param judgeName   the judge name
     */
    public Warrant(int violationId, LocalDate issueDate, LocalDate expiryDate, String judgeName) {
        this();
        this.violationId = violationId;
        this.issueDate = issueDate;
        this.expiryDate = expiryDate;
        this.judgeName = judgeName;

        violationIdProperty.set(violationId);
        issueDateProperty.set(issueDate);
        expiryDateProperty.set(expiryDate);
        judgeNameProperty.set(judgeName);
    }

    // ============================================
    // PRIVATE UPDATE METHODS
    // ============================================

    private void updateDerivedProperties() {
        // Update status display
        switch (status) {
            case STATUS_ACTIVE:
                statusDisplayProperty.set("Active");
                statusColorProperty.set("#F44336");
                break;
            case STATUS_EXECUTED:
                statusDisplayProperty.set("Executed");
                statusColorProperty.set("#4CAF50");
                break;
            case STATUS_EXPIRED:
                statusDisplayProperty.set("Expired");
                statusColorProperty.set("#9E9E9E");
                break;
            case STATUS_CANCELLED:
                statusDisplayProperty.set("Cancelled");
                statusColorProperty.set("#9E9E9E");
                break;
            default:
                statusDisplayProperty.set(status);
                statusColorProperty.set("#9E9E9E");
        }

        // Update formatted dates
        if (issueDate != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            formattedIssueDateProperty.set(issueDate.format(formatter));
        } else {
            formattedIssueDateProperty.set("");
        }

        if (expiryDate != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            formattedExpiryDateProperty.set(expiryDate.format(formatter));

            int daysRemaining = (int) java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
            if (daysRemaining < 0) {
                daysRemainingProperty.set("Expired");
            } else if (daysRemaining == 0) {
                daysRemainingProperty.set("Today");
            } else {
                daysRemainingProperty.set(daysRemaining + " days remaining");
            }
        } else {
            formattedExpiryDateProperty.set("");
            daysRemainingProperty.set("");
        }
    }

    // ============================================
    // GETTERS AND SETTERS WITH PROPERTY UPDATES
    // ============================================

    public int getViolationId() { return violationId; }
    public void setViolationId(int violationId) {
        this.violationId = violationId;
        violationIdProperty.set(violationId);
    }
    public IntegerProperty violationIdProperty() { return violationIdProperty; }

    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
        issueDateProperty.set(issueDate);
    }
    public ObjectProperty<LocalDate> issueDateProperty() { return issueDateProperty; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
        expiryDateProperty.set(expiryDate);
    }
    public ObjectProperty<LocalDate> expiryDateProperty() { return expiryDateProperty; }

    public String getJudgeName() { return judgeName; }
    public void setJudgeName(String judgeName) {
        this.judgeName = judgeName;
        judgeNameProperty.set(judgeName);
    }
    public StringProperty judgeNameProperty() { return judgeNameProperty; }

    public String getStatus() { return status; }
    public void setStatus(String status) {
        this.status = status;
        statusProperty.set(status);
    }
    public StringProperty statusProperty() { return statusProperty; }

    public int getVehicleId() { return vehicleId; }
    public void setVehicleId(int vehicleId) { this.vehicleId = vehicleId; }

    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
        registrationNumberProperty.set(registrationNumber);
    }
    public StringProperty registrationNumberProperty() { return registrationNumberProperty; }

    public double getFineAmount() { return fineAmount; }
    public void setFineAmount(double fineAmount) {
        this.fineAmount = fineAmount;
        fineAmountProperty.set(fineAmount);
    }
    public DoubleProperty fineAmountProperty() { return fineAmountProperty; }

    public String getStatusDisplay() { return statusDisplayProperty.get(); }
    public StringProperty statusDisplayProperty() { return statusDisplayProperty; }

    public String getStatusColor() { return statusColorProperty.get(); }
    public StringProperty statusColorProperty() { return statusColorProperty; }

    public String getFormattedIssueDate() { return formattedIssueDateProperty.get(); }
    public StringProperty formattedIssueDateProperty() { return formattedIssueDateProperty; }

    public String getFormattedExpiryDate() { return formattedExpiryDateProperty.get(); }
    public StringProperty formattedExpiryDateProperty() { return formattedExpiryDateProperty; }

    public String getDaysRemaining() { return daysRemainingProperty.get(); }
    public StringProperty daysRemainingProperty() { return daysRemainingProperty; }

    // ============================================
    // BUSINESS LOGIC METHODS
    // ============================================

    public boolean isActive() {
        return STATUS_ACTIVE.equals(status) && expiryDate != null && expiryDate.isAfter(LocalDate.now());
    }

    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }

    public boolean isExecuted() {
        return STATUS_EXECUTED.equals(status);
    }

    public String getFormattedFineAmount() {
        return String.format("M%,.2f", fineAmount);
    }

    public void execute() {
        this.status = STATUS_EXECUTED;
        statusProperty.set(STATUS_EXECUTED);
    }

    public void cancel() {
        this.status = STATUS_CANCELLED;
        statusProperty.set(STATUS_CANCELLED);
    }

    public void extendWarrant(int days) {
        if (expiryDate != null && days > 0) {
            this.expiryDate = expiryDate.plusDays(days);
            expiryDateProperty.set(this.expiryDate);
        }
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
        return "Warrant for " + registrationNumber + " - " + getStatusDisplay();
    }

    /**
     * Creates a copy of this warrant.
     *
     * @return a new Warrant instance
     */
    public Warrant copy() {
        Warrant copy = new Warrant();
        copy.setId(this.id);
        copy.setViolationId(this.violationId);
        copy.setIssueDate(this.issueDate);
        copy.setExpiryDate(this.expiryDate);
        copy.setJudgeName(this.judgeName);
        copy.setStatus(this.status);
        copy.setVehicleId(this.vehicleId);
        copy.setRegistrationNumber(this.registrationNumber);
        copy.setFineAmount(this.fineAmount);
        copy.setCreatedAt(this.getCreatedAt());
        copy.setUpdatedAt(this.getUpdatedAt());
        return copy;
    }
}