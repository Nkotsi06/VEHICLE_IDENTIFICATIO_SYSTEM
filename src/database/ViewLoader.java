package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewLoader {

    private DatabaseConnection dbConnection;

    public ViewLoader() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    public List<Map<String, Object>> loadView(String viewName) throws SQLException {
        return loadViewWithCondition(viewName, null, null);
    }

    public List<Map<String, Object>> loadViewWithCondition(String viewName, String condition, Object... params) throws SQLException {
        String sql = "SELECT * FROM " + viewName;
        if (condition != null && !condition.isEmpty()) {
            sql += " WHERE " + condition;
        }

        List<Map<String, Object>> results = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = dbConnection.getConnection();
            ps = conn.prepareStatement(sql);
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    ps.setObject(i + 1, params[i]);
                }
            }
            rs = ps.executeQuery();
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    row.put(columnName, rs.getObject(i));
                }
                results.add(row);
            }
            return results;
        } finally {
            dbConnection.closeResources(rs, ps, conn);
        }
    }

    public Map<String, Object> loadViewSingle(String viewName, String condition, Object... params) throws SQLException {
        List<Map<String, Object>> results = loadViewWithCondition(viewName, condition, params);
        return results.isEmpty() ? null : results.get(0);
    }

    public List<Map<String, Object>> loadViewWithPagination(String viewName, int limit, int offset) throws SQLException {
        String sql = "SELECT * FROM " + viewName + " LIMIT ? OFFSET ?";
        List<Map<String, Object>> results = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = dbConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, limit);
            ps.setInt(2, offset);
            rs = ps.executeQuery();
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    row.put(columnName, rs.getObject(i));
                }
                results.add(row);
            }
            return results;
        } finally {
            dbConnection.closeResources(rs, ps, conn);
        }
    }

    public int countViewRows(String viewName) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + viewName;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = dbConnection.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } finally {
            dbConnection.closeResources(rs, ps, conn);
        }
    }

    // ============================================
    // VEHICLE VIEWS
    // ============================================

    public List<Map<String, Object>> loadVehiclesView() throws SQLException {
        return loadView("vw_vehicles");
    }

    public List<Map<String, Object>> loadVehiclesByOwner(int ownerId) throws SQLException {
        return loadViewWithCondition("vw_vehicles", "owner_id = ?", ownerId);
    }

    public Map<String, Object> loadVehicleByRegistration(String registrationNumber) throws SQLException {
        return loadViewSingle("vw_vehicles", "registration_number = ?", registrationNumber);
    }

    // ============================================
    // CUSTOMER VIEWS
    // ============================================

    public List<Map<String, Object>> loadCustomersView() throws SQLException {
        return loadView("vw_customers");
    }

    public Map<String, Object> loadCustomerByUserId(int userId) throws SQLException {
        return loadViewSingle("vw_customers", "user_id = ?", userId);
    }

    // ============================================
    // VIOLATION VIEWS
    // ============================================

    public List<Map<String, Object>> loadViolationsView() throws SQLException {
        return loadView("vw_violations");
    }

    public List<Map<String, Object>> loadUnpaidViolations() throws SQLException {
        return loadView("vw_unpaid_violations");
    }

    // ============================================
    // STOLEN VEHICLE VIEWS
    // ============================================

    public List<Map<String, Object>> loadStolenVehiclesView() throws SQLException {
        return loadView("vw_stolen_vehicles");
    }

    public List<Map<String, Object>> loadActiveStolenVehicles() throws SQLException {
        return loadViewWithCondition("vw_stolen_vehicles", "status = 'ACTIVE'");
    }

    // ============================================
    // WARRANT VIEWS
    // ============================================

    public List<Map<String, Object>> loadActiveWarrants() throws SQLException {
        return loadView("vw_active_warrants");
    }

    // ============================================
    // WORKSHOP VIEWS
    // ============================================

    public List<Map<String, Object>> loadWorkshopsView() throws SQLException {
        return loadView("vw_workshops");
    }

    public List<Map<String, Object>> loadApprovedWorkshops() throws SQLException {
        return loadViewWithCondition("vw_workshops", "is_approved = true");
    }

    // ============================================
    // SERVICE RECORD VIEWS
    // ============================================

    public List<Map<String, Object>> loadServiceRecordsView() throws SQLException {
        return loadView("vw_service_records");
    }

    public List<Map<String, Object>> loadServiceRecordsByVehicle(int vehicleId) throws SQLException {
        return loadViewWithCondition("vw_service_records", "vehicle_id = ?", vehicleId);
    }

    // ============================================
    // INSURANCE VIEWS
    // ============================================

    public List<Map<String, Object>> loadInsurancePoliciesView() throws SQLException {
        return loadView("vw_insurance_policies");
    }

    public List<Map<String, Object>> loadActiveInsurancePolicies() throws SQLException {
        return loadView("vw_active_insurance");
    }

    public List<Map<String, Object>> loadInsuranceClaimsView() throws SQLException {
        return loadView("vw_insurance_claims");
    }

    // ============================================
    // CUSTOMER QUERY VIEWS
    // ============================================

    public List<Map<String, Object>> loadCustomerQueriesView() throws SQLException {
        return loadView("vw_customer_queries");
    }

    public List<Map<String, Object>> loadPendingQueries() throws SQLException {
        return loadView("vw_pending_queries");
    }

    // ============================================
    // CUSTOMER COMPLAINT VIEWS
    // ============================================

    public List<Map<String, Object>> loadCustomerComplaintsView() throws SQLException {
        return loadView("vw_customer_complaints");
    }

    // ============================================
    // CUSTOMER REVIEW VIEWS
    // ============================================

    public List<Map<String, Object>> loadCustomerReviewsView() throws SQLException {
        return loadView("vw_customer_reviews");
    }

    // ============================================
    // NOTIFICATION VIEWS
    // ============================================

    public List<Map<String, Object>> loadNotificationsByUser(int userId) throws SQLException {
        return loadViewWithCondition("vw_notifications", "user_id = ? ORDER BY created_at DESC", userId);
    }

    public List<Map<String, Object>> loadUnreadNotifications(int userId) throws SQLException {
        return loadViewWithCondition("vw_notifications", "user_id = ? AND is_read = false ORDER BY created_at DESC", userId);
    }

    // ============================================
    // AUDIT LOG VIEWS
    // ============================================

    public List<Map<String, Object>> loadAuditLogs() throws SQLException {
        return loadView("vw_audit_logs");
    }

    public List<Map<String, Object>> loadAuditLogsByUser(int userId) throws SQLException {
        return loadViewWithCondition("vw_audit_logs", "user_id = ?", userId);
    }

    // ============================================
    // DASHBOARD STATS VIEWS
    // ============================================

    public Map<String, Object> loadDashboardStats() throws SQLException {
        return loadViewSingle("vw_dashboard_stats", null);
    }

    // ============================================
    // DOCUMENT VIEWS
    // ============================================

    public List<Map<String, Object>> loadExpiredDocuments() throws SQLException {
        return loadView("vw_expired_documents");
    }

    public List<Map<String, Object>> loadVehicleDocumentExpiry() throws SQLException {
        return loadView("vw_vehicle_document_expiry");
    }

    // ============================================
    // BOLO ALERT VIEWS
    // ============================================

    public List<Map<String, Object>> loadActiveBOLOAlerts() throws SQLException {
        return loadView("vw_active_bolo_alerts");
    }

    // ============================================
    // GEOFENCE VIEWS
    // ============================================

    public List<Map<String, Object>> loadGeofenceAlerts() {
        try {
            return loadView("vw_geofence_alerts");
        } catch (SQLException e) {
            System.err.println("Warning: vw_geofence_alerts view not found. Returning empty list.");
            return new ArrayList<>();
        }
    }

    // ============================================
    // RISK SCORE VIEWS
    // ============================================

    public List<Map<String, Object>> loadVehicleRiskScores() throws SQLException {
        return loadView("vw_vehicle_risk_score");
    }

    public List<Map<String, Object>> loadHighRiskVehicles() throws SQLException {
        return loadViewWithCondition("vw_vehicle_risk_score", "risk_level IN ('HIGH', 'CRITICAL')");
    }

    // ============================================
    // DIGITAL WALLET VIEWS
    // ============================================

    public Map<String, Object> loadDigitalWalletByCustomer(int customerId) throws SQLException {
        return loadViewSingle("vw_digital_wallet", "customer_id = ?", customerId);
    }

    // ============================================
    // NO CLAIM BONUS VIEWS
    // ============================================

    public List<Map<String, Object>> loadNoClaimBonus() throws SQLException {
        return loadView("vw_no_claim_bonus");
    }

    // ============================================
    // SERVICE REMINDER VIEWS
    // ============================================

    public List<Map<String, Object>> loadServiceReminders() throws SQLException {
        return loadView("vw_service_reminder");
    }

    // ============================================
    // PART INVENTORY VIEWS
    // ============================================

    public List<Map<String, Object>> loadPartInventory() throws SQLException {
        return loadView("vw_part_inventory");
    }

    public List<Map<String, Object>> loadPartInventoryByWorkshop(int workshopId) throws SQLException {
        return loadViewWithCondition("vw_part_inventory", "workshop_id = ?", workshopId);
    }

    public List<Map<String, Object>> loadLowStockParts() throws SQLException {
        return loadViewWithCondition("vw_part_inventory", "stock_status IN ('LOW_STOCK', 'OUT_OF_STOCK')");
    }

    // ============================================
    // DIGITAL INSPECTION VIEWS
    // ============================================

    public List<Map<String, Object>> loadDigitalInspections() throws SQLException {
        return loadView("vw_digital_inspection");
    }

    public List<Map<String, Object>> loadDigitalInspectionsByVehicle(int vehicleId) throws SQLException {
        return loadViewWithCondition("vw_digital_inspection", "vehicle_id = ?", vehicleId);
    }

    // ============================================
    // WAIT TIME ESTIMATOR VIEWS
    // ============================================

    public List<Map<String, Object>> loadWaitTimeEstimator() throws SQLException {
        return loadView("vw_wait_time_estimator");
    }

    // ============================================
    // SYSTEM HEALTH VIEWS
    // ============================================

    public Map<String, Object> loadSystemHealth() throws SQLException {
        return loadViewSingle("vw_system_health", null);
    }

    // ============================================
    // MONTHLY REGISTRATIONS VIEWS
    // ============================================

    public List<Map<String, Object>> loadMonthlyRegistrations() throws SQLException {
        return loadView("vw_monthly_registrations");
    }

    // ============================================
    // VEHICLE STATUS DISTRIBUTION VIEWS
    // ============================================

    public List<Map<String, Object>> loadVehicleStatusDistribution() throws SQLException {
        return loadView("vw_vehicle_status_distribution");
    }

    // ============================================
    // WORKSHOP PERFORMANCE VIEWS
    // ============================================

    public List<Map<String, Object>> loadWorkshopPerformance() throws SQLException {
        return loadView("vw_workshop_performance");
    }

    // ============================================
    // OFFICER LOG VIEWS
    // ============================================

    public List<Map<String, Object>> loadOfficerLogs() throws SQLException {
        return loadView("vw_officer_logs");
    }

    public List<Map<String, Object>> loadOfficerLogsByOfficer(String officerName) throws SQLException {
        return loadViewWithCondition("vw_officer_logs", "officer_name ILIKE ?", "%" + officerName + "%");
    }

    // ============================================
    // VEHICLE HISTORY VIEWS
    // ============================================

    public List<Map<String, Object>> loadVehicleHistory(int vehicleId) throws SQLException {
        return loadViewWithCondition("vw_vehicle_history", "vehicle_id = ?", vehicleId);
    }

    // ============================================
    // POLICY RENEWAL VIEWS
    // ============================================

    public List<Map<String, Object>> loadPolicyRenewals() throws SQLException {
        return loadView("vw_policy_renewals");
    }

    // ============================================
    // VEHICLE SIGHTINGS VIEWS
    // ============================================

    public List<Map<String, Object>> loadVehicleSightings(int vehicleId) throws SQLException {
        return loadViewWithCondition("vw_vehicle_sightings", "vehicle_id = ? ORDER BY timestamp", vehicleId);
    }

    // ============================================
    // ROLE PERMISSION VIEWS
    // ============================================

    public List<Map<String, Object>> loadRolePermissions() throws SQLException {
        return loadView("role_permissions");
    }

    public List<Map<String, Object>> loadRolePermissionsByRole(String roleName) throws SQLException {
        return loadViewWithCondition("role_permissions", "role_name = ?", roleName);
    }

    // ============================================
    // POLICE OFFICER VIEWS
    // ============================================

    public List<Map<String, Object>> loadPoliceOfficers() throws SQLException {
        return loadView("vw_police_officers");
    }

    // ============================================
    // POLICE DASHBOARD STATS VIEWS
    // ============================================

    public Map<String, Object> loadPoliceDashboardStats() throws SQLException {
        return loadViewSingle("vw_police_dashboard_stats", null);
    }

    // ============================================
    // RANK CHANGE REQUEST VIEWS
    // ============================================

    public List<Map<String, Object>> loadRankChangeRequests() throws SQLException {
        return loadView("vw_rank_change_requests");
    }

    // ============================================
    // INSURANCE VERIFICATION VIEWS
    // ============================================

    public List<Map<String, Object>> loadInsuranceVerifications() throws SQLException {
        return loadView("vw_insurance_verifications");
    }

    // ============================================
    // FINANCIAL TRANSACTIONS VIEWS
    // ============================================

    public List<Map<String, Object>> loadFinancialTransactions() throws SQLException {
        return loadView("vw_financial_transactions");
    }
}