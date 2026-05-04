package models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Notification model representing system notifications for users.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class Notification extends BaseEntity {

    // Core fields
    private int id;
    private int userId;
    private String userName;
    private String message;
    private boolean isRead;
    private String type;
    private int referenceId;
    private LocalDateTime createdAt;

    // Notification type constants
    public static final String TYPE_INFO = "INFO";
    public static final String TYPE_SUCCESS = "SUCCESS";
    public static final String TYPE_WARNING = "WARNING";
    public static final String TYPE_ERROR = "ERROR";
    public static final String TYPE_ALERT = "ALERT";
    public static final String TYPE_REMINDER = "REMINDER";

    // JavaFX Properties for TableView binding
    private final StringProperty messageProperty = new SimpleStringProperty();
    private final StringProperty typeProperty = new SimpleStringProperty();
    private final ObjectProperty<LocalDateTime> createdAtProperty = new SimpleObjectProperty<>();
    private final StringProperty readProperty = new SimpleStringProperty();
    private final IntegerProperty userIdProperty = new SimpleIntegerProperty();
    private final StringProperty userNameProperty = new SimpleStringProperty();
    private final IntegerProperty referenceIdProperty = new SimpleIntegerProperty();
    private final BooleanProperty readBooleanProperty = new SimpleBooleanProperty();
    private final StringProperty typeDisplayProperty = new SimpleStringProperty();
    private final StringProperty typeColorProperty = new SimpleStringProperty();

    /**
     * Default constructor - initializes with unread status and current time.
     */
    public Notification() {
        super();
        this.isRead = false;
        this.createdAt = LocalDateTime.now();

        readProperty.set("Unread");
        readBooleanProperty.set(false);
        createdAtProperty.set(createdAt);
        updateTypeDisplay();
    }

    /**
     * Constructor for creating a new notification.
     *
     * @param userId      the user ID
     * @param message     the notification message
     * @param type        the notification type
     * @param referenceId the reference ID (optional)
     */
    public Notification(int userId, String message, String type, int referenceId) {
        this();
        this.userId = userId;
        this.message = message;
        this.type = type;
        this.referenceId = referenceId;
        this.createdAt = LocalDateTime.now();

        messageProperty.set(message);
        typeProperty.set(type);
        createdAtProperty.set(createdAt);
        userIdProperty.set(userId);
        referenceIdProperty.set(referenceId);
        updateTypeDisplay();
    }

    // ============================================
    // PRIVATE UPDATE METHODS
    // ============================================

    private void updateTypeDisplay() {
        switch (type) {
            case TYPE_INFO:
                typeDisplayProperty.set("Information");
                typeColorProperty.set("#2196F3");
                break;
            case TYPE_SUCCESS:
                typeDisplayProperty.set("Success");
                typeColorProperty.set("#4CAF50");
                break;
            case TYPE_WARNING:
                typeDisplayProperty.set("Warning");
                typeColorProperty.set("#FF9800");
                break;
            case TYPE_ERROR:
                typeDisplayProperty.set("Error");
                typeColorProperty.set("#F44336");
                break;
            case TYPE_ALERT:
                typeDisplayProperty.set("Alert");
                typeColorProperty.set("#9C27B0");
                break;
            case TYPE_REMINDER:
                typeDisplayProperty.set("Reminder");
                typeColorProperty.set("#00BCD4");
                break;
            default:
                typeDisplayProperty.set(type);
                typeColorProperty.set("#9E9E9E");
        }
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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
        userNameProperty.set(userName);
    }

    public StringProperty userNameProperty() {
        return userNameProperty;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
        messageProperty.set(message);
    }

    public StringProperty messageProperty() {
        return messageProperty;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
        readProperty.set(read ? "Read" : "Unread");
        readBooleanProperty.set(read);
    }

    public BooleanProperty readBooleanProperty() {
        return readBooleanProperty;
    }

    public StringProperty readProperty() {
        return readProperty;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
        typeProperty.set(type);
        updateTypeDisplay();
    }

    public StringProperty typeProperty() {
        return typeProperty;
    }

    public StringProperty typeDisplayProperty() {
        return typeDisplayProperty;
    }

    public StringProperty typeColorProperty() {
        return typeColorProperty;
    }

    public int getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(int referenceId) {
        this.referenceId = referenceId;
        referenceIdProperty.set(referenceId);
    }

    public IntegerProperty referenceIdProperty() {
        return referenceIdProperty;
    }

    @Override
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        createdAtProperty.set(createdAt);
    }

    public ObjectProperty<LocalDateTime> createdAtProperty() {
        return createdAtProperty;
    }

    // ============================================
    // BUSINESS LOGIC METHODS
    // ============================================

    public String getTypeDisplay() {
        return typeDisplayProperty.get();
    }

    public String getTypeColor() {
        return typeColorProperty.get();
    }

    public String getFormattedCreatedAt() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return createdAt != null ? createdAt.format(formatter) : "";
    }

    public String getRelativeTime() {
        if (createdAt == null) return "";
        LocalDateTime now = LocalDateTime.now();
        long seconds = java.time.Duration.between(createdAt, now).getSeconds();

        if (seconds < 60) {
            return seconds + " seconds ago";
        } else if (seconds < 3600) {
            return (seconds / 60) + " minutes ago";
        } else if (seconds < 86400) {
            return (seconds / 3600) + " hours ago";
        } else {
            return (seconds / 86400) + " days ago";
        }
    }

    public String getMessagePreview() {
        if (message == null) return "";
        if (message.length() <= 100) return message;
        return message.substring(0, 100) + "...";
    }

    public void markAsRead() {
        setRead(true);
    }

    public void markAsUnread() {
        setRead(false);
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
        return getTypeDisplay() + " - " + getMessagePreview() + " - " + (isRead ? "Read" : "Unread");
    }

    /**
     * Creates a copy of this notification.
     *
     * @return a new Notification instance
     */
    public Notification copy() {
        Notification copy = new Notification();
        copy.setId(this.id);
        copy.setUserId(this.userId);
        copy.setUserName(this.userName);
        copy.setMessage(this.message);
        copy.setRead(this.isRead);
        copy.setType(this.type);
        copy.setReferenceId(this.referenceId);
        copy.setCreatedAt(this.createdAt);
        copy.setCreatedAt(this.getCreatedAt());
        copy.setUpdatedAt(this.getUpdatedAt());
        return copy;
    }
}