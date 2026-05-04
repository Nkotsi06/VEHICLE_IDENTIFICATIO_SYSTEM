package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import database.ProcedureCaller;
import database.ViewLoader;
import models.AuditLog;

/**
 * AuditLogDAO - Uses ONLY stored procedures and views for all operations.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class AuditLogDAO extends BaseDAO<AuditLog> {

    private final ProcedureCaller procedureCaller;
    private final ViewLoader viewLoader;

    public AuditLogDAO() {
        this.procedureCaller = new ProcedureCaller();
        this.viewLoader = new ViewLoader();
    }

    @Override
    public AuditLog findById(int id) throws SQLException {
        List<AuditLog> results = viewLoader.loadAuditLogsWithCondition("id = ?", id);
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public List<AuditLog> findAll() throws SQLException {
        return viewLoader.loadAuditLogsWithLimit(1000);
    }

    public List<AuditLog> findAllWithPagination(int limit, int offset) throws SQLException {
        return viewLoader.loadAuditLogsWithPagination(limit, offset);
    }

    public List<AuditLog> findByUserId(int userId) throws SQLException {
        return viewLoader.loadAuditLogsWithCondition("user_id = ? ORDER BY timestamp DESC", userId);
    }

    public List<AuditLog> findByUsername(String username) throws SQLException {
        return viewLoader.loadAuditLogsWithCondition("username ILIKE ? ORDER BY timestamp DESC", "%" + username + "%");
    }

    public List<AuditLog> findByAction(String action) throws SQLException {
        return viewLoader.loadAuditLogsWithCondition("action ILIKE ? ORDER BY timestamp DESC", "%" + action + "%");
    }

    public List<AuditLog> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) throws SQLException {
        return viewLoader.loadAuditLogsWithCondition("timestamp BETWEEN ? AND ? ORDER BY timestamp DESC", startDate, endDate);
    }

    public List<AuditLog> findTodayLogs() throws SQLException {
        return viewLoader.loadAuditLogsWithCondition("DATE(timestamp) = CURRENT_DATE ORDER BY timestamp DESC");
    }

    public List<AuditLog> findLastHours(int hours) throws SQLException {
        return viewLoader.loadAuditLogsWithCondition(
                "timestamp >= CURRENT_TIMESTAMP - INTERVAL '" + hours + " hours' ORDER BY timestamp DESC");
    }

    @Override
    public boolean insert(AuditLog entity) throws SQLException {
        return procedureCaller.executeLogAuditAction(
                entity.getUserId(),
                entity.getAction(),
                entity.getIpAddress()
        );
    }

    public void logAction(int userId, String action, String ipAddress) throws SQLException {
        procedureCaller.executeLogAuditAction(userId, action, ipAddress);
    }

    @Override
    public boolean update(AuditLog entity) throws SQLException {
        // Audit logs are immutable
        return false;
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return procedureCaller.executeDeleteAuditLog(id);
    }

    public int deleteOldLogs(LocalDateTime beforeDate) throws SQLException {
        return procedureCaller.executeDeleteAuditLogsBefore(beforeDate);
    }

    public int deleteLogsOlderThanDays(int days) throws SQLException {
        return procedureCaller.executeDeleteAuditLogsOlderThan(days);
    }

    public int countTotalLogs() throws SQLException {
        return viewLoader.countAuditLogs();
    }

    public int countLogsByUser(int userId) throws SQLException {
        return viewLoader.countAuditLogsWithCondition("user_id = ?", userId);
    }

    public List<AuditLog> exportLogs(LocalDateTime startDate, LocalDateTime endDate) throws SQLException {
        return viewLoader.loadAuditLogsWithCondition("timestamp BETWEEN ? AND ? ORDER BY timestamp", startDate, endDate);
    }

    @Override
    protected AuditLog mapRow(ResultSet rs) throws SQLException {
        AuditLog log = new AuditLog();
        log.setId(rs.getInt("id"));
        if (rs.getObject("user_id") != null) {
            log.setUserId(rs.getInt("user_id"));
        }
        log.setUsername(rs.getString("username"));
        log.setAction(rs.getString("action"));
        if (rs.getTimestamp("timestamp") != null) {
            log.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
        }
        log.setIpAddress(rs.getString("ip_address"));
        return log;
    }
}