package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_insurance_policies", "id = ?", id);
        if (results.isEmpty()) {
            return null;
        }
        return mapMapToInsurancePolicy(results.get(0));
    }

    public InsurancePolicy findByPolicyNumber(String policyNumber) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_insurance_policies", "policy_number = ?", policyNumber);
        if (results.isEmpty()) {
            return null;
        }
        return mapMapToInsurancePolicy(results.get(0));
    }

    public List<InsurancePolicy> searchByPolicyNumber(String keyword) throws SQLException {
        String pattern = "%" + keyword + "%";
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_insurance_policies", "policy_number ILIKE ? ORDER BY policy_number", pattern);
        return mapMapsToInsurancePolicies(results);
    }

    public List<InsurancePolicy> searchByRegistrationNumber(String keyword) throws SQLException {
        String pattern = "%" + keyword + "%";
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_insurance_policies", "registration_number ILIKE ? ORDER BY registration_number", pattern);
        return mapMapsToInsurancePolicies(results);
    }

    public List<InsurancePolicy> searchByProviderName(String keyword) throws SQLException {
        String pattern = "%" + keyword + "%";
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_insurance_policies", "provider_name ILIKE ? ORDER BY provider_name", pattern);
        return mapMapsToInsurancePolicies(results);
    }

    public List<InsurancePolicy> searchInsurance(String keyword) throws SQLException {
        String pattern = "%" + keyword + "%";
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_insurance_policies",
                "policy_number ILIKE ? OR registration_number ILIKE ? OR provider_name ILIKE ? OR status ILIKE ? ORDER BY policy_number",
                pattern, pattern, pattern, pattern);
        return mapMapsToInsurancePolicies(results);
    }

    @Override
    public List<InsurancePolicy> findAll() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadView("vw_insurance_policies");
        return mapMapsToInsurancePolicies(results);
    }

    public List<InsurancePolicy> findByVehicleId(int vehicleId) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_insurance_policies", "vehicle_id = ? ORDER BY start_date DESC", vehicleId);
        return mapMapsToInsurancePolicies(results);
    }

    public List<InsurancePolicy> findActivePolicies() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_insurance_policies", "status = 'ACTIVE' AND end_date >= CURRENT_DATE ORDER BY end_date");
        return mapMapsToInsurancePolicies(results);
    }

    public List<InsurancePolicy> findByProviderId(int providerId) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_insurance_policies", "provider_id = ? ORDER BY start_date DESC", providerId);
        return mapMapsToInsurancePolicies(results);
    }

    public List<InsurancePolicy> findByCustomerId(int customerId) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_insurance_policies", "owner_id = ? ORDER BY start_date DESC", customerId);
        return mapMapsToInsurancePolicies(results);
    }

    public List<InsurancePolicy> findExpiringPolicies(int daysThreshold) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_insurance_policies",
                "status = 'ACTIVE' AND end_date <= CURRENT_DATE + ? ORDER BY end_date", daysThreshold);
        return mapMapsToInsurancePolicies(results);
    }

    public List<InsurancePolicy> findExpiredPolicies() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_insurance_policies", "end_date < CURRENT_DATE AND status = 'ACTIVE'");
        return mapMapsToInsurancePolicies(results);
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

    /**
     * Converts a List of Maps to a List of InsurancePolicy objects.
     *
     * @param maps the list of maps from the view loader
     * @return list of InsurancePolicy objects
     */
    private List<InsurancePolicy> mapMapsToInsurancePolicies(List<Map<String, Object>> maps) {
        List<InsurancePolicy> policies = new ArrayList<>();
        if (maps == null) {
            return policies;
        }
        for (Map<String, Object> map : maps) {
            InsurancePolicy policy = mapMapToInsurancePolicy(map);
            if (policy != null) {
                policies.add(policy);
            }
        }
        return policies;
    }

    /**
     * Converts a Map to an InsurancePolicy object.
     *
     * @param map the map from the view loader
     * @return InsurancePolicy object
     */
    private InsurancePolicy mapMapToInsurancePolicy(Map<String, Object> map) {
        if (map == null) {
            return null;
        }

        InsurancePolicy policy = new InsurancePolicy();

        policy.setId(getIntValue(map, "id"));
        policy.setVehicleId(getIntValue(map, "vehicle_id"));
        policy.setRegistrationNumber(getStringValue(map, "registration_number"));
        policy.setMake(getStringValue(map, "make"));
        policy.setModel(getStringValue(map, "model"));
        policy.setVehicleMake(getStringValue(map, "make"));
        policy.setVehicleModel(getStringValue(map, "model"));
        policy.setProviderId(getIntValue(map, "provider_id"));
        policy.setProviderName(getStringValue(map, "provider_name"));
        policy.setPolicyNumber(getStringValue(map, "policy_number"));
        policy.setStartDate(getLocalDateValue(map, "start_date"));
        policy.setEndDate(getLocalDateValue(map, "end_date"));
        policy.setPremium(getDoubleValue(map, "premium"));
        policy.setCoverageAmount(getDoubleValue(map, "coverage_amount"));
        policy.setStatus(getStringValue(map, "status"));
        policy.setOwnerName(getStringValue(map, "owner_name"));
        policy.setOwnerId(getIntValue(map, "owner_id"));
        policy.setCreatedAt(getLocalDateTimeValue(map, "created_at"));
        policy.setUpdatedAt(getLocalDateTimeValue(map, "updated_at"));

        return policy;
    }

    /**
     * Helper method to safely get Integer values from Map.
     */
    private Integer getIntValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return 0;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Long) {
            return ((Long) value).intValue();
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return 0;
    }

    /**
     * Helper method to safely get Double values from Map.
     */
    private Double getDoubleValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return 0.0;
        }
        if (value instanceof Double) {
            return (Double) value;
        }
        if (value instanceof Integer) {
            return ((Integer) value).doubleValue();
        }
        if (value instanceof Long) {
            return ((Long) value).doubleValue();
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return 0.0;
    }

    /**
     * Helper method to safely get String values from Map.
     */
    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "";
    }

    /**
     * Helper method to safely get LocalDate values from Map.
     */
    private LocalDate getLocalDateValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof java.sql.Date) {
            return ((java.sql.Date) value).toLocalDate();
        }
        if (value instanceof LocalDate) {
            return (LocalDate) value;
        }
        return null;
    }

    /**
     * Helper method to safely get LocalDateTime values from Map.
     */
    private LocalDateTime getLocalDateTimeValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof java.sql.Timestamp) {
            return ((java.sql.Timestamp) value).toLocalDateTime();
        }
        if (value instanceof LocalDateTime) {
            return (LocalDateTime) value;
        }
        return null;
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