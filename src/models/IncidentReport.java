package models;

import java.time.LocalDateTime;

public class IncidentReport extends BaseEntity {
    private int vehicleId;
    private String registrationNumber;
    private String incidentType;
    private LocalDateTime incidentDateTime;
    private String location;
    private String description;
    private String officerName;
    private String status;

    public IncidentReport() {
        super();
        this.status = "PENDING";
    }

    public IncidentReport(int vehicleId, String incidentType, LocalDateTime incidentDateTime,
                          String location, String description, String officerName) {
        this();
        this.vehicleId = vehicleId;
        this.incidentType = incidentType;
        this.incidentDateTime = incidentDateTime;
        this.location = location;
        this.description = description;
        this.officerName = officerName;
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
    }

    public String getIncidentType() {
        return incidentType;
    }

    public void setIncidentType(String incidentType) {
        this.incidentType = incidentType;
    }

    public LocalDateTime getIncidentDateTime() {
        return incidentDateTime;
    }

    public void setIncidentDateTime(LocalDateTime incidentDateTime) {
        this.incidentDateTime = incidentDateTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
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
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return incidentType + " - " + incidentDateTime + " - " + status;
    }
}