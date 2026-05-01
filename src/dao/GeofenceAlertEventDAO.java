package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import models.GeofenceAlertEvent;

public class GeofenceAlertEventDAO extends BaseDAO<GeofenceAlertEvent> {

    @Override
    public GeofenceAlertEvent findById(int id) throws SQLException {
        String sql = "SELECT * FROM vw_geofence_alerts WHERE id = ?";
        return executeQuerySingle(sql, id);
    }

    @Override
    public List<GeofenceAlertEvent> findAll() throws SQLException {
        String sql = "SELECT * FROM vw_geofence_alerts ORDER BY alert_timestamp DESC";
        return executeQuery(sql);
    }

    public List<GeofenceAlertEvent> findByVehicleId(int vehicleId) throws SQLException {
        String sql = "SELECT * FROM vw_geofence_alerts WHERE vehicle_id = ? ORDER BY alert_timestamp DESC";
        return executeQuery(sql, vehicleId);
    }

    public List<GeofenceAlertEvent> findByZoneId(int zoneId) throws SQLException {
        String sql = "SELECT * FROM vw_geofence_alerts WHERE geofence_zone_id = ? ORDER BY alert_timestamp DESC";
        return executeQuery(sql, zoneId);
    }

    public List<GeofenceAlertEvent> findUnnotifiedAlerts() throws SQLException {
        String sql = "SELECT * FROM vw_geofence_alerts WHERE is_notified = false ORDER BY alert_timestamp";
        return executeQuery(sql);
    }

    @Override
    public boolean insert(GeofenceAlertEvent entity) throws SQLException {
        return executeProcedure("sp_send_geofence_alert",
                entity.getVehicleId(),
                entity.getGeofenceZoneId(),
                entity.getAlertType()
        );
    }

    public boolean markAsNotified(int eventId) throws SQLException {
        String sql = "UPDATE geofence_alert_events SET is_notified = true WHERE id = ?";
        int result = executeUpdate(sql, eventId);
        return result > 0;
    }

    @Override
    public boolean update(GeofenceAlertEvent entity) throws SQLException {
        String sql = "UPDATE geofence_alert_events SET is_notified = ? WHERE id = ?";
        int result = executeUpdate(sql, entity.isNotified(), entity.getId());
        return result > 0;
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM geofence_alert_events WHERE id = ?";
        int result = executeUpdate(sql, id);
        return result > 0;
    }

    @Override
    protected GeofenceAlertEvent mapRow(ResultSet rs) throws SQLException {
        GeofenceAlertEvent event = new GeofenceAlertEvent();
        event.setId(rs.getInt("id"));
        event.setGeofenceZoneId(rs.getInt("geofence_zone_id"));
        event.setZoneName(rs.getString("zone_name"));
        event.setZoneType(rs.getString("zone_type"));
        event.setVehicleId(rs.getInt("vehicle_id"));
        event.setRegistrationNumber(rs.getString("registration_number"));
        event.setAlertType(rs.getString("alert_type"));

        if (rs.getTimestamp("alert_timestamp") != null) {
            event.setAlertTimestamp(rs.getTimestamp("alert_timestamp").toLocalDateTime());
        }
        if (rs.getObject("vehicle_location_lat") != null) {
            event.setVehicleLocationLat(rs.getDouble("vehicle_location_lat"));
        }
        if (rs.getObject("vehicle_location_lng") != null) {
            event.setVehicleLocationLng(rs.getDouble("vehicle_location_lng"));
        }
        event.setNotified(rs.getBoolean("is_notified"));

        if (rs.getTimestamp("created_at") != null) {
            event.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            event.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        return event;
    }
}