package models;

import java.time.LocalDate;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class StolenVehicle extends BaseEntity {
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
    private String description;  // ADDED: missing field

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
    private final StringProperty descriptionProperty = new SimpleStringProperty();  // ADDED

    public StolenVehicle() {
        super();
        this.status = "ACTIVE";
    }

    public StolenVehicle(int vehicleId, LocalDate reportedDate, String caseNumber, String assignedOfficer) {
        this();
        this.vehicleId = vehicleId;
        this.reportedDate = reportedDate;
        this.caseNumber = caseNumber;
        this.assignedOfficer = assignedOfficer;
    }

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

    public boolean isActive() {
        return "ACTIVE".equals(status);
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
        return registrationNumber + " - " + caseNumber + " (" + status + ")";
    }
}