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
import models.NoClaimBonusRecord;

/**
 * NoClaimBonusRecordDAO - Uses ONLY stored procedures and views for all operations.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class NoClaimBonusRecordDAO extends BaseDAO<NoClaimBonusRecord> {

    private final ProcedureCaller procedureCaller;
    private final ViewLoader viewLoader;

    public NoClaimBonusRecordDAO() {
        this.procedureCaller = new ProcedureCaller();
        this.viewLoader = new ViewLoader();
    }

    @Override
    public NoClaimBonusRecord findById(int id) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_no_claim_bonus", "id = ?", id);
        if (results.isEmpty()) {
            return null;
        }
        return mapMapToNoClaimBonusRecord(results.get(0));
    }

    public NoClaimBonusRecord findByPolicyId(int policyId) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_no_claim_bonus", "insurance_policy_id = ?", policyId);
        if (results.isEmpty()) {
            return null;
        }
        return mapMapToNoClaimBonusRecord(results.get(0));
    }

    @Override
    public List<NoClaimBonusRecord> findAll() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadView("vw_no_claim_bonus");
        return mapMapsToNoClaimBonusRecords(results);
    }

    public void calculateBonus(int policyId) throws SQLException {
        procedureCaller.executeCalculateNoClaimBonus(policyId);
    }

    @Override
    public boolean insert(NoClaimBonusRecord entity) throws SQLException {
        return procedureCaller.executeInsertNoClaimBonusRecord(
                entity.getInsurancePolicyId(),
                entity.getPolicyYear(),
                entity.getClaimFreeYears(),
                entity.getBonusPercentage(),
                entity.getCalculatedDate()
        );
    }

    public int insertAndGetId(NoClaimBonusRecord entity) throws SQLException {
        boolean success = procedureCaller.executeInsertNoClaimBonusRecord(
                entity.getInsurancePolicyId(),
                entity.getPolicyYear(),
                entity.getClaimFreeYears(),
                entity.getBonusPercentage(),
                entity.getCalculatedDate()
        );
        if (success) {
            NoClaimBonusRecord saved = findByPolicyId(entity.getInsurancePolicyId());
            if (saved != null) {
                entity.setId(saved.getId());
                return saved.getId();
            }
        }
        return -1;
    }

    @Override
    public boolean update(NoClaimBonusRecord entity) throws SQLException {
        return procedureCaller.executeUpdateNoClaimBonusRecord(
                entity.getInsurancePolicyId(),
                entity.getPolicyYear(),
                entity.getClaimFreeYears(),
                entity.getBonusPercentage(),
                entity.getCalculatedDate()
        );
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return procedureCaller.executeDeleteNoClaimBonusRecord(id);
    }

    public boolean deleteByPolicyId(int policyId) throws SQLException {
        return procedureCaller.executeDeleteNoClaimBonusRecordsByPolicy(policyId);
    }

    /**
     * Converts a List of Maps to a List of NoClaimBonusRecord objects.
     */
    private List<NoClaimBonusRecord> mapMapsToNoClaimBonusRecords(List<Map<String, Object>> maps) {
        List<NoClaimBonusRecord> records = new ArrayList<>();
        if (maps == null) {
            return records;
        }
        for (Map<String, Object> map : maps) {
            NoClaimBonusRecord record = mapMapToNoClaimBonusRecord(map);
            if (record != null) {
                records.add(record);
            }
        }
        return records;
    }

    /**
     * Converts a Map to a NoClaimBonusRecord object.
     */
    private NoClaimBonusRecord mapMapToNoClaimBonusRecord(Map<String, Object> map) {
        if (map == null) {
            return null;
        }

        NoClaimBonusRecord record = new NoClaimBonusRecord();

        record.setId(getIntValue(map, "id"));
        record.setInsurancePolicyId(getIntValue(map, "insurance_policy_id"));
        record.setPolicyNumber(getStringValue(map, "policy_number"));
        record.setRegistrationNumber(getStringValue(map, "registration_number"));
        record.setPolicyYear(getIntValue(map, "policy_year"));
        record.setClaimFreeYears(getIntValue(map, "claim_free_years"));
        record.setBonusPercentage(getDoubleValue(map, "bonus_percentage"));
        record.setBasePremium(getDoubleValue(map, "base_premium"));
        record.setDiscountedPremium(getDoubleValue(map, "discounted_premium"));
        record.setSavings(getDoubleValue(map, "savings"));

        record.setCalculatedDate(getLocalDateValue(map, "calculated_date"));
        record.setCreatedAt(getLocalDateTimeValue(map, "created_at"));
        record.setUpdatedAt(getLocalDateTimeValue(map, "updated_at"));

        return record;
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
    protected NoClaimBonusRecord mapRow(ResultSet rs) throws SQLException {
        NoClaimBonusRecord record = new NoClaimBonusRecord();
        record.setId(rs.getInt("id"));
        record.setInsurancePolicyId(rs.getInt("insurance_policy_id"));
        record.setPolicyNumber(rs.getString("policy_number"));
        record.setPolicyYear(rs.getInt("policy_year"));
        record.setClaimFreeYears(rs.getInt("claim_free_years"));
        record.setBonusPercentage(rs.getDouble("bonus_percentage"));

        if (rs.getDate("calculated_date") != null) {
            record.setCalculatedDate(rs.getDate("calculated_date").toLocalDate());
        }

        if (rs.getTimestamp("created_at") != null) {
            record.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            record.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        return record;
    }
}