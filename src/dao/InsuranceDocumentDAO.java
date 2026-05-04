package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

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
        List<InsuranceDocument> results = viewLoader.loadViewWithCondition("vw_insurance_documents", "id = ?", id);
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public List<InsuranceDocument> findAll() throws SQLException {
        return viewLoader.loadView("vw_insurance_documents");
    }

    public List<InsuranceDocument> findByInsuranceId(int insuranceId) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_insurance_documents", "insurance_id = ? ORDER BY upload_date DESC", insuranceId);
    }

    public List<InsuranceDocument> findByDocumentType(String documentType) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_insurance_documents", "document_type = ? ORDER BY upload_date DESC", documentType);
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