package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import models.VehicleHistory;

public class VehicleHistoryDAO extends BaseDAO<VehicleHistory> {

    @Override
    public VehicleHistory findById(int id) throws SQLException {
        String sql = "SELECT * FROM vw_vehicle_history WHERE id = ?";
        return executeQuerySingle(sql, id);
    }

    @Override
    public List<VehicleHistory> findAll() throws SQLException {
        String sql = "SELECT * FROM vw_vehicle_history";
        return executeQuery(sql);
    }

    public List<VehicleHistory> findByVehicleId(int vehicleId) throws SQLException {
        String sql = "SELECT * FROM vw_vehicle_history WHERE vehicle_id = ? ORDER BY event_date DESC";
        return executeQuery(sql, vehicleId);
    }

    public List<VehicleHistory> findByVehicleIdAndDateRange(int vehicleId, LocalDate startDate, LocalDate endDate) throws SQLException {
        String sql = "SELECT * FROM vw_vehicle_history WHERE vehicle_id = ? AND event_date BETWEEN ? AND ? ORDER BY event_date DESC";
        return executeQuery(sql, vehicleId, startDate, endDate);
    }

    public List<VehicleHistory> findByEventType(String eventType) throws SQLException {
        String sql = "SELECT * FROM vw_vehicle_history WHERE event_type = ? ORDER BY event_date DESC";
        return executeQuery(sql, eventType);
    }

    @Override
    public boolean insert(VehicleHistory entity) throws SQLException {
        String sql = "INSERT INTO vehicle_history (vehicle_id, event_type, event_date, description, details) VALUES (?, ?, ?, ?, ?)";
        int result = executeUpdate(sql,
                entity.getVehicleId(),
                entity.getEventType(),
                entity.getEventDate(),
                entity.getDescription(),
                entity.getDetails()
        );
        return result > 0;
    }

    @Override
    public boolean update(VehicleHistory entity) throws SQLException {
        String sql = "UPDATE vehicle_history SET description = ?, details = ? WHERE id = ?";
        int result = executeUpdate(sql, entity.getDescription(), entity.getDetails(), entity.getId());
        return result > 0;
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM vehicle_history WHERE id = ?";
        int result = executeUpdate(sql, id);
        return result > 0;
    }

    public boolean deleteByVehicleId(int vehicleId) throws SQLException {
        String sql = "DELETE FROM vehicle_history WHERE vehicle_id = ?";
        int result = executeUpdate(sql, vehicleId);
        return result > 0;
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