package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_parts_inventory", "part_id = ?", id);
        if (results.isEmpty()) {
            return null;
        }
        return mapMapToPartInventory(results.get(0));
    }

    public PartInventory findByPartNumber(String partNumber) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_parts_inventory", "part_number = ?", partNumber);
        if (results.isEmpty()) {
            return null;
        }
        return mapMapToPartInventory(results.get(0));
    }

    @Override
    public List<PartInventory> findAll() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadView("vw_parts_inventory");
        return mapMapsToPartInventories(results);
    }

    public List<PartInventory> findByWorkshopId(int workshopId) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_parts_inventory", "workshop_id = ? ORDER BY part_name", workshopId);
        return mapMapsToPartInventories(results);
    }

    public List<PartInventory> findLowStockItems() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_parts_inventory", "stock_status IN ('LOW_STOCK', 'OUT_OF_STOCK') ORDER BY quantity");
        return mapMapsToPartInventories(results);
    }

    public List<PartInventory> findOutOfStockItems() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_parts_inventory", "stock_status = 'OUT_OF_STOCK'");
        return mapMapsToPartInventories(results);
    }

    public List<PartInventory> findLowStockByWorkshopId(int workshopId) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_parts_inventory", "workshop_id = ? AND stock_status IN ('LOW_STOCK', 'OUT_OF_STOCK')", workshopId);
        return mapMapsToPartInventories(results);
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

    /**
     * Inserts a part inventory and returns the generated ID.
     *
     * @param entity the PartInventory entity to insert
     * @return the generated part ID, or -1 if insertion failed
     * @throws SQLException if database error occurs
     */
    public int insertAndGetId(PartInventory entity) throws SQLException {
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
            return partId;
        }
        return -1;
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

    /**
     * Converts a List of Maps to a List of PartInventory objects.
     */
    private List<PartInventory> mapMapsToPartInventories(List<Map<String, Object>> maps) {
        List<PartInventory> parts = new ArrayList<>();
        if (maps == null) {
            return parts;
        }
        for (Map<String, Object> map : maps) {
            PartInventory part = mapMapToPartInventory(map);
            if (part != null) {
                parts.add(part);
            }
        }
        return parts;
    }

    /**
     * Converts a Map to a PartInventory object.
     */
    private PartInventory mapMapToPartInventory(Map<String, Object> map) {
        if (map == null) {
            return null;
        }

        PartInventory part = new PartInventory();

        part.setId(getIntValue(map, "part_id"));
        part.setWorkshopId(getIntValue(map, "workshop_id"));
        part.setWorkshopName(getStringValue(map, "workshop_name"));
        part.setPartName(getStringValue(map, "part_name"));
        part.setPartNumber(getStringValue(map, "part_number"));
        part.setQuantity(getIntValue(map, "quantity"));
        part.setReorderLevel(getIntValue(map, "reorder_level"));
        part.setUnitPrice(getDoubleValue(map, "unit_price"));
        part.setStockStatus(getStringValue(map, "stock_status"));

        part.setCreatedAt(getLocalDateTimeValue(map, "created_at"));
        part.setUpdatedAt(getLocalDateTimeValue(map, "updated_at"));

        return part;
    }

    private Integer getIntValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return 0;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Long) return ((Long) value).intValue();
        if (value instanceof Number) return ((Number) value).intValue();
        return 0;
    }

    private Double getDoubleValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return 0.0;
        if (value instanceof Double) return (Double) value;
        if (value instanceof Integer) return ((Integer) value).doubleValue();
        if (value instanceof Long) return ((Long) value).doubleValue();
        if (value instanceof Number) return ((Number) value).doubleValue();
        return 0.0;
    }

    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "";
    }

    private LocalDateTime getLocalDateTimeValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof java.sql.Timestamp) return ((java.sql.Timestamp) value).toLocalDateTime();
        if (value instanceof LocalDateTime) return (LocalDateTime) value;
        return null;
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