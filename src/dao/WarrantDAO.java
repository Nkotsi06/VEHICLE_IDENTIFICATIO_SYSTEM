package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import database.ProcedureCaller;
import database.ViewLoader;
import models.Warrant;

/**
 * WarrantDAO - Uses ONLY stored procedures and views for all operations.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class WarrantDAO extends BaseDAO<Warrant> {

    private final ProcedureCaller procedureCaller;
    private final ViewLoader viewLoader;

    public WarrantDAO() {
        this.procedureCaller = new ProcedureCaller();
        this.viewLoader = new ViewLoader();
    }

    @Override
    public Warrant findById(int id) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_warrants", "id = ?", id);
        if (results.isEmpty()) {
            return null;
        }
        return mapMapToWarrant(results.get(0));
    }

    @Override
    public List<Warrant> findAll() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadView("vw_warrants");
        return mapMapsToWarrants(results);
    }

    public List<Warrant> findActiveWarrants() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadView("vw_active_warrants");
        return mapMapsToWarrants(results);
    }

    public List<Warrant> findByVehicleId(int vehicleId) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_active_warrants", "vehicle_id = ?", vehicleId);
        return mapMapsToWarrants(results);
    }

    public List<Warrant> findByViolationId(int violationId) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_warrants", "violation_id = ?", violationId);
        return mapMapsToWarrants(results);
    }

    public List<Warrant> findExpiredWarrants() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_warrants", "expiry_date < CURRENT_DATE AND status = 'ACTIVE'");
        return mapMapsToWarrants(results);
    }

    public List<Warrant> findByJudge(String judgeName) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_warrants", "judge_name ILIKE ? ORDER BY issue_date DESC", "%" + judgeName + "%");
        return mapMapsToWarrants(results);
    }

    public boolean issueWarrant(int violationId, String judgeName, LocalDate issueDate, LocalDate expiryDate) throws SQLException {
        Integer warrantId = procedureCaller.executeIssueWarrant(violationId, judgeName, issueDate, expiryDate);
        return warrantId != null && warrantId > 0;
    }

    @Override
    public boolean insert(Warrant entity) throws SQLException {
        Integer warrantId = procedureCaller.executeIssueWarrant(
                entity.getViolationId(),
                entity.getJudgeName(),
                entity.getIssueDate(),
                entity.getExpiryDate()
        );
        if (warrantId != null && warrantId > 0) {
            entity.setId(warrantId);
            return true;
        }
        return false;
    }

    @Override
    public boolean update(Warrant entity) throws SQLException {
        if ("EXECUTED".equals(entity.getStatus())) {
            return procedureCaller.executeExecuteWarrant(entity.getId());
        } else if ("CANCELLED".equals(entity.getStatus())) {
            return procedureCaller.executeCancelWarrant(entity.getId());
        }
        return false;
    }

    public boolean closeWarrant(int warrantId) throws SQLException {
        return procedureCaller.executeExecuteWarrant(warrantId);
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return procedureCaller.executeDeleteWarrant(id);
    }

    public int countActiveWarrants() throws SQLException {
        return viewLoader.countViewRowsWithCondition("vw_active_warrants", "1=1");
    }

    /**
     * Converts a List of Maps to a List of Warrant objects.
     */
    private List<Warrant> mapMapsToWarrants(List<Map<String, Object>> maps) {
        List<Warrant> warrants = new ArrayList<>();
        if (maps == null) {
            return warrants;
        }
        for (Map<String, Object> map : maps) {
            Warrant warrant = mapMapToWarrant(map);
            if (warrant != null) {
                warrants.add(warrant);
            }
        }
        return warrants;
    }

    /**
     * Converts a Map to a Warrant object.
     */
    private Warrant mapMapToWarrant(Map<String, Object> map) {
        if (map == null) {
            return null;
        }

        Warrant warrant = new Warrant();

        warrant.setId(getIntValue(map, "id"));
        warrant.setViolationId(getIntValue(map, "violation_id"));
        warrant.setVehicleId(getIntValue(map, "vehicle_id"));
        warrant.setRegistrationNumber(getStringValue(map, "registration_number"));
        warrant.setJudgeName(getStringValue(map, "judge_name"));
        warrant.setStatus(getStringValue(map, "status"));
        warrant.setFineAmount(getDoubleValue(map, "fine_amount"));

        warrant.setIssueDate(getLocalDateValue(map, "issue_date"));
        warrant.setExpiryDate(getLocalDateValue(map, "expiry_date"));
        warrant.setCreatedAt(getLocalDateTimeValue(map, "created_at"));
        warrant.setUpdatedAt(getLocalDateTimeValue(map, "updated_at"));

        return warrant;
    }

    private Integer getIntValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return 0;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Long) return ((Long) value).intValue();
        if (value instanceof Number) return ((Number) value).intValue();
        return 0;
    }

    private Double getDoubleValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return 0.0;
        if (value instanceof Double) return (Double) value;
        if (value instanceof Integer) return ((Integer) value).doubleValue();
        if (value instanceof Long) return ((Long) value).doubleValue();
        if (value instanceof Number) return ((Number) value).doubleValue();
        return 0.0;
    }

    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "";
    }

    private LocalDate getLocalDateValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof java.sql.Date) return ((java.sql.Date) value).toLocalDate();
        if (value instanceof LocalDate) return (LocalDate) value;
        return null;
    }

    private LocalDateTime getLocalDateTimeValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof java.sql.Timestamp) return ((java.sql.Timestamp) value).toLocalDateTime();
        if (value instanceof LocalDateTime) return (LocalDateTime) value;
        return null;
    }

    @Override
    protected Warrant mapRow(ResultSet rs) throws SQLException {
        Warrant warrant = new Warrant();
        warrant.setId(rs.getInt("id"));
        warrant.setViolationId(rs.getInt("violation_id"));
        warrant.setVehicleId(rs.getInt("vehicle_id"));
        warrant.setRegistrationNumber(rs.getString("registration_number"));

        if (rs.getDate("issue_date") != null) {
            warrant.setIssueDate(rs.getDate("issue_date").toLocalDate());
        }
        if (rs.getDate("expiry_date") != null) {
            warrant.setExpiryDate(rs.getDate("expiry_date").toLocalDate());
        }
        warrant.setJudgeName(rs.getString("judge_name"));
        warrant.setStatus(rs.getString("status"));
        warrant.setFineAmount(rs.getDouble("fine_amount"));

        if (rs.getTimestamp("created_at") != null) {
            warrant.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            warrant.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        return warrant;
    }
}