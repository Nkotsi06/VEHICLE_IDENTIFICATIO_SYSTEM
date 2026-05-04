package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import database.ProcedureCaller;
import database.ViewLoader;
import models.InsuranceVerification;

/**
 * InsuranceVerificationDAO - Uses ONLY stored procedures and views for all operations.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class InsuranceVerificationDAO extends BaseDAO<InsuranceVerification> {

    private final ProcedureCaller procedureCaller;
    private final ViewLoader viewLoader;

    public InsuranceVerificationDAO() {
        this.procedureCaller = new ProcedureCaller();
        this.viewLoader = new ViewLoader();
    }

    @Override
    public InsuranceVerification findById(int id) throws SQLException {
        List<InsuranceVerification> results = viewLoader.loadViewWithCondition("vw_insurance_verifications", "id = ?", id);
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public List<InsuranceVerification> findAll() throws SQLException {
        return viewLoader.loadView("vw_insurance_verifications");
    }

    public List<InsuranceVerification> findByInsuranceId(int insuranceId) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_insurance_verifications", "insurance_id = ? ORDER BY verification_date DESC", insuranceId);
    }

    public List<InsuranceVerification> findByVehicleId(int vehicleId) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_insurance_verifications", "vehicle_id = ? ORDER BY verification_date DESC", vehicleId);
    }

    public List<InsuranceVerification> findByStatus(String verificationStatus) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_insurance_verifications", "verification_status = ? ORDER BY verification_date DESC", verificationStatus);
    }

    public List<InsuranceVerification> findPendingVerifications() throws SQLException {
        return viewLoader.loadViewWithCondition("vw_insurance_verifications", "verification_status = 'PENDING' ORDER BY verification_date");
    }

    public List<InsuranceVerification> findByVerifier(int verifiedBy) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_insurance_verifications", "verified_by = ? ORDER BY verification_date DESC", verifiedBy);
    }

    @Override
    public boolean insert(InsuranceVerification entity) throws SQLException {
        return procedureCaller.executeInsertInsuranceVerification(
                entity.getInsuranceId(),
                entity.getVerifiedBy(),
                entity.getVerificationDate(),
                entity.getVerificationStatus(),
                entity.getNotes()
        );
    }

    @Override
    public boolean update(InsuranceVerification entity) throws SQLException {
        return procedureCaller.executeUpdateInsuranceVerification(
                entity.getId(),
                entity.getVerificationStatus(),
                entity.getNotes()
        );
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return procedureCaller.executeDeleteInsuranceVerification(id);
    }

    public boolean verifyPolicy(int insuranceId, int verifiedBy, String status, String notes) throws SQLException {
        return procedureCaller.executeVerifyInsurancePolicy(insuranceId, verifiedBy, status, notes);
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