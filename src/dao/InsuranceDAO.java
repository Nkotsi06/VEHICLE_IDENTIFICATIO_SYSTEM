package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import database.ProcedureCaller;
import database.ViewLoader;
import models.InsuranceClaim;
import models.InsurancePayment;
import models.InsurancePolicy;
import models.InsuranceProvider;
import models.InsuranceVerification;

/**
 * InsuranceDAO - Facade that uses ONLY stored procedures and views for all operations.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class InsuranceDAO extends BaseDAO<InsurancePolicy> {

    private final ProcedureCaller procedureCaller;
    private final ViewLoader viewLoader;
    private final InsurancePolicyDAO policyDAO;
    private final InsuranceClaimDAO claimDAO;
    private final InsuranceProviderDAO providerDAO;
    private final InsurancePaymentDAO paymentDAO;
    private final InsuranceVerificationDAO verificationDAO;

    public InsuranceDAO() {
        this.procedureCaller = new ProcedureCaller();
        this.viewLoader = new ViewLoader();
        this.policyDAO = new InsurancePolicyDAO();
        this.claimDAO = new InsuranceClaimDAO();
        this.providerDAO = new InsuranceProviderDAO();
        this.paymentDAO = new InsurancePaymentDAO();
        this.verificationDAO = new InsuranceVerificationDAO();
    }

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
        return policyDAO.insertAndGetId(entity);
    }

    @Override
    public boolean update(InsurancePolicy entity) throws SQLException {
        return policyDAO.update(entity);
    }

    public boolean renewPolicy(int policyId, double newPremium) throws SQLException {
        return procedureCaller.executeCreatePolicyRenewal(policyId, LocalDate.now().plusYears(1), newPremium);
    }

    public boolean processRenewalPayment(int renewalId) throws SQLException {
        return procedureCaller.executeProcessRenewalPayment(renewalId);
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
        procedureCaller.executeCalculateNoClaimBonus(policyId);
        return viewLoader.getNoClaimBonusPercentage(policyId);
    }

    public double calculateRiskPremium(int vehicleId) throws SQLException {
        return procedureCaller.executeCalculateRiskPremium(vehicleId);
    }

    public int countActivePolicies() throws SQLException {
        return policyDAO.countActivePolicies();
    }

    // Helper method - NOT an override
    public InsurancePolicy mapRow(ResultSet rs) throws SQLException {
        return policyDAO.mapRow(rs);
    }
}