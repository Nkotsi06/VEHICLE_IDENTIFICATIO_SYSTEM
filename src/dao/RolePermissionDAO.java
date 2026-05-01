package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import models.RolePermission;

public class RolePermissionDAO extends BaseDAO<RolePermission> {

    @Override
    public RolePermission findById(int id) throws SQLException {
        String sql = "SELECT * FROM role_permissions WHERE id = ?";
        return executeQuerySingle(sql, id);
    }

    @Override
    public List<RolePermission> findAll() throws SQLException {
        String sql = "SELECT * FROM role_permissions ORDER BY role_name, permission_key";
        return executeQuery(sql);
    }

    public List<RolePermission> findByRole(String roleName) throws SQLException {
        String sql = "SELECT * FROM role_permissions WHERE role_name = ? ORDER BY permission_key";
        return executeQuery(sql, roleName);
    }

    public boolean hasPermission(String roleName, String permissionKey) throws SQLException {
        String sql = "SELECT permission_value FROM role_permissions WHERE role_name = ? AND permission_key = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, roleName);
            ps.setString(2, permissionKey);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getBoolean("permission_value");
            }
            return false;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    @Override
    public boolean insert(RolePermission entity) throws SQLException {
        String sql = "INSERT INTO role_permissions (role_name, permission_key, permission_value) VALUES (?, ?, ?) ON CONFLICT (role_name, permission_key) DO NOTHING";
        int result = executeUpdate(sql, entity.getRoleName(), entity.getPermissionKey(), entity.hasPermission());
        return result > 0;
    }

    @Override
    public boolean update(RolePermission entity) throws SQLException {
        String sql = "UPDATE role_permissions SET permission_value = ? WHERE role_name = ? AND permission_key = ?";
        int result = executeUpdate(sql, entity.hasPermission(), entity.getRoleName(), entity.getPermissionKey());
        return result > 0;
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM role_permissions WHERE id = ?";
        int result = executeUpdate(sql, id);
        return result > 0;
    }

    public boolean deleteByRoleAndPermission(String roleName, String permissionKey) throws SQLException {
        String sql = "DELETE FROM role_permissions WHERE role_name = ? AND permission_key = ?";
        int result = executeUpdate(sql, roleName, permissionKey);
        return result > 0;
    }

    public boolean grantPermission(String roleName, String permissionKey) throws SQLException {
        String sql = "INSERT INTO role_permissions (role_name, permission_key, permission_value) VALUES (?, ?, true) ON CONFLICT (role_name, permission_key) DO UPDATE SET permission_value = true";
        int result = executeUpdate(sql, roleName, permissionKey);
        return result > 0;
    }

    public boolean revokePermission(String roleName, String permissionKey) throws SQLException {
        String sql = "UPDATE role_permissions SET permission_value = false WHERE role_name = ? AND permission_key = ?";
        int result = executeUpdate(sql, roleName, permissionKey);
        return result > 0;
    }

    @Override
    protected RolePermission mapRow(ResultSet rs) throws SQLException {
        RolePermission permission = new RolePermission();
        // FIXED: Changed from rs.getInt("id") to handle potential column name
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