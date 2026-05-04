package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import database.ProcedureCaller;
import database.ViewLoader;
import models.Warrant;

/**
 * WarrantDAO - Uses ONLY stored procedures and views for all operations.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class WarrantDAO extends BaseDAO<Warrant> {

    private final ProcedureCaller procedureCaller;
    private final ViewLoader viewLoader;

    public WarrantDAO() {
        this.procedureCaller = new ProcedureCaller();
        this.viewLoader = new ViewLoader();
    }

    @Override
    public Warrant findById(int id) throws SQLException {
        List<Warrant> results = viewLoader.loadViewWithCondition("vw_warrants", "id = ?", id);
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public List<Warrant> findAll() throws SQLException {
        return viewLoader.loadView("vw_warrants");
    }

    public List<Warrant> findActiveWarrants() throws SQLException {
        return viewLoader.loadView("vw_active_warrants");
    }

    public List<Warrant> findByVehicleId(int vehicleId) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_active_warrants", "vehicle_id = ?", vehicleId);
    }

    public List<Warrant> findByViolationId(int violationId) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_warrants", "violation_id = ?", violationId);
    }

    public List<Warrant> findExpiredWarrants() throws SQLException {
        return viewLoader.loadViewWithCondition("vw_warrants", "expiry_date < CURRENT_DATE AND status = 'ACTIVE'");
    }

    public List<Warrant> findByJudge(String judgeName) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_warrants", "judge_name ILIKE ? ORDER BY issue_date DESC", "%" + judgeName + "%");
    }

    public boolean issueWarrant(int violationId, String judgeName, LocalDate issueDate, LocalDate expiryDate) throws SQLException {
        Integer warrantId = procedureCaller.executeIssueWarrant(violationId, judgeName, issueDate, expiryDate);
        return warrantId != null && warrantId > 0;
    }

    @Override
    public boolean insert(Warrant entity) throws SQLException {
        Integer warrantId = procedureCaller.executeIssueWarrant(
                entity.getViolationId(),
                entity.getJudgeName(),
                entity.getIssueDate(),
                entity.getExpiryDate()
        );
        if (warrantId != null && warrantId > 0) {
            entity.setId(warrantId);
            return true;
        }
        return false;
    }

    @Override
    public boolean update(Warrant entity) throws SQLException {
        if ("EXECUTED".equals(entity.getStatus())) {
            return procedureCaller.executeExecuteWarrant(entity.getId());
        } else if ("CANCELLED".equals(entity.getStatus())) {
            return procedureCaller.executeCancelWarrant(entity.getId());
        }
        return false;
    }

    public boolean closeWarrant(int warrantId) throws SQLException {
        return procedureCaller.executeExecuteWarrant(warrantId);
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return procedureCaller.executeDeleteWarrant(id);
    }

    public int countActiveWarrants() throws SQLException {
        return viewLoader.countViewRowsWithCondition("vw_active_warrants", "1=1");
    }

    @Override
    protected Warrant mapRow(ResultSet rs) throws SQLException {
        Warrant warrant = new Warrant();
        warrant.setId(rs.getInt("id"));
        warrant.setViolationId(rs.getInt("violation_id"));
        warrant.setVehicleId(rs.getInt("vehicle_id"));
        warrant.setRegistrationNumber(rs.getString("registration_number"));

        if (rs.getDate("issue_date") != null) {
            warrant.setIssueDate(rs.getDate("issue_date").toLocalDate());
        }
        if (rs.getDate("expiry_date") != null) {
            warrant.setExpiryDate(rs.getDate("expiry_date").toLocalDate());
        }
        warrant.setJudgeName(rs.getString("judge_name"));
        warrant.setStatus(rs.getString("status"));
        warrant.setFineAmount(rs.getDouble("fine_amount"));

        if (rs.getTimestamp("created_at") != null) {
            warrant.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            warrant.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        return warrant;
    }
}