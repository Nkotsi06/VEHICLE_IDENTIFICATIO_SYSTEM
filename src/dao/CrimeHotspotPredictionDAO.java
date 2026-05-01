package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import models.CrimeHotspotPrediction;

public class CrimeHotspotPredictionDAO extends BaseDAO<CrimeHotspotPrediction> {

    @Override
    public CrimeHotspotPrediction findById(int id) throws SQLException {
        String sql = "SELECT * FROM vw_predictive_crime_hotspots WHERE id = ?";
        return executeQuerySingle(sql, id);
    }

    @Override
    public List<CrimeHotspotPrediction> findAll() throws SQLException {
        String sql = "SELECT * FROM vw_predictive_crime_hotspots ORDER BY probability_score DESC";
        return executeQuery(sql);
    }

    public List<CrimeHotspotPrediction> findByRiskLevel(String riskLevel) throws SQLException {
        String sql = "SELECT * FROM vw_predictive_crime_hotspots WHERE risk_level = ? ORDER BY probability_score DESC";
        return executeQuery(sql, riskLevel);
    }

    public List<CrimeHotspotPrediction> findByCrimeType(String crimeType) throws SQLException {
        String sql = "SELECT * FROM vw_predictive_crime_hotspots WHERE crime_type = ? ORDER BY probability_score DESC";
        return executeQuery(sql, crimeType);
    }

    public List<CrimeHotspotPrediction> findHighRiskPredictions() throws SQLException {
        String sql = "SELECT * FROM vw_predictive_crime_hotspots WHERE risk_level IN ('HIGH', 'CRITICAL') ORDER BY probability_score DESC";
        return executeQuery(sql);
    }

    public void runPrediction() throws SQLException {
        String sql = "CALL sp_predict_crime_hotspots()";
        executeUpdate(sql);
    }

    @Override
    public boolean insert(CrimeHotspotPrediction entity) throws SQLException {
        String sql = "INSERT INTO crime_hotspot_predictions (prediction_date, center_lat, center_lng, radius_meters, crime_type, probability_score, risk_level) VALUES (?, ?, ?, ?, ?, ?, ?)";
        int result = executeUpdate(sql,
                entity.getPredictionDate(),
                entity.getCenterLat(),
                entity.getCenterLng(),
                entity.getRadiusMeters(),
                entity.getCrimeType(),
                entity.getProbabilityScore(),
                entity.getRiskLevel()
        );
        return result > 0;
    }

    @Override
    public boolean update(CrimeHotspotPrediction entity) throws SQLException {
        return false;
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM crime_hotspot_predictions WHERE id = ?";
        int result = executeUpdate(sql, id);
        return result > 0;
    }

    public boolean deleteOldPredictions(LocalDate beforeDate) throws SQLException {
        String sql = "DELETE FROM crime_hotspot_predictions WHERE prediction_date < ?";
        int result = executeUpdate(sql, beforeDate);
        return result > 0;
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