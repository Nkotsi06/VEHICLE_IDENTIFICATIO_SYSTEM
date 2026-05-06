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
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_vehicle_documents", "id = ?", id);
        if (results.isEmpty()) {
            return null;
        }
        return mapMapToVehicleDocument(results.get(0));
    }

    @Override
    public List<VehicleDocument> findAll() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadView("vw_vehicle_documents");
        return mapMapsToVehicleDocuments(results);
    }

    public List<VehicleDocument> findByVehicleId(int vehicleId) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_vehicle_documents", "vehicle_id = ? ORDER BY expiry_date", vehicleId);
        return mapMapsToVehicleDocuments(results);
    }

    public List<VehicleDocument> findByRegistrationNumber(String registrationNumber) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_vehicle_documents", "registration_number = ? ORDER BY expiry_date", registrationNumber);
        return mapMapsToVehicleDocuments(results);
    }

    public List<VehicleDocument> findExpiredDocuments() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadView("vw_expired_documents");
        return mapMapsToVehicleDocuments(results);
    }

    public List<VehicleDocument> findExpiringDocuments(int daysThreshold) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_vehicle_document_expiry", "days_remaining BETWEEN 0 AND ? ORDER BY expiry_date", daysThreshold);
        return mapMapsToVehicleDocuments(results);
    }

    public List<VehicleDocument> findByDocumentType(String documentType) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_vehicle_documents", "document_type = ? ORDER BY expiry_date", documentType);
        return mapMapsToVehicleDocuments(results);
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

    /**
     * Converts a List of Maps to a List of VehicleDocument objects.
     */
    private List<VehicleDocument> mapMapsToVehicleDocuments(List<Map<String, Object>> maps) {
        List<VehicleDocument> documents = new ArrayList<>();
        if (maps == null) {
            return documents;
        }
        for (Map<String, Object> map : maps) {
            VehicleDocument doc = mapMapToVehicleDocument(map);
            if (doc != null) {
                documents.add(doc);
            }
        }
        return documents;
    }

    /**
     * Converts a Map to a VehicleDocument object.
     */
    private VehicleDocument mapMapToVehicleDocument(Map<String, Object> map) {
        if (map == null) {
            return null;
        }

        VehicleDocument doc = new VehicleDocument();

        doc.setId(getIntValue(map, "id"));
        doc.setVehicleId(getIntValue(map, "vehicle_id"));
        doc.setRegistrationNumber(getStringValue(map, "registration_number"));
        doc.setDocumentType(getStringValue(map, "document_type"));
        doc.setDocumentNumber(getStringValue(map, "document_number"));
        doc.setDocumentFilePath(getStringValue(map, "document_file_path"));
        doc.setStatus(getStringValue(map, "status"));

        doc.setIssueDate(getLocalDateValue(map, "issue_date"));
        doc.setExpiryDate(getLocalDateValue(map, "expiry_date"));
        doc.setCreatedAt(getLocalDateTimeValue(map, "created_at"));
        doc.setUpdatedAt(getLocalDateTimeValue(map, "updated_at"));

        return doc;
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