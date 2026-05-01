package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import models.VehicleStatus;

public class VehicleStatusDAO extends BaseDAO<VehicleStatus> {

    @Override
    public VehicleStatus findById(int id) throws SQLException {
        String sql = "SELECT * FROM vw_vehicle_status WHERE id = ?";
        return executeQuerySingle(sql, id);
    }

    public VehicleStatus findByStatusName(String statusName) throws SQLException {
        String sql = "SELECT * FROM vw_vehicle_status WHERE status_name = ?";
        return executeQuerySingle(sql, statusName);
    }

    @Override
    public List<VehicleStatus> findAll() throws SQLException {
        String sql = "SELECT * FROM vw_vehicle_status ORDER BY id";
        return executeQuery(sql);
    }

    @Override
    public boolean insert(VehicleStatus entity) throws SQLException {
        String sql = "INSERT INTO vehicle_status (status_name, description, color_code) VALUES (?, ?, ?)";
        int result = executeUpdate(sql, entity.getStatusName(), entity.getDescription(), entity.getColorCode());
        return result > 0;
    }

    @Override
    public boolean update(VehicleStatus entity) throws SQLException {
        String sql = "UPDATE vehicle_status SET status_name = ?, description = ?, color_code = ? WHERE id = ?";
        int result = executeUpdate(sql, entity.getStatusName(), entity.getDescription(), entity.getColorCode(), entity.getId());
        return result > 0;
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM vehicle_status WHERE id = ?";
        int result = executeUpdate(sql, id);
        return result > 0;
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