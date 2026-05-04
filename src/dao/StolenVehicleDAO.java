package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import database.ProcedureCaller;
import database.ViewLoader;
import models.StolenVehicle;

/**
 * StolenVehicleDAO - Uses ONLY stored procedures and views for all operations.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class StolenVehicleDAO extends BaseDAO<StolenVehicle> {

    private final ProcedureCaller procedureCaller;
    private final ViewLoader viewLoader;

    public StolenVehicleDAO() {
        this.procedureCaller = new ProcedureCaller();
        this.viewLoader = new ViewLoader();
    }

    @Override
    public StolenVehicle findById(int id) throws SQLException {
        List<StolenVehicle> results = viewLoader.loadViewWithCondition("vw_stolen_vehicles", "id = ?", id);
        return results.isEmpty() ? null : results.get(0);
    }

    public StolenVehicle findByCaseNumber(String caseNumber) throws SQLException {
        List<StolenVehicle> results = viewLoader.loadViewWithCondition("vw_stolen_vehicles", "case_number = ?", caseNumber);
        return results.isEmpty() ? null : results.get(0);
    }

    public StolenVehicle findByVehicleId(int vehicleId) throws SQLException {
        List<StolenVehicle> results = viewLoader.loadViewWithCondition("vw_stolen_vehicles", "vehicle_id = ? AND status = 'ACTIVE'", vehicleId);
        return results.isEmpty() ? null : results.get(0);
    }

    public StolenVehicle findActiveByVehicleId(int vehicleId) throws SQLException {
        return findByVehicleId(vehicleId);
    }

    @Override
    public List<StolenVehicle> findAll() throws SQLException {
        return viewLoader.loadView("vw_stolen_vehicles");
    }

    public List<StolenVehicle> findActiveStolenVehicles() throws SQLException {
        return viewLoader.loadView("vw_active_stolen_vehicles");
    }

    public List<StolenVehicle> findRecoveredVehicles() throws SQLException {
        return viewLoader.loadViewWithCondition("vw_stolen_vehicles", "status = 'RECOVERED' ORDER BY recovered_date DESC");
    }

    public List<StolenVehicle> findByOfficer(String officerName) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_stolen_vehicles", "assigned_officer ILIKE ? ORDER BY reported_date DESC", "%" + officerName + "%");
    }

    public List<StolenVehicle> findByRegistrationNumber(String registrationNumber) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_stolen_vehicles", "registration_number ILIKE ? ORDER BY reported_date DESC", "%" + registrationNumber + "%");
    }

    public List<StolenVehicle> findStolenBetween(LocalDate startDate, LocalDate endDate) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_stolen_vehicles", "reported_date BETWEEN ? AND ? ORDER BY reported_date DESC", startDate, endDate);
    }

    public List<StolenVehicle> findNearbyStolen(Double latitude, Double longitude, double radiusKm) throws SQLException {
        if (latitude == null || longitude == null) {
            return new ArrayList<>();
        }
        return procedureCaller.executeFindNearbyStolenVehicles(latitude, longitude, radiusKm);
    }

    public boolean insertStolenVehicle(int vehicleId, String caseNumber, String officerName, String badgeNumber, double latitude, double longitude, String description) throws SQLException {
        Integer stolenId = procedureCaller.executeReportStolenVehicle(vehicleId, caseNumber, officerName, badgeNumber, latitude, longitude, description);
        return stolenId != null && stolenId > 0;
    }

    @Override
    public boolean insert(StolenVehicle entity) throws SQLException {
        Integer stolenId = procedureCaller.executeReportStolenVehicle(
                entity.getVehicleId(),
                entity.getCaseNumber(),
                entity.getAssignedOfficer(),
                "",
                entity.getLatitude(),
                entity.getLongitude(),
                entity.getDescription()
        );
        if (stolenId != null && stolenId > 0) {
            entity.setId(stolenId);
            return true;
        }
        return false;
    }

    public boolean updateStatus(int stolenVehicleId, String status) throws SQLException {
        return procedureCaller.executeUpdateStolenStatus(stolenVehicleId, status);
    }

    public boolean recoverVehicle(int stolenVehicleId, LocalDate recoveredDate) throws SQLException {
        return procedureCaller.executeRecoverStolenVehicle(stolenVehicleId, recoveredDate);
    }

    @Override
    public boolean update(StolenVehicle entity) throws SQLException {
        return updateStatus(entity.getId(), entity.getStatus());
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return procedureCaller.executeDeleteStolenVehicle(id);
    }

    public int countActiveStolen() throws SQLException {
        return viewLoader.countViewRowsWithCondition("vw_stolen_vehicles", "status = 'ACTIVE'");
    }

    @Override
    protected StolenVehicle mapRow(ResultSet rs) throws SQLException {
        StolenVehicle stolen = new StolenVehicle();
        stolen.setId(rs.getInt("id"));
        stolen.setVehicleId(rs.getInt("vehicle_id"));
        stolen.setRegistrationNumber(rs.getString("registration_number"));
        stolen.setMake(rs.getString("make"));
        stolen.setModel(rs.getString("model"));

        if (rs.getDate("reported_date") != null) {
            stolen.setReportedDate(rs.getDate("reported_date").toLocalDate());
        }
        stolen.setCaseNumber(rs.getString("case_number"));
        stolen.setStatus(rs.getString("status"));
        stolen.setAssignedOfficer(rs.getString("assigned_officer"));
        if (rs.getDate("recovered_date") != null) {
            stolen.setRecoveredDate(rs.getDate("recovered_date").toLocalDate());
        }
        stolen.setDescription(rs.getString("description"));

        if (rs.getTimestamp("created_at") != null) {
            stolen.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            stolen.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        return stolen;
    }
}