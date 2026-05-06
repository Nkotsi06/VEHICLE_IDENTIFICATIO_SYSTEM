package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import database.ProcedureCaller;
import database.ViewLoader;
import models.OfficerLog;

/**
 * OfficerLogDAO - Uses ONLY stored procedures and views for all operations.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class OfficerLogDAO extends BaseDAO<OfficerLog> {

    private final ProcedureCaller procedureCaller;
    private final ViewLoader viewLoader;

    public OfficerLogDAO() {
        this.procedureCaller = new ProcedureCaller();
        this.viewLoader = new ViewLoader();
    }

    @Override
    public OfficerLog findById(int id) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_officer_logs", "id = ?", id);
        if (results.isEmpty()) {
            return null;
        }
        return mapMapToOfficerLog(results.get(0));
    }

    @Override
    public List<OfficerLog> findAll() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadView("vw_officer_logs");
        return mapMapsToOfficerLogs(results);
    }

    public List<OfficerLog> findByOfficerName(String officerName) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_officer_logs", "officer_name ILIKE ? ORDER BY timestamp DESC", "%" + officerName + "%");
        return mapMapsToOfficerLogs(results);
    }

    public List<OfficerLog> findByBadgeNumber(String badgeNumber) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_officer_logs", "badge_number = ? ORDER BY timestamp DESC", badgeNumber);
        return mapMapsToOfficerLogs(results);
    }

    public List<OfficerLog> findByVehicleId(int vehicleId) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_officer_logs", "vehicle_id = ? ORDER BY timestamp DESC", vehicleId);
        return mapMapsToOfficerLogs(results);
    }

    public List<OfficerLog> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_officer_logs", "timestamp BETWEEN ? AND ? ORDER BY timestamp DESC", startDate, endDate);
        return mapMapsToOfficerLogs(results);
    }

    public List<OfficerLog> findByAction(String action) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_officer_logs", "action ILIKE ? ORDER BY timestamp DESC", "%" + action + "%");
        return mapMapsToOfficerLogs(results);
    }

    @Override
    public boolean insert(OfficerLog entity) throws SQLException {
        return procedureCaller.executeLogOfficerAction(
                entity.getOfficerName(),
                entity.getBadgeNumber(),
                entity.getAction(),
                entity.getVehicleId() > 0 ? entity.getVehicleId() : null
        );
    }

    @Override
    public boolean update(OfficerLog entity) throws SQLException {
        // Officer logs are typically immutable
        return false;
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return procedureCaller.executeDeleteOfficerLog(id);
    }

    public boolean deleteOldLogs(LocalDateTime beforeDate) throws SQLException {
        return procedureCaller.executeDeleteOldOfficerLogs(beforeDate);
    }

    /**
     * Converts a List of Maps to a List of OfficerLog objects.
     *
     * @param maps the list of maps from the view loader
     * @return list of OfficerLog objects
     */
    private List<OfficerLog> mapMapsToOfficerLogs(List<Map<String, Object>> maps) {
        List<OfficerLog> logs = new ArrayList<>();
        if (maps == null) {
            return logs;
        }
        for (Map<String, Object> map : maps) {
            OfficerLog log = mapMapToOfficerLog(map);
            if (log != null) {
                logs.add(log);
            }
        }
        return logs;
    }

    /**
     * Converts a Map to an OfficerLog object.
     *
     * @param map the map from the view loader
     * @return OfficerLog object
     */
    private OfficerLog mapMapToOfficerLog(Map<String, Object> map) {
        if (map == null) {
            return null;
        }

        OfficerLog log = new OfficerLog();

        log.setId(getIntValue(map, "id"));
        log.setOfficerName(getStringValue(map, "officer_name"));
        log.setBadgeNumber(getStringValue(map, "badge_number"));
        log.setAction(getStringValue(map, "action"));
        log.setVehicleId(getIntValue(map, "vehicle_id"));
        log.setRegistrationNumber(getStringValue(map, "registration_number"));

        log.setTimestamp(getLocalDateTimeValue(map, "timestamp"));
        log.setCreatedAt(getLocalDateTimeValue(map, "created_at"));
        log.setUpdatedAt(getLocalDateTimeValue(map, "updated_at"));

        return log;
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
    protected OfficerLog mapRow(ResultSet rs) throws SQLException {
        OfficerLog log = new OfficerLog();
        log.setId(rs.getInt("id"));
        log.setOfficerName(rs.getString("officer_name"));
        log.setBadgeNumber(rs.getString("badge_number"));
        log.setAction(rs.getString("action"));
        log.setVehicleId(rs.getInt("vehicle_id"));
        log.setRegistrationNumber(rs.getString("registration_number"));

        if (rs.getTimestamp("timestamp") != null) {
            log.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
        }
        if (rs.getTimestamp("created_at") != null) {
            log.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            log.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        return log;
    }
}