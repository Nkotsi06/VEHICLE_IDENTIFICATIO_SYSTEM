package models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * IncidentReport model representing police incident reports for vehicles.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class IncidentReport extends BaseEntity {

    // Core fields
    private int id;
    private int vehicleId;
    private String registrationNumber;
    private String incidentType;
    private LocalDateTime incidentDateTime;
    private String location;
    private String description;
    private String officerName;
    private String status;
    private String caseNumber;
    private String witnesses;

    // Incident type constants
    public static final String TYPE_ACCIDENT = "ACCIDENT";
    public static final String TYPE_THEFT = "THEFT";
    public static final String TYPE_VANDALISM = "VANDALISM";
    public static final String TYPE_HIJACKING = "HIJACKING";
    public static final String TYPE_HIT_AND_RUN = "HIT_AND_RUN";

    // Status constants
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_INVESTIGATING = "INVESTIGATING";
    public static final String STATUS_RESOLVED = "RESOLVED";
    public static final String STATUS_CLOSED = "CLOSED";

    /**
     * Default constructor - initializes with PENDING status.
     */
    public IncidentReport() {
        super();
        this.status = STATUS_PENDING;
    }

    /**
     * Constructor for creating a new incident report.
     *
     * @param vehicleId       the vehicle ID
     * @param incidentType    the incident type
     * @param incidentDateTime the incident date and time
     * @param location        the incident location
     * @param description     the incident description
     * @param officerName     the reporting officer's name
     */
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

    // ============================================
    // GETTERS AND SETTERS
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

    public String getCaseNumber() {
        return caseNumber;
    }

    public void setCaseNumber(String caseNumber) {
        this.caseNumber = caseNumber;
    }

    public String getWitnesses() {
        return witnesses;
    }

    public void setWitnesses(String witnesses) {
        this.witnesses = witnesses;
    }

    // ============================================
    // BUSINESS LOGIC METHODS
    // ============================================

    /**
     * Gets the formatted incident date and time.
     *
     * @return formatted date-time string
     */
    public String getFormattedIncidentDateTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return incidentDateTime != null ? incidentDateTime.format(formatter) : "";
    }

    /**
     * Gets the incident type display name.
     *
     * @return human-readable incident type
     */
    public String getIncidentTypeDisplay() {
        switch (incidentType) {
            case TYPE_ACCIDENT: return "Accident";
            case TYPE_THEFT: return "Theft";
            case TYPE_VANDALISM: return "Vandalism";
            case TYPE_HIJACKING: return "Hijacking";
            case TYPE_HIT_AND_RUN: return "Hit and Run";
            default: return incidentType != null ? incidentType.replace("_", " ") : "Unknown";
        }
    }

    /**
     * Gets the status display name.
     *
     * @return human-readable status
     */
    public String getStatusDisplay() {
        switch (status) {
            case STATUS_PENDING: return "Pending";
            case STATUS_INVESTIGATING: return "Under Investigation";
            case STATUS_RESOLVED: return "Resolved";
            case STATUS_CLOSED: return "Closed";
            default: return status;
        }
    }

    /**
     * Gets the status color.
     *
     * @return hex color code
     */
    public String getStatusColor() {
        switch (status) {
            case STATUS_PENDING: return "#FF9800";
            case STATUS_INVESTIGATING: return "#2196F3";
            case STATUS_RESOLVED: return "#4CAF50";
            case STATUS_CLOSED: return "#9E9E9E";
            default: return "#9E9E9E";
        }
    }

    /**
     * Gets the incident type color.
     *
     * @return hex color code
     */
    public String getIncidentTypeColor() {
        switch (incidentType) {
            case TYPE_ACCIDENT: return "#FF9800";
            case TYPE_THEFT: return "#F44336";
            case TYPE_VANDALISM: return "#9C27B0";
            case TYPE_HIJACKING: return "#D32F2F";
            case TYPE_HIT_AND_RUN: return "#E91E63";
            default: return "#9E9E9E";
        }
    }

    /**
     * Checks if the incident is resolved.
     *
     * @return true if resolved or closed
     */
    public boolean isResolved() {
        return STATUS_RESOLVED.equals(status) || STATUS_CLOSED.equals(status);
    }

    /**
     * Marks the incident as under investigation.
     */
    public void markInvestigating() {
        this.status = STATUS_INVESTIGATING;
    }

    /**
     * Marks the incident as resolved.
     */
    public void markResolved() {
        this.status = STATUS_RESOLVED;
    }

    /**
     * Marks the incident as closed.
     */
    public void markClosed() {
        this.status = STATUS_CLOSED;
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
        return getIncidentTypeDisplay() + " - " + registrationNumber + " - " + getStatusDisplay();
    }

    /**
     * Creates a copy of this incident report.
     *
     * @return a new IncidentReport instance
     */
    public IncidentReport copy() {
        IncidentReport copy = new IncidentReport();
        copy.setId(this.id);
        copy.setVehicleId(this.vehicleId);
        copy.setRegistrationNumber(this.registrationNumber);
        copy.setIncidentType(this.incidentType);
        copy.setIncidentDateTime(this.incidentDateTime);
        copy.setLocation(this.location);
        copy.setDescription(this.description);
        copy.setOfficerName(this.officerName);
        copy.setStatus(this.status);
        copy.setCaseNumber(this.caseNumber);
        copy.setWitnesses(this.witnesses);
        copy.setCreatedAt(this.getCreatedAt());
        copy.setUpdatedAt(this.getUpdatedAt());
        return copy;
    }
}