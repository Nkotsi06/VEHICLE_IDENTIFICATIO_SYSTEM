package models;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * PoliceReport model representing police reports for vehicles.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class PoliceReport extends BaseEntity {

    // Core fields
    private int id;
    private int vehicleId;
    private String registrationNumber;
    private String make;
    private String model;
    private LocalDate reportDate;
    private String reportType;
    private String description;
    private String officerName;
    private String badgeNumber;
    private String caseNumber;
    private String location;
    private String status;

    // Report type constants
    public static final String TYPE_STOLEN = "STOLEN";
    public static final String TYPE_ACCIDENT = "ACCIDENT";
    public static final String TYPE_THEFT = "THEFT";
    public static final String TYPE_VANDALISM = "VANDALISM";
    public static final String TYPE_HIJACKING = "HIJACKING";
    public static final String TYPE_HIT_AND_RUN = "HIT_AND_RUN";

    // Status constants
    public static final String STATUS_DRAFT = "DRAFT";
    public static final String STATUS_SUBMITTED = "SUBMITTED";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_CLOSED = "CLOSED";

    // JavaFX Properties
    private final IntegerProperty vehicleIdProperty = new SimpleIntegerProperty();
    private final StringProperty registrationNumberProperty = new SimpleStringProperty();
    private final StringProperty makeProperty = new SimpleStringProperty();
    private final StringProperty modelProperty = new SimpleStringProperty();
    private final StringProperty reportTypeProperty = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> reportDateProperty = new SimpleObjectProperty<>();
    private final StringProperty officerNameProperty = new SimpleStringProperty();
    private final StringProperty badgeNumberProperty = new SimpleStringProperty();
    private final StringProperty caseNumberProperty = new SimpleStringProperty();
    private final StringProperty locationProperty = new SimpleStringProperty();
    private final StringProperty statusProperty = new SimpleStringProperty();
    private final StringProperty reportTypeDisplayProperty = new SimpleStringProperty();
    private final StringProperty statusDisplayProperty = new SimpleStringProperty();
    private final StringProperty statusColorProperty = new SimpleStringProperty();

    /**
     * Default constructor.
     */
    public PoliceReport() {
        super();
        this.reportDate = LocalDate.now();
        this.status = STATUS_SUBMITTED;

        reportDateProperty.set(reportDate);
        statusProperty.set(STATUS_SUBMITTED);
        updateDisplayProperties();

        reportTypeProperty.addListener((obs, oldVal, newVal) -> updateDisplayProperties());
        statusProperty.addListener((obs, oldVal, newVal) -> updateDisplayProperties());
    }

    /**
     * Constructor for creating a new police report.
     *
     * @param vehicleId      the vehicle ID
     * @param reportDate     the report date
     * @param reportType     the report type
     * @param description    the description
     * @param officerName    the officer name
     * @param badgeNumber    the badge number
     * @param caseNumber     the case number
     */
    public PoliceReport(int vehicleId, LocalDate reportDate, String reportType, String description,
                        String officerName, String badgeNumber, String caseNumber) {
        this();
        this.vehicleId = vehicleId;
        this.reportDate = reportDate;
        this.reportType = reportType;
        this.description = description;
        this.officerName = officerName;
        this.badgeNumber = badgeNumber;
        this.caseNumber = caseNumber;

        vehicleIdProperty.set(vehicleId);
        reportDateProperty.set(reportDate);
        reportTypeProperty.set(reportType);
        officerNameProperty.set(officerName);
        badgeNumberProperty.set(badgeNumber);
        caseNumberProperty.set(caseNumber);
    }

    // ============================================
    // PRIVATE UPDATE METHODS
    // ============================================

    private void updateDisplayProperties() {
        // Update report type display
        switch (reportType) {
            case TYPE_STOLEN:
                reportTypeDisplayProperty.set("Stolen Vehicle");
                break;
            case TYPE_ACCIDENT:
                reportTypeDisplayProperty.set("Accident");
                break;
            case TYPE_THEFT:
                reportTypeDisplayProperty.set("Theft");
                break;
            case TYPE_VANDALISM:
                reportTypeDisplayProperty.set("Vandalism");
                break;
            case TYPE_HIJACKING:
                reportTypeDisplayProperty.set("Hijacking");
                break;
            case TYPE_HIT_AND_RUN:
                reportTypeDisplayProperty.set("Hit and Run");
                break;
            default:
                reportTypeDisplayProperty.set(reportType != null ? reportType : "Unknown");
        }

        // Update status display
        switch (status) {
            case STATUS_DRAFT:
                statusDisplayProperty.set("Draft");
                statusColorProperty.set("#9E9E9E");
                break;
            case STATUS_SUBMITTED:
                statusDisplayProperty.set("Submitted");
                statusColorProperty.set("#2196F3");
                break;
            case STATUS_APPROVED:
                statusDisplayProperty.set("Approved");
                statusColorProperty.set("#4CAF50");
                break;
            case STATUS_CLOSED:
                statusDisplayProperty.set("Closed");
                statusColorProperty.set("#9E9E9E");
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

    public LocalDate getReportDate() {
        return reportDate;
    }

    public void setReportDate(LocalDate reportDate) {
        this.reportDate = reportDate;
        reportDateProperty.set(reportDate);
    }

    public ObjectProperty<LocalDate> reportDateProperty() {
        return reportDateProperty;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
        reportTypeProperty.set(reportType);
    }

    public StringProperty reportTypeProperty() {
        return reportTypeProperty;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOfficerName() {
        return officerName;
    }

    public void setOfficerName(String officerName) {
        this.officerName = officerName;
        officerNameProperty.set(officerName);
    }

    public StringProperty officerNameProperty() {
        return officerNameProperty;
    }

    public String getBadgeNumber() {
        return badgeNumber;
    }

    public void setBadgeNumber(String badgeNumber) {
        this.badgeNumber = badgeNumber;
        badgeNumberProperty.set(badgeNumber);
    }

    public StringProperty badgeNumberProperty() {
        return badgeNumberProperty;
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
        locationProperty.set(location);
    }

    public StringProperty locationProperty() {
        return locationProperty;
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

    public String getReportTypeDisplay() {
        return reportTypeDisplayProperty.get();
    }

    public StringProperty reportTypeDisplayProperty() {
        return reportTypeDisplayProperty;
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

    public String getFormattedReportDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return reportDate != null ? reportDate.format(formatter) : "";
    }

    public String getVehicleInfo() {
        String info = registrationNumber != null ? registrationNumber : "";
        if (make != null && model != null) {
            info += " (" + make + " " + model + ")";
        }
        return info;
    }

    public boolean isStolenReport() {
        return TYPE_STOLEN.equals(reportType);
    }

    public boolean isAccidentReport() {
        return TYPE_ACCIDENT.equals(reportType);
    }

    public boolean isApproved() {
        return STATUS_APPROVED.equals(status);
    }

    public boolean isSubmitted() {
        return STATUS_SUBMITTED.equals(status);
    }

    public void approve() {
        this.status = STATUS_APPROVED;
        statusProperty.set(STATUS_APPROVED);
    }

    public void close() {
        this.status = STATUS_CLOSED;
        statusProperty.set(STATUS_CLOSED);
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
        return getReportTypeDisplay() + " - " + caseNumber + " - " + getFormattedReportDate();
    }

    /**
     * Creates a copy of this police report.
     *
     * @return a new PoliceReport instance
     */
    public PoliceReport copy() {
        PoliceReport copy = new PoliceReport();
        copy.setId(this.id);
        copy.setVehicleId(this.vehicleId);
        copy.setRegistrationNumber(this.registrationNumber);
        copy.setMake(this.make);
        copy.setModel(this.model);
        copy.setReportDate(this.reportDate);
        copy.setReportType(this.reportType);
        copy.setDescription(this.description);
        copy.setOfficerName(this.officerName);
        copy.setBadgeNumber(this.badgeNumber);
        copy.setCaseNumber(this.caseNumber);
        copy.setLocation(this.location);
        copy.setStatus(this.status);
        copy.setCreatedAt(this.getCreatedAt());
        copy.setUpdatedAt(this.getUpdatedAt());
        return copy;
    }
}