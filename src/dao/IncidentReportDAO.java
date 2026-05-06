package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_police_reports", "id = ?", id);
        if (results.isEmpty()) {
            return null;
        }
        return mapToIncidentReport(results.get(0));
    }

    @Override
    public List<IncidentReport> findAll() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadView("vw_police_reports");
        return mapToIncidentReportList(results);
    }

    public List<IncidentReport> findByVehicleId(int vehicleId) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_police_reports", "vehicle_id = ? ORDER BY report_date DESC", vehicleId);
        return mapToIncidentReportList(results);
    }

    public List<IncidentReport> findByReportType(String reportType) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_police_reports", "report_type = ? ORDER BY report_date DESC", reportType);
        return mapToIncidentReportList(results);
    }

    public List<IncidentReport> findByOfficer(String officerName) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_police_reports", "officer_name ILIKE ? ORDER BY report_date DESC", "%" + officerName + "%");
        return mapToIncidentReportList(results);
    }

    public List<IncidentReport> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_police_reports",
                "report_date BETWEEN ? AND ? ORDER BY report_date DESC", startDate, endDate);
        return mapToIncidentReportList(results);
    }

    @Override
    public boolean insert(IncidentReport entity) throws SQLException {
        // FIXED: Convert LocalDateTime to LocalDate (not java.sql.Date)
        LocalDate reportDate = null;
        if (entity.getIncidentDateTime() != null) {
            reportDate = entity.getIncidentDateTime().toLocalDate();
        }

        Integer reportId = procedureCaller.executeCreatePoliceReport(
                entity.getVehicleId(),
                reportDate,
                entity.getIncidentType(),
                entity.getDescription(),
                entity.getOfficerName(),
                entity.getBadgeNumber() != null ? entity.getBadgeNumber() : "",
                entity.getCaseNumber() != null ? entity.getCaseNumber() : "",
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

    // ============================================
    // HELPER METHODS FOR MAPPING
    // ============================================

    private IncidentReport mapToIncidentReport(Map<String, Object> map) {
        if (map == null) return null;

        IncidentReport report = new IncidentReport();

        if (map.get("id") != null) report.setId(((Number) map.get("id")).intValue());
        if (map.get("vehicle_id") != null) report.setVehicleId(((Number) map.get("vehicle_id")).intValue());
        if (map.get("registration_number") != null) report.setRegistrationNumber(map.get("registration_number").toString());
        if (map.get("make") != null) report.setMake(map.get("make").toString());
        if (map.get("model") != null) report.setModel(map.get("model").toString());
        if (map.get("report_type") != null) report.setIncidentType(map.get("report_type").toString());
        if (map.get("description") != null) report.setDescription(map.get("description").toString());
        if (map.get("officer_name") != null) report.setOfficerName(map.get("officer_name").toString());
        if (map.get("badge_number") != null) report.setBadgeNumber(map.get("badge_number").toString());
        if (map.get("case_number") != null) report.setCaseNumber(map.get("case_number").toString());
        if (map.get("location") != null) report.setLocation(map.get("location").toString());
        if (map.get("latitude") != null) report.setLatitude(((Number) map.get("latitude")).doubleValue());
        if (map.get("longitude") != null) report.setLongitude(((Number) map.get("longitude")).doubleValue());
        if (map.get("status") != null) report.setStatus(map.get("status").toString());

        if (map.get("report_date") != null) {
            Object dateObj = map.get("report_date");
            if (dateObj instanceof java.sql.Date) {
                report.setIncidentDateTime(((java.sql.Date) dateObj).toLocalDate().atStartOfDay());
            } else if (dateObj instanceof LocalDateTime) {
                report.setIncidentDateTime((LocalDateTime) dateObj);
            }
        }
        if (map.get("created_at") instanceof java.sql.Timestamp) {
            report.setCreatedAt(((java.sql.Timestamp) map.get("created_at")).toLocalDateTime());
        }
        if (map.get("updated_at") instanceof java.sql.Timestamp) {
            report.setUpdatedAt(((java.sql.Timestamp) map.get("updated_at")).toLocalDateTime());
        }

        return report;
    }

    private List<IncidentReport> mapToIncidentReportList(List<Map<String, Object>> maps) {
        List<IncidentReport> reports = new ArrayList<>();
        if (maps != null) {
            for (Map<String, Object> map : maps) {
                reports.add(mapToIncidentReport(map));
            }
        }
        return reports;
    }

    @Override
    protected IncidentReport mapRow(ResultSet rs) throws SQLException {
        IncidentReport report = new IncidentReport();
        report.setId(rs.getInt("id"));
        report.setVehicleId(rs.getInt("vehicle_id"));
        report.setRegistrationNumber(rs.getString("registration_number"));

        try {
            report.setMake(rs.getString("make"));
        } catch (SQLException e) {}

        try {
            report.setModel(rs.getString("model"));
        } catch (SQLException e) {}

        if (rs.getDate("report_date") != null) {
            report.setIncidentDateTime(rs.getDate("report_date").toLocalDate().atStartOfDay());
        }
        report.setIncidentType(rs.getString("report_type"));
        report.setDescription(rs.getString("description"));
        report.setOfficerName(rs.getString("officer_name"));
        report.setLocation(rs.getString("location"));

        try {
            report.setLatitude(rs.getDouble("latitude"));
        } catch (SQLException e) {}

        try {
            report.setLongitude(rs.getDouble("longitude"));
        } catch (SQLException e) {}

        if (rs.getTimestamp("created_at") != null) {
            report.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            report.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        return report;
    }
}