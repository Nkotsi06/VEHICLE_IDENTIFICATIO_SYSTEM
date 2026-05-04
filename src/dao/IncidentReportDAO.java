package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import database.ProcedureCaller;
import database.ViewLoader;
import models.IncidentReport;

/**
 * IncidentReportDAO - Uses ONLY stored procedures and views for all operations.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class IncidentReportDAO extends BaseDAO<IncidentReport> {

    private final ProcedureCaller procedureCaller;
    private final ViewLoader viewLoader;

    public IncidentReportDAO() {
        this.procedureCaller = new ProcedureCaller();
        this.viewLoader = new ViewLoader();
    }

    @Override
    public IncidentReport findById(int id) throws SQLException {
        List<IncidentReport> results = viewLoader.loadViewWithCondition("vw_police_reports", "id = ?", id);
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public List<IncidentReport> findAll() throws SQLException {
        return viewLoader.loadView("vw_police_reports");
    }

    public List<IncidentReport> findByVehicleId(int vehicleId) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_police_reports", "vehicle_id = ? ORDER BY report_date DESC", vehicleId);
    }

    public List<IncidentReport> findByReportType(String reportType) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_police_reports", "report_type = ? ORDER BY report_date DESC", reportType);
    }

    public List<IncidentReport> findByOfficer(String officerName) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_police_reports", "officer_name ILIKE ? ORDER BY report_date DESC", "%" + officerName + "%");
    }

    public List<IncidentReport> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_police_reports",
                "report_date BETWEEN ? AND ? ORDER BY report_date DESC", startDate, endDate);
    }

    @Override
    public boolean insert(IncidentReport entity) throws SQLException {
        Integer reportId = procedureCaller.executeCreatePoliceReport(
                entity.getVehicleId(),
                java.sql.Date.valueOf(entity.getIncidentDateTime().toLocalDate()),
                entity.getIncidentType(),
                entity.getDescription(),
                entity.getOfficerName(),
                "", // badgeNumber
                "", // caseNumber
                entity.getLocation(),
                entity.getLatitude(),
                entity.getLongitude()
        );
        if (reportId != null && reportId > 0) {
            entity.setId(reportId);
            return true;
        }
        return false;
    }

    @Override
    public boolean update(IncidentReport entity) throws SQLException {
        return procedureCaller.executeUpdatePoliceReport(
                entity.getId(),
                entity.getDescription()
        );
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return procedureCaller.executeDeletePoliceReport(id);
    }

    public int countByVehicle(int vehicleId) throws SQLException {
        return viewLoader.countViewRowsWithCondition("vw_police_reports", "vehicle_id = ?", vehicleId);
    }

    @Override
    protected IncidentReport mapRow(ResultSet rs) throws SQLException {
        IncidentReport report = new IncidentReport();
        report.setId(rs.getInt("id"));
        report.setVehicleId(rs.getInt("vehicle_id"));
        report.setRegistrationNumber(rs.getString("registration_number"));

        if (rs.getDate("report_date") != null) {
            report.setIncidentDateTime(rs.getDate("report_date").toLocalDate().atStartOfDay());
        }
        report.setIncidentType(rs.getString("report_type"));
        report.setDescription(rs.getString("description"));
        report.setOfficerName(rs.getString("officer_name"));
        report.setLocation(rs.getString("location"));
        report.setLatitude(rs.getDouble("latitude"));
        report.setLongitude(rs.getDouble("longitude"));

        if (rs.getTimestamp("created_at") != null) {
            report.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            report.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        return report;
    }
}