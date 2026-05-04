package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

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
        List<VehicleSighting> results = viewLoader.loadViewWithCondition("vw_vehicle_sightings", "id = ?", id);
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public List<VehicleSighting> findAll() throws SQLException {
        return viewLoader.loadView("vw_vehicle_sightings");
    }

    public List<VehicleSighting> findByVehicleId(int vehicleId) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_vehicle_sightings", "vehicle_id = ? ORDER BY timestamp DESC", vehicleId);
    }

    public List<VehicleSighting> findByLicensePlate(String licensePlate) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_vehicle_sightings", "license_plate = ? ORDER BY timestamp DESC", licensePlate);
    }

    public List<VehicleSighting> findBySourceType(String sourceType) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_vehicle_sightings", "source_type = ? ORDER BY timestamp DESC", sourceType);
    }

    public List<VehicleSighting> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_vehicle_sightings", "timestamp BETWEEN ? AND ? ORDER BY timestamp", startDate, endDate);
    }

    public List<VehicleSighting> findByVehicleAndDateRange(int vehicleId, LocalDateTime startDate, LocalDateTime endDate) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_vehicle_sightings", "vehicle_id = ? AND timestamp BETWEEN ? AND ? ORDER BY timestamp", vehicleId, startDate, endDate);
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

        if (rs.getTimestamp("created_at") != null) {
            sighting.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            sighting.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        return sighting;
    }
}