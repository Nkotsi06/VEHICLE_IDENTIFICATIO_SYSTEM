package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import models.InsuranceClaim;
import models.InsurancePayment;
import models.InsurancePolicy;
import models.InsuranceProvider;
import models.InsuranceVerification;

public class InsuranceDAO extends BaseDAO<InsurancePolicy> {

    private InsurancePolicyDAO policyDAO = new InsurancePolicyDAO();
    private InsuranceClaimDAO claimDAO = new InsuranceClaimDAO();
    private InsuranceProviderDAO providerDAO = new InsuranceProviderDAO();
    private InsurancePaymentDAO paymentDAO = new InsurancePaymentDAO();
    private InsuranceVerificationDAO verificationDAO = new InsuranceVerificationDAO();

    @Override
    public InsurancePolicy findById(int id) throws SQLException {
        return policyDAO.findById(id);
    }

    @Override
    public List<InsurancePolicy> findAll() throws SQLException {
        return policyDAO.findAll();
    }

    public List<InsurancePolicy> findActivePolicies() throws SQLException {
        return policyDAO.findActivePolicies();
    }

    public List<InsurancePolicy> findByVehicleId(int vehicleId) throws SQLException {
        return policyDAO.findByVehicleId(vehicleId);
    }

    public List<InsurancePolicy> findByProviderId(int providerId) throws SQLException {
        return policyDAO.findByProviderId(providerId);
    }

    public List<InsurancePolicy> findExpiringPolicies(int daysThreshold) throws SQLException {
        return policyDAO.findExpiringPolicies(daysThreshold);
    }

    @Override
    public boolean insert(InsurancePolicy entity) throws SQLException {
        return policyDAO.insert(entity);
    }

    public int insertAndGetId(InsurancePolicy entity) throws SQLException {
        String sql = "CALL sp_add_insurance_policy(?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        java.sql.CallableStatement cs = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            cs = conn.prepareCall("{call sp_add_insurance_policy(?, ?, ?, ?, ?, ?, ?, ?)}");
            cs.setInt(1, entity.getVehicleId());
            cs.setInt(2, entity.getProviderId());
            cs.setString(3, entity.getPolicyNumber());
            cs.setDate(4, java.sql.Date.valueOf(entity.getStartDate()));
            cs.setDate(5, java.sql.Date.valueOf(entity.getEndDate()));
            cs.setDouble(6, entity.getPremium());
            cs.setDouble(7, entity.getCoverageAmount());
            cs.setString(8, entity.getStatus());
            cs.execute();

            String querySql = "SELECT id FROM insurance_policies WHERE policy_number = ?";
            ps = conn.prepareStatement(querySql);
            ps.setString(1, entity.getPolicyNumber());
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
            return -1;
        } finally {
            closeResources(rs, ps, conn);
            if (cs != null) cs.close();
        }
    }

    @Override
    public boolean update(InsurancePolicy entity) throws SQLException {
        return policyDAO.update(entity);
    }

    public boolean renewPolicy(int policyId, double newPremium) throws SQLException {
        String sql = "CALL sp_create_policy_renewal(?, ?, ?)";
        int result = executeUpdate(sql, policyId, LocalDate.now().plusYears(1), newPremium);
        return result >= 0;
    }

    public boolean processRenewalPayment(int renewalId) throws SQLException {
        String sql = "CALL sp_process_renewal_payment(?)";
        int result = executeUpdate(sql, renewalId);
        return result >= 0;
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return policyDAO.delete(id);
    }

    public List<InsuranceClaim> getClaimsByPolicy(int policyId) throws SQLException {
        return claimDAO.findByPolicyId(policyId);
    }

    public List<InsuranceClaim> getClaimsByVehicle(int vehicleId) throws SQLException {
        return claimDAO.findByVehicleId(vehicleId);
    }

    public List<InsuranceClaim> getPendingClaims() throws SQLException {
        return claimDAO.findPendingClaims();
    }

    public boolean submitClaim(int policyId, double amount, String description) throws SQLException {
        InsuranceClaim claim = new InsuranceClaim(policyId, amount, description);
        return claimDAO.insert(claim);
    }

    public boolean approveClaim(int claimId, double approvedAmount) throws SQLException {
        return claimDAO.approveClaim(claimId, approvedAmount);
    }

    public boolean rejectClaim(int claimId, String reason) throws SQLException {
        return claimDAO.rejectClaim(claimId, reason);
    }

    public List<InsuranceProvider> getProviders() throws SQLException {
        return providerDAO.findAll();
    }

    public InsuranceProvider getProviderById(int providerId) throws SQLException {
        return providerDAO.findById(providerId);
    }

    public InsuranceProvider getProviderByName(String name) throws SQLException {
        return providerDAO.findByName(name);
    }

    public List<InsurancePayment> getPaymentsByPolicy(int policyId) throws SQLException {
        return paymentDAO.findByInsuranceId(policyId);
    }

    public boolean recordPayment(int insuranceId, double amount, LocalDate dueDate, String paymentMethod) throws SQLException {
        InsurancePayment payment = new InsurancePayment(insuranceId, amount, dueDate, paymentMethod);
        return paymentDAO.insert(payment);
    }

    public boolean verifyInsurance(int insuranceId, int verifiedBy, String status, String notes) throws SQLException {
        return verificationDAO.verifyPolicy(insuranceId, verifiedBy, status, notes);
    }

    public List<InsuranceVerification> getVerificationsByInsurance(int insuranceId) throws SQLException {
        return verificationDAO.findByInsuranceId(insuranceId);
    }

    public List<InsuranceVerification> getPendingVerifications() throws SQLException {
        return verificationDAO.findPendingVerifications();
    }

    public double calculateNoClaimBonus(int policyId) throws SQLException {
        String sql = "CALL sp_calculate_no_claim_bonus(?)";
        executeUpdate(sql, policyId);

        String querySql = "SELECT bonus_percentage FROM vw_no_claim_bonus WHERE policy_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(querySql);
            ps.setInt(1, policyId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getDouble("bonus_percentage");
            }
            return 0;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    public double calculateRiskPremium(int vehicleId) throws SQLException {
        String sql = "SELECT * FROM vw_risk_assessment WHERE vehicle_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, vehicleId);
            rs = ps.executeQuery();
            if (rs.next()) {
                String riskCategory = rs.getString("risk_category");
                switch (riskCategory) {
                    case "LOW": return 1000.00;
                    case "MEDIUM": return 1500.00;
                    case "HIGH": return 2500.00;
                    case "CRITICAL": return 4000.00;
                    default: return 1200.00;
                }
            }
            return 1200.00;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    public int countActivePolicies() throws SQLException {
        return policyDAO.countActivePolicies();
    }

    // Helper method - NOT an override
    // Remove the @Override annotation
    public InsurancePolicy mapRow(ResultSet rs) throws SQLException {
        return policyDAO.mapRow(rs);
    }
}