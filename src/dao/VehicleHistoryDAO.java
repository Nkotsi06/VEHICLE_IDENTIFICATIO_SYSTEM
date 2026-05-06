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
import models.VehicleHistory;

/**
 * VehicleHistoryDAO - Uses ONLY stored procedures and views for all operations.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class VehicleHistoryDAO extends BaseDAO<VehicleHistory> {

    private final ProcedureCaller procedureCaller;
    private final ViewLoader viewLoader;

    public VehicleHistoryDAO() {
        this.procedureCaller = new ProcedureCaller();
        this.viewLoader = new ViewLoader();
    }

    @Override
    public VehicleHistory findById(int id) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_vehicle_history", "id = ?", id);
        if (results.isEmpty()) {
            return null;
        }
        return mapMapToVehicleHistory(results.get(0));
    }

    @Override
    public List<VehicleHistory> findAll() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadView("vw_vehicle_history");
        return mapMapsToVehicleHistories(results);
    }

    public List<VehicleHistory> findByVehicleId(int vehicleId) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_vehicle_history", "vehicle_id = ? ORDER BY event_date DESC", vehicleId);
        return mapMapsToVehicleHistories(results);
    }

    public List<VehicleHistory> findByVehicleIdAndDateRange(int vehicleId, LocalDate startDate, LocalDate endDate) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_vehicle_history", "vehicle_id = ? AND event_date BETWEEN ? AND ? ORDER BY event_date DESC", vehicleId, startDate, endDate);
        return mapMapsToVehicleHistories(results);
    }

    public List<VehicleHistory> findByEventType(String eventType) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_vehicle_history", "event_type = ? ORDER BY event_date DESC", eventType);
        return mapMapsToVehicleHistories(results);
    }

    @Override
    public boolean insert(VehicleHistory entity) throws SQLException {
        return procedureCaller.executeInsertVehicleHistory(
                entity.getVehicleId(),
                entity.getEventType(),
                entity.getEventDate(),
                entity.getDescription(),
                entity.getDetails()
        );
    }

    @Override
    public boolean update(VehicleHistory entity) throws SQLException {
        return procedureCaller.executeUpdateVehicleHistory(
                entity.getId(),
                entity.getDescription(),
                entity.getDetails()
        );
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return procedureCaller.executeDeleteVehicleHistory(id);
    }

    public boolean deleteByVehicleId(int vehicleId) throws SQLException {
        return procedureCaller.executeDeleteVehicleHistoryByVehicle(vehicleId);
    }

    /**
     * Converts a List of Maps to a List of VehicleHistory objects.
     */
    private List<VehicleHistory> mapMapsToVehicleHistories(List<Map<String, Object>> maps) {
        List<VehicleHistory> histories = new ArrayList<>();
        if (maps == null) {
            return histories;
        }
        for (Map<String, Object> map : maps) {
            VehicleHistory history = mapMapToVehicleHistory(map);
            if (history != null) {
                histories.add(history);
            }
        }
        return histories;
    }

    /**
     * Converts a Map to a VehicleHistory object.
     */
    private VehicleHistory mapMapToVehicleHistory(Map<String, Object> map) {
        if (map == null) {
            return null;
        }

        VehicleHistory history = new VehicleHistory();

        history.setId(getIntValue(map, "id"));
        history.setVehicleId(getIntValue(map, "vehicle_id"));
        history.setRegistrationNumber(getStringValue(map, "registration_number"));
        history.setEventType(getStringValue(map, "event_type"));
        history.setDescription(getStringValue(map, "description"));
        history.setDetails(getStringValue(map, "details"));

        history.setEventDate(getLocalDateValue(map, "event_date"));
        history.setCreatedAt(getLocalDateTimeValue(map, "created_at"));
        history.setUpdatedAt(getLocalDateTimeValue(map, "updated_at"));

        return history;
    }

    private Integer getIntValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return 0;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Long) return ((Long) value).intValue();
        if (value instanceof Number) return ((Number) value).intValue();
        return 0;
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
    protected VehicleHistory mapRow(ResultSet rs) throws SQLException {
        VehicleHistory history = new VehicleHistory();
        history.setId(rs.getInt("id"));
        history.setVehicleId(rs.getInt("vehicle_id"));
        history.setRegistrationNumber(rs.getString("registration_number"));
        history.setEventType(rs.getString("event_type"));

        if (rs.getDate("event_date") != null) {
            history.setEventDate(rs.getDate("event_date").toLocalDate());
        }
        history.setDescription(rs.getString("description"));
        history.setDetails(rs.getString("details"));

        if (rs.getTimestamp("created_at") != null) {
            history.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            history.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        return history;
    }
}