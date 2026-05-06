package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import database.ProcedureCaller;
import database.ViewLoader;
import models.DigitalInspection;
import models.InspectionChecklistItem;

/**
 * InspectionDAO - Facade that uses ONLY stored procedures and views for all operations.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class InspectionDAO extends BaseDAO<DigitalInspection> {

    private final ProcedureCaller procedureCaller;
    private final ViewLoader viewLoader;
    private final DigitalInspectionDAO inspectionDAO;
    private final InspectionChecklistItemDAO itemDAO;

    public InspectionDAO() {
        this.procedureCaller = new ProcedureCaller();
        this.viewLoader = new ViewLoader();
        this.inspectionDAO = new DigitalInspectionDAO();
        this.itemDAO = new InspectionChecklistItemDAO();
    }

    @Override
    public DigitalInspection findById(int id) throws SQLException {
        return inspectionDAO.findById(id);
    }

    @Override
    public List<DigitalInspection> findAll() throws SQLException {
        return inspectionDAO.findAll();
    }

    public List<DigitalInspection> findByVehicleId(int vehicleId) throws SQLException {
        return inspectionDAO.findByVehicleId(vehicleId);
    }

    public List<DigitalInspection> findByWorkshopId(int workshopId) throws SQLException {
        return inspectionDAO.findByWorkshopId(workshopId);
    }

    public List<DigitalInspection> findByServiceRecordId(int serviceRecordId) throws SQLException {
        return inspectionDAO.findByServiceRecordId(serviceRecordId);
    }

    public int startInspection(int serviceRecordId, String inspectorName) throws SQLException {
        return inspectionDAO.startInspection(serviceRecordId, inspectorName);
    }

    public boolean completeInspection(int inspectionId, String overallCondition, String recommendations) throws SQLException {
        return inspectionDAO.completeInspection(inspectionId, overallCondition, recommendations);
    }

    public boolean addInspectionItem(int inspectionId, String itemName, String status, String notes) throws SQLException {
        InspectionChecklistItem item = new InspectionChecklistItem(inspectionId, itemName, status, notes);
        return itemDAO.insert(item);
    }

    public boolean addInspectionItemWithPhoto(int inspectionId, String itemName, String status, String notes, String photoPath) throws SQLException {
        return procedureCaller.executeAddInspectionChecklistItemWithPhoto(inspectionId, itemName, status, notes, photoPath);
    }

    public List<InspectionChecklistItem> getInspectionItems(int inspectionId) throws SQLException {
        return itemDAO.findByInspectionId(inspectionId);
    }

    public boolean updateInspectionItemStatus(int itemId, String status, String notes) throws SQLException {
        InspectionChecklistItem item = itemDAO.findById(itemId);
        if (item != null) {
            item.setStatus(status);
            item.setNotes(notes);
            return itemDAO.update(item);
        }
        return false;
    }

    public boolean deleteInspection(int inspectionId) throws SQLException {
        return inspectionDAO.delete(inspectionId);
    }

    public boolean deleteInspectionItem(int itemId) throws SQLException {
        return itemDAO.delete(itemId);
    }

    public double getPassRateByWorkshop(int workshopId) throws SQLException {
        List<DigitalInspection> inspections = findByWorkshopId(workshopId);
        if (inspections.isEmpty()) return 0.0;
        int totalPassed = 0;
        int totalItems = 0;
        for (DigitalInspection inspection : inspections) {
            totalPassed += inspection.getPassedItems();
            totalItems += inspection.getTotalItems();
        }
        return totalItems == 0 ? 0.0 : (double) totalPassed / totalItems * 100;
    }

    public List<DigitalInspection> getInspectionsByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_digital_inspections",
                "inspection_date BETWEEN ? AND ? ORDER BY inspection_date DESC", startDate, endDate);
        return inspectionDAO.mapMapsToDigitalInspections(results);
    }

    @Override
    public boolean insert(DigitalInspection entity) throws SQLException {
        return inspectionDAO.insert(entity);
    }

    @Override
    public boolean update(DigitalInspection entity) throws SQLException {
        return inspectionDAO.update(entity);
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return inspectionDAO.delete(id);
    }

    @Override
    protected DigitalInspection mapRow(ResultSet rs) throws SQLException {
        return inspectionDAO.mapRow(rs);
    }
}