package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.List;

import models.ExpiredDocumentAlert;

public class ExpiredDocumentAlertDAO extends BaseDAO<ExpiredDocumentAlert> {

    @Override
    public ExpiredDocumentAlert findById(int id) throws SQLException {
        String sql = "SELECT * FROM vw_expired_documents WHERE id = ?";
        return executeQuerySingle(sql, id);
    }

    @Override
    public List<ExpiredDocumentAlert> findAll() throws SQLException {
        String sql = "SELECT * FROM vw_expired_documents ORDER BY expiry_date";
        return executeQuery(sql);
    }

    public List<ExpiredDocumentAlert> findByVehicleId(int vehicleId) throws SQLException {
        String sql = "SELECT * FROM vw_expired_documents WHERE vehicle_id = ? ORDER BY expiry_date";
        return executeQuery(sql, vehicleId);
    }

    public List<ExpiredDocumentAlert> findByAlertLevel(String alertLevel) throws SQLException {
        String sql = "SELECT * FROM vw_expired_documents WHERE expiry_status = ? ORDER BY expiry_date";
        return executeQuery(sql, alertLevel);
    }

    public List<ExpiredDocumentAlert> findCriticalAlerts() throws SQLException {
        String sql = "SELECT * FROM vw_expired_documents WHERE expiry_status IN ('EXPIRED', 'CRITICAL') ORDER BY expiry_date";
        return executeQuery(sql);
    }

    public List<ExpiredDocumentAlert> findExpiringWithinDays(int days) throws SQLException {
        String sql = "SELECT vd.*, v.registration_number, v.make, v.model FROM vehicle_documents vd " +
                "JOIN vehicles v ON vd.vehicle_id = v.id " +
                "WHERE vd.expiry_date BETWEEN CURRENT_DATE AND CURRENT_DATE + ? " +
                "ORDER BY vd.expiry_date";
        return executeQuery(sql, days);
    }

    public void checkVehicleDocuments(String registrationNumber) throws SQLException {
        String sql = "CALL sp_check_vehicle_documents(?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        java.sql.CallableStatement cs = null;
        try {
            conn = getConnection();
            cs = conn.prepareCall("{call sp_check_vehicle_documents(?, ?, ?, ?, ?, ?)}");
            cs.setString(1, registrationNumber);
            cs.registerOutParameter(2, Types.INTEGER);
            cs.registerOutParameter(3, Types.INTEGER);
            cs.registerOutParameter(4, Types.INTEGER);
            cs.registerOutParameter(5, Types.VARCHAR);
            cs.registerOutParameter(6, Types.VARCHAR);
            cs.execute();
        } finally {
            if (cs != null) cs.close();
            closeResources(null, null, conn);
        }
    }

    public void runExpiredDocumentDetection() throws SQLException {
        return;
    }

    @Override
    public boolean insert(ExpiredDocumentAlert entity) throws SQLException {
        return false;
    }

    @Override
    public boolean update(ExpiredDocumentAlert entity) throws SQLException {
        return false;
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return false;
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

        if (rs.getDate("expiry_date") != null) {
            alert.setExpiryDate(rs.getDate("expiry_date").toLocalDate());
        }

        if (alert.getExpiryDate() != null) {
            long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(alert.getExpiryDate(), LocalDate.now());
            alert.setDaysOverdue((int) Math.max(0, daysOverdue));
        }

        String expiryStatus = rs.getString("expiry_status");
        if ("EXPIRED".equals(expiryStatus)) {
            alert.setAlertLevel("HIGH");
            alert.setRecommendedAction("IMMEDIATE_FINE");
        } else if ("CRITICAL".equals(expiryStatus)) {
            alert.setAlertLevel("MEDIUM");
            alert.setRecommendedAction("WARNING_NOTICE");
        } else if ("WARNING".equals(expiryStatus)) {
            alert.setAlertLevel("LOW");
            alert.setRecommendedAction("REMINDER");
        }

        return alert;
    }
}