package models;

import javafx.beans.property.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * OfficerActivityLog model representing activity logs for police officers.
 * Tracks officer actions for audit and monitoring purposes.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class OfficerActivityLog extends BaseEntity {

    // Core fields
    private int id;
    private int officerId;
    private String officerName;
    private String actionType;
    private String actionDescription;
    private String targetType;
    private int targetId;
    private String targetName;
    private String ipAddress;
    private LocalDateTime createdAt;

    // Action type constants
    public static final String ACTION_LOGIN = "LOGIN";
    public static final String ACTION_LOGOUT = "LOGOUT";
    public static final String ACTION_SEARCH = "SEARCH";
    public static final String ACTION_VIEW = "VIEW";
    public static final String ACTION_UPDATE = "UPDATE";
    public static final String ACTION_DELETE = "DELETE";
    public static final String ACTION_EXPORT = "EXPORT";
    public static final String ACTION_ISSUE_WARRANT = "ISSUE_WARRANT";
    public static final String ACTION_ISSUE_VIOLATION = "ISSUE_VIOLATION";
    public static final String ACTION_GENERATE_BOLO = "GENERATE_BOLO";

    // Target type constants
    public static final String TARGET_VEHICLE = "VEHICLE";
    public static final String TARGET_OWNER = "OWNER";
    public static final String TARGET_VIOLATION = "VIOLATION";
    public static final String TARGET_WARRANT = "WARRANT";
    public static final String TARGET_BOLO = "BOLO";
    public static final String TARGET_REPORT = "REPORT";

    // JavaFX Properties
    private final IntegerProperty officerIdProperty = new SimpleIntegerProperty();
    private final StringProperty officerNameProperty = new SimpleStringProperty();
    private final StringProperty actionTypeProperty = new SimpleStringProperty();
    private final StringProperty actionDescriptionProperty = new SimpleStringProperty();
    private final StringProperty targetTypeProperty = new SimpleStringProperty();
    private final IntegerProperty targetIdProperty = new SimpleIntegerProperty();
    private final StringProperty targetNameProperty = new SimpleStringProperty();
    private final StringProperty ipAddressProperty = new SimpleStringProperty();
    private final ObjectProperty<LocalDateTime> createdAtProperty = new SimpleObjectProperty<>();
    private final StringProperty actionDisplayProperty = new SimpleStringProperty();
    private final StringProperty targetDisplayProperty = new SimpleStringProperty();
    private final StringProperty actionColorProperty = new SimpleStringProperty();

    /**
     * Default constructor - initializes with current timestamp.
     */
    public OfficerActivityLog() {
        super();
        this.createdAt = LocalDateTime.now();
        createdAtProperty.set(createdAt);
    }

    /**
     * Constructor for creating an activity log.
     *
     * @param officerId          the officer ID
     * @param officerName        the officer name
     * @param actionType         the action type
     * @param actionDescription  the action description
     * @param targetType         the target type
     * @param targetId           the target ID
     * @param targetName         the target name
     * @param ipAddress          the IP address
     */
    public OfficerActivityLog(int officerId, String officerName, String actionType,
                              String actionDescription, String targetType, int targetId,
                              String targetName, String ipAddress) {
        this();
        this.officerId = officerId;
        this.officerName = officerName;
        this.actionType = actionType;
        this.actionDescription = actionDescription;
        this.targetType = targetType;
        this.targetId = targetId;
        this.targetName = targetName;
        this.ipAddress = ipAddress;

        officerIdProperty.set(officerId);
        officerNameProperty.set(officerName);
        actionTypeProperty.set(actionType);
        actionDescriptionProperty.set(actionDescription);
        targetTypeProperty.set(targetType);
        targetIdProperty.set(targetId);
        targetNameProperty.set(targetName);
        ipAddressProperty.set(ipAddress);
        updateDisplayProperties();
    }

    // ============================================
    // PRIVATE UPDATE METHODS
    // ============================================

    private void updateDisplayProperties() {
        // Update action display
        switch (actionType) {
            case ACTION_LOGIN:
                actionDisplayProperty.set("Login");
                actionColorProperty.set("#4CAF50");
                break;
            case ACTION_LOGOUT:
                actionDisplayProperty.set("Logout");
                actionColorProperty.set("#9E9E9E");
                break;
            case ACTION_SEARCH:
                actionDisplayProperty.set("Search");
                actionColorProperty.set("#2196F3");
                break;
            case ACTION_VIEW:
                actionDisplayProperty.set("View");
                actionColorProperty.set("#00BCD4");
                break;
            case ACTION_UPDATE:
                actionDisplayProperty.set("Update");
                actionColorProperty.set("#FF9800");
                break;
            case ACTION_DELETE:
                actionDisplayProperty.set("Delete");
                actionColorProperty.set("#F44336");
                break;
            case ACTION_EXPORT:
                actionDisplayProperty.set("Export");
                actionColorProperty.set("#9C27B0");
                break;
            case ACTION_ISSUE_WARRANT:
                actionDisplayProperty.set("Issue Warrant");
                actionColorProperty.set("#F44336");
                break;
            case ACTION_ISSUE_VIOLATION:
                actionDisplayProperty.set("Issue Violation");
                actionColorProperty.set("#FF9800");
                break;
            case ACTION_GENERATE_BOLO:
                actionDisplayProperty.set("Generate BOLO");
                actionColorProperty.set("#E91E63");
                break;
            default:
                actionDisplayProperty.set(actionType);
                actionColorProperty.set("#9E9E9E");
        }

        // Update target display
        switch (targetType) {
            case TARGET_VEHICLE:
                targetDisplayProperty.set("Vehicle");
                break;
            case TARGET_OWNER:
                targetDisplayProperty.set("Owner");
                break;
            case TARGET_VIOLATION:
                targetDisplayProperty.set("Violation");
                break;
            case TARGET_WARRANT:
                targetDisplayProperty.set("Warrant");
                break;
            case TARGET_BOLO:
                targetDisplayProperty.set("BOLO Alert");
                break;
            case TARGET_REPORT:
                targetDisplayProperty.set("Report");
                break;
            default:
                targetDisplayProperty.set(targetType);
        }
    }

    // ============================================
    // GETTERS AND SETTERS WITH PROPERTY UPDATES
    // ============================================

    public int getOfficerId() { return officerId; }
    public void setOfficerId(int value) {
        officerId = value;
        officerIdProperty.set(value);
    }
    public IntegerProperty officerIdProperty() { return officerIdProperty; }

    public String getOfficerName() { return officerName; }
    public void setOfficerName(String value) {
        officerName = value;
        officerNameProperty.set(value);
    }
    public StringProperty officerNameProperty() { return officerNameProperty; }

    public String getActionType() { return actionType; }
    public void setActionType(String value) {
        actionType = value;
        actionTypeProperty.set(value);
        updateDisplayProperties();
    }
    public StringProperty actionTypeProperty() { return actionTypeProperty; }

    public String getActionDescription() { return actionDescription; }
    public void setActionDescription(String value) {
        actionDescription = value;
        actionDescriptionProperty.set(value);
    }
    public StringProperty actionDescriptionProperty() { return actionDescriptionProperty; }

    public String getTargetType() { return targetType; }
    public void setTargetType(String value) {
        targetType = value;
        targetTypeProperty.set(value);
        updateDisplayProperties();
    }
    public StringProperty targetTypeProperty() { return targetTypeProperty; }

    public int getTargetId() { return targetId; }
    public void setTargetId(int value) {
        targetId = value;
        targetIdProperty.set(value);
    }
    public IntegerProperty targetIdProperty() { return targetIdProperty; }

    public String getTargetName() { return targetName; }
    public void setTargetName(String value) {
        targetName = value;
        targetNameProperty.set(value);
    }
    public StringProperty targetNameProperty() { return targetNameProperty; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String value) {
        ipAddress = value;
        ipAddressProperty.set(value);
    }
    public StringProperty ipAddressProperty() { return ipAddressProperty; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) {
        createdAt = value;
        createdAtProperty.set(value);
    }
    public ObjectProperty<LocalDateTime> createdAtProperty() { return createdAtProperty; }

    public String getActionDisplay() { return actionDisplayProperty.get(); }
    public StringProperty actionDisplayProperty() { return actionDisplayProperty; }

    public String getActionColor() { return actionColorProperty.get(); }
    public StringProperty actionColorProperty() { return actionColorProperty; }

    public String getTargetDisplay() { return targetDisplayProperty.get(); }
    public StringProperty targetDisplayProperty() { return targetDisplayProperty; }

    // ============================================
    // BUSINESS LOGIC METHODS
    // ============================================

    public String getFormattedCreatedAt() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return createdAt != null ? createdAt.format(formatter) : "";
    }

    public String getFormattedDateTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return createdAt != null ? createdAt.format(formatter) : "";
    }

    // ============================================
    // OVERRIDE METHODS
    // ============================================

    @Override
    public int getId() { return id; }
    @Override
    public void setId(int id) { this.id = id; }

    @Override
    public String toString() {
        return officerName + " - " + getActionDisplay() + " - " + getFormattedCreatedAt();
    }

    /**
     * Creates a copy of this activity log.
     *
     * @return a new OfficerActivityLog instance
     */
    public OfficerActivityLog copy() {
        OfficerActivityLog copy = new OfficerActivityLog();
        copy.setId(this.id);
        copy.setOfficerId(this.officerId);
        copy.setOfficerName(this.officerName);
        copy.setActionType(this.actionType);
        copy.setActionDescription(this.actionDescription);
        copy.setTargetType(this.targetType);
        copy.setTargetId(this.targetId);
        copy.setTargetName(this.targetName);
        copy.setIpAddress(this.ipAddress);
        copy.setCreatedAt(this.createdAt);
        copy.setCreatedAt(this.getCreatedAt());
        copy.setUpdatedAt(this.getUpdatedAt());
        return copy;
    }
}