package models;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javafx.beans.property.*;

/**
 * ExpiredDocumentAlert model representing alerts for expired or soon-to-expire documents.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class ExpiredDocumentAlert extends BaseEntity {

    // Core fields
    private int id;
    private int vehicleId;
    private String registrationNumber;
    private String make;
    private String model;
    private String documentType;
    private String documentNumber;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String alertLevel;
    private String recommendedAction;
    private int daysOverdue;
    private boolean notified;

    // Alert level constants
    public static final String ALERT_CRITICAL = "CRITICAL";
    public static final String ALERT_HIGH = "HIGH";
    public static final String ALERT_MEDIUM = "MEDIUM";
    public static final String ALERT_LOW = "LOW";
    public static final String ALERT_NONE = "NONE";

    // Recommended action constants
    public static final String ACTION_IMMEDIATE_VEHICLE_IMPOUND = "IMMEDIATE_VEHICLE_IMPOUND";
    public static final String ACTION_ON_THE_SPOT_FINE = "ON_THE_SPOT_FINE";
    public static final String ACTION_WARNING_NOTICE = "WARNING_NOTICE";
    public static final String ACTION_REMINDER = "REMINDER";
    public static final String ACTION_NO_ACTION = "NO_ACTION";

    // JavaFX Properties
    private final IntegerProperty vehicleIdProperty = new SimpleIntegerProperty();
    private final StringProperty registrationNumberProperty = new SimpleStringProperty();
    private final StringProperty makeProperty = new SimpleStringProperty();
    private final StringProperty modelProperty = new SimpleStringProperty();
    private final StringProperty documentTypeProperty = new SimpleStringProperty();
    private final StringProperty documentNumberProperty = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> issueDateProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> expiryDateProperty = new SimpleObjectProperty<>();
    private final StringProperty alertLevelProperty = new SimpleStringProperty();
    private final StringProperty recommendedActionProperty = new SimpleStringProperty();
    private final IntegerProperty daysOverdueProperty = new SimpleIntegerProperty();
    private final BooleanProperty notifiedProperty = new SimpleBooleanProperty();
    private final StringProperty alertColorProperty = new SimpleStringProperty();
    private final StringProperty daysRemainingDisplayProperty = new SimpleStringProperty();

    /**
     * Default constructor.
     */
    public ExpiredDocumentAlert() {
        super();
        this.notified = false;
        notifiedProperty.set(false);
        updateAlertColor();
    }

    // ============================================
    // PRIVATE UPDATE METHODS
    // ============================================

    private void updateAlertColor() {
        switch (alertLevel) {
            case ALERT_CRITICAL:
                alertColorProperty.set("#D32F2F");
                break;
            case ALERT_HIGH:
                alertColorProperty.set("#F44336");
                break;
            case ALERT_MEDIUM:
                alertColorProperty.set("#FF9800");
                break;
            case ALERT_LOW:
                alertColorProperty.set("#FFC107");
                break;
            default:
                alertColorProperty.set("#4CAF50");
        }
    }

    // ============================================
    // GETTERS AND SETTERS WITH PROPERTY UPDATES
    // ============================================

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
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

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
        makeProperty.set(make);
    }

    public StringProperty makeProperty() {
        return makeProperty;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
        modelProperty.set(model);
    }

    public StringProperty modelProperty() {
        return modelProperty;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
        documentTypeProperty.set(documentType);
    }

    public StringProperty documentTypeProperty() {
        return documentTypeProperty;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
        documentNumberProperty.set(documentNumber);
    }

    public StringProperty documentNumberProperty() {
        return documentNumberProperty;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
        issueDateProperty.set(issueDate);
    }

    public ObjectProperty<LocalDate> issueDateProperty() {
        return issueDateProperty;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
        expiryDateProperty.set(expiryDate);
        updateDaysRemainingDisplay();
    }

    public ObjectProperty<LocalDate> expiryDateProperty() {
        return expiryDateProperty;
    }

    public String getAlertLevel() {
        return alertLevel;
    }

    public void setAlertLevel(String alertLevel) {
        this.alertLevel = alertLevel;
        alertLevelProperty.set(alertLevel);
        updateAlertColor();
    }

    public StringProperty alertLevelProperty() {
        return alertLevelProperty;
    }

    public String getRecommendedAction() {
        return recommendedAction;
    }

    public void setRecommendedAction(String recommendedAction) {
        this.recommendedAction = recommendedAction;
        recommendedActionProperty.set(recommendedAction);
    }

    public StringProperty recommendedActionProperty() {
        return recommendedActionProperty;
    }

    public int getDaysOverdue() {
        return daysOverdue;
    }

    public void setDaysOverdue(int daysOverdue) {
        this.daysOverdue = daysOverdue;
        daysOverdueProperty.set(daysOverdue);
        updateDaysRemainingDisplay();
    }

    public IntegerProperty daysOverdueProperty() {
        return daysOverdueProperty;
    }

    public boolean isNotified() {
        return notified;
    }

    public void setNotified(boolean notified) {
        this.notified = notified;
        notifiedProperty.set(notified);
    }

    public BooleanProperty notifiedProperty() {
        return notifiedProperty;
    }

    public String getAlertColor() {
        return alertColorProperty.get();
    }

    public StringProperty alertColorProperty() {
        return alertColorProperty;
    }

    public String getDaysRemainingDisplay() {
        return daysRemainingDisplayProperty.get();
    }

    public StringProperty daysRemainingDisplayProperty() {
        return daysRemainingDisplayProperty;
    }

    // ============================================
    // BUSINESS LOGIC METHODS
    // ============================================

    /**
     * Gets the days remaining until expiry.
     * Returns negative value if expired.
     *
     * @return days remaining (negative if expired)
     */
    public int getDaysRemaining() {
        if (expiryDate == null) return 0;
        return (int) java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
    }

    /**
     * Gets the formatted days remaining string.
     *
     * @return formatted string (e.g., "5 days left" or "Expired 3 days ago")
     */
    public String getFormattedDaysRemaining() {
        int days = getDaysRemaining();
        if (days < 0) {
            return "Expired " + Math.abs(days) + " days ago";
        } else if (days == 0) {
            return "Expires today";
        } else {
            return days + " days left";
        }
    }

    private void updateDaysRemainingDisplay() {
        daysRemainingDisplayProperty.set(getFormattedDaysRemaining());
    }

    public String getFormattedIssueDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return issueDate != null ? issueDate.format(formatter) : "";
    }

    public String getFormattedExpiryDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return expiryDate != null ? expiryDate.format(formatter) : "";
    }

    public String getAlertLevelDisplay() {
        switch (alertLevel) {
            case ALERT_CRITICAL: return "Critical";
            case ALERT_HIGH: return "High";
            case ALERT_MEDIUM: return "Medium";
            case ALERT_LOW: return "Low";
            default: return "None";
        }
    }

    public String getVehicleInfo() {
        String info = registrationNumber != null ? registrationNumber : "";
        if (make != null && model != null) {
            info += " (" + make + " " + model + ")";
        }
        return info;
    }

    // ============================================
    // OVERRIDE METHODS
    // ============================================

    @Override
    public String toString() {
        return getAlertLevelDisplay() + " Alert: " + documentType + " - " + getFormattedDaysRemaining();
    }

    /**
     * Creates a copy of this alert.
     *
     * @return a new ExpiredDocumentAlert instance
     */
    public ExpiredDocumentAlert copy() {
        ExpiredDocumentAlert copy = new ExpiredDocumentAlert();
        copy.setId(this.id);
        copy.setVehicleId(this.vehicleId);
        copy.setRegistrationNumber(this.registrationNumber);
        copy.setMake(this.make);
        copy.setModel(this.model);
        copy.setDocumentType(this.documentType);
        copy.setDocumentNumber(this.documentNumber);
        copy.setIssueDate(this.issueDate);
        copy.setExpiryDate(this.expiryDate);
        copy.setAlertLevel(this.alertLevel);
        copy.setRecommendedAction(this.recommendedAction);
        copy.setDaysOverdue(this.daysOverdue);
        copy.setNotified(this.notified);
        copy.setCreatedAt(this.getCreatedAt());
        copy.setUpdatedAt(this.getUpdatedAt());
        return copy;
    }
}