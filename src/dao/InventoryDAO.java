package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import models.InventoryAlert;
import models.PartInventory;

public class InventoryDAO extends BaseDAO<PartInventory> {

    private PartInventoryDAO partDAO = new PartInventoryDAO();
    private InventoryAlertDAO alertDAO = new InventoryAlertDAO();

    @Override
    public PartInventory findById(int id) throws SQLException {
        return partDAO.findById(id);
    }

    public PartInventory findByPartNumber(String partNumber) throws SQLException {
        return partDAO.findByPartNumber(partNumber);
    }

    @Override
    public List<PartInventory> findAll() throws SQLException {
        return partDAO.findAll();
    }

    public List<PartInventory> findByWorkshopId(int workshopId) throws SQLException {
        return partDAO.findByWorkshopId(workshopId);
    }

    public List<PartInventory> findLowStockItems() throws SQLException {
        return partDAO.findLowStockItems();
    }

    public List<PartInventory> findOutOfStockItems() throws SQLException {
        return partDAO.findOutOfStockItems();
    }

    @Override
    public boolean insert(PartInventory entity) throws SQLException {
        return partDAO.insert(entity);
    }

    public int insertAndGetId(PartInventory entity) throws SQLException {
        String sql = "CALL sp_add_part_to_inventory(?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        java.sql.CallableStatement cs = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            cs = conn.prepareCall("{call sp_add_part_to_inventory(?, ?, ?, ?, ?, ?)}");
            cs.setInt(1, entity.getWorkshopId());
            cs.setString(2, entity.getPartName());
            cs.setString(3, entity.getPartNumber());
            cs.setInt(4, entity.getQuantity());
            cs.setInt(5, entity.getReorderLevel());
            cs.setDouble(6, entity.getUnitPrice());
            cs.execute();

            String querySql = "SELECT id FROM parts_inventory WHERE part_number = ?";
            ps = conn.prepareStatement(querySql);
            ps.setString(1, entity.getPartNumber());
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
            return -1;
        } finally {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
            if (cs != null) cs.close();
            if (conn != null) conn.close();
        }
    }

    public boolean updateQuantity(int partId, int quantityChange) throws SQLException {
        return partDAO.updateQuantity(partId, quantityChange);
    }

    public boolean addStock(int partId, int quantity) throws SQLException {
        return updateQuantity(partId, quantity);
    }

    public boolean removeStock(int partId, int quantity) throws SQLException {
        return updateQuantity(partId, -quantity);
    }

    @Override
    public boolean update(PartInventory entity) throws SQLException {
        return partDAO.update(entity);
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return partDAO.delete(id);
    }

    public List<InventoryAlert> getInventoryAlerts() throws SQLException {
        return alertDAO.findUnresolvedAlerts();
    }

    public List<InventoryAlert> getAlertsByPart(int partId) throws SQLException {
        return alertDAO.findByPartInventoryId(partId);
    }

    public boolean resolveAlert(int alertId) throws SQLException {
        return alertDAO.resolveAlert(alertId);
    }

    public double getTotalInventoryValue(int workshopId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(quantity * unit_price), 0) FROM parts_inventory WHERE workshop_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, workshopId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
            return 0;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    public boolean checkAndGenerateAlerts(int workshopId) throws SQLException {
        List<PartInventory> lowStockItems = findLowStockItems();
        for (PartInventory part : lowStockItems) {
            if (part.getWorkshopId() == workshopId) {
                InventoryAlert alert = new InventoryAlert(part.getId(), "LOW_STOCK",
                        "Part " + part.getPartName() + " is low on stock. Current quantity: " + part.getQuantity());
                alertDAO.insert(alert);
            }
        }
        return true;
    }

    // FIXED: Changed from private to protected with @Override
    @Override
    protected PartInventory mapRow(ResultSet rs) throws SQLException {
        return partDAO.mapRow(rs);
    }
}