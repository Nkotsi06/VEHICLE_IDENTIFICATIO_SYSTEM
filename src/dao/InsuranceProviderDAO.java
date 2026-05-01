package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import models.InsuranceProvider;

public class InsuranceProviderDAO extends BaseDAO<InsuranceProvider> {

    @Override
    public InsuranceProvider findById(int id) throws SQLException {
        String sql = "SELECT * FROM vw_insurance_providers WHERE id = ?";
        return executeQuerySingle(sql, id);
    }

    public InsuranceProvider findByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM vw_insurance_providers WHERE user_id = ?";
        return executeQuerySingle(sql, userId);
    }

    public InsuranceProvider findByName(String name) throws SQLException {
        String sql = "SELECT * FROM vw_insurance_providers WHERE name = ?";
        return executeQuerySingle(sql, name);
    }

    public List<InsuranceProvider> findByNamePattern(String namePattern) throws SQLException {
        String sql = "SELECT * FROM vw_insurance_providers WHERE name ILIKE ? ORDER BY name";
        String searchPattern = "%" + namePattern + "%";
        return executeQuery(sql, searchPattern);
    }

    @Override
    public List<InsuranceProvider> findAll() throws SQLException {
        String sql = "SELECT * FROM vw_insurance_providers ORDER BY name";
        return executeQuery(sql);
    }

    public List<InsuranceProvider> findActiveProviders() throws SQLException {
        String sql = "SELECT * FROM vw_insurance_providers WHERE status = 'ACTIVE' ORDER BY name";
        return executeQuery(sql);
    }

    public List<InsuranceProvider> findByRating(double minRating) throws SQLException {
        String sql = "SELECT * FROM vw_insurance_providers WHERE rating >= ? ORDER BY rating DESC";
        return executeQuery(sql, minRating);
    }

    @Override
    public boolean insert(InsuranceProvider entity) throws SQLException {
        String sql = "INSERT INTO insurance_providers (user_id, name, registration_number, license_number, contact_phone, contact_email, address, rating, coverage_details, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        int result = executeUpdate(sql,
                entity.getUserId() > 0 ? entity.getUserId() : null,
                entity.getName(),
                entity.getRegistrationNumber(),
                entity.getLicenseNumber(),
                entity.getContactPhone(),
                entity.getContactEmail(),
                entity.getAddress(),
                entity.getRating(),
                entity.getCoverageDetails(),
                entity.getStatus() != null ? entity.getStatus() : "ACTIVE"
        );
        return result > 0;
    }

    @Override
    public boolean update(InsuranceProvider entity) throws SQLException {
        String sql = "UPDATE insurance_providers SET name = ?, registration_number = ?, license_number = ?, contact_phone = ?, contact_email = ?, address = ?, rating = ?, coverage_details = ?, status = ? WHERE id = ?";
        int result = executeUpdate(sql,
                entity.getName(),
                entity.getRegistrationNumber(),
                entity.getLicenseNumber(),
                entity.getContactPhone(),
                entity.getContactEmail(),
                entity.getAddress(),
                entity.getRating(),
                entity.getCoverageDetails(),
                entity.getStatus(),
                entity.getId()
        );
        return result > 0;
    }

    public boolean updateStatus(int providerId, String status) throws SQLException {
        String sql = "UPDATE insurance_providers SET status = ? WHERE id = ?";
        int result = executeUpdate(sql, status, providerId);
        return result > 0;
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM insurance_providers WHERE id = ?";
        int result = executeUpdate(sql, id);
        return result > 0;
    }

    @Override
    protected InsuranceProvider mapRow(ResultSet rs) throws SQLException {
        InsuranceProvider provider = new InsuranceProvider();
        provider.setId(rs.getInt("id"));

        try {
            int userId = rs.getInt("user_id");
            if (!rs.wasNull()) {
                provider.setUserId(userId);
            }
        } catch (SQLException e) {}

        provider.setName(rs.getString("name"));
        provider.setRegistrationNumber(rs.getString("registration_number"));
        provider.setLicenseNumber(rs.getString("license_number"));
        provider.setContactPhone(rs.getString("contact_phone"));
        provider.setContactEmail(rs.getString("contact_email"));
        provider.setAddress(rs.getString("address"));

        double rating = rs.getDouble("rating");
        if (!rs.wasNull()) {
            provider.setRating(rating);
        }

        provider.setCoverageDetails(rs.getString("coverage_details"));
        provider.setStatus(rs.getString("status"));

        if (rs.getTimestamp("created_at") != null) {
            provider.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            provider.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }

        return provider;
    }
}