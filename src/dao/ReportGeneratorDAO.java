package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportGeneratorDAO extends BaseDAO<Object> {

    @Override
    public Object findById(int id) throws SQLException {
        return null;
    }

    @Override
    public List<Object> findAll() throws SQLException {
        return null;
    }

    // ============================================
    // REQUIRED BaseDAO mapRow METHOD
    // ============================================

    @Override
    protected Object mapRow(ResultSet rs) throws SQLException {
        // This DAO doesn't map to a single model - it returns Map<String, Object> for reports
        // Returning null is acceptable since this DAO is not used for standard entity operations
        return null;
    }

    // ============================================
    // REPORT GENERATION METHODS
    // ============================================

    public List<Map<String, Object>> generateVehicleReport(LocalDate startDate, LocalDate endDate) throws SQLException {
        String sql = "SELECT v.registration_number, v.make, v.model, v.year, u.full_name as owner_name, " +
                "vs.status_name, COUNT(sr.id) as service_count, COALESCE(SUM(sr.cost), 0) as total_service_cost " +
                "FROM vehicles v " +
                "LEFT JOIN customers c ON v.owner_id = c.id " +
                "LEFT JOIN users u ON c.user_id = u.id " +
                "LEFT JOIN vehicle_status vs ON v.status_id = vs.id " +
                "LEFT JOIN service_records sr ON v.id = sr.vehicle_id AND sr.service_date BETWEEN ? AND ? " +
                "WHERE v.created_at BETWEEN ? AND ? " +
                "GROUP BY v.id, v.registration_number, v.make, v.model, v.year, u.full_name, vs.status_name " +
                "ORDER BY v.created_at DESC";

        List<Map<String, Object>> results = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setDate(1, java.sql.Date.valueOf(startDate));
            ps.setDate(2, java.sql.Date.valueOf(endDate));
            ps.setDate(3, java.sql.Date.valueOf(startDate));
            ps.setDate(4, java.sql.Date.valueOf(endDate));
            rs = ps.executeQuery();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("registration_number", rs.getString("registration_number"));
                row.put("make", rs.getString("make"));
                row.put("model", rs.getString("model"));
                row.put("year", rs.getInt("year"));
                row.put("owner_name", rs.getString("owner_name"));
                row.put("status_name", rs.getString("status_name"));
                row.put("service_count", rs.getInt("service_count"));
                row.put("total_service_cost", rs.getDouble("total_service_cost"));
                results.add(row);
            }

            return results;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    public List<Map<String, Object>> generateViolationReport(LocalDate startDate, LocalDate endDate) throws SQLException {
        String sql = "SELECT v.registration_number, v.make, v.model, vi.violation_type, " +
                "vi.violation_date, vi.fine_amount, vi.payment_status, vi.officer_name " +
                "FROM violations vi " +
                "JOIN vehicles v ON vi.vehicle_id = v.id " +
                "WHERE vi.violation_date BETWEEN ? AND ? " +
                "ORDER BY vi.violation_date DESC";

        List<Map<String, Object>> results = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setDate(1, java.sql.Date.valueOf(startDate));
            ps.setDate(2, java.sql.Date.valueOf(endDate));
            rs = ps.executeQuery();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("registration_number", rs.getString("registration_number"));
                row.put("make", rs.getString("make"));
                row.put("model", rs.getString("model"));
                row.put("violation_type", rs.getString("violation_type"));
                row.put("violation_date", rs.getDate("violation_date").toLocalDate());
                row.put("fine_amount", rs.getDouble("fine_amount"));
                row.put("payment_status", rs.getString("payment_status"));
                row.put("officer_name", rs.getString("officer_name"));
                results.add(row);
            }

            return results;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    public List<Map<String, Object>> generateFinancialReport(LocalDate startDate, LocalDate endDate) throws SQLException {
        String sql = "SELECT 'Fine Payment' as type, payment_date as date, amount, receipt_number as reference " +
                "FROM payments WHERE payment_date BETWEEN ? AND ? " +
                "UNION ALL " +
                "SELECT 'Insurance Payment' as type, payment_date as date, amount + COALESCE(late_fee, 0) as amount, receipt_number as reference " +
                "FROM insurance_payments WHERE payment_date BETWEEN ? AND ? AND status = 'COMPLETED' " +
                "ORDER BY date DESC";

        List<Map<String, Object>> results = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setDate(1, java.sql.Date.valueOf(startDate));
            ps.setDate(2, java.sql.Date.valueOf(endDate));
            ps.setDate(3, java.sql.Date.valueOf(startDate));
            ps.setDate(4, java.sql.Date.valueOf(endDate));
            rs = ps.executeQuery();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("type", rs.getString("type"));
                row.put("date", rs.getDate("date").toLocalDate());
                row.put("amount", rs.getDouble("amount"));
                row.put("reference", rs.getString("reference"));
                results.add(row);
            }

            return results;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    public Map<String, Object> generateSummaryStatistics() throws SQLException {
        String sql = "SELECT * FROM vw_dashboard_stats";
        Map<String, Object> stats = new HashMap<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            if (rs.next()) {
                stats.put("total_vehicles", rs.getInt("total_vehicles"));
                stats.put("total_customers", rs.getInt("total_customers"));
                stats.put("stolen_count", rs.getInt("stolen_count"));
                stats.put("active_insurance", rs.getInt("active_insurance"));
                stats.put("unpaid_fines", rs.getDouble("unpaid_fines"));
                stats.put("pending_queries", rs.getInt("pending_queries"));
                stats.put("pending_workshops", rs.getInt("pending_workshops"));
                stats.put("pending_claims", rs.getInt("pending_claims"));
            }

            return stats;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    public List<Map<String, Object>> generateWorkshopPerformanceReport() throws SQLException {
        String sql = "SELECT * FROM vw_workshop_performance ORDER BY service_count DESC";
        List<Map<String, Object>> results = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("workshop_name", rs.getString("workshop_name"));
                row.put("service_count", rs.getInt("service_count"));
                row.put("total_revenue", rs.getDouble("total_revenue"));
                row.put("average_service_cost", rs.getDouble("average_service_cost"));
                row.put("avg_rating", rs.getDouble("avg_rating"));
                results.add(row);
            }

            return results;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    public List<Map<String, Object>> generateStolenVehicleReport() throws SQLException {
        String sql = "SELECT v.registration_number, v.make, v.model, " +
                "sr.reported_date, sr.case_number, sr.assigned_officer, sr.status " +
                "FROM stolen_vehicles sr " +
                "JOIN vehicles v ON sr.vehicle_id = v.id " +
                "WHERE sr.status = 'ACTIVE' " +
                "ORDER BY sr.reported_date DESC";

        List<Map<String, Object>> results = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("registration_number", rs.getString("registration_number"));
                row.put("make", rs.getString("make"));
                row.put("model", rs.getString("model"));
                row.put("reported_date", rs.getDate("reported_date").toLocalDate());
                row.put("case_number", rs.getString("case_number"));
                row.put("assigned_officer", rs.getString("assigned_officer"));
                row.put("status", rs.getString("status"));
                results.add(row);
            }

            return results;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    public List<Map<String, Object>> generateExpiredDocumentsReport() throws SQLException {
        String sql = "SELECT v.registration_number, vd.document_type, vd.expiry_date, " +
                "(vd.expiry_date - CURRENT_DATE) as days_remaining, " +
                "CASE WHEN vd.expiry_date < CURRENT_DATE THEN 'EXPIRED' ELSE 'EXPIRING SOON' END as expiry_status " +
                "FROM vehicle_documents vd " +
                "JOIN vehicles v ON vd.vehicle_id = v.id " +
                "WHERE vd.expiry_date <= CURRENT_DATE + INTERVAL '30 days' " +
                "ORDER BY vd.expiry_date";

        List<Map<String, Object>> results = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("registration_number", rs.getString("registration_number"));
                row.put("document_type", rs.getString("document_type"));
                row.put("expiry_date", rs.getDate("expiry_date").toLocalDate());
                row.put("days_remaining", rs.getInt("days_remaining"));
                row.put("expiry_status", rs.getString("expiry_status"));
                results.add(row);
            }

            return results;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    // ============================================
    // POLICE-SPECIFIC REPORT METHODS
    // ============================================

    public List<Map<String, Object>> generateWarrantsReport() throws SQLException {
        String sql = "SELECT v.registration_number, vi.violation_type, " +
                "w.issue_date, w.expiry_date, w.judge_name, w.status " +
                "FROM warrants w " +
                "JOIN violations vi ON w.violation_id = vi.id " +
                "JOIN vehicles v ON vi.vehicle_id = v.id " +
                "ORDER BY w.issue_date DESC";

        List<Map<String, Object>> results = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("registration_number", rs.getString("registration_number"));
                row.put("violation_type", rs.getString("violation_type"));
                row.put("issue_date", rs.getDate("issue_date").toLocalDate());
                row.put("expiry_date", rs.getDate("expiry_date") != null ? rs.getDate("expiry_date").toLocalDate() : null);
                row.put("judge_name", rs.getString("judge_name"));
                row.put("status", rs.getString("status"));
                results.add(row);
            }

            return results;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    public List<Map<String, Object>> generateBOLOAlertsReport() throws SQLException {
        String sql = "SELECT v.registration_number, b.message, b.priority, " +
                "b.alert_date, b.expiry_date, b.status " +
                "FROM bolo_alerts b " +
                "JOIN vehicles v ON b.vehicle_id = v.id " +
                "WHERE b.status = 'ACTIVE' OR b.expiry_date > CURRENT_DATE " +
                "ORDER BY b.priority DESC, b.alert_date DESC";

        List<Map<String, Object>> results = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("registration_number", rs.getString("registration_number"));
                row.put("message", rs.getString("message"));
                row.put("priority", rs.getString("priority"));
                row.put("alert_date", rs.getDate("alert_date").toLocalDate());
                row.put("expiry_date", rs.getDate("expiry_date") != null ? rs.getDate("expiry_date").toLocalDate() : null);
                row.put("status", rs.getString("status"));
                results.add(row);
            }

            return results;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    public List<Map<String, Object>> generateGeofenceAlertsReport() throws SQLException {
        String sql = "SELECT gz.zone_name, v.registration_number, gae.alert_type, " +
                "gae.alert_timestamp, gae.is_notified " +
                "FROM geofence_alert_events gae " +
                "JOIN geofence_zones gz ON gae.geofence_zone_id = gz.id " +
                "JOIN vehicles v ON gae.vehicle_id = v.id " +
                "WHERE gae.alert_timestamp >= CURRENT_DATE - INTERVAL '30 days' " +
                "ORDER BY gae.alert_timestamp DESC";

        List<Map<String, Object>> results = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("zone_name", rs.getString("zone_name"));
                row.put("registration_number", rs.getString("registration_number"));
                row.put("alert_type", rs.getString("alert_type"));
                row.put("alert_timestamp", rs.getTimestamp("alert_timestamp").toLocalDateTime());
                row.put("is_notified", rs.getBoolean("is_notified"));
                results.add(row);
            }

            return results;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    public List<Map<String, Object>> generateOfficerActivityReport() throws SQLException {
        String sql = "SELECT ol.officer_name, ol.action, " +
                "v.registration_number, ol.timestamp " +
                "FROM officer_logs ol " +
                "LEFT JOIN vehicles v ON ol.vehicle_id = v.id " +
                "WHERE ol.timestamp >= CURRENT_DATE - INTERVAL '30 days' " +
                "ORDER BY ol.timestamp DESC";

        List<Map<String, Object>> results = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("officer_name", rs.getString("officer_name"));
                row.put("action", rs.getString("action"));
                row.put("registration_number", rs.getString("registration_number"));
                row.put("timestamp", rs.getTimestamp("timestamp").toLocalDateTime());
                results.add(row);
            }

            return results;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    // ============================================
    // SCHEDULED REPORTS METHODS
    // ============================================

    public boolean scheduleReport(String reportName, String frequency, String recipientEmail) throws SQLException {
        String sql = "INSERT INTO scheduled_reports (report_name, frequency, recipient_email, next_run, created_at) " +
                "VALUES (?, ?, ?, CURRENT_DATE + 1, CURRENT_TIMESTAMP)";
        int result = executeUpdate(sql, reportName, frequency, recipientEmail);
        return result > 0;
    }

    public List<String> getScheduledReports() throws SQLException {
        String sql = "SELECT report_name, frequency, recipient_email, next_run FROM scheduled_reports ORDER BY next_run";
        List<String> reports = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                String report = rs.getString("report_name") + " - " +
                        rs.getString("frequency") + " - " +
                        rs.getString("recipient_email") + " - Next: " +
                        rs.getDate("next_run");
                reports.add(report);
            }
            return reports;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    public boolean unscheduleReport(String reportIdentifier) throws SQLException {
        String reportName = reportIdentifier.split(" - ")[0];
        String sql = "DELETE FROM scheduled_reports WHERE report_name = ?";
        int result = executeUpdate(sql, reportName);
        return result > 0;
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
}