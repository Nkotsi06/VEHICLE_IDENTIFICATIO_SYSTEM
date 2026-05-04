package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

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
        List<ExpiredDocumentAlert> results = viewLoader.loadViewWithCondition("vw_vehicle_document_expiry", "id = ?", id);
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public List<ExpiredDocumentAlert> findAll() throws SQLException {
        return viewLoader.loadView("vw_vehicle_document_expiry");
    }

    public List<ExpiredDocumentAlert> findByVehicleId(int vehicleId) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_vehicle_document_expiry", "vehicle_id = ? ORDER BY expiry_date", vehicleId);
    }

    public List<ExpiredDocumentAlert> findByRegistrationNumber(String registrationNumber) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_vehicle_document_expiry", "registration_number = ? ORDER BY expiry_date", registrationNumber);
    }

    public List<ExpiredDocumentAlert> findByDocumentType(String documentType) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_vehicle_document_expiry", "document_type = ? ORDER BY expiry_date", documentType);
    }

    public List<ExpiredDocumentAlert> findExpiredDocuments() throws SQLException {
        return viewLoader.loadView("vw_expired_documents");
    }

    public List<ExpiredDocumentAlert> findExpiringDocuments(int daysThreshold) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_vehicle_document_expiry",
                "expiry_date BETWEEN CURRENT_DATE AND CURRENT_DATE + ? ORDER BY expiry_date", daysThreshold);
    }

    public List<ExpiredDocumentAlert> findCriticalAlerts() throws SQLException {
        return viewLoader.loadViewWithCondition("vw_vehicle_document_expiry",
                "expiry_status IN ('EXPIRED', 'CRITICAL') ORDER BY expiry_date");
    }

    public List<ExpiredDocumentAlert> findAlertsByStatus(String expiryStatus) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_vehicle_document_expiry", "expiry_status = ? ORDER BY expiry_date", expiryStatus);
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
        return viewLoader.loadViewWithCondition("vw_vehicle_complete_document_status", "vehicle_id = ?", vehicleId);
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