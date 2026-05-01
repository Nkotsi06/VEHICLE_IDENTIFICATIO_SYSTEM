package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import models.ExpiredDocumentAlert;

public class ExpiryAlertDAO extends BaseDAO<ExpiredDocumentAlert> {

    @Override
    public ExpiredDocumentAlert findById(int id) throws SQLException {
        String sql = "SELECT * FROM vw_vehicle_document_expiry WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
            return null;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    @Override
    public List<ExpiredDocumentAlert> findAll() throws SQLException {
        String sql = "SELECT * FROM vw_vehicle_document_expiry ORDER BY expiry_date";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<ExpiredDocumentAlert> alerts = new ArrayList<>();
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                alerts.add(mapRow(rs));
            }
            return alerts;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    public List<ExpiredDocumentAlert> findByVehicleId(int vehicleId) throws SQLException {
        String sql = "SELECT * FROM vw_vehicle_document_expiry WHERE vehicle_id = ? ORDER BY expiry_date";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<ExpiredDocumentAlert> alerts = new ArrayList<>();
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, vehicleId);
            rs = ps.executeQuery();
            while (rs.next()) {
                alerts.add(mapRow(rs));
            }
            return alerts;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    public List<ExpiredDocumentAlert> findByRegistrationNumber(String registrationNumber) throws SQLException {
        String sql = "SELECT * FROM vw_vehicle_document_expiry WHERE registration_number = ? ORDER BY expiry_date";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<ExpiredDocumentAlert> alerts = new ArrayList<>();
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, registrationNumber);
            rs = ps.executeQuery();
            while (rs.next()) {
                alerts.add(mapRow(rs));
            }
            return alerts;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    public List<ExpiredDocumentAlert> findByDocumentType(String documentType) throws SQLException {
        String sql = "SELECT * FROM vw_vehicle_document_expiry WHERE document_type = ? ORDER BY expiry_date";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<ExpiredDocumentAlert> alerts = new ArrayList<>();
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, documentType);
            rs = ps.executeQuery();
            while (rs.next()) {
                alerts.add(mapRow(rs));
            }
            return alerts;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    public List<ExpiredDocumentAlert> findExpiredDocuments() throws SQLException {
        String sql = "SELECT * FROM vw_expired_documents ORDER BY expiry_date";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<ExpiredDocumentAlert> alerts = new ArrayList<>();
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                alerts.add(mapRow(rs));
            }
            return alerts;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    public List<ExpiredDocumentAlert> findExpiringDocuments(int daysThreshold) throws SQLException {
        String sql = "SELECT * FROM vw_vehicle_document_expiry WHERE expiry_date BETWEEN CURRENT_DATE AND CURRENT_DATE + ? ORDER BY expiry_date";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<ExpiredDocumentAlert> alerts = new ArrayList<>();
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, daysThreshold);
            rs = ps.executeQuery();
            while (rs.next()) {
                alerts.add(mapRow(rs));
            }
            return alerts;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    public List<ExpiredDocumentAlert> findCriticalAlerts() throws SQLException {
        String sql = "SELECT * FROM vw_vehicle_document_expiry WHERE expiry_status IN ('EXPIRED', 'CRITICAL') ORDER BY expiry_date";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<ExpiredDocumentAlert> alerts = new ArrayList<>();
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                alerts.add(mapRow(rs));
            }
            return alerts;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    public List<ExpiredDocumentAlert> findAlertsByStatus(String expiryStatus) throws SQLException {
        String sql = "SELECT * FROM vw_vehicle_document_expiry WHERE expiry_status = ? ORDER BY expiry_date";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<ExpiredDocumentAlert> alerts = new ArrayList<>();
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, expiryStatus);
            rs = ps.executeQuery();
            while (rs.next()) {
                alerts.add(mapRow(rs));
            }
            return alerts;
        } finally {
            closeResources(rs, ps, conn);
        }
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
        String sql = "CALL sp_detect_expired_documents()";
        Connection conn = null;
        java.sql.CallableStatement cs = null;
        try {
            conn = getConnection();
            cs = conn.prepareCall("{call sp_detect_expired_documents()}");
            cs.execute();
        } finally {
            if (cs != null) cs.close();
            closeResources(null, null, conn);
        }
    }

    public boolean generateViolationForExpiredDocuments(int vehicleId) throws SQLException {
        String sql = "INSERT INTO violations (vehicle_id, violation_date, violation_type, fine_amount, officer_name, payment_status) " +
                "SELECT id, CURRENT_DATE, 'EXPIRED_VEHICLE_DOCUMENTS', 500.00, 'SYSTEM', 'UNPAID' " +
                "FROM vehicles WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, vehicleId);
            int result = ps.executeUpdate();
            return result > 0;
        } finally {
            closeResources(null, ps, conn);
        }
    }

    public List<ExpiredDocumentAlert> getVehicleCompleteDocumentStatus(int vehicleId) throws SQLException {
        String sql = "SELECT * FROM vw_vehicle_complete_document_status WHERE vehicle_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<ExpiredDocumentAlert> alerts = new ArrayList<>();
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, vehicleId);
            rs = ps.executeQuery();
            while (rs.next()) {
                alerts.add(mapRow(rs));
            }
            return alerts;
        } finally {
            closeResources(rs, ps, conn);
        }
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
        String sql = "DELETE FROM vehicle_documents WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            int result = ps.executeUpdate();
            return result > 0;
        } finally {
            closeResources(null, ps, conn);
        }
    }

    public boolean deleteByVehicleId(int vehicleId) throws SQLException {
        String sql = "DELETE FROM vehicle_documents WHERE vehicle_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, vehicleId);
            int result = ps.executeUpdate();
            return result > 0;
        } finally {
            closeResources(null, ps, conn);
        }
    }

    public int countExpiredDocuments() throws SQLException {
        String sql = "SELECT COUNT(*) FROM vw_expired_documents";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } finally {
            closeResources(rs, ps, conn);
        }
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
        alert.setDaysOverdue(rs.getInt("days_remaining") < 0 ? Math.abs(rs.getInt("days_remaining")) : 0);

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