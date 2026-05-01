package models;

import java.time.LocalDate;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ExpiredDocumentAlert extends BaseEntity {
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

    // JavaFX Properties
    private final StringProperty registrationNumberProperty = new SimpleStringProperty();
    private final StringProperty documentTypeProperty = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> expiryDateProperty = new SimpleObjectProperty<>();
    private final IntegerProperty daysOverdueProperty = new SimpleIntegerProperty();
    private final StringProperty alertLevelProperty = new SimpleStringProperty();

    public ExpiredDocumentAlert() {
        super();
        this.isNotified = false;
    }

    public ExpiredDocumentAlert(int vehicleId, int documentId, String documentType, LocalDate expiryDate) {
        this();
        this.vehicleId = vehicleId;
        this.documentId = documentId;
        this.documentType = documentType;
        this.expiryDate = expiryDate;
        calculateDaysOverdue();
        determineAlertLevel();
        determineRecommendedAction();
    }

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

    public int getDocumentId() { return documentId; }
    public void setDocumentId(int documentId) { this.documentId = documentId; }

    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) {
        this.documentType = documentType;
        documentTypeProperty.set(documentType);
    }
    public StringProperty documentTypeProperty() { return documentTypeProperty; }

    public String getDocumentNumber() { return documentNumber; }
    public void setDocumentNumber(String documentNumber) { this.documentNumber = documentNumber; }

    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
        expiryDateProperty.set(expiryDate);
        calculateDaysOverdue();
        determineAlertLevel();
        determineRecommendedAction();
    }
    public ObjectProperty<LocalDate> expiryDateProperty() { return expiryDateProperty; }

    public int getDaysOverdue() { return daysOverdue; }
    public void setDaysOverdue(int daysOverdue) {
        this.daysOverdue = daysOverdue;
        daysOverdueProperty.set(daysOverdue);
    }
    public IntegerProperty daysOverdueProperty() { return daysOverdueProperty; }

    private void calculateDaysOverdue() {
        if (expiryDate != null) {
            this.daysOverdue = (int) (LocalDate.now().toEpochDay() - expiryDate.toEpochDay());
            if (this.daysOverdue < 0) this.daysOverdue = 0;
            daysOverdueProperty.set(this.daysOverdue);
        } else {
            this.daysOverdue = 0;
        }
    }

    public String getAlertLevel() { return alertLevel; }
    public void setAlertLevel(String alertLevel) {
        this.alertLevel = alertLevel;
        alertLevelProperty.set(alertLevel);
    }
    public StringProperty alertLevelProperty() { return alertLevelProperty; }

    private void determineAlertLevel() {
        if (daysOverdue >= 90) {
            this.alertLevel = "CRITICAL";
        } else if (daysOverdue >= 30) {
            this.alertLevel = "HIGH";
        } else if (daysOverdue >= 1) {
            this.alertLevel = "MEDIUM";
        } else {
            this.alertLevel = "LOW";
        }
        alertLevelProperty.set(this.alertLevel);
    }

    public String getRecommendedAction() { return recommendedAction; }
    public void setRecommendedAction(String recommendedAction) { this.recommendedAction = recommendedAction; }

    private void determineRecommendedAction() {
        if (daysOverdue >= 90) {
            this.recommendedAction = "IMMEDIATE_VEHICLE_IMPOUND";
        } else if (daysOverdue >= 30) {
            this.recommendedAction = "COURT_SUMMONS";
        } else if (daysOverdue >= 1) {
            this.recommendedAction = "ON_THE_SPOT_FINE";
        } else {
            this.recommendedAction = "WARNING_NOTICE";
        }
    }

    public boolean isNotified() { return isNotified; }
    public void setNotified(boolean notified) { isNotified = notified; }

    public LocalDate getNotifiedDate() { return notifiedDate; }
    public void setNotifiedDate(LocalDate notifiedDate) { this.notifiedDate = notifiedDate; }

    public String getAlertLevelColor() {
        switch (alertLevel) {
            case "CRITICAL": return "#D32F2F";
            case "HIGH": return "#F44336";
            case "MEDIUM": return "#FF9800";
            case "LOW": return "#FFC107";
            default: return "#9E9E9E";
        }
    }

    @Override
    public int getId() { return id; }
    @Override
    public void setId(int id) { this.id = id; }

    @Override
    public String toString() {
        return documentType + " expired for " + registrationNumber + " (" + daysOverdue + " days) - " + alertLevel;
    }
}