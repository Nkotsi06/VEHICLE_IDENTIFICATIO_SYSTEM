package models;

import java.time.LocalDateTime;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Admin extends BaseEntity {
    private int id;
    private int userId;
    private String username;
    private String password;
    private String fullName;
    private String email;
    private String department;
    private String position;
    private boolean isSuperAdmin;
    private boolean isActive;

    // JavaFX Properties
    private final StringProperty usernameProperty = new SimpleStringProperty();
    private final StringProperty fullNameProperty = new SimpleStringProperty();
    private final StringProperty emailProperty = new SimpleStringProperty();
    private final BooleanProperty activeProperty = new SimpleBooleanProperty();

    public Admin() {
        super();
        this.isSuperAdmin = false;
        this.isActive = true;
    }

    public Admin(int userId, String department, String position) {
        this();
        this.userId = userId;
        this.department = department;
        this.position = position;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) {
        this.username = username;
        usernameProperty.set(username);
    }
    public StringProperty usernameProperty() { return usernameProperty; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) {
        this.fullName = fullName;
        fullNameProperty.set(fullName);
    }
    public StringProperty fullNameProperty() { return fullNameProperty; }

    public String getEmail() { return email; }
    public void setEmail(String email) {
        this.email = email;
        emailProperty.set(email);
    }
    public StringProperty emailProperty() { return emailProperty; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public boolean isSuperAdmin() { return isSuperAdmin; }
    public void setSuperAdmin(boolean superAdmin) { isSuperAdmin = superAdmin; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) {
        isActive = active;
        activeProperty.set(active);
    }
    public BooleanProperty activeProperty() { return activeProperty; }

    public boolean canManageUsers() {
        return isSuperAdmin || "USER_MANAGEMENT".equals(department);
    }

    public boolean canViewAuditLogs() {
        return isSuperAdmin || "AUDIT".equals(department);
    }

    @Override
    public int getId() { return id; }
    @Override
    public void setId(int id) { this.id = id; }

    @Override
    public String toString() {
        return fullName + " - " + position + " (" + department + ")";
    }
}