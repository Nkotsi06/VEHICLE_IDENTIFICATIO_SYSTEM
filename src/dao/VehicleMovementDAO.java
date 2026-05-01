package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import models.VehicleMovementRecord;
import models.VehicleSighting;

public class VehicleMovementDAO extends BaseDAO<VehicleMovementRecord> {

    private VehicleSightingDAO sightingDAO = new VehicleSightingDAO();

    @Override
    public VehicleMovementRecord findById(int id) throws SQLException {
        return null;
    }

    @Override
    public List<VehicleMovementRecord> findAll() throws SQLException {
        return null;
    }

    @Override
    protected VehicleMovementRecord mapRow(ResultSet rs) throws SQLException {
        // This method is required by BaseDAO but not used for stored procedure calls
        // Return null or implement if needed for JDBC queries
        VehicleMovementRecord record = new VehicleMovementRecord();
        record.setVehicleId(rs.getInt("vehicle_id"));
        if (rs.getTimestamp("start_datetime") != null) {
            record.setStartDateTime(rs.getTimestamp("start_datetime").toLocalDateTime());
        }
        if (rs.getTimestamp("end_datetime") != null) {
            record.setEndDateTime(rs.getTimestamp("end_datetime").toLocalDateTime());
        }
        record.setNumberOfSightings(rs.getInt("number_of_sightings"));

        if (rs.getDouble("total_distance_km") > 0) {
            record.setTotalDistanceKm(rs.getDouble("total_distance_km"));
        }
        if (rs.getDouble("average_speed_kmph") > 0) {
            record.setAverageSpeedKmph(rs.getDouble("average_speed_kmph"));
        }

        return record;
    }

    public VehicleMovementRecord reconstructMovement(int vehicleId, LocalDate startDate, LocalDate endDate) throws SQLException {
        String sql = "CALL sp_reconstruct_vehicle_movement(?, ?, ?, ?, ?)";
        Connection conn = null;
        try {
            conn = getConnection();
            var cs = conn.prepareCall("{call sp_reconstruct_vehicle_movement(?, ?, ?, ?, ?)}");
            cs.setInt(1, vehicleId);
            cs.setDate(2, java.sql.Date.valueOf(startDate));
            cs.setDate(3, java.sql.Date.valueOf(endDate));
            cs.registerOutParameter(4, java.sql.Types.INTEGER);
            cs.registerOutParameter(5, java.sql.Types.OTHER);
            cs.execute();

            VehicleMovementRecord record = new VehicleMovementRecord();
            record.setVehicleId(vehicleId);
            record.setStartDateTime(startDate.atStartOfDay());
            record.setEndDateTime(endDate.atTime(23, 59, 59));
            record.setNumberOfSightings(cs.getInt(4));

            // Parse the JSON string from the database without using org.json
            if (cs.getObject(5) != null) {
                String jsonStr = cs.getString(5);
                List<VehicleSighting> sightings = parseSightingsFromJson(jsonStr);
                for (VehicleSighting sighting : sightings) {
                    record.addSighting(sighting);
                }
            }

            return record;
        } finally {
            closeResources(null, null, conn);
        }
    }

    public VehicleMovementRecord getReconstructionWithMap(int vehicleId, LocalDate startDate, LocalDate endDate) throws SQLException {
        String sql = "CALL sp_get_vehicle_reconstruction_with_map(?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        try {
            conn = getConnection();
            var cs = conn.prepareCall("{call sp_get_vehicle_reconstruction_with_map(?, ?, ?, ?, ?, ?)}");
            cs.setInt(1, vehicleId);
            cs.setDate(2, java.sql.Date.valueOf(startDate));
            cs.setDate(3, java.sql.Date.valueOf(endDate));
            cs.registerOutParameter(4, java.sql.Types.OTHER);
            cs.registerOutParameter(5, java.sql.Types.DECIMAL);
            cs.registerOutParameter(6, java.sql.Types.DECIMAL);
            cs.execute();

            VehicleMovementRecord record = new VehicleMovementRecord();
            record.setVehicleId(vehicleId);
            record.setStartDateTime(startDate.atStartOfDay());
            record.setEndDateTime(endDate.atTime(23, 59, 59));

            // Parse the JSON string from the database without using org.json
            if (cs.getObject(4) != null) {
                String jsonStr = cs.getString(4);
                List<VehicleSighting> sightings = parseSightingsFromJson(jsonStr);
                for (int i = 0; i < sightings.size(); i++) {
                    VehicleSighting sighting = sightings.get(i);
                    sighting.setSequenceNumber(i + 1);
                    record.addSighting(sighting);
                }
            }

            if (cs.getObject(5) != null) {
                record.setTotalDistanceKm(cs.getDouble(5));
            }
            if (cs.getObject(6) != null) {
                record.setAverageSpeedKmph(cs.getDouble(6));
            }

            return record;
        } finally {
            closeResources(null, null, conn);
        }
    }

    private List<VehicleSighting> parseSightingsFromJson(String jsonStr) {
        List<VehicleSighting> sightings = new ArrayList<>();

        if (jsonStr == null || jsonStr.isEmpty() || jsonStr.equals("[]")) {
            return sightings;
        }

        // Manual JSON parsing without external library
        try {
            // Remove outer brackets
            String trimmed = jsonStr.trim();
            if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                String content = trimmed.substring(1, trimmed.length() - 1);

                // Split by "},{"
                String[] objects = content.split("\\},\\{");

                for (String objStr : objects) {
                    // Add back curly braces
                    if (!objStr.startsWith("{")) objStr = "{" + objStr;
                    if (!objStr.endsWith("}")) objStr = objStr + "}";

                    VehicleSighting sighting = parseSightingObject(objStr);
                    if (sighting != null) {
                        sightings.add(sighting);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sightings;
    }

    private VehicleSighting parseSightingObject(String objStr) {
        try {
            VehicleSighting sighting = new VehicleSighting();

            // Extract sequence
            String sequenceValue = extractJsonValue(objStr, "sequence");
            if (sequenceValue != null) {
                sighting.setSequenceNumber(Integer.parseInt(sequenceValue));
            }

            // Extract timestamp
            String timestampValue = extractJsonValue(objStr, "timestamp");
            if (timestampValue != null) {
                sighting.setTimestamp(LocalDateTime.parse(timestampValue.replace(" ", "T")));
            }

            // Extract source
            String sourceValue = extractJsonValue(objStr, "source");
            if (sourceValue != null) {
                sighting.setSourceType(sourceValue);
            }

            // Extract latitude
            String latValue = extractJsonValue(objStr, "latitude");
            if (latValue != null) {
                sighting.setLatitude(Double.parseDouble(latValue));
            }

            // Extract longitude
            String lngValue = extractJsonValue(objStr, "longitude");
            if (lngValue != null) {
                sighting.setLongitude(Double.parseDouble(lngValue));
            }

            // Extract estimated_speed
            String speedValue = extractJsonValue(objStr, "estimated_speed");
            if (speedValue != null && !speedValue.equals("null")) {
                sighting.setEstimatedSpeed(Double.parseDouble(speedValue));
            }

            return sighting;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String extractJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\":";
        int keyIndex = json.indexOf(searchKey);
        if (keyIndex == -1) return null;

        int startIndex = keyIndex + searchKey.length();
        char firstChar = json.charAt(startIndex);

        if (firstChar == '"') {
            // String value
            int endIndex = json.indexOf("\"", startIndex + 1);
            return json.substring(startIndex + 1, endIndex);
        } else {
            // Number or boolean value
            int endIndex = startIndex;
            while (endIndex < json.length()) {
                char c = json.charAt(endIndex);
                if (c == ',' || c == '}' || c == ' ') {
                    break;
                }
                endIndex++;
            }
            return json.substring(startIndex, endIndex).trim();
        }
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
        VehicleSighting sighting = new VehicleSighting(vehicleId, licensePlate, "traffic_camera", latitude, longitude, timestamp);
        sighting.setSourceDeviceId(cameraId);
        sighting.setConfidenceScore(confidenceScore);
        return sightingDAO.insert(sighting);
    }

    public boolean addTollGateSighting(int vehicleId, String licensePlate, String tollBoothId,
                                       double latitude, double longitude, LocalDateTime timestamp,
                                       String direction, double amount) throws SQLException {
        VehicleSighting sighting = new VehicleSighting(vehicleId, licensePlate, "toll_gate", latitude, longitude, timestamp);
        sighting.setSourceDeviceId(tollBoothId);
        sighting.setDirection(direction);
        sighting.setAdditionalData(String.valueOf(amount));
        return sightingDAO.insert(sighting);
    }

    public boolean addParkingLog(int vehicleId, String licensePlate, String parkingLotId,
                                 double latitude, double longitude, LocalDateTime entryTime,
                                 LocalDateTime exitTime) throws SQLException {
        VehicleSighting sighting = new VehicleSighting(vehicleId, licensePlate, "parking_lot", latitude, longitude, entryTime);
        sighting.setSourceDeviceId(parkingLotId);
        if (exitTime != null) {
            sighting.setAdditionalData(exitTime.toString());
        }
        return sightingDAO.insert(sighting);
    }

    public boolean addGasStationSighting(int vehicleId, String licensePlate, String stationId,
                                         double latitude, double longitude, LocalDateTime timestamp,
                                         String fuelType) throws SQLException {
        VehicleSighting sighting = new VehicleSighting(vehicleId, licensePlate, "gas_station", latitude, longitude, timestamp);
        sighting.setSourceDeviceId(stationId);
        sighting.setAdditionalData(fuelType);
        return sightingDAO.insert(sighting);
    }

    public boolean addANPRSighting(int vehicleId, String licensePlate, String anprDeviceId,
                                   double latitude, double longitude, LocalDateTime timestamp,
                                   double confidenceScore) throws SQLException {
        VehicleSighting sighting = new VehicleSighting(vehicleId, licensePlate, "anpr_system", latitude, longitude, timestamp);
        sighting.setSourceDeviceId(anprDeviceId);
        sighting.setConfidenceScore(confidenceScore);
        return sightingDAO.insert(sighting);
    }

    public String generateMovementReport(int vehicleId, LocalDate startDate, LocalDate endDate) throws SQLException {
        VehicleMovementRecord record = reconstructMovement(vehicleId, startDate, endDate);

        StringBuilder report = new StringBuilder();
        report.append("{");
        report.append("\"vehicle_id\":").append(vehicleId).append(",");
        report.append("\"start_date\":\"").append(startDate.toString()).append("\",");
        report.append("\"end_date\":\"").append(endDate.toString()).append("\",");
        report.append("\"total_sightings\":").append(record.getNumberOfSightings()).append(",");
        report.append("\"total_distance_km\":").append(record.getTotalDistanceKm() != null ? record.getTotalDistanceKm() : 0).append(",");
        report.append("\"average_speed_kmph\":").append(record.getAverageSpeedKmph() != null ? record.getAverageSpeedKmph() : 0).append(",");
        report.append("\"suspicious_score\":").append(record.getSuspiciousScore() != null ? record.getSuspiciousScore() : 0).append(",");
        report.append("\"suspicious_level\":\"").append(record.getSuspiciousLevel()).append("\",");
        report.append("\"route_points\":[");

        List<VehicleSighting> sightings = record.getSightings();
        for (int i = 0; i < sightings.size(); i++) {
            VehicleSighting sighting = sightings.get(i);
            if (i > 0) report.append(",");
            report.append("{");
            report.append("\"sequence\":").append(sighting.getSequenceNumber()).append(",");
            report.append("\"timestamp\":\"").append(sighting.getTimestamp().toString()).append("\",");
            report.append("\"latitude\":").append(sighting.getLatitude()).append(",");
            report.append("\"longitude\":").append(sighting.getLongitude()).append(",");
            report.append("\"source\":\"").append(sighting.getSourceType()).append("\"");
            report.append("}");
        }

        report.append("]}");

        return report.toString();
    }

    public List<VehicleMovementSummary> getRecentVehicleMovements(int limit) throws SQLException {
        String sql = "SELECT v.id, v.registration_number, v.make, v.model, " +
                "MAX(vs.timestamp) as last_sighting, COUNT(vs.id) as sighting_count " +
                "FROM vehicles v " +
                "LEFT JOIN vw_vehicle_sightings vs ON v.id = vs.vehicle_id " +
                "GROUP BY v.id, v.registration_number, v.make, v.model " +
                "ORDER BY last_sighting DESC NULLS LAST LIMIT ?";

        List<VehicleMovementSummary> summaries = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, limit);
            rs = ps.executeQuery();

            while (rs.next()) {
                VehicleMovementSummary summary = new VehicleMovementSummary();
                summary.vehicleId = rs.getInt("id");
                summary.registrationNumber = rs.getString("registration_number");
                summary.make = rs.getString("make");
                summary.model = rs.getString("model");
                if (rs.getTimestamp("last_sighting") != null) {
                    summary.lastSighting = rs.getTimestamp("last_sighting").toLocalDateTime();
                }
                summary.sightingCount = rs.getInt("sighting_count");
                summaries.add(summary);
            }

            return summaries;
        } finally {
            closeResources(rs, ps, conn);
        }
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
}