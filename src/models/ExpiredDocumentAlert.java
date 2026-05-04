package models;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * ExpiredDocumentAlert model representing alerts for expired or soon-to-expire vehicle documents.
 * Used by police for enforcement actions.
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
    private int documentId;
    private String documentType;
    private String documentNumber;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private int daysOverdue;
    private String alertLevel;
    private String recommendedAction;
    private boolean isNotified;
    private LocalDate notifiedDate;

    // Alert level constants
    public static final String ALERT_CRITICAL = "CRITICAL";
    public static final String ALERT_HIGH = "HIGH";
    public static final String ALERT_MEDIUM = "MEDIUM";
    public static final String ALERT_LOW = "LOW";

    // Recommended action constants
    public static final String ACTION_IMMEDIATE_IMPOUND = "IMMEDIATE_VEHICLE_IMPOUND";
    public static final String ACTION_COURT_SUMMONS = "COURT_SUMMONS";
    public static final String ACTION_ON_THE_SPOT_FINE = "ON_THE_SPOT_FINE";
    public static final String ACTION_WARNING_NOTICE = "WARNING_NOTICE";

    // Threshold constants
    private static final int CRITICAL_THRESHOLD_DAYS = 90;
    private static final int HIGH_THRESHOLD_DAYS = 30;
    private static final int MEDIUM_THRESHOLD_DAYS = 1;

    // JavaFX Properties for TableView binding
    private final StringProperty registrationNumberProperty = new SimpleStringProperty();
    private final StringProperty documentTypeProperty = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> expiryDateProperty = new SimpleObjectProperty<>();
    private final IntegerProperty daysOverdueProperty = new SimpleIntegerProperty();
    private final StringProperty alertLevelProperty = new SimpleStringProperty();
    private final StringProperty recommendedActionProperty = new SimpleStringProperty();
    private final StringProperty makeProperty = new SimpleStringProperty();
    private final StringProperty modelProperty = new SimpleStringProperty();

    /**
     * Default constructor - initializes alert state.
     */
    public ExpiredDocumentAlert() {
        super();
        this.isNotified = false;
        this.daysOverdue = 0;
        this.alertLevel = ALERT_LOW;
        this.recommendedAction = ACTION_WARNING_NOTICE;

        alertLevelProperty.set(ALERT_LOW);
        recommendedActionProperty.set(ACTION_WARNING_NOTICE);
        daysOverdueProperty.set(0);
    }

    /**
     * Constructor for creating an alert from a document.
     *
     * @param vehicleId    the vehicle ID
     * @param documentId   the document ID
     * @param documentType the type of document
     * @param expiryDate   the expiry date
     */
    public ExpiredDocumentAlert(int vehicleId, int documentId, String documentType, LocalDate expiryDate) {
        this();
        this.vehicleId = vehicleId;
        this.documentId = documentId;
        this.documentType = documentType;
        this.expiryDate = expiryDate;

        documentTypeProperty.set(documentType);
        expiryDateProperty.set(expiryDate);

        calculateDaysOverdue();
        determineAlertLevel();
        determineRecommendedAction();
    }

    // ============================================
    // GETTERS AND SETTERS WITH PROPERTY UPDATES
    // ============================================

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

    public int getDocumentId() {
        return documentId;
    }

    public void setDocumentId(int documentId) {
        this.documentId = documentId;
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
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
        expiryDateProperty.set(expiryDate);
        calculateDaysOverdue();
        determineAlertLevel();
        determineRecommendedAction();
    }

    public ObjectProperty<LocalDate> expiryDateProperty() {
        return expiryDateProperty;
    }

    public int getDaysOverdue() {
        return daysOverdue;
    }

    public void setDaysOverdue(int daysOverdue) {
        this.daysOverdue = daysOverdue;
        daysOverdueProperty.set(daysOverdue);
    }

    public IntegerProperty daysOverdueProperty() {
        return daysOverdueProperty;
    }

    /**
     * Calculates days overdue based on expiry date.
     */
    private void calculateDaysOverdue() {
        if (expiryDate != null) {
            this.daysOverdue = (int) (LocalDate.now().toEpochDay() - expiryDate.toEpochDay());
            if (this.daysOverdue < 0) this.daysOverdue = 0;
            daysOverdueProperty.set(this.daysOverdue);
        } else {
            this.daysOverdue = 0;
        }
    }

    public String getAlertLevel() {
        return alertLevel;
    }

    public void setAlertLevel(String alertLevel) {
        this.alertLevel = alertLevel;
        alertLevelProperty.set(alertLevel);
    }

    public StringProperty alertLevelProperty() {
        return alertLevelProperty;
    }

    /**
     * Determines alert level based on days overdue.
     */
    private void determineAlertLevel() {
        if (daysOverdue >= CRITICAL_THRESHOLD_DAYS) {
            this.alertLevel = ALERT_CRITICAL;
        } else if (daysOverdue >= HIGH_THRESHOLD_DAYS) {
            this.alertLevel = ALERT_HIGH;
        } else if (daysOverdue >= MEDIUM_THRESHOLD_DAYS) {
            this.alertLevel = ALERT_MEDIUM;
        } else {
            this.alertLevel = ALERT_LOW;
        }
        alertLevelProperty.set(this.alertLevel);
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

    /**
     * Determines recommended action based on alert level.
     */
    private void determineRecommendedAction() {
        if (daysOverdue >= CRITICAL_THRESHOLD_DAYS) {
            this.recommendedAction = ACTION_IMMEDIATE_IMPOUND;
        } else if (daysOverdue >= HIGH_THRESHOLD_DAYS) {
            this.recommendedAction = ACTION_COURT_SUMMONS;
        } else if (daysOverdue >= MEDIUM_THRESHOLD_DAYS) {
            this.recommendedAction = ACTION_ON_THE_SPOT_FINE;
        } else {
            this.recommendedAction = ACTION_WARNING_NOTICE;
        }
        recommendedActionProperty.set(this.recommendedAction);
    }

    public boolean isNotified() {
        return isNotified;
    }

    public void setNotified(boolean notified) {
        isNotified = notified;
    }

    public LocalDate getNotifiedDate() {
        return notifiedDate;
    }

    public void setNotifiedDate(LocalDate notifiedDate) {
        this.notifiedDate = notifiedDate;
    }

    // ============================================
    // BUSINESS LOGIC METHODS
    // ============================================

    /**
     * Gets the CSS color for the alert level.
     *
     * @return hex color code
     */
    public String getAlertLevelColor() {
        switch (alertLevel) {
            case ALERT_CRITICAL: return "#D32F2F";
            case ALERT_HIGH: return "#F44336";
            case ALERT_MEDIUM: return "#FF9800";
            case ALERT_LOW: return "#FFC107";
            default: return "#9E9E9E";
        }
    }

    /**
     * Gets the alert level display name.
     *
     * @return human-readable alert level
     */
    public String getAlertLevelDisplay() {
        switch (alertLevel) {
            case ALERT_CRITICAL: return "Critical";
            case ALERT_HIGH: return "High";
            case ALERT_MEDIUM: return "Medium";
            case ALERT_LOW: return "Low";
            default: return alertLevel;
        }
    }

    /**
     * Gets the recommended action display name.
     *
     * @return human-readable action
     */
    public String getRecommendedActionDisplay() {
        switch (recommendedAction) {
            case ACTION_IMMEDIATE_IMPOUND: return "Immediate Vehicle Impound";
            case ACTION_COURT_SUMMONS: return "Court Summons";
            case ACTION_ON_THE_SPOT_FINE: return "On-the-Spot Fine";
            case ACTION_WARNING_NOTICE: return "Warning Notice";
            default: return recommendedAction;
        }
    }

    /**
     * Gets the formatted expiry date.
     *
     * @return formatted date string
     */
    public String getFormattedExpiryDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return expiryDate != null ? expiryDate.format(formatter) : "";
    }

    /**
     * Gets the formatted issue date.
     *
     * @return formatted date string
     */
    public String getFormattedIssueDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return issueDate != null ? issueDate.format(formatter) : "";
    }

    /**
     * Gets the formatted notified date.
     *
     * @return formatted date string
     */
    public String getFormattedNotifiedDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return notifiedDate != null ? notifiedDate.format(formatter) : "";
    }

    /**
     * Checks if the alert is critical.
     *
     * @return true if critical
     */
    public boolean isCritical() {
        return ALERT_CRITICAL.equals(alertLevel);
    }

    /**
     * Checks if the alert is high priority.
     *
     * @return true if critical or high
     */
    public boolean isHighPriority() {
        return ALERT_CRITICAL.equals(alertLevel) || ALERT_HIGH.equals(alertLevel);
    }

    /**
     * Marks the alert as notified.
     */
    public void markNotified() {
        this.isNotified = true;
        this.notifiedDate = LocalDate.now();
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
        return documentType + " expired for " + registrationNumber + " (" + daysOverdue + " days) - " + getAlertLevelDisplay();
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
        copy.setDocumentId(this.documentId);
        copy.setDocumentType(this.documentType);
        copy.setDocumentNumber(this.documentNumber);
        copy.setIssueDate(this.issueDate);
        copy.setExpiryDate(this.expiryDate);
        copy.setDaysOverdue(this.daysOverdue);
        copy.setAlertLevel(this.alertLevel);
        copy.setRecommendedAction(this.recommendedAction);
        copy.setNotified(this.isNotified);
        copy.setNotifiedDate(this.notifiedDate);
        copy.setCreatedAt(this.getCreatedAt());
        copy.setUpdatedAt(this.getUpdatedAt());
        return copy;
    }
}