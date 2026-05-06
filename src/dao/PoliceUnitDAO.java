package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import database.ProcedureCaller;
import database.ViewLoader;
import models.PoliceUnit;

/**
 * PoliceUnitDAO - Uses ONLY stored procedures and views for all operations.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class PoliceUnitDAO extends BaseDAO<PoliceUnit> {

    private final ProcedureCaller procedureCaller;
    private final ViewLoader viewLoader;

    public PoliceUnitDAO() {
        this.procedureCaller = new ProcedureCaller();
        this.viewLoader = new ViewLoader();
    }

    @Override
    public PoliceUnit findById(int id) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_police_units", "id = ?", id);
        if (results.isEmpty()) {
            return null;
        }
        return mapMapToPoliceUnit(results.get(0));
    }

    public PoliceUnit findByUnitId(String unitId) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_police_units", "unit_id = ?", unitId);
        if (results.isEmpty()) {
            return null;
        }
        return mapMapToPoliceUnit(results.get(0));
    }

    @Override
    public List<PoliceUnit> findAll() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadView("vw_police_units");
        return mapMapsToPoliceUnits(results);
    }

    public List<PoliceUnit> findByStatus(String status) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_police_units", "status = ? ORDER BY officer_name", status);
        return mapMapsToPoliceUnits(results);
    }

    public List<PoliceUnit> findAvailableUnits() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_police_units", "status IN ('AVAILABLE', 'ON_PATROL') ORDER BY officer_name");
        return mapMapsToPoliceUnits(results);
    }

    @Override
    public boolean insert(PoliceUnit entity) throws SQLException {
        return procedureCaller.executeRegisterPoliceUnit(
                entity.getUnitId(),
                entity.getOfficerName(),
                entity.getBadgeNumber(),
                entity.getDeviceId()
        );
    }

    public boolean updateLocation(String unitId, double latitude, double longitude) throws SQLException {
        return procedureCaller.executeUpdatePoliceUnitLocation(unitId, latitude, longitude);
    }

    public boolean updateStatus(int unitId, String status) throws SQLException {
        return procedureCaller.executeUpdatePoliceUnitStatus(unitId, status);
    }

    @Override
    public boolean update(PoliceUnit entity) throws SQLException {
        return procedureCaller.executeUpdatePoliceUnit(
                entity.getId(),
                entity.getOfficerName(),
                entity.getBadgeNumber(),
                entity.getStatus(),
                entity.getDeviceId()
        );
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return procedureCaller.executeDeletePoliceUnit(id);
    }

    /**
     * Converts a List of Maps to a List of PoliceUnit objects.
     */
    private List<PoliceUnit> mapMapsToPoliceUnits(List<Map<String, Object>> maps) {
        List<PoliceUnit> units = new ArrayList<>();
        if (maps == null) {
            return units;
        }
        for (Map<String, Object> map : maps) {
            PoliceUnit unit = mapMapToPoliceUnit(map);
            if (unit != null) {
                units.add(unit);
            }
        }
        return units;
    }

    /**
     * Converts a Map to a PoliceUnit object.
     */
    private PoliceUnit mapMapToPoliceUnit(Map<String, Object> map) {
        if (map == null) {
            return null;
        }

        PoliceUnit unit = new PoliceUnit();

        unit.setId(getIntValue(map, "id"));
        unit.setUnitId(getStringValue(map, "unit_id"));
        unit.setOfficerName(getStringValue(map, "officer_name"));
        unit.setBadgeNumber(getStringValue(map, "badge_number"));
        unit.setStatus(getStringValue(map, "status"));
        unit.setDeviceId(getStringValue(map, "device_id"));

        unit.setCurrentLocationLat(getDoubleValue(map, "current_location_lat"));
        unit.setCurrentLocationLng(getDoubleValue(map, "current_location_lng"));
        unit.setLastLocationUpdate(getLocalDateTimeValue(map, "last_location_update"));
        unit.setCreatedAt(getLocalDateTimeValue(map, "created_at"));
        unit.setUpdatedAt(getLocalDateTimeValue(map, "updated_at"));

        return unit;
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

    private LocalDateTime getLocalDateTimeValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof java.sql.Timestamp) return ((java.sql.Timestamp) value).toLocalDateTime();
        if (value instanceof LocalDateTime) return (LocalDateTime) value;
        return null;
    }

    @Override
    protected PoliceUnit mapRow(ResultSet rs) throws SQLException {
        PoliceUnit unit = new PoliceUnit();
        unit.setId(rs.getInt("id"));
        unit.setUnitId(rs.getString("unit_id"));
        unit.setOfficerName(rs.getString("officer_name"));
        unit.setBadgeNumber(rs.getString("badge_number"));

        if (rs.getObject("current_location_lat") != null) {
            unit.setCurrentLocationLat(rs.getDouble("current_location_lat"));
        }
        if (rs.getObject("current_location_lng") != null) {
            unit.setCurrentLocationLng(rs.getDouble("current_location_lng"));
        }
        if (rs.getTimestamp("last_location_update") != null) {
            unit.setLastLocationUpdate(rs.getTimestamp("last_location_update").toLocalDateTime());
        }
        unit.setStatus(rs.getString("status"));
        unit.setDeviceId(rs.getString("device_id"));

        if (rs.getTimestamp("created_at") != null) {
            unit.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            unit.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        return unit;
    }
}