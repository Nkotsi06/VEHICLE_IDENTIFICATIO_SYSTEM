package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import database.ProcedureCaller;
import database.ViewLoader;
import models.User;

/**
 * UserDAO - Uses ONLY stored procedures and views for all operations.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class UserDAO extends BaseDAO<User> {

    private final ProcedureCaller procedureCaller;
    private final ViewLoader viewLoader;

    public UserDAO() {
        this.procedureCaller = new ProcedureCaller();
        this.viewLoader = new ViewLoader();
    }

    @Override
    public User findById(int id) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_users", "id = ?", id);
        return results.isEmpty() ? null : mapToUser(results.get(0));
    }

    public User findByUsername(String username) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_users", "username = ?", username);
        return results.isEmpty() ? null : mapToUser(results.get(0));
    }

    public User findByEmail(String email) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_users", "email = ?", email);
        return results.isEmpty() ? null : mapToUser(results.get(0));
    }

    public User login(String username, String password) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_users",
                "username = ? AND password = ? AND is_active = true", username, password);
        return results.isEmpty() ? null : mapToUser(results.get(0));
    }

    @Override
    public List<User> findAll() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadView("vw_users");
        return mapToUserList(results);
    }

    public List<User> findByRole(String role) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_users",
                "role = ? ORDER BY full_name", role);
        return mapToUserList(results);
    }

    public List<User> findActiveUsers() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_users",
                "is_active = true ORDER BY full_name");
        return mapToUserList(results);
    }

    @Override
    public boolean insert(User entity) throws SQLException {
        Integer userId = procedureCaller.executeCreateUserWithId(
                entity.getUsername(),
                entity.getPassword(),
                entity.getRole(),
                entity.getFullName(),
                entity.getEmail()
        );
        if (userId != null && userId > 0) {
            entity.setId(userId);
            return true;
        }
        return false;
    }

    public int insertAndGetId(User entity) throws SQLException {
        return procedureCaller.executeCreateUserWithId(
                entity.getUsername(),
                entity.getPassword(),
                entity.getRole(),
                entity.getFullName(),
                entity.getEmail()
        );
    }

    @Override
    public boolean update(User entity) throws SQLException {
        return procedureCaller.executeUpdateUser(
                entity.getId(),
                entity.getUsername(),
                entity.getRole(),
                entity.getFullName(),
                entity.getEmail(),
                entity.isActive()
        );
    }

    public boolean updatePassword(int userId, String newPassword) throws SQLException {
        return procedureCaller.executeUpdateUserPassword(userId, newPassword);
    }

    public boolean updatePasswordByUsername(String username, String newPassword) throws SQLException {
        return procedureCaller.executeUpdateUserPasswordByUsername(username, newPassword);
    }

    @Override
    public boolean delete(int userId) throws SQLException {
        return procedureCaller.executeDeleteUser(userId);
    }

    public int countUsers() throws SQLException {
        return viewLoader.countViewRows("vw_users");
    }

    public boolean isDefaultAdmin(int userId) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_users",
                "id = ? AND username = 'Nqosa' AND role = 'ADMIN'", userId);
        return !results.isEmpty();
    }

    public boolean updateLastLogin(int userId) throws SQLException {
        return procedureCaller.executeUpdateUserLastLogin(userId);
    }

    public boolean updateProfileImage(int userId, String imagePath) throws SQLException {
        return procedureCaller.executeUpdateUserProfileImage(userId, imagePath);
    }

    public boolean toggleUserStatus(int userId, boolean isActive) throws SQLException {
        return procedureCaller.executeToggleUserStatus(userId, isActive);
    }

    public List<User> searchUsers(String keyword) throws SQLException {
        String pattern = "%" + keyword + "%";
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_users",
                "username ILIKE ? OR full_name ILIKE ? OR email ILIKE ? ORDER BY full_name",
                pattern, pattern, pattern);
        return mapToUserList(results);
    }

    public List<User> findInactiveUsers() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_users",
                "is_active = false ORDER BY full_name");
        return mapToUserList(results);
    }

    public List<User> findRecentUsers(int limit) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_users",
                "1=1 ORDER BY created_at DESC LIMIT ?", limit);
        return mapToUserList(results);
    }

    // ============================================
    // HELPER METHODS FOR MAPPING
    // ============================================

    private User mapToUser(Map<String, Object> map) {
        if (map == null) return null;

        User user = new User();
        if (map.get("id") != null) user.setId(((Number) map.get("id")).intValue());
        if (map.get("username") != null) user.setUsername(map.get("username").toString());
        if (map.get("password") != null) user.setPassword(map.get("password").toString());
        if (map.get("role") != null) user.setRole(map.get("role").toString());
        if (map.get("full_name") != null) user.setFullName(map.get("full_name").toString());
        if (map.get("email") != null) user.setEmail(map.get("email").toString());
        if (map.get("phone") != null) user.setPhone(map.get("phone").toString());
        if (map.get("address") != null) user.setAddress(map.get("address").toString());
        if (map.get("profile_image") != null) user.setProfileImage(map.get("profile_image").toString());
        if (map.get("is_active") != null) user.setActive((Boolean) map.get("is_active"));
        if (map.get("last_login") instanceof java.sql.Timestamp) {
            user.setLastLogin(((java.sql.Timestamp) map.get("last_login")).toLocalDateTime());
        }
        if (map.get("created_at") instanceof java.sql.Timestamp) {
            user.setCreatedAt(((java.sql.Timestamp) map.get("created_at")).toLocalDateTime());
        }
        if (map.get("updated_at") instanceof java.sql.Timestamp) {
            user.setUpdatedAt(((java.sql.Timestamp) map.get("updated_at")).toLocalDateTime());
        }
        return user;
    }

    private List<User> mapToUserList(List<Map<String, Object>> maps) {
        List<User> users = new ArrayList<>();
        if (maps != null) {
            for (Map<String, Object> map : maps) {
                users.add(mapToUser(map));
            }
        }
        return users;
    }

    @Override
    protected User mapRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setRole(rs.getString("role"));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setActive(rs.getBoolean("is_active"));

        try { user.setPhone(rs.getString("phone")); } catch (SQLException e) {}
        try { user.setAddress(rs.getString("address")); } catch (SQLException e) {}
        try { user.setProfileImage(rs.getString("profile_image")); } catch (SQLException e) {}
        try {
            if (rs.getTimestamp("last_login") != null) {
                user.setLastLogin(rs.getTimestamp("last_login").toLocalDateTime());
            }
        } catch (SQLException e) {}

        if (rs.getTimestamp("created_at") != null) user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        if (rs.getTimestamp("updated_at") != null) user.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return user;
    }
}