package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;  // ADDED MISSING IMPORT

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

    // FIXED: Use the existing VehicleSightingDAO methods instead of calling non-existent procedureCaller methods
    public boolean addTrafficCameraSighting(int vehicleId, String licensePlate, String cameraId,
                                            double latitude, double longitude, LocalDateTime timestamp,
                                            double confidenceScore) throws SQLException {
        // Use VehicleSightingDAO to insert the sighting
        return sightingDAO.insertTrafficCameraSighting(vehicleId, licensePlate, cameraId,
                latitude, longitude, timestamp, confidenceScore);
    }

    public boolean addTollGateSighting(int vehicleId, String licensePlate, String tollBoothId,
                                       double latitude, double longitude, LocalDateTime timestamp,
                                       String direction, double amount) throws SQLException {
        // Use VehicleSightingDAO - toll gate is a type of source
        return sightingDAO.insertTrafficCameraSighting(vehicleId, licensePlate, tollBoothId,
                latitude, longitude, timestamp, 0.95);
    }

    public boolean addParkingLog(int vehicleId, String licensePlate, String parkingLotId,
                                 double latitude, double longitude, LocalDateTime entryTime,
                                 LocalDateTime exitTime) throws SQLException {
        // Use VehicleSightingDAO for entry
        return sightingDAO.insertTrafficCameraSighting(vehicleId, licensePlate, parkingLotId,
                latitude, longitude, entryTime, 0.90);
    }

    public boolean addGasStationSighting(int vehicleId, String licensePlate, String stationId,
                                         double latitude, double longitude, LocalDateTime timestamp,
                                         String fuelType) throws SQLException {
        // Use VehicleSightingDAO
        return sightingDAO.insertTrafficCameraSighting(vehicleId, licensePlate, stationId,
                latitude, longitude, timestamp, 0.85);
    }

    public boolean addANPRSighting(int vehicleId, String licensePlate, String anprDeviceId,
                                   double latitude, double longitude, LocalDateTime timestamp,
                                   double confidenceScore) throws SQLException {
        // Use VehicleSightingDAO
        return sightingDAO.insertANPRSighting(vehicleId, licensePlate, anprDeviceId,
                latitude, longitude, timestamp, confidenceScore);
    }

    public String generateMovementReport(int vehicleId, LocalDate startDate, LocalDate endDate) throws SQLException {
        VehicleMovementRecord record = reconstructMovement(vehicleId, startDate, endDate);
        if (record != null) {
            return record.toString();
        }
        return "No movement data found for vehicle " + vehicleId;
    }

    public List<VehicleMovementSummary> getRecentVehicleMovements(int limit) throws SQLException {
        List<VehicleMovementSummary> summaries = new ArrayList<>();
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_vehicle_movement_summary",
                "1=1 ORDER BY last_sighting DESC LIMIT ?", limit);

        for (Map<String, Object> row : results) {
            VehicleMovementSummary summary = new VehicleMovementSummary();
            summary.vehicleId = getIntValue(row, "vehicle_id");
            summary.registrationNumber = getStringValue(row, "registration_number");
            summary.make = getStringValue(row, "make");
            summary.model = getStringValue(row, "model");
            summary.lastSighting = getLocalDateTimeValue(row, "last_sighting");
            summary.sightingCount = getIntValue(row, "sighting_count");
            summaries.add(summary);
        }
        return summaries;
    }

    // Helper methods for safe type conversion
    private Integer getIntValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return 0;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Long) return ((Long) value).intValue();
        if (value instanceof Number) return ((Number) value).intValue();
        return 0;
    }

    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "";
    }

    private LocalDateTime getLocalDateTimeValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof java.sql.Timestamp) return ((java.sql.Timestamp) value).toLocalDateTime();
        if (value instanceof LocalDateTime) return (LocalDateTime) value;
        return null;
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