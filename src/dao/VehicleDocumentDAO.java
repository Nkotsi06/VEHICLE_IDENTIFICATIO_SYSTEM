package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import database.ProcedureCaller;
import database.ViewLoader;
import models.VehicleDocument;

/**
 * VehicleDocumentDAO - Uses ONLY stored procedures and views for all operations.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class VehicleDocumentDAO extends BaseDAO<VehicleDocument> {

    private final ProcedureCaller procedureCaller;
    private final ViewLoader viewLoader;

    public VehicleDocumentDAO() {
        this.procedureCaller = new ProcedureCaller();
        this.viewLoader = new ViewLoader();
    }

    @Override
    public VehicleDocument findById(int id) throws SQLException {
        List<VehicleDocument> results = viewLoader.loadViewWithCondition("vw_vehicle_documents", "id = ?", id);
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public List<VehicleDocument> findAll() throws SQLException {
        return viewLoader.loadView("vw_vehicle_documents");
    }

    public List<VehicleDocument> findByVehicleId(int vehicleId) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_vehicle_documents", "vehicle_id = ? ORDER BY expiry_date", vehicleId);
    }

    public List<VehicleDocument> findByRegistrationNumber(String registrationNumber) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_vehicle_documents", "registration_number = ? ORDER BY expiry_date", registrationNumber);
    }

    public List<VehicleDocument> findExpiredDocuments() throws SQLException {
        return viewLoader.loadView("vw_expired_documents");
    }

    public List<VehicleDocument> findExpiringDocuments(int daysThreshold) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_vehicle_document_expiry", "days_remaining BETWEEN 0 AND ? ORDER BY expiry_date", daysThreshold);
    }

    public List<VehicleDocument> findByDocumentType(String documentType) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_vehicle_documents", "document_type = ? ORDER BY expiry_date", documentType);
    }

    @Override
    public boolean insert(VehicleDocument entity) throws SQLException {
        Integer docId = procedureCaller.executeInsertVehicleDocument(
                entity.getVehicleId(),
                entity.getDocumentType(),
                entity.getDocumentNumber(),
                entity.getIssueDate(),
                entity.getExpiryDate(),
                entity.getDocumentFilePath(),
                entity.getStatus()
        );
        if (docId != null && docId > 0) {
            entity.setId(docId);
            return true;
        }
        return false;
    }

    public int insertAndGetId(VehicleDocument entity) throws SQLException {
        return procedureCaller.executeInsertVehicleDocument(
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
        return procedureCaller.executeUpdateVehicleDocument(
                entity.getId(),
                entity.getDocumentNumber(),
                entity.getIssueDate(),
                entity.getExpiryDate(),
                entity.getDocumentFilePath(),
                entity.getStatus()
        );
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return procedureCaller.executeDeleteVehicleDocument(id);
    }

    public boolean deleteByVehicleId(int vehicleId) throws SQLException {
        return procedureCaller.executeDeleteVehicleDocumentsByVehicle(vehicleId);
    }

    public boolean updateStatus(int documentId, String status) throws SQLException {
        return procedureCaller.executeUpdateVehicleDocumentStatus(documentId, status);
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