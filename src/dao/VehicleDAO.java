package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import models.Vehicle;

public class VehicleDAO extends BaseDAO<Vehicle> {

    @Override
    public Vehicle findById(int id) throws SQLException {
        String sql = "SELECT * FROM vw_vehicles WHERE id = ?";
        return executeQuerySingle(sql, id);
    }

    public Vehicle findByRegistrationNumber(String registrationNumber) throws SQLException {
        String sql = "SELECT * FROM vw_vehicles WHERE registration_number = ?";
        return executeQuerySingle(sql, registrationNumber);
    }

    public Vehicle findByChassisNumber(String chassisNumber) throws SQLException {
        String sql = "SELECT * FROM vw_vehicles WHERE chassis_number = ?";
        return executeQuerySingle(sql, chassisNumber);
    }

    public Vehicle findByEngineNumber(String engineNumber) throws SQLException {
        String sql = "SELECT * FROM vw_vehicles WHERE engine_number = ?";
        return executeQuerySingle(sql, engineNumber);
    }

    @Override
    public List<Vehicle> findAll() throws SQLException {
        String sql = "SELECT * FROM vw_vehicles ORDER BY registration_number";
        return executeQuery(sql);
    }

    public List<Vehicle> findByOwnerId(int ownerId) throws SQLException {
        String sql = "SELECT * FROM vw_vehicles WHERE owner_id = ? ORDER BY registration_number";
        return executeQuery(sql, ownerId);
    }

    public List<Vehicle> findByStatusId(int statusId) throws SQLException {
        String sql = "SELECT * FROM vw_vehicles WHERE status_id = ? ORDER BY registration_number";
        return executeQuery(sql, statusId);
    }

    public List<Vehicle> findByStatusName(String statusName) throws SQLException {
        String sql = "SELECT * FROM vw_vehicles WHERE status_name = ? ORDER BY registration_number";
        return executeQuery(sql, statusName);
    }

    public List<Vehicle> findByMake(String make) throws SQLException {
        String sql = "SELECT * FROM vw_vehicles WHERE make ILIKE ? ORDER BY registration_number";
        return executeQuery(sql, "%" + make + "%");
    }

    public List<Vehicle> findByYearRange(int startYear, int endYear) throws SQLException {
        String sql = "SELECT * FROM vw_vehicles WHERE year BETWEEN ? AND ? ORDER BY year";
        return executeQuery(sql, startYear, endYear);
    }

    public List<Vehicle> searchVehicles(String keyword) throws SQLException {
        String sql = "SELECT * FROM vw_vehicles WHERE registration_number ILIKE ? OR make ILIKE ? OR model ILIKE ? ORDER BY registration_number";
        String searchPattern = "%" + keyword + "%";
        return executeQuery(sql, searchPattern, searchPattern, searchPattern);
    }

    public int countByOwnerId(int ownerId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM vehicles WHERE owner_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, ownerId);
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
    public boolean insert(Vehicle entity) throws SQLException {
        Integer vehicleId = executeProcedureWithInOutParameter("sp_register_vehicle",
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
        Integer vehicleId = executeProcedureWithInOutParameter("sp_register_vehicle",
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
            return vehicleId;
        }
        return -1;
    }

    @Override
    public boolean update(Vehicle entity) throws SQLException {
        return executeProcedure("sp_update_vehicle",
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
        return executeProcedure("sp_update_vehicle_status", vehicleId, statusId);
    }

    public boolean updateLocation(int vehicleId, double latitude, double longitude) throws SQLException {
        String sql = "UPDATE vehicles SET current_location_lat = ?, current_location_lng = ?, last_updated_location = ? WHERE id = ?";
        int result = executeUpdate(sql, latitude, longitude, LocalDateTime.now(), vehicleId);
        return result > 0;
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return executeProcedure("sp_delete_vehicle", id);
    }

    public int countVehicles() throws SQLException {
        String sql = "SELECT COUNT(*) FROM vehicles";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
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