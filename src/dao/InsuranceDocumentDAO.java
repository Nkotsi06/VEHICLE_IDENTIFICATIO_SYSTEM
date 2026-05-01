package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import models.InsuranceDocument;

public class InsuranceDocumentDAO extends BaseDAO<InsuranceDocument> {

    @Override
    public InsuranceDocument findById(int id) throws SQLException {
        String sql = "SELECT * FROM insurance_documents WHERE id = ?";
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
    public List<InsuranceDocument> findAll() throws SQLException {
        String sql = "SELECT * FROM insurance_documents ORDER BY upload_date DESC";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<InsuranceDocument> documents = new ArrayList<>();
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                documents.add(mapRow(rs));
            }
            return documents;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    public List<InsuranceDocument> findByInsuranceId(int insuranceId) throws SQLException {
        String sql = "SELECT * FROM insurance_documents WHERE insurance_id = ? ORDER BY upload_date DESC";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<InsuranceDocument> documents = new ArrayList<>();
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, insuranceId);
            rs = ps.executeQuery();
            while (rs.next()) {
                documents.add(mapRow(rs));
            }
            return documents;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    public List<InsuranceDocument> findByDocumentType(String documentType) throws SQLException {
        String sql = "SELECT * FROM insurance_documents WHERE document_type = ? ORDER BY upload_date DESC";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<InsuranceDocument> documents = new ArrayList<>();
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, documentType);
            rs = ps.executeQuery();
            while (rs.next()) {
                documents.add(mapRow(rs));
            }
            return documents;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    @Override
    public boolean insert(InsuranceDocument entity) throws SQLException {
        String sql = "INSERT INTO insurance_documents (insurance_id, file_name, file_path, document_type, file_size) VALUES (?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, entity.getInsuranceId());
            ps.setString(2, entity.getFileName());
            ps.setString(3, entity.getFilePath());
            ps.setString(4, entity.getDocumentType());
            ps.setLong(5, entity.getFileSize());
            int result = ps.executeUpdate();
            return result > 0;
        } finally {
            closeResources(null, ps, conn);
        }
    }

    public int insertAndGetId(InsuranceDocument entity) throws SQLException {
        String sql = "INSERT INTO insurance_documents (insurance_id, file_name, file_path, document_type, file_size) VALUES (?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setInt(1, entity.getInsuranceId());
            ps.setString(2, entity.getFileName());
            ps.setString(3, entity.getFilePath());
            ps.setString(4, entity.getDocumentType());
            ps.setLong(5, entity.getFileSize());
            int affectedRows = ps.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating document failed, no rows affected.");
            }

            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                throw new SQLException("Creating document failed, no ID obtained.");
            }
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    @Override
    public boolean update(InsuranceDocument entity) throws SQLException {
        String sql = "UPDATE insurance_documents SET file_name = ?, file_path = ?, document_type = ?, file_size = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, entity.getFileName());
            ps.setString(2, entity.getFilePath());
            ps.setString(3, entity.getDocumentType());
            ps.setLong(4, entity.getFileSize());
            ps.setInt(5, entity.getId());
            int result = ps.executeUpdate();
            return result > 0;
        } finally {
            closeResources(null, ps, conn);
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM insurance_documents WHERE id = ?";
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

    public boolean deleteByInsuranceId(int insuranceId) throws SQLException {
        String sql = "DELETE FROM insurance_documents WHERE insurance_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, insuranceId);
            int result = ps.executeUpdate();
            return result > 0;
        } finally {
            closeResources(null, ps, conn);
        }
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