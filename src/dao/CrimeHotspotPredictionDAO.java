package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

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
        List<CrimeHotspotPrediction> results = viewLoader.loadViewWithCondition(VIEW_NAME, "id = ?", id);
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public List<CrimeHotspotPrediction> findAll() throws SQLException {
        // Use view - NO direct SQL
        return viewLoader.loadView(VIEW_NAME);
    }

    public List<CrimeHotspotPrediction> findByRiskLevel(String riskLevel) throws SQLException {
        // Use view - NO direct SQL
        return viewLoader.loadViewWithCondition(VIEW_NAME, "risk_level = ? ORDER BY probability_score DESC", riskLevel);
    }

    public List<CrimeHotspotPrediction> findByCrimeType(String crimeType) throws SQLException {
        // Use view - NO direct SQL
        return viewLoader.loadViewWithCondition(VIEW_NAME, "crime_type = ? ORDER BY probability_score DESC", crimeType);
    }

    public List<CrimeHotspotPrediction> findHighRiskPredictions() throws SQLException {
        // Use view - NO direct SQL
        return viewLoader.loadViewWithCondition(VIEW_NAME, "risk_level IN ('HIGH', 'CRITICAL') ORDER BY probability_score DESC");
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