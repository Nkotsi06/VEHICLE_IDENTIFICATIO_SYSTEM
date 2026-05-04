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
 * StolenVehicle model representing stolen vehicle reports.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class StolenVehicle extends BaseEntity {

    // Core fields
    private int id;
    private int vehicleId;
    private String registrationNumber;
    private String make;
    private String model;
    private LocalDate reportedDate;
    private String caseNumber;
    private String status;
    private String assignedOfficer;
    private LocalDate recoveredDate;
    private Double latitude;
    private Double longitude;
    private Double distance;
    private String description;

    // Status constants
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_RECOVERED = "RECOVERED";
    public static final String STATUS_CLOSED = "CLOSED";
    public static final String STATUS_UNDER_INVESTIGATION = "UNDER_INVESTIGATION";

    // JavaFX Properties for TableView binding
    private final StringProperty registrationNumberProperty = new SimpleStringProperty();
    private final StringProperty makeProperty = new SimpleStringProperty();
    private final StringProperty modelProperty = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> reportedDateProperty = new SimpleObjectProperty<>();
    private final StringProperty caseNumberProperty = new SimpleStringProperty();
    private final StringProperty statusProperty = new SimpleStringProperty();
    private final StringProperty assignedOfficerProperty = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> recoveredDateProperty = new SimpleObjectProperty<>();
    private final DoubleProperty distanceProperty = new SimpleDoubleProperty();
    private final StringProperty descriptionProperty = new SimpleStringProperty();
    private final StringProperty statusDisplayProperty = new SimpleStringProperty();
    private final StringProperty statusColorProperty = new SimpleStringProperty();

    /**
     * Default constructor - initializes with ACTIVE status.
     */
    public StolenVehicle() {
        super();
        this.status = STATUS_ACTIVE;

        statusProperty.set(STATUS_ACTIVE);
        updateStatusDisplay();

        statusProperty.addListener((obs, oldVal, newVal) -> updateStatusDisplay());
    }

    /**
     * Constructor for creating a new stolen vehicle report.
     *
     * @param vehicleId       the vehicle ID
     * @param reportedDate    the reported date
     * @param caseNumber      the case number
     * @param assignedOfficer the assigned officer
     */
    public StolenVehicle(int vehicleId, LocalDate reportedDate, String caseNumber, String assignedOfficer) {
        this();
        this.vehicleId = vehicleId;
        this.reportedDate = reportedDate;
        this.caseNumber = caseNumber;
        this.assignedOfficer = assignedOfficer;

        reportedDateProperty.set(reportedDate);
        caseNumberProperty.set(caseNumber);
        assignedOfficerProperty.set(assignedOfficer);
    }

    // ============================================
    // PRIVATE UPDATE METHODS
    // ============================================

    private void updateStatusDisplay() {
        switch (status) {
            case STATUS_ACTIVE:
                statusDisplayProperty.set("Active");
                statusColorProperty.set("#F44336");
                break;
            case STATUS_RECOVERED:
                statusDisplayProperty.set("Recovered");
                statusColorProperty.set("#4CAF50");
                break;
            case STATUS_CLOSED:
                statusDisplayProperty.set("Closed");
                statusColorProperty.set("#9E9E9E");
                break;
            case STATUS_UNDER_INVESTIGATION:
                statusDisplayProperty.set("Under Investigation");
                statusColorProperty.set("#FF9800");
                break;
            default:
                statusDisplayProperty.set(status);
                statusColorProperty.set("#9E9E9E");
        }
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

    public LocalDate getReportedDate() {
        return reportedDate;
    }

    public void setReportedDate(LocalDate reportedDate) {
        this.reportedDate = reportedDate;
        reportedDateProperty.set(reportedDate);
    }

    public ObjectProperty<LocalDate> reportedDateProperty() {
        return reportedDateProperty;
    }

    public String getCaseNumber() {
        return caseNumber;
    }

    public void setCaseNumber(String caseNumber) {
        this.caseNumber = caseNumber;
        caseNumberProperty.set(caseNumber);
    }

    public StringProperty caseNumberProperty() {
        return caseNumberProperty;
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

    public String getAssignedOfficer() {
        return assignedOfficer;
    }

    public void setAssignedOfficer(String assignedOfficer) {
        this.assignedOfficer = assignedOfficer;
        assignedOfficerProperty.set(assignedOfficer);
    }

    public StringProperty assignedOfficerProperty() {
        return assignedOfficerProperty;
    }

    public LocalDate getRecoveredDate() {
        return recoveredDate;
    }

    public void setRecoveredDate(LocalDate recoveredDate) {
        this.recoveredDate = recoveredDate;
        recoveredDateProperty.set(recoveredDate);
    }

    public ObjectProperty<LocalDate> recoveredDateProperty() {
        return recoveredDateProperty;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
        distanceProperty.set(distance != null ? distance : 0);
    }

    public DoubleProperty distanceProperty() {
        return distanceProperty;
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

    public String getStatusDisplay() {
        return statusDisplayProperty.get();
    }

    public StringProperty statusDisplayProperty() {
        return statusDisplayProperty;
    }

    public String getStatusColor() {
        return statusColorProperty.get();
    }

    public StringProperty statusColorProperty() {
        return statusColorProperty;
    }

    // ============================================
    // BUSINESS LOGIC METHODS
    // ============================================

    public boolean isActive() {
        return STATUS_ACTIVE.equals(status);
    }

    public boolean isRecovered() {
        return STATUS_RECOVERED.equals(status);
    }

    public String getFormattedReportedDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return reportedDate != null ? reportedDate.format(formatter) : "";
    }

    public String getFormattedRecoveredDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return recoveredDate != null ? recoveredDate.format(formatter) : "";
    }

    public String getFormattedDistance() {
        if (distance == null) return "N/A";
        if (distance < 1) return String.format("%.0f meters", distance * 1000);
        return String.format("%.2f km", distance);
    }

    public void markAsRecovered(LocalDate date) {
        this.status = STATUS_RECOVERED;
        this.recoveredDate = date;
        statusProperty.set(STATUS_RECOVERED);
        recoveredDateProperty.set(date);
    }

    public void markAsUnderInvestigation() {
        this.status = STATUS_UNDER_INVESTIGATION;
        statusProperty.set(STATUS_UNDER_INVESTIGATION);
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
        return registrationNumber + " - " + caseNumber + " (" + getStatusDisplay() + ")";
    }

    /**
     * Creates a copy of this stolen vehicle record.
     *
     * @return a new StolenVehicle instance
     */
    public StolenVehicle copy() {
        StolenVehicle copy = new StolenVehicle();
        copy.setId(this.id);
        copy.setVehicleId(this.vehicleId);
        copy.setRegistrationNumber(this.registrationNumber);
        copy.setMake(this.make);
        copy.setModel(this.model);
        copy.setReportedDate(this.reportedDate);
        copy.setCaseNumber(this.caseNumber);
        copy.setStatus(this.status);
        copy.setAssignedOfficer(this.assignedOfficer);
        copy.setRecoveredDate(this.recoveredDate);
        copy.setLatitude(this.latitude);
        copy.setLongitude(this.longitude);
        copy.setDistance(this.distance);
        copy.setDescription(this.description);
        copy.setCreatedAt(this.getCreatedAt());
        copy.setUpdatedAt(this.getUpdatedAt());
        return copy;
    }
}