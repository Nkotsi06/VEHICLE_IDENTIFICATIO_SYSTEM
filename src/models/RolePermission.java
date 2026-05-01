package models;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import java.time.LocalDateTime;

public class RolePermission {
    private IntegerProperty id = new SimpleIntegerProperty();
    private StringProperty roleName = new SimpleStringProperty();
    private StringProperty permissionKey = new SimpleStringProperty();
    private BooleanProperty permissionValue = new SimpleBooleanProperty(false);
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public RolePermission() {}

    public RolePermission(String roleName, String permissionKey, boolean permissionValue) {
        this.roleName.set(roleName);
        this.permissionKey.set(permissionKey);
        this.permissionValue.set(permissionValue);
    }

    // ID
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }
    public IntegerProperty idProperty() { return id; }

    // Role Name
    public String getRoleName() { return roleName.get(); }
    public void setRoleName(String roleName) { this.roleName.set(roleName); }
    public StringProperty roleNameProperty() { return roleName; }

    // Permission Key
    public String getPermissionKey() { return permissionKey.get(); }
    public void setPermissionKey(String permissionKey) { this.permissionKey.set(permissionKey); }
    public StringProperty permissionKeyProperty() { return permissionKey; }

    // Permission Value (hasPermission)
    public boolean hasPermission() { return permissionValue.get(); }
    public void setPermissionValue(boolean value) { this.permissionValue.set(value); }
    public BooleanProperty permissionValueProperty() { return permissionValue; }

    // Created At
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Updated At
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "RolePermission{" +
                "roleName='" + getRoleName() + '\'' +
                ", permissionKey='" + getPermissionKey() + '\'' +
                ", hasPermission=" + hasPermission() +
                '}';
    }
}