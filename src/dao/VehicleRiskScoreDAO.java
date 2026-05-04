package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

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
        List<VehicleRiskScore> results = viewLoader.loadViewWithCondition("vw_vehicle_risk_score", "id = ?", id);
        return results.isEmpty() ? null : results.get(0);
    }

    public VehicleRiskScore findByVehicleId(int vehicleId) throws SQLException {
        List<VehicleRiskScore> results = viewLoader.loadViewWithCondition("vw_vehicle_risk_score", "vehicle_id = ?", vehicleId);
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public List<VehicleRiskScore> findAll() throws SQLException {
        return viewLoader.loadView("vw_vehicle_risk_score");
    }

    public List<VehicleRiskScore> findHighRiskVehicles() throws SQLException {
        return viewLoader.loadViewWithCondition("vw_vehicle_risk_score", "risk_level IN ('HIGH', 'CRITICAL') ORDER BY risk_score DESC");
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