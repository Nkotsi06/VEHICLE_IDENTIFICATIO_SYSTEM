package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import database.ProcedureCaller;
import database.ViewLoader;
import models.VehicleStatus;

/**
 * VehicleStatusDAO - Uses ONLY stored procedures and views for all operations.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class VehicleStatusDAO extends BaseDAO<VehicleStatus> {

    private final ProcedureCaller procedureCaller;
    private final ViewLoader viewLoader;

    public VehicleStatusDAO() {
        this.procedureCaller = new ProcedureCaller();
        this.viewLoader = new ViewLoader();
    }

    @Override
    public VehicleStatus findById(int id) throws SQLException {
        List<VehicleStatus> results = viewLoader.loadViewWithCondition("vw_vehicle_status", "id = ?", id);
        return results.isEmpty() ? null : results.get(0);
    }

    public VehicleStatus findByStatusName(String statusName) throws SQLException {
        List<VehicleStatus> results = viewLoader.loadViewWithCondition("vw_vehicle_status", "status_name = ?", statusName);
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public List<VehicleStatus> findAll() throws SQLException {
        return viewLoader.loadView("vw_vehicle_status");
    }

    @Override
    public boolean insert(VehicleStatus entity) throws SQLException {
        return procedureCaller.executeInsertVehicleStatus(
                entity.getStatusName(),
                entity.getDescription(),
                entity.getColorCode()
        );
    }

    @Override
    public boolean update(VehicleStatus entity) throws SQLException {
        return procedureCaller.executeUpdateVehicleStatus(
                entity.getId(),
                entity.getStatusName(),
                entity.getDescription(),
                entity.getColorCode()
        );
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return procedureCaller.executeDeleteVehicleStatus(id);
    }

    @Override
    protected VehicleStatus mapRow(ResultSet rs) throws SQLException {
        VehicleStatus status = new VehicleStatus();
        status.setId(rs.getInt("id"));
        status.setStatusName(rs.getString("status_name"));
        status.setDescription(rs.getString("description"));
        status.setColorCode(rs.getString("color_code"));
        if (rs.getTimestamp("created_at") != null) {
            status.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            status.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        return status;
    }
}