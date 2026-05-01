package dao;

import java.sql.*;
import java.util.List;
import models.PoliceUnit;

public class PoliceUnitDAO extends BaseDAO<PoliceUnit> {

    @Override
    public PoliceUnit findById(int id) throws SQLException {
        String sql = "SELECT * FROM police_units WHERE id = ?";
        return executeQuerySingle(sql, id);
    }

    public PoliceUnit findByUnitId(String unitId) throws SQLException {
        String sql = "SELECT * FROM police_units WHERE unit_id = ?";
        return executeQuerySingle(sql, unitId);
    }

    @Override
    public List<PoliceUnit> findAll() throws SQLException {
        String sql = "SELECT * FROM police_units ORDER BY officer_name";
        return executeQuery(sql);
    }

    public List<PoliceUnit> findByStatus(String status) throws SQLException {
        String sql = "SELECT * FROM police_units WHERE status = ? ORDER BY officer_name";
        return executeQuery(sql, status);
    }

    public List<PoliceUnit> findAvailableUnits() throws SQLException {
        String sql = "SELECT * FROM police_units WHERE status IN ('AVAILABLE', 'ON_PATROL') ORDER BY officer_name";
        return executeQuery(sql);
    }

    @Override
    public boolean insert(PoliceUnit entity) throws SQLException {
        String sql = "INSERT INTO police_units (unit_id, officer_name, badge_number, status) VALUES (?, ?, ?, ?)";
        int result = executeUpdate(sql, entity.getUnitId(), entity.getOfficerName(), entity.getBadgeNumber(), entity.getStatus());
        return result > 0;
    }

    public boolean updateLocation(String unitId, double latitude, double longitude) throws SQLException {
        String sql = "CALL sp_update_police_unit_location(?, ?, ?)";
        int result = executeUpdate(sql, unitId, latitude, longitude);
        return result >= 0;
    }

    public boolean updateStatus(int unitId, String status) throws SQLException {
        String sql = "UPDATE police_units SET status = ? WHERE id = ?";
        int result = executeUpdate(sql, status, unitId);
        return result > 0;
    }

    @Override
    public boolean update(PoliceUnit entity) throws SQLException {
        String sql = "UPDATE police_units SET officer_name = ?, badge_number = ?, status = ?, device_id = ? WHERE id = ?";
        int result = executeUpdate(sql,
                entity.getOfficerName(),
                entity.getBadgeNumber(),
                entity.getStatus(),
                entity.getDeviceId(),
                entity.getId()
        );
        return result > 0;
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM police_units WHERE id = ?";
        int result = executeUpdate(sql, id);
        return result > 0;
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