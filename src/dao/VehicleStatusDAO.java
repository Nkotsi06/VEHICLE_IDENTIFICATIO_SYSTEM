package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import database.ProcedureCaller;
import database.ViewLoader;
import models.VehicleStatus;

/**
 * VehicleStatusDAO - Uses ONLY stored procedures and views for all operations.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class VehicleStatusDAO extends BaseDAO<VehicleStatus> {

    private final ProcedureCaller procedureCaller;
    private final ViewLoader viewLoader;

    public VehicleStatusDAO() {
        this.procedureCaller = new ProcedureCaller();
        this.viewLoader = new ViewLoader();
    }

    @Override
    public VehicleStatus findById(int id) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_vehicle_status", "id = ?", id);
        if (results.isEmpty()) {
            return null;
        }
        return mapMapToVehicleStatus(results.get(0));
    }

    public VehicleStatus findByStatusName(String statusName) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_vehicle_status", "status_name = ?", statusName);
        if (results.isEmpty()) {
            return null;
        }
        return mapMapToVehicleStatus(results.get(0));
    }

    @Override
    public List<VehicleStatus> findAll() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadView("vw_vehicle_status");
        return mapMapsToVehicleStatuses(results);
    }

    @Override
    public boolean insert(VehicleStatus entity) throws SQLException {
        return procedureCaller.executeInsertVehicleStatus(
                entity.getStatusName(),
                entity.getDescription(),
                entity.getColorCode()
        );
    }

    @Override
    public boolean update(VehicleStatus entity) throws SQLException {
        return procedureCaller.executeUpdateVehicleStatus(
                entity.getId(),
                entity.getStatusName(),
                entity.getDescription(),
                entity.getColorCode()
        );
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return procedureCaller.executeDeleteVehicleStatus(id);
    }

    /**
     * Converts a List of Maps to a List of VehicleStatus objects.
     *
     * @param maps the list of maps from the view loader
     * @return list of VehicleStatus objects
     */
    private List<VehicleStatus> mapMapsToVehicleStatuses(List<Map<String, Object>> maps) {
        List<VehicleStatus> statuses = new ArrayList<>();
        if (maps == null) {
            return statuses;
        }
        for (Map<String, Object> map : maps) {
            VehicleStatus status = mapMapToVehicleStatus(map);
            if (status != null) {
                statuses.add(status);
            }
        }
        return statuses;
    }

    /**
     * Converts a Map to a VehicleStatus object.
     *
     * @param map the map from the view loader
     * @return VehicleStatus object
     */
    private VehicleStatus mapMapToVehicleStatus(Map<String, Object> map) {
        if (map == null) {
            return null;
        }

        VehicleStatus status = new VehicleStatus();

        status.setId(getIntValue(map, "id"));
        status.setStatusName(getStringValue(map, "status_name"));
        status.setDescription(getStringValue(map, "description"));
        status.setColorCode(getStringValue(map, "color_code"));
        status.setCreatedAt(getLocalDateTimeValue(map, "created_at"));
        status.setUpdatedAt(getLocalDateTimeValue(map, "updated_at"));

        return status;
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
    protected VehicleStatus mapRow(ResultSet rs) throws SQLException {
        VehicleStatus status = new VehicleStatus();
        status.setId(rs.getInt("id"));
        status.setStatusName(rs.getString("status_name"));
        status.setDescription(rs.getString("description"));
        status.setColorCode(rs.getString("color_code"));
        if (rs.getTimestamp("created_at") != null) {
            status.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            status.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        return status;
    }
}