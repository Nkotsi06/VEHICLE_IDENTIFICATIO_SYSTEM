package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import models.PartInventory;

public class PartInventoryDAO extends BaseDAO<PartInventory> {

    @Override
    public PartInventory findById(int id) throws SQLException {
        String sql = "SELECT * FROM vw_part_inventory WHERE part_id = ?";
        return executeQuerySingle(sql, id);
    }

    public PartInventory findByPartNumber(String partNumber) throws SQLException {
        String sql = "SELECT * FROM vw_part_inventory WHERE part_number = ?";
        return executeQuerySingle(sql, partNumber);
    }

    @Override
    public List<PartInventory> findAll() throws SQLException {
        String sql = "SELECT * FROM vw_part_inventory ORDER BY part_name";
        return executeQuery(sql);
    }

    public List<PartInventory> findByWorkshopId(int workshopId) throws SQLException {
        String sql = "SELECT * FROM vw_part_inventory WHERE workshop_id = ? ORDER BY part_name";
        return executeQuery(sql, workshopId);
    }

    public List<PartInventory> findLowStockItems() throws SQLException {
        String sql = "SELECT * FROM vw_part_inventory WHERE stock_status IN ('LOW_STOCK', 'OUT_OF_STOCK') ORDER BY quantity";
        return executeQuery(sql);
    }

    public List<PartInventory> findOutOfStockItems() throws SQLException {
        String sql = "SELECT * FROM vw_part_inventory WHERE stock_status = 'OUT_OF_STOCK'";
        return executeQuery(sql);
    }

    public List<PartInventory> findLowStockByWorkshopId(int workshopId) throws SQLException {
        String sql = "SELECT pi.part_id, pi.workshop_id, pi.part_name, pi.part_number, pi.quantity, pi.reorder_level, pi.unit_price, pi.created_at, pi.updated_at, w.workshop_name " +
                "FROM vw_part_inventory pi " +
                "WHERE pi.workshop_id = ? AND pi.stock_status IN ('LOW_STOCK', 'OUT_OF_STOCK') " +
                "ORDER BY (pi.reorder_level - pi.quantity) DESC";
        return executeQuery(sql, workshopId);
    }

    public int countLowStockByWorkshopId(int workshopId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM parts_inventory WHERE workshop_id = ? AND quantity <= reorder_level";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, workshopId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    @Override
    public boolean insert(PartInventory entity) throws SQLException {
        return executeProcedure("sp_add_part_to_inventory",
                entity.getWorkshopId(),
                entity.getPartName(),
                entity.getPartNumber(),
                entity.getQuantity(),
                entity.getReorderLevel(),
                entity.getUnitPrice()
        );
    }

    public boolean updateQuantity(int partId, int quantityChange) throws SQLException {
        return executeProcedure("sp_update_part_quantity", partId, quantityChange);
    }

    @Override
    public boolean update(PartInventory entity) throws SQLException {
        String sql = "UPDATE parts_inventory SET part_name = ?, quantity = ?, reorder_level = ?, unit_price = ? WHERE id = ?";
        int result = executeUpdate(sql, entity.getPartName(), entity.getQuantity(), entity.getReorderLevel(), entity.getUnitPrice(), entity.getId());
        return result > 0;
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM parts_inventory WHERE id = ?";
        int result = executeUpdate(sql, id);
        return result > 0;
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