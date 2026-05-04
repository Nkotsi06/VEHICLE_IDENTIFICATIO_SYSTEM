package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import database.ProcedureCaller;
import database.ViewLoader;
import models.InspectionChecklistItem;

/**
 * InspectionChecklistItemDAO - Uses ONLY stored procedures and views for all operations.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class InspectionChecklistItemDAO extends BaseDAO<InspectionChecklistItem> {

    private final ProcedureCaller procedureCaller;
    private final ViewLoader viewLoader;

    public InspectionChecklistItemDAO() {
        this.procedureCaller = new ProcedureCaller();
        this.viewLoader = new ViewLoader();
    }

    @Override
    public InspectionChecklistItem findById(int id) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_inspection_checklist_items", "id = ?", id);
        return results.isEmpty() ? null : mapToInspectionChecklistItem(results.get(0));
    }

    @Override
    public List<InspectionChecklistItem> findAll() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadView("vw_inspection_checklist_items");
        return mapToInspectionChecklistItemList(results);
    }

    public List<InspectionChecklistItem> findByInspectionId(int inspectionId) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_inspection_checklist_items", "inspection_id = ? ORDER BY id", inspectionId);
        return mapToInspectionChecklistItemList(results);
    }

    public List<InspectionChecklistItem> findByStatus(String status) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_inspection_checklist_items", "status = ?", status);
        return mapToInspectionChecklistItemList(results);
    }

    public List<InspectionChecklistItem> findFailedItemsByInspection(int inspectionId) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_inspection_checklist_items", "inspection_id = ? AND status = 'FAIL'", inspectionId);
        return mapToInspectionChecklistItemList(results);
    }

    @Override
    public boolean insert(InspectionChecklistItem entity) throws SQLException {
        return procedureCaller.executeAddInspectionChecklistItem(
                entity.getInspectionId(),
                entity.getItemName(),
                entity.getStatus(),
                entity.getNotes()
        );
    }

    public int insertAndGetId(InspectionChecklistItem entity) throws SQLException {
        Integer itemId = procedureCaller.executeAddInspectionChecklistItemWithId(
                entity.getInspectionId(),
                entity.getItemName(),
                entity.getStatus(),
                entity.getNotes(),
                entity.getPhotoPath()
        );
        if (itemId != null && itemId > 0) {
            entity.setId(itemId);
            return itemId;
        }
        return -1;
    }

    @Override
    public boolean update(InspectionChecklistItem entity) throws SQLException {
        return procedureCaller.executeUpdateInspectionChecklistItem(
                entity.getId(),
                entity.getStatus(),
                entity.getNotes(),
                entity.getPhotoPath()
        );
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return procedureCaller.executeDeleteInspectionChecklistItem(id);
    }

    public boolean deleteByInspectionId(int inspectionId) throws SQLException {
        return procedureCaller.executeDeleteInspectionChecklistItemsByInspection(inspectionId);
    }

    // ============================================
    // HELPER METHODS FOR MAPPING
    // ============================================

    /**
     * Converts a Map to an InspectionChecklistItem object.
     *
     * @param map The map containing the data
     * @return InspectionChecklistItem object
     */
    private InspectionChecklistItem mapToInspectionChecklistItem(Map<String, Object> map) {
        if (map == null) return null;

        InspectionChecklistItem item = new InspectionChecklistItem();

        if (map.get("id") != null) item.setId(((Number) map.get("id")).intValue());
        if (map.get("inspection_id") != null) item.setInspectionId(((Number) map.get("inspection_id")).intValue());
        if (map.get("item_name") != null) item.setItemName(map.get("item_name").toString());
        if (map.get("status") != null) item.setStatus(map.get("status").toString());
        if (map.get("notes") != null) item.setNotes(map.get("notes").toString());
        if (map.get("photo_path") != null) item.setPhotoPath(map.get("photo_path").toString());

        if (map.get("created_at") instanceof java.sql.Timestamp) {
            item.setCreatedAt(((java.sql.Timestamp) map.get("created_at")).toLocalDateTime());
        }
        if (map.get("updated_at") instanceof java.sql.Timestamp) {
            item.setUpdatedAt(((java.sql.Timestamp) map.get("updated_at")).toLocalDateTime());
        }

        // Update JavaFX properties
        item.itemNameProperty().set(item.getItemName());
        item.statusProperty().set(item.getStatus());
        item.notesProperty().set(item.getNotes());

        return item;
    }

    /**
     * Converts a list of Maps to a list of InspectionChecklistItem objects.
     *
     * @param maps List of maps containing the data
     * @return List of InspectionChecklistItem objects
     */
    private List<InspectionChecklistItem> mapToInspectionChecklistItemList(List<Map<String, Object>> maps) {
        List<InspectionChecklistItem> items = new ArrayList<>();
        if (maps != null) {
            for (Map<String, Object> map : maps) {
                items.add(mapToInspectionChecklistItem(map));
            }
        }
        return items;
    }

    @Override
    protected InspectionChecklistItem mapRow(ResultSet rs) throws SQLException {
        InspectionChecklistItem item = new InspectionChecklistItem();
        item.setId(rs.getInt("id"));
        item.setInspectionId(rs.getInt("inspection_id"));
        item.setItemName(rs.getString("item_name"));
        item.setStatus(rs.getString("status"));
        item.setNotes(rs.getString("notes"));
        item.setPhotoPath(rs.getString("photo_path"));

        if (rs.getTimestamp("created_at") != null) {
            item.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            item.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        return item;
    }
}