package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import database.ProcedureCaller;
import database.ViewLoader;
import models.InsuranceDocument;

/**
 * InsuranceDocumentDAO - Uses ONLY stored procedures and views for all operations.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class InsuranceDocumentDAO extends BaseDAO<InsuranceDocument> {

    private final ProcedureCaller procedureCaller;
    private final ViewLoader viewLoader;

    public InsuranceDocumentDAO() {
        this.procedureCaller = new ProcedureCaller();
        this.viewLoader = new ViewLoader();
    }

    @Override
    public InsuranceDocument findById(int id) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_insurance_documents", "id = ?", id);
        if (results.isEmpty()) {
            return null;
        }
        return mapMapToInsuranceDocument(results.get(0));
    }

    @Override
    public List<InsuranceDocument> findAll() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadView("vw_insurance_documents");
        return mapMapsToInsuranceDocuments(results);
    }

    public List<InsuranceDocument> findByInsuranceId(int insuranceId) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_insurance_documents", "insurance_id = ? ORDER BY upload_date DESC", insuranceId);
        return mapMapsToInsuranceDocuments(results);
    }

    public List<InsuranceDocument> findByDocumentType(String documentType) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_insurance_documents", "document_type = ? ORDER BY upload_date DESC", documentType);
        return mapMapsToInsuranceDocuments(results);
    }

    @Override
    public boolean insert(InsuranceDocument entity) throws SQLException {
        Integer docId = procedureCaller.executeInsertInsuranceDocument(
                entity.getInsuranceId(),
                entity.getFileName(),
                entity.getFilePath(),
                entity.getDocumentType(),
                entity.getFileSize()
        );
        if (docId != null && docId > 0) {
            entity.setId(docId);
            return true;
        }
        return false;
    }

    public int insertAndGetId(InsuranceDocument entity) throws SQLException {
        Integer docId = procedureCaller.executeInsertInsuranceDocumentWithReturn(
                entity.getInsuranceId(),
                entity.getFileName(),
                entity.getFilePath(),
                entity.getDocumentType(),
                entity.getFileSize()
        );
        if (docId != null && docId > 0) {
            entity.setId(docId);
            return docId;
        }
        return -1;
    }

    @Override
    public boolean update(InsuranceDocument entity) throws SQLException {
        return procedureCaller.executeUpdateInsuranceDocument(
                entity.getId(),
                entity.getFileName(),
                entity.getFilePath(),
                entity.getDocumentType(),
                entity.getFileSize()
        );
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return procedureCaller.executeDeleteInsuranceDocument(id);
    }

    public boolean deleteByInsuranceId(int insuranceId) throws SQLException {
        return procedureCaller.executeDeleteInsuranceDocumentsByInsurance(insuranceId);
    }

    /**
     * Converts a List of Maps to a List of InsuranceDocument objects.
     */
    private List<InsuranceDocument> mapMapsToInsuranceDocuments(List<Map<String, Object>> maps) {
        List<InsuranceDocument> documents = new ArrayList<>();
        if (maps == null) {
            return documents;
        }
        for (Map<String, Object> map : maps) {
            InsuranceDocument doc = mapMapToInsuranceDocument(map);
            if (doc != null) {
                documents.add(doc);
            }
        }
        return documents;
    }

    /**
     * Converts a Map to an InsuranceDocument object.
     */
    private InsuranceDocument mapMapToInsuranceDocument(Map<String, Object> map) {
        if (map == null) {
            return null;
        }

        InsuranceDocument doc = new InsuranceDocument();

        doc.setId(getIntValue(map, "id"));
        doc.setInsuranceId(getIntValue(map, "insurance_id"));
        doc.setFileName(getStringValue(map, "file_name"));
        doc.setFilePath(getStringValue(map, "file_path"));
        doc.setDocumentType(getStringValue(map, "document_type"));
        doc.setFileSize(getLongValue(map, "file_size"));

        doc.setUploadDate(getLocalDateTimeValue(map, "upload_date"));
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

    private Long getLongValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return 0L;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Integer) return ((Integer) value).longValue();
        if (value instanceof Number) return ((Number) value).longValue();
        return 0L;
    }

    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "";
    }

    private LocalDateTime getLocalDateTimeValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof java.sql.Timestamp) return ((java.sql.Timestamp) value).toLocalDateTime();
        if (value instanceof LocalDateTime) return (LocalDateTime) value;
        return null;
    }

    @Override
    protected InsuranceDocument mapRow(ResultSet rs) throws SQLException {
        InsuranceDocument doc = new InsuranceDocument();
        doc.setId(rs.getInt("id"));
        doc.setInsuranceId(rs.getInt("insurance_id"));
        doc.setFileName(rs.getString("file_name"));
        doc.setFilePath(rs.getString("file_path"));
        doc.setDocumentType(rs.getString("document_type"));
        doc.setFileSize(rs.getLong("file_size"));

        if (rs.getTimestamp("upload_date") != null) {
            doc.setUploadDate(rs.getTimestamp("upload_date").toLocalDateTime());
        }
        if (rs.getTimestamp("created_at") != null) {
            doc.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            doc.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        return doc;
    }
}