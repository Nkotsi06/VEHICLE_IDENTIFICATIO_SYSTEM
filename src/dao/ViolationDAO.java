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
import models.Violation;

/**
 * ViolationDAO - Uses ONLY stored procedures and views for all operations.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class ViolationDAO extends BaseDAO<Violation> {

    private final ProcedureCaller procedureCaller;
    private final ViewLoader viewLoader;

    public ViolationDAO() {
        this.procedureCaller = new ProcedureCaller();
        this.viewLoader = new ViewLoader();
    }

    @Override
    public Violation findById(int id) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_violations", "id = ?", id);
        if (results.isEmpty()) {
            return null;
        }
        return mapMapToViolation(results.get(0));
    }

    @Override
    public List<Violation> findAll() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadView("vw_violations");
        return mapMapsToViolations(results);
    }

    public List<Violation> findByVehicleId(int vehicleId) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_violations", "vehicle_id = ? ORDER BY violation_date DESC", vehicleId);
        return mapMapsToViolations(results);
    }

    public List<Violation> findByRegistrationNumber(String registrationNumber) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_violations", "registration_number = ? ORDER BY violation_date DESC", registrationNumber);
        return mapMapsToViolations(results);
    }

    public List<Violation> findUnpaidViolations() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadView("vw_unpaid_violations");
        return mapMapsToViolations(results);
    }

    public List<Violation> findPaidViolations() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_violations", "payment_status = 'PAID' ORDER BY violation_date DESC");
        return mapMapsToViolations(results);
    }

    public List<Violation> findByViolationType(String violationType) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_violations", "violation_type ILIKE ? ORDER BY violation_date DESC", "%" + violationType + "%");
        return mapMapsToViolations(results);
    }

    public List<Violation> findByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_violations", "violation_date BETWEEN ? AND ? ORDER BY violation_date DESC", startDate, endDate);
        return mapMapsToViolations(results);
    }

    public List<Violation> findByOfficer(String officerName) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_violations", "officer_name ILIKE ? ORDER BY violation_date DESC", "%" + officerName + "%");
        return mapMapsToViolations(results);
    }

    @Override
    public boolean insert(Violation entity) throws SQLException {
        // Convert LocalDate to java.sql.Date
        java.sql.Date sqlViolationDate = entity.getViolationDate() != null ?
                java.sql.Date.valueOf(entity.getViolationDate()) : null;

        Integer violationId = procedureCaller.executeAddViolation(
                entity.getVehicleId(),
                sqlViolationDate,
                entity.getViolationType(),
                entity.getFineAmount(),
                entity.getLocation(),
                entity.getOfficerName(),
                entity.getLatitude(),
                entity.getLongitude()
        );
        if (violationId != null && violationId > 0) {
            entity.setId(violationId);
            return true;
        }
        return false;
    }

    @Override
    public boolean update(Violation entity) throws SQLException {
        // Convert LocalDate to java.sql.Date
        java.sql.Date sqlViolationDate = entity.getViolationDate() != null ?
                java.sql.Date.valueOf(entity.getViolationDate()) : null;

        return procedureCaller.executeUpdateViolation(
                entity.getId(),
                entity.getVehicleId(),
                sqlViolationDate,
                entity.getViolationType(),
                entity.getFineAmount(),
                entity.getLocation(),
                entity.getOfficerName(),
                entity.getPaymentStatus()
        );
    }

    public boolean markAsPaid(int violationId) throws SQLException {
        return procedureCaller.executeMarkViolationPaid(violationId);
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return procedureCaller.executeDeleteViolation(id);
    }

    public double getTotalUnpaidFines() throws SQLException {
        return viewLoader.getSumUnpaidFines();
    }

    public int countViolationsByVehicle(int vehicleId) throws SQLException {
        return viewLoader.countViewRowsWithCondition("vw_violations", "vehicle_id = ?", vehicleId);
    }

    /**
     * Converts a List of Maps to a List of Violation objects.
     *
     * @param maps the list of maps from the view loader
     * @return list of Violation objects
     */
    private List<Violation> mapMapsToViolations(List<Map<String, Object>> maps) {
        List<Violation> violations = new ArrayList<>();
        if (maps == null) {
            return violations;
        }
        for (Map<String, Object> map : maps) {
            Violation violation = mapMapToViolation(map);
            if (violation != null) {
                violations.add(violation);
            }
        }
        return violations;
    }

    /**
     * Converts a Map to a Violation object.
     *
     * @param map the map from the view loader
     * @return Violation object
     */
    private Violation mapMapToViolation(Map<String, Object> map) {
        if (map == null) {
            return null;
        }

        Violation violation = new Violation();

        violation.setId(getIntValue(map, "id"));
        violation.setVehicleId(getIntValue(map, "vehicle_id"));
        violation.setRegistrationNumber(getStringValue(map, "registration_number"));
        violation.setMake(getStringValue(map, "make"));
        violation.setModel(getStringValue(map, "model"));
        violation.setViolationType(getStringValue(map, "violation_type"));
        violation.setFineAmount(getDoubleValue(map, "fine_amount"));
        violation.setPaymentStatus(getStringValue(map, "payment_status"));
        violation.setLocation(getStringValue(map, "location"));
        violation.setOfficerName(getStringValue(map, "officer_name"));

        // Handle latitude
        Object latObj = map.get("latitude");
        if (latObj instanceof Number) {
            violation.setLatitude(((Number) latObj).doubleValue());
        }

        // Handle longitude
        Object lngObj = map.get("longitude");
        if (lngObj instanceof Number) {
            violation.setLongitude(((Number) lngObj).doubleValue());
        }

        violation.setViolationDate(getLocalDateValue(map, "violation_date"));
        violation.setCreatedAt(getLocalDateTimeValue(map, "created_at"));
        violation.setUpdatedAt(getLocalDateTimeValue(map, "updated_at"));

        return violation;
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
     * Helper method to safely get Double values from Map.
     */
    private Double getDoubleValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return 0.0;
        }
        if (value instanceof Double) {
            return (Double) value;
        }
        if (value instanceof Integer) {
            return ((Integer) value).doubleValue();
        }
        if (value instanceof Long) {
            return ((Long) value).doubleValue();
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return 0.0;
    }

    /**
     * Helper method to safely get String values from Map.
     */
    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "";
    }

    /**
     * Helper method to safely get LocalDate values from Map.
     */
    private LocalDate getLocalDateValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof java.sql.Date) {
            return ((java.sql.Date) value).toLocalDate();
        }
        if (value instanceof LocalDate) {
            return (LocalDate) value;
        }
        return null;
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
    protected Violation mapRow(ResultSet rs) throws SQLException {
        Violation violation = new Violation();
        violation.setId(rs.getInt("id"));
        violation.setVehicleId(rs.getInt("vehicle_id"));
        violation.setRegistrationNumber(rs.getString("registration_number"));
        violation.setMake(rs.getString("make"));
        violation.setModel(rs.getString("model"));

        if (rs.getDate("violation_date") != null) {
            violation.setViolationDate(rs.getDate("violation_date").toLocalDate());
        }
        violation.setViolationType(rs.getString("violation_type"));
        violation.setFineAmount(rs.getDouble("fine_amount"));
        violation.setPaymentStatus(rs.getString("payment_status"));
        violation.setLocation(rs.getString("location"));
        violation.setOfficerName(rs.getString("officer_name"));

        // Get latitude and longitude if they exist in the result set
        try {
            if (rs.getObject("latitude") != null) {
                violation.setLatitude(rs.getDouble("latitude"));
            }
        } catch (SQLException e) {
            // Column might not exist - ignore
        }

        try {
            if (rs.getObject("longitude") != null) {
                violation.setLongitude(rs.getDouble("longitude"));
            }
        } catch (SQLException e) {
            // Column might not exist - ignore
        }

        if (rs.getTimestamp("created_at") != null) {
            violation.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            violation.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        return violation;
    }
}