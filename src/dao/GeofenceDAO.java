package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import models.GeofenceAlertEvent;
import models.GeofenceZone;

public class GeofenceDAO extends BaseDAO<GeofenceZone> {

    private GeofenceZoneDAO zoneDAO = new GeofenceZoneDAO();
    private GeofenceAlertEventDAO eventDAO = new GeofenceAlertEventDAO();

    @Override
    public GeofenceZone findById(int id) throws SQLException {
        return zoneDAO.findById(id);
    }

    @Override
    public List<GeofenceZone> findAll() throws SQLException {
        return zoneDAO.findAll();
    }

    public List<GeofenceZone> findActiveZones() throws SQLException {
        return zoneDAO.findActiveZones();
    }

    public List<GeofenceZone> findByZoneType(String zoneType) throws SQLException {
        return zoneDAO.findByZoneType(zoneType);
    }

    @Override
    public boolean insert(GeofenceZone entity) throws SQLException {
        return zoneDAO.insert(entity);
    }

    @Override
    public boolean update(GeofenceZone entity) throws SQLException {
        return zoneDAO.update(entity);
    }

    public boolean deactivateZone(int zoneId) throws SQLException {
        return zoneDAO.deactivateZone(zoneId);
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return zoneDAO.delete(id);
    }

    // Methods that return GeofenceAlertEvent
    public List<GeofenceAlertEvent> getAlertEvents() throws SQLException {
        return eventDAO.findAll();
    }

    public List<GeofenceAlertEvent> getAlertEventsByVehicle(int vehicleId) throws SQLException {
        return eventDAO.findByVehicleId(vehicleId);
    }

    public List<GeofenceAlertEvent> getAlertEventsByZone(int zoneId) throws SQLException {
        return eventDAO.findByZoneId(zoneId);
    }

    public List<GeofenceAlertEvent> getUnnotifiedAlerts() throws SQLException {
        return eventDAO.findUnnotifiedAlerts();
    }

    public boolean sendGeofenceAlert(int vehicleId, int zoneId, String alertType) throws SQLException {
        String sql = "CALL sp_send_geofence_alert(?, ?, ?)";
        Connection conn = null;
        java.sql.CallableStatement cs = null;
        try {
            conn = getConnection();
            cs = conn.prepareCall("{call sp_send_geofence_alert(?, ?, ?)}");
            cs.setInt(1, vehicleId);
            cs.setInt(2, zoneId);
            cs.setString(3, alertType);
            cs.execute();
            return true;
        } finally {
            if (cs != null) cs.close();
            closeResources(null, null, conn);
        }
    }

    public boolean markAlertAsNotified(int eventId) throws SQLException {
        return eventDAO.markAsNotified(eventId);
    }

    public boolean isVehicleInZone(int vehicleId, int zoneId) throws SQLException {
        String sql = "SELECT calculate_distance(v.current_location_lat, v.current_location_lng, " +
                "gz.center_lat, gz.center_lng) <= gz.radius_meters / 1000.0 as is_in_zone " +
                "FROM vehicles v, geofence_zones gz WHERE v.id = ? AND gz.id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, vehicleId);
            ps.setInt(2, zoneId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getBoolean("is_in_zone");
            }
            return false;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    public List<GeofenceZone> findZonesContainingPoint(double latitude, double longitude) throws SQLException {
        String sql = "SELECT * FROM geofence_zones WHERE is_active = true AND " +
                "calculate_distance(?, ?, center_lat, center_lng) <= radius_meters / 1000.0 " +
                "ORDER BY priority";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<GeofenceZone> zones = new ArrayList<>();
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setDouble(1, latitude);
            ps.setDouble(2, longitude);
            rs = ps.executeQuery();
            while (rs.next()) {
                zones.add(zoneDAO.mapRow(rs));
            }
            return zones;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    public int countActiveZones() throws SQLException {
        String sql = "SELECT COUNT(*) FROM geofence_zones WHERE is_active = true";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    public int countAlertEventsByDateRange(LocalDateTime startDate, LocalDateTime endDate) throws SQLException {
        String sql = "SELECT COUNT(*) FROM geofence_alert_events WHERE alert_timestamp BETWEEN ? AND ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setObject(1, startDate);
            ps.setObject(2, endDate);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    @Override
    protected GeofenceZone mapRow(ResultSet rs) throws SQLException {
        // Delegate to zoneDAO's mapRow method
        return zoneDAO.mapRow(rs);
    }
}