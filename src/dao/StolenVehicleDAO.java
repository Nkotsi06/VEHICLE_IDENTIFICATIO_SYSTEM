package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import database.ProcedureCaller;
import database.ViewLoader;
import models.StolenVehicle;

/**
 * StolenVehicleDAO - Uses ONLY stored procedures and views for all operations.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class StolenVehicleDAO extends BaseDAO<StolenVehicle> {

    private final ProcedureCaller procedureCaller;
    private final ViewLoader viewLoader;

    public StolenVehicleDAO() {
        this.procedureCaller = new ProcedureCaller();
        this.viewLoader = new ViewLoader();
    }

    @Override
    public StolenVehicle findById(int id) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_stolen_vehicles", "id = ?", id);
        if (results.isEmpty()) {
            return null;
        }
        return mapMapToStolenVehicle(results.get(0));
    }

    public StolenVehicle findByCaseNumber(String caseNumber) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_stolen_vehicles", "case_number = ?", caseNumber);
        if (results.isEmpty()) {
            return null;
        }
        return mapMapToStolenVehicle(results.get(0));
    }

    public StolenVehicle findByVehicleId(int vehicleId) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_stolen_vehicles", "vehicle_id = ? AND status = 'ACTIVE'", vehicleId);
        if (results.isEmpty()) {
            return null;
        }
        return mapMapToStolenVehicle(results.get(0));
    }

    public StolenVehicle findActiveByVehicleId(int vehicleId) throws SQLException {
        return findByVehicleId(vehicleId);
    }

    @Override
    public List<StolenVehicle> findAll() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadView("vw_stolen_vehicles");
        return mapMapsToStolenVehicles(results);
    }

    public List<StolenVehicle> findActiveStolenVehicles() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadView("vw_active_stolen_vehicles");
        return mapMapsToStolenVehicles(results);
    }

    public List<StolenVehicle> findRecoveredVehicles() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_stolen_vehicles", "status = 'RECOVERED' ORDER BY recovered_date DESC");
        return mapMapsToStolenVehicles(results);
    }

    public List<StolenVehicle> findByOfficer(String officerName) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_stolen_vehicles", "assigned_officer ILIKE ? ORDER BY reported_date DESC", "%" + officerName + "%");
        return mapMapsToStolenVehicles(results);
    }

    public List<StolenVehicle> findByRegistrationNumber(String registrationNumber) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_stolen_vehicles", "registration_number ILIKE ? ORDER BY reported_date DESC", "%" + registrationNumber + "%");
        return mapMapsToStolenVehicles(results);
    }

    public List<StolenVehicle> findStolenBetween(LocalDate startDate, LocalDate endDate) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_stolen_vehicles", "reported_date BETWEEN ? AND ? ORDER BY reported_date DESC", startDate, endDate);
        return mapMapsToStolenVehicles(results);
    }

    public List<StolenVehicle> findNearbyStolen(Double latitude, Double longitude, double radiusKm) throws SQLException {
        if (latitude == null || longitude == null) {
            return new ArrayList<>();
        }
        return procedureCaller.executeFindNearbyStolenVehicles(latitude, longitude, radiusKm);
    }

    public boolean insertStolenVehicle(int vehicleId, String caseNumber, String officerName, String badgeNumber, double latitude, double longitude, String description) throws SQLException {
        Integer stolenId = procedureCaller.executeReportStolenVehicle(vehicleId, caseNumber, officerName, badgeNumber, latitude, longitude, description);
        return stolenId != null && stolenId > 0;
    }

    @Override
    public boolean insert(StolenVehicle entity) throws SQLException {
        Integer stolenId = procedureCaller.executeReportStolenVehicle(
                entity.getVehicleId(),
                entity.getCaseNumber(),
                entity.getAssignedOfficer(),
                "",
                entity.getLatitude(),
                entity.getLongitude(),
                entity.getDescription()
        );
        if (stolenId != null && stolenId > 0) {
            entity.setId(stolenId);
            return true;
        }
        return false;
    }

    public boolean updateStatus(int stolenVehicleId, String status) throws SQLException {
        return procedureCaller.executeUpdateStolenStatus(stolenVehicleId, status);
    }

    public boolean recoverVehicle(int stolenVehicleId, LocalDate recoveredDate) throws SQLException {
        return procedureCaller.executeRecoverStolenVehicle(stolenVehicleId, recoveredDate);
    }

    @Override
    public boolean update(StolenVehicle entity) throws SQLException {
        return updateStatus(entity.getId(), entity.getStatus());
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return procedureCaller.executeDeleteStolenVehicle(id);
    }

    public int countActiveStolen() throws SQLException {
        return viewLoader.countViewRowsWithCondition("vw_stolen_vehicles", "status = 'ACTIVE'");
    }

    /**
     * Converts a List of Maps to a List of StolenVehicle objects.
     *
     * @param maps the list of maps from the view loader
     * @return list of StolenVehicle objects
     */
    private List<StolenVehicle> mapMapsToStolenVehicles(List<Map<String, Object>> maps) {
        List<StolenVehicle> vehicles = new ArrayList<>();
        if (maps == null) {
            return vehicles;
        }
        for (Map<String, Object> map : maps) {
            StolenVehicle vehicle = mapMapToStolenVehicle(map);
            if (vehicle != null) {
                vehicles.add(vehicle);
            }
        }
        return vehicles;
    }

    /**
     * Converts a Map to a StolenVehicle object.
     *
     * @param map the map from the view loader
     * @return StolenVehicle object
     */
    private StolenVehicle mapMapToStolenVehicle(Map<String, Object> map) {
        if (map == null) {
            return null;
        }

        StolenVehicle vehicle = new StolenVehicle();

        vehicle.setId(getIntValue(map, "id"));
        vehicle.setVehicleId(getIntValue(map, "vehicle_id"));
        vehicle.setRegistrationNumber(getStringValue(map, "registration_number"));
        vehicle.setMake(getStringValue(map, "make"));
        vehicle.setModel(getStringValue(map, "model"));
        vehicle.setCaseNumber(getStringValue(map, "case_number"));
        vehicle.setStatus(getStringValue(map, "status"));
        vehicle.setAssignedOfficer(getStringValue(map, "assigned_officer"));
        vehicle.setDescription(getStringValue(map, "description"));

        vehicle.setLatitude(getDoubleValue(map, "latitude"));
        vehicle.setLongitude(getDoubleValue(map, "longitude"));
        vehicle.setReportedDate(getLocalDateValue(map, "reported_date"));
        vehicle.setRecoveredDate(getLocalDateValue(map, "recovered_date"));
        vehicle.setCreatedAt(getLocalDateTimeValue(map, "created_at"));
        vehicle.setUpdatedAt(getLocalDateTimeValue(map, "updated_at"));

        return vehicle;
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
     * Helper method to safely get LocalDate values from Map.
     */
    private LocalDate getLocalDateValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof java.sql.Date) {
            return ((java.sql.Date) value).toLocalDate();
        }
        if (value instanceof LocalDate) {
            return (LocalDate) value;
        }
        return null;
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
    protected StolenVehicle mapRow(ResultSet rs) throws SQLException {
        StolenVehicle stolen = new StolenVehicle();
        stolen.setId(rs.getInt("id"));
        stolen.setVehicleId(rs.getInt("vehicle_id"));
        stolen.setRegistrationNumber(rs.getString("registration_number"));
        stolen.setMake(rs.getString("make"));
        stolen.setModel(rs.getString("model"));

        if (rs.getDate("reported_date") != null) {
            stolen.setReportedDate(rs.getDate("reported_date").toLocalDate());
        }
        stolen.setCaseNumber(rs.getString("case_number"));
        stolen.setStatus(rs.getString("status"));
        stolen.setAssignedOfficer(rs.getString("assigned_officer"));
        if (rs.getDate("recovered_date") != null) {
            stolen.setRecoveredDate(rs.getDate("recovered_date").toLocalDate());
        }
        stolen.setDescription(rs.getString("description"));

        if (rs.getTimestamp("created_at") != null) {
            stolen.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            stolen.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        return stolen;
    }
}