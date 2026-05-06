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
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_insurance_claims", "id = ?", id);
        if (results.isEmpty()) {
            return null;
        }
        return mapMapToInsuranceClaim(results.get(0));
    }

    @Override
    public List<InsuranceClaim> findAll() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadView("vw_insurance_claims");
        return mapMapsToInsuranceClaims(results);
    }

    public List<InsuranceClaim> findByPolicyId(int policyId) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_insurance_claims", "policy_id = ? ORDER BY claim_date DESC", policyId);
        return mapMapsToInsuranceClaims(results);
    }

    public List<InsuranceClaim> findByVehicleId(int vehicleId) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_insurance_claims", "vehicle_id = ? ORDER BY claim_date DESC", vehicleId);
        return mapMapsToInsuranceClaims(results);
    }

    public List<InsuranceClaim> findByProviderId(int providerId) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_insurance_claims", "provider_id = ? ORDER BY claim_date DESC", providerId);
        return mapMapsToInsuranceClaims(results);
    }

    public List<InsuranceClaim> findByProviderIdAndDateRange(int providerId, LocalDate startDate, LocalDate endDate) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_insurance_claims",
                "provider_id = ? AND claim_date BETWEEN ? AND ? ORDER BY claim_date DESC",
                providerId, startDate, endDate);
        return mapMapsToInsuranceClaims(results);
    }

    /**
     * Finds insurance claims within a date range.
     *
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return list of insurance claims in the date range
     * @throws SQLException if database error occurs
     */
    public List<InsuranceClaim> findByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_insurance_claims",
                "claim_date BETWEEN ? AND ? ORDER BY claim_date DESC", startDate, endDate);
        return mapMapsToInsuranceClaims(results);
    }

    public List<InsuranceClaim> findPendingClaims() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_insurance_claims", "status = 'PENDING' ORDER BY claim_date");
        return mapMapsToInsuranceClaims(results);
    }

    public List<InsuranceClaim> findByStatus(String status) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_insurance_claims", "status = ? ORDER BY claim_date DESC", status);
        return mapMapsToInsuranceClaims(results);
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

    /**
     * Converts a List of Maps to a List of InsuranceClaim objects.
     *
     * @param maps the list of maps from the view loader
     * @return list of InsuranceClaim objects
     */
    private List<InsuranceClaim> mapMapsToInsuranceClaims(List<Map<String, Object>> maps) {
        List<InsuranceClaim> claims = new ArrayList<>();
        if (maps == null) {
            return claims;
        }
        for (Map<String, Object> map : maps) {
            InsuranceClaim claim = mapMapToInsuranceClaim(map);
            if (claim != null) {
                claims.add(claim);
            }
        }
        return claims;
    }

    /**
     * Converts a Map to an InsuranceClaim object.
     *
     * @param map the map from the view loader
     * @return InsuranceClaim object
     */
    private InsuranceClaim mapMapToInsuranceClaim(Map<String, Object> map) {
        if (map == null) {
            return null;
        }

        InsuranceClaim claim = new InsuranceClaim();

        claim.setId(getIntValue(map, "id"));
        claim.setPolicyId(getIntValue(map, "policy_id"));
        claim.setVehicleId(getIntValue(map, "vehicle_id"));
        claim.setRegistrationNumber(getStringValue(map, "registration_number"));
        claim.setPolicyNumber(getStringValue(map, "policy_number"));
        claim.setClaimAmount(getDoubleValue(map, "claim_amount"));
        claim.setDescription(getStringValue(map, "description"));
        claim.setStatus(getStringValue(map, "status"));
        claim.setRejectionReason(getStringValue(map, "rejection_reason"));

        // Handle approved_amount (can be null)
        Object approvedAmountObj = map.get("approved_amount");
        if (approvedAmountObj instanceof Number) {
            claim.setApprovedAmount(((Number) approvedAmountObj).doubleValue());
        }

        claim.setClaimDate(getLocalDateValue(map, "claim_date"));
        claim.setCreatedAt(getLocalDateTimeValue(map, "created_at"));
        claim.setUpdatedAt(getLocalDateTimeValue(map, "updated_at"));

        return claim;
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