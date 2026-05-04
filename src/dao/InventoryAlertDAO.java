package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import database.ProcedureCaller;
import database.ViewLoader;
import models.InventoryAlert;

/**
 * InventoryAlertDAO - Uses ONLY stored procedures and views for all operations.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class InventoryAlertDAO extends BaseDAO<InventoryAlert> {

    private final ProcedureCaller procedureCaller;
    private final ViewLoader viewLoader;

    public InventoryAlertDAO() {
        this.procedureCaller = new ProcedureCaller();
        this.viewLoader = new ViewLoader();
    }

    @Override
    public InventoryAlert findById(int id) throws SQLException {
        List<InventoryAlert> results = viewLoader.loadViewWithCondition("vw_inventory_alerts", "id = ?", id);
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public List<InventoryAlert> findAll() throws SQLException {
        return viewLoader.loadView("vw_inventory_alerts");
    }

    public List<InventoryAlert> findByPartInventoryId(int partInventoryId) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_inventory_alerts", "part_inventory_id = ? ORDER BY created_at DESC", partInventoryId);
    }

    public List<InventoryAlert> findByWorkshopId(int workshopId) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_inventory_alerts", "workshop_id = ? AND is_resolved = false ORDER BY created_at DESC", workshopId);
    }

    public List<InventoryAlert> findUnresolvedAlerts() throws SQLException {
        return viewLoader.loadViewWithCondition("vw_inventory_alerts", "is_resolved = false ORDER BY created_at");
    }

    public List<InventoryAlert> findByAlertType(String alertType) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_inventory_alerts", "alert_type = ? AND is_resolved = false ORDER BY created_at", alertType);
    }

    public List<InventoryAlert> findResolvedAlerts() throws SQLException {
        return viewLoader.loadViewWithCondition("vw_inventory_alerts", "is_resolved = true ORDER BY resolved_at DESC");
    }

    @Override
    public boolean insert(InventoryAlert entity) throws SQLException {
        Integer alertId = procedureCaller.executeInsertInventoryAlert(
                entity.getPartInventoryId(),
                entity.getAlertType(),
                entity.getMessage()
        );
        if (alertId != null && alertId > 0) {
            entity.setId(alertId);
            return true;
        }
        return false;
    }

    public int insertAndGetId(InventoryAlert entity) throws SQLException {
        return procedureCaller.executeInsertInventoryAlert(
                entity.getPartInventoryId(),
                entity.getAlertType(),
                entity.getMessage()
        );
    }

    public boolean resolveAlert(int alertId) throws SQLException {
        return procedureCaller.executeResolveInventoryAlert(alertId);
    }

    public boolean resolveAlertByPartId(int partInventoryId) throws SQLException {
        return procedureCaller.executeResolveInventoryAlertsByPart(partInventoryId);
    }

    @Override
    public boolean update(InventoryAlert entity) throws SQLException {
        if (entity.isResolved()) {
            return resolveAlert(entity.getId());
        }
        return false;
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return procedureCaller.executeDeleteInventoryAlert(id);
    }

    public boolean deleteResolvedAlerts() throws SQLException {
        return procedureCaller.executeDeleteResolvedInventoryAlerts();
    }

    public int countUnresolvedAlerts() throws SQLException {
        return viewLoader.countViewRowsWithCondition("vw_inventory_alerts", "is_resolved = false");
    }

    public int countUnresolvedAlertsByWorkshop(int workshopId) throws SQLException {
        return viewLoader.countViewRowsWithCondition("vw_inventory_alerts", "workshop_id = ? AND is_resolved = false", workshopId);
    }

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