package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import database.ProcedureCaller;
import database.ViewLoader;
import models.OfficerActivityLog;

/**
 * OfficerActivityLogDAO - Uses ONLY stored procedures and views for all operations.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class OfficerActivityLogDAO extends BaseDAO<OfficerActivityLog> {

    private final ProcedureCaller procedureCaller;
    private final ViewLoader viewLoader;

    public OfficerActivityLogDAO() {
        this.procedureCaller = new ProcedureCaller();
        this.viewLoader = new ViewLoader();
    }

    @Override
    public OfficerActivityLog findById(int id) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_officer_activity_log", "id = ?", id);
        if (results.isEmpty()) {
            return null;
        }
        return mapMapToOfficerActivityLog(results.get(0));
    }

    @Override
    public List<OfficerActivityLog> findAll() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadView("vw_officer_activity_log");
        return mapMapsToOfficerActivityLogs(results);
    }

    public List<OfficerActivityLog> findByOfficerId(int officerId) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_officer_activity_log", "officer_id = ? ORDER BY created_at DESC", officerId);
        return mapMapsToOfficerActivityLogs(results);
    }

    public List<OfficerActivityLog> findByActionType(String actionType) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_officer_activity_log", "action_type = ? ORDER BY created_at DESC", actionType);
        return mapMapsToOfficerActivityLogs(results);
    }

    @Override
    public boolean insert(OfficerActivityLog entity) throws SQLException {
        return procedureCaller.executeInsertOfficerActivityLog(
                entity.getOfficerId(),
                entity.getActionType(),
                entity.getActionDescription(),
                entity.getTargetType(),
                entity.getTargetId() > 0 ? entity.getTargetId() : null,
                entity.getIpAddress()
        );
    }

    public void logActivity(int officerId, String actionType, String description,
                            String targetType, Integer targetId, String ipAddress) throws SQLException {
        procedureCaller.executeInsertOfficerActivityLog(officerId, actionType, description, targetType, targetId, ipAddress);
    }

    @Override
    public boolean update(OfficerActivityLog entity) throws SQLException {
        // Activity logs are immutable
        return false;
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return procedureCaller.executeDeleteOfficerActivityLog(id);
    }

    public int deleteOldLogs(LocalDateTime beforeDate) throws SQLException {
        return procedureCaller.executeDeleteOldOfficerActivityLogs(beforeDate);
    }

    /**
     * Converts a List of Maps to a List of OfficerActivityLog objects.
     *
     * @param maps the list of maps from the view loader
     * @return list of OfficerActivityLog objects
     */
    private List<OfficerActivityLog> mapMapsToOfficerActivityLogs(List<Map<String, Object>> maps) {
        List<OfficerActivityLog> logs = new ArrayList<>();
        if (maps == null) {
            return logs;
        }
        for (Map<String, Object> map : maps) {
            OfficerActivityLog log = mapMapToOfficerActivityLog(map);
            if (log != null) {
                logs.add(log);
            }
        }
        return logs;
    }

    /**
     * Converts a Map to an OfficerActivityLog object.
     *
     * @param map the map from the view loader
     * @return OfficerActivityLog object
     */
    private OfficerActivityLog mapMapToOfficerActivityLog(Map<String, Object> map) {
        if (map == null) {
            return null;
        }

        OfficerActivityLog log = new OfficerActivityLog();

        log.setId(getIntValue(map, "id"));
        log.setOfficerId(getIntValue(map, "officer_id"));
        log.setOfficerName(getStringValue(map, "officer_name"));
        log.setActionType(getStringValue(map, "action_type"));
        log.setActionDescription(getStringValue(map, "action_description"));
        log.setTargetType(getStringValue(map, "target_type"));

        int targetId = getIntValue(map, "target_id");
        if (targetId > 0) {
            log.setTargetId(targetId);
        }

        log.setIpAddress(getStringValue(map, "ip_address"));
        log.setCreatedAt(getLocalDateTimeValue(map, "created_at"));

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
    protected OfficerActivityLog mapRow(ResultSet rs) throws SQLException {
        OfficerActivityLog log = new OfficerActivityLog();
        log.setId(rs.getInt("id"));
        log.setOfficerId(rs.getInt("officer_id"));
        log.setOfficerName(rs.getString("officer_name"));
        log.setActionType(rs.getString("action_type"));
        log.setActionDescription(rs.getString("action_description"));
        log.setTargetType(rs.getString("target_type"));

        int targetId = rs.getInt("target_id");
        if (targetId > 0) {
            log.setTargetId(targetId);
        }

        log.setIpAddress(rs.getString("ip_address"));

        if (rs.getTimestamp("created_at") != null) {
            log.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }

        return log;
    }
}