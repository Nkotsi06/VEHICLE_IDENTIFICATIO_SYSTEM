package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import models.AuditLog;

public class AuditDAO extends BaseDAO<AuditLog> {

    @Override
    public AuditLog findById(int id) throws SQLException {
        String sql = "SELECT * FROM vw_audit_logs WHERE id = ?";
        return executeQuerySingle(sql, id);
    }

    @Override
    public List<AuditLog> findAll() throws SQLException {
        String sql = "SELECT * FROM vw_audit_logs LIMIT 1000";
        return executeQuery(sql);
    }

    public List<AuditLog> findAllWithPagination(int limit, int offset) throws SQLException {
        String sql = "SELECT * FROM vw_audit_logs LIMIT ? OFFSET ?";
        return executeQuery(sql, limit, offset);
    }

    public List<AuditLog> findByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM vw_audit_logs WHERE user_id = ? ORDER BY timestamp DESC";
        return executeQuery(sql, userId);
    }

    public List<AuditLog> findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM vw_audit_logs WHERE username ILIKE ? ORDER BY timestamp DESC";
        return executeQuery(sql, "%" + username + "%");
    }

    public List<AuditLog> findByAction(String action) throws SQLException {
        String sql = "SELECT * FROM vw_audit_logs WHERE action ILIKE ? ORDER BY timestamp DESC";
        return executeQuery(sql, "%" + action + "%");
    }

    public List<AuditLog> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) throws SQLException {
        String sql = "SELECT * FROM vw_audit_logs WHERE timestamp BETWEEN ? AND ? ORDER BY timestamp DESC";
        return executeQuery(sql, startDate, endDate);
    }

    public List<AuditLog> findTodayLogs() throws SQLException {
        String sql = "SELECT * FROM vw_audit_logs WHERE DATE(timestamp) = CURRENT_DATE ORDER BY timestamp DESC";
        return executeQuery(sql);
    }

    public List<AuditLog> findLastHours(int hours) throws SQLException {
        String sql = "SELECT * FROM vw_audit_logs WHERE timestamp >= CURRENT_TIMESTAMP - INTERVAL '" + hours + " hours' ORDER BY timestamp DESC";
        return executeQuery(sql);
    }

    @Override
    public boolean insert(AuditLog entity) throws SQLException {
        return executeProcedure("sp_log_audit_action",
                entity.getUserId(),
                entity.getAction(),
                entity.getIpAddress()
        );
    }

    public void logAction(int userId, String action, String ipAddress) throws SQLException {
        insert(new AuditLog(userId, action, ipAddress));
    }

    @Override
    public boolean update(AuditLog entity) throws SQLException {
        return false;
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM audit_logs WHERE id = ?";
        int result = executeUpdate(sql, id);
        return result > 0;
    }

    public int deleteOldLogs(LocalDateTime beforeDate) throws SQLException {
        String sql = "DELETE FROM audit_logs WHERE timestamp < ?";
        int result = executeUpdate(sql, beforeDate);
        return result;
    }

    public int deleteLogsOlderThanDays(int days) throws SQLException {
        String sql = "DELETE FROM audit_logs WHERE timestamp < CURRENT_TIMESTAMP - INTERVAL '" + days + " days'";
        int result = executeUpdate(sql);
        return result;
    }

    public int countTotalLogs() throws SQLException {
        String sql = "SELECT COUNT(*) FROM audit_logs";
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

    public int countLogsByUser(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM audit_logs WHERE user_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    public List<AuditLog> exportLogs(LocalDateTime startDate, LocalDateTime endDate) throws SQLException {
        String sql = "SELECT * FROM vw_audit_logs WHERE timestamp BETWEEN ? AND ? ORDER BY timestamp";
        return executeQuery(sql, startDate, endDate);
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