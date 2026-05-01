package database;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

public class ProcedureCaller {

    private DatabaseConnection dbConnection;

    public ProcedureCaller() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    public CallableStatement prepareCall(String sql) throws SQLException {
        Connection conn = dbConnection.getConnection();
        return conn.prepareCall(sql);
    }

    /**
     * Builds a PostgreSQL CALL statement for a procedure.
     * PostgreSQL requires: CALL procedure_name(?, ?, ?)
     */
    private String buildPostgresCall(String procedureName, int paramCount) {
        StringBuilder sql = new StringBuilder("CALL ");
        sql.append(procedureName).append("(");
        for (int i = 0; i < paramCount; i++) {
            if (i > 0) sql.append(",");
            sql.append("?");
        }
        sql.append(")");
        return sql.toString();
    }

    public boolean executeProcedure(String procedureName, Object... params) throws SQLException {
        String sql = buildPostgresCall(procedureName, params.length);
        try (CallableStatement cs = prepareCall(sql)) {
            for (int i = 0; i < params.length; i++) {
                cs.setObject(i + 1, params[i]);
            }
            return cs.execute();
        }
    }

    public <T> T executeProcedureWithOutParameter(String procedureName, int outParamType, Object... params) throws SQLException {
        String sql = buildPostgresCall(procedureName, params.length + 1);
        try (CallableStatement cs = prepareCall(sql)) {
            for (int i = 0; i < params.length; i++) {
                cs.setObject(i + 1, params[i]);
            }
            cs.registerOutParameter(params.length + 1, outParamType);
            cs.execute();
            @SuppressWarnings("unchecked")
            T result = (T) cs.getObject(params.length + 1);
            return result;
        }
    }

    public Integer executeProcedureWithIntegerOut(String procedureName, Object... params) throws SQLException {
        return executeProcedureWithOutParameter(procedureName, Types.INTEGER, params);
    }

    public String executeProcedureWithStringOut(String procedureName, Object... params) throws SQLException {
        return executeProcedureWithOutParameter(procedureName, Types.VARCHAR, params);
    }

    // ============================================
    // USER MANAGEMENT PROCEDURES
    // ============================================

    public boolean executeCreateUser(String username, String password, String role, String fullName, String email) throws SQLException {
        return executeProcedure("sp_create_user", username, password, role, fullName, email);
    }

    public Integer executeCreateUserWithId(String username, String password, String role, String fullName, String email) throws SQLException {
        return executeProcedureWithIntegerOut("sp_create_user", username, password, role, fullName, email);
    }

    public boolean executeUpdateUser(int id, String username, String role, String fullName, String email, boolean isActive) throws SQLException {
        return executeProcedure("sp_update_user", id, username, role, fullName, email, isActive);
    }

    public boolean executeDeleteUser(int id) throws SQLException {
        return executeProcedure("sp_delete_user", id);
    }

    public boolean executeUpdateUserPassword(int userId, String newPassword) throws SQLException {
        return executeProcedure("sp_update_user_password", userId, newPassword);
    }

    // ============================================
    // VEHICLE MANAGEMENT PROCEDURES
    // ============================================

    public Integer executeRegisterVehicle(String registrationNumber, String make, String model, int year,
                                          int ownerId, int statusId, String color, String engineNumber, String chassisNumber) throws SQLException {
        return executeProcedureWithIntegerOut("sp_register_vehicle", registrationNumber, make, model, year, ownerId, statusId, color, engineNumber, chassisNumber);
    }

    public boolean executeUpdateVehicle(int id, String registrationNumber, String make, String model, int year,
                                        int ownerId, int statusId, String color, String engineNumber, String chassisNumber) throws SQLException {
        return executeProcedure("sp_update_vehicle", id, registrationNumber, make, model, year, ownerId, statusId, color, engineNumber, chassisNumber);
    }

    public boolean executeDeleteVehicle(int id) throws SQLException {
        return executeProcedure("sp_delete_vehicle", id);
    }

    public boolean executeUpdateVehicleStatus(int vehicleId, int statusId) throws SQLException {
        return executeProcedure("sp_update_vehicle_status", vehicleId, statusId);
    }

    // ============================================
    // VIOLATION PROCEDURES
    // ============================================

    public boolean executeAddViolation(int vehicleId, java.sql.Date violationDate, String violationType,
                                       double fineAmount, String location, String officerName, String paymentStatus) throws SQLException {
        return executeProcedure("sp_add_violation", vehicleId, violationDate, violationType, fineAmount, location, officerName, paymentStatus);
    }

    public boolean executeUpdateViolation(int id, int vehicleId, java.sql.Date violationDate, String violationType,
                                          double fineAmount, String location, String officerName, String paymentStatus) throws SQLException {
        return executeProcedure("sp_update_violation", id, vehicleId, violationDate, violationType, fineAmount, location, officerName, paymentStatus);
    }

    public boolean executeDeleteViolation(int id) throws SQLException {
        return executeProcedure("sp_delete_violation", id);
    }

    public boolean executeMarkViolationPaid(int violationId) throws SQLException {
        return executeProcedure("sp_mark_violation_paid", violationId);
    }

    // ============================================
    // STOLEN VEHICLE PROCEDURES
    // ============================================

    public boolean executeReportStolenVehicle(int vehicleId, String caseNumber, String officerName, String badgeNumber) throws SQLException {
        return executeProcedure("sp_report_stolen_vehicle", vehicleId, caseNumber, officerName, badgeNumber);
    }

    public boolean executeUpdateStolenStatus(int stolenVehicleId, String status) throws SQLException {
        return executeProcedure("sp_update_stolen_status", stolenVehicleId, status);
    }

    // ============================================
    // WARRANT PROCEDURES
    // ============================================

    public boolean executeIssueWarrant(int violationId, String judgeName, java.sql.Date issueDate, java.sql.Date expiryDate) throws SQLException {
        return executeProcedure("sp_issue_warrant", violationId, judgeName, issueDate, expiryDate);
    }

    // ============================================
    // BOLO ALERT PROCEDURES
    // ============================================

    public boolean executeGenerateBOLOAlert(int vehicleId, Integer stolenVehicleId, String message, String priority) throws SQLException {
        return executeProcedure("sp_generate_bolo_alert", vehicleId, stolenVehicleId, message, priority);
    }

    // ============================================
    // INSURANCE PROCEDURES
    // ============================================

    public Integer executeAddInsurancePolicy(int vehicleId, int providerId, String policyNumber, java.sql.Date startDate,
                                             java.sql.Date endDate, double premium, double coverageAmount, String status) throws SQLException {
        return executeProcedureWithIntegerOut("sp_add_insurance_policy", vehicleId, providerId, policyNumber, startDate, endDate, premium, coverageAmount, status);
    }

    public boolean executeSubmitInsuranceClaim(int policyId, double claimAmount, String description) throws SQLException {
        return executeProcedure("sp_submit_insurance_claim", policyId, claimAmount, description);
    }

    public boolean executeApproveClaim(int claimId, double approvedAmount) throws SQLException {
        return executeProcedure("sp_approve_claim", claimId, approvedAmount);
    }

    public boolean executeRejectClaim(int claimId, String rejectionReason) throws SQLException {
        return executeProcedure("sp_reject_claim", claimId, rejectionReason);
    }

    // ============================================
    // WORKSHOP PROCEDURES
    // ============================================

    public boolean executeRegisterWorkshop(int userId, String workshopName, String address, String phone, String email, String licenseNumber) throws SQLException {
        return executeProcedure("sp_register_workshop", userId, workshopName, address, phone, email, licenseNumber);
    }

    public boolean executeApproveWorkshop(int workshopId) throws SQLException {
        return executeProcedure("sp_approve_workshop", workshopId);
    }

    public boolean executeAddMechanic(int workshopId, String name, String specialization, String phone) throws SQLException {
        return executeProcedure("sp_add_mechanic", workshopId, name, specialization, phone);
    }

    public boolean executeUpdateMechanic(int id, int workshopId, String name, String specialization, String phone) throws SQLException {
        return executeProcedure("sp_update_mechanic", id, workshopId, name, specialization, phone);
    }

    public boolean executeDeleteMechanic(int id) throws SQLException {
        return executeProcedure("sp_delete_mechanic", id);
    }

    public boolean executeAddServiceRecord(int vehicleId, int workshopId, Integer mechanicId, java.sql.Date serviceDate,
                                           String serviceType, String description, double cost, int odometerReading) throws SQLException {
        return executeProcedure("sp_add_service_record", vehicleId, workshopId, mechanicId, serviceDate, serviceType, description, cost, odometerReading);
    }

    // ============================================
    // CUSTOMER PROCEDURES
    // ============================================

    public boolean executeCreateCustomer(int userId, String name, String address, String phone, String nationalId, String driversLicenseNumber) throws SQLException {
        return executeProcedure("sp_create_customer", userId, name, address, phone, nationalId, driversLicenseNumber);
    }

    public boolean executeUpdateCustomer(int id, int userId, String name, String address, String phone, String nationalId, String driversLicenseNumber) throws SQLException {
        return executeProcedure("sp_update_customer", id, userId, name, address, phone, nationalId, driversLicenseNumber);
    }

    public boolean executeDeleteCustomer(int id) throws SQLException {
        return executeProcedure("sp_delete_customer", id);
    }

    public boolean executeSubmitQuery(int customerId, int vehicleId, String queryText) throws SQLException {
        return executeProcedure("sp_submit_query", customerId, vehicleId, queryText);
    }

    public boolean executeRespondToQuery(int queryId, String responseText) throws SQLException {
        return executeProcedure("sp_respond_to_query", queryId, responseText);
    }

    public boolean executeSubmitComplaint(int customerId, int workshopId, String complaintText) throws SQLException {
        return executeProcedure("sp_submit_complaint", customerId, workshopId, complaintText);
    }

    public boolean executeUpdateComplaintStatus(int complaintId, String status) throws SQLException {
        return executeProcedure("sp_update_complaint_status", complaintId, status);
    }

    public boolean executeSubmitReview(int customerId, int workshopId, int rating, String reviewText) throws SQLException {
        return executeProcedure("sp_submit_review", customerId, workshopId, rating, reviewText);
    }

    // ============================================
    // DIGITAL WALLET PROCEDURES
    // ============================================

    public boolean executeCreateDigitalWallet(int customerId) throws SQLException {
        return executeProcedure("sp_create_digital_wallet", customerId);
    }

    public boolean executeAddWalletBalance(int customerId, double amount, String referenceId) throws SQLException {
        return executeProcedure("sp_add_wallet_balance", customerId, amount, referenceId);
    }

    // ============================================
    // NOTIFICATION PROCEDURES
    // ============================================

    public boolean executeSendNotification(int userId, String message, String type, int referenceId) throws SQLException {
        return executeProcedure("sp_send_notification", userId, message, type, referenceId);
    }

    public boolean executeMarkNotificationRead(int notificationId) throws SQLException {
        return executeProcedure("sp_mark_notification_read", notificationId);
    }

    // ============================================
    // AUDIT PROCEDURES
    // ============================================

    public boolean executeLogAuditAction(int userId, String action, String ipAddress) throws SQLException {
        return executeProcedure("sp_log_audit_action", userId, action, ipAddress);
    }

    // ============================================
    // INVENTORY PROCEDURES
    // ============================================

    public boolean executeAddPartToInventory(int workshopId, String partName, String partNumber, int quantity, int reorderLevel, double unitPrice) throws SQLException {
        return executeProcedure("sp_add_part_to_inventory", workshopId, partName, partNumber, quantity, reorderLevel, unitPrice);
    }

    public boolean executeUpdatePartQuantity(int partId, int quantityChange) throws SQLException {
        return executeProcedure("sp_update_part_quantity", partId, quantityChange);
    }

    // ============================================
    // INSPECTION PROCEDURES
    // ============================================

    public Integer executeStartDigitalInspection(int serviceRecordId, String inspectorName) throws SQLException {
        return executeProcedureWithIntegerOut("sp_start_digital_inspection", serviceRecordId, inspectorName);
    }

    public boolean executeCompleteInspection(int inspectionId, String overallCondition, String recommendations) throws SQLException {
        return executeProcedure("sp_complete_inspection", inspectionId, overallCondition, recommendations);
    }

    // ============================================
    // POLICY RENEWAL PROCEDURES
    // ============================================

    public boolean executeCreatePolicyRenewal(int insuranceId, java.sql.Date renewalDate, double premium) throws SQLException {
        return executeProcedure("sp_create_policy_renewal", insuranceId, renewalDate, premium);
    }

    public boolean executeProcessRenewalPayment(int renewalId) throws SQLException {
        return executeProcedure("sp_process_renewal_payment", renewalId);
    }

    // ============================================
    // RISK SCORE PROCEDURES
    // ============================================

    public boolean executeCalculateVehicleRiskScoreForVehicle(int vehicleId) throws SQLException {
        return executeProcedure("sp_calculate_vehicle_risk_score_for_vehicle", vehicleId);
    }

    public boolean executeCalculateAllVehicleRiskScores() throws SQLException {
        return executeProcedure("sp_calculate_vehicle_risk_score");
    }

    // ============================================
    // POLICE REPORT PROCEDURES
    // ============================================

    public boolean executeCreatePoliceReport(int vehicleId, java.sql.Date reportDate, String reportType,
                                             String description, String officerName, String badgeNumber, String caseNumber) throws SQLException {
        return executeProcedure("sp_create_police_report", vehicleId, reportDate, reportType, description, officerName, badgeNumber, caseNumber);
    }

    // ============================================
    // SERVICE REMINDER PROCEDURES
    // ============================================

    public boolean executeSendServiceReminders() throws SQLException {
        return executeProcedure("sp_send_service_reminders");
    }

    // ============================================
    // EXPIRED DOCUMENT PROCEDURES
    // ============================================

    public boolean executeDetectExpiredDocuments() throws SQLException {
        return executeProcedure("sp_detect_expired_documents");
    }

    // ============================================
    // POLICE UNIT PROCEDURES
    // ============================================

    public boolean executeUpdatePoliceUnitLocation(String unitId, double latitude, double longitude) throws SQLException {
        return executeProcedure("sp_update_police_unit_location", unitId, latitude, longitude);
    }

    // ============================================
    // GEOFENCE PROCEDURES
    // ============================================

    public boolean executeSendGeofenceAlert(int vehicleId, int zoneId, String alertType) throws SQLException {
        return executeProcedure("sp_send_geofence_alert", vehicleId, zoneId, alertType);
    }

    // ============================================
    // OFFICER LOG PROCEDURES
    // ============================================

    public boolean executeLogOfficerAction(String officerName, String badgeNumber, String action, Integer vehicleId) throws SQLException {
        return executeProcedure("sp_log_officer_action", officerName, badgeNumber, action, vehicleId);
    }

    // ============================================
    // NO CLAIM BONUS PROCEDURES
    // ============================================

    public boolean executeCalculateNoClaimBonus(int policyId) throws SQLException {
        return executeProcedure("sp_calculate_no_claim_bonus", policyId);
    }

    // ============================================
    // BULK IMPORT PROCEDURES
    // ============================================

    public boolean executeBulkImportCustomers(String customersJson) throws SQLException {
        return executeProcedure("sp_bulk_import_customers", customersJson);
    }
}