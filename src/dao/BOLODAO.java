package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import models.BOLOAlert;

public class BOLODAO extends BaseDAO<BOLOAlert> {

    @Override
    public BOLOAlert findById(int id) throws SQLException {
        String sql = "SELECT * FROM bolo_alerts WHERE id = ?";
        return executeQuerySingle(sql, id);
    }

    @Override
    public List<BOLOAlert> findAll() throws SQLException {
        String sql = "SELECT * FROM bolo_alerts ORDER BY alert_date DESC";
        return executeQuery(sql);
    }

    public List<BOLOAlert> findActiveAlerts() throws SQLException {
        String sql = "SELECT * FROM vw_active_bolo_alerts ORDER BY priority DESC, alert_date DESC";
        return executeQuery(sql);
    }

    public List<BOLOAlert> findByVehicleId(int vehicleId) throws SQLException {
        String sql = "SELECT * FROM bolo_alerts WHERE vehicle_id = ? ORDER BY alert_date DESC";
        return executeQuery(sql, vehicleId);
    }

    @Override
    public boolean insert(BOLOAlert entity) throws SQLException {
        return executeProcedure("sp_generate_bolo_alert",
                entity.getVehicleId(),
                entity.getStolenVehicleId() > 0 ? entity.getStolenVehicleId() : null,
                entity.getMessage(),
                entity.getPriority()
        );
    }

    public boolean cancelAlert(int alertId) throws SQLException {
        String sql = "UPDATE bolo_alerts SET status = 'CANCELLED' WHERE id = ?";
        int result = executeUpdate(sql, alertId);
        return result > 0;
    }

    @Override
    public boolean update(BOLOAlert entity) throws SQLException {
        String sql = "UPDATE bolo_alerts SET status = ? WHERE id = ?";
        int result = executeUpdate(sql, entity.getStatus(), entity.getId());
        return result > 0;
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM bolo_alerts WHERE id = ?";
        int result = executeUpdate(sql, id);
        return result > 0;
    }

    @Override
    protected BOLOAlert mapRow(ResultSet rs) throws SQLException {
        BOLOAlert alert = new BOLOAlert();
        alert.setId(rs.getInt("id"));
        alert.setVehicleId(rs.getInt("vehicle_id"));
        alert.setRegistrationNumber(rs.getString("registration_number"));
        alert.setMake(rs.getString("make"));
        alert.setModel(rs.getString("model"));

        if (rs.getDate("alert_date") != null) {
            alert.setAlertDate(rs.getDate("alert_date").toLocalDate());
        }
        if (rs.getDate("expiry_date") != null) {
            alert.setExpiryDate(rs.getDate("expiry_date").toLocalDate());
        }
        alert.setMessage(rs.getString("message"));
        alert.setPriority(rs.getString("priority"));
        alert.setStatus(rs.getString("status"));

        if (rs.getTimestamp("created_at") != null) {
            alert.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            alert.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        return alert;
    }
}