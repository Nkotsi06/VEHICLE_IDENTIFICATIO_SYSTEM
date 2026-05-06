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
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_police_reports", "id = ?", id);
        if (results.isEmpty()) {
            return null;
        }
        return mapMapToPoliceReport(results.get(0));
    }

    public PoliceReport findByCaseNumber(String caseNumber) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_police_reports", "case_number = ?", caseNumber);
        if (results.isEmpty()) {
            return null;
        }
        return mapMapToPoliceReport(results.get(0));
    }

    @Override
    public List<PoliceReport> findAll() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadView("vw_police_reports");
        return mapMapsToPoliceReports(results);
    }

    public List<PoliceReport> findByVehicleId(int vehicleId) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_police_reports", "vehicle_id = ? ORDER BY report_date DESC", vehicleId);
        return mapMapsToPoliceReports(results);
    }

    public List<PoliceReport> findByReportType(String reportType) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_police_reports", "report_type = ? ORDER BY report_date DESC", reportType);
        return mapMapsToPoliceReports(results);
    }

    public List<PoliceReport> findByOfficer(String officerName) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_police_reports", "officer_name ILIKE ? ORDER BY report_date DESC", "%" + officerName + "%");
        return mapMapsToPoliceReports(results);
    }

    public List<PoliceReport> findByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_police_reports", "report_date BETWEEN ? AND ? ORDER BY report_date DESC", startDate, endDate);
        return mapMapsToPoliceReports(results);
    }

    @Override
    public boolean insert(PoliceReport entity) throws SQLException {
        Integer reportId = procedureCaller.executeCreatePoliceReport(
                entity.getVehicleId(),
                entity.getReportDate(),
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
                entity.getDescription()
        );
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return procedureCaller.executeDeletePoliceReport(id);
    }

    public int countReportsByType(String reportType) throws SQLException {
        return viewLoader.countViewRowsWithCondition("vw_police_reports", "report_type = ?", reportType);
    }

    /**
     * Converts a List of Maps to a List of PoliceReport objects.
     */
    private List<PoliceReport> mapMapsToPoliceReports(List<Map<String, Object>> maps) {
        List<PoliceReport> reports = new ArrayList<>();
        if (maps == null) {
            return reports;
        }
        for (Map<String, Object> map : maps) {
            PoliceReport report = mapMapToPoliceReport(map);
            if (report != null) {
                reports.add(report);
            }
        }
        return reports;
    }

    /**
     * Converts a Map to a PoliceReport object.
     */
    private PoliceReport mapMapToPoliceReport(Map<String, Object> map) {
        if (map == null) {
            return null;
        }

        PoliceReport report = new PoliceReport();

        report.setId(getIntValue(map, "id"));
        report.setVehicleId(getIntValue(map, "vehicle_id"));
        report.setRegistrationNumber(getStringValue(map, "registration_number"));
        report.setMake(getStringValue(map, "make"));
        report.setModel(getStringValue(map, "model"));
        report.setReportType(getStringValue(map, "report_type"));
        report.setDescription(getStringValue(map, "description"));
        report.setOfficerName(getStringValue(map, "officer_name"));
        report.setBadgeNumber(getStringValue(map, "badge_number"));
        report.setCaseNumber(getStringValue(map, "case_number"));
        report.setLocation(getStringValue(map, "location"));
        report.setLatitude(getDoubleValue(map, "latitude"));
        report.setLongitude(getDoubleValue(map, "longitude"));
        report.setStatus(getStringValue(map, "status"));

        report.setReportDate(getLocalDateValue(map, "report_date"));
        report.setCreatedAt(getLocalDateTimeValue(map, "created_at"));
        report.setUpdatedAt(getLocalDateTimeValue(map, "updated_at"));

        return report;
    }

    private Integer getIntValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return 0;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Long) return ((Long) value).intValue();
        if (value instanceof Number) return ((Number) value).intValue();
        return 0;
    }

    private Double getDoubleValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return 0.0;
        if (value instanceof Double) return (Double) value;
        if (value instanceof Integer) return ((Integer) value).doubleValue();
        if (value instanceof Long) return ((Long) value).doubleValue();
        if (value instanceof Number) return ((Number) value).doubleValue();
        return 0.0;
    }

    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "";
    }

    private LocalDate getLocalDateValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof java.sql.Date) return ((java.sql.Date) value).toLocalDate();
        if (value instanceof LocalDate) return (LocalDate) value;
        return null;
    }

    private LocalDateTime getLocalDateTimeValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof java.sql.Timestamp) return ((java.sql.Timestamp) value).toLocalDateTime();
        if (value instanceof LocalDateTime) return (LocalDateTime) value;
        return null;
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
        report.setStatus(rs.getString("status"));

        if (rs.getTimestamp("created_at") != null) {
            report.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            report.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        return report;
    }
}