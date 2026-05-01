package models;

import java.time.LocalDate;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class VehicleDocument extends BaseEntity {
    private int id;
    private int vehicleId;
    private String registrationNumber;
    private String documentType;
    private String documentNumber;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String documentFilePath;
    private String status;
    private int daysRemaining;
    private String expiryStatus;

    // JavaFX Properties for TableView binding
    private final IntegerProperty vehicleIdProperty = new SimpleIntegerProperty();
    private final StringProperty registrationNumberProperty = new SimpleStringProperty();
    private final StringProperty documentTypeProperty = new SimpleStringProperty();
    private final StringProperty documentNumberProperty = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> issueDateProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> expiryDateProperty = new SimpleObjectProperty<>();
    private final StringProperty statusProperty = new SimpleStringProperty();
    private final IntegerProperty daysRemainingProperty = new SimpleIntegerProperty();
    private final StringProperty expiryStatusProperty = new SimpleStringProperty();

    public VehicleDocument() {
        super();
        this.status = "ACTIVE";
    }

    public VehicleDocument(int vehicleId, String documentType, String documentNumber,
                           LocalDate issueDate, LocalDate expiryDate) {
        this();
        this.vehicleId = vehicleId;
        this.documentType = documentType;
        this.documentNumber = documentNumber;
        this.issueDate = issueDate;
        this.expiryDate = expiryDate;

        // Update properties
        vehicleIdProperty.set(vehicleId);
        documentTypeProperty.set(documentType);
        documentNumberProperty.set(documentNumber);
        issueDateProperty.set(issueDate);
        expiryDateProperty.set(expiryDate);
        statusProperty.set("ACTIVE");
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
        calculateDaysRemaining();
    }

    public ObjectProperty<LocalDate> expiryDateProperty() {
        return expiryDateProperty;
    }

    public String getDocumentFilePath() {
        return documentFilePath;
    }

    public void setDocumentFilePath(String documentFilePath) {
        this.documentFilePath = documentFilePath;
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

    public int getDaysRemaining() {
        return daysRemaining;
    }

    public void setDaysRemaining(int daysRemaining) {
        this.daysRemaining = daysRemaining;
        daysRemainingProperty.set(daysRemaining);
    }

    public IntegerProperty daysRemainingProperty() {
        return daysRemainingProperty;
    }

    public String getExpiryStatus() {
        return expiryStatus;
    }

    public void setExpiryStatus(String expiryStatus) {
        this.expiryStatus = expiryStatus;
        expiryStatusProperty.set(expiryStatus);
    }

    public StringProperty expiryStatusProperty() {
        return expiryStatusProperty;
    }

    private void calculateDaysRemaining() {
        if (expiryDate != null) {
            this.daysRemaining = (int) java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
            daysRemainingProperty.set(this.daysRemaining);
            updateExpiryStatus();
        }
    }

    private void updateExpiryStatus() {
        if (daysRemaining < 0) {
            this.expiryStatus = "EXPIRED";
        } else if (daysRemaining <= 7) {
            this.expiryStatus = "CRITICAL";
        } else if (daysRemaining <= 15) {
            this.expiryStatus = "WARNING";
        } else if (daysRemaining <= 30) {
            this.expiryStatus = "DUE_SOON";
        } else {
            this.expiryStatus = "VALID";
        }
        expiryStatusProperty.set(this.expiryStatus);
    }

    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }

    public boolean isExpiringSoon() {
        if (expiryDate == null) return false;
        LocalDate now = LocalDate.now();
        return expiryDate.isAfter(now) && expiryDate.minusDays(30).isBefore(now);
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
        return documentType + " - " + documentNumber + " - Expires: " + expiryDate;
    }
}