package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import database.ProcedureCaller;
import database.ViewLoader;
import models.InsurancePolicy;

/**
 * InsurancePolicyDAO - Uses ONLY stored procedures and views for all operations.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class InsurancePolicyDAO extends BaseDAO<InsurancePolicy> {

    private final ProcedureCaller procedureCaller;
    private final ViewLoader viewLoader;

    public InsurancePolicyDAO() {
        this.procedureCaller = new ProcedureCaller();
        this.viewLoader = new ViewLoader();
    }

    @Override
    public InsurancePolicy findById(int id) throws SQLException {
        List<InsurancePolicy> results = viewLoader.loadViewWithCondition("vw_insurance_policies", "id = ?", id);
        return results.isEmpty() ? null : results.get(0);
    }

    public InsurancePolicy findByPolicyNumber(String policyNumber) throws SQLException {
        List<InsurancePolicy> results = viewLoader.loadViewWithCondition("vw_insurance_policies", "policy_number = ?", policyNumber);
        return results.isEmpty() ? null : results.get(0);
    }

    public List<InsurancePolicy> searchByPolicyNumber(String keyword) throws SQLException {
        String pattern = "%" + keyword + "%";
        return viewLoader.loadViewWithCondition("vw_insurance_policies", "policy_number ILIKE ? ORDER BY policy_number", pattern);
    }

    public List<InsurancePolicy> searchByRegistrationNumber(String keyword) throws SQLException {
        String pattern = "%" + keyword + "%";
        return viewLoader.loadViewWithCondition("vw_insurance_policies", "registration_number ILIKE ? ORDER BY registration_number", pattern);
    }

    public List<InsurancePolicy> searchByProviderName(String keyword) throws SQLException {
        String pattern = "%" + keyword + "%";
        return viewLoader.loadViewWithCondition("vw_insurance_policies", "provider_name ILIKE ? ORDER BY provider_name", pattern);
    }

    public List<InsurancePolicy> searchInsurance(String keyword) throws SQLException {
        String pattern = "%" + keyword + "%";
        return viewLoader.loadViewWithCondition("vw_insurance_policies",
                "policy_number ILIKE ? OR registration_number ILIKE ? OR provider_name ILIKE ? OR status ILIKE ? ORDER BY policy_number",
                pattern, pattern, pattern, pattern);
    }

    @Override
    public List<InsurancePolicy> findAll() throws SQLException {
        return viewLoader.loadView("vw_insurance_policies");
    }

    public List<InsurancePolicy> findByVehicleId(int vehicleId) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_insurance_policies", "vehicle_id = ? ORDER BY start_date DESC", vehicleId);
    }

    public List<InsurancePolicy> findActivePolicies() throws SQLException {
        return viewLoader.loadViewWithCondition("vw_insurance_policies", "status = 'ACTIVE' AND end_date >= CURRENT_DATE ORDER BY end_date");
    }

    public List<InsurancePolicy> findByProviderId(int providerId) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_insurance_policies", "provider_id = ? ORDER BY start_date DESC", providerId);
    }

    public List<InsurancePolicy> findByCustomerId(int customerId) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_insurance_policies", "owner_id = ? ORDER BY start_date DESC", customerId);
    }

    public List<InsurancePolicy> findExpiringPolicies(int daysThreshold) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_insurance_policies",
                "status = 'ACTIVE' AND end_date <= CURRENT_DATE + ? ORDER BY end_date", daysThreshold);
    }

    public List<InsurancePolicy> findExpiredPolicies() throws SQLException {
        return viewLoader.loadViewWithCondition("vw_insurance_policies", "end_date < CURRENT_DATE AND status = 'ACTIVE'");
    }

    public int countExpiredByCustomerId(int customerId) throws SQLException {
        return viewLoader.countViewRowsWithCondition("vw_insurance_policies", "owner_id = ? AND (status = 'EXPIRED' OR end_date < CURRENT_DATE)", customerId);
    }

    public int countByProviderId(int providerId) throws SQLException {
        return viewLoader.countViewRowsWithCondition("vw_insurance_policies", "provider_id = ?", providerId);
    }

    public int countActiveByProviderId(int providerId) throws SQLException {
        return viewLoader.countViewRowsWithCondition("vw_insurance_policies", "provider_id = ? AND status = 'ACTIVE' AND end_date >= CURRENT_DATE", providerId);
    }

    public double getTotalPremiumByProvider(int providerId) throws SQLException {
        return viewLoader.getSumPremiumByProvider(providerId);
    }

    public int countActivePolicies() throws SQLException {
        return viewLoader.countViewRowsWithCondition("vw_insurance_policies", "status = 'ACTIVE' AND end_date >= CURRENT_DATE");
    }

    @Override
    public boolean insert(InsurancePolicy entity) throws SQLException {
        Integer policyId = procedureCaller.executeAddInsurancePolicy(
                entity.getVehicleId(),
                entity.getProviderId(),
                entity.getPolicyNumber(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getPremium(),
                entity.getCoverageAmount()
        );
        if (policyId != null && policyId > 0) {
            entity.setId(policyId);
            return true;
        }
        return false;
    }

    public int insertAndGetId(InsurancePolicy entity) throws SQLException {
        return procedureCaller.executeAddInsurancePolicy(
                entity.getVehicleId(),
                entity.getProviderId(),
                entity.getPolicyNumber(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getPremium(),
                entity.getCoverageAmount()
        );
    }

    @Override
    public boolean update(InsurancePolicy entity) throws SQLException {
        return procedureCaller.executeUpdateInsurancePolicy(
                entity.getId(),
                entity.getVehicleId(),
                entity.getProviderId(),
                entity.getPolicyNumber(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getPremium(),
                entity.getCoverageAmount(),
                entity.getStatus()
        );
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return procedureCaller.executeDeleteInsurancePolicy(id);
    }

    @Override
    protected InsurancePolicy mapRow(ResultSet rs) throws SQLException {
        InsurancePolicy policy = new InsurancePolicy();
        policy.setId(rs.getInt("id"));
        policy.setVehicleId(rs.getInt("vehicle_id"));
        policy.setRegistrationNumber(rs.getString("registration_number"));
        policy.setMake(rs.getString("make"));
        policy.setModel(rs.getString("model"));
        policy.setProviderId(rs.getInt("provider_id"));
        policy.setProviderName(rs.getString("provider_name"));
        policy.setPolicyNumber(rs.getString("policy_number"));

        if (rs.getDate("start_date") != null) {
            policy.setStartDate(rs.getDate("start_date").toLocalDate());
        }
        if (rs.getDate("end_date") != null) {
            policy.setEndDate(rs.getDate("end_date").toLocalDate());
        }
        policy.setPremium(rs.getDouble("premium"));
        policy.setCoverageAmount(rs.getDouble("coverage_amount"));
        policy.setStatus(rs.getString("status"));

        if (rs.getTimestamp("created_at") != null) {
            policy.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            policy.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        return policy;
    }
}