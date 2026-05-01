package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import models.GeofenceZone;

public class GeofenceZoneDAO extends BaseDAO<GeofenceZone> {

    @Override
    public GeofenceZone findById(int id) throws SQLException {
        String sql = "SELECT * FROM geofence_zones WHERE id = ?";
        return executeQuerySingle(sql, id);
    }

    public GeofenceZone findByZoneName(String zoneName) throws SQLException {
        String sql = "SELECT * FROM geofence_zones WHERE zone_name = ?";
        return executeQuerySingle(sql, zoneName);
    }

    @Override
    public List<GeofenceZone> findAll() throws SQLException {
        String sql = "SELECT * FROM geofence_zones ORDER BY priority, zone_name";
        return executeQuery(sql);
    }

    public List<GeofenceZone> findByZoneType(String zoneType) throws SQLException {
        String sql = "SELECT * FROM geofence_zones WHERE zone_type = ? ORDER BY zone_name";
        return executeQuery(sql, zoneType);
    }

    public List<GeofenceZone> findActiveZones() throws SQLException {
        String sql = "SELECT * FROM geofence_zones WHERE is_active = true ORDER BY priority, zone_name";
        return executeQuery(sql);
    }

    @Override
    public boolean insert(GeofenceZone entity) throws SQLException {
        String sql = "INSERT INTO geofence_zones (zone_name, center_lat, center_lng, radius_meters, zone_type, priority, is_active) VALUES (?, ?, ?, ?, ?, ?, ?)";
        int result = executeUpdate(sql,
                entity.getZoneName(),
                entity.getCenterLat(),
                entity.getCenterLng(),
                entity.getRadiusMeters(),
                entity.getZoneType(),
                entity.getPriority(),
                entity.isActive()
        );
        return result > 0;
    }

    @Override
    public boolean update(GeofenceZone entity) throws SQLException {
        String sql = "UPDATE geofence_zones SET zone_name = ?, center_lat = ?, center_lng = ?, radius_meters = ?, zone_type = ?, priority = ?, is_active = ? WHERE id = ?";
        int result = executeUpdate(sql,
                entity.getZoneName(),
                entity.getCenterLat(),
                entity.getCenterLng(),
                entity.getRadiusMeters(),
                entity.getZoneType(),
                entity.getPriority(),
                entity.isActive(),
                entity.getId()
        );
        return result > 0;
    }

    public boolean deactivateZone(int zoneId) throws SQLException {
        String sql = "UPDATE geofence_zones SET is_active = false WHERE id = ?";
        int result = executeUpdate(sql, zoneId);
        return result > 0;
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM geofence_zones WHERE id = ?";
        int result = executeUpdate(sql, id);
        return result > 0;
    }

    @Override
    protected GeofenceZone mapRow(ResultSet rs) throws SQLException {
        GeofenceZone zone = new GeofenceZone();
        zone.setId(rs.getInt("id"));
        zone.setZoneName(rs.getString("zone_name"));
        zone.setCenterLat(rs.getDouble("center_lat"));
        zone.setCenterLng(rs.getDouble("center_lng"));
        zone.setRadiusMeters(rs.getInt("radius_meters"));
        zone.setZoneType(rs.getString("zone_type"));
        zone.setPriority(rs.getInt("priority"));
        zone.setActive(rs.getBoolean("is_active"));

        if (rs.getTimestamp("created_at") != null) {
            zone.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            zone.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        return zone;
    }
}