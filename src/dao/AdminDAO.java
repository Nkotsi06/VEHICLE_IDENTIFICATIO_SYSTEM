package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import models.Admin;
import models.User;

public class AdminDAO extends BaseDAO<Admin> {

    private UserDAO userDAO = new UserDAO();

    @Override
    public Admin findById(int id) throws SQLException {
        String sql = "SELECT u.id, u.username, u.password, u.role, u.full_name, u.email, u.is_active, " +
                "u.created_at, u.updated_at " +
                "FROM users u WHERE u.id = ? AND u.role = 'ADMIN'";

        Admin admin = executeQuerySingle(sql, id);
        if (admin != null) {
            User user = userDAO.findById(id);
            admin.setUserId(user.getId());
            admin.setUsername(user.getUsername());
            admin.setFullName(user.getFullName());
            admin.setEmail(user.getEmail());
        }
        return admin;
    }

    public Admin findByUserId(int userId) throws SQLException {
        return findById(userId);
    }

    public Admin findByUsername(String username) throws SQLException {
        String sql = "SELECT u.id, u.username, u.password, u.role, u.full_name, u.email, u.is_active, " +
                "u.created_at, u.updated_at FROM users u WHERE u.username = ? AND u.role = 'ADMIN'";

        Admin admin = executeQuerySingle(sql, username);
        if (admin != null) {
            User user = userDAO.findByUsername(username);
            admin.setUserId(user.getId());
            admin.setUsername(user.getUsername());
            admin.setFullName(user.getFullName());
            admin.setEmail(user.getEmail());
        }
        return admin;
    }

    @Override
    public List<Admin> findAll() throws SQLException {
        String sql = "SELECT u.id, u.username, u.password, u.role, u.full_name, u.email, u.is_active, " +
                "u.created_at, u.updated_at FROM users u WHERE u.role = 'ADMIN' ORDER BY u.full_name";
        return executeQuery(sql);
    }

    public List<Admin> findSuperAdmins() throws SQLException {
        String sql = "SELECT u.id, u.username, u.password, u.role, u.full_name, u.email, u.is_active, " +
                "u.created_at, u.updated_at FROM users u WHERE u.role = 'ADMIN' AND u.is_active = true";
        return executeQuery(sql);
    }

    @Override
    public boolean insert(Admin entity) throws SQLException {
        User user = new User();
        user.setUsername(entity.getUsername());
        user.setPassword(entity.getPassword());
        user.setRole("ADMIN");
        user.setFullName(entity.getFullName());
        user.setEmail(entity.getEmail());
        user.setActive(true);

        return userDAO.insert(user);
    }

    public int insertAndGetId(Admin entity) throws SQLException {
        User user = new User();
        user.setUsername(entity.getUsername());
        user.setPassword(entity.getPassword());
        user.setRole("ADMIN");
        user.setFullName(entity.getFullName());
        user.setEmail(entity.getEmail());
        user.setActive(true);

        return userDAO.insertAndGetId(user);
    }

    @Override
    public boolean update(Admin entity) throws SQLException {
        User user = userDAO.findById(entity.getUserId());
        if (user != null) {
            user.setUsername(entity.getUsername());
            user.setFullName(entity.getFullName());
            user.setEmail(entity.getEmail());
            user.setActive(entity.isActive());
            return userDAO.update(user);
        }
        return false;
    }

    public boolean updatePassword(int adminId, String newPassword) throws SQLException {
        return userDAO.updatePassword(adminId, newPassword);
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return userDAO.delete(id);
    }

    public int countAdmins() throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE role = 'ADMIN' AND is_active = true";
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