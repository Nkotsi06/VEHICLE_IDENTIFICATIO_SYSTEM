package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import database.ProcedureCaller;
import database.ViewLoader;
import models.Admin;
import models.User;

/**
 * AdminDAO - Uses ONLY stored procedures and views for all operations.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class AdminDAO extends BaseDAO<Admin> {

    private final ProcedureCaller procedureCaller;
    private final ViewLoader viewLoader;
    private final UserDAO userDAO;

    public AdminDAO() {
        this.procedureCaller = new ProcedureCaller();
        this.viewLoader = new ViewLoader();
        this.userDAO = new UserDAO();
    }

    @Override
    public Admin findById(int id) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_users",
                "id = ? AND role = 'ADMIN'", id);
        if (results.isEmpty()) return null;

        Admin admin = mapToAdmin(results.get(0));
        User user = userDAO.findById(id);
        if (user != null) {
            admin.setUserId(user.getId());
            admin.setUsername(user.getUsername());
            admin.setFullName(user.getFullName());
            admin.setEmail(user.getEmail());
            admin.setActive(user.isActive());
            admin.setCreatedAt(user.getCreatedAt());
            admin.setUpdatedAt(user.getUpdatedAt());
        }
        return admin;
    }

    public Admin findByUserId(int userId) throws SQLException {
        return findById(userId);
    }

    public Admin findByUsername(String username) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_users",
                "username = ? AND role = 'ADMIN'", username);
        if (results.isEmpty()) return null;

        Admin admin = mapToAdmin(results.get(0));
        User user = userDAO.findByUsername(username);
        if (user != null) {
            admin.setUserId(user.getId());
            admin.setUsername(user.getUsername());
            admin.setFullName(user.getFullName());
            admin.setEmail(user.getEmail());
            admin.setActive(user.isActive());
        }
        return admin;
    }

    @Override
    public List<Admin> findAll() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_users", "role = 'ADMIN' ORDER BY full_name");
        return mapToAdminList(results);
    }

    public List<Admin> findActiveAdmins() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_users",
                "role = 'ADMIN' AND is_active = true ORDER BY full_name");
        return mapToAdminList(results);
    }

    @Override
    public boolean insert(Admin entity) throws SQLException {
        Integer userId = procedureCaller.executeCreateUserWithId(
                entity.getUsername(),
                entity.getPassword(),
                "ADMIN",
                entity.getFullName(),
                entity.getEmail()
        );
        if (userId != null && userId > 0) {
            entity.setUserId(userId);
            return true;
        }
        return false;
    }

    public int insertAndGetId(Admin entity) throws SQLException {
        return procedureCaller.executeCreateUserWithId(
                entity.getUsername(),
                entity.getPassword(),
                "ADMIN",
                entity.getFullName(),
                entity.getEmail()
        );
    }

    @Override
    public boolean update(Admin entity) throws SQLException {
        return procedureCaller.executeUpdateUser(
                entity.getUserId(),
                entity.getUsername(),
                "ADMIN",
                entity.getFullName(),
                entity.getEmail(),
                entity.isActive()
        );
    }

    public boolean updatePassword(int adminId, String newPassword) throws SQLException {
        return procedureCaller.executeUpdateUserPassword(adminId, newPassword);
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return procedureCaller.executeDeleteUser(id);
    }

    public int countAdmins() throws SQLException {
        return viewLoader.countViewRowsWithCondition("vw_users", "role = 'ADMIN' AND is_active = true");
    }

    // ============================================
    // HELPER METHODS FOR MAPPING
    // ============================================

    private Admin mapToAdmin(Map<String, Object> map) {
        if (map == null) return null;

        Admin admin = new Admin();
        if (map.get("id") != null) admin.setUserId(((Number) map.get("id")).intValue());
        if (map.get("username") != null) admin.setUsername(map.get("username").toString());
        if (map.get("full_name") != null) admin.setFullName(map.get("full_name").toString());
        if (map.get("email") != null) admin.setEmail(map.get("email").toString());
        if (map.get("is_active") != null) admin.setActive((Boolean) map.get("is_active"));
        admin.setDepartment("ADMINISTRATION");
        admin.setPosition("SYSTEM_ADMIN");
        admin.setSuperAdmin(true);

        if (map.get("created_at") instanceof java.sql.Timestamp) {
            admin.setCreatedAt(((java.sql.Timestamp) map.get("created_at")).toLocalDateTime());
        }
        if (map.get("updated_at") instanceof java.sql.Timestamp) {
            admin.setUpdatedAt(((java.sql.Timestamp) map.get("updated_at")).toLocalDateTime());
        }
        return admin;
    }

    private List<Admin> mapToAdminList(List<Map<String, Object>> maps) {
        List<Admin> admins = new ArrayList<>();
        if (maps != null) {
            for (Map<String, Object> map : maps) {
                admins.add(mapToAdmin(map));
            }
        }
        return admins;
    }

    @Override
    protected Admin mapRow(ResultSet rs) throws SQLException {
        Admin admin = new Admin();
        admin.setUserId(rs.getInt("id"));
        admin.setUsername(rs.getString("username"));
        admin.setFullName(rs.getString("full_name"));
        admin.setEmail(rs.getString("email"));
        admin.setActive(rs.getBoolean("is_active"));
        admin.setDepartment("ADMINISTRATION");
        admin.setPosition("SYSTEM_ADMIN");
        admin.setSuperAdmin(true);

        if (rs.getTimestamp("created_at") != null) {
            admin.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            admin.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        return admin;
    }
}