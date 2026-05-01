package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import models.VehicleMovementRecord;
import models.VehicleSighting;

public class VehicleMovementRecordDAO extends BaseDAO<VehicleMovementRecord> {

    @Override
    public VehicleMovementRecord findById(int id) throws SQLException {
        return null;
    }

    @Override
    public List<VehicleMovementRecord> findAll() throws SQLException {
        return null;
    }

    public VehicleMovementRecord reconstructMovement(int vehicleId, LocalDate startDate, LocalDate endDate) throws SQLException {
        String sql = "CALL sp_reconstruct_vehicle_movement(?, ?, ?, ?, ?)";
        Connection conn = null;
        java.sql.CallableStatement cs = null;
        try {
            conn = getConnection();
            cs = conn.prepareCall("{call sp_reconstruct_vehicle_movement(?, ?, ?, ?, ?)}");
            cs.setInt(1, vehicleId);
            cs.setDate(2, java.sql.Date.valueOf(startDate));
            cs.setDate(3, java.sql.Date.valueOf(endDate));
            cs.registerOutParameter(4, Types.INTEGER);
            cs.registerOutParameter(5, Types.OTHER);
            cs.execute();

            VehicleMovementRecord record = new VehicleMovementRecord();
            record.setVehicleId(vehicleId);
            record.setStartDateTime(startDate.atStartOfDay());
            record.setEndDateTime(endDate.atTime(23, 59, 59));
            record.setNumberOfSightings(cs.getInt(4));

            return record;
        } finally {
            closeResources(null, cs, conn);
        }
    }

    public VehicleMovementRecord getReconstructionWithMap(int vehicleId, LocalDate startDate, LocalDate endDate) throws SQLException {
        String sql = "CALL sp_get_vehicle_reconstruction_with_map(?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        java.sql.CallableStatement cs = null;
        try {
            conn = getConnection();
            cs = conn.prepareCall("{call sp_get_vehicle_reconstruction_with_map(?, ?, ?, ?, ?, ?)}");
            cs.setInt(1, vehicleId);
            cs.setDate(2, java.sql.Date.valueOf(startDate));
            cs.setDate(3, java.sql.Date.valueOf(endDate));
            cs.registerOutParameter(4, Types.OTHER);
            cs.registerOutParameter(5, Types.DECIMAL);
            cs.registerOutParameter(6, Types.DECIMAL);
            cs.execute();

            VehicleMovementRecord record = new VehicleMovementRecord();
            record.setVehicleId(vehicleId);
            record.setStartDateTime(startDate.atStartOfDay());
            record.setEndDateTime(endDate.atTime(23, 59, 59));

            if (cs.getObject(5) != null) {
                record.setTotalDistanceKm(cs.getDouble(5));
            }
            if (cs.getObject(6) != null) {
                record.setAverageSpeedKmph(cs.getDouble(6));
            }

            return record;
        } finally {
            closeResources(null, cs, conn);
        }
    }

    @Override
    public boolean insert(VehicleMovementRecord entity) throws SQLException {
        return false;
    }

    @Override
    public boolean update(VehicleMovementRecord entity) throws SQLException {
        return false;
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return false;
    }

    // Required mapRow method implementation
    @Override
    protected VehicleMovementRecord mapRow(ResultSet rs) throws SQLException {
        VehicleMovementRecord record = new VehicleMovementRecord();
        record.setId(rs.getInt("id"));
        record.setVehicleId(rs.getInt("vehicle_id"));

        if (rs.getTimestamp("start_date") != null) {
            record.setStartDateTime(rs.getTimestamp("start_date").toLocalDateTime());
        }
        if (rs.getTimestamp("end_date") != null) {
            record.setEndDateTime(rs.getTimestamp("end_date").toLocalDateTime());
        }

        if (rs.getObject("total_distance_km") != null) {
            record.setTotalDistanceKm(rs.getDouble("total_distance_km"));
        }
        if (rs.getObject("average_speed_kmph") != null) {
            record.setAverageSpeedKmph(rs.getDouble("average_speed_kmph"));
        }
        if (rs.getObject("number_of_sightings") != null) {
            record.setNumberOfSightings(rs.getInt("number_of_sightings"));
        }
        if (rs.getObject("suspicious_score") != null) {
            record.setSuspiciousScore(rs.getDouble("suspicious_score"));
        }

        // Note: suspiciousLevel is calculated in the model based on suspiciousScore
        // No need to set it directly

        if (rs.getTimestamp("created_at") != null) {
            record.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            record.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }

        return record;
    }
}