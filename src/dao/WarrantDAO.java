package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import models.Warrant;

public class WarrantDAO extends BaseDAO<Warrant> {

    @Override
    public Warrant findById(int id) throws SQLException {
        String sql = "SELECT * FROM warrants WHERE id = ?";
        return executeQuerySingle(sql, id);
    }

    @Override
    public List<Warrant> findAll() throws SQLException {
        String sql = "SELECT * FROM vw_active_warrants ORDER BY issue_date DESC";
        return executeQuery(sql);
    }

    public List<Warrant> findActiveWarrants() throws SQLException {
        String sql = "SELECT * FROM vw_active_warrants ORDER BY expiry_date ASC";
        return executeQuery(sql);
    }

    public List<Warrant> findByVehicleId(int vehicleId) throws SQLException {
        String sql = "SELECT * FROM vw_active_warrants WHERE vehicle_id = ?";
        return executeQuery(sql, vehicleId);
    }

    public List<Warrant> findByViolationId(int violationId) throws SQLException {
        String sql = "SELECT * FROM warrants WHERE violation_id = ?";
        return executeQuery(sql, violationId);
    }

    public List<Warrant> findExpiredWarrants() throws SQLException {
        String sql = "SELECT * FROM warrants WHERE expiry_date < CURRENT_DATE AND status = 'ACTIVE'";
        return executeQuery(sql);
    }

    public List<Warrant> findByJudge(String judgeName) throws SQLException {
        String sql = "SELECT * FROM warrants WHERE judge_name ILIKE ? ORDER BY issue_date DESC";
        return executeQuery(sql, "%" + judgeName + "%");
    }

    public boolean issueWarrant(int violationId, String judgeName, LocalDate issueDate, LocalDate expiryDate) throws SQLException {
        return executeProcedure("sp_issue_warrant", violationId, judgeName, issueDate, expiryDate);
    }

    @Override
    public boolean insert(Warrant entity) throws SQLException {
        return executeProcedure("sp_issue_warrant",
                entity.getViolationId(),
                entity.getJudgeName(),
                entity.getIssueDate(),
                entity.getExpiryDate()
        );
    }

    @Override
    public boolean update(Warrant entity) throws SQLException {
        String sql = "UPDATE warrants SET status = ? WHERE id = ?";
        int result = executeUpdate(sql, entity.getStatus(), entity.getId());
        return result > 0;
    }

    public boolean closeWarrant(int warrantId) throws SQLException {
        String sql = "CALL sp_close_warrant(?)";
        int result = executeUpdate(sql, warrantId);
        return result >= 0;
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM warrants WHERE id = ?";
        int result = executeUpdate(sql, id);
        return result > 0;
    }

    public int countActiveWarrants() throws SQLException {
        String sql = "SELECT COUNT(*) FROM warrants WHERE status = 'ACTIVE' AND expiry_date >= CURRENT_DATE";
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