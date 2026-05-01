package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import models.InventoryAlert;

public class InventoryAlertDAO extends BaseDAO<InventoryAlert> {

    @Override
    public InventoryAlert findById(int id) throws SQLException {
        String sql = "SELECT ia.*, pi.part_name FROM inventory_alerts ia " +
                "LEFT JOIN parts_inventory pi ON ia.part_inventory_id = pi.id " +
                "WHERE ia.id = ?";
        return executeQuerySingle(sql, id);
    }

    @Override
    public List<InventoryAlert> findAll() throws SQLException {
        String sql = "SELECT ia.*, pi.part_name FROM inventory_alerts ia " +
                "LEFT JOIN parts_inventory pi ON ia.part_inventory_id = pi.id " +
                "ORDER BY ia.created_at DESC";
        return executeQuery(sql);
    }

    public List<InventoryAlert> findByPartInventoryId(int partInventoryId) throws SQLException {
        String sql = "SELECT ia.*, pi.part_name FROM inventory_alerts ia " +
                "LEFT JOIN parts_inventory pi ON ia.part_inventory_id = pi.id " +
                "WHERE ia.part_inventory_id = ? ORDER BY ia.created_at DESC";
        return executeQuery(sql, partInventoryId);
    }

    public List<InventoryAlert> findByWorkshopId(int workshopId) throws SQLException {
        String sql = "SELECT ia.*, pi.part_name FROM inventory_alerts ia " +
                "JOIN parts_inventory pi ON ia.part_inventory_id = pi.id " +
                "WHERE pi.workshop_id = ? AND ia.is_resolved = false " +
                "ORDER BY ia.created_at DESC";
        return executeQuery(sql, workshopId);
    }

    public List<InventoryAlert> findUnresolvedAlerts() throws SQLException {
        String sql = "SELECT ia.*, pi.part_name FROM inventory_alerts ia " +
                "LEFT JOIN parts_inventory pi ON ia.part_inventory_id = pi.id " +
                "WHERE ia.is_resolved = false ORDER BY ia.created_at";
        return executeQuery(sql);
    }

    public List<InventoryAlert> findByAlertType(String alertType) throws SQLException {
        String sql = "SELECT ia.*, pi.part_name FROM inventory_alerts ia " +
                "LEFT JOIN parts_inventory pi ON ia.part_inventory_id = pi.id " +
                "WHERE ia.alert_type = ? AND ia.is_resolved = false ORDER BY ia.created_at";
        return executeQuery(sql, alertType);
    }

    public List<InventoryAlert> findResolvedAlerts() throws SQLException {
        String sql = "SELECT ia.*, pi.part_name FROM inventory_alerts ia " +
                "LEFT JOIN parts_inventory pi ON ia.part_inventory_id = pi.id " +
                "WHERE ia.is_resolved = true ORDER BY ia.resolved_at DESC";
        return executeQuery(sql);
    }

    @Override
    public boolean insert(InventoryAlert entity) throws SQLException {
        String sql = "INSERT INTO inventory_alerts (part_inventory_id, alert_type, message) VALUES (?, ?, ?)";
        int result = executeUpdate(sql,
                entity.getPartInventoryId(),
                entity.getAlertType(),
                entity.getMessage()
        );
        return result > 0;
    }

    public int insertAndGetId(InventoryAlert entity) throws SQLException {
        String sql = "INSERT INTO inventory_alerts (part_inventory_id, alert_type, message) VALUES (?, ?, ?)";
        return executeUpdateWithGeneratedKeys(sql,
                entity.getPartInventoryId(),
                entity.getAlertType(),
                entity.getMessage()
        );
    }

    public boolean resolveAlert(int alertId) throws SQLException {
        String sql = "UPDATE inventory_alerts SET is_resolved = true, resolved_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        int result = executeUpdate(sql, alertId);
        return result > 0;
    }

    public boolean resolveAlertByPartId(int partInventoryId) throws SQLException {
        String sql = "UPDATE inventory_alerts SET is_resolved = true, resolved_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP WHERE part_inventory_id = ? AND is_resolved = false";
        int result = executeUpdate(sql, partInventoryId);
        return result > 0;
    }

    @Override
    public boolean update(InventoryAlert entity) throws SQLException {
        String sql = "UPDATE inventory_alerts SET is_resolved = ?, resolved_at = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        int result = executeUpdate(sql, entity.isResolved(), entity.getResolvedAt(), entity.getId());
        return result > 0;
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM inventory_alerts WHERE id = ?";
        int result = executeUpdate(sql, id);
        return result > 0;
    }

    public boolean deleteResolvedAlerts() throws SQLException {
        String sql = "DELETE FROM inventory_alerts WHERE is_resolved = true";
        int result = executeUpdate(sql);
        return result > 0;
    }

    public int countUnresolvedAlerts() throws SQLException {
        String sql = "SELECT COUNT(*) FROM inventory_alerts WHERE is_resolved = false";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    public int countUnresolvedAlertsByWorkshop(int workshopId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM inventory_alerts ia " +
                "JOIN parts_inventory pi ON ia.part_inventory_id = pi.id " +
                "WHERE pi.workshop_id = ? AND ia.is_resolved = false";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, workshopId);
            rs = rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    // FIXED: Changed from private to protected with @Override
    @Override
    protected InventoryAlert mapRow(ResultSet rs) throws SQLException {
        InventoryAlert alert = new InventoryAlert();
        alert.setId(rs.getInt("id"));
        alert.setPartInventoryId(rs.getInt("part_inventory_id"));
        alert.setPartName(rs.getString("part_name"));
        alert.setAlertType(rs.getString("alert_type"));
        alert.setMessage(rs.getString("message"));
        alert.setResolved(rs.getBoolean("is_resolved"));

        if (rs.getTimestamp("resolved_at") != null) {
            alert.setResolvedAt(rs.getTimestamp("resolved_at").toLocalDateTime());
        }
        if (rs.getTimestamp("created_at") != null) {
            alert.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            alert.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        return alert;
    }
}