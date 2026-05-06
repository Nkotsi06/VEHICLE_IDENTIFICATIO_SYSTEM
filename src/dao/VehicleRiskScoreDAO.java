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
import models.VehicleRiskScore;

/**
 * VehicleRiskScoreDAO - Uses ONLY stored procedures and views for all operations.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class VehicleRiskScoreDAO extends BaseDAO<VehicleRiskScore> {

    private final ProcedureCaller procedureCaller;
    private final ViewLoader viewLoader;

    public VehicleRiskScoreDAO() {
        this.procedureCaller = new ProcedureCaller();
        this.viewLoader = new ViewLoader();
    }

    @Override
    public VehicleRiskScore findById(int id) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_vehicle_risk_score", "id = ?", id);
        if (results.isEmpty()) {
            return null;
        }
        return mapMapToVehicleRiskScore(results.get(0));
    }

    public VehicleRiskScore findByVehicleId(int vehicleId) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_vehicle_risk_score", "vehicle_id = ?", vehicleId);
        if (results.isEmpty()) {
            return null;
        }
        return mapMapToVehicleRiskScore(results.get(0));
    }

    @Override
    public List<VehicleRiskScore> findAll() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadView("vw_vehicle_risk_score");
        return mapMapsToVehicleRiskScores(results);
    }

    public List<VehicleRiskScore> findHighRiskVehicles() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_vehicle_risk_score", "risk_level IN ('HIGH', 'CRITICAL') ORDER BY risk_score DESC");
        return mapMapsToVehicleRiskScores(results);
    }

    public void calculateRiskScore(int vehicleId) throws SQLException {
        procedureCaller.executeCalculateVehicleRiskScoreForVehicle(vehicleId);
    }

    public void calculateAllRiskScores() throws SQLException {
        procedureCaller.executeCalculateAllVehicleRiskScores();
    }

    @Override
    public boolean insert(VehicleRiskScore entity) throws SQLException {
        return procedureCaller.executeInsertVehicleRiskScore(
                entity.getVehicleId(),
                entity.getRiskScore(),
                entity.getRiskFactors(),
                entity.getLastCalculationDate()
        );
    }

    @Override
    public boolean update(VehicleRiskScore entity) throws SQLException {
        return procedureCaller.executeUpdateVehicleRiskScore(
                entity.getVehicleId(),
                entity.getRiskScore(),
                entity.getRiskFactors(),
                entity.getLastCalculationDate()
        );
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return procedureCaller.executeDeleteVehicleRiskScore(id);
    }

    public boolean deleteByVehicleId(int vehicleId) throws SQLException {
        return procedureCaller.executeDeleteVehicleRiskScoreByVehicle(vehicleId);
    }

    /**
     * Converts a List of Maps to a List of VehicleRiskScore objects.
     *
     * @param maps the list of maps from the view loader
     * @return list of VehicleRiskScore objects
     */
    private List<VehicleRiskScore> mapMapsToVehicleRiskScores(List<Map<String, Object>> maps) {
        List<VehicleRiskScore> scores = new ArrayList<>();
        if (maps == null) {
            return scores;
        }
        for (Map<String, Object> map : maps) {
            VehicleRiskScore score = mapMapToVehicleRiskScore(map);
            if (score != null) {
                scores.add(score);
            }
        }
        return scores;
    }

    /**
     * Converts a Map to a VehicleRiskScore object.
     *
     * @param map the map from the view loader
     * @return VehicleRiskScore object
     */
    private VehicleRiskScore mapMapToVehicleRiskScore(Map<String, Object> map) {
        if (map == null) {
            return null;
        }

        VehicleRiskScore score = new VehicleRiskScore();

        score.setId(getIntValue(map, "id"));
        score.setVehicleId(getIntValue(map, "vehicle_id"));
        score.setRegistrationNumber(getStringValue(map, "registration_number"));
        score.setMake(getStringValue(map, "make"));
        score.setModel(getStringValue(map, "model"));
        score.setRiskScore(getDoubleValue(map, "risk_score"));
        score.setRiskFactors(getStringValue(map, "risk_factors"));
        score.setRiskLevel(getStringValue(map, "risk_level"));

        score.setLastCalculationDate(getLocalDateValue(map, "last_calculation_date"));
        score.setCreatedAt(getLocalDateTimeValue(map, "created_at"));
        score.setUpdatedAt(getLocalDateTimeValue(map, "updated_at"));

        return score;
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
    protected VehicleRiskScore mapRow(ResultSet rs) throws SQLException {
        VehicleRiskScore score = new VehicleRiskScore();
        score.setId(rs.getInt("id"));
        score.setVehicleId(rs.getInt("vehicle_id"));
        score.setRegistrationNumber(rs.getString("registration_number"));
        score.setMake(rs.getString("make"));
        score.setModel(rs.getString("model"));
        score.setRiskScore(rs.getDouble("risk_score"));
        score.setRiskFactors(rs.getString("risk_factors"));

        String riskLevel = rs.getString("risk_level");
        score.setRiskLevel(riskLevel);

        if (rs.getDate("last_calculation_date") != null) {
            score.setLastCalculationDate(rs.getDate("last_calculation_date").toLocalDate());
        }

        if (rs.getTimestamp("created_at") != null) {
            score.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            score.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        return score;
    }
}