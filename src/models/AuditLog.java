package models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.IntegerProperty;

/**
 * AuditLog model representing system audit trail entries.
 * Records user actions for security and compliance purposes.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class AuditLog extends BaseEntity {

    // Core fields
    private int id;
    private int userId;
    private String username;
    private String action;
    private LocalDateTime timestamp;
    private String ipAddress;
    private String details;

    // Action type constants
    public static final String ACTION_LOGIN = "LOGIN";
    public static final String ACTION_LOGOUT = "LOGOUT";
    public static final String ACTION_CREATE = "CREATE";
    public static final String ACTION_UPDATE = "UPDATE";
    public static final String ACTION_DELETE = "DELETE";
    public static final String ACTION_VIEW = "VIEW";
    public static final String ACTION_EXPORT = "EXPORT";
    public static final String ACTION_BACKUP = "BACKUP";
    public static final String ACTION_RESTORE = "RESTORE";

    // JavaFX Properties for TableView binding
    private final IntegerProperty userIdProperty = new SimpleIntegerProperty();
    private final StringProperty usernameProperty = new SimpleStringProperty();
    private final StringProperty actionProperty = new SimpleStringProperty();
    private final ObjectProperty<LocalDateTime> timestampProperty = new SimpleObjectProperty<>();
    private final StringProperty ipAddressProperty = new SimpleStringProperty();
    private final StringProperty detailsProperty = new SimpleStringProperty();

    /**
     * Default constructor - sets timestamp to current time.
     */
    public AuditLog() {
        super();
        this.timestamp = LocalDateTime.now();
        timestampProperty.set(this.timestamp);
    }

    /**
     * Constructor for creating an audit log entry.
     *
     * @param userId    the ID of the user performing the action
     * @param username  the username of the user
     * @param action    the action performed
     * @param ipAddress the IP address of the user
     */
    public AuditLog(int userId, String username, String action, String ipAddress) {
        this();
        this.userId = userId;
        this.username = username;
        this.action = action;
        this.ipAddress = ipAddress;

        userIdProperty.set(userId);
        usernameProperty.set(username);
        actionProperty.set(action);
        ipAddressProperty.set(ipAddress);
    }

    /**
     * Constructor with additional details.
     *
     * @param userId    the ID of the user
     * @param username  the username
     * @param action    the action performed
     * @param ipAddress the IP address
     * @param details   additional details about the action
     */
    public AuditLog(int userId, String username, String action, String ipAddress, String details) {
        this(userId, username, action, ipAddress);
        this.details = details;
        detailsProperty.set(details);
    }

    // ============================================
    // GETTERS AND SETTERS WITH PROPERTY UPDATES
    // ============================================

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
        userIdProperty.set(userId);
    }

    public IntegerProperty userIdProperty() {
        return userIdProperty;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
        usernameProperty.set(username);
    }

    public StringProperty usernameProperty() {
        return usernameProperty;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
        actionProperty.set(action);
    }

    public StringProperty actionProperty() {
        return actionProperty;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
        timestampProperty.set(timestamp);
    }

    public ObjectProperty<LocalDateTime> timestampProperty() {
        return timestampProperty;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
        ipAddressProperty.set(ipAddress);
    }

    public StringProperty ipAddressProperty() {
        return ipAddressProperty;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
        detailsProperty.set(details);
    }

    public StringProperty detailsProperty() {
        return detailsProperty;
    }

    // ============================================
    // UTILITY METHODS
    // ============================================

    /**
     * Gets the formatted timestamp for display.
     *
     * @return formatted date-time string
     */
    public String getFormattedTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return timestamp != null ? timestamp.format(formatter) : "";
    }

    /**
     * Gets the action display name.
     *
     * @return human-readable action name
     */
    public String getActionDisplayName() {
        switch (action) {
            case ACTION_LOGIN: return "Login";
            case ACTION_LOGOUT: return "Logout";
            case ACTION_CREATE: return "Create";
            case ACTION_UPDATE: return "Update";
            case ACTION_DELETE: return "Delete";
            case ACTION_VIEW: return "View";
            case ACTION_EXPORT: return "Export";
            case ACTION_BACKUP: return "Backup";
            case ACTION_RESTORE: return "Restore";
            default: return action != null ? action : "Unknown";
        }
    }

    /**
     * Gets the CSS color class for the action.
     *
     * @return CSS class name
     */
    public String getActionColorClass() {
        switch (action) {
            case ACTION_LOGIN: return "info";
            case ACTION_LOGOUT: return "secondary";
            case ACTION_CREATE: return "success";
            case ACTION_UPDATE: return "warning";
            case ACTION_DELETE: return "danger";
            case ACTION_VIEW: return "primary";
            default: return "default";
        }
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
        return username + " - " + getActionDisplayName() + " - " + getFormattedTimestamp();
    }

    /**
     * Creates a copy of this audit log.
     *
     * @return a new AuditLog instance with the same values
     */
    public AuditLog copy() {
        AuditLog copy = new AuditLog();
        copy.setId(this.id);
        copy.setUserId(this.userId);
        copy.setUsername(this.username);
        copy.setAction(this.action);
        copy.setTimestamp(this.timestamp);
        copy.setIpAddress(this.ipAddress);
        copy.setDetails(this.details);
        copy.setCreatedAt(this.getCreatedAt());
        copy.setUpdatedAt(this.getUpdatedAt());
        return copy;
    }
}