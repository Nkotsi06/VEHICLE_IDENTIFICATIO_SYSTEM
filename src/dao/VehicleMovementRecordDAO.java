package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import database.ProcedureCaller;
import database.ViewLoader;
import models.VehicleMovementRecord;

/**
 * VehicleMovementRecordDAO - Uses ONLY stored procedures and views for all operations.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class VehicleMovementRecordDAO extends BaseDAO<VehicleMovementRecord> {

    private final ProcedureCaller procedureCaller;
    private final ViewLoader viewLoader;

    public VehicleMovementRecordDAO() {
        this.procedureCaller = new ProcedureCaller();
        this.viewLoader = new ViewLoader();
    }

    @Override
    public VehicleMovementRecord findById(int id) throws SQLException {
        return null;
    }

    @Override
    public List<VehicleMovementRecord> findAll() throws SQLException {
        return null;
    }

    public VehicleMovementRecord reconstructMovement(int vehicleId, LocalDate startDate, LocalDate endDate) throws SQLException {
        return procedureCaller.executeReconstructVehicleMovement(vehicleId, startDate, endDate);
    }

    public VehicleMovementRecord getReconstructionWithMap(int vehicleId, LocalDate startDate, LocalDate endDate) throws SQLException {
        return procedureCaller.executeGetVehicleReconstructionWithMap(vehicleId, startDate, endDate);
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

        if (rs.getTimestamp("created_at") != null) {
            record.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            record.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }

        return record;
    }
}