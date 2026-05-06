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
import models.CrimeHotspotPrediction;

/**
 * CrimeHotspotPredictionDAO - Uses ONLY stored procedures and views for all operations.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class CrimeHotspotPredictionDAO extends BaseDAO<CrimeHotspotPrediction> {

    private final ProcedureCaller procedureCaller;
    private final ViewLoader viewLoader;

    // View name constant
    private static final String VIEW_NAME = "vw_predictive_crime_hotspots";

    public CrimeHotspotPredictionDAO() {
        this.procedureCaller = new ProcedureCaller();
        this.viewLoader = new ViewLoader();
    }

    @Override
    public CrimeHotspotPrediction findById(int id) throws SQLException {
        // Use view - NO direct SQL
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition(VIEW_NAME, "id = ?", id);
        if (results.isEmpty()) {
            return null;
        }
        return mapMapToCrimeHotspotPrediction(results.get(0));
    }

    @Override
    public List<CrimeHotspotPrediction> findAll() throws SQLException {
        // Use view - NO direct SQL
        List<Map<String, Object>> results = viewLoader.loadView(VIEW_NAME);
        return mapMapsToCrimeHotspotPredictions(results);
    }

    public List<CrimeHotspotPrediction> findByRiskLevel(String riskLevel) throws SQLException {
        // Use view - NO direct SQL
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition(VIEW_NAME, "risk_level = ? ORDER BY probability_score DESC", riskLevel);
        return mapMapsToCrimeHotspotPredictions(results);
    }

    public List<CrimeHotspotPrediction> findByCrimeType(String crimeType) throws SQLException {
        // Use view - NO direct SQL
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition(VIEW_NAME, "crime_type = ? ORDER BY probability_score DESC", crimeType);
        return mapMapsToCrimeHotspotPredictions(results);
    }

    public List<CrimeHotspotPrediction> findHighRiskPredictions() throws SQLException {
        // Use view - NO direct SQL
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition(VIEW_NAME, "risk_level IN ('HIGH', 'CRITICAL') ORDER BY probability_score DESC");
        return mapMapsToCrimeHotspotPredictions(results);
    }

    public void runPrediction() throws SQLException {
        // Use stored procedure - NO direct SQL
        procedureCaller.executeRunCrimeHotspotPrediction();
    }

    @Override
    public boolean insert(CrimeHotspotPrediction entity) throws SQLException {
        // Use stored procedure - NO direct SQL
        return procedureCaller.executeInsertCrimeHotspotPrediction(
                entity.getPredictionDate(),
                entity.getCenterLat(),
                entity.getCenterLng(),
                entity.getRadiusMeters(),
                entity.getCrimeType(),
                entity.getProbabilityScore(),
                entity.getRiskLevel()
        );
    }

    @Override
    public boolean update(CrimeHotspotPrediction entity) throws SQLException {
        // Predictions are typically not updated - they are regenerated
        return false;
    }

    @Override
    public boolean delete(int id) throws SQLException {
        // Use stored procedure - NO direct SQL
        return procedureCaller.executeDeleteCrimeHotspotPrediction(id);
    }

    public boolean deleteOldPredictions(LocalDate beforeDate) throws SQLException {
        // Use stored procedure - NO direct SQL
        return procedureCaller.executeDeleteOldCrimeHotspotPredictions(beforeDate);
    }

    public int getHighRiskCount() throws SQLException {
        // Use view - NO direct SQL
        return viewLoader.countViewRowsWithCondition(VIEW_NAME, "risk_level IN ('HIGH', 'CRITICAL')");
    }

    /**
     * Converts a List of Maps to a List of CrimeHotspotPrediction objects.
     *
     * @param maps the list of maps from the view loader
     * @return list of CrimeHotspotPrediction objects
     */
    private List<CrimeHotspotPrediction> mapMapsToCrimeHotspotPredictions(List<Map<String, Object>> maps) {
        List<CrimeHotspotPrediction> predictions = new ArrayList<>();
        if (maps == null) {
            return predictions;
        }
        for (Map<String, Object> map : maps) {
            CrimeHotspotPrediction prediction = mapMapToCrimeHotspotPrediction(map);
            if (prediction != null) {
                predictions.add(prediction);
            }
        }
        return predictions;
    }

    /**
     * Converts a Map to a CrimeHotspotPrediction object.
     *
     * @param map the map from the view loader
     * @return CrimeHotspotPrediction object
     */
    private CrimeHotspotPrediction mapMapToCrimeHotspotPrediction(Map<String, Object> map) {
        if (map == null) {
            return null;
        }

        CrimeHotspotPrediction prediction = new CrimeHotspotPrediction();

        prediction.setId(getIntValue(map, "id"));
        prediction.setPredictionDate(getLocalDateValue(map, "prediction_date"));
        prediction.setCenterLat(getDoubleValue(map, "center_lat"));
        prediction.setCenterLng(getDoubleValue(map, "center_lng"));
        prediction.setRadiusMeters(getIntValue(map, "radius_meters"));
        prediction.setCrimeType(getStringValue(map, "crime_type"));
        prediction.setProbabilityScore(getDoubleValue(map, "probability_score"));
        prediction.setRiskLevel(getStringValue(map, "risk_level"));
        prediction.setCreatedAt(getLocalDateTimeValue(map, "created_at"));

        return prediction;
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
     * Helper method to safely get LocalDate values from Map.
     */
    private LocalDate getLocalDateValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof java.sql.Date) {
            return ((java.sql.Date) value).toLocalDate();
        }
        if (value instanceof LocalDate) {
            return (LocalDate) value;
        }
        return null;
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
    protected CrimeHotspotPrediction mapRow(ResultSet rs) throws SQLException {
        CrimeHotspotPrediction prediction = new CrimeHotspotPrediction();
        prediction.setId(rs.getInt("id"));

        if (rs.getDate("prediction_date") != null) {
            prediction.setPredictionDate(rs.getDate("prediction_date").toLocalDate());
        }
        prediction.setCenterLat(rs.getDouble("center_lat"));
        prediction.setCenterLng(rs.getDouble("center_lng"));
        prediction.setRadiusMeters(rs.getInt("radius_meters"));
        prediction.setCrimeType(rs.getString("crime_type"));
        prediction.setProbabilityScore(rs.getDouble("probability_score"));
        prediction.setRiskLevel(rs.getString("risk_level"));

        if (rs.getTimestamp("created_at") != null) {
            prediction.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }

        return prediction;
    }
}