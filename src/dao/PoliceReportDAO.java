package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import database.ProcedureCaller;
import database.ViewLoader;
import models.PoliceReport;

/**
 * PoliceReportDAO - Uses ONLY stored procedures and views for all operations.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class PoliceReportDAO extends BaseDAO<PoliceReport> {

    private final ProcedureCaller procedureCaller;
    private final ViewLoader viewLoader;

    public PoliceReportDAO() {
        this.procedureCaller = new ProcedureCaller();
        this.viewLoader = new ViewLoader();
    }

    @Override
    public PoliceReport findById(int id) throws SQLException {
        List<PoliceReport> results = viewLoader.loadViewWithCondition("vw_police_reports", "id = ?", id);
        return results.isEmpty() ? null : results.get(0);
    }

    public PoliceReport findByCaseNumber(String caseNumber) throws SQLException {
        List<PoliceReport> results = viewLoader.loadViewWithCondition("vw_police_reports", "case_number = ?", caseNumber);
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public List<PoliceReport> findAll() throws SQLException {
        return viewLoader.loadView("vw_police_reports");
    }

    public List<PoliceReport> findByVehicleId(int vehicleId) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_police_reports", "vehicle_id = ? ORDER BY report_date DESC", vehicleId);
    }

    public List<PoliceReport> findByReportType(String reportType) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_police_reports", "report_type = ? ORDER BY report_date DESC", reportType);
    }

    public List<PoliceReport> findByOfficer(String officerName) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_police_reports", "officer_name ILIKE ? ORDER BY report_date DESC", "%" + officerName + "%");
    }

    public List<PoliceReport> findByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_police_reports", "report_date BETWEEN ? AND ? ORDER BY report_date DESC", startDate, endDate);
    }

    @Override
    public boolean insert(PoliceReport entity) throws SQLException {
        Integer reportId = procedureCaller.executeCreatePoliceReport(
                entity.getVehicleId(),
                java.sql.Date.valueOf(entity.getReportDate()),
                entity.getReportType(),
                entity.getDescription(),
                entity.getOfficerName(),
                entity.getBadgeNumber(),
                entity.getCaseNumber(),
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
    public boolean update(PoliceReport entity) throws SQLException {
        return procedureCaller.executeUpdatePoliceReport(
                entity.getId(),
                entity.getVehicleId(),
                entity.getReportDate(),
                entity.getReportType(),
                entity.getDescription(),
                entity.getOfficerName(),
                entity.getBadgeNumber(),
                entity.getCaseNumber(),
                entity.getLocation()
        );
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return procedureCaller.executeDeletePoliceReport(id);
    }

    public int countReportsByType(String reportType) throws SQLException {
        return viewLoader.countViewRowsWithCondition("vw_police_reports", "report_type = ?", reportType);
    }

    @Override
    protected PoliceReport mapRow(ResultSet rs) throws SQLException {
        PoliceReport report = new PoliceReport();
        report.setId(rs.getInt("id"));
        report.setVehicleId(rs.getInt("vehicle_id"));
        report.setRegistrationNumber(rs.getString("registration_number"));
        report.setMake(rs.getString("make"));
        report.setModel(rs.getString("model"));

        if (rs.getDate("report_date") != null) {
            report.setReportDate(rs.getDate("report_date").toLocalDate());
        }
        report.setReportType(rs.getString("report_type"));
        report.setDescription(rs.getString("description"));
        report.setOfficerName(rs.getString("officer_name"));
        report.setBadgeNumber(rs.getString("badge_number"));
        report.setCaseNumber(rs.getString("case_number"));
        report.setLocation(rs.getString("location"));

        if (rs.getTimestamp("created_at") != null) {
            report.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            report.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        return report;
    }
}