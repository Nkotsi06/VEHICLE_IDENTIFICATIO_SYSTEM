package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import models.DigitalInspection;
import models.InspectionChecklistItem;

public class DigitalInspectionDAO extends BaseDAO<DigitalInspection> {

    private InspectionChecklistItemDAO checklistItemDAO = new InspectionChecklistItemDAO();

    @Override
    public DigitalInspection findById(int id) throws SQLException {
        String sql = "SELECT * FROM vw_digital_inspection WHERE inspection_id = ?";
        DigitalInspection inspection = executeQuerySingle(sql, id);
        if (inspection != null) {
            List<InspectionChecklistItem> items = checklistItemDAO.findByInspectionId(id);
            inspection.setChecklistItems(items);
        }
        return inspection;
    }

    @Override
    public List<DigitalInspection> findAll() throws SQLException {
        String sql = "SELECT * FROM vw_digital_inspection ORDER BY inspection_date DESC";
        return executeQuery(sql);
    }

    public List<DigitalInspection> findByVehicleId(int vehicleId) throws SQLException {
        String sql = "SELECT * FROM vw_digital_inspection WHERE vehicle_id = ? ORDER BY inspection_date DESC";
        return executeQuery(sql, vehicleId);
    }

    public List<DigitalInspection> findByWorkshopId(int workshopId) throws SQLException {
        String sql = "SELECT * FROM vw_digital_inspection WHERE workshop_id = ? ORDER BY inspection_date DESC";
        return executeQuery(sql, workshopId);
    }

    public List<DigitalInspection> findByServiceRecordId(int serviceRecordId) throws SQLException {
        String sql = "SELECT * FROM vw_digital_inspection WHERE service_record_id = ?";
        return executeQuery(sql, serviceRecordId);
    }

    public int countCompletedByWorkshopId(int workshopId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM digital_inspections di " +
                "JOIN service_records sr ON di.service_record_id = sr.id " +
                "WHERE sr.workshop_id = ? AND di.overall_condition IS NOT NULL";
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

    public int startInspection(int serviceRecordId, String inspectorName) throws SQLException {
        String sql = "CALL sp_start_digital_inspection(?, ?, ?)";
        Connection conn = null;
        java.sql.CallableStatement cs = null;
        try {
            conn = getConnection();
            cs = conn.prepareCall("{call sp_start_digital_inspection(?, ?, ?)}");
            cs.setInt(1, serviceRecordId);
            cs.setString(2, inspectorName);
            cs.registerOutParameter(3, Types.INTEGER);
            cs.execute();
            return cs.getInt(3);
        } finally {
            if (cs != null) cs.close();
            closeResources(null, null, conn);
        }
    }

    public boolean completeInspection(int inspectionId, String overallCondition, String recommendations) throws SQLException {
        return executeProcedure("sp_complete_inspection", inspectionId, overallCondition, recommendations);
    }

    @Override
    public boolean insert(DigitalInspection entity) throws SQLException {
        return false;
    }

    @Override
    public boolean update(DigitalInspection entity) throws SQLException {
        String sql = "UPDATE digital_inspections SET overall_condition = ?, recommendations = ? WHERE id = ?";
        int result = executeUpdate(sql, entity.getOverallCondition(), entity.getRecommendations(), entity.getId());
        return result > 0;
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM digital_inspections WHERE id = ?";
        int result = executeUpdate(sql, id);
        return result > 0;
    }

    @Override
    protected DigitalInspection mapRow(ResultSet rs) throws SQLException {
        DigitalInspection inspection = new DigitalInspection();
        inspection.setId(rs.getInt("inspection_id"));
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