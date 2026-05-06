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
 * VehicleDocument model representing legal documents for vehicles.
 * Tracks document expiry and status for compliance checking.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class VehicleDocument extends BaseEntity {

    // Core fields
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

    // ADDED FIELDS for vehicle make and model
    private String vehicleMake;
    private String vehicleModel;

    // Document type constants
    public static final String TYPE_LICENSE_DISC = "LICENSE_DISC";
    public static final String TYPE_ROAD_WORTHY = "ROAD_WORTHY";
    public static final String TYPE_INSURANCE = "INSURANCE";
    public static final String TYPE_REGISTRATION = "REGISTRATION";
    public static final String TYPE_ID_PROOF = "ID_PROOF";

    // Status constants
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_EXPIRED = "EXPIRED";
    public static final String STATUS_SUSPENDED = "SUSPENDED";
    public static final String STATUS_REVOKED = "REVOKED";

    // Expiry status constants
    public static final String EXPIRY_EXPIRED = "EXPIRED";
    public static final String EXPIRY_CRITICAL = "CRITICAL";
    public static final String EXPIRY_WARNING = "WARNING";
    public static final String EXPIRY_DUE_SOON = "DUE_SOON";
    public static final String EXPIRY_VALID = "VALID";

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
    private final StringProperty documentTypeDisplayProperty = new SimpleStringProperty();
    private final StringProperty expiryColorProperty = new SimpleStringProperty();

    // ADDED PROPERTIES
    private final StringProperty vehicleMakeProperty = new SimpleStringProperty();
    private final StringProperty vehicleModelProperty = new SimpleStringProperty();

    /**
     * Default constructor - initializes with ACTIVE status.
     */
    public VehicleDocument() {
        super();
        this.status = STATUS_ACTIVE;

        statusProperty.set(STATUS_ACTIVE);
        updateDocumentTypeDisplay();
        updateExpiryColor();

        documentTypeProperty.addListener((obs, oldVal, newVal) -> updateDocumentTypeDisplay());
        expiryDateProperty.addListener((obs, oldVal, newVal) -> {
            calculateDaysRemaining();
            updateExpiryColor();
        });
    }

    /**
     * Constructor for creating a new vehicle document.
     *
     * @param vehicleId      the vehicle ID
     * @param documentType   the document type
     * @param documentNumber the document number
     * @param issueDate      the issue date
     * @param expiryDate     the expiry date
     */
    public VehicleDocument(int vehicleId, String documentType, String documentNumber,
                           LocalDate issueDate, LocalDate expiryDate) {
        this();
        this.vehicleId = vehicleId;
        this.documentType = documentType;
        this.documentNumber = documentNumber;
        this.issueDate = issueDate;
        this.expiryDate = expiryDate;

        vehicleIdProperty.set(vehicleId);
        documentTypeProperty.set(documentType);
        documentNumberProperty.set(documentNumber);
        issueDateProperty.set(issueDate);
        expiryDateProperty.set(expiryDate);
        calculateDaysRemaining();
    }

    // ============================================
    // PRIVATE UPDATE METHODS
    // ============================================

    private void updateDocumentTypeDisplay() {
        switch (documentType) {
            case TYPE_LICENSE_DISC:
                documentTypeDisplayProperty.set("License Disc");
                break;
            case TYPE_ROAD_WORTHY:
                documentTypeDisplayProperty.set("Road Worthy Certificate");
                break;
            case TYPE_INSURANCE:
                documentTypeDisplayProperty.set("Insurance Certificate");
                break;
            case TYPE_REGISTRATION:
                documentTypeDisplayProperty.set("Registration Certificate");
                break;
            case TYPE_ID_PROOF:
                documentTypeDisplayProperty.set("ID Proof");
                break;
            default:
                documentTypeDisplayProperty.set(documentType != null ? documentType.replace("_", " ") : "Unknown");
        }
    }

    private void updateExpiryColor() {
        switch (expiryStatus) {
            case EXPIRY_EXPIRED:
                expiryColorProperty.set("#F44336");
                break;
            case EXPIRY_CRITICAL:
                expiryColorProperty.set("#FF9800");
                break;
            case EXPIRY_WARNING:
                expiryColorProperty.set("#FFC107");
                break;
            case EXPIRY_DUE_SOON:
                expiryColorProperty.set("#8BC34A");
                break;
            case EXPIRY_VALID:
                expiryColorProperty.set("#4CAF50");
                break;
            default:
                expiryColorProperty.set("#9E9E9E");
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

    // ADDED GETTERS AND SETTERS
    public String getVehicleMake() {
        return vehicleMake;
    }

    public void setVehicleMake(String vehicleMake) {
        this.vehicleMake = vehicleMake;
        vehicleMakeProperty.set(vehicleMake);
    }

    public StringProperty vehicleMakeProperty() {
        return vehicleMakeProperty;
    }

    public String getVehicleModel() {
        return vehicleModel;
    }

    public void setVehicleModel(String vehicleModel) {
        this.vehicleModel = vehicleModel;
        vehicleModelProperty.set(vehicleModel);
    }

    public StringProperty vehicleModelProperty() {
        return vehicleModelProperty;
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
        updateExpiryColor();
    }

    public StringProperty expiryStatusProperty() {
        return expiryStatusProperty;
    }

    public String getDocumentTypeDisplay() {
        return documentTypeDisplayProperty.get();
    }

    public StringProperty documentTypeDisplayProperty() {
        return documentTypeDisplayProperty;
    }

    public String getExpiryColor() {
        return expiryColorProperty.get();
    }

    public StringProperty expiryColorProperty() {
        return expiryColorProperty;
    }

    // ============================================
    // BUSINESS LOGIC METHODS
    // ============================================

    private void calculateDaysRemaining() {
        if (expiryDate != null) {
            this.daysRemaining = (int) java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
            daysRemainingProperty.set(this.daysRemaining);
            updateExpiryStatus();
        }
    }

    private void updateExpiryStatus() {
        if (daysRemaining < 0) {
            this.expiryStatus = EXPIRY_EXPIRED;
        } else if (daysRemaining <= 7) {
            this.expiryStatus = EXPIRY_CRITICAL;
        } else if (daysRemaining <= 15) {
            this.expiryStatus = EXPIRY_WARNING;
        } else if (daysRemaining <= 30) {
            this.expiryStatus = EXPIRY_DUE_SOON;
        } else {
            this.expiryStatus = EXPIRY_VALID;
        }
        expiryStatusProperty.set(this.expiryStatus);
        updateExpiryColor();
    }

    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }

    public boolean isExpiringSoon() {
        if (expiryDate == null) return false;
        LocalDate now = LocalDate.now();
        return expiryDate.isAfter(now) && expiryDate.minusDays(30).isBefore(now);
    }

    public String getFormattedIssueDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return issueDate != null ? issueDate.format(formatter) : "";
    }

    public String getFormattedExpiryDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return expiryDate != null ? expiryDate.format(formatter) : "";
    }

    public String getExpiryStatusDisplay() {
        switch (expiryStatus) {
            case EXPIRY_EXPIRED: return "Expired";
            case EXPIRY_CRITICAL: return "Critical";
            case EXPIRY_WARNING: return "Warning";
            case EXPIRY_DUE_SOON: return "Due Soon";
            case EXPIRY_VALID: return "Valid";
            default: return expiryStatus;
        }
    }

    public String getVehicleInfo() {
        String info = registrationNumber != null ? registrationNumber : "";
        if (vehicleMake != null && vehicleModel != null) {
            info += " (" + vehicleMake + " " + vehicleModel + ")";
        }
        return info;
    }

    // ============================================
    // OVERRIDE METHODS
    // ============================================

    @Override
    public String toString() {
        return getDocumentTypeDisplay() + " - " + documentNumber + " - Expires: " + getFormattedExpiryDate();
    }

    /**
     * Creates a copy of this vehicle document.
     *
     * @return a new VehicleDocument instance
     */
    public VehicleDocument copy() {
        VehicleDocument copy = new VehicleDocument();
        copy.setId(this.id);
        copy.setVehicleId(this.vehicleId);
        copy.setRegistrationNumber(this.registrationNumber);
        copy.setVehicleMake(this.vehicleMake);
        copy.setVehicleModel(this.vehicleModel);
        copy.setDocumentType(this.documentType);
        copy.setDocumentNumber(this.documentNumber);
        copy.setIssueDate(this.issueDate);
        copy.setExpiryDate(this.expiryDate);
        copy.setDocumentFilePath(this.documentFilePath);
        copy.setStatus(this.status);
        copy.setDaysRemaining(this.daysRemaining);
        copy.setExpiryStatus(this.expiryStatus);
        copy.setCreatedAt(this.getCreatedAt());
        copy.setUpdatedAt(this.getUpdatedAt());
        return copy;
    }
}