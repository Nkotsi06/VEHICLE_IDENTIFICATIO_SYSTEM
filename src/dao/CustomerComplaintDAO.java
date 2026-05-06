package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import database.ProcedureCaller;
import database.ViewLoader;
import models.CustomerComplaint;

/**
 * CustomerComplaintDAO - Uses ONLY stored procedures and views for all operations.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class CustomerComplaintDAO extends BaseDAO<CustomerComplaint> {

    private final ProcedureCaller procedureCaller;
    private final ViewLoader viewLoader;

    public CustomerComplaintDAO() {
        this.procedureCaller = new ProcedureCaller();
        this.viewLoader = new ViewLoader();
    }

    @Override
    public CustomerComplaint findById(int id) throws SQLException {
        // Use view - NO direct SQL
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_customer_complaints", "id = ?", id);
        if (results.isEmpty()) {
            return null;
        }
        return mapMapToCustomerComplaint(results.get(0));
    }

    @Override
    public List<CustomerComplaint> findAll() throws SQLException {
        // Use view - NO direct SQL
        List<Map<String, Object>> results = viewLoader.loadView("vw_customer_complaints");
        return mapMapsToCustomerComplaints(results);
    }

    public List<CustomerComplaint> findByCustomerId(int customerId) throws SQLException {
        // Use view - NO direct SQL
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_customer_complaints", "customer_id = ? ORDER BY complaint_date DESC", customerId);
        return mapMapsToCustomerComplaints(results);
    }

    public List<CustomerComplaint> findByWorkshopId(int workshopId) throws SQLException {
        // Use view - NO direct SQL
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_customer_complaints", "workshop_id = ? ORDER BY complaint_date DESC", workshopId);
        return mapMapsToCustomerComplaints(results);
    }

    public List<CustomerComplaint> findPendingComplaints() throws SQLException {
        // Use view - NO direct SQL
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_customer_complaints", "resolution_status = 'PENDING' ORDER BY complaint_date");
        return mapMapsToCustomerComplaints(results);
    }

    public List<CustomerComplaint> findByStatus(String status) throws SQLException {
        // Use view - NO direct SQL
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_customer_complaints", "resolution_status = ? ORDER BY complaint_date DESC", status);
        return mapMapsToCustomerComplaints(results);
    }

    public int countPendingByCustomerId(int customerId) throws SQLException {
        // Use view - NO direct SQL
        return viewLoader.countViewRowsWithCondition("customer_complaints", "customer_id = ? AND resolution_status = 'PENDING'", customerId);
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

    public boolean updateStatus(int complaintId, String status, String resolutionNotes) throws SQLException {
        // Use stored procedure - NO direct SQL
        return procedureCaller.executeUpdateComplaintStatus(complaintId, status, resolutionNotes);
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

    /**
     * Converts a List of Maps to a List of CustomerComplaint objects.
     *
     * @param maps the list of maps from the view loader
     * @return list of CustomerComplaint objects
     */
    private List<CustomerComplaint> mapMapsToCustomerComplaints(List<Map<String, Object>> maps) {
        List<CustomerComplaint> complaints = new ArrayList<>();
        if (maps == null) {
            return complaints;
        }
        for (Map<String, Object> map : maps) {
            CustomerComplaint complaint = mapMapToCustomerComplaint(map);
            if (complaint != null) {
                complaints.add(complaint);
            }
        }
        return complaints;
    }

    /**
     * Converts a Map to a CustomerComplaint object.
     *
     * @param map the map from the view loader
     * @return CustomerComplaint object
     */
    private CustomerComplaint mapMapToCustomerComplaint(Map<String, Object> map) {
        if (map == null) {
            return null;
        }

        CustomerComplaint complaint = new CustomerComplaint();

        complaint.setId(getIntValue(map, "id"));
        complaint.setCustomerId(getIntValue(map, "customer_id"));
        complaint.setCustomerName(getStringValue(map, "customer_name"));
        complaint.setWorkshopId(getIntValue(map, "workshop_id"));
        complaint.setWorkshopName(getStringValue(map, "workshop_name"));
        complaint.setComplaintText(getStringValue(map, "complaint_text"));
        complaint.setResolutionStatus(getStringValue(map, "resolution_status"));
        complaint.setResolutionNotes(getStringValue(map, "resolution_notes"));

        complaint.setComplaintDate(getLocalDateTimeValue(map, "complaint_date"));
        complaint.setCreatedAt(getLocalDateTimeValue(map, "created_at"));
        complaint.setUpdatedAt(getLocalDateTimeValue(map, "updated_at"));

        return complaint;
    }

    /**
     * Helper method to safely get Integer values from Map.
     */
    private Integer getIntValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return 0;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Long) {
            return ((Long) value).intValue();
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return 0;
    }

    /**
     * Helper method to safely get String values from Map.
     */
    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "";
    }

    /**
     * Helper method to safely get LocalDateTime values from Map.
     */
    private LocalDateTime getLocalDateTimeValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof java.sql.Timestamp) {
            return ((java.sql.Timestamp) value).toLocalDateTime();
        }
        if (value instanceof LocalDateTime) {
            return (LocalDateTime) value;
        }
        return null;
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