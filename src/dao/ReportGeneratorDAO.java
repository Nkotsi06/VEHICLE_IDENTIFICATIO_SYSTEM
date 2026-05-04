package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import database.ProcedureCaller;
import database.ViewLoader;

/**
 * ReportGeneratorDAO - Uses ONLY stored procedures and views for report generation.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class ReportGeneratorDAO extends BaseDAO<Object> {

    private final ProcedureCaller procedureCaller;
    private final ViewLoader viewLoader;

    public ReportGeneratorDAO() {
        this.procedureCaller = new ProcedureCaller();
        this.viewLoader = new ViewLoader();
    }

    @Override
    public Object findById(int id) throws SQLException {
        return null;
    }

    @Override
    public List<Object> findAll() throws SQLException {
        return null;
    }

    // ============================================
    // REPORT GENERATION METHODS USING VIEWS
    // ============================================

    public List<Map<String, Object>> generateVehicleReport(LocalDate startDate, LocalDate endDate) throws SQLException {
        // Use view - NO direct SQL
        return viewLoader.loadViewWithCondition("vw_vehicles", "created_at BETWEEN ? AND ?", startDate, endDate);
    }

    public List<Map<String, Object>> generateViolationReport(LocalDate startDate, LocalDate endDate) throws SQLException {
        // Use view - NO direct SQL
        return viewLoader.loadViewWithCondition("vw_violations", "violation_date BETWEEN ? AND ?", startDate, endDate);
    }

    public List<Map<String, Object>> generateFinancialReport(LocalDate startDate, LocalDate endDate) throws SQLException {
        // Use view - NO direct SQL
        return viewLoader.loadViewWithCondition("vw_financial_transactions", "transaction_date BETWEEN ? AND ?", startDate, endDate);
    }

    public Map<String, Object> generateSummaryStatistics() throws SQLException {
        // Use view - NO direct SQL
        return viewLoader.loadViewSingle("vw_dashboard_stats", null);
    }

    public List<Map<String, Object>> generateWorkshopPerformanceReport() throws SQLException {
        // Use view - NO direct SQL
        return viewLoader.loadView("vw_workshop_performance");
    }

    public List<Map<String, Object>> generateStolenVehicleReport() throws SQLException {
        // Use view - NO direct SQL
        return viewLoader.loadView("vw_stolen_vehicles");
    }

    public List<Map<String, Object>> generateExpiredDocumentsReport() throws SQLException {
        // Use view - NO direct SQL
        return viewLoader.loadView("vw_expired_documents");
    }

    public List<Map<String, Object>> generateAuditLogsReport() throws SQLException {
        // Use view - NO direct SQL
        return viewLoader.loadView("vw_audit_logs");
    }

    public List<Map<String, Object>> generateWarrantsReport() throws SQLException {
        // Use view - NO direct SQL
        return viewLoader.loadView("vw_warrants");
    }

    public List<Map<String, Object>> generateBOLOAlertsReport() throws SQLException {
        // Use view - NO direct SQL
        return viewLoader.loadView("vw_bolo_alerts");
    }

    public List<Map<String, Object>> generateGeofenceAlertsReport() throws SQLException {
        // Use view - NO direct SQL
        return viewLoader.loadView("vw_geofence_alerts");
    }

    public List<Map<String, Object>> generateOfficerActivityReport() throws SQLException {
        // Use view - NO direct SQL
        return viewLoader.loadView("vw_officer_activity_log");
    }

    // ============================================
    // SCHEDULED REPORTS METHODS
    // ============================================

    public boolean scheduleReport(String reportName, String frequency, String recipientEmail) throws SQLException {
        return procedureCaller.executeScheduleReport(reportName, frequency, recipientEmail);
    }

    public List<String> getScheduledReports() throws SQLException {
        // Use view - NO direct SQL
        List<Map<String, Object>> reports = viewLoader.loadView("vw_scheduled_reports");
        List<String> reportStrings = new ArrayList<>();
        for (Map<String, Object> report : reports) {
            String r = report.get("report_name") + " - " +
                    report.get("frequency") + " - " +
                    report.get("recipient_email") + " - Next: " +
                    report.get("next_run");
            reportStrings.add(r);
        }
        return reportStrings;
    }

    public boolean unscheduleReport(String reportIdentifier) throws SQLException {
        String reportName = reportIdentifier.split(" - ")[0];
        return procedureCaller.executeUnscheduleReport(reportName);
    }

    // ============================================
    // REQUIRED BaseDAO METHODS
    // ============================================

    @Override
    public boolean insert(Object entity) throws SQLException {
        return false;
    }

    @Override
    public boolean update(Object entity) throws SQLException {
        return false;
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return false;
    }

    @Override
    protected Object mapRow(ResultSet rs) throws SQLException {
        return null;
    }
}