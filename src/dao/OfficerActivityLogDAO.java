package dao;

import java.sql.*;
import java.util.List;
import models.OfficerActivityLog;

public class OfficerActivityLogDAO extends BaseDAO<OfficerActivityLog> {

    @Override
    public OfficerActivityLog findById(int id) throws SQLException {
        String sql = "SELECT oal.*, po.badge_number, u.full_name as officer_name " +
                "FROM officer_activity_log oal " +
                "JOIN police_officers po ON oal.officer_id = po.id " +
                "JOIN users u ON po.user_id = u.id " +
                "WHERE oal.id = ?";
        return executeQuerySingle(sql, id);
    }

    @Override
    public List<OfficerActivityLog> findAll() throws SQLException {
        String sql = "SELECT oal.*, po.badge_number, u.full_name as officer_name " +
                "FROM officer_activity_log oal " +
                "JOIN police_officers po ON oal.officer_id = po.id " +
                "JOIN users u ON po.user_id = u.id " +
                "ORDER BY oal.created_at DESC LIMIT 100";
        return executeQuery(sql);
    }

    public List<OfficerActivityLog> findByOfficerId(int officerId) throws SQLException {
        String sql = "SELECT oal.*, po.badge_number, u.full_name as officer_name " +
                "FROM officer_activity_log oal " +
                "JOIN police_officers po ON oal.officer_id = po.id " +
                "JOIN users u ON po.user_id = u.id " +
                "WHERE oal.officer_id = ? " +
                "ORDER BY oal.created_at DESC LIMIT 50";
        return executeQuery(sql, officerId);
    }

    public List<OfficerActivityLog> findByActionType(String actionType) throws SQLException {
        String sql = "SELECT oal.*, po.badge_number, u.full_name as officer_name " +
                "FROM officer_activity_log oal " +
                "JOIN police_officers po ON oal.officer_id = po.id " +
                "JOIN users u ON po.user_id = u.id " +
                "WHERE oal.action_type = ? " +
                "ORDER BY oal.created_at DESC";
        return executeQuery(sql, actionType);
    }

    @Override
    public boolean insert(OfficerActivityLog entity) throws SQLException {
        String sql = "INSERT INTO officer_activity_log (officer_id, action_type, action_description, target_type, target_id, ip_address) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        int result = executeUpdate(sql,
                entity.getOfficerId(),
                entity.getActionType(),
                entity.getActionDescription(),
                entity.getTargetType(),
                entity.getTargetId() > 0 ? entity.getTargetId() : null,
                entity.getIpAddress()
        );
        return result > 0;
    }

    public void logActivity(int officerId, String actionType, String description, String targetType, Integer targetId, String ipAddress) throws SQLException {
        OfficerActivityLog log = new OfficerActivityLog();
        log.setOfficerId(officerId);
        log.setActionType(actionType);
        log.setActionDescription(description);
        log.setTargetType(targetType);
        log.setTargetId(targetId != null ? targetId : 0);
        log.setIpAddress(ipAddress);
        insert(log);
    }

    @Override
    public boolean update(OfficerActivityLog entity) throws SQLException {
        return false;
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM officer_activity_log WHERE id = ?";
        int result = executeUpdate(sql, id);
        return result > 0;
    }

    @Override
    protected OfficerActivityLog mapRow(ResultSet rs) throws SQLException {
        OfficerActivityLog log = new OfficerActivityLog();
        log.setId(rs.getInt("id"));
        log.setOfficerId(rs.getInt("officer_id"));
        log.setOfficerName(rs.getString("officer_name"));
        log.setActionType(rs.getString("action_type"));
        log.setActionDescription(rs.getString("action_description"));
        log.setTargetType(rs.getString("target_type"));

        int targetId = rs.getInt("target_id");
        if (targetId > 0) {
            log.setTargetId(targetId);
        }

        log.setIpAddress(rs.getString("ip_address"));

        if (rs.getTimestamp("created_at") != null) {
            log.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }

        return log;
    }
}