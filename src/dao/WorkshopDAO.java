package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import models.Workshop;

public class WorkshopDAO extends BaseDAO<Workshop> {

    @Override
    public Workshop findById(int id) throws SQLException {
        String sql = "SELECT * FROM vw_workshops WHERE id = ?";
        return executeQuerySingle(sql, id);
    }

    public Workshop findByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM vw_workshops WHERE user_id = ?";
        return executeQuerySingle(sql, userId);
    }

    public Workshop findByLicenseNumber(String licenseNumber) throws SQLException {
        String sql = "SELECT * FROM vw_workshops WHERE license_number = ?";
        return executeQuerySingle(sql, licenseNumber);
    }

    @Override
    public List<Workshop> findAll() throws SQLException {
        String sql = "SELECT * FROM vw_workshops ORDER BY workshop_name";
        return executeQuery(sql);
    }

    public List<Workshop> findApprovedWorkshops() throws SQLException {
        String sql = "SELECT * FROM vw_workshops WHERE is_approved = true ORDER BY workshop_name";
        return executeQuery(sql);
    }

    public List<Workshop> findPendingApproval() throws SQLException {
        String sql = "SELECT * FROM vw_workshops WHERE is_approved = false ORDER BY created_at";
        return executeQuery(sql);
    }

    public List<Workshop> searchWorkshops(String keyword) throws SQLException {
        String sql = "SELECT * FROM vw_workshops WHERE workshop_name ILIKE ? OR address ILIKE ? ORDER BY workshop_name";
        String searchPattern = "%" + keyword + "%";
        return executeQuery(sql, searchPattern, searchPattern);
    }

    @Override
    public boolean insert(Workshop entity) throws SQLException {
        // CORRECTED: Use direct INSERT instead of stored procedure call
        // This avoids the procedure/function confusion
        String sql = "INSERT INTO workshops (user_id, workshop_name, address, phone, email, license_number, is_approved) " +
                "VALUES (?, ?, ?, ?, ?, ?, false) RETURNING id";

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, entity.getUserId());
            ps.setString(2, entity.getWorkshopName());
            ps.setString(3, entity.getAddress());
            ps.setString(4, entity.getPhone());
            ps.setString(5, entity.getEmail());
            ps.setString(6, entity.getLicenseNumber());

            rs = ps.executeQuery();
            if (rs.next()) {
                int workshopId = rs.getInt(1);
                entity.setId(workshopId);

                // Notify admin for approval via direct insert
                notifyAdminOfNewWorkshop(workshopId, entity.getWorkshopName());

                return true;
            }
            return false;
        } finally {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
            closeResources(null, null, conn);
        }
    }

    /**
     * Notify admin users about new workshop registration
     */
    private void notifyAdminOfNewWorkshop(int workshopId, String workshopName) {
        String sql = "INSERT INTO notifications (user_id, message, type, reference_id) " +
                "SELECT id, ?, 'WORKSHOP_REGISTRATION', ? FROM users WHERE role = 'ADMIN' AND is_active = true";

        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, "New workshop registration pending approval: " + workshopName);
            ps.setInt(2, workshopId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace(); // Log but don't fail the operation
        } finally {
            if (ps != null) {
                try { ps.close(); } catch (SQLException e) {}
            }
            if (conn != null) {
                try { conn.close(); } catch (SQLException e) {}
            }
        }
    }

    /**
     * Approve a workshop using direct SQL update
     */
    public boolean approveWorkshop(int workshopId) throws SQLException {
        String sql = "UPDATE workshops SET is_approved = true, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, workshopId);
            int result = ps.executeUpdate();

            if (result > 0) {
                // Notify workshop owner
                notifyWorkshopOwnerOfApproval(workshopId);
            }
            return result > 0;
        } finally {
            if (ps != null) ps.close();
            closeResources(null, null, conn);
        }
    }

    /**
     * Notify workshop owner about approval
     */
    private void notifyWorkshopOwnerOfApproval(int workshopId) {
        String sql = "INSERT INTO notifications (user_id, message, type, reference_id) " +
                "SELECT w.user_id, ?, 'WORKSHOP_APPROVED', ? FROM workshops w WHERE w.id = ?";

        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, "Your workshop has been approved! You can now access the system.");
            ps.setInt(2, workshopId);
            ps.setInt(3, workshopId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (ps != null) {
                try { ps.close(); } catch (SQLException e) {}
            }
            if (conn != null) {
                try { conn.close(); } catch (SQLException e) {}
            }
        }
    }

    @Override
    public boolean update(Workshop entity) throws SQLException {
        String sql = "UPDATE workshops SET workshop_name = ?, address = ?, phone = ?, email = ?, license_number = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        int result = executeUpdate(sql,
                entity.getWorkshopName(),
                entity.getAddress(),
                entity.getPhone(),
                entity.getEmail(),
                entity.getLicenseNumber(),
                entity.getId()
        );
        return result > 0;
    }

    @Override
    public boolean delete(int id) throws SQLException {
        // Delete user will cascade to workshop due to ON DELETE CASCADE
        String sql = "DELETE FROM workshops WHERE id = ?";
        int result = executeUpdate(sql, id);
        return result > 0;
    }

    @Override
    protected Workshop mapRow(ResultSet rs) throws SQLException {
        Workshop workshop = new Workshop();
        workshop.setId(rs.getInt("id"));

        try {
            workshop.setUserId(rs.getInt("user_id"));
        } catch (SQLException e) {
            workshop.setUserId(0);
        }

        workshop.setWorkshopName(rs.getString("workshop_name"));
        workshop.setAddress(rs.getString("address"));
        workshop.setPhone(rs.getString("phone"));
        workshop.setEmail(rs.getString("email"));
        workshop.setLicenseNumber(rs.getString("license_number"));

        try {
            workshop.setApproved(rs.getBoolean("is_approved"));
        } catch (SQLException e) {
            workshop.setApproved(false);
        }

        try {
            workshop.setOwnerName(rs.getString("owner_name"));
        } catch (SQLException e) {
            // Column might not exist in all views
        }

        try {
            if (rs.getTimestamp("created_at") != null) {
                workshop.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            }
        } catch (SQLException e) {
            // Ignore
        }

        try {
            if (rs.getTimestamp("updated_at") != null) {
                workshop.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
            }
        } catch (SQLException e) {
            // Ignore
        }

        return workshop;
    }
}