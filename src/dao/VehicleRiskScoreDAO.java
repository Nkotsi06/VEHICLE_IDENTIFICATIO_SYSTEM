package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import models.VehicleRiskScore;

public class VehicleRiskScoreDAO extends BaseDAO<VehicleRiskScore> {

    @Override
    public VehicleRiskScore findById(int id) throws SQLException {
        String sql = "SELECT * FROM vw_vehicle_risk_score WHERE id = ?";
        return executeQuerySingle(sql, id);
    }

    public VehicleRiskScore findByVehicleId(int vehicleId) throws SQLException {
        String sql = "SELECT * FROM vw_vehicle_risk_score WHERE vehicle_id = ?";
        return executeQuerySingle(sql, vehicleId);
    }

    @Override
    public List<VehicleRiskScore> findAll() throws SQLException {
        String sql = "SELECT * FROM vw_vehicle_risk_score ORDER BY risk_score DESC";
        return executeQuery(sql);
    }

    public List<VehicleRiskScore> findHighRiskVehicles() throws SQLException {
        String sql = "SELECT * FROM vw_vehicle_risk_score WHERE risk_level IN ('HIGH', 'CRITICAL') ORDER BY risk_score DESC";
        return executeQuery(sql);
    }

    public void calculateRiskScore(int vehicleId) throws SQLException {
        String sql = "CALL sp_calculate_vehicle_risk_score_for_vehicle(?)";
        executeUpdate(sql, vehicleId);
    }

    public void calculateAllRiskScores() throws SQLException {
        String sql = "CALL sp_calculate_vehicle_risk_score()";
        executeUpdate(sql);
    }

    @Override
    public boolean insert(VehicleRiskScore entity) throws SQLException {
        String sql = "INSERT INTO vehicle_risk_scores (vehicle_id, risk_score, risk_factors, last_calculation_date) VALUES (?, ?, ?, ?)";
        int result = executeUpdate(sql,
                entity.getVehicleId(),
                entity.getRiskScore(),
                entity.getRiskFactors(),
                entity.getLastCalculationDate()
        );
        return result > 0;
    }

    @Override
    public boolean update(VehicleRiskScore entity) throws SQLException {
        String sql = "UPDATE vehicle_risk_scores SET risk_score = ?, risk_factors = ?, last_calculation_date = ? WHERE vehicle_id = ?";
        int result = executeUpdate(sql,
                entity.getRiskScore(),
                entity.getRiskFactors(),
                entity.getLastCalculationDate(),
                entity.getVehicleId()
        );
        return result > 0;
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM vehicle_risk_scores WHERE id = ?";
        int result = executeUpdate(sql, id);
        return result > 0;
    }

    @Override
    protected VehicleRiskScore mapRow(ResultSet rs) throws SQLException {
        VehicleRiskScore score = new VehicleRiskScore();
        score.setId(rs.getInt("id"));
        score.setVehicleId(rs.getInt("vehicle_id"));
        score.setRegistrationNumber(rs.getString("registration_number"));
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