package models;

import java.time.LocalDateTime;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.IntegerProperty;

/**
 * Admin model representing system administrators.
 * Extends BaseEntity and provides JavaFX property bindings for UI components.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class Admin extends BaseEntity {

    // Core fields
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

    // Department constants
    public static final String DEPT_USER_MANAGEMENT = "USER_MANAGEMENT";
    public static final String DEPT_AUDIT = "AUDIT";
    public static final String DEPT_SYSTEM = "SYSTEM";
    public static final String DEPT_SUPPORT = "SUPPORT";

    // JavaFX Properties for TableView binding
    private final StringProperty usernameProperty = new SimpleStringProperty();
    private final StringProperty fullNameProperty = new SimpleStringProperty();
    private final StringProperty emailProperty = new SimpleStringProperty();
    private final StringProperty departmentProperty = new SimpleStringProperty();
    private final StringProperty positionProperty = new SimpleStringProperty();
    private final BooleanProperty activeProperty = new SimpleBooleanProperty();
    private final BooleanProperty superAdminProperty = new SimpleBooleanProperty();
    private final IntegerProperty userIdProperty = new SimpleIntegerProperty();

    /**
     * Default constructor - initializes default values.
     */
    public Admin() {
        super();
        this.isSuperAdmin = false;
        this.isActive = true;
        this.department = DEPT_SYSTEM;
        this.position = "Administrator";

        // Initialize properties
        activeProperty.set(true);
        superAdminProperty.set(false);
    }

    /**
     * Constructor for creating an admin with basic info.
     *
     * @param userId    the associated user ID
     * @param department the admin's department
     * @param position   the admin's position
     */
    public Admin(int userId, String department, String position) {
        this();
        this.userId = userId;
        this.department = department;
        this.position = position;
        userIdProperty.set(userId);
        departmentProperty.set(department);
        positionProperty.set(position);
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
        fullNameProperty.set(fullName);
    }

    public StringProperty fullNameProperty() {
        return fullNameProperty;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
        emailProperty.set(email);
    }

    public StringProperty emailProperty() {
        return emailProperty;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
        departmentProperty.set(department);
    }

    public StringProperty departmentProperty() {
        return departmentProperty;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
        positionProperty.set(position);
    }

    public StringProperty positionProperty() {
        return positionProperty;
    }

    public boolean isSuperAdmin() {
        return isSuperAdmin;
    }

    public void setSuperAdmin(boolean superAdmin) {
        isSuperAdmin = superAdmin;
        superAdminProperty.set(superAdmin);
    }

    public BooleanProperty superAdminProperty() {
        return superAdminProperty;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
        activeProperty.set(active);
    }

    public BooleanProperty activeProperty() {
        return activeProperty;
    }

    // ============================================
    // BUSINESS LOGIC METHODS
    // ============================================

    /**
     * Checks if the admin can manage users.
     *
     * @return true if super admin or in user management department
     */
    public boolean canManageUsers() {
        return isSuperAdmin || DEPT_USER_MANAGEMENT.equals(department);
    }

    /**
     * Checks if the admin can view audit logs.
     *
     * @return true if super admin or in audit department
     */
    public boolean canViewAuditLogs() {
        return isSuperAdmin || DEPT_AUDIT.equals(department);
    }

    /**
     * Checks if the admin can manage system settings.
     *
     * @return true if super admin or in system department
     */
    public boolean canManageSystem() {
        return isSuperAdmin || DEPT_SYSTEM.equals(department);
    }

    /**
     * Gets the display name for the admin.
     *
     * @return formatted display name
     */
    public String getDisplayName() {
        return fullName + " (" + position + ")";
    }

    /**
     * Gets the role display string.
     *
     * @return role description
     */
    public String getRoleDisplay() {
        if (isSuperAdmin) {
            return "Super Administrator";
        }
        return department.replace("_", " ") + " Administrator";
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
        return fullName + " - " + position + " (" + department + ")";
    }

    /**
     * Creates a copy of this admin object.
     *
     * @return a new Admin instance with the same values
     */
    public Admin copy() {
        Admin copy = new Admin();
        copy.setId(this.id);
        copy.setUserId(this.userId);
        copy.setUsername(this.username);
        copy.setPassword(this.password);
        copy.setFullName(this.fullName);
        copy.setEmail(this.email);
        copy.setDepartment(this.department);
        copy.setPosition(this.position);
        copy.setSuperAdmin(this.isSuperAdmin);
        copy.setActive(this.isActive);
        copy.setCreatedAt(this.getCreatedAt());
        copy.setUpdatedAt(this.getUpdatedAt());
        return copy;
    }
}