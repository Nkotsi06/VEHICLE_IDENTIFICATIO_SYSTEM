package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MobilePatrolSyncDAO extends BaseDAO<Object> {

    @Override
    public Object findById(int id) throws SQLException {
        return null;
    }

    @Override
    public List<Object> findAll() throws SQLException {
        return null;
    }

    public List<Map<String, Object>> getMobilePatrolData(String unitId) throws SQLException {
        String sql = "SELECT * FROM vw_mobile_patrol_sync WHERE unit_id = ?";
        List<Map<String, Object>> results = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, unitId);
            rs = ps.executeQuery();

            while (rs.next()) {
                Map<String, Object> data = new HashMap<>();
                data.put("police_unit_id", rs.getInt("police_unit_id"));
                data.put("unit_id", rs.getString("unit_id"));
                data.put("officer_name", rs.getString("officer_name"));
                data.put("badge_number", rs.getString("badge_number"));
                data.put("current_location_lat", rs.getDouble("current_location_lat"));
                data.put("current_location_lng", rs.getDouble("current_location_lng"));
                data.put("status", rs.getString("status"));
                data.put("nearby_vehicle_id", rs.getInt("nearby_vehicle_id"));
                data.put("registration_number", rs.getString("registration_number"));
                data.put("vehicle_risk_score", rs.getDouble("vehicle_risk_score"));
                data.put("is_stolen", rs.getBoolean("is_stolen"));
                data.put("bolo_message", rs.getString("bolo_message"));
                data.put("bolo_priority", rs.getString("bolo_priority"));
                results.add(data);
            }

            return results;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    public boolean syncPoliceUnitLocation(String unitId, double latitude, double longitude) throws SQLException {
        String sql = "CALL sp_update_police_unit_location(?, ?, ?)";
        int result = executeUpdate(sql, unitId, latitude, longitude);
        return result >= 0;
    }

    public boolean queueSyncData(String unitId, String actionType, String actionData) throws SQLException {
        String sql = "INSERT INTO mobile_patrol_sync_queue (police_unit_id, action_type, action_data, sync_status) " +
                "SELECT id, ?, ?, 'PENDING' FROM police_units WHERE unit_id = ?";
        int result = executeUpdate(sql, actionType, actionData, unitId);
        return result > 0;
    }

    public List<Map<String, Object>> getPendingSyncData(String unitId) throws SQLException {
        String sql = "SELECT mpsq.id, mpsq.action_type, mpsq.action_data, mpsq.created_at " +
                "FROM mobile_patrol_sync_queue mpsq " +
                "JOIN police_units pu ON mpsq.police_unit_id = pu.id " +
                "WHERE pu.unit_id = ? AND mpsq.sync_status = 'PENDING' " +
                "ORDER BY mpsq.created_at";

        List<Map<String, Object>> results = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, unitId);
            rs = ps.executeQuery();

            while (rs.next()) {
                Map<String, Object> data = new HashMap<>();
                data.put("id", rs.getInt("id"));
                data.put("action_type", rs.getString("action_type"));
                data.put("action_data", rs.getString("action_data"));
                data.put("created_at", rs.getTimestamp("created_at").toString());
                results.add(data);
            }

            return results;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    // ADD THIS METHOD - fixes the error
    public List<String> getPendingSyncItems(String unitId) throws SQLException {
        String sql = "SELECT mpsq.action_type, mpsq.created_at " +
                "FROM mobile_patrol_sync_queue mpsq " +
                "JOIN police_units pu ON mpsq.police_unit_id = pu.id " +
                "WHERE pu.unit_id = ? AND mpsq.sync_status = 'PENDING' " +
                "ORDER BY mpsq.created_at";

        List<String> results = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, unitId);
            rs = ps.executeQuery();

            while (rs.next()) {
                String item = rs.getString("action_type") + " - " + rs.getTimestamp("created_at");
                results.add(item);
            }

            return results;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    public boolean markSyncCompleted(int syncId) throws SQLException {
        String sql = "UPDATE mobile_patrol_sync_queue SET sync_status = 'SYNCED', synced_at = CURRENT_TIMESTAMP WHERE id = ?";
        int result = executeUpdate(sql, syncId);
        return result > 0;
    }

    public boolean registerPoliceUnit(String unitId, String officerName, String badgeNumber, String deviceId) throws SQLException {
        String sql = "INSERT INTO police_units (unit_id, officer_name, badge_number, device_id, status) VALUES (?, ?, ?, ?, 'AVAILABLE')";
        int result = executeUpdate(sql, unitId, officerName, badgeNumber, deviceId);
        return result > 0;
    }

    public boolean syncUnitData(String unitId) throws SQLException {
        String sql = "UPDATE mobile_patrol_sync_queue SET sync_status = 'SYNCED', synced_at = CURRENT_TIMESTAMP " +
                "WHERE police_unit_id = (SELECT id FROM police_units WHERE unit_id = ?) AND sync_status = 'PENDING'";
        int result = executeUpdate(sql, unitId);
        return result >= 0;
    }

    public boolean sendBroadcastAlert(String unitId, String message) throws SQLException {
        String sql = "INSERT INTO notifications (user_id, message, type, reference_id) " +
                "SELECT u.id, ?, 'BROADCAST_ALERT', 0 FROM users u WHERE u.role = 'POLICE' AND u.is_active = true";
        int result = executeUpdate(sql, message);
        return result > 0;
    }

    @Override
    public boolean insert(Object entity) throws SQLException {
        return false;
    }

    @Override
    public boolean update(Object entity) throws SQLException {
        return false;
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return false;
    }

    @Override
    protected Object mapRow(ResultSet rs) throws SQLException {
        return null;
    }
}