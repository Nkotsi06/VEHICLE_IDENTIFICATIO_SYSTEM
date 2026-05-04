package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import database.ProcedureCaller;
import database.ViewLoader;
import models.Vehicle;

/**
 * VehicleDAO - Uses ONLY stored procedures and views for all operations.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class VehicleDAO extends BaseDAO<Vehicle> {

    private final ProcedureCaller procedureCaller;
    private final ViewLoader viewLoader;

    public VehicleDAO() {
        this.procedureCaller = new ProcedureCaller();
        this.viewLoader = new ViewLoader();
    }

    @Override
    public Vehicle findById(int id) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_vehicles", "id = ?", id);
        return results.isEmpty() ? null : mapToVehicle(results.get(0));
    }

    public Vehicle findByRegistrationNumber(String registrationNumber) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_vehicles", "registration_number = ?", registrationNumber);
        return results.isEmpty() ? null : mapToVehicle(results.get(0));
    }

    public Vehicle findByChassisNumber(String chassisNumber) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_vehicles", "chassis_number = ?", chassisNumber);
        return results.isEmpty() ? null : mapToVehicle(results.get(0));
    }

    public Vehicle findByEngineNumber(String engineNumber) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_vehicles", "engine_number = ?", engineNumber);
        return results.isEmpty() ? null : mapToVehicle(results.get(0));
    }

    @Override
    public List<Vehicle> findAll() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadView("vw_vehicles");
        return mapToVehicleList(results);
    }

    public List<Vehicle> findByOwnerId(int ownerId) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_vehicles", "owner_id = ? ORDER BY registration_number", ownerId);
        return mapToVehicleList(results);
    }

    public List<Vehicle> findByStatusId(int statusId) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_vehicles", "status_id = ? ORDER BY registration_number", statusId);
        return mapToVehicleList(results);
    }

    public List<Vehicle> findByStatusName(String statusName) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_vehicles", "status_name = ? ORDER BY registration_number", statusName);
        return mapToVehicleList(results);
    }

    public List<Vehicle> findByMake(String make) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_vehicles", "make ILIKE ? ORDER BY registration_number", "%" + make + "%");
        return mapToVehicleList(results);
    }

    public List<Vehicle> findByYearRange(int startYear, int endYear) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_vehicles", "year BETWEEN ? AND ? ORDER BY year", startYear, endYear);
        return mapToVehicleList(results);
    }

    public List<Vehicle> searchVehicles(String keyword) throws SQLException {
        String pattern = "%" + keyword + "%";
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_vehicles",
                "registration_number ILIKE ? OR make ILIKE ? OR model ILIKE ? ORDER BY registration_number",
                pattern, pattern, pattern);
        return mapToVehicleList(results);
    }

    public int countByOwnerId(int ownerId) throws SQLException {
        return viewLoader.countViewRowsWithCondition("vw_vehicles", "owner_id = ?", ownerId);
    }

    @Override
    public boolean insert(Vehicle entity) throws SQLException {
        Integer vehicleId = procedureCaller.executeRegisterVehicle(
                entity.getRegistrationNumber(),
                entity.getMake(),
                entity.getModel(),
                entity.getYear(),
                entity.getOwnerId(),
                entity.getStatusId(),
                entity.getColor(),
                entity.getEngineNumber(),
                entity.getChassisNumber()
        );
        if (vehicleId != null && vehicleId > 0) {
            entity.setId(vehicleId);
            return true;
        }
        return false;
    }

    public int insertAndGetId(Vehicle entity) throws SQLException {
        return procedureCaller.executeRegisterVehicle(
                entity.getRegistrationNumber(),
                entity.getMake(),
                entity.getModel(),
                entity.getYear(),
                entity.getOwnerId(),
                entity.getStatusId(),
                entity.getColor(),
                entity.getEngineNumber(),
                entity.getChassisNumber()
        );
    }

    @Override
    public boolean update(Vehicle entity) throws SQLException {
        return procedureCaller.executeUpdateVehicle(
                entity.getId(),
                entity.getRegistrationNumber(),
                entity.getMake(),
                entity.getModel(),
                entity.getYear(),
                entity.getOwnerId(),
                entity.getStatusId(),
                entity.getColor(),
                entity.getEngineNumber(),
                entity.getChassisNumber()
        );
    }

    public boolean updateStatus(int vehicleId, int statusId) throws SQLException {
        return procedureCaller.executeUpdateVehicleStatus(vehicleId, statusId);
    }

    public boolean updateLocation(int vehicleId, double latitude, double longitude) throws SQLException {
        return procedureCaller.executeUpdateVehicleLocation(vehicleId, latitude, longitude);
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return procedureCaller.executeDeleteVehicle(id);
    }

    public int countVehicles() throws SQLException {
        return viewLoader.countViewRows("vw_vehicles");
    }

    // ============================================
    // HELPER METHODS FOR MAPPING
    // ============================================

    private Vehicle mapToVehicle(Map<String, Object> map) {
        if (map == null) return null;

        Vehicle vehicle = new Vehicle();
        if (map.get("id") != null) vehicle.setId(((Number) map.get("id")).intValue());
        if (map.get("registration_number") != null) vehicle.setRegistrationNumber(map.get("registration_number").toString());
        if (map.get("make") != null) vehicle.setMake(map.get("make").toString());
        if (map.get("model") != null) vehicle.setModel(map.get("model").toString());
        if (map.get("year") != null) vehicle.setYear(((Number) map.get("year")).intValue());
        if (map.get("owner_id") != null) vehicle.setOwnerId(((Number) map.get("owner_id")).intValue());
        if (map.get("owner_name") != null) vehicle.setOwnerName(map.get("owner_name").toString());
        if (map.get("status_id") != null) vehicle.setStatusId(((Number) map.get("status_id")).intValue());
        if (map.get("status_name") != null) vehicle.setStatusName(map.get("status_name").toString());
        if (map.get("color_code") != null) vehicle.setStatusColorCode(map.get("color_code").toString());
        if (map.get("color") != null) vehicle.setColor(map.get("color").toString());
        if (map.get("engine_number") != null) vehicle.setEngineNumber(map.get("engine_number").toString());
        if (map.get("chassis_number") != null) vehicle.setChassisNumber(map.get("chassis_number").toString());

        if (map.get("current_location_lat") != null) {
            vehicle.setCurrentLocationLat(((Number) map.get("current_location_lat")).doubleValue());
        }
        if (map.get("current_location_lng") != null) {
            vehicle.setCurrentLocationLng(((Number) map.get("current_location_lng")).doubleValue());
        }
        if (map.get("last_updated_location") instanceof java.sql.Timestamp) {
            vehicle.setLastUpdatedLocation(((java.sql.Timestamp) map.get("last_updated_location")).toLocalDateTime());
        }
        if (map.get("created_at") instanceof java.sql.Timestamp) {
            vehicle.setCreatedAt(((java.sql.Timestamp) map.get("created_at")).toLocalDateTime());
        }
        if (map.get("updated_at") instanceof java.sql.Timestamp) {
            vehicle.setUpdatedAt(((java.sql.Timestamp) map.get("updated_at")).toLocalDateTime());
        }
        return vehicle;
    }

    private List<Vehicle> mapToVehicleList(List<Map<String, Object>> maps) {
        List<Vehicle> vehicles = new ArrayList<>();
        if (maps != null) {
            for (Map<String, Object> map : maps) {
                vehicles.add(mapToVehicle(map));
            }
        }
        return vehicles;
    }

    @Override
    protected Vehicle mapRow(ResultSet rs) throws SQLException {
        Vehicle vehicle = new Vehicle();
        vehicle.setId(rs.getInt("id"));
        vehicle.setRegistrationNumber(rs.getString("registration_number"));
        vehicle.setMake(rs.getString("make"));
        vehicle.setModel(rs.getString("model"));
        vehicle.setYear(rs.getInt("year"));
        vehicle.setOwnerId(rs.getInt("owner_id"));
        vehicle.setOwnerName(rs.getString("owner_name"));
        vehicle.setStatusId(rs.getInt("status_id"));
        vehicle.setStatusName(rs.getString("status_name"));
        vehicle.setStatusColorCode(rs.getString("color_code"));
        vehicle.setColor(rs.getString("color"));
        vehicle.setEngineNumber(rs.getString("engine_number"));
        vehicle.setChassisNumber(rs.getString("chassis_number"));

        if (rs.getObject("current_location_lat") != null) {
            vehicle.setCurrentLocationLat(rs.getDouble("current_location_lat"));
        }
        if (rs.getObject("current_location_lng") != null) {
            vehicle.setCurrentLocationLng(rs.getDouble("current_location_lng"));
        }
        if (rs.getTimestamp("last_updated_location") != null) {
            vehicle.setLastUpdatedLocation(rs.getTimestamp("last_updated_location").toLocalDateTime());
        }
        if (rs.getTimestamp("created_at") != null) {
            vehicle.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            vehicle.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        return vehicle;
    }
}