package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import database.ProcedureCaller;
import database.ViewLoader;
import models.PartInventory;

/**
 * PartInventoryDAO - Uses ONLY stored procedures and views for all operations.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class PartInventoryDAO extends BaseDAO<PartInventory> {

    private final ProcedureCaller procedureCaller;
    private final ViewLoader viewLoader;

    public PartInventoryDAO() {
        this.procedureCaller = new ProcedureCaller();
        this.viewLoader = new ViewLoader();
    }

    @Override
    public PartInventory findById(int id) throws SQLException {
        List<PartInventory> results = viewLoader.loadViewWithCondition("vw_parts_inventory", "part_id = ?", id);
        return results.isEmpty() ? null : results.get(0);
    }

    public PartInventory findByPartNumber(String partNumber) throws SQLException {
        List<PartInventory> results = viewLoader.loadViewWithCondition("vw_parts_inventory", "part_number = ?", partNumber);
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public List<PartInventory> findAll() throws SQLException {
        return viewLoader.loadView("vw_parts_inventory");
    }

    public List<PartInventory> findByWorkshopId(int workshopId) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_parts_inventory", "workshop_id = ? ORDER BY part_name", workshopId);
    }

    public List<PartInventory> findLowStockItems() throws SQLException {
        return viewLoader.loadViewWithCondition("vw_parts_inventory", "stock_status IN ('LOW_STOCK', 'OUT_OF_STOCK') ORDER BY quantity");
    }

    public List<PartInventory> findOutOfStockItems() throws SQLException {
        return viewLoader.loadViewWithCondition("vw_parts_inventory", "stock_status = 'OUT_OF_STOCK'");
    }

    public List<PartInventory> findLowStockByWorkshopId(int workshopId) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_parts_inventory", "workshop_id = ? AND stock_status IN ('LOW_STOCK', 'OUT_OF_STOCK')", workshopId);
    }

    public int countLowStockByWorkshopId(int workshopId) throws SQLException {
        return viewLoader.countViewRowsWithCondition("vw_parts_inventory", "workshop_id = ? AND stock_status IN ('LOW_STOCK', 'OUT_OF_STOCK')", workshopId);
    }

    @Override
    public boolean insert(PartInventory entity) throws SQLException {
        Integer partId = procedureCaller.executeAddPartToInventory(
                entity.getWorkshopId(),
                entity.getPartName(),
                entity.getPartNumber(),
                entity.getQuantity(),
                entity.getReorderLevel(),
                entity.getUnitPrice()
        );
        if (partId != null && partId > 0) {
            entity.setId(partId);
            return true;
        }
        return false;
    }

    public boolean updateQuantity(int partId, int quantityChange) throws SQLException {
        return procedureCaller.executeUpdatePartQuantity(partId, quantityChange);
    }

    @Override
    public boolean update(PartInventory entity) throws SQLException {
        return procedureCaller.executeUpdatePartInventory(
                entity.getId(),
                entity.getPartName(),
                entity.getQuantity(),
                entity.getReorderLevel(),
                entity.getUnitPrice()
        );
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return procedureCaller.executeDeletePartInventory(id);
    }

    public double getTotalInventoryValueByWorkshop(int workshopId) throws SQLException {
        return viewLoader.getSumInventoryValueByWorkshop(workshopId);
    }

    @Override
    protected PartInventory mapRow(ResultSet rs) throws SQLException {
        PartInventory part = new PartInventory();
        part.setId(rs.getInt("part_id"));
        part.setWorkshopId(rs.getInt("workshop_id"));
        part.setWorkshopName(rs.getString("workshop_name"));
        part.setPartName(rs.getString("part_name"));
        part.setPartNumber(rs.getString("part_number"));
        part.setQuantity(rs.getInt("quantity"));
        part.setReorderLevel(rs.getInt("reorder_level"));
        part.setUnitPrice(rs.getDouble("unit_price"));
        part.setStockStatus(rs.getString("stock_status"));

        if (rs.getTimestamp("created_at") != null) {
            part.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            part.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        return part;
    }
}