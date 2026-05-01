package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import models.VehicleSighting;

public class VehicleSightingDAO extends BaseDAO<VehicleSighting> {

    @Override
    public VehicleSighting findById(int id) throws SQLException {
        String sql = "SELECT * FROM vw_vehicle_sightings WHERE id = ?";
        return executeQuerySingle(sql, id);
    }

    @Override
    public List<VehicleSighting> findAll() throws SQLException {
        String sql = "SELECT * FROM vw_vehicle_sightings ORDER BY timestamp DESC LIMIT 1000";
        return executeQuery(sql);
    }

    public List<VehicleSighting> findByVehicleId(int vehicleId) throws SQLException {
        String sql = "SELECT * FROM vw_vehicle_sightings WHERE vehicle_id = ? ORDER BY timestamp";
        return executeQuery(sql, vehicleId);
    }

    public List<VehicleSighting> findByLicensePlate(String licensePlate) throws SQLException {
        String sql = "SELECT * FROM vw_vehicle_sightings WHERE license_plate = ? ORDER BY timestamp DESC";
        return executeQuery(sql, licensePlate);
    }

    public List<VehicleSighting> findBySourceType(String sourceType) throws SQLException {
        String sql = "SELECT * FROM vw_vehicle_sightings WHERE source_type = ? ORDER BY timestamp DESC";
        return executeQuery(sql, sourceType);
    }

    public List<VehicleSighting> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) throws SQLException {
        String sql = "SELECT * FROM vw_vehicle_sightings WHERE timestamp BETWEEN ? AND ? ORDER BY timestamp";
        return executeQuery(sql, startDate, endDate);
    }

    public List<VehicleSighting> findByVehicleAndDateRange(int vehicleId, LocalDateTime startDate, LocalDateTime endDate) throws SQLException {
        String sql = "SELECT * FROM vw_vehicle_sightings WHERE vehicle_id = ? AND timestamp BETWEEN ? AND ? ORDER BY timestamp";
        return executeQuery(sql, vehicleId, startDate, endDate);
    }

    public boolean insertTrafficCameraSighting(int vehicleId, String licensePlate, String cameraId,
                                               double latitude, double longitude, LocalDateTime timestamp) throws SQLException {
        return insertTrafficCameraSighting(vehicleId, licensePlate, cameraId, latitude, longitude, timestamp, 0.95);
    }

    public boolean insertTrafficCameraSighting(int vehicleId, String licensePlate, String cameraId,
                                               double latitude, double longitude, LocalDateTime timestamp, double confidenceScore) throws SQLException {
        String sql = "INSERT INTO vehicle_sightings (vehicle_id, license_plate, source_type, source_device_id, latitude, longitude, timestamp, confidence_score) " +
                "VALUES (?, ?, 'traffic_camera', ?, ?, ?, ?, ?)";
        int result = executeUpdate(sql,
                vehicleId > 0 ? vehicleId : null,
                licensePlate,
                cameraId,
                latitude,
                longitude,
                timestamp,
                confidenceScore
        );
        return result > 0;
    }

    public boolean insertANPRSighting(int vehicleId, String licensePlate, String anprDeviceId,
                                      double latitude, double longitude, LocalDateTime timestamp, double confidenceScore) throws SQLException {
        String sql = "INSERT INTO vehicle_sightings (vehicle_id, license_plate, source_type, source_device_id, latitude, longitude, timestamp, confidence_score) " +
                "VALUES (?, ?, 'anpr_system', ?, ?, ?, ?, ?)";
        int result = executeUpdate(sql,
                vehicleId > 0 ? vehicleId : null,
                licensePlate,
                anprDeviceId,
                latitude,
                longitude,
                timestamp,
                confidenceScore
        );
        return result > 0;
    }

    @Override
    public boolean insert(VehicleSighting entity) throws SQLException {
        String sql = "INSERT INTO vehicle_sightings (vehicle_id, license_plate, source_type, source_device_id, latitude, longitude, timestamp, confidence_score) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        int result = executeUpdate(sql,
                entity.getVehicleId(),
                entity.getLicensePlate(),
                entity.getSourceType(),
                entity.getSourceDeviceId(),
                entity.getLatitude(),
                entity.getLongitude(),
                entity.getTimestamp(),
                entity.getConfidenceScore()
        );
        return result > 0;
    }

    @Override
    public boolean update(VehicleSighting entity) throws SQLException {
        return false;
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM vehicle_sightings WHERE id = ?";
        int result = executeUpdate(sql, id);
        return result > 0;
    }

    public int countSightingsByVehicle(int vehicleId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM vehicle_sightings WHERE vehicle_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, vehicleId);
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