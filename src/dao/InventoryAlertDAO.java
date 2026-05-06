package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import database.ProcedureCaller;
import database.ViewLoader;
import models.InventoryAlert;

/**
 * InventoryAlertDAO - Uses ONLY stored procedures and views for all operations.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class InventoryAlertDAO extends BaseDAO<InventoryAlert> {

    private final ProcedureCaller procedureCaller;
    private final ViewLoader viewLoader;

    public InventoryAlertDAO() {
        this.procedureCaller = new ProcedureCaller();
        this.viewLoader = new ViewLoader();
    }

    @Override
    public InventoryAlert findById(int id) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_inventory_alerts", "id = ?", id);
        if (results.isEmpty()) {
            return null;
        }
        return mapMapToInventoryAlert(results.get(0));
    }

    @Override
    public List<InventoryAlert> findAll() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadView("vw_inventory_alerts");
        return mapMapsToInventoryAlerts(results);
    }

    public List<InventoryAlert> findByPartInventoryId(int partInventoryId) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_inventory_alerts", "part_inventory_id = ? ORDER BY created_at DESC", partInventoryId);
        return mapMapsToInventoryAlerts(results);
    }

    public List<InventoryAlert> findByWorkshopId(int workshopId) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_inventory_alerts", "workshop_id = ? AND is_resolved = false ORDER BY created_at DESC", workshopId);
        return mapMapsToInventoryAlerts(results);
    }

    public List<InventoryAlert> findUnresolvedAlerts() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_inventory_alerts", "is_resolved = false ORDER BY created_at");
        return mapMapsToInventoryAlerts(results);
    }

    public List<InventoryAlert> findByAlertType(String alertType) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_inventory_alerts", "alert_type = ? AND is_resolved = false ORDER BY created_at", alertType);
        return mapMapsToInventoryAlerts(results);
    }

    public List<InventoryAlert> findResolvedAlerts() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_inventory_alerts", "is_resolved = true ORDER BY resolved_at DESC");
        return mapMapsToInventoryAlerts(results);
    }

    @Override
    public boolean insert(InventoryAlert entity) throws SQLException {
        Integer alertId = procedureCaller.executeInsertInventoryAlert(
                entity.getPartInventoryId(),
                entity.getAlertType(),
                entity.getMessage()
        );
        if (alertId != null && alertId > 0) {
            entity.setId(alertId);
            return true;
        }
        return false;
    }

    public int insertAndGetId(InventoryAlert entity) throws SQLException {
        Integer alertId = procedureCaller.executeInsertInventoryAlert(
                entity.getPartInventoryId(),
                entity.getAlertType(),
                entity.getMessage()
        );
        if (alertId != null && alertId > 0) {
            entity.setId(alertId);
            return alertId;
        }
        return -1;
    }

    public boolean resolveAlert(int alertId) throws SQLException {
        return procedureCaller.executeResolveInventoryAlert(alertId);
    }

    public boolean resolveAlertByPartId(int partInventoryId) throws SQLException {
        return procedureCaller.executeResolveInventoryAlertsByPart(partInventoryId);
    }

    @Override
    public boolean update(InventoryAlert entity) throws SQLException {
        if (entity.isResolved()) {
            return resolveAlert(entity.getId());
        }
        return false;
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return procedureCaller.executeDeleteInventoryAlert(id);
    }

    public boolean deleteResolvedAlerts() throws SQLException {
        return procedureCaller.executeDeleteResolvedInventoryAlerts();
    }

    public int countUnresolvedAlerts() throws SQLException {
        return viewLoader.countViewRowsWithCondition("vw_inventory_alerts", "is_resolved = false");
    }

    public int countUnresolvedAlertsByWorkshop(int workshopId) throws SQLException {
        return viewLoader.countViewRowsWithCondition("vw_inventory_alerts", "workshop_id = ? AND is_resolved = false", workshopId);
    }

    /**
     * Converts a List of Maps to a List of InventoryAlert objects.
     */
    private List<InventoryAlert> mapMapsToInventoryAlerts(List<Map<String, Object>> maps) {
        List<InventoryAlert> alerts = new ArrayList<>();
        if (maps == null) {
            return alerts;
        }
        for (Map<String, Object> map : maps) {
            InventoryAlert alert = mapMapToInventoryAlert(map);
            if (alert != null) {
                alerts.add(alert);
            }
        }
        return alerts;
    }

    /**
     * Converts a Map to an InventoryAlert object.
     */
    private InventoryAlert mapMapToInventoryAlert(Map<String, Object> map) {
        if (map == null) {
            return null;
        }

        InventoryAlert alert = new InventoryAlert();

        alert.setId(getIntValue(map, "id"));
        alert.setPartInventoryId(getIntValue(map, "part_inventory_id"));
        alert.setPartName(getStringValue(map, "part_name"));
        alert.setAlertType(getStringValue(map, "alert_type"));
        alert.setMessage(getStringValue(map, "message"));

        Boolean isResolved = (Boolean) map.get("is_resolved");
        alert.setResolved(isResolved != null && isResolved);

        alert.setResolvedAt(getLocalDateTimeValue(map, "resolved_at"));
        alert.setCreatedAt(getLocalDateTimeValue(map, "created_at"));
        alert.setUpdatedAt(getLocalDateTimeValue(map, "updated_at"));

        return alert;
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
    protected InventoryAlert mapRow(ResultSet rs) throws SQLException {
        InventoryAlert alert = new InventoryAlert();
        alert.setId(rs.getInt("id"));
        alert.setPartInventoryId(rs.getInt("part_inventory_id"));
        alert.setPartName(rs.getString("part_name"));
        alert.setAlertType(rs.getString("alert_type"));
        alert.setMessage(rs.getString("message"));
        alert.setResolved(rs.getBoolean("is_resolved"));

        if (rs.getTimestamp("resolved_at") != null) {
            alert.setResolvedAt(rs.getTimestamp("resolved_at").toLocalDateTime());
        }
        if (rs.getTimestamp("created_at") != null) {
            alert.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            alert.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        return alert;
    }
}