package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import models.OfficerLog;

public class OfficerLogDAO extends BaseDAO<OfficerLog> {

    @Override
    public OfficerLog findById(int id) throws SQLException {
        String sql = "SELECT * FROM vw_officer_logs WHERE id = ?";
        return executeQuerySingle(sql, id);
    }

    @Override
    public List<OfficerLog> findAll() throws SQLException {
        String sql = "SELECT * FROM vw_officer_logs ORDER BY timestamp DESC";
        return executeQuery(sql);
    }

    public List<OfficerLog> findByOfficerName(String officerName) throws SQLException {
        String sql = "SELECT * FROM vw_officer_logs WHERE officer_name ILIKE ? ORDER BY timestamp DESC";
        return executeQuery(sql, "%" + officerName + "%");
    }

    public List<OfficerLog> findByBadgeNumber(String badgeNumber) throws SQLException {
        String sql = "SELECT * FROM vw_officer_logs WHERE badge_number = ? ORDER BY timestamp DESC";
        return executeQuery(sql, badgeNumber);
    }

    public List<OfficerLog> findByVehicleId(int vehicleId) throws SQLException {
        String sql = "SELECT * FROM vw_officer_logs WHERE vehicle_id = ? ORDER BY timestamp DESC";
        return executeQuery(sql, vehicleId);
    }

    public List<OfficerLog> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) throws SQLException {
        String sql = "SELECT * FROM vw_officer_logs WHERE timestamp BETWEEN ? AND ? ORDER BY timestamp DESC";
        return executeQuery(sql, startDate, endDate);
    }

    public List<OfficerLog> findByAction(String action) throws SQLException {
        String sql = "SELECT * FROM vw_officer_logs WHERE action ILIKE ? ORDER BY timestamp DESC";
        return executeQuery(sql, "%" + action + "%");
    }

    @Override
    public boolean insert(OfficerLog entity) throws SQLException {
        return executeProcedure("sp_log_officer_action",
                entity.getOfficerName(),
                entity.getBadgeNumber(),
                entity.getAction(),
                entity.getVehicleId() > 0 ? entity.getVehicleId() : null
        );
    }

    @Override
    public boolean update(OfficerLog entity) throws SQLException {
        String sql = "UPDATE officer_logs SET action = ? WHERE id = ?";
        int result = executeUpdate(sql, entity.getAction(), entity.getId());
        return result > 0;
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM officer_logs WHERE id = ?";
        int result = executeUpdate(sql, id);
        return result > 0;
    }

    public boolean deleteOldLogs(LocalDateTime beforeDate) throws SQLException {
        String sql = "DELETE FROM officer_logs WHERE timestamp < ?";
        int result = executeUpdate(sql, beforeDate);
        return result > 0;
    }

    @Override
    protected OfficerLog mapRow(ResultSet rs) throws SQLException {
        OfficerLog log = new OfficerLog();
        log.setId(rs.getInt("id"));
        log.setOfficerName(rs.getString("officer_name"));
        log.setBadgeNumber(rs.getString("badge_number"));
        log.setAction(rs.getString("action"));
        log.setVehicleId(rs.getInt("vehicle_id"));
        log.setRegistrationNumber(rs.getString("registration_number"));

        if (rs.getTimestamp("timestamp") != null) {
            log.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
        }
        if (rs.getTimestamp("created_at") != null) {
            log.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            log.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        return log;
    }
}