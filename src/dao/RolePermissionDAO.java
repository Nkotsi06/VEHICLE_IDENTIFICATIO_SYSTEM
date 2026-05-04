package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

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
        List<RolePermission> results = viewLoader.loadViewWithCondition("vw_role_permissions", "id = ?", id);
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public List<RolePermission> findAll() throws SQLException {
        return viewLoader.loadView("vw_role_permissions");
    }

    public List<RolePermission> findByRole(String roleName) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_role_permissions", "role_name = ? ORDER BY permission_key", roleName);
    }

    public boolean hasPermission(String roleName, String permissionKey) throws SQLException {
        List<RolePermission> results = viewLoader.loadViewWithCondition("vw_role_permissions",
                "role_name = ? AND permission_key = ?", roleName, permissionKey);
        if (results.isEmpty()) return false;
        return results.get(0).hasPermission();
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