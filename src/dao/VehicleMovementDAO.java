package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import database.ProcedureCaller;
import database.ViewLoader;
import models.VehicleMovementRecord;
import models.VehicleSighting;

/**
 * VehicleMovementDAO - Uses ONLY stored procedures and views for all operations.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class VehicleMovementDAO extends BaseDAO<VehicleMovementRecord> {

    private final ProcedureCaller procedureCaller;
    private final ViewLoader viewLoader;
    private final VehicleSightingDAO sightingDAO;

    public VehicleMovementDAO() {
        this.procedureCaller = new ProcedureCaller();
        this.viewLoader = new ViewLoader();
        this.sightingDAO = new VehicleSightingDAO();
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

    public List<VehicleSighting> getSightingsByVehicle(int vehicleId) throws SQLException {
        return sightingDAO.findByVehicleId(vehicleId);
    }

    public List<VehicleSighting> getSightingsByVehicleAndDateRange(int vehicleId, LocalDateTime startDate, LocalDateTime endDate) throws SQLException {
        return sightingDAO.findByVehicleAndDateRange(vehicleId, startDate, endDate);
    }

    public List<VehicleSighting> getSightingsByLicensePlate(String licensePlate) throws SQLException {
        return sightingDAO.findByLicensePlate(licensePlate);
    }

    public boolean addTrafficCameraSighting(int vehicleId, String licensePlate, String cameraId,
                                            double latitude, double longitude, LocalDateTime timestamp,
                                            double confidenceScore) throws SQLException {
        return procedureCaller.executeAddTrafficCameraSighting(vehicleId, licensePlate, cameraId, latitude, longitude, timestamp, confidenceScore);
    }

    public boolean addTollGateSighting(int vehicleId, String licensePlate, String tollBoothId,
                                       double latitude, double longitude, LocalDateTime timestamp,
                                       String direction, double amount) throws SQLException {
        return procedureCaller.executeAddTollGateSighting(vehicleId, licensePlate, tollBoothId, latitude, longitude, timestamp, direction, amount);
    }

    public boolean addParkingLog(int vehicleId, String licensePlate, String parkingLotId,
                                 double latitude, double longitude, LocalDateTime entryTime,
                                 LocalDateTime exitTime) throws SQLException {
        return procedureCaller.executeAddParkingLog(vehicleId, licensePlate, parkingLotId, latitude, longitude, entryTime, exitTime);
    }

    public boolean addGasStationSighting(int vehicleId, String licensePlate, String stationId,
                                         double latitude, double longitude, LocalDateTime timestamp,
                                         String fuelType) throws SQLException {
        return procedureCaller.executeAddGasStationSighting(vehicleId, licensePlate, stationId, latitude, longitude, timestamp, fuelType);
    }

    public boolean addANPRSighting(int vehicleId, String licensePlate, String anprDeviceId,
                                   double latitude, double longitude, LocalDateTime timestamp,
                                   double confidenceScore) throws SQLException {
        return procedureCaller.executeAddANPRSighting(vehicleId, licensePlate, anprDeviceId, latitude, longitude, timestamp, confidenceScore);
    }

    public String generateMovementReport(int vehicleId, LocalDate startDate, LocalDate endDate) throws SQLException {
        return procedureCaller.executeGenerateMovementReport(vehicleId, startDate, endDate);
    }

    public List<VehicleMovementSummary> getRecentVehicleMovements(int limit) throws SQLException {
        List<VehicleMovementSummary> summaries = new ArrayList<>();
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_vehicle_movement_summary", "1=1 ORDER BY last_sighting DESC LIMIT ?", limit);

        for (Map<String, Object> row : results) {
            VehicleMovementSummary summary = new VehicleMovementSummary();
            summary.vehicleId = (Integer) row.get("vehicle_id");
            summary.registrationNumber = (String) row.get("registration_number");
            summary.make = (String) row.get("make");
            summary.model = (String) row.get("model");
            if (row.get("last_sighting") != null) {
                summary.lastSighting = ((java.sql.Timestamp) row.get("last_sighting")).toLocalDateTime();
            }
            summary.sightingCount = ((Number) row.get("sighting_count")).intValue();
            summaries.add(summary);
        }
        return summaries;
    }

    public static class VehicleMovementSummary {
        public int vehicleId;
        public String registrationNumber;
        public String make;
        public String model;
        public LocalDateTime lastSighting;
        public int sightingCount;
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