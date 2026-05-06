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
import models.PolicyRenewal;

/**
 * PolicyRenewalDAO - Uses ONLY stored procedures and views for all operations.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class PolicyRenewalDAO extends BaseDAO<PolicyRenewal> {

    private final ProcedureCaller procedureCaller;
    private final ViewLoader viewLoader;

    public PolicyRenewalDAO() {
        this.procedureCaller = new ProcedureCaller();
        this.viewLoader = new ViewLoader();
    }

    @Override
    public PolicyRenewal findById(int id) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_policy_renewals", "id = ?", id);
        if (results.isEmpty()) {
            return null;
        }
        return mapMapToPolicyRenewal(results.get(0));
    }

    @Override
    public List<PolicyRenewal> findAll() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadView("vw_policy_renewals");
        return mapMapsToPolicyRenewals(results);
    }

    public List<PolicyRenewal> findByInsuranceId(int insuranceId) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_policy_renewals", "insurance_id = ? ORDER BY renewal_date DESC", insuranceId);
        return mapMapsToPolicyRenewals(results);
    }

    public List<PolicyRenewal> findByVehicleId(int vehicleId) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_policy_renewals", "vehicle_id = ? ORDER BY renewal_date DESC", vehicleId);
        return mapMapsToPolicyRenewals(results);
    }

    public List<PolicyRenewal> findPendingRenewals() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_policy_renewals", "payment_status = 'PENDING' ORDER BY renewal_date");
        return mapMapsToPolicyRenewals(results);
    }

    public List<PolicyRenewal> findRenewalsByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_policy_renewals", "renewal_date BETWEEN ? AND ? ORDER BY renewal_date", startDate, endDate);
        return mapMapsToPolicyRenewals(results);
    }

    public boolean createRenewal(int insuranceId, LocalDate renewalDate, double premium) throws SQLException {
        return procedureCaller.executeCreatePolicyRenewal(insuranceId, renewalDate, premium);
    }

    public boolean processRenewalPayment(int renewalId) throws SQLException {
        return procedureCaller.executeProcessRenewalPayment(renewalId);
    }

    @Override
    public boolean insert(PolicyRenewal entity) throws SQLException {
        return createRenewal(entity.getInsuranceId(), entity.getRenewalDate(), entity.getPremium());
    }

    @Override
    public boolean update(PolicyRenewal entity) throws SQLException {
        if ("PAID".equals(entity.getPaymentStatus())) {
            return processRenewalPayment(entity.getId());
        }
        return false;
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return procedureCaller.executeDeletePolicyRenewal(id);
    }

    /**
     * Converts a List of Maps to a List of PolicyRenewal objects.
     */
    private List<PolicyRenewal> mapMapsToPolicyRenewals(List<Map<String, Object>> maps) {
        List<PolicyRenewal> renewals = new ArrayList<>();
        if (maps == null) {
            return renewals;
        }
        for (Map<String, Object> map : maps) {
            PolicyRenewal renewal = mapMapToPolicyRenewal(map);
            if (renewal != null) {
                renewals.add(renewal);
            }
        }
        return renewals;
    }

    /**
     * Converts a Map to a PolicyRenewal object.
     */
    private PolicyRenewal mapMapToPolicyRenewal(Map<String, Object> map) {
        if (map == null) {
            return null;
        }

        PolicyRenewal renewal = new PolicyRenewal();

        renewal.setId(getIntValue(map, "id"));
        renewal.setInsuranceId(getIntValue(map, "insurance_id"));
        renewal.setPolicyNumber(getStringValue(map, "policy_number"));
        renewal.setVehicleId(getIntValue(map, "vehicle_id"));
        renewal.setRegistrationNumber(getStringValue(map, "registration_number"));
        renewal.setPremium(getDoubleValue(map, "premium"));
        renewal.setPaymentStatus(getStringValue(map, "payment_status"));

        renewal.setRenewalDate(getLocalDateValue(map, "renewal_date"));
        renewal.setPaymentDate(getLocalDateValue(map, "payment_date"));
        renewal.setCreatedAt(getLocalDateTimeValue(map, "created_at"));
        renewal.setUpdatedAt(getLocalDateTimeValue(map, "updated_at"));

        return renewal;
    }

    private Integer getIntValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return 0;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Long) return ((Long) value).intValue();
        if (value instanceof Number) return ((Number) value).intValue();
        return 0;
    }

    private Double getDoubleValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return 0.0;
        if (value instanceof Double) return (Double) value;
        if (value instanceof Integer) return ((Integer) value).doubleValue();
        if (value instanceof Long) return ((Long) value).doubleValue();
        if (value instanceof Number) return ((Number) value).doubleValue();
        return 0.0;
    }

    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "";
    }

    private LocalDate getLocalDateValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof java.sql.Date) return ((java.sql.Date) value).toLocalDate();
        if (value instanceof LocalDate) return (LocalDate) value;
        return null;
    }

    private LocalDateTime getLocalDateTimeValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof java.sql.Timestamp) return ((java.sql.Timestamp) value).toLocalDateTime();
        if (value instanceof LocalDateTime) return (LocalDateTime) value;
        return null;
    }

    @Override
    protected PolicyRenewal mapRow(ResultSet rs) throws SQLException {
        PolicyRenewal renewal = new PolicyRenewal();
        renewal.setId(rs.getInt("id"));
        renewal.setInsuranceId(rs.getInt("insurance_id"));
        renewal.setPolicyNumber(rs.getString("policy_number"));
        renewal.setVehicleId(rs.getInt("vehicle_id"));
        renewal.setRegistrationNumber(rs.getString("registration_number"));

        if (rs.getDate("renewal_date") != null) {
            renewal.setRenewalDate(rs.getDate("renewal_date").toLocalDate());
        }
        renewal.setPremium(rs.getDouble("premium"));
        renewal.setPaymentStatus(rs.getString("payment_status"));

        if (rs.getDate("payment_date") != null) {
            renewal.setPaymentDate(rs.getDate("payment_date").toLocalDate());
        }

        if (rs.getTimestamp("created_at") != null) {
            renewal.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            renewal.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        return renewal;
    }
}