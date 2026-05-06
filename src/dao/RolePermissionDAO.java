package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import database.ProcedureCaller;
import database.ViewLoader;
import models.RolePermission;

/**
 * RolePermissionDAO - Uses ONLY stored procedures and views for all operations.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class RolePermissionDAO extends BaseDAO<RolePermission> {

    private final ProcedureCaller procedureCaller;
    private final ViewLoader viewLoader;

    public RolePermissionDAO() {
        this.procedureCaller = new ProcedureCaller();
        this.viewLoader = new ViewLoader();
    }

    @Override
    public RolePermission findById(int id) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_role_permissions", "id = ?", id);
        if (results.isEmpty()) {
            return null;
        }
        return mapMapToRolePermission(results.get(0));
    }

    @Override
    public List<RolePermission> findAll() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadView("vw_role_permissions");
        return mapMapsToRolePermissions(results);
    }

    public List<RolePermission> findByRole(String roleName) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_role_permissions", "role_name = ? ORDER BY permission_key", roleName);
        return mapMapsToRolePermissions(results);
    }

    public boolean hasPermission(String roleName, String permissionKey) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_role_permissions",
                "role_name = ? AND permission_key = ?", roleName, permissionKey);
        if (results.isEmpty()) return false;
        Boolean value = (Boolean) results.get(0).get("permission_value");
        return value != null && value;
    }

    @Override
    public boolean insert(RolePermission entity) throws SQLException {
        return procedureCaller.executeInsertRolePermission(
                entity.getRoleName(),
                entity.getPermissionKey(),
                entity.hasPermission()
        );
    }

    @Override
    public boolean update(RolePermission entity) throws SQLException {
        return procedureCaller.executeUpdateRolePermission(
                entity.getRoleName(),
                entity.getPermissionKey(),
                entity.hasPermission()
        );
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return procedureCaller.executeDeleteRolePermission(id);
    }

    public boolean deleteByRoleAndPermission(String roleName, String permissionKey) throws SQLException {
        return procedureCaller.executeDeleteRolePermissionByKey(roleName, permissionKey);
    }

    public boolean grantPermission(String roleName, String permissionKey) throws SQLException {
        return procedureCaller.executeGrantPermission(roleName, permissionKey);
    }

    public boolean revokePermission(String roleName, String permissionKey) throws SQLException {
        return procedureCaller.executeRevokePermission(roleName, permissionKey);
    }

    /**
     * Converts a List of Maps to a List of RolePermission objects.
     */
    private List<RolePermission> mapMapsToRolePermissions(List<Map<String, Object>> maps) {
        List<RolePermission> permissions = new ArrayList<>();
        if (maps == null) {
            return permissions;
        }
        for (Map<String, Object> map : maps) {
            RolePermission permission = mapMapToRolePermission(map);
            if (permission != null) {
                permissions.add(permission);
            }
        }
        return permissions;
    }

    /**
     * Converts a Map to a RolePermission object.
     */
    private RolePermission mapMapToRolePermission(Map<String, Object> map) {
        if (map == null) {
            return null;
        }

        RolePermission permission = new RolePermission();

        permission.setId(getIntValue(map, "id"));
        permission.setRoleName(getStringValue(map, "role_name"));
        permission.setPermissionKey(getStringValue(map, "permission_key"));

        Boolean value = (Boolean) map.get("permission_value");
        permission.setPermissionValue(value != null && value);

        permission.setCreatedAt(getLocalDateTimeValue(map, "created_at"));
        permission.setUpdatedAt(getLocalDateTimeValue(map, "updated_at"));

        return permission;
    }

    private Integer getIntValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return 0;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Long) return ((Long) value).intValue();
        if (value instanceof Number) return ((Number) value).intValue();
        return 0;
    }

    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "";
    }

    private LocalDateTime getLocalDateTimeValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof java.sql.Timestamp) return ((java.sql.Timestamp) value).toLocalDateTime();
        if (value instanceof LocalDateTime) return (LocalDateTime) value;
        return null;
    }

    @Override
    protected RolePermission mapRow(ResultSet rs) throws SQLException {
        RolePermission permission = new RolePermission();
        try {
            permission.setId(rs.getInt("id"));
        } catch (SQLException e) {
            permission.setId(0);
        }
        permission.setRoleName(rs.getString("role_name"));
        permission.setPermissionKey(rs.getString("permission_key"));
        permission.setPermissionValue(rs.getBoolean("permission_value"));

        try {
            if (rs.getTimestamp("created_at") != null) {
                permission.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            }
            if (rs.getTimestamp("updated_at") != null) {
                permission.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
            }
        } catch (SQLException e) {
            // Ignore if timestamps don't exist
        }
        return permission;
    }
}