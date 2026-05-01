package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import models.InsuranceVerification;

public class InsuranceVerificationDAO extends BaseDAO<InsuranceVerification> {

    @Override
    public InsuranceVerification findById(int id) throws SQLException {
        String sql = "SELECT * FROM insurance_verifications WHERE id = ?";
        return executeQuerySingle(sql, id);
    }

    @Override
    public List<InsuranceVerification> findAll() throws SQLException {
        String sql = "SELECT * FROM insurance_verifications ORDER BY verification_date DESC";
        return executeQuery(sql);
    }

    public List<InsuranceVerification> findByInsuranceId(int insuranceId) throws SQLException {
        String sql = "SELECT * FROM insurance_verifications WHERE insurance_id = ? ORDER BY verification_date DESC";
        return executeQuery(sql, insuranceId);
    }

    public List<InsuranceVerification> findByVehicleId(int vehicleId) throws SQLException {
        String sql = "SELECT * FROM insurance_verifications WHERE vehicle_id = ? ORDER BY verification_date DESC";
        return executeQuery(sql, vehicleId);
    }

    public List<InsuranceVerification> findByStatus(String verificationStatus) throws SQLException {
        String sql = "SELECT * FROM insurance_verifications WHERE verification_status = ? ORDER BY verification_date DESC";
        return executeQuery(sql, verificationStatus);
    }

    public List<InsuranceVerification> findPendingVerifications() throws SQLException {
        String sql = "SELECT * FROM insurance_verifications WHERE verification_status = 'PENDING' ORDER BY verification_date";
        return executeQuery(sql);
    }

    public List<InsuranceVerification> findByVerifier(int verifiedBy) throws SQLException {
        String sql = "SELECT * FROM insurance_verifications WHERE verified_by = ? ORDER BY verification_date DESC";
        return executeQuery(sql, verifiedBy);
    }

    @Override
    public boolean insert(InsuranceVerification entity) throws SQLException {
        String sql = "INSERT INTO insurance_verifications (insurance_id, verified_by, verification_date, verification_status, notes) VALUES (?, ?, ?, ?, ?)";
        int result = executeUpdate(sql,
                entity.getInsuranceId(),
                entity.getVerifiedBy(),
                entity.getVerificationDate(),
                entity.getVerificationStatus(),
                entity.getNotes()
        );
        return result > 0;
    }

    @Override
    public boolean update(InsuranceVerification entity) throws SQLException {
        String sql = "UPDATE insurance_verifications SET verification_status = ?, notes = ? WHERE id = ?";
        int result = executeUpdate(sql, entity.getVerificationStatus(), entity.getNotes(), entity.getId());
        return result > 0;
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM insurance_verifications WHERE id = ?";
        int result = executeUpdate(sql, id);
        return result > 0;
    }

    public boolean verifyPolicy(int insuranceId, int verifiedBy, String status, String notes) throws SQLException {
        String sql = "INSERT INTO insurance_verifications (insurance_id, verified_by, verification_date, verification_status, notes) VALUES (?, ?, CURRENT_DATE, ?, ?)";
        int result = executeUpdate(sql, insuranceId, verifiedBy, status, notes);
        return result > 0;
    }

    @Override
    protected InsuranceVerification mapRow(ResultSet rs) throws SQLException {
        InsuranceVerification verification = new InsuranceVerification();
        verification.setId(rs.getInt("id"));
        verification.setInsuranceId(rs.getInt("insurance_id"));
        verification.setVerifiedBy(rs.getInt("verified_by"));

        if (rs.getDate("verification_date") != null) {
            verification.setVerificationDate(rs.getDate("verification_date").toLocalDate());
        }
        verification.setVerificationStatus(rs.getString("verification_status"));
        verification.setNotes(rs.getString("notes"));

        if (rs.getTimestamp("created_at") != null) {
            verification.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            verification.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        return verification;
    }
}