package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_insurance_providers", "id = ?", id);
        if (results.isEmpty()) {
            return null;
        }
        return mapMapToInsuranceProvider(results.get(0));
    }

    public InsuranceProvider findByUserId(int userId) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_insurance_providers", "user_id = ?", userId);
        if (results.isEmpty()) {
            return null;
        }
        return mapMapToInsuranceProvider(results.get(0));
    }

    public InsuranceProvider findByName(String name) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_insurance_providers", "name = ?", name);
        if (results.isEmpty()) {
            return null;
        }
        return mapMapToInsuranceProvider(results.get(0));
    }

    public List<InsuranceProvider> findByNamePattern(String namePattern) throws SQLException {
        String pattern = "%" + namePattern + "%";
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_insurance_providers", "name ILIKE ? ORDER BY name", pattern);
        return mapMapsToInsuranceProviders(results);
    }

    @Override
    public List<InsuranceProvider> findAll() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadView("vw_insurance_providers");
        return mapMapsToInsuranceProviders(results);
    }

    public List<InsuranceProvider> findActiveProviders() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_insurance_providers", "status = 'ACTIVE' ORDER BY name");
        return mapMapsToInsuranceProviders(results);
    }

    public List<InsuranceProvider> findByRating(double minRating) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_insurance_providers", "rating >= ? ORDER BY rating DESC", minRating);
        return mapMapsToInsuranceProviders(results);
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

    /**
     * Converts a List of Maps to a List of InsuranceProvider objects.
     *
     * @param maps the list of maps from the view loader
     * @return list of InsuranceProvider objects
     */
    private List<InsuranceProvider> mapMapsToInsuranceProviders(List<Map<String, Object>> maps) {
        List<InsuranceProvider> providers = new ArrayList<>();
        if (maps == null) {
            return providers;
        }
        for (Map<String, Object> map : maps) {
            InsuranceProvider provider = mapMapToInsuranceProvider(map);
            if (provider != null) {
                providers.add(provider);
            }
        }
        return providers;
    }

    /**
     * Converts a Map to an InsuranceProvider object.
     *
     * @param map the map from the view loader
     * @return InsuranceProvider object
     */
    private InsuranceProvider mapMapToInsuranceProvider(Map<String, Object> map) {
        if (map == null) {
            return null;
        }

        InsuranceProvider provider = new InsuranceProvider();

        provider.setId(getIntValue(map, "id"));
        provider.setUserId(getIntValue(map, "user_id"));
        provider.setName(getStringValue(map, "name"));
        provider.setRegistrationNumber(getStringValue(map, "registration_number"));
        provider.setLicenseNumber(getStringValue(map, "license_number"));
        provider.setContactPhone(getStringValue(map, "contact_phone"));
        provider.setContactEmail(getStringValue(map, "contact_email"));
        provider.setAddress(getStringValue(map, "address"));
        provider.setRating(getDoubleValue(map, "rating"));
        provider.setCoverageDetails(getStringValue(map, "coverage_details"));
        provider.setStatus(getStringValue(map, "status"));

        provider.setCreatedAt(getLocalDateTimeValue(map, "created_at"));
        provider.setUpdatedAt(getLocalDateTimeValue(map, "updated_at"));

        return provider;
    }

    /**
     * Helper method to safely get Integer values from Map.
     */
    private Integer getIntValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return 0;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Long) {
            return ((Long) value).intValue();
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return 0;
    }

    /**
     * Helper method to safely get Double values from Map.
     */
    private Double getDoubleValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return 0.0;
        }
        if (value instanceof Double) {
            return (Double) value;
        }
        if (value instanceof Integer) {
            return ((Integer) value).doubleValue();
        }
        if (value instanceof Long) {
            return ((Long) value).doubleValue();
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return 0.0;
    }

    /**
     * Helper method to safely get String values from Map.
     */
    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "";
    }

    /**
     * Helper method to safely get LocalDateTime values from Map.
     */
    private LocalDateTime getLocalDateTimeValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof java.sql.Timestamp) {
            return ((java.sql.Timestamp) value).toLocalDateTime();
        }
        if (value instanceof LocalDateTime) {
            return (LocalDateTime) value;
        }
        return null;
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