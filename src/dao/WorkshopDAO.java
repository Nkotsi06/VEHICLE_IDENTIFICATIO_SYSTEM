package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import database.ProcedureCaller;
import database.ViewLoader;
import models.Workshop;

/**
 * WorkshopDAO - Uses ONLY stored procedures and views for all operations.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class WorkshopDAO extends BaseDAO<Workshop> {

    private final ProcedureCaller procedureCaller;
    private final ViewLoader viewLoader;

    public WorkshopDAO() {
        this.procedureCaller = new ProcedureCaller();
        this.viewLoader = new ViewLoader();
    }

    @Override
    public Workshop findById(int id) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_workshops", "id = ?", id);
        return results.isEmpty() ? null : mapToWorkshop(results.get(0));
    }

    public Workshop findByUserId(int userId) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_workshops", "user_id = ?", userId);
        return results.isEmpty() ? null : mapToWorkshop(results.get(0));
    }

    public Workshop findByLicenseNumber(String licenseNumber) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_workshops", "license_number = ?", licenseNumber);
        return results.isEmpty() ? null : mapToWorkshop(results.get(0));
    }

    @Override
    public List<Workshop> findAll() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadView("vw_workshops");
        return mapToWorkshopList(results);
    }

    public List<Workshop> findApprovedWorkshops() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_workshops", "is_approved = true ORDER BY workshop_name");
        return mapToWorkshopList(results);
    }

    public List<Workshop> findPendingApproval() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_workshops", "is_approved = false ORDER BY created_at");
        return mapToWorkshopList(results);
    }

    public List<Workshop> searchWorkshops(String keyword) throws SQLException {
        String pattern = "%" + keyword + "%";
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_workshops",
                "workshop_name ILIKE ? OR address ILIKE ? ORDER BY workshop_name", pattern, pattern);
        return mapToWorkshopList(results);
    }

    @Override
    public boolean insert(Workshop entity) throws SQLException {
        Integer workshopId = procedureCaller.executeRegisterWorkshop(
                entity.getUserId(),
                entity.getWorkshopName(),
                entity.getAddress(),
                entity.getPhone(),
                entity.getEmail(),
                entity.getLicenseNumber()
        );
        if (workshopId != null && workshopId > 0) {
            entity.setId(workshopId);
            return true;
        }
        return false;
    }

    public boolean approveWorkshop(int workshopId) throws SQLException {
        return procedureCaller.executeApproveWorkshop(workshopId);
    }

    @Override
    public boolean update(Workshop entity) throws SQLException {
        return procedureCaller.executeUpdateWorkshop(
                entity.getId(),
                entity.getWorkshopName(),
                entity.getAddress(),
                entity.getPhone(),
                entity.getEmail(),
                entity.getLicenseNumber()
        );
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return procedureCaller.executeDeleteWorkshop(id);
    }

    public int countApprovedWorkshops() throws SQLException {
        return viewLoader.countViewRowsWithCondition("vw_workshops", "is_approved = true");
    }

    public int countPendingWorkshops() throws SQLException {
        return viewLoader.countViewRowsWithCondition("vw_workshops", "is_approved = false");
    }

    // ============================================
    // HELPER METHODS FOR MAPPING
    // ============================================

    private Workshop mapToWorkshop(Map<String, Object> map) {
        if (map == null) return null;

        Workshop workshop = new Workshop();
        if (map.get("id") != null) workshop.setId(((Number) map.get("id")).intValue());
        if (map.get("user_id") != null) workshop.setUserId(((Number) map.get("user_id")).intValue());
        if (map.get("workshop_name") != null) workshop.setWorkshopName(map.get("workshop_name").toString());
        if (map.get("address") != null) workshop.setAddress(map.get("address").toString());
        if (map.get("phone") != null) workshop.setPhone(map.get("phone").toString());
        if (map.get("email") != null) workshop.setEmail(map.get("email").toString());
        if (map.get("license_number") != null) workshop.setLicenseNumber(map.get("license_number").toString());
        if (map.get("owner_name") != null) workshop.setOwnerName(map.get("owner_name").toString());
        if (map.get("is_approved") != null) workshop.setApproved((Boolean) map.get("is_approved"));

        if (map.get("created_at") instanceof java.sql.Timestamp) {
            workshop.setCreatedAt(((java.sql.Timestamp) map.get("created_at")).toLocalDateTime());
        }
        if (map.get("updated_at") instanceof java.sql.Timestamp) {
            workshop.setUpdatedAt(((java.sql.Timestamp) map.get("updated_at")).toLocalDateTime());
        }
        return workshop;
    }

    private List<Workshop> mapToWorkshopList(List<Map<String, Object>> maps) {
        List<Workshop> workshops = new ArrayList<>();
        if (maps != null) {
            for (Map<String, Object> map : maps) {
                workshops.add(mapToWorkshop(map));
            }
        }
        return workshops;
    }

    @Override
    protected Workshop mapRow(ResultSet rs) throws SQLException {
        Workshop workshop = new Workshop();
        workshop.setId(rs.getInt("id"));
        workshop.setUserId(rs.getInt("user_id"));
        workshop.setWorkshopName(rs.getString("workshop_name"));
        workshop.setAddress(rs.getString("address"));
        workshop.setPhone(rs.getString("phone"));
        workshop.setEmail(rs.getString("email"));
        workshop.setLicenseNumber(rs.getString("license_number"));
        workshop.setApproved(rs.getBoolean("is_approved"));
        workshop.setOwnerName(rs.getString("owner_name"));

        if (rs.getTimestamp("created_at") != null) {
            workshop.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            workshop.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        return workshop;
    }
}