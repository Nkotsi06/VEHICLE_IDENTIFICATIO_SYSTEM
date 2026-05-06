package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import database.ProcedureCaller;
import database.ViewLoader;
import models.GeofenceAlertEvent;

/**
 * GeofenceAlertEventDAO - Uses ONLY stored procedures and views for all operations.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class GeofenceAlertEventDAO extends BaseDAO<GeofenceAlertEvent> {

    private final ProcedureCaller procedureCaller;
    private final ViewLoader viewLoader;

    public GeofenceAlertEventDAO() {
        this.procedureCaller = new ProcedureCaller();
        this.viewLoader = new ViewLoader();
    }

    @Override
    public GeofenceAlertEvent findById(int id) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_geofence_alerts", "id = ?", id);
        return results.isEmpty() ? null : mapToGeofenceAlertEvent(results.get(0));
    }

    @Override
    public List<GeofenceAlertEvent> findAll() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadView("vw_geofence_alerts");
        return mapToGeofenceAlertEventList(results);
    }

    public List<GeofenceAlertEvent> findByVehicleId(int vehicleId) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_geofence_alerts",
                "vehicle_id = ? ORDER BY alert_timestamp DESC", vehicleId);
        return mapToGeofenceAlertEventList(results);
    }

    public List<GeofenceAlertEvent> findByZoneId(int zoneId) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_geofence_alerts",
                "geofence_zone_id = ? ORDER BY alert_timestamp DESC", zoneId);
        return mapToGeofenceAlertEventList(results);
    }

    public List<GeofenceAlertEvent> findUnnotifiedAlerts() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_geofence_alerts",
                "is_notified = false ORDER BY alert_timestamp");
        return mapToGeofenceAlertEventList(results);
    }

    public int countByDateRange(LocalDateTime startDate, LocalDateTime endDate) throws SQLException {
        return viewLoader.countViewRowsWithCondition("vw_geofence_alerts",
                "alert_timestamp BETWEEN ? AND ?", startDate, endDate);
    }

    @Override
    public boolean insert(GeofenceAlertEvent entity) throws SQLException {
        return procedureCaller.executeSendGeofenceAlert(
                entity.getVehicleId(),
                entity.getGeofenceZoneId(),
                entity.getAlertType()
        );
    }

    public boolean markAsNotified(int eventId) throws SQLException {
        return procedureCaller.executeMarkGeofenceAlertNotified(eventId);
    }

    @Override
    public boolean update(GeofenceAlertEvent entity) throws SQLException {
        return markAsNotified(entity.getId());
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return procedureCaller.executeDeleteGeofenceAlert(id);
    }

    public int getUnnotifiedAlertCount() throws SQLException {
        return viewLoader.countViewRowsWithCondition("vw_geofence_alerts", "is_notified = false");
    }

    public int getAlertCountByZone(int zoneId) throws SQLException {
        return viewLoader.countViewRowsWithCondition("vw_geofence_alerts", "geofence_zone_id = ?", zoneId);
    }

    // ============================================
    // HELPER METHODS FOR MAPPING
    // ============================================

    private GeofenceAlertEvent mapToGeofenceAlertEvent(Map<String, Object> map) {
        if (map == null) return null;

        GeofenceAlertEvent event = new GeofenceAlertEvent();

        if (map.get("id") != null) event.setId(((Number) map.get("id")).intValue());
        if (map.get("geofence_zone_id") != null) event.setGeofenceZoneId(((Number) map.get("geofence_zone_id")).intValue());
        if (map.get("zone_name") != null) event.setZoneName(map.get("zone_name").toString());
        if (map.get("zone_type") != null) event.setZoneType(map.get("zone_type").toString());
        if (map.get("vehicle_id") != null) event.setVehicleId(((Number) map.get("vehicle_id")).intValue());
        if (map.get("registration_number") != null) event.setRegistrationNumber(map.get("registration_number").toString());
        if (map.get("alert_type") != null) event.setAlertType(map.get("alert_type").toString());
        if (map.get("vehicle_location_lat") != null) event.setVehicleLocationLat(((Number) map.get("vehicle_location_lat")).doubleValue());
        if (map.get("vehicle_location_lng") != null) event.setVehicleLocationLng(((Number) map.get("vehicle_location_lng")).doubleValue());
        if (map.get("is_notified") != null) event.setNotified((Boolean) map.get("is_notified"));
        if (map.get("priority") != null) event.setPriority(map.get("priority").toString());

        if (map.get("alert_timestamp") instanceof java.sql.Timestamp) {
            event.setAlertTimestamp(((java.sql.Timestamp) map.get("alert_timestamp")).toLocalDateTime());
        }
        if (map.get("created_at") instanceof java.sql.Timestamp) {
            event.setCreatedAt(((java.sql.Timestamp) map.get("created_at")).toLocalDateTime());
        }
        if (map.get("updated_at") instanceof java.sql.Timestamp) {
            event.setUpdatedAt(((java.sql.Timestamp) map.get("updated_at")).toLocalDateTime());
        }

        return event;
    }

    private List<GeofenceAlertEvent> mapToGeofenceAlertEventList(List<Map<String, Object>> maps) {
        List<GeofenceAlertEvent> events = new ArrayList<>();
        if (maps != null) {
            for (Map<String, Object> map : maps) {
                events.add(mapToGeofenceAlertEvent(map));
            }
        }
        return events;
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
        event.setPriority(rs.getString("priority"));

        if (rs.getTimestamp("created_at") != null) {
            event.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            event.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        return event;
    }
}