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
import models.ExpiredDocumentAlert;

/**
 * ExpiryAlertDAO - Uses ONLY stored procedures and views for all operations.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class ExpiryAlertDAO extends BaseDAO<ExpiredDocumentAlert> {

    private final ProcedureCaller procedureCaller;
    private final ViewLoader viewLoader;

    public ExpiryAlertDAO() {
        this.procedureCaller = new ProcedureCaller();
        this.viewLoader = new ViewLoader();
    }

    @Override
    public ExpiredDocumentAlert findById(int id) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_vehicle_document_expiry", "id = ?", id);
        return results.isEmpty() ? null : mapToExpiredDocumentAlert(results.get(0));
    }

    @Override
    public List<ExpiredDocumentAlert> findAll() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadView("vw_vehicle_document_expiry");
        return mapToExpiredDocumentAlertList(results);
    }

    public List<ExpiredDocumentAlert> findByVehicleId(int vehicleId) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_vehicle_document_expiry", "vehicle_id = ? ORDER BY expiry_date", vehicleId);
        return mapToExpiredDocumentAlertList(results);
    }

    public List<ExpiredDocumentAlert> findByRegistrationNumber(String registrationNumber) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_vehicle_document_expiry", "registration_number = ? ORDER BY expiry_date", registrationNumber);
        return mapToExpiredDocumentAlertList(results);
    }

    public List<ExpiredDocumentAlert> findByDocumentType(String documentType) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_vehicle_document_expiry", "document_type = ? ORDER BY expiry_date", documentType);
        return mapToExpiredDocumentAlertList(results);
    }

    public List<ExpiredDocumentAlert> findExpiredDocuments() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadView("vw_expired_documents");
        return mapToExpiredDocumentAlertList(results);
    }

    public List<ExpiredDocumentAlert> findExpiringDocuments(int daysThreshold) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_vehicle_document_expiry",
                "expiry_date BETWEEN CURRENT_DATE AND CURRENT_DATE + ? ORDER BY expiry_date", daysThreshold);
        return mapToExpiredDocumentAlertList(results);
    }

    public List<ExpiredDocumentAlert> findCriticalAlerts() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_vehicle_document_expiry",
                "expiry_status IN ('EXPIRED', 'CRITICAL') ORDER BY expiry_date");
        return mapToExpiredDocumentAlertList(results);
    }

    public List<ExpiredDocumentAlert> findAlertsByStatus(String expiryStatus) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_vehicle_document_expiry", "expiry_status = ? ORDER BY expiry_date", expiryStatus);
        return mapToExpiredDocumentAlertList(results);
    }

    public void checkVehicleDocuments(String registrationNumber) throws SQLException {
        procedureCaller.executeCheckVehicleDocuments(registrationNumber);
    }

    public void runExpiredDocumentDetection() throws SQLException {
        procedureCaller.executeDetectExpiredDocuments();
    }

    public boolean generateViolationForExpiredDocuments(int vehicleId) throws SQLException {
        return procedureCaller.executeGenerateViolationForExpiredDocuments(vehicleId);
    }

    public List<ExpiredDocumentAlert> getVehicleCompleteDocumentStatus(int vehicleId) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_vehicle_complete_document_status", "vehicle_id = ?", vehicleId);
        return mapToExpiredDocumentAlertList(results);
    }

    @Override
    public boolean insert(ExpiredDocumentAlert entity) throws SQLException {
        // Alerts are auto-generated
        return false;
    }

    @Override
    public boolean update(ExpiredDocumentAlert entity) throws SQLException {
        // Alerts are auto-generated
        return false;
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return procedureCaller.executeDeleteExpiredDocumentAlert(id);
    }

    public boolean deleteByVehicleId(int vehicleId) throws SQLException {
        return procedureCaller.executeDeleteExpiredDocumentsByVehicle(vehicleId);
    }

    public int countExpiredDocuments() throws SQLException {
        return viewLoader.countViewRows("vw_expired_documents");
    }

    // ============================================
    // HELPER METHODS FOR MAPPING
    // ============================================

    private ExpiredDocumentAlert mapToExpiredDocumentAlert(Map<String, Object> map) {
        if (map == null) return null;

        ExpiredDocumentAlert alert = new ExpiredDocumentAlert();

        if (map.get("id") != null) alert.setId(((Number) map.get("id")).intValue());
        if (map.get("vehicle_id") != null) alert.setVehicleId(((Number) map.get("vehicle_id")).intValue());
        if (map.get("registration_number") != null) alert.setRegistrationNumber(map.get("registration_number").toString());
        if (map.get("make") != null) alert.setMake(map.get("make").toString());
        if (map.get("model") != null) alert.setModel(map.get("model").toString());
        if (map.get("document_type") != null) alert.setDocumentType(map.get("document_type").toString());
        if (map.get("document_number") != null) alert.setDocumentNumber(map.get("document_number").toString());

        if (map.get("issue_date") != null) {
            Object dateObj = map.get("issue_date");
            if (dateObj instanceof java.sql.Date) {
                alert.setIssueDate(((java.sql.Date) dateObj).toLocalDate());
            } else if (dateObj instanceof LocalDate) {
                alert.setIssueDate((LocalDate) dateObj);
            }
        }
        if (map.get("expiry_date") != null) {
            Object dateObj = map.get("expiry_date");
            if (dateObj instanceof java.sql.Date) {
                alert.setExpiryDate(((java.sql.Date) dateObj).toLocalDate());
            } else if (dateObj instanceof LocalDate) {
                alert.setExpiryDate((LocalDate) dateObj);
            }
        }

        int daysRemaining = map.get("days_remaining") != null ? ((Number) map.get("days_remaining")).intValue() : 0;
        alert.setDaysOverdue(daysRemaining < 0 ? Math.abs(daysRemaining) : 0);

        String expiryStatus = map.get("expiry_status") != null ? map.get("expiry_status").toString() : "";
        if ("EXPIRED".equals(expiryStatus)) {
            alert.setAlertLevel("CRITICAL");
            alert.setRecommendedAction("IMMEDIATE_VEHICLE_IMPOUND");
        } else if ("CRITICAL".equals(expiryStatus)) {
            alert.setAlertLevel("HIGH");
            alert.setRecommendedAction("ON_THE_SPOT_FINE");
        } else if ("WARNING".equals(expiryStatus)) {
            alert.setAlertLevel("MEDIUM");
            alert.setRecommendedAction("WARNING_NOTICE");
        } else if ("DUE_SOON".equals(expiryStatus)) {
            alert.setAlertLevel("LOW");
            alert.setRecommendedAction("REMINDER");
        } else {
            alert.setAlertLevel("NONE");
            alert.setRecommendedAction("NO_ACTION");
        }

        alert.setNotified(false);

        if (map.get("created_at") instanceof java.sql.Timestamp) {
            alert.setCreatedAt(((java.sql.Timestamp) map.get("created_at")).toLocalDateTime());
        }
        if (map.get("updated_at") instanceof java.sql.Timestamp) {
            alert.setUpdatedAt(((java.sql.Timestamp) map.get("updated_at")).toLocalDateTime());
        }

        return alert;
    }

    private List<ExpiredDocumentAlert> mapToExpiredDocumentAlertList(List<Map<String, Object>> maps) {
        List<ExpiredDocumentAlert> alerts = new ArrayList<>();
        if (maps != null) {
            for (Map<String, Object> map : maps) {
                alerts.add(mapToExpiredDocumentAlert(map));
            }
        }
        return alerts;
    }

    @Override
    protected ExpiredDocumentAlert mapRow(ResultSet rs) throws SQLException {
        ExpiredDocumentAlert alert = new ExpiredDocumentAlert();
        alert.setId(rs.getInt("id"));
        alert.setVehicleId(rs.getInt("vehicle_id"));
        alert.setRegistrationNumber(rs.getString("registration_number"));
        alert.setMake(rs.getString("make"));
        alert.setModel(rs.getString("model"));
        alert.setDocumentType(rs.getString("document_type"));
        alert.setDocumentNumber(rs.getString("document_number"));

        if (rs.getDate("issue_date") != null) {
            alert.setIssueDate(rs.getDate("issue_date").toLocalDate());
        }
        if (rs.getDate("expiry_date") != null) {
            alert.setExpiryDate(rs.getDate("expiry_date").toLocalDate());
        }

        int daysRemaining = rs.getInt("days_remaining");
        alert.setDaysOverdue(daysRemaining < 0 ? Math.abs(daysRemaining) : 0);

        String expiryStatus = rs.getString("expiry_status");
        if ("EXPIRED".equals(expiryStatus)) {
            alert.setAlertLevel("CRITICAL");
            alert.setRecommendedAction("IMMEDIATE_VEHICLE_IMPOUND");
        } else if ("CRITICAL".equals(expiryStatus)) {
            alert.setAlertLevel("HIGH");
            alert.setRecommendedAction("ON_THE_SPOT_FINE");
        } else if ("WARNING".equals(expiryStatus)) {
            alert.setAlertLevel("MEDIUM");
            alert.setRecommendedAction("WARNING_NOTICE");
        } else if ("DUE_SOON".equals(expiryStatus)) {
            alert.setAlertLevel("LOW");
            alert.setRecommendedAction("REMINDER");
        } else {
            alert.setAlertLevel("NONE");
            alert.setRecommendedAction("NO_ACTION");
        }

        alert.setNotified(false);

        if (rs.getTimestamp("created_at") != null) {
            alert.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            alert.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        return alert;
    }
}