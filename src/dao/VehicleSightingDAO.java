package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import database.ProcedureCaller;
import database.ViewLoader;
import models.VehicleSighting;

/**
 * VehicleSightingDAO - Uses ONLY stored procedures and views for all operations.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class VehicleSightingDAO extends BaseDAO<VehicleSighting> {

    private final ProcedureCaller procedureCaller;
    private final ViewLoader viewLoader;

    public VehicleSightingDAO() {
        this.procedureCaller = new ProcedureCaller();
        this.viewLoader = new ViewLoader();
    }

    @Override
    public VehicleSighting findById(int id) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_vehicle_sightings", "id = ?", id);
        if (results.isEmpty()) {
            return null;
        }
        return mapMapToVehicleSighting(results.get(0));
    }

    @Override
    public List<VehicleSighting> findAll() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadView("vw_vehicle_sightings");
        return mapMapsToVehicleSightings(results);
    }

    public List<VehicleSighting> findByVehicleId(int vehicleId) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_vehicle_sightings", "vehicle_id = ? ORDER BY timestamp DESC", vehicleId);
        return mapMapsToVehicleSightings(results);
    }

    public List<VehicleSighting> findByLicensePlate(String licensePlate) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_vehicle_sightings", "license_plate = ? ORDER BY timestamp DESC", licensePlate);
        return mapMapsToVehicleSightings(results);
    }

    public List<VehicleSighting> findBySourceType(String sourceType) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_vehicle_sightings", "source_type = ? ORDER BY timestamp DESC", sourceType);
        return mapMapsToVehicleSightings(results);
    }

    public List<VehicleSighting> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_vehicle_sightings", "timestamp BETWEEN ? AND ? ORDER BY timestamp", startDate, endDate);
        return mapMapsToVehicleSightings(results);
    }

    public List<VehicleSighting> findByVehicleAndDateRange(int vehicleId, LocalDateTime startDate, LocalDateTime endDate) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_vehicle_sightings", "vehicle_id = ? AND timestamp BETWEEN ? AND ? ORDER BY timestamp", vehicleId, startDate, endDate);
        return mapMapsToVehicleSightings(results);
    }

    public boolean insertTrafficCameraSighting(int vehicleId, String licensePlate, String cameraId,
                                               double latitude, double longitude, LocalDateTime timestamp) throws SQLException {
        return insertTrafficCameraSighting(vehicleId, licensePlate, cameraId, latitude, longitude, timestamp, 0.95);
    }

    public boolean insertTrafficCameraSighting(int vehicleId, String licensePlate, String cameraId,
                                               double latitude, double longitude, LocalDateTime timestamp, double confidenceScore) throws SQLException {
        Integer sightingId = procedureCaller.executeAddVehicleSighting(
                licensePlate,
                "traffic_camera",
                cameraId,
                latitude,
                longitude,
                timestamp,
                confidenceScore
        );
        return sightingId != null && sightingId > 0;
    }

    public boolean insertANPRSighting(int vehicleId, String licensePlate, String anprDeviceId,
                                      double latitude, double longitude, LocalDateTime timestamp, double confidenceScore) throws SQLException {
        Integer sightingId = procedureCaller.executeAddVehicleSighting(
                licensePlate,
                "anpr_system",
                anprDeviceId,
                latitude,
                longitude,
                timestamp,
                confidenceScore
        );
        return sightingId != null && sightingId > 0;
    }

    @Override
    public boolean insert(VehicleSighting entity) throws SQLException {
        Integer sightingId = procedureCaller.executeAddVehicleSighting(
                entity.getLicensePlate(),
                entity.getSourceType(),
                entity.getSourceDeviceId(),
                entity.getLatitude(),
                entity.getLongitude(),
                entity.getTimestamp(),
                entity.getConfidenceScore()
        );
        if (sightingId != null && sightingId > 0) {
            entity.setId(sightingId);
            return true;
        }
        return false;
    }

    @Override
    public boolean update(VehicleSighting entity) throws SQLException {
        // Sightings are immutable
        return false;
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return procedureCaller.executeDeleteVehicleSighting(id);
    }

    public int countSightingsByVehicle(int vehicleId) throws SQLException {
        return viewLoader.countViewRowsWithCondition("vw_vehicle_sightings", "vehicle_id = ?", vehicleId);
    }

    /**
     * Converts a List of Maps to a List of VehicleSighting objects.
     *
     * @param maps the list of maps from the view loader
     * @return list of VehicleSighting objects
     */
    private List<VehicleSighting> mapMapsToVehicleSightings(List<Map<String, Object>> maps) {
        List<VehicleSighting> sightings = new ArrayList<>();
        if (maps == null) {
            return sightings;
        }
        for (Map<String, Object> map : maps) {
            VehicleSighting sighting = mapMapToVehicleSighting(map);
            if (sighting != null) {
                sightings.add(sighting);
            }
        }
        return sightings;
    }

    /**
     * Converts a Map to a VehicleSighting object.
     *
     * @param map the map from the view loader
     * @return VehicleSighting object
     */
    private VehicleSighting mapMapToVehicleSighting(Map<String, Object> map) {
        if (map == null) {
            return null;
        }

        VehicleSighting sighting = new VehicleSighting();

        sighting.setId(getIntValue(map, "id"));
        sighting.setVehicleId(getIntValue(map, "vehicle_id"));
        sighting.setRegistrationNumber(getStringValue(map, "registration_number"));
        sighting.setLicensePlate(getStringValue(map, "license_plate"));
        sighting.setSourceType(getStringValue(map, "source_type"));
        sighting.setSourceDeviceId(getStringValue(map, "source_device_id"));
        sighting.setLatitude(getDoubleValue(map, "latitude"));
        sighting.setLongitude(getDoubleValue(map, "longitude"));
        sighting.setConfidenceScore(getDoubleValue(map, "confidence_score"));
        sighting.setImagePath(getStringValue(map, "image_path"));
        sighting.setAlertStatus(getStringValue(map, "alert_status"));

        sighting.setTimestamp(getLocalDateTimeValue(map, "timestamp"));
        sighting.setCreatedAt(getLocalDateTimeValue(map, "created_at"));
        sighting.setUpdatedAt(getLocalDateTimeValue(map, "updated_at"));

        return sighting;
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
     * Helper method to safely get Double values from Map.
     */
    private Double getDoubleValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return 0.0;
        }
        if (value instanceof Double) {
            return (Double) value;
        }
        if (value instanceof Integer) {
            return ((Integer) value).doubleValue();
        }
        if (value instanceof Long) {
            return ((Long) value).doubleValue();
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return 0.0;
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
    protected VehicleSighting mapRow(ResultSet rs) throws SQLException {
        VehicleSighting sighting = new VehicleSighting();
        sighting.setId(rs.getInt("id"));

        if (rs.getObject("vehicle_id") != null) {
            sighting.setVehicleId(rs.getInt("vehicle_id"));
        }
        sighting.setRegistrationNumber(rs.getString("registration_number"));
        sighting.setLicensePlate(rs.getString("license_plate"));
        sighting.setSourceType(rs.getString("source_type"));
        sighting.setSourceDeviceId(rs.getString("source_device_id"));
        sighting.setLatitude(rs.getDouble("latitude"));
        sighting.setLongitude(rs.getDouble("longitude"));

        if (rs.getTimestamp("timestamp") != null) {
            sighting.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
        }
        sighting.setConfidenceScore(rs.getDouble("confidence_score"));

        try {
            sighting.setImagePath(rs.getString("image_path"));
        } catch (SQLException e) {
            // Column might not exist
        }

        try {
            sighting.setAlertStatus(rs.getString("alert_status"));
        } catch (SQLException e) {
            // Column might not exist
        }

        if (rs.getTimestamp("created_at") != null) {
            sighting.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            sighting.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        return sighting;
    }
}