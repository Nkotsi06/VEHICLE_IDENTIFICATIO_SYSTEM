package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import models.VehicleDocument;

public class VehicleDocumentDAO extends BaseDAO<VehicleDocument> {

    @Override
    public VehicleDocument findById(int id) throws SQLException {
        String sql = "SELECT * FROM vw_vehicle_documents WHERE id = ?";
        return executeQuerySingle(sql, id);
    }

    @Override
    public List<VehicleDocument> findAll() throws SQLException {
        String sql = "SELECT * FROM vw_vehicle_documents ORDER BY expiry_date";
        return executeQuery(sql);
    }

    public List<VehicleDocument> findByVehicleId(int vehicleId) throws SQLException {
        String sql = "SELECT * FROM vw_vehicle_documents WHERE vehicle_id = ? ORDER BY expiry_date";
        return executeQuery(sql, vehicleId);
    }

    public List<VehicleDocument> findByRegistrationNumber(String registrationNumber) throws SQLException {
        String sql = "SELECT vd.* FROM vw_vehicle_documents vd " +
                "WHERE vd.registration_number = ? ORDER BY vd.expiry_date";
        return executeQuery(sql, registrationNumber);
    }

    public List<VehicleDocument> findExpiredDocuments() throws SQLException {
        String sql = "SELECT * FROM vw_expired_documents ORDER BY expiry_date";
        return executeQuery(sql);
    }

    public List<VehicleDocument> findExpiringDocuments(int daysThreshold) throws SQLException {
        String sql = "SELECT * FROM vw_vehicle_document_expiry WHERE days_remaining BETWEEN 0 AND ? ORDER BY expiry_date";
        return executeQuery(sql, daysThreshold);
    }

    public List<VehicleDocument> findByDocumentType(String documentType) throws SQLException {
        String sql = "SELECT * FROM vw_vehicle_documents WHERE document_type = ? ORDER BY expiry_date";
        return executeQuery(sql, documentType);
    }

    @Override
    public boolean insert(VehicleDocument entity) throws SQLException {
        String sql = "INSERT INTO vehicle_documents (vehicle_id, document_type, document_number, issue_date, expiry_date, document_file_path, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        int result = executeUpdate(sql,
                entity.getVehicleId(),
                entity.getDocumentType(),
                entity.getDocumentNumber(),
                entity.getIssueDate(),
                entity.getExpiryDate(),
                entity.getDocumentFilePath(),
                entity.getStatus()
        );
        return result > 0;
    }

    public int insertAndGetId(VehicleDocument entity) throws SQLException {
        String sql = "INSERT INTO vehicle_documents (vehicle_id, document_type, document_number, issue_date, expiry_date, document_file_path, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        return executeUpdateWithGeneratedKeys(sql,
                entity.getVehicleId(),
                entity.getDocumentType(),
                entity.getDocumentNumber(),
                entity.getIssueDate(),
                entity.getExpiryDate(),
                entity.getDocumentFilePath(),
                entity.getStatus()
        );
    }

    @Override
    public boolean update(VehicleDocument entity) throws SQLException {
        String sql = "UPDATE vehicle_documents SET document_number = ?, issue_date = ?, expiry_date = ?, document_file_path = ?, status = ? WHERE id = ?";
        int result = executeUpdate(sql,
                entity.getDocumentNumber(),
                entity.getIssueDate(),
                entity.getExpiryDate(),
                entity.getDocumentFilePath(),
                entity.getStatus(),
                entity.getId()
        );
        return result > 0;
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM vehicle_documents WHERE id = ?";
        int result = executeUpdate(sql, id);
        return result > 0;
    }

    public boolean deleteByVehicleId(int vehicleId) throws SQLException {
        String sql = "DELETE FROM vehicle_documents WHERE vehicle_id = ?";
        int result = executeUpdate(sql, vehicleId);
        return result > 0;
    }

    public boolean updateStatus(int documentId, String status) throws SQLException {
        String sql = "UPDATE vehicle_documents SET status = ? WHERE id = ?";
        int result = executeUpdate(sql, status, documentId);
        return result > 0;
    }

    @Override
    protected VehicleDocument mapRow(ResultSet rs) throws SQLException {
        VehicleDocument doc = new VehicleDocument();
        doc.setId(rs.getInt("id"));
        doc.setVehicleId(rs.getInt("vehicle_id"));
        doc.setRegistrationNumber(rs.getString("registration_number"));
        doc.setDocumentType(rs.getString("document_type"));
        doc.setDocumentNumber(rs.getString("document_number"));

        if (rs.getDate("issue_date") != null) {
            doc.setIssueDate(rs.getDate("issue_date").toLocalDate());
        }
        if (rs.getDate("expiry_date") != null) {
            doc.setExpiryDate(rs.getDate("expiry_date").toLocalDate());
        }
        doc.setDocumentFilePath(rs.getString("document_file_path"));
        doc.setStatus(rs.getString("status"));

        if (rs.getTimestamp("created_at") != null) {
            doc.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            doc.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        return doc;
    }
}