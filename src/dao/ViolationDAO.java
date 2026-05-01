package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import models.Violation;

public class ViolationDAO extends BaseDAO<Violation> {

    @Override
    public Violation findById(int id) throws SQLException {
        String sql = "SELECT * FROM vw_violations WHERE id = ?";
        return executeQuerySingle(sql, id);
    }

    @Override
    public List<Violation> findAll() throws SQLException {
        String sql = "SELECT * FROM vw_violations ORDER BY violation_date DESC";
        return executeQuery(sql);
    }

    public List<Violation> findByVehicleId(int vehicleId) throws SQLException {
        String sql = "SELECT * FROM vw_violations WHERE vehicle_id = ? ORDER BY violation_date DESC";
        return executeQuery(sql, vehicleId);
    }

    public List<Violation> findByRegistrationNumber(String registrationNumber) throws SQLException {
        String sql = "SELECT * FROM vw_violations WHERE registration_number = ? ORDER BY violation_date DESC";
        return executeQuery(sql, registrationNumber);
    }

    public List<Violation> findUnpaidViolations() throws SQLException {
        String sql = "SELECT * FROM vw_unpaid_violations ORDER BY violation_date DESC";
        return executeQuery(sql);
    }

    public List<Violation> findPaidViolations() throws SQLException {
        String sql = "SELECT * FROM vw_violations WHERE payment_status = 'PAID' ORDER BY violation_date DESC";
        return executeQuery(sql);
    }

    public List<Violation> findByViolationType(String violationType) throws SQLException {
        String sql = "SELECT * FROM vw_violations WHERE violation_type ILIKE ? ORDER BY violation_date DESC";
        return executeQuery(sql, "%" + violationType + "%");
    }

    public List<Violation> findByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        String sql = "SELECT * FROM vw_violations WHERE violation_date BETWEEN ? AND ? ORDER BY violation_date DESC";
        return executeQuery(sql, startDate, endDate);
    }

    public List<Violation> findByOfficer(String officerName) throws SQLException {
        String sql = "SELECT * FROM vw_violations WHERE officer_name ILIKE ? ORDER BY violation_date DESC";
        return executeQuery(sql, "%" + officerName + "%");
    }

    @Override
    public boolean insert(Violation entity) throws SQLException {
        return executeProcedure("sp_add_violation",
                entity.getVehicleId(),
                entity.getViolationDate(),
                entity.getViolationType(),
                entity.getFineAmount(),
                entity.getLocation(),
                entity.getOfficerName(),
                entity.getPaymentStatus()
        );
    }

    @Override
    public boolean update(Violation entity) throws SQLException {
        return executeProcedure("sp_update_violation",
                entity.getId(),
                entity.getVehicleId(),
                entity.getViolationDate(),
                entity.getViolationType(),
                entity.getFineAmount(),
                entity.getLocation(),
                entity.getOfficerName(),
                entity.getPaymentStatus()
        );
    }

    public boolean markAsPaid(int violationId) throws SQLException {
        return executeProcedure("sp_mark_violation_paid", violationId);
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return executeProcedure("sp_delete_violation", id);
    }

    public double getTotalUnpaidFines() throws SQLException {
        String sql = "SELECT COALESCE(SUM(fine_amount), 0) FROM violations WHERE payment_status = 'UNPAID'";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
            return 0;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    public int countViolationsByVehicle(int vehicleId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM violations WHERE vehicle_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, vehicleId);
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
    protected Violation mapRow(ResultSet rs) throws SQLException {
        Violation violation = new Violation();
        violation.setId(rs.getInt("id"));
        violation.setVehicleId(rs.getInt("vehicle_id"));
        violation.setRegistrationNumber(rs.getString("registration_number"));
        violation.setMake(rs.getString("make"));
        violation.setModel(rs.getString("model"));

        if (rs.getDate("violation_date") != null) {
            violation.setViolationDate(rs.getDate("violation_date").toLocalDate());
        }
        violation.setViolationType(rs.getString("violation_type"));
        violation.setFineAmount(rs.getDouble("fine_amount"));
        violation.setPaymentStatus(rs.getString("payment_status"));
        violation.setLocation(rs.getString("location"));
        violation.setOfficerName(rs.getString("officer_name"));

        if (rs.getTimestamp("created_at") != null) {
            violation.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            violation.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        return violation;
    }
}