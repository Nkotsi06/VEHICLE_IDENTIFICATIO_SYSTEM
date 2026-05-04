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
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for loading data from database views.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class ViewLoader {

    private static final Logger LOGGER = Logger.getLogger(ViewLoader.class.getName());
    private DatabaseConnection dbConnection;

    // Simple cache for frequently accessed views
    private static final Map<String, List<Map<String, Object>>> cache = new ConcurrentHashMap<>();
    private static final long CACHE_TTL_MS = 60000; // 1 minute
    private static long lastCacheClear = System.currentTimeMillis();

    public ViewLoader() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    /**
     * Clears expired cache entries.
     */
    private static void clearExpiredCache() {
        long now = System.currentTimeMillis();
        if (now - lastCacheClear > CACHE_TTL_MS) {
            cache.clear();
            lastCacheClear = now;
        }
    }

    /**
     * Loads all data from a view.
     *
     * @param viewName the view name
     * @return list of rows as maps
     * @throws SQLException if query fails
     */
    public List<Map<String, Object>> loadView(String viewName) throws SQLException {
        return loadViewWithCondition(viewName, null, null);
    }

    /**
     * Loads data from a view with a WHERE condition.
     *
     * @param viewName  the view name
     * @param condition the WHERE clause (without the word WHERE)
     * @param params    parameters for the condition
     * @return list of rows as maps
     * @throws SQLException if query fails
     */
    public List<Map<String, Object>> loadViewWithCondition(String viewName, String condition, Object... params)
            throws SQLException {
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
            LOGGER.fine("Loaded " + results.size() + " rows from " + viewName);
            return results;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading view: " + viewName, e);
            throw e;
        } finally {
            dbConnection.closeResources(rs, ps, conn);
        }
    }

    /**
     * Loads a single row from a view.
     *
     * @param viewName  the view name
     * @param condition the WHERE clause
     * @param params    parameters
     * @return single row as map, or null if none
     * @throws SQLException if query fails
     */
    public Map<String, Object> loadViewSingle(String viewName, String condition, Object... params) throws SQLException {
        List<Map<String, Object>> results = loadViewWithCondition(viewName, condition, params);
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * Loads view with pagination.
     *
     * @param viewName the view name
     * @param limit    maximum rows
     * @param offset   offset
     * @return paginated results
     * @throws SQLException if query fails
     */
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

    /**
     * Counts rows in a view.
     *
     * @param viewName the view name
     * @return row count
     * @throws SQLException if query fails
     */
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

    /**
     * Counts rows in a view with a WHERE condition.
     *
     * @param viewName  the view name
     * @param condition the WHERE clause (without the word WHERE)
     * @param params    parameters for the condition
     * @return row count
     * @throws SQLException if query fails
     */
    public int countViewRowsWithCondition(String viewName, String condition, Object... params) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + viewName;
        if (condition != null && !condition.isEmpty()) {
            sql += " WHERE " + condition;
        }

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

    public List<Map<String, Object>> loadVehicleHistory(int vehicleId) throws SQLException {
        return loadViewWithCondition("vw_vehicle_history", "vehicle_id = ?", vehicleId);
    }

    public List<Map<String, Object>> loadVehicleDocuments(int vehicleId) throws SQLException {
        return loadViewWithCondition("vw_vehicle_documents", "vehicle_id = ?", vehicleId);
    }

    public List<Map<String, Object>> loadExpiredDocuments() throws SQLException {
        return loadView("vw_expired_documents");
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

    public Map<String, Object> loadCustomerById(int customerId) throws SQLException {
        return loadViewSingle("vw_customers", "id = ?", customerId);
    }

    // ============================================
    // VIOLATION VIEWS
    // ============================================

    public List<Map<String, Object>> loadViolationsView() throws SQLException {
        return loadView("vw_violations");
    }

    public List<Map<String, Object>> loadViolationsByVehicle(int vehicleId) throws SQLException {
        return loadViewWithCondition("vw_violations", "vehicle_id = ?", vehicleId);
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

    public List<Map<String, Object>> loadWarrants() throws SQLException {
        return loadView("vw_warrants");
    }

    public List<Map<String, Object>> loadActiveWarrants() throws SQLException {
        return loadView("vw_active_warrants");
    }

    public List<Map<String, Object>> loadWarrantsByVehicle(int vehicleId) throws SQLException {
        return loadViewWithCondition("vw_warrants", "vehicle_id = ?", vehicleId);
    }

    // ============================================
    // BOLO ALERT VIEWS
    // ============================================

    public List<Map<String, Object>> loadBOLOAlerts() throws SQLException {
        return loadView("vw_bolo_alerts");
    }

    public List<Map<String, Object>> loadActiveBOLOAlerts() throws SQLException {
        return loadView("vw_active_bolo_alerts");
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

    public List<Map<String, Object>> loadPendingWorkshops() throws SQLException {
        return loadViewWithCondition("vw_workshops", "is_approved = false");
    }

    // ============================================
    // MECHANIC VIEWS
    // ============================================

    public List<Map<String, Object>> loadMechanics() throws SQLException {
        return loadView("vw_mechanics");
    }

    public List<Map<String, Object>> loadMechanicsByWorkshop(int workshopId) throws SQLException {
        return loadViewWithCondition("vw_mechanics", "workshop_id = ?", workshopId);
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

    public List<Map<String, Object>> loadServiceRecordsByWorkshop(int workshopId) throws SQLException {
        return loadViewWithCondition("vw_service_records", "workshop_id = ?", workshopId);
    }

    // ============================================
    // INSURANCE VIEWS
    // ============================================

    public List<Map<String, Object>> loadInsurancePoliciesView() throws SQLException {
        return loadView("vw_insurance_policies");
    }

    public List<Map<String, Object>> loadActiveInsurancePolicies() throws SQLException {
        return loadViewWithCondition("vw_insurance_policies", "status = 'ACTIVE'");
    }

    public List<Map<String, Object>> loadInsuranceClaimsView() throws SQLException {
        return loadView("vw_insurance_claims");
    }

    public List<Map<String, Object>> loadPendingInsuranceClaims() throws SQLException {
        return loadView("vw_pending_insurance_claims");
    }

    public List<Map<String, Object>> loadInsuranceProviders() throws SQLException {
        return loadView("vw_insurance_providers");
    }

    public List<Map<String, Object>> loadActiveInsuranceProviders() throws SQLException {
        return loadView("vw_active_insurance_providers");
    }

    public List<Map<String, Object>> loadPolicyRenewals() throws SQLException {
        return loadView("vw_policy_renewals");
    }

    public List<Map<String, Object>> loadNoClaimBonus() throws SQLException {
        return loadView("vw_no_claim_bonus");
    }

    public List<Map<String, Object>> loadInsuranceVerifications() throws SQLException {
        return loadView("vw_insurance_verifications");
    }

    // ============================================
    // CUSTOMER SERVICE VIEWS
    // ============================================

    public List<Map<String, Object>> loadCustomerQueriesView() throws SQLException {
        return loadView("vw_customer_queries");
    }

    public List<Map<String, Object>> loadPendingQueries() throws SQLException {
        return loadView("vw_pending_queries");
    }

    public List<Map<String, Object>> loadCustomerComplaintsView() throws SQLException {
        return loadView("vw_customer_complaints");
    }

    public List<Map<String, Object>> loadPendingComplaints() throws SQLException {
        return loadView("vw_pending_complaints");
    }

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

    public Map<String, Object> loadPoliceDashboardStats() throws SQLException {
        return loadViewSingle("vw_police_dashboard_stats", null);
    }

    public Map<String, Object> loadSystemHealth() throws SQLException {
        return loadViewSingle("vw_system_health", null);
    }

    // ============================================
    // GEOFENCE VIEWS
    // ============================================

    public List<Map<String, Object>> loadGeofenceZones() throws SQLException {
        return loadView("vw_geofence_zones");
    }

    public List<Map<String, Object>> loadGeofenceAlerts() throws SQLException {
        return loadView("vw_geofence_alerts");
    }

    public List<Map<String, Object>> loadUnnotifiedGeofenceAlerts() throws SQLException {
        return loadView("vw_unnotified_geofence_alerts");
    }

    // ============================================
    // RISK SCORE VIEWS
    // ============================================

    public List<Map<String, Object>> loadVehicleRiskScores() throws SQLException {
        return loadView("vw_vehicle_risk_score");
    }

    public List<Map<String, Object>> loadHighRiskVehicles() throws SQLException {
        return loadViewWithCondition("vw_vehicle_risk_score", "risk_level IN ('CRITICAL', 'HIGH')");
    }

    // ============================================
    // DIGITAL WALLET VIEWS
    // ============================================

    public Map<String, Object> loadDigitalWalletByCustomer(int customerId) throws SQLException {
        return loadViewSingle("vw_digital_wallet", "customer_id = ?", customerId);
    }

    public List<Map<String, Object>> loadWalletTransactions(int walletId) throws SQLException {
        return loadViewWithCondition("vw_wallet_transactions", "wallet_id = ?", walletId);
    }

    // ============================================
    // SERVICE REMINDER VIEWS
    // ============================================

    public List<Map<String, Object>> loadServiceReminders() throws SQLException {
        return loadView("vw_service_reminder");
    }

    // ============================================
    // INVENTORY VIEWS
    // ============================================

    public List<Map<String, Object>> loadPartInventory() throws SQLException {
        return loadView("vw_parts_inventory");
    }

    public List<Map<String, Object>> loadPartInventoryByWorkshop(int workshopId) throws SQLException {
        return loadViewWithCondition("vw_parts_inventory", "workshop_id = ?", workshopId);
    }

    public List<Map<String, Object>> loadLowStockParts() throws SQLException {
        return loadView("vw_low_stock_inventory");
    }

    public List<Map<String, Object>> loadInventoryAlerts() throws SQLException {
        return loadView("vw_inventory_alerts");
    }

    // ============================================
    // WAIT TIME ESTIMATOR VIEWS
    // ============================================

    public List<Map<String, Object>> loadWaitTimeEstimator() throws SQLException {
        return loadView("vw_wait_time_estimator");
    }

    // ============================================
    // WORKSHOP PERFORMANCE VIEWS
    // ============================================

    public List<Map<String, Object>> loadWorkshopPerformance() throws SQLException {
        return loadView("vw_workshop_performance");
    }

    // ============================================
    // OFFICER VIEWS
    // ============================================

    public List<Map<String, Object>> loadPoliceOfficers() throws SQLException {
        return loadView("vw_police_officers");
    }

    public List<Map<String, Object>> loadOfficerLogs() throws SQLException {
        return loadView("vw_officer_logs");
    }

    public List<Map<String, Object>> loadOfficerActivityLog() throws SQLException {
        return loadView("vw_officer_activity_log");
    }

    public List<Map<String, Object>> loadRankChangeRequests() throws SQLException {
        return loadView("vw_rank_change_requests");
    }

    // ============================================
    // VEHICLE SIGHTING VIEWS
    // ============================================

    public List<Map<String, Object>> loadVehicleSightings(int vehicleId) throws SQLException {
        return loadViewWithCondition("vw_vehicle_sightings", "vehicle_id = ? ORDER BY timestamp DESC", vehicleId);
    }

    public List<Map<String, Object>> loadAllVehicleSightings() throws SQLException {
        return loadView("vw_vehicle_sightings");
    }

    // ============================================
    // POLICE UNIT VIEWS
    // ============================================

    public List<Map<String, Object>> loadPoliceUnits() throws SQLException {
        return loadView("vw_police_units");
    }

    // ============================================
    // VEHICLE MOVEMENT VIEWS
    // ============================================

    public List<Map<String, Object>> loadVehicleMovementRecords(int vehicleId) throws SQLException {
        return loadViewWithCondition("vw_vehicle_movement_records", "vehicle_id = ?", vehicleId);
    }

    public List<Map<String, Object>> loadSuspiciousVehicleMovements() throws SQLException {
        return loadView("vw_suspicious_vehicle_movements");
    }

    // ============================================
    // FINANCIAL VIEWS
    // ============================================

    public List<Map<String, Object>> loadFinancialTransactions() throws SQLException {
        return loadView("vw_financial_transactions");
    }

    public List<Map<String, Object>> loadRevenueSummary() throws SQLException {
        return loadView("vw_revenue_summary");
    }

    // ============================================
    // STATISTICS VIEWS
    // ============================================

    public List<Map<String, Object>> loadMonthlyRegistrations() throws SQLException {
        return loadView("vw_monthly_registrations");
    }

    public List<Map<String, Object>> loadVehicleStatusDistribution() throws SQLException {
        return loadView("vw_vehicle_status_distribution");
    }

    // ============================================
    // ROLE PERMISSION VIEWS
    // ============================================

    public List<Map<String, Object>> loadRolePermissions() throws SQLException {
        return loadView("vw_role_permissions");
    }

    public List<Map<String, Object>> loadRolePermissionsByRole(String roleName) throws SQLException {
        return loadViewWithCondition("vw_role_permissions", "role_name = ?", roleName);
    }

    // ============================================
    // DIGITAL INSPECTION VIEWS
    // ============================================

    public List<Map<String, Object>> loadDigitalInspections() throws SQLException {
        try {
            return loadView("vw_digital_inspections");
        } catch (SQLException e) {
            LOGGER.warning("vw_digital_inspections view not found. Returning empty list.");
            return new ArrayList<>();
        }
    }

    public List<Map<String, Object>> loadInspectionChecklistItems(int inspectionId) throws SQLException {
        return loadViewWithCondition("vw_inspection_checklist_items", "inspection_id = ?", inspectionId);
    }

    // ============================================
    // HELPER METHODS
    // ============================================

    /**
     * Clears the view cache.
     */
    public static void clearCache() {
        cache.clear();
        LOGGER.info("View cache cleared");
    }

    /**
     * Checks if a view exists.
     *
     * @param viewName the view name
     * @return true if view exists
     */
    public boolean viewExists(String viewName) {
        try {
            String sql = "SELECT 1 FROM information_schema.views WHERE table_name = ?";
            Connection conn = dbConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, viewName);
                ResultSet rs = ps.executeQuery();
                return rs.next();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error checking view existence: " + viewName, e);
            return false;
        }
    }
}