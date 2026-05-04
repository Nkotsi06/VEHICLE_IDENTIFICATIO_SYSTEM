package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import database.ProcedureCaller;
import database.ViewLoader;
import models.OfficerActivityLog;

/**
 * OfficerActivityLogDAO - Uses ONLY stored procedures and views for all operations.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class OfficerActivityLogDAO extends BaseDAO<OfficerActivityLog> {

    private final ProcedureCaller procedureCaller;
    private final ViewLoader viewLoader;

    public OfficerActivityLogDAO() {
        this.procedureCaller = new ProcedureCaller();
        this.viewLoader = new ViewLoader();
    }

    @Override
    public OfficerActivityLog findById(int id) throws SQLException {
        List<OfficerActivityLog> results = viewLoader.loadViewWithCondition("vw_officer_activity_log", "id = ?", id);
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public List<OfficerActivityLog> findAll() throws SQLException {
        return viewLoader.loadView("vw_officer_activity_log");
    }

    public List<OfficerActivityLog> findByOfficerId(int officerId) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_officer_activity_log", "officer_id = ? ORDER BY created_at DESC", officerId);
    }

    public List<OfficerActivityLog> findByActionType(String actionType) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_officer_activity_log", "action_type = ? ORDER BY created_at DESC", actionType);
    }

    @Override
    public boolean insert(OfficerActivityLog entity) throws SQLException {
        return procedureCaller.executeInsertOfficerActivityLog(
                entity.getOfficerId(),
                entity.getActionType(),
                entity.getActionDescription(),
                entity.getTargetType(),
                entity.getTargetId() > 0 ? entity.getTargetId() : null,
                entity.getIpAddress()
        );
    }

    public void logActivity(int officerId, String actionType, String description,
                            String targetType, Integer targetId, String ipAddress) throws SQLException {
        procedureCaller.executeInsertOfficerActivityLog(officerId, actionType, description, targetType, targetId, ipAddress);
    }

    @Override
    public boolean update(OfficerActivityLog entity) throws SQLException {
        // Activity logs are immutable
        return false;
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return procedureCaller.executeDeleteOfficerActivityLog(id);
    }

    public int deleteOldLogs(java.time.LocalDateTime beforeDate) throws SQLException {
        return procedureCaller.executeDeleteOldOfficerActivityLogs(beforeDate);
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