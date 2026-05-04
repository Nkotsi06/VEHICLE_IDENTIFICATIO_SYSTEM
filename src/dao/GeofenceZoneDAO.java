package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import database.ProcedureCaller;
import database.ViewLoader;
import models.GeofenceZone;

/**
 * GeofenceZoneDAO - Uses ONLY stored procedures and views for all operations.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class GeofenceZoneDAO extends BaseDAO<GeofenceZone> {

    private final ProcedureCaller procedureCaller;
    private final ViewLoader viewLoader;

    public GeofenceZoneDAO() {
        this.procedureCaller = new ProcedureCaller();
        this.viewLoader = new ViewLoader();
    }

    @Override
    public GeofenceZone findById(int id) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_geofence_zones", "id = ?", id);
        return results.isEmpty() ? null : mapToGeofenceZone(results.get(0));
    }

    public GeofenceZone findByZoneName(String zoneName) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_geofence_zones", "zone_name = ?", zoneName);
        return results.isEmpty() ? null : mapToGeofenceZone(results.get(0));
    }

    @Override
    public List<GeofenceZone> findAll() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadView("vw_geofence_zones");
        return mapToGeofenceZoneList(results);
    }

    public List<GeofenceZone> findByZoneType(String zoneType) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_geofence_zones",
                "zone_type = ? ORDER BY zone_name", zoneType);
        return mapToGeofenceZoneList(results);
    }

    public List<GeofenceZone> findActiveZones() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_geofence_zones",
                "is_active = true ORDER BY priority, zone_name");
        return mapToGeofenceZoneList(results);
    }

    @Override
    public boolean insert(GeofenceZone entity) throws SQLException {
        Integer zoneId = procedureCaller.executeCreateGeofenceZone(
                entity.getZoneName(),
                entity.getCenterLat(),
                entity.getCenterLng(),
                entity.getRadiusMeters(),
                entity.getZoneType(),
                entity.getPriority()
        );
        if (zoneId != null && zoneId > 0) {
            entity.setId(zoneId);
            return true;
        }
        return false;
    }

    @Override
    public boolean update(GeofenceZone entity) throws SQLException {
        return procedureCaller.executeUpdateGeofenceZone(
                entity.getId(),
                entity.getZoneName(),
                entity.getCenterLat(),
                entity.getCenterLng(),
                entity.getRadiusMeters(),
                entity.getZoneType(),
                entity.getPriority(),
                entity.isActive()
        );
    }

    public boolean deactivateZone(int zoneId) throws SQLException {
        return procedureCaller.executeDeactivateGeofenceZone(zoneId);
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return procedureCaller.executeDeleteGeofenceZone(id);
    }

    public List<GeofenceZone> findZonesContainingPoint(double latitude, double longitude) throws SQLException {
        // Use stored procedure - NO direct SQL
        return procedureCaller.executeFindZonesContainingPoint(latitude, longitude);
    }

    public int countActiveZones() throws SQLException {
        return viewLoader.countViewRowsWithCondition("vw_geofence_zones", "is_active = true");
    }

    // ============================================
    // HELPER METHODS FOR MAPPING
    // ============================================

    /**
     * Converts a Map to a GeofenceZone object.
     *
     * @param map The map containing the data
     * @return GeofenceZone object
     */
    private GeofenceZone mapToGeofenceZone(Map<String, Object> map) {
        if (map == null) {
            return null;
        }

        GeofenceZone zone = new GeofenceZone();

        if (map.get("id") != null) {
            zone.setId(((Number) map.get("id")).intValue());
        }
        if (map.get("zone_name") != null) {
            zone.setZoneName(map.get("zone_name").toString());
        }
        if (map.get("center_lat") != null) {
            zone.setCenterLat(((Number) map.get("center_lat")).doubleValue());
        }
        if (map.get("center_lng") != null) {
            zone.setCenterLng(((Number) map.get("center_lng")).doubleValue());
        }
        if (map.get("radius_meters") != null) {
            zone.setRadiusMeters(((Number) map.get("radius_meters")).intValue());
        }
        if (map.get("zone_type") != null) {
            zone.setZoneType(map.get("zone_type").toString());
        }
        if (map.get("priority") != null) {
            zone.setPriority(((Number) map.get("priority")).intValue());
        }
        if (map.get("is_active") != null) {
            zone.setActive((Boolean) map.get("is_active"));
        }
        if (map.get("created_at") != null && map.get("created_at") instanceof java.sql.Timestamp) {
            zone.setCreatedAt(((java.sql.Timestamp) map.get("created_at")).toLocalDateTime());
        }
        if (map.get("updated_at") != null && map.get("updated_at") instanceof java.sql.Timestamp) {
            zone.setUpdatedAt(((java.sql.Timestamp) map.get("updated_at")).toLocalDateTime());
        }

        return zone;
    }

    /**
     * Converts a list of Maps to a list of GeofenceZone objects.
     *
     * @param maps List of maps containing the data
     * @return List of GeofenceZone objects
     */
    private List<GeofenceZone> mapToGeofenceZoneList(List<Map<String, Object>> maps) {
        List<GeofenceZone> zones = new ArrayList<>();
        if (maps != null) {
            for (Map<String, Object> map : maps) {
                zones.add(mapToGeofenceZone(map));
            }
        }
        return zones;
    }

    @Override
    protected GeofenceZone mapRow(ResultSet rs) throws SQLException {
        // This method is kept for compatibility but is not used when using views
        // The actual mapping is done via mapToGeofenceZone
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