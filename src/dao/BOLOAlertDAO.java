package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import database.ProcedureCaller;
import database.ViewLoader;
import models.BOLOAlert;

/**
 * BOLOAlertDAO - Uses ONLY stored procedures and views for all operations.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class BOLOAlertDAO extends BaseDAO<BOLOAlert> {

    private final ProcedureCaller procedureCaller;
    private final ViewLoader viewLoader;

    public BOLOAlertDAO() {
        this.procedureCaller = new ProcedureCaller();
        this.viewLoader = new ViewLoader();
    }

    @Override
    public BOLOAlert findById(int id) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_bolo_alerts", "id = ?", id);
        return results.isEmpty() ? null : mapToBOLOAlert(results.get(0));
    }

    @Override
    public List<BOLOAlert> findAll() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadView("vw_bolo_alerts");
        return mapToBOLOAlertList(results);
    }

    public List<BOLOAlert> findActiveAlerts() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadView("vw_active_bolo_alerts");
        return mapToBOLOAlertList(results);
    }

    public List<BOLOAlert> findByVehicleId(int vehicleId) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_bolo_alerts",
                "vehicle_id = ? ORDER BY alert_date DESC", vehicleId);
        return mapToBOLOAlertList(results);
    }

    public List<BOLOAlert> findByPriority(String priority) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_active_bolo_alerts",
                "priority = ? ORDER BY alert_date DESC", priority);
        return mapToBOLOAlertList(results);
    }

    @Override
    public boolean insert(BOLOAlert entity) throws SQLException {
        Integer alertId = procedureCaller.executeGenerateBOLOAlert(
                entity.getVehicleId(),
                entity.getMessage(),
                entity.getPriority(),
                entity.getStolenVehicleId() > 0 ? entity.getStolenVehicleId() : null
        );
        if (alertId != null && alertId > 0) {
            entity.setId(alertId);
            return true;
        }
        return false;
    }

    public boolean cancelAlert(int alertId) throws SQLException {
        return procedureCaller.executeCancelBOLOAlert(alertId);
    }

    @Override
    public boolean update(BOLOAlert entity) throws SQLException {
        if ("CANCELLED".equals(entity.getStatus())) {
            return cancelAlert(entity.getId());
        }
        return false;
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return cancelAlert(id);
    }

    public int getActiveAlertCount() throws SQLException {
        return viewLoader.countViewRows("vw_active_bolo_alerts");
    }

    public List<BOLOAlert> findExpiringAlerts(int daysThreshold) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_active_bolo_alerts",
                "expiry_date <= CURRENT_DATE + INTERVAL '" + daysThreshold + " days'");
        return mapToBOLOAlertList(results);
    }

    // ============================================
    // HELPER METHODS FOR MAPPING
    // ============================================

    /**
     * Converts a Map to a BOLOAlert object.
     *
     * @param map The map containing the data
     * @return BOLOAlert object
     */
    private BOLOAlert mapToBOLOAlert(Map<String, Object> map) {
        if (map == null) return null;

        BOLOAlert alert = new BOLOAlert();

        if (map.get("id") != null) alert.setId(((Number) map.get("id")).intValue());
        if (map.get("vehicle_id") != null) alert.setVehicleId(((Number) map.get("vehicle_id")).intValue());
        if (map.get("registration_number") != null) alert.setRegistrationNumber(map.get("registration_number").toString());
        if (map.get("make") != null) alert.setMake(map.get("make").toString());
        if (map.get("model") != null) alert.setModel(map.get("model").toString());
        if (map.get("stolen_vehicle_id") != null) alert.setStolenVehicleId(((Number) map.get("stolen_vehicle_id")).intValue());
        if (map.get("stolen_case_number") != null) alert.setStolenCaseNumber(map.get("stolen_case_number").toString());
        if (map.get("message") != null) alert.setMessage(map.get("message").toString());
        if (map.get("priority") != null) alert.setPriority(map.get("priority").toString());
        if (map.get("status") != null) alert.setStatus(map.get("status").toString());
        if (map.get("distributed_to_all") != null) alert.setDistributedToAll((Boolean) map.get("distributed_to_all"));

        if (map.get("alert_date") instanceof java.sql.Date) {
            alert.setAlertDate(((java.sql.Date) map.get("alert_date")).toLocalDate());
        } else if (map.get("alert_date") instanceof LocalDate) {
            alert.setAlertDate((LocalDate) map.get("alert_date"));
        }

        if (map.get("expiry_date") instanceof java.sql.Date) {
            alert.setExpiryDate(((java.sql.Date) map.get("expiry_date")).toLocalDate());
        } else if (map.get("expiry_date") instanceof LocalDate) {
            alert.setExpiryDate((LocalDate) map.get("expiry_date"));
        }

        if (map.get("created_at") instanceof java.sql.Timestamp) {
            alert.setCreatedAt(((java.sql.Timestamp) map.get("created_at")).toLocalDateTime());
        }
        if (map.get("updated_at") instanceof java.sql.Timestamp) {
            alert.setUpdatedAt(((java.sql.Timestamp) map.get("updated_at")).toLocalDateTime());
        }

        // Update JavaFX properties
        alert.vehicleIdProperty().set(alert.getVehicleId());
        alert.registrationNumberProperty().set(alert.getRegistrationNumber());
        alert.makeProperty().set(alert.getMake());
        alert.modelProperty().set(alert.getModel());
        alert.messageProperty().set(alert.getMessage());
        alert.priorityProperty().set(alert.getPriority());
        alert.statusProperty().set(alert.getStatus());
        alert.expiryDateProperty().set(alert.getExpiryDate());
        alert.distributedProperty().set(alert.isDistributedToAll());

        return alert;
    }

    /**
     * Converts a list of Maps to a list of BOLOAlert objects.
     *
     * @param maps List of maps containing the data
     * @return List of BOLOAlert objects
     */
    private List<BOLOAlert> mapToBOLOAlertList(List<Map<String, Object>> maps) {
        List<BOLOAlert> alerts = new ArrayList<>();
        if (maps != null) {
            for (Map<String, Object> map : maps) {
                alerts.add(mapToBOLOAlert(map));
            }
        }
        return alerts;
    }

    @Override
    protected BOLOAlert mapRow(ResultSet rs) throws SQLException {
        BOLOAlert alert = new BOLOAlert();
        alert.setId(rs.getInt("id"));
        alert.setVehicleId(rs.getInt("vehicle_id"));
        alert.setRegistrationNumber(rs.getString("registration_number"));
        alert.setMake(rs.getString("make"));
        alert.setModel(rs.getString("model"));

        if (rs.getDate("alert_date") != null) {
            alert.setAlertDate(rs.getDate("alert_date").toLocalDate());
        }
        if (rs.getDate("expiry_date") != null) {
            alert.setExpiryDate(rs.getDate("expiry_date").toLocalDate());
        }
        alert.setMessage(rs.getString("message"));
        alert.setPriority(rs.getString("priority"));
        alert.setStatus(rs.getString("status"));

        if (rs.getTimestamp("created_at") != null) {
            alert.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            alert.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        return alert;
    }
}