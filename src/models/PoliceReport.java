package models;

import java.time.LocalDate;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class PoliceReport extends BaseEntity {
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

    // JavaFX Properties
    private final StringProperty caseNumberProperty = new SimpleStringProperty();
    private final StringProperty registrationNumberProperty = new SimpleStringProperty();
    private final StringProperty reportTypeProperty = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> reportDateProperty = new SimpleObjectProperty<>();
    private final StringProperty officerNameProperty = new SimpleStringProperty();

    public PoliceReport() {
        super();
    }

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

        this.caseNumberProperty.set(caseNumber);
        this.reportTypeProperty.set(reportType);
        this.reportDateProperty.set(reportDate);
        this.officerNameProperty.set(officerName);
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

    public LocalDate getReportDate() { return reportDate; }
    public void setReportDate(LocalDate reportDate) {
        this.reportDate = reportDate;
        reportDateProperty.set(reportDate);
    }
    public ObjectProperty<LocalDate> reportDateProperty() { return reportDateProperty; }

    public String getReportType() { return reportType; }
    public void setReportType(String reportType) {
        this.reportType = reportType;
        reportTypeProperty.set(reportType);
    }
    public StringProperty reportTypeProperty() { return reportTypeProperty; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getOfficerName() { return officerName; }
    public void setOfficerName(String officerName) {
        this.officerName = officerName;
        officerNameProperty.set(officerName);
    }
    public StringProperty officerNameProperty() { return officerNameProperty; }

    public String getBadgeNumber() { return badgeNumber; }
    public void setBadgeNumber(String badgeNumber) { this.badgeNumber = badgeNumber; }

    public String getCaseNumber() { return caseNumber; }
    public void setCaseNumber(String caseNumber) {
        this.caseNumber = caseNumber;
        caseNumberProperty.set(caseNumber);
    }
    public StringProperty caseNumberProperty() { return caseNumberProperty; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    @Override
    public int getId() { return id; }
    @Override
    public void setId(int id) { this.id = id; }

    @Override
    public String toString() {
        return reportType + " - " + caseNumber + " - " + reportDate;
    }
}