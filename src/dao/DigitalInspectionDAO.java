package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

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
        List<DigitalInspection> results = viewLoader.loadViewWithCondition("vw_digital_inspections", "id = ?", id);
        if (results.isEmpty()) return null;

        DigitalInspection inspection = results.get(0);
        // Load checklist items using view
        List<InspectionChecklistItem> items = viewLoader.loadViewWithCondition("vw_inspection_checklist_items", "inspection_id = ?", id);
        inspection.setChecklistItems(items);
        return inspection;
    }

    @Override
    public List<DigitalInspection> findAll() throws SQLException {
        return viewLoader.loadView("vw_digital_inspections");
    }

    public List<DigitalInspection> findByVehicleId(int vehicleId) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_digital_inspections", "vehicle_id = ? ORDER BY inspection_date DESC", vehicleId);
    }

    public List<DigitalInspection> findByServiceRecordId(int serviceRecordId) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_digital_inspections", "service_record_id = ?", serviceRecordId);
    }

    public List<DigitalInspection> findByWorkshopId(int workshopId) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_digital_inspections", "workshop_id = ? ORDER BY inspection_date DESC", workshopId);
    }

    public List<DigitalInspection> findByInspector(String inspectorName) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_digital_inspections", "inspector_name ILIKE ? ORDER BY inspection_date DESC", "%" + inspectorName + "%");
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

    public boolean completeInspection(int inspectionId, String overallCondition, String recommendations) throws SQLException {
        return procedureCaller.executeCompleteInspection(inspectionId, overallCondition, recommendations);
    }

    public boolean addChecklistItem(int inspectionId, String itemName, String status, String notes) throws SQLException {
        return procedureCaller.executeAddInspectionChecklistItem(inspectionId, itemName, status, notes);
    }

    public boolean updateChecklistItem(int itemId, String status, String notes) throws SQLException {
        return procedureCaller.executeUpdateInspectionChecklistItem(itemId, status, notes);
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

    @Override
    protected DigitalInspection mapRow(ResultSet rs) throws SQLException {
        DigitalInspection inspection = new DigitalInspection();
        inspection.setId(rs.getInt("id"));
        inspection.setServiceRecordId(rs.getInt("service_record_id"));
        inspection.setServiceType(rs.getString("service_type"));
        inspection.setVehicleId(rs.getInt("vehicle_id"));
        inspection.setRegistrationNumber(rs.getString("registration_number"));
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