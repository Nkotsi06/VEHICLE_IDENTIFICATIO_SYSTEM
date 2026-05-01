package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import models.CustomerComplaint;

public class ComplaintDAO extends BaseDAO<CustomerComplaint> {

    @Override
    public CustomerComplaint findById(int id) throws SQLException {
        String sql = "SELECT * FROM vw_customer_complaints WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
            return null;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    @Override
    public List<CustomerComplaint> findAll() throws SQLException {
        String sql = "SELECT * FROM vw_customer_complaints ORDER BY complaint_date DESC";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<CustomerComplaint> complaints = new ArrayList<>();
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                complaints.add(mapRow(rs));
            }
            return complaints;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    public List<CustomerComplaint> findByCustomerId(int customerId) throws SQLException {
        String sql = "SELECT * FROM vw_customer_complaints WHERE customer_id = ? ORDER BY complaint_date DESC";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<CustomerComplaint> complaints = new ArrayList<>();
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, customerId);
            rs = ps.executeQuery();
            while (rs.next()) {
                complaints.add(mapRow(rs));
            }
            return complaints;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    public List<CustomerComplaint> findByCustomerName(String customerName) throws SQLException {
        String sql = "SELECT * FROM vw_customer_complaints WHERE customer_name ILIKE ? ORDER BY complaint_date DESC";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<CustomerComplaint> complaints = new ArrayList<>();
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, "%" + customerName + "%");
            rs = ps.executeQuery();
            while (rs.next()) {
                complaints.add(mapRow(rs));
            }
            return complaints;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    public List<CustomerComplaint> findByWorkshopId(int workshopId) throws SQLException {
        String sql = "SELECT * FROM vw_customer_complaints WHERE workshop_id = ? ORDER BY complaint_date DESC";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<CustomerComplaint> complaints = new ArrayList<>();
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, workshopId);
            rs = ps.executeQuery();
            while (rs.next()) {
                complaints.add(mapRow(rs));
            }
            return complaints;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    public List<CustomerComplaint> findByWorkshopName(String workshopName) throws SQLException {
        String sql = "SELECT * FROM vw_customer_complaints WHERE workshop_name ILIKE ? ORDER BY complaint_date DESC";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<CustomerComplaint> complaints = new ArrayList<>();
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, "%" + workshopName + "%");
            rs = ps.executeQuery();
            while (rs.next()) {
                complaints.add(mapRow(rs));
            }
            return complaints;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    public List<CustomerComplaint> findPendingComplaints() throws SQLException {
        String sql = "SELECT * FROM vw_customer_complaints WHERE resolution_status = 'PENDING' ORDER BY complaint_date";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<CustomerComplaint> complaints = new ArrayList<>();
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                complaints.add(mapRow(rs));
            }
            return complaints;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    public List<CustomerComplaint> findByStatus(String status) throws SQLException {
        String sql = "SELECT * FROM vw_customer_complaints WHERE resolution_status = ? ORDER BY complaint_date DESC";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<CustomerComplaint> complaints = new ArrayList<>();
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, status);
            rs = ps.executeQuery();
            while (rs.next()) {
                complaints.add(mapRow(rs));
            }
            return complaints;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    public List<CustomerComplaint> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) throws SQLException {
        String sql = "SELECT * FROM vw_customer_complaints WHERE complaint_date BETWEEN ? AND ? ORDER BY complaint_date DESC";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<CustomerComplaint> complaints = new ArrayList<>();
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setObject(1, startDate);
            ps.setObject(2, endDate);
            rs = ps.executeQuery();
            while (rs.next()) {
                complaints.add(mapRow(rs));
            }
            return complaints;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    @Override
    public boolean insert(CustomerComplaint entity) throws SQLException {
        String sql = "CALL sp_submit_complaint(?, ?, ?)";
        Connection conn = null;
        java.sql.CallableStatement cs = null;
        try {
            conn = getConnection();
            cs = conn.prepareCall("{call sp_submit_complaint(?, ?, ?)}");
            cs.setInt(1, entity.getCustomerId());
            cs.setInt(2, entity.getWorkshopId());
            cs.setString(3, entity.getComplaintText());
            return cs.execute();
        } finally {
            if (cs != null) cs.close();
            closeResources(null, null, conn);
        }
    }

    public int insertAndGetId(CustomerComplaint entity) throws SQLException {
        String sql = "CALL sp_submit_complaint(?, ?, ?)";
        Connection conn = null;
        java.sql.CallableStatement cs = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            cs = conn.prepareCall("{call sp_submit_complaint(?, ?, ?)}");
            cs.setInt(1, entity.getCustomerId());
            cs.setInt(2, entity.getWorkshopId());
            cs.setString(3, entity.getComplaintText());
            cs.execute();

            String querySql = "SELECT id FROM customer_complaints WHERE customer_id = ? ORDER BY complaint_date DESC LIMIT 1";
            ps = conn.prepareStatement(querySql);
            ps.setInt(1, entity.getCustomerId());
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
            return -1;
        } finally {
            closeResources(rs, ps, null);
            if (cs != null) cs.close();
            closeResources(null, null, conn);
        }
    }

    public boolean updateStatus(int complaintId, String status, String resolutionNotes) throws SQLException {
        String sql = "CALL sp_update_complaint_status(?, ?)";
        Connection conn = null;
        java.sql.CallableStatement cs = null;
        try {
            conn = getConnection();
            cs = conn.prepareCall("{call sp_update_complaint_status(?, ?)}");
            cs.setInt(1, complaintId);
            cs.setString(2, status);
            cs.execute();

            if (resolutionNotes != null && !resolutionNotes.isEmpty()) {
                String updateSql = "UPDATE customer_complaints SET resolution_notes = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
                PreparedStatement ps = conn.prepareStatement(updateSql);
                ps.setString(1, resolutionNotes);
                ps.setInt(2, complaintId);
                ps.executeUpdate();
                ps.close();
            }
            return true;
        } finally {
            if (cs != null) cs.close();
            closeResources(null, null, conn);
        }
    }

    public boolean resolveComplaint(int complaintId, String resolutionNotes) throws SQLException {
        return updateStatus(complaintId, "RESOLVED", resolutionNotes);
    }

    public boolean dismissComplaint(int complaintId, String reason) throws SQLException {
        return updateStatus(complaintId, "DISMISSED", reason);
    }

    @Override
    public boolean update(CustomerComplaint entity) throws SQLException {
        String sql = "UPDATE customer_complaints SET resolution_status = ?, resolution_notes = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, entity.getResolutionStatus());
            ps.setString(2, entity.getResolutionNotes());
            ps.setInt(3, entity.getId());
            int result = ps.executeUpdate();
            return result > 0;
        } finally {
            closeResources(null, ps, conn);
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM customer_complaints WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            int result = ps.executeUpdate();
            return result > 0;
        } finally {
            closeResources(null, ps, conn);
        }
    }

    public int countPendingComplaints() throws SQLException {
        String sql = "SELECT COUNT(*) FROM customer_complaints WHERE resolution_status = 'PENDING'";
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

    public int countComplaintsByWorkshop(int workshopId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM customer_complaints WHERE workshop_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, workshopId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    public double getResolutionRate() throws SQLException {
        String sql = "SELECT ROUND(COUNT(CASE WHEN resolution_status != 'PENDING' THEN 1 END) * 100.0 / NULLIF(COUNT(*), 0), 2) FROM customer_complaints";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
            return 0;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    @Override
    protected CustomerComplaint mapRow(ResultSet rs) throws SQLException {
        CustomerComplaint complaint = new CustomerComplaint();
        complaint.setId(rs.getInt("id"));
        complaint.setCustomerId(rs.getInt("customer_id"));
        complaint.setCustomerName(rs.getString("customer_name"));
        complaint.setWorkshopId(rs.getInt("workshop_id"));
        complaint.setWorkshopName(rs.getString("workshop_name"));

        if (rs.getTimestamp("complaint_date") != null) {
            complaint.setComplaintDate(rs.getTimestamp("complaint_date").toLocalDateTime());
        }
        complaint.setComplaintText(rs.getString("complaint_text"));
        complaint.setResolutionStatus(rs.getString("resolution_status"));
        complaint.setResolutionNotes(rs.getString("resolution_notes"));

        if (rs.getTimestamp("created_at") != null) {
            complaint.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            complaint.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        return complaint;
    }
}