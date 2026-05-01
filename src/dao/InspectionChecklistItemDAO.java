package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import models.InspectionChecklistItem;

public class InspectionChecklistItemDAO extends BaseDAO<InspectionChecklistItem> {

    @Override
    public InspectionChecklistItem findById(int id) throws SQLException {
        String sql = "SELECT * FROM inspection_checklist_items WHERE id = ?";
        return executeQuerySingle(sql, id);
    }

    @Override
    public List<InspectionChecklistItem> findAll() throws SQLException {
        String sql = "SELECT * FROM inspection_checklist_items ORDER BY inspection_id, id";
        return executeQuery(sql);
    }

    public List<InspectionChecklistItem> findByInspectionId(int inspectionId) throws SQLException {
        String sql = "SELECT * FROM inspection_checklist_items WHERE inspection_id = ? ORDER BY id";
        return executeQuery(sql, inspectionId);
    }

    public List<InspectionChecklistItem> findByStatus(String status) throws SQLException {
        String sql = "SELECT * FROM inspection_checklist_items WHERE status = ?";
        return executeQuery(sql, status);
    }

    public List<InspectionChecklistItem> findFailedItemsByInspection(int inspectionId) throws SQLException {
        String sql = "SELECT * FROM inspection_checklist_items WHERE inspection_id = ? AND status = 'FAIL'";
        return executeQuery(sql, inspectionId);
    }

    @Override
    public boolean insert(InspectionChecklistItem entity) throws SQLException {
        String sql = "CALL sp_add_inspection_item(?, ?, ?, ?)";
        int result = executeUpdate(sql,
                entity.getInspectionId(),
                entity.getItemName(),
                entity.getStatus(),
                entity.getNotes()
        );
        return result >= 0;
    }

    public int insertAndGetId(InspectionChecklistItem entity) throws SQLException {
        String sql = "INSERT INTO inspection_checklist_items (inspection_id, item_name, status, notes, photo_path) VALUES (?, ?, ?, ?, ?)";
        return executeUpdateWithGeneratedKeys(sql,
                entity.getInspectionId(),
                entity.getItemName(),
                entity.getStatus(),
                entity.getNotes(),
                entity.getPhotoPath()
        );
    }

    @Override
    public boolean update(InspectionChecklistItem entity) throws SQLException {
        String sql = "UPDATE inspection_checklist_items SET status = ?, notes = ?, photo_path = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        int result = executeUpdate(sql, entity.getStatus(), entity.getNotes(), entity.getPhotoPath(), entity.getId());
        return result > 0;
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM inspection_checklist_items WHERE id = ?";
        int result = executeUpdate(sql, id);
        return result > 0;
    }

    public boolean deleteByInspectionId(int inspectionId) throws SQLException {
        String sql = "DELETE FROM inspection_checklist_items WHERE inspection_id = ?";
        int result = executeUpdate(sql, inspectionId);
        return result > 0;
    }

    // FIXED: Changed from private to protected to match BaseDAO
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