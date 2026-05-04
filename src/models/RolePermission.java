package models;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * RolePermission model representing permissions assigned to user roles.
 * Used for Role-Based Access Control (RBAC).
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class RolePermission extends BaseEntity {

    // Core fields
    private IntegerProperty id = new SimpleIntegerProperty();
    private StringProperty roleName = new SimpleStringProperty();
    private StringProperty permissionKey = new SimpleStringProperty();
    private BooleanProperty permissionValue = new SimpleBooleanProperty(false);
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Common permission keys
    public static final String PERMISSION_VIEW_DASHBOARD = "VIEW_DASHBOARD";
    public static final String PERMISSION_MANAGE_USERS = "MANAGE_USERS";
    public static final String PERMISSION_VIEW_AUDIT_LOGS = "VIEW_AUDIT_LOGS";
    public static final String PERMISSION_MANAGE_VEHICLES = "MANAGE_VEHICLES";
    public static final String PERMISSION_VIEW_REPORTS = "VIEW_REPORTS";
    public static final String PERMISSION_EXPORT_DATA = "EXPORT_DATA";
    public static final String PERMISSION_MANAGE_BACKUPS = "MANAGE_BACKUPS";
    public static final String PERMISSION_ISSUE_VIOLATIONS = "ISSUE_VIOLATIONS";
    public static final String PERMISSION_ISSUE_WARRANTS = "ISSUE_WARRANTS";
    public static final String PERMISSION_MANAGE_BOLO = "MANAGE_BOLO";
    public static final String PERMISSION_VIEW_GEOFENCING = "VIEW_GEOFENCING";

    // Role constants
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_POLICE = "POLICE";
    public static final String ROLE_CUSTOMER = "CUSTOMER";
    public static final String ROLE_WORKSHOP = "WORKSHOP";
    public static final String ROLE_INSURANCE = "INSURANCE";

    // Computed properties
    private final StringProperty permissionDisplayProperty = new SimpleStringProperty();

    /**
     * Default constructor.
     */
    public RolePermission() {
        super();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        permissionValue.addListener((obs, oldVal, newVal) -> updatePermissionDisplay());
        updatePermissionDisplay();
    }

    /**
     * Constructor with all fields.
     *
     * @param roleName        the role name
     * @param permissionKey   the permission key
     * @param permissionValue whether the permission is granted
     */
    public RolePermission(String roleName, String permissionKey, boolean permissionValue) {
        this();
        this.roleName.set(roleName);
        this.permissionKey.set(permissionKey);
        this.permissionValue.set(permissionValue);
    }

    // ============================================
    // PRIVATE UPDATE METHODS
    // ============================================

    private void updatePermissionDisplay() {
        permissionDisplayProperty.set(permissionValue.get() ? "Granted" : "Denied");
    }

    // ============================================
    // PROPERTY ACCESSORS
    // ============================================

    public IntegerProperty idProperty() { return id; }

    public StringProperty roleNameProperty() { return roleName; }

    public StringProperty permissionKeyProperty() { return permissionKey; }

    public BooleanProperty permissionValueProperty() { return permissionValue; }

    // ============================================
    // GETTERS AND SETTERS (ONLY @Override versions)
    // ============================================

    // REMOVED: duplicate getId() and setId() methods
    // Only keeping the @Override versions from BaseEntity

    public String getRoleName() { return roleName.get(); }
    public void setRoleName(String roleName) { this.roleName.set(roleName); }

    public String getPermissionKey() { return permissionKey.get(); }
    public void setPermissionKey(String permissionKey) { this.permissionKey.set(permissionKey); }

    public boolean hasPermission() { return permissionValue.get(); }
    public void setPermissionValue(boolean value) { this.permissionValue.set(value); }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getPermissionDisplay() { return permissionDisplayProperty.get(); }
    public StringProperty permissionDisplayProperty() { return permissionDisplayProperty; }

    // ============================================
    // BUSINESS LOGIC METHODS
    // ============================================

    public String getFormattedCreatedAt() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return createdAt != null ? createdAt.format(formatter) : "";
    }

    public String getFormattedUpdatedAt() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return updatedAt != null ? updatedAt.format(formatter) : "";
    }

    public String getPermissionKeyDisplay() {
        return permissionKey.get().replace("_", " ");
    }

    // ============================================
    // OVERRIDE METHODS (from BaseEntity)
    // ============================================

    @Override
    public int getId() {
        return id.get();
    }

    @Override
    public void setId(int id) {
        this.id.set(id);
    }

    @Override
    public String toString() {
        return "RolePermission{" +
                "roleName='" + getRoleName() + '\'' +
                ", permissionKey='" + getPermissionKey() + '\'' +
                ", hasPermission=" + hasPermission() +
                '}';
    }

    /**
     * Creates a copy of this role permission.
     *
     * @return a new RolePermission instance
     */
    public RolePermission copy() {
        RolePermission copy = new RolePermission();
        copy.setId(this.getId());
        copy.setRoleName(this.getRoleName());
        copy.setPermissionKey(this.getPermissionKey());
        copy.setPermissionValue(this.hasPermission());
        copy.setCreatedAt(this.getCreatedAt());
        copy.setUpdatedAt(this.getUpdatedAt());
        return copy;
    }
}