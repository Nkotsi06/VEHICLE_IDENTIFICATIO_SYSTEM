package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import models.User;

public class UserDAO extends BaseDAO<User> {

    @Override
    public User findById(int id) throws SQLException {
        String sql = "SELECT * FROM vw_users WHERE id = ?";
        return executeQuerySingle(sql, id);
    }

    public User findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM vw_users WHERE username = ?";
        return executeQuerySingle(sql, username);
    }

    public User findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM vw_users WHERE email = ?";
        return executeQuerySingle(sql, email);
    }

    public User login(String username, String password) throws SQLException {
        String sql = "SELECT * FROM vw_users WHERE username = ? AND password = ? AND is_active = true";
        return executeQuerySingle(sql, username, password);
    }

    @Override
    public List<User> findAll() throws SQLException {
        String sql = "SELECT * FROM vw_users ORDER BY id";
        return executeQuery(sql);
    }

    public List<User> findByRole(String role) throws SQLException {
        String sql = "SELECT * FROM vw_users WHERE role = ? ORDER BY full_name";
        return executeQuery(sql, role);
    }

    public List<User> findActiveUsers() throws SQLException {
        String sql = "SELECT * FROM vw_users WHERE is_active = true ORDER BY full_name";
        return executeQuery(sql);
    }

    @Override
    public boolean insert(User entity) throws SQLException {
        Integer userId = executeProcedureWithInOutParameter("sp_create_user",
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
        Integer userId = executeProcedureWithInOutParameter("sp_create_user",
                entity.getUsername(),
                entity.getPassword(),
                entity.getRole(),
                entity.getFullName(),
                entity.getEmail()
        );
        if (userId != null && userId > 0) {
            entity.setId(userId);
            return userId;
        }
        return -1;
    }

    @Override
    public boolean update(User entity) throws SQLException {
        String sql = "UPDATE users SET username = ?, full_name = ?, email = ?, phone = ?, address = ?, profile_image = ?, is_active = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, entity.getUsername());
            ps.setString(2, entity.getFullName());
            ps.setString(3, entity.getEmail());
            ps.setString(4, entity.getPhone());
            ps.setString(5, entity.getAddress());
            ps.setString(6, entity.getProfileImage());
            ps.setBoolean(7, entity.isActive());
            ps.setInt(8, entity.getId());
            int result = ps.executeUpdate();
            return result > 0;
        } finally {
            closeResources(null, ps, conn);
        }
    }

    public boolean updatePassword(int userId, String newPassword) throws SQLException {
        return executeProcedure("sp_update_user_password", userId, newPassword);
    }

    public boolean updatePasswordByUsername(String username, String newPassword) throws SQLException {
        return executeProcedure("sp_update_user_password_by_username", username, newPassword);
    }

    /**
     * Completely delete a user and all related records
     * This method handles the cascading delete in the correct order
     */
    @Override
    public boolean delete(int userId) throws SQLException {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);  // Start transaction

            // 1. Get the user's role first to know which related tables to check
            String role = getUserRole(conn, userId);

            // 2. Delete from role-specific tables based on user role
            if ("CUSTOMER".equals(role)) {
                // Get customer ID
                int customerId = getCustomerId(conn, userId);
                if (customerId > 0) {
                    // Delete digital wallet
                    deleteDigitalWallet(conn, customerId);
                    // Delete wallet transactions
                    deleteWalletTransactions(conn, customerId);
                    // Delete customer
                    deleteCustomer(conn, userId);
                }
            } else if ("WORKSHOP".equals(role)) {
                // Delete workshop
                deleteWorkshop(conn, userId);
                // Delete mechanics (cascaded by workshop deletion)
            } else if ("POLICE".equals(role)) {
                // Delete police officer
                deletePoliceOfficer(conn, userId);
                // Delete officer activity logs
                deleteOfficerActivityLogs(conn, userId);
                // Delete rank change requests
                deleteRankChangeRequests(conn, userId);
            } else if ("INSURANCE".equals(role)) {
                // Delete insurance provider
                deleteInsuranceProvider(conn, userId);
            }

            // 3. Delete notifications
            deleteNotifications(conn, userId);

            // 4. Delete audit logs (set user_id to NULL)
            deleteAuditLogs(conn, userId);

            // 5. Finally, delete the user
            boolean userDeleted = deleteUserRecord(conn, userId);

            if (userDeleted) {
                conn.commit();  // Commit transaction
                return true;
            } else {
                conn.rollback();  // Rollback on failure
                return false;
            }

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            throw e;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
                closeResources(null, null, conn);
            }
        }
    }

    // Helper methods for cascading delete
    private String getUserRole(Connection conn, int userId) throws SQLException {
        String sql = "SELECT role FROM users WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("role");
            }
            return null;
        }
    }

    private int getCustomerId(Connection conn, int userId) throws SQLException {
        String sql = "SELECT id FROM customers WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
            return -1;
        }
    }

    private void deleteDigitalWallet(Connection conn, int customerId) throws SQLException {
        String sql = "DELETE FROM digital_wallets WHERE customer_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ps.executeUpdate();
        }
    }

    private void deleteWalletTransactions(Connection conn, int customerId) throws SQLException {
        String sql = "DELETE FROM wallet_transactions WHERE wallet_id IN (SELECT id FROM digital_wallets WHERE customer_id = ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ps.executeUpdate();
        }
    }

    private void deleteCustomer(Connection conn, int userId) throws SQLException {
        String sql = "DELETE FROM customers WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    private void deleteWorkshop(Connection conn, int userId) throws SQLException {
        // First get workshop ID
        String getWorkshopIdSql = "SELECT id FROM workshops WHERE user_id = ?";
        int workshopId = -1;
        try (PreparedStatement ps = conn.prepareStatement(getWorkshopIdSql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                workshopId = rs.getInt("id");
            }
        }

        if (workshopId > 0) {
            // Delete mechanics
            String deleteMechanicsSql = "DELETE FROM mechanics WHERE workshop_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(deleteMechanicsSql)) {
                ps.setInt(1, workshopId);
                ps.executeUpdate();
            }

            // Delete part inventory
            String deleteInventorySql = "DELETE FROM parts_inventory WHERE workshop_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(deleteInventorySql)) {
                ps.setInt(1, workshopId);
                ps.executeUpdate();
            }

            // Delete service records
            String deleteServicesSql = "DELETE FROM service_records WHERE workshop_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(deleteServicesSql)) {
                ps.setInt(1, workshopId);
                ps.executeUpdate();
            }
        }

        // Delete workshop
        String sql = "DELETE FROM workshops WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    private void deletePoliceOfficer(Connection conn, int userId) throws SQLException {
        String sql = "DELETE FROM police_officers WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    private void deleteOfficerActivityLogs(Connection conn, int userId) throws SQLException {
        String sql = "DELETE FROM officer_activity_log WHERE officer_id IN (SELECT id FROM police_officers WHERE user_id = ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    private void deleteRankChangeRequests(Connection conn, int userId) throws SQLException {
        String sql = "DELETE FROM rank_change_requests WHERE officer_id IN (SELECT id FROM police_officers WHERE user_id = ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    private void deleteInsuranceProvider(Connection conn, int userId) throws SQLException {
        String sql = "DELETE FROM insurance_providers WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    private void deleteNotifications(Connection conn, int userId) throws SQLException {
        String sql = "DELETE FROM notifications WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    private void deleteAuditLogs(Connection conn, int userId) throws SQLException {
        String sql = "DELETE FROM audit_logs WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    private boolean deleteUserRecord(Connection conn, int userId) throws SQLException {
        String sql = "DELETE FROM users WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            int result = ps.executeUpdate();
            return result > 0;
        }
    }

    public int countUsers() throws SQLException {
        String sql = "SELECT COUNT(*) FROM vw_users";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    public boolean isDefaultAdmin(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM vw_users WHERE id = ? AND username = 'Nqosa' AND role = 'ADMIN'";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    public boolean updateLastLogin(int userId) throws SQLException {
        String sql = "UPDATE users SET last_login = CURRENT_TIMESTAMP WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            int result = ps.executeUpdate();
            return result > 0;
        } finally {
            closeResources(null, ps, conn);
        }
    }

    public boolean updateProfileImage(int userId, String imagePath) throws SQLException {
        String sql = "UPDATE users SET profile_image = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, imagePath);
            ps.setInt(2, userId);
            int result = ps.executeUpdate();
            return result > 0;
        } finally {
            closeResources(null, ps, conn);
        }
    }

    public boolean toggleUserStatus(int userId, boolean isActive) throws SQLException {
        String sql = "UPDATE users SET is_active = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setBoolean(1, isActive);
            ps.setInt(2, userId);
            int result = ps.executeUpdate();
            return result > 0;
        } finally {
            closeResources(null, ps, conn);
        }
    }

    public List<User> searchUsers(String keyword) throws SQLException {
        String sql = "SELECT * FROM vw_users WHERE username ILIKE ? OR full_name ILIKE ? OR email ILIKE ? ORDER BY full_name";
        String searchPattern = "%" + keyword + "%";
        return executeQuery(sql, searchPattern, searchPattern, searchPattern);
    }

    public List<User> findInactiveUsers() throws SQLException {
        String sql = "SELECT * FROM vw_users WHERE is_active = false ORDER BY full_name";
        return executeQuery(sql);
    }

    public List<User> findRecentUsers(int limit) throws SQLException {
        String sql = "SELECT * FROM vw_users ORDER BY created_at DESC LIMIT ?";
        return executeQuery(sql, limit);
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

        try {
            user.setPhone(rs.getString("phone"));
        } catch (SQLException e) { }
        try {
            user.setAddress(rs.getString("address"));
        } catch (SQLException e) { }
        try {
            user.setProfileImage(rs.getString("profile_image"));
        } catch (SQLException e) { }
        try {
            if (rs.getTimestamp("last_login") != null) {
                user.setLastLogin(rs.getTimestamp("last_login").toLocalDateTime());
            }
        } catch (SQLException e) { }

        if (rs.getTimestamp("created_at") != null) {
            user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            user.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        return user;
    }
}