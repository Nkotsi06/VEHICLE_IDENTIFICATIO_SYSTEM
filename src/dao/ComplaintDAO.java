package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import database.ProcedureCaller;
import database.ViewLoader;
import models.CustomerComplaint;

/**
 * ComplaintDAO - Uses ONLY stored procedures and views for all operations.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class ComplaintDAO extends BaseDAO<CustomerComplaint> {

    private final ProcedureCaller procedureCaller;
    private final ViewLoader viewLoader;

    public ComplaintDAO() {
        this.procedureCaller = new ProcedureCaller();
        this.viewLoader = new ViewLoader();
    }

    @Override
    public CustomerComplaint findById(int id) throws SQLException {
        // Use view - NO direct SQL
        List<CustomerComplaint> results = viewLoader.loadViewWithCondition("vw_customer_complaints", "id = ?", id);
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public List<CustomerComplaint> findAll() throws SQLException {
        // Use view - NO direct SQL
        return viewLoader.loadView("vw_customer_complaints");
    }

    public List<CustomerComplaint> findByCustomerId(int customerId) throws SQLException {
        // Use view - NO direct SQL
        return viewLoader.loadViewWithCondition("vw_customer_complaints", "customer_id = ? ORDER BY complaint_date DESC", customerId);
    }

    public List<CustomerComplaint> findByCustomerName(String customerName) throws SQLException {
        // Use view - NO direct SQL
        return viewLoader.loadViewWithCondition("vw_customer_complaints", "customer_name ILIKE ? ORDER BY complaint_date DESC", "%" + customerName + "%");
    }

    public List<CustomerComplaint> findByWorkshopId(int workshopId) throws SQLException {
        // Use view - NO direct SQL
        return viewLoader.loadViewWithCondition("vw_customer_complaints", "workshop_id = ? ORDER BY complaint_date DESC", workshopId);
    }

    public List<CustomerComplaint> findByWorkshopName(String workshopName) throws SQLException {
        // Use view - NO direct SQL
        return viewLoader.loadViewWithCondition("vw_customer_complaints", "workshop_name ILIKE ? ORDER BY complaint_date DESC", "%" + workshopName + "%");
    }

    public List<CustomerComplaint> findPendingComplaints() throws SQLException {
        // Use view - NO direct SQL
        return viewLoader.loadViewWithCondition("vw_customer_complaints", "resolution_status = 'PENDING' ORDER BY complaint_date");
    }

    public List<CustomerComplaint> findByStatus(String status) throws SQLException {
        // Use view - NO direct SQL
        return viewLoader.loadViewWithCondition("vw_customer_complaints", "resolution_status = ? ORDER BY complaint_date DESC", status);
    }

    public List<CustomerComplaint> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) throws SQLException {
        // Use view - NO direct SQL
        return viewLoader.loadViewWithCondition("vw_customer_complaints", "complaint_date BETWEEN ? AND ? ORDER BY complaint_date DESC", startDate, endDate);
    }

    @Override
    public boolean insert(CustomerComplaint entity) throws SQLException {
        // Use stored procedure - NO direct SQL
        Integer complaintId = procedureCaller.executeSubmitComplaint(
                entity.getCustomerId(),
                entity.getWorkshopId(),
                entity.getComplaintText()
        );
        if (complaintId != null && complaintId > 0) {
            entity.setId(complaintId);
            return true;
        }
        return false;
    }

    public int insertAndGetId(CustomerComplaint entity) throws SQLException {
        // Use stored procedure - NO direct SQL
        Integer complaintId = procedureCaller.executeSubmitComplaint(
                entity.getCustomerId(),
                entity.getWorkshopId(),
                entity.getComplaintText()
        );
        return complaintId != null ? complaintId : -1;
    }

    public boolean updateStatus(int complaintId, String status, String resolutionNotes) throws SQLException {
        // Use stored procedure - NO direct SQL
        return procedureCaller.executeUpdateComplaintStatus(complaintId, status, resolutionNotes);
    }

    public boolean resolveComplaint(int complaintId, String resolutionNotes) throws SQLException {
        return updateStatus(complaintId, "RESOLVED", resolutionNotes);
    }

    public boolean dismissComplaint(int complaintId, String reason) throws SQLException {
        return updateStatus(complaintId, "DISMISSED", reason);
    }

    @Override
    public boolean update(CustomerComplaint entity) throws SQLException {
        // Use stored procedure - NO direct SQL
        return updateStatus(entity.getId(), entity.getResolutionStatus(), entity.getResolutionNotes());
    }

    @Override
    public boolean delete(int id) throws SQLException {
        // Use stored procedure - NO direct SQL
        return procedureCaller.executeDeleteComplaint(id);
    }

    public int countPendingComplaints() throws SQLException {
        // Use view - NO direct SQL
        return viewLoader.countViewRowsWithCondition("customer_complaints", "resolution_status = 'PENDING'");
    }

    public int countComplaintsByWorkshop(int workshopId) throws SQLException {
        // Use view - NO direct SQL
        return viewLoader.countViewRowsWithCondition("customer_complaints", "workshop_id = ?", workshopId);
    }

    public double getResolutionRate() throws SQLException {
        // Use view - NO direct SQL
        int total = viewLoader.countViewRows("customer_complaints");
        if (total == 0) return 0;
        int resolved = viewLoader.countViewRowsWithCondition("customer_complaints", "resolution_status != 'PENDING'");
        return (double) resolved / total * 100;
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