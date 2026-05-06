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
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_insurance_verifications", "id = ?", id);
        if (results.isEmpty()) {
            return null;
        }
        return mapMapToInsuranceVerification(results.get(0));
    }

    @Override
    public List<InsuranceVerification> findAll() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadView("vw_insurance_verifications");
        return mapMapsToInsuranceVerifications(results);
    }

    public List<InsuranceVerification> findByInsuranceId(int insuranceId) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_insurance_verifications", "insurance_id = ? ORDER BY verification_date DESC", insuranceId);
        return mapMapsToInsuranceVerifications(results);
    }

    public List<InsuranceVerification> findByVehicleId(int vehicleId) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_insurance_verifications", "vehicle_id = ? ORDER BY verification_date DESC", vehicleId);
        return mapMapsToInsuranceVerifications(results);
    }

    public List<InsuranceVerification> findByStatus(String verificationStatus) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_insurance_verifications", "verification_status = ? ORDER BY verification_date DESC", verificationStatus);
        return mapMapsToInsuranceVerifications(results);
    }

    public List<InsuranceVerification> findPendingVerifications() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_insurance_verifications", "verification_status = 'PENDING' ORDER BY verification_date");
        return mapMapsToInsuranceVerifications(results);
    }

    public List<InsuranceVerification> findByVerifier(int verifiedBy) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_insurance_verifications", "verified_by = ? ORDER BY verification_date DESC", verifiedBy);
        return mapMapsToInsuranceVerifications(results);
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

    /**
     * Converts a List of Maps to a List of InsuranceVerification objects.
     */
    private List<InsuranceVerification> mapMapsToInsuranceVerifications(List<Map<String, Object>> maps) {
        List<InsuranceVerification> verifications = new ArrayList<>();
        if (maps == null) {
            return verifications;
        }
        for (Map<String, Object> map : maps) {
            InsuranceVerification verification = mapMapToInsuranceVerification(map);
            if (verification != null) {
                verifications.add(verification);
            }
        }
        return verifications;
    }

    /**
     * Converts a Map to an InsuranceVerification object.
     */
    private InsuranceVerification mapMapToInsuranceVerification(Map<String, Object> map) {
        if (map == null) {
            return null;
        }

        InsuranceVerification verification = new InsuranceVerification();

        verification.setId(getIntValue(map, "id"));
        verification.setInsuranceId(getIntValue(map, "insurance_id"));
        verification.setPolicyNumber(getStringValue(map, "policy_number"));
        verification.setVerifiedBy(getIntValue(map, "verified_by"));
        verification.setVerifiedByName(getStringValue(map, "verified_by_name"));
        verification.setVerificationStatus(getStringValue(map, "verification_status"));
        verification.setNotes(getStringValue(map, "notes"));

        verification.setVerificationDate(getLocalDateValue(map, "verification_date"));
        verification.setCreatedAt(getLocalDateTimeValue(map, "created_at"));
        verification.setUpdatedAt(getLocalDateTimeValue(map, "updated_at"));

        return verification;
    }

    private Integer getIntValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return 0;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Long) return ((Long) value).intValue();
        if (value instanceof Number) return ((Number) value).intValue();
        return 0;
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
    protected InsuranceVerification mapRow(ResultSet rs) throws SQLException {
        InsuranceVerification verification = new InsuranceVerification();
        verification.setId(rs.getInt("id"));
        verification.setInsuranceId(rs.getInt("insurance_id"));
        verification.setVerifiedBy(rs.getInt("verified_by"));
        verification.setVerifiedByName(rs.getString("verified_by_name"));

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