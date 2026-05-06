package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_audit_logs", "id = ?", id);
        return results.isEmpty() ? null : mapToAuditLog(results.get(0));
    }

    @Override
    public List<AuditLog> findAll() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithPagination("vw_audit_logs", 1000, 0);
        return mapToAuditLogList(results);
    }

    public List<AuditLog> findAllWithPagination(int limit, int offset) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithPagination("vw_audit_logs", limit, offset);
        return mapToAuditLogList(results);
    }

    public List<AuditLog> findByUserId(int userId) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_audit_logs", "user_id = ? ORDER BY timestamp DESC", userId);
        return mapToAuditLogList(results);
    }

    public List<AuditLog> findByUsername(String username) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_audit_logs", "username ILIKE ? ORDER BY timestamp DESC", "%" + username + "%");
        return mapToAuditLogList(results);
    }

    public List<AuditLog> findByAction(String action) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_audit_logs", "action ILIKE ? ORDER BY timestamp DESC", "%" + action + "%");
        return mapToAuditLogList(results);
    }

    public List<AuditLog> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_audit_logs",
                "timestamp BETWEEN ? AND ? ORDER BY timestamp DESC",
                Timestamp.valueOf(startDate), Timestamp.valueOf(endDate));
        return mapToAuditLogList(results);
    }

    public List<AuditLog> findTodayLogs() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_audit_logs",
                "DATE(timestamp) = CURRENT_DATE ORDER BY timestamp DESC");
        return mapToAuditLogList(results);
    }

    public List<AuditLog> findLastHours(int hours) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_audit_logs",
                "timestamp >= CURRENT_TIMESTAMP - INTERVAL '" + hours + " hours' ORDER BY timestamp DESC");
        return mapToAuditLogList(results);
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
        return false;
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return procedureCaller.executeDeleteAuditLog(id);
    }

    public boolean deleteOldLogs(LocalDateTime beforeDate) throws SQLException {
        return procedureCaller.executeDeleteAuditLogsBefore(Timestamp.valueOf(beforeDate));
    }

    public boolean deleteLogsOlderThanDays(int days) throws SQLException {
        return procedureCaller.executeDeleteAuditLogsOlderThan(days);
    }

    public int countTotalLogs() throws SQLException {
        return viewLoader.countViewRows("vw_audit_logs");
    }

    public int countLogsByUser(int userId) throws SQLException {
        return viewLoader.countViewRowsWithCondition("vw_audit_logs", "user_id = ?", userId);
    }

    public List<AuditLog> exportLogs(LocalDateTime startDate, LocalDateTime endDate) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_audit_logs",
                "timestamp BETWEEN ? AND ? ORDER BY timestamp",
                Timestamp.valueOf(startDate), Timestamp.valueOf(endDate));
        return mapToAuditLogList(results);
    }

    // ============================================
    // HELPER METHODS FOR MAPPING
    // ============================================

    private AuditLog mapToAuditLog(Map<String, Object> map) {
        if (map == null) return null;

        AuditLog log = new AuditLog();
        if (map.get("id") != null) log.setId(((Number) map.get("id")).intValue());
        if (map.get("user_id") != null) log.setUserId(((Number) map.get("user_id")).intValue());
        if (map.get("username") != null) log.setUsername(map.get("username").toString());
        if (map.get("action") != null) log.setAction(map.get("action").toString());
        if (map.get("ip_address") != null) log.setIpAddress(map.get("ip_address").toString());
        if (map.get("timestamp") instanceof Timestamp) {
            log.setTimestamp(((Timestamp) map.get("timestamp")).toLocalDateTime());
        }
        return log;
    }

    private List<AuditLog> mapToAuditLogList(List<Map<String, Object>> maps) {
        List<AuditLog> logs = new ArrayList<>();
        if (maps != null) {
            for (Map<String, Object> map : maps) {
                logs.add(mapToAuditLog(map));
            }
        }
        return logs;
    }

    @Override
    protected AuditLog mapRow(ResultSet rs) throws SQLException {
        AuditLog log = new AuditLog();
        log.setId(rs.getInt("id"));
        if (rs.getObject("user_id") != null) log.setUserId(rs.getInt("user_id"));
        log.setUsername(rs.getString("username"));
        log.setAction(rs.getString("action"));
        if (rs.getTimestamp("timestamp") != null) log.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
        log.setIpAddress(rs.getString("ip_address"));
        return log;
    }
}