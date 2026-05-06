package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import database.ProcedureCaller;
import database.ViewLoader;
import models.DigitalInspection;
import models.InspectionChecklistItem;

/**
 * DigitalInspectionDAO - Uses ONLY stored procedures and views for all operations.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class DigitalInspectionDAO extends BaseDAO<DigitalInspection> {

    private final ProcedureCaller procedureCaller;
    private final ViewLoader viewLoader;

    public DigitalInspectionDAO() {
        this.procedureCaller = new ProcedureCaller();
        this.viewLoader = new ViewLoader();
    }

    @Override
    public DigitalInspection findById(int id) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_digital_inspections", "id = ?", id);
        if (results.isEmpty()) return null;

        DigitalInspection inspection = mapToDigitalInspection(results.get(0));
        // Load checklist items using view
        List<Map<String, Object>> itemsResult = viewLoader.loadViewWithCondition("vw_inspection_checklist_items", "inspection_id = ?", id);
        List<InspectionChecklistItem> items = mapToInspectionChecklistItemList(itemsResult);
        inspection.setChecklistItems(items);
        return inspection;
    }

    @Override
    public List<DigitalInspection> findAll() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadView("vw_digital_inspections");
        return mapToDigitalInspectionList(results);
    }

    // ADDED METHOD - Alias for mapToDigitalInspectionList to support InspectionDAO
    public List<DigitalInspection> mapMapsToDigitalInspections(List<Map<String, Object>> results) {
        return mapToDigitalInspectionList(results);
    }

    public List<DigitalInspection> findByVehicleId(int vehicleId) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_digital_inspections", "vehicle_id = ? ORDER BY inspection_date DESC", vehicleId);
        return mapToDigitalInspectionList(results);
    }

    public List<DigitalInspection> findByServiceRecordId(int serviceRecordId) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_digital_inspections", "service_record_id = ?", serviceRecordId);
        return mapToDigitalInspectionList(results);
    }

    public List<DigitalInspection> findByWorkshopId(int workshopId) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_digital_inspections", "workshop_id = ? ORDER BY inspection_date DESC", workshopId);
        return mapToDigitalInspectionList(results);
    }

    public List<DigitalInspection> findByInspector(String inspectorName) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_digital_inspections", "inspector_name ILIKE ? ORDER BY inspection_date DESC", "%" + inspectorName + "%");
        return mapToDigitalInspectionList(results);
    }

    /**
     * Counts the number of completed inspections for a workshop.
     *
     * @param workshopId the workshop ID
     * @return count of completed inspections
     * @throws SQLException if database error occurs
     */
    public int countCompletedByWorkshopId(int workshopId) throws SQLException {
        return viewLoader.countViewRowsWithCondition("vw_digital_inspections",
                "workshop_id = ? AND overall_condition IS NOT NULL", workshopId);
    }

    /**
     * Counts the number of inspections for a workshop (both completed and in-progress).
     *
     * @param workshopId the workshop ID
     * @return total count of inspections
     * @throws SQLException if database error occurs
     */
    public int countTotalByWorkshopId(int workshopId) throws SQLException {
        return viewLoader.countViewRowsWithCondition("vw_digital_inspections", "workshop_id = ?", workshopId);
    }

    @Override
    public boolean insert(DigitalInspection entity) throws SQLException {
        Integer inspectionId = procedureCaller.executeStartDigitalInspection(
                entity.getServiceRecordId(),
                entity.getInspectorName()
        );
        if (inspectionId != null && inspectionId > 0) {
            entity.setId(inspectionId);
            return true;
        }
        return false;
    }

    // ADDED METHOD - Alias for insert to support InspectionDAO
    public int startInspection(int serviceRecordId, String inspectorName) throws SQLException {
        Integer inspectionId = procedureCaller.executeStartDigitalInspection(serviceRecordId, inspectorName);
        return inspectionId != null ? inspectionId : -1;
    }

    public boolean completeInspection(int inspectionId, String overallCondition, String recommendations) throws SQLException {
        return procedureCaller.executeCompleteInspection(inspectionId, overallCondition, recommendations);
    }

    /**
     * Adds a checklist item to an inspection.
     *
     * @param inspectionId the inspection ID
     * @param itemName the item name
     * @param status the status (PASS, FAIL, WARNING, NOT_CHECKED)
     * @param notes the notes
     * @return true if successful
     * @throws SQLException if database error occurs
     */
    public boolean addChecklistItem(int inspectionId, String itemName, String status, String notes) throws SQLException {
        return procedureCaller.executeAddInspectionChecklistItem(inspectionId, itemName, status, notes);
    }

    /**
     * Adds a checklist item with a photo to an inspection.
     *
     * @param inspectionId the inspection ID
     * @param itemName the item name
     * @param status the status (PASS, FAIL, WARNING, NOT_CHECKED)
     * @param notes the notes
     * @param photoPath the path to the photo
     * @return true if successful
     * @throws SQLException if database error occurs
     */
    public boolean addChecklistItemWithPhoto(int inspectionId, String itemName, String status, String notes, String photoPath) throws SQLException {
        return procedureCaller.executeAddInspectionChecklistItemWithPhoto(inspectionId, itemName, status, notes, photoPath);
    }

    /**
     * Updates a checklist item.
     *
     * @param itemId the checklist item ID
     * @param status the new status
     * @param notes the new notes
     * @return true if successful
     * @throws SQLException if database error occurs
     */
    public boolean updateChecklistItem(int itemId, String status, String notes) throws SQLException {
        return procedureCaller.executeUpdateInspectionChecklistItem(itemId, status, notes, null);
    }

    /**
     * Updates a checklist item with a photo.
     *
     * @param itemId the checklist item ID
     * @param status the new status
     * @param notes the new notes
     * @param photoPath the new photo path
     * @return true if successful
     * @throws SQLException if database error occurs
     */
    public boolean updateChecklistItemWithPhoto(int itemId, String status, String notes, String photoPath) throws SQLException {
        return procedureCaller.executeUpdateInspectionChecklistItem(itemId, status, notes, photoPath);
    }

    @Override
    public boolean update(DigitalInspection entity) throws SQLException {
        return completeInspection(entity.getId(), entity.getOverallCondition(), entity.getRecommendations());
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return procedureCaller.executeDeleteDigitalInspection(id);
    }

    public double getPassRateForWorkshop(int workshopId) throws SQLException {
        List<DigitalInspection> inspections = findByWorkshopId(workshopId);
        if (inspections.isEmpty()) return 0.0;
        int totalItems = 0;
        int passedItems = 0;
        for (DigitalInspection inspection : inspections) {
            totalItems += inspection.getTotalItems();
            passedItems += inspection.getPassedItems();
        }
        return totalItems == 0 ? 0.0 : (double) passedItems / totalItems * 100;
    }

    // ============================================
    // HELPER METHODS FOR MAPPING
    // ============================================

    private DigitalInspection mapToDigitalInspection(Map<String, Object> map) {
        if (map == null) return null;

        DigitalInspection inspection = new DigitalInspection();

        if (map.get("id") != null) inspection.setId(((Number) map.get("id")).intValue());
        if (map.get("service_record_id") != null) inspection.setServiceRecordId(((Number) map.get("service_record_id")).intValue());
        if (map.get("service_type") != null) inspection.setServiceType(map.get("service_type").toString());
        if (map.get("vehicle_id") != null) inspection.setVehicleId(((Number) map.get("vehicle_id")).intValue());
        if (map.get("registration_number") != null) inspection.setRegistrationNumber(map.get("registration_number").toString());
        if (map.get("workshop_id") != null) inspection.setWorkshopId(((Number) map.get("workshop_id")).intValue());
        if (map.get("workshop_name") != null) inspection.setWorkshopName(map.get("workshop_name").toString());
        if (map.get("inspector_name") != null) inspection.setInspectorName(map.get("inspector_name").toString());
        if (map.get("overall_condition") != null) inspection.setOverallCondition(map.get("overall_condition").toString());
        if (map.get("recommendations") != null) inspection.setRecommendations(map.get("recommendations").toString());

        if (map.get("inspection_date") != null) {
            Object dateObj = map.get("inspection_date");
            if (dateObj instanceof java.sql.Date) {
                inspection.setInspectionDate(((java.sql.Date) dateObj).toLocalDate());
            } else if (dateObj instanceof LocalDateTime) {
                inspection.setInspectionDate(((LocalDateTime) dateObj).toLocalDate());
            }
        }
        if (map.get("created_at") instanceof java.sql.Timestamp) {
            inspection.setCreatedAt(((java.sql.Timestamp) map.get("created_at")).toLocalDateTime());
        }
        if (map.get("updated_at") instanceof java.sql.Timestamp) {
            inspection.setUpdatedAt(((java.sql.Timestamp) map.get("updated_at")).toLocalDateTime());
        }

        return inspection;
    }

    private List<DigitalInspection> mapToDigitalInspectionList(List<Map<String, Object>> maps) {
        List<DigitalInspection> inspections = new ArrayList<>();
        if (maps != null) {
            for (Map<String, Object> map : maps) {
                inspections.add(mapToDigitalInspection(map));
            }
        }
        return inspections;
    }

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

        return item;
    }

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
    protected DigitalInspection mapRow(ResultSet rs) throws SQLException {
        DigitalInspection inspection = new DigitalInspection();
        inspection.setId(rs.getInt("id"));
        inspection.setServiceRecordId(rs.getInt("service_record_id"));
        inspection.setServiceType(rs.getString("service_type"));
        inspection.setVehicleId(rs.getInt("vehicle_id"));
        inspection.setRegistrationNumber(rs.getString("registration_number"));
        inspection.setWorkshopId(rs.getInt("workshop_id"));
        inspection.setWorkshopName(rs.getString("workshop_name"));
        inspection.setInspectorName(rs.getString("inspector_name"));

        if (rs.getDate("inspection_date") != null) {
            inspection.setInspectionDate(rs.getDate("inspection_date").toLocalDate());
        }
        inspection.setOverallCondition(rs.getString("overall_condition"));
        inspection.setRecommendations(rs.getString("recommendations"));

        if (rs.getTimestamp("created_at") != null) {
            inspection.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            inspection.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        return inspection;
    }
}