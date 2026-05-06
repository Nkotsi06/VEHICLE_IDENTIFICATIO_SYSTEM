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
 * Violation model representing traffic violations issued to vehicles.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class Violation extends BaseEntity {

    // Core fields
    private int id;
    private int vehicleId;
    private String registrationNumber;
    private String make;
    private String model;
    private LocalDate violationDate;
    private String violationType;
    private double fineAmount;
    private String paymentStatus;
    private String location;
    private String officerName;
    private String description;
    private Double latitude;
    private Double longitude;

    // Violation type constants
    public static final String TYPE_SPEEDING = "SPEEDING";
    public static final String TYPE_NO_INSURANCE = "NO_INSURANCE";
    public static final String TYPE_EXPIRED_DOCS = "EXPIRED_DOCS";
    public static final String TYPE_NO_LICENSE = "NO_LICENSE";
    public static final String TYPE_ILLEGAL_PARKING = "ILLEGAL_PARKING";
    public static final String TYPE_RED_LIGHT = "RED_LIGHT";
    public static final String TYPE_DRUNK_DRIVING = "DRUNK_DRIVING";
    public static final String TYPE_HIT_AND_RUN = "HIT_AND_RUN";

    // Payment status constants
    public static final String PAYMENT_UNPAID = "UNPAID";
    public static final String PAYMENT_PAID = "PAID";
    public static final String PAYMENT_PARTIAL = "PARTIAL";
    public static final String PAYMENT_DISPUTED = "DISPUTED";

    // JavaFX Properties for TableView binding
    private final StringProperty registrationNumberProperty = new SimpleStringProperty();
    private final StringProperty violationTypeProperty = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> violationDateProperty = new SimpleObjectProperty<>();
    private final DoubleProperty fineAmountProperty = new SimpleDoubleProperty();
    private final StringProperty paymentStatusProperty = new SimpleStringProperty();
    private final StringProperty officerNameProperty = new SimpleStringProperty();
    private final StringProperty descriptionProperty = new SimpleStringProperty();
    private final StringProperty violationTypeDisplayProperty = new SimpleStringProperty();
    private final StringProperty paymentStatusDisplayProperty = new SimpleStringProperty();
    private final StringProperty paymentColorProperty = new SimpleStringProperty();
    private final StringProperty formattedFineProperty = new SimpleStringProperty();
    private final DoubleProperty latitudeProperty = new SimpleDoubleProperty();
    private final DoubleProperty longitudeProperty = new SimpleDoubleProperty();
    private final StringProperty locationProperty = new SimpleStringProperty();

    /**
     * Default constructor.
     */
    public Violation() {
        super();
        this.paymentStatus = PAYMENT_UNPAID;

        paymentStatusProperty.set(PAYMENT_UNPAID);
        updateDerivedProperties();

        violationTypeProperty.addListener((obs, oldVal, newVal) -> updateDerivedProperties());
        paymentStatusProperty.addListener((obs, oldVal, newVal) -> updateDerivedProperties());
        fineAmountProperty.addListener((obs, oldVal, newVal) -> updateDerivedProperties());
    }

    /**
     * Constructor for creating a new violation.
     *
     * @param vehicleId      the vehicle ID
     * @param violationDate  the violation date
     * @param violationType  the violation type
     * @param fineAmount     the fine amount
     * @param officerName    the officer name
     */
    public Violation(int vehicleId, LocalDate violationDate, String violationType, double fineAmount, String officerName) {
        this();
        this.vehicleId = vehicleId;
        this.violationDate = violationDate;
        this.violationType = violationType;
        this.fineAmount = fineAmount;
        this.officerName = officerName;

        violationDateProperty.set(violationDate);
        violationTypeProperty.set(violationType);
        fineAmountProperty.set(fineAmount);
        officerNameProperty.set(officerName);
    }

    // ============================================
    // PRIVATE UPDATE METHODS
    // ============================================

    private void updateDerivedProperties() {
        // Update violation type display
        switch (violationType) {
            case TYPE_SPEEDING:
                violationTypeDisplayProperty.set("Speeding");
                break;
            case TYPE_NO_INSURANCE:
                violationTypeDisplayProperty.set("No Insurance");
                break;
            case TYPE_EXPIRED_DOCS:
                violationTypeDisplayProperty.set("Expired Documents");
                break;
            case TYPE_NO_LICENSE:
                violationTypeDisplayProperty.set("No License");
                break;
            case TYPE_ILLEGAL_PARKING:
                violationTypeDisplayProperty.set("Illegal Parking");
                break;
            case TYPE_RED_LIGHT:
                violationTypeDisplayProperty.set("Ran Red Light");
                break;
            case TYPE_DRUNK_DRIVING:
                violationTypeDisplayProperty.set("Drunk Driving");
                break;
            case TYPE_HIT_AND_RUN:
                violationTypeDisplayProperty.set("Hit and Run");
                break;
            default:
                violationTypeDisplayProperty.set(violationType != null ? violationType.replace("_", " ") : "Unknown");
        }

        // Update payment status display
        switch (paymentStatus) {
            case PAYMENT_UNPAID:
                paymentStatusDisplayProperty.set("Unpaid");
                paymentColorProperty.set("#F44336");
                break;
            case PAYMENT_PAID:
                paymentStatusDisplayProperty.set("Paid");
                paymentColorProperty.set("#4CAF50");
                break;
            case PAYMENT_PARTIAL:
                paymentStatusDisplayProperty.set("Partial");
                paymentColorProperty.set("#FF9800");
                break;
            case PAYMENT_DISPUTED:
                paymentStatusDisplayProperty.set("Disputed");
                paymentColorProperty.set("#9C27B0");
                break;
            default:
                paymentStatusDisplayProperty.set(paymentStatus);
                paymentColorProperty.set("#9E9E9E");
        }

        // Update formatted fine
        formattedFineProperty.set(String.format("M%,.2f", fineAmount));
    }

    // ============================================
    // GETTERS AND SETTERS WITH PROPERTY UPDATES
    // ============================================

    @Override
    public int getId() { return id; }
    @Override
    public void setId(int id) { this.id = id; }

    public int getVehicleId() { return vehicleId; }
    public void setVehicleId(int vehicleId) { this.vehicleId = vehicleId; }

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

    public LocalDate getViolationDate() { return violationDate; }
    public void setViolationDate(LocalDate violationDate) {
        this.violationDate = violationDate;
        violationDateProperty.set(violationDate);
    }
    public ObjectProperty<LocalDate> violationDateProperty() { return violationDateProperty; }

    public String getViolationType() { return violationType; }
    public void setViolationType(String violationType) {
        this.violationType = violationType;
        violationTypeProperty.set(violationType);
    }
    public StringProperty violationTypeProperty() { return violationTypeProperty; }

    public double getFineAmount() { return fineAmount; }
    public void setFineAmount(double fineAmount) {
        this.fineAmount = fineAmount;
        fineAmountProperty.set(fineAmount);
    }
    public DoubleProperty fineAmountProperty() { return fineAmountProperty; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
        paymentStatusProperty.set(paymentStatus);
    }
    public StringProperty paymentStatusProperty() { return paymentStatusProperty; }

    public String getLocation() { return location; }
    public void setLocation(String location) {
        this.location = location;
        locationProperty.set(location);
    }
    public StringProperty locationProperty() { return locationProperty; }

    public String getOfficerName() { return officerName; }
    public void setOfficerName(String officerName) {
        this.officerName = officerName;
        officerNameProperty.set(officerName);
    }
    public StringProperty officerNameProperty() { return officerNameProperty; }

    public String getDescription() { return description; }
    public void setDescription(String description) {
        this.description = description;
        descriptionProperty.set(description);
    }
    public StringProperty descriptionProperty() { return descriptionProperty; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
        if (latitude != null) {
            latitudeProperty.set(latitude);
        }
    }
    public DoubleProperty latitudeProperty() { return latitudeProperty; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
        if (longitude != null) {
            longitudeProperty.set(longitude);
        }
    }
    public DoubleProperty longitudeProperty() { return longitudeProperty; }

    public String getViolationTypeDisplay() { return violationTypeDisplayProperty.get(); }
    public StringProperty violationTypeDisplayProperty() { return violationTypeDisplayProperty; }

    public String getPaymentStatusDisplay() { return paymentStatusDisplayProperty.get(); }
    public StringProperty paymentStatusDisplayProperty() { return paymentStatusDisplayProperty; }

    public String getPaymentColor() { return paymentColorProperty.get(); }
    public StringProperty paymentColorProperty() { return paymentColorProperty; }

    public String getFormattedFine() { return formattedFineProperty.get(); }
    public StringProperty formattedFineProperty() { return formattedFineProperty; }

    // ============================================
    // BUSINESS LOGIC METHODS
    // ============================================

    public boolean isPaid() {
        return PAYMENT_PAID.equals(paymentStatus);
    }

    public boolean isUnpaid() {
        return PAYMENT_UNPAID.equals(paymentStatus);
    }

    public String getFormattedViolationDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return violationDate != null ? violationDate.format(formatter) : "";
    }

    public void markAsPaid() {
        this.paymentStatus = PAYMENT_PAID;
        paymentStatusProperty.set(PAYMENT_PAID);
    }

    public void markAsDisputed() {
        this.paymentStatus = PAYMENT_DISPUTED;
        paymentStatusProperty.set(PAYMENT_DISPUTED);
    }

    public double getLatePenalty() {
        if (isPaid() || violationDate == null) return 0;
        long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(violationDate, LocalDate.now());
        if (daysOverdue <= 30) return 0;
        return fineAmount * 0.10 * ((daysOverdue - 30) / 30);
    }

    public double getTotalDue() {
        return fineAmount + getLatePenalty();
    }

    public String getFormattedTotalDue() {
        return String.format("M%,.2f", getTotalDue());
    }

    // ============================================
    // OVERRIDE METHODS
    // ============================================

    @Override
    public String toString() {
        return getViolationTypeDisplay() + " - " + getFormattedViolationDate() + " - " + getFormattedFine();
    }

    /**
     * Creates a copy of this violation.
     *
     * @return a new Violation instance
     */
    public Violation copy() {
        Violation copy = new Violation();
        copy.setId(this.id);
        copy.setVehicleId(this.vehicleId);
        copy.setRegistrationNumber(this.registrationNumber);
        copy.setMake(this.make);
        copy.setModel(this.model);
        copy.setViolationDate(this.violationDate);
        copy.setViolationType(this.violationType);
        copy.setFineAmount(this.fineAmount);
        copy.setPaymentStatus(this.paymentStatus);
        copy.setLocation(this.location);
        copy.setOfficerName(this.officerName);
        copy.setDescription(this.description);
        copy.setLatitude(this.latitude);
        copy.setLongitude(this.longitude);
        copy.setCreatedAt(this.getCreatedAt());
        copy.setUpdatedAt(this.getUpdatedAt());
        return copy;
    }
}