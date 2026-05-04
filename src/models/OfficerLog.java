package models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * OfficerLog model representing simplified officer activity logs.
 * Used for quick viewing of officer actions.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class OfficerLog extends BaseEntity {

    // Core fields
    private int id;
    private String officerName;
    private String badgeNumber;
    private String action;
    private int vehicleId;
    private String registrationNumber;
    private LocalDateTime timestamp;

    // Action constants
    public static final String ACTION_VIEW = "VIEW";
    public static final String ACTION_REPORT = "REPORT";
    public static final String ACTION_BOLO = "BOLO";
    public static final String ACTION_WARRANT = "WARRANT";
    public static final String ACTION_VIOLATION = "VIOLATION";
    public static final String ACTION_STOLEN = "STOLEN";

    // JavaFX Properties for TableView binding
    private final StringProperty officerNameProperty = new SimpleStringProperty();
    private final StringProperty badgeNumberProperty = new SimpleStringProperty();
    private final StringProperty actionProperty = new SimpleStringProperty();
    private final IntegerProperty vehicleIdProperty = new SimpleIntegerProperty();
    private final StringProperty registrationNumberProperty = new SimpleStringProperty();
    private final ObjectProperty<LocalDateTime> timestampProperty = new SimpleObjectProperty<>();
    private final StringProperty actionDisplayProperty = new SimpleStringProperty();
    private final StringProperty formattedTimestampProperty = new SimpleStringProperty();

    /**
     * Default constructor.
     */
    public OfficerLog() {
        super();
        this.timestamp = LocalDateTime.now();
        timestampProperty.set(timestamp);
    }

    /**
     * Constructor for creating an officer log entry.
     *
     * @param officerName        the officer name
     * @param badgeNumber        the badge number
     * @param action             the action performed
     * @param vehicleId          the vehicle ID
     * @param registrationNumber the registration number
     */
    public OfficerLog(String officerName, String badgeNumber, String action, int vehicleId, String registrationNumber) {
        this();
        this.officerName = officerName;
        this.badgeNumber = badgeNumber;
        this.action = action;
        this.vehicleId = vehicleId;
        this.registrationNumber = registrationNumber;
        this.timestamp = LocalDateTime.now();

        officerNameProperty.set(officerName);
        badgeNumberProperty.set(badgeNumber);
        actionProperty.set(action);
        vehicleIdProperty.set(vehicleId);
        registrationNumberProperty.set(registrationNumber);
        timestampProperty.set(timestamp);
        updateDisplayProperties();
    }

    /**
     * Constructor for creating an officer log entry with vehicleId.
     *
     * @param officerName the officer name
     * @param badgeNumber the badge number
     * @param action      the action performed
     * @param vehicleId   the vehicle ID
     */
    public OfficerLog(String officerName, String badgeNumber, String action, int vehicleId) {
        this(officerName, badgeNumber, action, vehicleId, null);
    }

    // ============================================
    // PRIVATE UPDATE METHODS
    // ============================================

    private void updateDisplayProperties() {
        switch (action) {
            case ACTION_VIEW:
                actionDisplayProperty.set("Viewed");
                break;
            case ACTION_REPORT:
                actionDisplayProperty.set("Generated Report");
                break;
            case ACTION_BOLO:
                actionDisplayProperty.set("Issued BOLO Alert");
                break;
            case ACTION_WARRANT:
                actionDisplayProperty.set("Issued Warrant");
                break;
            case ACTION_VIOLATION:
                actionDisplayProperty.set("Issued Violation");
                break;
            case ACTION_STOLEN:
                actionDisplayProperty.set("Reported Stolen");
                break;
            default:
                actionDisplayProperty.set(action);
        }

        formattedTimestampProperty.set(formatTimestamp(timestamp));
    }

    private String formatTimestamp(LocalDateTime ts) {
        if (ts == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return ts.format(formatter);
    }

    // ============================================
    // GETTERS AND SETTERS WITH PROPERTY UPDATES
    // ============================================

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

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
        actionProperty.set(action);
        updateDisplayProperties();
    }

    public StringProperty actionProperty() {
        return actionProperty;
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

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
        timestampProperty.set(timestamp);
        formattedTimestampProperty.set(formatTimestamp(timestamp));
    }

    public ObjectProperty<LocalDateTime> timestampProperty() {
        return timestampProperty;
    }

    public String getActionDisplay() {
        return actionDisplayProperty.get();
    }

    public StringProperty actionDisplayProperty() {
        return actionDisplayProperty;
    }

    public String getFormattedTimestamp() {
        return formattedTimestampProperty.get();
    }

    public StringProperty formattedTimestampProperty() {
        return formattedTimestampProperty;
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
        return officerName + " - " + getActionDisplay() + " - " + getFormattedTimestamp();
    }

    /**
     * Creates a copy of this officer log.
     *
     * @return a new OfficerLog instance
     */
    public OfficerLog copy() {
        OfficerLog copy = new OfficerLog();
        copy.setId(this.id);
        copy.setOfficerName(this.officerName);
        copy.setBadgeNumber(this.badgeNumber);
        copy.setAction(this.action);
        copy.setVehicleId(this.vehicleId);
        copy.setRegistrationNumber(this.registrationNumber);
        copy.setTimestamp(this.timestamp);
        copy.setCreatedAt(this.getCreatedAt());
        copy.setUpdatedAt(this.getUpdatedAt());
        return copy;
    }
}