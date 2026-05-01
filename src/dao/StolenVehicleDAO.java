package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import models.StolenVehicle;

public class StolenVehicleDAO extends BaseDAO<StolenVehicle> {

    @Override
    public StolenVehicle findById(int id) throws SQLException {
        String sql = "SELECT * FROM vw_stolen_vehicles WHERE id = ?";
        return executeQuerySingle(sql, id);
    }

    public StolenVehicle findByCaseNumber(String caseNumber) throws SQLException {
        String sql = "SELECT * FROM vw_stolen_vehicles WHERE case_number = ?";
        return executeQuerySingle(sql, caseNumber);
    }

    public StolenVehicle findByVehicleId(int vehicleId) throws SQLException {
        String sql = "SELECT * FROM vw_stolen_vehicles WHERE vehicle_id = ? AND status = 'ACTIVE'";
        return executeQuerySingle(sql, vehicleId);
    }

    public StolenVehicle findActiveByVehicleId(int vehicleId) throws SQLException {
        String sql = "SELECT * FROM vw_stolen_vehicles WHERE vehicle_id = ? AND status = 'ACTIVE'";
        return executeQuerySingle(sql, vehicleId);
    }

    @Override
    public List<StolenVehicle> findAll() throws SQLException {
        String sql = "SELECT * FROM vw_stolen_vehicles ORDER BY reported_date DESC";
        return executeQuery(sql);
    }

    public List<StolenVehicle> findActiveStolenVehicles() throws SQLException {
        String sql = "SELECT * FROM vw_active_stolen_vehicles ORDER BY reported_date DESC";
        return executeQuery(sql);
    }

    public List<StolenVehicle> findRecoveredVehicles() throws SQLException {
        String sql = "SELECT * FROM vw_stolen_vehicles WHERE status = 'RECOVERED' ORDER BY recovered_date DESC";
        return executeQuery(sql);
    }

    public List<StolenVehicle> findByOfficer(String officerName) throws SQLException {
        String sql = "SELECT * FROM vw_stolen_vehicles WHERE assigned_officer ILIKE ? ORDER BY reported_date DESC";
        return executeQuery(sql, "%" + officerName + "%");
    }

    public List<StolenVehicle> findByRegistrationNumber(String registrationNumber) throws SQLException {
        String sql = "SELECT * FROM vw_stolen_vehicles WHERE registration_number ILIKE ? ORDER BY reported_date DESC";
        return executeQuery(sql, "%" + registrationNumber + "%");
    }

    public List<StolenVehicle> findStolenBetween(LocalDate startDate, LocalDate endDate) throws SQLException {
        String sql = "SELECT * FROM vw_stolen_vehicles WHERE reported_date BETWEEN ? AND ? ORDER BY reported_date DESC";
        return executeQuery(sql, startDate, endDate);
    }

    public List<StolenVehicle> findNearbyStolen(Double latitude, Double longitude, double radiusKm) throws SQLException {
        if (latitude == null || longitude == null) {
            return new ArrayList<>();
        }

        String sql = "SELECT sv.*, v.make, v.model, v.registration_number, v.current_location_lat, v.current_location_lng " +
                "FROM stolen_vehicles sv " +
                "JOIN vehicles v ON sv.vehicle_id = v.id " +
                "WHERE sv.status = 'ACTIVE' " +
                "AND v.current_location_lat IS NOT NULL " +
                "AND v.current_location_lng IS NOT NULL " +
                "AND calculate_distance(?, ?, v.current_location_lat, v.current_location_lng) <= ? " +
                "ORDER BY calculate_distance(?, ?, v.current_location_lat, v.current_location_lng)";

        List<StolenVehicle> results = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setDouble(1, latitude);
            ps.setDouble(2, longitude);
            ps.setDouble(3, radiusKm);
            ps.setDouble(4, latitude);
            ps.setDouble(5, longitude);
            rs = ps.executeQuery();

            while (rs.next()) {
                StolenVehicle vehicle = mapRow(rs);
                if (rs.getObject("current_location_lat") != null) {
                    vehicle.setLatitude(rs.getDouble("current_location_lat"));
                }
                if (rs.getObject("current_location_lng") != null) {
                    vehicle.setLongitude(rs.getDouble("current_location_lng"));
                }
                results.add(vehicle);
            }

            return results;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    public boolean insertStolenVehicle(int vehicleId, String caseNumber, String officerName, String badgeNumber) throws SQLException {
        return executeProcedure("sp_report_stolen_vehicle", vehicleId, caseNumber, officerName, badgeNumber);
    }

    @Override
    public boolean insert(StolenVehicle entity) throws SQLException {
        return executeProcedure("sp_report_stolen_vehicle",
                entity.getVehicleId(),
                entity.getCaseNumber(),
                entity.getAssignedOfficer(),
                ""
        );
    }

    public boolean updateStatus(int stolenVehicleId, String status) throws SQLException {
        return executeProcedure("sp_update_stolen_status", stolenVehicleId, status);
    }

    public boolean recoverVehicle(int stolenVehicleId, LocalDate recoveredDate) throws SQLException {
        String sql = "CALL sp_recover_vehicle(?, ?)";
        int result = executeUpdate(sql, stolenVehicleId, recoveredDate);
        return result >= 0;
    }

    @Override
    public boolean update(StolenVehicle entity) throws SQLException {
        String sql = "UPDATE stolen_vehicles SET status = ?, recovered_date = ? WHERE id = ?";
        int result = executeUpdate(sql, entity.getStatus(), entity.getRecoveredDate(), entity.getId());
        return result > 0;
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM stolen_vehicles WHERE id = ?";
        int result = executeUpdate(sql, id);
        return result > 0;
    }

    public int countActiveStolen() throws SQLException {
        String sql = "SELECT COUNT(*) FROM stolen_vehicles WHERE status = 'ACTIVE'";
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

        if (rs.getTimestamp("created_at") != null) {
            stolen.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            stolen.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        return stolen;
    }
}