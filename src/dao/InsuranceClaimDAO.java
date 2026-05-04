package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import database.ProcedureCaller;
import database.ViewLoader;
import models.InsuranceClaim;

/**
 * InsuranceClaimDAO - Uses ONLY stored procedures and views for all operations.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class InsuranceClaimDAO extends BaseDAO<InsuranceClaim> {

    private final ProcedureCaller procedureCaller;
    private final ViewLoader viewLoader;

    public InsuranceClaimDAO() {
        this.procedureCaller = new ProcedureCaller();
        this.viewLoader = new ViewLoader();
    }

    @Override
    public InsuranceClaim findById(int id) throws SQLException {
        List<InsuranceClaim> results = viewLoader.loadViewWithCondition("vw_insurance_claims", "id = ?", id);
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public List<InsuranceClaim> findAll() throws SQLException {
        return viewLoader.loadView("vw_insurance_claims");
    }

    public List<InsuranceClaim> findByPolicyId(int policyId) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_insurance_claims", "policy_id = ? ORDER BY claim_date DESC", policyId);
    }

    public List<InsuranceClaim> findByVehicleId(int vehicleId) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_insurance_claims", "vehicle_id = ? ORDER BY claim_date DESC", vehicleId);
    }

    public List<InsuranceClaim> findByProviderId(int providerId) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_insurance_claims", "provider_id = ? ORDER BY claim_date DESC", providerId);
    }

    public List<InsuranceClaim> findByProviderIdAndDateRange(int providerId, LocalDate startDate, LocalDate endDate) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_insurance_claims",
                "provider_id = ? AND claim_date BETWEEN ? AND ? ORDER BY claim_date DESC",
                providerId, startDate, endDate);
    }

    public List<InsuranceClaim> findPendingClaims() throws SQLException {
        return viewLoader.loadViewWithCondition("vw_insurance_claims", "status = 'PENDING' ORDER BY claim_date");
    }

    public List<InsuranceClaim> findByStatus(String status) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_insurance_claims", "status = ? ORDER BY claim_date DESC", status);
    }

    public int countByProviderId(int providerId) throws SQLException {
        return viewLoader.countViewRowsWithCondition("vw_insurance_claims", "provider_id = ?", providerId);
    }

    public int countPendingByProviderId(int providerId) throws SQLException {
        return viewLoader.countViewRowsWithCondition("vw_insurance_claims", "provider_id = ? AND status = 'PENDING'", providerId);
    }

    public int countPendingClaims() throws SQLException {
        return viewLoader.countViewRowsWithCondition("vw_insurance_claims", "status = 'PENDING'");
    }

    public int countResolvedByProviderId(int providerId) throws SQLException {
        return viewLoader.countViewRowsWithCondition("vw_insurance_claims", "provider_id = ? AND status IN ('APPROVED', 'PAID')", providerId);
    }

    @Override
    public boolean insert(InsuranceClaim entity) throws SQLException {
        Integer claimId = procedureCaller.executeSubmitInsuranceClaim(
                entity.getPolicyId(),
                entity.getClaimAmount(),
                entity.getDescription()
        );
        if (claimId != null && claimId > 0) {
            entity.setId(claimId);
            return true;
        }
        return false;
    }

    public boolean approveClaim(int claimId, double approvedAmount) throws SQLException {
        return procedureCaller.executeApproveClaim(claimId, approvedAmount);
    }

    public boolean rejectClaim(int claimId, String rejectionReason) throws SQLException {
        return procedureCaller.executeRejectClaim(claimId, rejectionReason);
    }

    @Override
    public boolean update(InsuranceClaim entity) throws SQLException {
        if ("APPROVED".equals(entity.getStatus())) {
            return approveClaim(entity.getId(), entity.getApprovedAmount());
        } else if ("REJECTED".equals(entity.getStatus())) {
            return rejectClaim(entity.getId(), entity.getRejectionReason());
        }
        return false;
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return procedureCaller.executeDeleteInsuranceClaim(id);
    }

    @Override
    protected InsuranceClaim mapRow(ResultSet rs) throws SQLException {
        InsuranceClaim claim = new InsuranceClaim();
        claim.setId(rs.getInt("id"));
        claim.setPolicyId(rs.getInt("policy_id"));
        claim.setVehicleId(rs.getInt("vehicle_id"));
        claim.setRegistrationNumber(rs.getString("registration_number"));
        claim.setPolicyNumber(rs.getString("policy_number"));

        if (rs.getDate("claim_date") != null) {
            claim.setClaimDate(rs.getDate("claim_date").toLocalDate());
        }
        claim.setClaimAmount(rs.getDouble("claim_amount"));
        claim.setDescription(rs.getString("description"));
        claim.setStatus(rs.getString("status"));

        if (rs.getObject("approved_amount") != null) {
            claim.setApprovedAmount(rs.getDouble("approved_amount"));
        }
        claim.setRejectionReason(rs.getString("rejection_reason"));

        if (rs.getTimestamp("created_at") != null) {
            claim.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            claim.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        return claim;
    }
}