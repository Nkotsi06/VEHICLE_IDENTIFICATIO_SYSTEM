package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import database.ProcedureCaller;
import database.ViewLoader;
import models.OfficerLog;

/**
 * OfficerLogDAO - Uses ONLY stored procedures and views for all operations.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class OfficerLogDAO extends BaseDAO<OfficerLog> {

    private final ProcedureCaller procedureCaller;
    private final ViewLoader viewLoader;

    public OfficerLogDAO() {
        this.procedureCaller = new ProcedureCaller();
        this.viewLoader = new ViewLoader();
    }

    @Override
    public OfficerLog findById(int id) throws SQLException {
        List<OfficerLog> results = viewLoader.loadViewWithCondition("vw_officer_logs", "id = ?", id);
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public List<OfficerLog> findAll() throws SQLException {
        return viewLoader.loadView("vw_officer_logs");
    }

    public List<OfficerLog> findByOfficerName(String officerName) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_officer_logs", "officer_name ILIKE ? ORDER BY timestamp DESC", "%" + officerName + "%");
    }

    public List<OfficerLog> findByBadgeNumber(String badgeNumber) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_officer_logs", "badge_number = ? ORDER BY timestamp DESC", badgeNumber);
    }

    public List<OfficerLog> findByVehicleId(int vehicleId) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_officer_logs", "vehicle_id = ? ORDER BY timestamp DESC", vehicleId);
    }

    public List<OfficerLog> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_officer_logs", "timestamp BETWEEN ? AND ? ORDER BY timestamp DESC", startDate, endDate);
    }

    public List<OfficerLog> findByAction(String action) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_officer_logs", "action ILIKE ? ORDER BY timestamp DESC", "%" + action + "%");
    }

    @Override
    public boolean insert(OfficerLog entity) throws SQLException {
        return procedureCaller.executeLogOfficerAction(
                entity.getOfficerName(),
                entity.getBadgeNumber(),
                entity.getAction(),
                entity.getVehicleId() > 0 ? entity.getVehicleId() : null
        );
    }

    @Override
    public boolean update(OfficerLog entity) throws SQLException {
        // Officer logs are typically immutable
        return false;
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return procedureCaller.executeDeleteOfficerLog(id);
    }

    public boolean deleteOldLogs(LocalDateTime beforeDate) throws SQLException {
        return procedureCaller.executeDeleteOldOfficerLogs(beforeDate);
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