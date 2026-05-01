package models;

import java.time.LocalDateTime;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class AuditLog extends BaseEntity {
    private int id;
    private int userId;
    private String username;
    private String action;
    private LocalDateTime timestamp;
    private String ipAddress;

    // JavaFX Properties for TableView binding
    private final StringProperty usernameProperty = new SimpleStringProperty();
    private final StringProperty actionProperty = new SimpleStringProperty();
    private final ObjectProperty<LocalDateTime> timestampProperty = new SimpleObjectProperty<>();
    private final StringProperty ipAddressProperty = new SimpleStringProperty();

    public AuditLog() {
        super();
        this.timestamp = LocalDateTime.now();
    }

    public AuditLog(int userId, String action, String ipAddress) {
        this();
        this.userId = userId;
        this.action = action;
        this.ipAddress = ipAddress;
        this.timestamp = LocalDateTime.now();
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
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
        return username + " - " + action + " - " + timestamp;
    }
}