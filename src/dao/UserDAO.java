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

    /**
     * Authenticates a user by username and password
     * @param username the username
     * @param password the password (plain text)
     * @return User object if authenticated, null otherwise
     */
    public User login(String username, String password) throws SQLException {
        System.out.println("\n========== UserDAO.login() ==========");
        System.out.println("Username: '" + username + "'");
        System.out.println("Password: '" + password + "'");
        System.out.println("=====================================\n");

        // First, check if user exists (case-insensitive for debugging)
        List<Map<String, Object>> userCheck = viewLoader.loadViewWithCondition("vw_users",
                "username ILIKE ?", username);

        if (userCheck != null && !userCheck.isEmpty()) {
            Map<String, Object> foundUser = userCheck.get(0);
            String dbUsername = foundUser.get("username").toString();
            String dbPassword = foundUser.get("password").toString();
            boolean isActive = (Boolean) foundUser.get("is_active");

            System.out.println("User found in database:");
            System.out.println("  Username: '" + dbUsername + "'");
            System.out.println("  Password in DB: '" + dbPassword + "'");
            System.out.println("  Active: " + isActive);
            System.out.println("  Password match: " + (dbPassword.equals(password)));

            if (!isActive) {
                System.out.println("  WARNING: User is INACTIVE!");
            }
        } else {
            System.out.println("User NOT found in database: '" + username + "'");
            System.out.println("  Try checking case sensitivity or if user exists.");
        }

        // Attempt exact match login
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_users",
                "username = ? AND password = ? AND is_active = true", username, password);

        if (results != null && !results.isEmpty()) {
            System.out.println("LOGIN SUCCESSFUL for user: " + username);
            return mapToUser(results.get(0));
        }

        // Try case-insensitive login as fallback (for debugging)
        List<Map<String, Object>> caseInsensitiveResults = viewLoader.loadViewWithCondition("vw_users",
                "username ILIKE ? AND password = ? AND is_active = true", username, password);

        if (caseInsensitiveResults != null && !caseInsensitiveResults.isEmpty()) {
            System.out.println("Case-insensitive login SUCCESSFUL for user: " + username);
            return mapToUser(caseInsensitiveResults.get(0));
        }

        System.out.println("LOGIN FAILED for user: " + username);
        System.out.println("=====================================\n");
        return null;
    }

    /**
     * Authenticates a user by username only (no password check)
     * Used for debugging
     */
    public User loginDebug(String username) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_users",
                "username = ? AND is_active = true", username);
        return results.isEmpty() ? null : mapToUser(results.get(0));
    }

    @Override
    public List<User> findAll() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadView("vw_users");
        System.out.println("findAll() returned " + (results != null ? results.size() : 0) + " users");
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
        System.out.println("Inserting user: " + entity.getUsername());
        Integer userId = procedureCaller.executeCreateUserWithId(
                entity.getUsername(),
                entity.getPassword(),
                entity.getRole(),
                entity.getFullName(),
                entity.getEmail()
        );
        if (userId != null && userId > 0) {
            entity.setId(userId);
            System.out.println("User inserted with ID: " + userId);
            return true;
        }
        System.out.println("Failed to insert user");
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

    /**
     * Debug method to print all users in the database
     */
    public void debugPrintAllUsers() {
        try {
            System.out.println("\n========== ALL USERS IN DATABASE ==========");
            List<Map<String, Object>> results = viewLoader.loadView("vw_users");
            if (results == null || results.isEmpty()) {
                System.out.println("No users found in database!");
            } else {
                for (Map<String, Object> map : results) {
                    String username = map.get("username") != null ? map.get("username").toString() : "NULL";
                    String role = map.get("role") != null ? map.get("role").toString() : "NULL";
                    boolean isActive = map.get("is_active") != null && (Boolean) map.get("is_active");
                    String password = map.get("password") != null ? map.get("password").toString() : "NULL";
                    System.out.println("  ID: " + map.get("id") +
                            " | Username: " + username +
                            " | Role: " + role +
                            " | Active: " + isActive +
                            " | Password: '" + password + "'");
                }
            }
            System.out.println("===========================================\n");
        } catch (SQLException e) {
            System.err.println("Error printing users: " + e.getMessage());
            e.printStackTrace();
        }
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
                User user = mapToUser(map);
                if (user != null) {
                    users.add(user);
                }
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