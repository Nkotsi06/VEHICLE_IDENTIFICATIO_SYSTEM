package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import database.ProcedureCaller;
import database.ViewLoader;
import models.VehicleHistory;

/**
 * VehicleHistoryDAO - Uses ONLY stored procedures and views for all operations.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class VehicleHistoryDAO extends BaseDAO<VehicleHistory> {

    private final ProcedureCaller procedureCaller;
    private final ViewLoader viewLoader;

    public VehicleHistoryDAO() {
        this.procedureCaller = new ProcedureCaller();
        this.viewLoader = new ViewLoader();
    }

    @Override
    public VehicleHistory findById(int id) throws SQLException {
        List<VehicleHistory> results = viewLoader.loadViewWithCondition("vw_vehicle_history", "id = ?", id);
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public List<VehicleHistory> findAll() throws SQLException {
        return viewLoader.loadView("vw_vehicle_history");
    }

    public List<VehicleHistory> findByVehicleId(int vehicleId) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_vehicle_history", "vehicle_id = ? ORDER BY event_date DESC", vehicleId);
    }

    public List<VehicleHistory> findByVehicleIdAndDateRange(int vehicleId, LocalDate startDate, LocalDate endDate) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_vehicle_history", "vehicle_id = ? AND event_date BETWEEN ? AND ? ORDER BY event_date DESC", vehicleId, startDate, endDate);
    }

    public List<VehicleHistory> findByEventType(String eventType) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_vehicle_history", "event_type = ? ORDER BY event_date DESC", eventType);
    }

    @Override
    public boolean insert(VehicleHistory entity) throws SQLException {
        return procedureCaller.executeInsertVehicleHistory(
                entity.getVehicleId(),
                entity.getEventType(),
                entity.getEventDate(),
                entity.getDescription(),
                entity.getDetails()
        );
    }

    @Override
    public boolean update(VehicleHistory entity) throws SQLException {
        return procedureCaller.executeUpdateVehicleHistory(
                entity.getId(),
                entity.getDescription(),
                entity.getDetails()
        );
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return procedureCaller.executeDeleteVehicleHistory(id);
    }

    public boolean deleteByVehicleId(int vehicleId) throws SQLException {
        return procedureCaller.executeDeleteVehicleHistoryByVehicle(vehicleId);
    }

    @Override
    protected VehicleHistory mapRow(ResultSet rs) throws SQLException {
        VehicleHistory history = new VehicleHistory();
        history.setId(rs.getInt("id"));
        history.setVehicleId(rs.getInt("vehicle_id"));
        history.setRegistrationNumber(rs.getString("registration_number"));
        history.setEventType(rs.getString("event_type"));

        if (rs.getDate("event_date") != null) {
            history.setEventDate(rs.getDate("event_date").toLocalDate());
        }
        history.setDescription(rs.getString("description"));
        history.setDetails(rs.getString("details"));

        if (rs.getTimestamp("created_at") != null) {
            history.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            history.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        return history;
    }
}