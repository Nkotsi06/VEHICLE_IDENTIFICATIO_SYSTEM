package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import models.DigitalInspection;
import models.InspectionChecklistItem;

public class InspectionDAO extends BaseDAO<DigitalInspection> {

    private DigitalInspectionDAO inspectionDAO = new DigitalInspectionDAO();
    private InspectionChecklistItemDAO itemDAO = new InspectionChecklistItemDAO();

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
        InspectionChecklistItem item = new InspectionChecklistItem(inspectionId, itemName, status, notes);
        item.setPhotoPath(photoPath);
        return itemDAO.insert(item);
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
        String sql = "SELECT ROUND(AVG(pass_percentage), 2) FROM vw_digital_inspection WHERE workshop_id = ?";
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

    public List<DigitalInspection> getInspectionsByDateRange(java.time.LocalDate startDate, java.time.LocalDate endDate) throws SQLException {
        String sql = "SELECT * FROM vw_digital_inspection WHERE inspection_date BETWEEN ? AND ? ORDER BY inspection_date DESC";
        return executeQuery(sql, startDate, endDate);
    }

    @Override
    public boolean insert(DigitalInspection entity) throws SQLException {
        return false;
    }

    @Override
    public boolean update(DigitalInspection entity) throws SQLException {
        return inspectionDAO.update(entity);
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return inspectionDAO.delete(id);
    }

    // Required mapRow method implementation for BaseDAO
    @Override
    protected DigitalInspection mapRow(ResultSet rs) throws SQLException {
        // Delegate to the inspectionDAO's mapRow method since this is a facade DAO
        return inspectionDAO.mapRow(rs);
    }
}