package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import database.ProcedureCaller;
import database.ViewLoader;
import models.InsuranceProvider;

/**
 * InsuranceProviderDAO - Uses ONLY stored procedures and views for all operations.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class InsuranceProviderDAO extends BaseDAO<InsuranceProvider> {

    private final ProcedureCaller procedureCaller;
    private final ViewLoader viewLoader;

    public InsuranceProviderDAO() {
        this.procedureCaller = new ProcedureCaller();
        this.viewLoader = new ViewLoader();
    }

    @Override
    public InsuranceProvider findById(int id) throws SQLException {
        List<InsuranceProvider> results = viewLoader.loadViewWithCondition("vw_insurance_providers", "id = ?", id);
        return results.isEmpty() ? null : results.get(0);
    }

    public InsuranceProvider findByUserId(int userId) throws SQLException {
        List<InsuranceProvider> results = viewLoader.loadViewWithCondition("vw_insurance_providers", "user_id = ?", userId);
        return results.isEmpty() ? null : results.get(0);
    }

    public InsuranceProvider findByName(String name) throws SQLException {
        List<InsuranceProvider> results = viewLoader.loadViewWithCondition("vw_insurance_providers", "name = ?", name);
        return results.isEmpty() ? null : results.get(0);
    }

    public List<InsuranceProvider> findByNamePattern(String namePattern) throws SQLException {
        String pattern = "%" + namePattern + "%";
        return viewLoader.loadViewWithCondition("vw_insurance_providers", "name ILIKE ? ORDER BY name", pattern);
    }

    @Override
    public List<InsuranceProvider> findAll() throws SQLException {
        return viewLoader.loadView("vw_insurance_providers");
    }

    public List<InsuranceProvider> findActiveProviders() throws SQLException {
        return viewLoader.loadViewWithCondition("vw_insurance_providers", "status = 'ACTIVE' ORDER BY name");
    }

    public List<InsuranceProvider> findByRating(double minRating) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_insurance_providers", "rating >= ? ORDER BY rating DESC", minRating);
    }

    @Override
    public boolean insert(InsuranceProvider entity) throws SQLException {
        return procedureCaller.executeInsertInsuranceProvider(
                entity.getUserId(),
                entity.getName(),
                entity.getRegistrationNumber(),
                entity.getLicenseNumber(),
                entity.getContactPhone(),
                entity.getContactEmail(),
                entity.getAddress(),
                entity.getRating(),
                entity.getCoverageDetails(),
                entity.getStatus()
        );
    }

    @Override
    public boolean update(InsuranceProvider entity) throws SQLException {
        return procedureCaller.executeUpdateInsuranceProvider(
                entity.getId(),
                entity.getName(),
                entity.getRegistrationNumber(),
                entity.getLicenseNumber(),
                entity.getContactPhone(),
                entity.getContactEmail(),
                entity.getAddress(),
                entity.getRating(),
                entity.getCoverageDetails(),
                entity.getStatus()
        );
    }

    public boolean updateStatus(int providerId, String status) throws SQLException {
        return procedureCaller.executeUpdateInsuranceProviderStatus(providerId, status);
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return procedureCaller.executeDeleteInsuranceProvider(id);
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