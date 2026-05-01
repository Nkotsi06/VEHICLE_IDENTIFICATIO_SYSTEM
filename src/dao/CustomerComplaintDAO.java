package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import models.CustomerComplaint;

public class CustomerComplaintDAO extends BaseDAO<CustomerComplaint> {

    @Override
    public CustomerComplaint findById(int id) throws SQLException {
        String sql = "SELECT * FROM vw_customer_complaints WHERE id = ?";
        return executeQuerySingle(sql, id);
    }

    @Override
    public List<CustomerComplaint> findAll() throws SQLException {
        String sql = "SELECT * FROM vw_customer_complaints ORDER BY complaint_date DESC";
        return executeQuery(sql);
    }

    public List<CustomerComplaint> findByCustomerId(int customerId) throws SQLException {
        String sql = "SELECT * FROM vw_customer_complaints WHERE customer_id = ? ORDER BY complaint_date DESC";
        return executeQuery(sql, customerId);
    }

    public List<CustomerComplaint> findByWorkshopId(int workshopId) throws SQLException {
        String sql = "SELECT * FROM vw_customer_complaints WHERE workshop_id = ? ORDER BY complaint_date DESC";
        return executeQuery(sql, workshopId);
    }

    public List<CustomerComplaint> findPendingComplaints() throws SQLException {
        String sql = "SELECT * FROM vw_customer_complaints WHERE resolution_status = 'PENDING' ORDER BY complaint_date";
        return executeQuery(sql);
    }

    public List<CustomerComplaint> findByStatus(String status) throws SQLException {
        String sql = "SELECT * FROM vw_customer_complaints WHERE resolution_status = ? ORDER BY complaint_date DESC";
        return executeQuery(sql, status);
    }

    public int countPendingByCustomerId(int customerId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM customer_complaints WHERE customer_id = ? AND resolution_status = 'PENDING'";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, customerId);
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
    public boolean insert(CustomerComplaint entity) throws SQLException {
        return executeProcedure("sp_submit_complaint",
                entity.getCustomerId(),
                entity.getWorkshopId(),
                entity.getComplaintText()
        );
    }

    public boolean updateStatus(int complaintId, String status, String resolutionNotes) throws SQLException {
        return executeProcedure("sp_update_complaint_status", complaintId, status);
    }

    @Override
    public boolean update(CustomerComplaint entity) throws SQLException {
        String sql = "UPDATE customer_complaints SET resolution_status = ?, resolution_notes = ? WHERE id = ?";
        int result = executeUpdate(sql, entity.getResolutionStatus(), entity.getResolutionNotes(), entity.getId());
        return result > 0;
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM customer_complaints WHERE id = ?";
        int result = executeUpdate(sql, id);
        return result > 0;
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