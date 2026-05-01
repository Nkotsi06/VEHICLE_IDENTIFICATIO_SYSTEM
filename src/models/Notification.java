package models;

import java.time.LocalDateTime;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Notification extends BaseEntity {
    private int id;
    private int userId;
    private String userName;
    private String message;
    private boolean isRead;
    private String type;
    private int referenceId;
    private LocalDateTime createdAt;

    // JavaFX Properties for TableView binding
    private final StringProperty messageProperty = new SimpleStringProperty();
    private final StringProperty typeProperty = new SimpleStringProperty();
    private final ObjectProperty<LocalDateTime> createdAtProperty = new SimpleObjectProperty<>();
    private final StringProperty readProperty = new SimpleStringProperty();
    private final IntegerProperty userIdProperty = new SimpleIntegerProperty();
    private final StringProperty userNameProperty = new SimpleStringProperty();
    private final IntegerProperty referenceIdProperty = new SimpleIntegerProperty();

    public Notification() {
        super();
        this.isRead = false;
        this.createdAt = LocalDateTime.now();
    }

    public Notification(int userId, String message, String type, int referenceId) {
        this();
        this.userId = userId;
        this.message = message;
        this.type = type;
        this.referenceId = referenceId;
        this.createdAt = LocalDateTime.now();

        // Update properties
        messageProperty.set(message);
        typeProperty.set(type);
        createdAtProperty.set(createdAt);
        userIdProperty.set(userId);
        referenceIdProperty.set(referenceId);
        readProperty.set("Unread");
    }

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
    }

    public StringProperty typeProperty() {
        return typeProperty;
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
        return type + " - " + message.substring(0, Math.min(50, message.length())) + " - " + (isRead ? "Read" : "Unread");
    }
}