package database;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import models.GeofenceZone;
import models.InsuranceProvider;
import models.StolenVehicle;
import models.VehicleMovementRecord;

/**
 * Utility class for calling PostgreSQL stored procedures.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class ProcedureCaller {

    private static final Logger LOGGER = Logger.getLogger(ProcedureCaller.class.getName());
    private DatabaseConnection dbConnection;

    public ProcedureCaller() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    /**
     * Prepares a CallableStatement for a procedure.
     *
     * @param sql the CALL SQL statement
     * @return CallableStatement
     * @throws SQLException if preparation fails
     */
    public CallableStatement prepareCall(String sql) throws SQLException {
        Connection conn = dbConnection.getConnection();
        return conn.prepareCall(sql);
    }

    /**
     * Builds a PostgreSQL CALL statement.
     *
     * @param procedureName the procedure name
     * @param paramCount    number of parameters
     * @return CALL statement string
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

    /**
     * Executes a procedure with no return value.
     *
     * @param procedureName the procedure name
     * @param params        parameters
     * @return true if successful
     * @throws SQLException if execution fails
     */
    public boolean executeProcedure(String procedureName, Object... params) throws SQLException {
        String sql = buildPostgresCall(procedureName, params.length);
        try (CallableStatement cs = prepareCall(sql)) {
            for (int i = 0; i < params.length; i++) {
                cs.setObject(i + 1, params[i]);
            }
            boolean result = cs.execute();
            LOGGER.fine("Executed procedure: " + procedureName);
            return result;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to execute procedure: " + procedureName, e);
            throw e;
        }
    }

    /**
     * Executes a procedure with an OUT parameter and returns its value.
     *
     * @param <T>            the return type
     * @param procedureName  the procedure name
     * @param outParamType   the SQL type of the OUT parameter
     * @param params         input parameters (OUT parameter excluded)
     * @return the OUT parameter value
     * @throws SQLException if execution fails
     */
    @SuppressWarnings("unchecked")
    public <T> T executeProcedureWithOutParameter(String procedureName, int outParamType, Object... params)
            throws SQLException {
        String sql = buildPostgresCall(procedureName, params.length + 1);
        try (CallableStatement cs = prepareCall(sql)) {
            for (int i = 0; i < params.length; i++) {
                cs.setObject(i + 1, params[i]);
            }
            cs.registerOutParameter(params.length + 1, outParamType);
            cs.execute();
            T result = (T) cs.getObject(params.length + 1);
            LOGGER.fine("Executed procedure with OUT parameter: " + procedureName);
            return result;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to execute procedure with OUT: " + procedureName, e);
            throw e;
        }
    }

    /**
     * Executes procedure returning Integer OUT parameter.
     *
     * @param procedureName procedure name
     * @param params        parameters
     * @return Integer result
     * @throws SQLException if execution fails
     */
    public Integer executeProcedureWithIntegerOut(String procedureName, Object... params) throws SQLException {
        return executeProcedureWithOutParameter(procedureName, Types.INTEGER, params);
    }

    /**
     * Executes procedure returning String OUT parameter.
     *
     * @param procedureName procedure name
     * @param params        parameters
     * @return String result
     * @throws SQLException if execution fails
     */
    public String executeProcedureWithStringOut(String procedureName, Object... params) throws SQLException {
        return executeProcedureWithOutParameter(procedureName, Types.VARCHAR, params);
    }

    /**
     * Executes procedure returning Long OUT parameter.
     *
     * @param procedureName procedure name
     * @param params        parameters
     * @return Long result
     * @throws SQLException if execution fails
     */
    public Long executeProcedureWithLongOut(String procedureName, Object... params) throws SQLException {
        return executeProcedureWithOutParameter(procedureName, Types.BIGINT, params);
    }

    /**
     * Executes procedure returning Double OUT parameter.
     *
     * @param procedureName procedure name
     * @param params        parameters
     * @return Double result
     * @throws SQLException if execution fails
     */
    public Double executeProcedureWithDoubleOut(String procedureName, Object... params) throws SQLException {
        return executeProcedureWithOutParameter(procedureName, Types.DOUBLE, params);
    }

    /**
     * Executes procedure returning Boolean OUT parameter.
     *
     * @param procedureName procedure name
     * @param params        parameters
     * @return Boolean result
     * @throws SQLException if execution fails
     */
    public Boolean executeProcedureWithBooleanOut(String procedureName, Object... params) throws SQLException {
        return executeProcedureWithOutParameter(procedureName, Types.BOOLEAN, params);
    }

    /**
     * Executes procedure with transaction support.
     *
     * @param procedureName procedure name
     * @param params        parameters
     * @return true if successful
     * @throws SQLException if execution fails
     */
    public boolean executeProcedureInTransaction(String procedureName, Object... params) throws SQLException {
        try {
            dbConnection.beginTransaction();
            boolean result = executeProcedure(procedureName, params);
            dbConnection.commitTransaction();
            return result;
        } catch (SQLException e) {
            dbConnection.rollbackTransaction();
            throw e;
        }
    }

    // ============================================
    // USER MANAGEMENT PROCEDURES
    // ============================================

    public boolean executeCreateUser(String username, String password, String role, String fullName, String email)
            throws SQLException {
        return executeProcedure("sp_create_user", username, password, role, fullName, email);
    }

    public Integer executeCreateUserWithId(String username, String password, String role, String fullName, String email)
            throws SQLException {
        return executeProcedureWithIntegerOut("sp_create_user", username, password, role, fullName, email);
    }

    public boolean executeUpdateUser(int id, String username, String role, String fullName, String email, boolean isActive)
            throws SQLException {
        return executeProcedure("sp_update_user", id, username, role, fullName, email, isActive);
    }

    public boolean executeDeleteUser(int id) throws SQLException {
        return executeProcedure("sp_delete_user", id);
    }

    public boolean executeUpdateUserPassword(int userId, String newPassword) throws SQLException {
        return executeProcedure("sp_update_user_password", userId, newPassword);
    }

    public boolean executeUpdateUserPasswordByUsername(String username, String newPassword) throws SQLException {
        return executeProcedure("sp_update_user_password_by_username", username, newPassword);
    }

    public boolean executeUpdateUserLastLogin(int userId) throws SQLException {
        return executeProcedure("sp_update_user_last_login", userId);
    }

    public boolean executeUpdateUserProfileImage(int userId, String imagePath) throws SQLException {
        return executeProcedure("sp_update_user_profile_image", userId, imagePath);
    }

    public boolean executeToggleUserStatus(int userId, boolean isActive) throws SQLException {
        return executeProcedure("sp_toggle_user_status", userId, isActive);
    }

    // ============================================
    // CUSTOMER PROCEDURES
    // ============================================

    public Integer executeCreateCustomer(int userId, String name, String address, String phone,
                                         String nationalId, String driversLicenseNumber) throws SQLException {
        return executeProcedureWithIntegerOut("sp_create_customer", userId, name, address, phone, nationalId, driversLicenseNumber);
    }

    public boolean executeUpdateCustomer(int id, int userId, String name, String address, String phone,
                                         String nationalId, String driversLicenseNumber) throws SQLException {
        return executeProcedure("sp_update_customer", id, userId, name, address, phone, nationalId, driversLicenseNumber);
    }

    public boolean executeDeleteCustomer(int id) throws SQLException {
        return executeProcedure("sp_delete_customer", id);
    }

    // ============================================
    // VEHICLE MANAGEMENT PROCEDURES
    // ============================================

    public Integer executeRegisterVehicle(String registrationNumber, String make, String model, int year,
                                          int ownerId, int statusId, String color, String engineNumber, String chassisNumber)
            throws SQLException {
        return executeProcedureWithIntegerOut("sp_register_vehicle", registrationNumber, make, model, year,
                ownerId, statusId, color, engineNumber, chassisNumber);
    }

    public boolean executeUpdateVehicle(int id, String registrationNumber, String make, String model, int year,
                                        int ownerId, int statusId, String color, String engineNumber, String chassisNumber)
            throws SQLException {
        return executeProcedure("sp_update_vehicle", id, registrationNumber, make, model, year,
                ownerId, statusId, color, engineNumber, chassisNumber);
    }

    public boolean executeDeleteVehicle(int id) throws SQLException {
        return executeProcedure("sp_delete_vehicle", id);
    }

    public boolean executeUpdateVehicleStatus(int vehicleId, int statusId) throws SQLException {
        return executeProcedure("sp_update_vehicle_status", vehicleId, statusId);
    }

    public boolean executeUpdateVehicleLocation(int vehicleId, double latitude, double longitude) throws SQLException {
        return executeProcedure("sp_update_vehicle_location", vehicleId, latitude, longitude);
    }

    // ============================================
    // VIOLATION PROCEDURES
    // ============================================

    public Integer executeAddViolation(int vehicleId, Date violationDate, String violationType,
                                       double fineAmount, String location, String officerName,
                                       double latitude, double longitude) throws SQLException {
        return executeProcedureWithIntegerOut("sp_add_violation", vehicleId, violationDate, violationType,
                fineAmount, location, officerName, latitude, longitude);
    }

    public boolean executeUpdateViolation(int id, int vehicleId, Date violationDate, String violationType,
                                          double fineAmount, String location, String officerName, String paymentStatus)
            throws SQLException {
        return executeProcedure("sp_update_violation", id, vehicleId, violationDate, violationType,
                fineAmount, location, officerName, paymentStatus);
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

    public Integer executeReportStolenVehicle(int vehicleId, String caseNumber, String officerName,
                                              String badgeNumber, double latitude, double longitude, String description)
            throws SQLException {
        return executeProcedureWithIntegerOut("sp_report_stolen_vehicle", vehicleId, caseNumber, officerName,
                badgeNumber, latitude, longitude, description);
    }

    public boolean executeUpdateStolenStatus(int stolenVehicleId, String status) throws SQLException {
        return executeProcedure("sp_update_stolen_status", stolenVehicleId, status);
    }

    public boolean executeRecoverStolenVehicle(int stolenVehicleId, LocalDate recoveredDate) throws SQLException {
        return executeProcedure("sp_recover_stolen_vehicle", stolenVehicleId, Date.valueOf(recoveredDate));
    }

    public boolean executeDeleteStolenVehicle(int stolenVehicleId) throws SQLException {
        return executeProcedure("sp_delete_stolen_vehicle", stolenVehicleId);
    }

    // ============================================
    // WARRANT PROCEDURES
    // ============================================

    public Integer executeIssueWarrant(int violationId, String judgeName, LocalDate issueDate, LocalDate expiryDate)
            throws SQLException {
        return executeProcedureWithIntegerOut("sp_issue_warrant", violationId, judgeName,
                Date.valueOf(issueDate), Date.valueOf(expiryDate));
    }

    public boolean executeExecuteWarrant(int warrantId) throws SQLException {
        return executeProcedure("sp_execute_warrant", warrantId);
    }

    public boolean executeCancelWarrant(int warrantId) throws SQLException {
        return executeProcedure("sp_cancel_warrant", warrantId);
    }

    public boolean executeDeleteWarrant(int warrantId) throws SQLException {
        return executeProcedure("sp_delete_warrant", warrantId);
    }

    // ============================================
    // BOLO ALERT PROCEDURES
    // ============================================

    public Integer executeGenerateBOLOAlert(int vehicleId, String message, String priority, Integer referenceId)
            throws SQLException {
        return executeProcedureWithIntegerOut("sp_generate_bolo_alert", vehicleId, message, priority, referenceId);
    }

    public boolean executeCancelBOLOAlert(int alertId) throws SQLException {
        return executeProcedure("sp_cancel_bolo_alert", alertId);
    }

    public boolean executeDeleteBOLOAlert(int alertId) throws SQLException {
        return executeProcedure("sp_delete_bolo_alert", alertId);
    }

    // ============================================
    // INSURANCE PROCEDURES
    // ============================================

    public Integer executeAddInsurancePolicy(int vehicleId, int providerId, String policyNumber,
                                             LocalDate startDate, LocalDate endDate,
                                             double premium, double coverageAmount) throws SQLException {
        return executeProcedureWithIntegerOut("sp_add_insurance_policy", vehicleId, providerId, policyNumber,
                Date.valueOf(startDate), Date.valueOf(endDate), premium, coverageAmount);
    }

    public Integer executeSubmitInsuranceClaim(int policyId, double claimAmount, String description) throws SQLException {
        return executeProcedureWithIntegerOut("sp_submit_insurance_claim", policyId, claimAmount, description);
    }

    public boolean executeApproveClaim(int claimId, double approvedAmount) throws SQLException {
        return executeProcedure("sp_approve_claim", claimId, approvedAmount);
    }

    public boolean executeRejectClaim(int claimId, String rejectionReason) throws SQLException {
        return executeProcedure("sp_reject_claim", claimId, rejectionReason);
    }

    public boolean executeDeleteInsuranceClaim(int claimId) throws SQLException {
        return executeProcedure("sp_delete_insurance_claim", claimId);
    }

    public boolean executeCalculateNoClaimBonus(int policyId) throws SQLException {
        return executeProcedure("sp_calculate_no_claim_bonus", policyId);
    }

    public boolean executeProcessPolicyRenewal(int renewalId) throws SQLException {
        return executeProcedure("sp_process_policy_renewal", renewalId);
    }

    public boolean executeUpdateInsurancePolicy(int id, int vehicleId, int providerId, String policyNumber,
                                                LocalDate startDate, LocalDate endDate,
                                                double premium, double coverageAmount, String status)
            throws SQLException {
        return executeProcedure("sp_update_insurance_policy", id, vehicleId, providerId, policyNumber,
                Date.valueOf(startDate), Date.valueOf(endDate), premium, coverageAmount, status);
    }

    public boolean executeDeleteInsurancePolicy(int id) throws SQLException {
        return executeProcedure("sp_delete_insurance_policy", id);
    }

    // ============================================
    // INSURANCE PROVIDER PROCEDURES
    // ============================================

    public boolean executeInsertInsuranceProvider(int userId, String name, String registrationNumber,
                                                  String licenseNumber, String contactPhone, String contactEmail,
                                                  String address, Double rating, String coverageDetails, String status)
            throws SQLException {
        return executeProcedure("sp_insert_insurance_provider", userId, name, registrationNumber,
                licenseNumber, contactPhone, contactEmail, address, rating, coverageDetails, status);
    }

    public boolean executeUpdateInsuranceProvider(int id, String name, String registrationNumber,
                                                  String licenseNumber, String contactPhone, String contactEmail,
                                                  String address, Double rating, String coverageDetails, String status)
            throws SQLException {
        return executeProcedure("sp_update_insurance_provider", id, name, registrationNumber,
                licenseNumber, contactPhone, contactEmail, address, rating, coverageDetails, status);
    }

    public boolean executeUpdateInsuranceProviderStatus(int providerId, String status) throws SQLException {
        return executeProcedure("sp_update_insurance_provider_status", providerId, status);
    }

    public boolean executeDeleteInsuranceProvider(int id) throws SQLException {
        return executeProcedure("sp_delete_insurance_provider", id);
    }

    // ============================================
    // INSURANCE VERIFICATION PROCEDURES
    // ============================================

    public boolean executeInsertInsuranceVerification(int insuranceId, int verifiedBy, LocalDate verificationDate,
                                                      String verificationStatus, String notes) throws SQLException {
        return executeProcedure("sp_insert_insurance_verification", insuranceId, verifiedBy,
                Date.valueOf(verificationDate), verificationStatus, notes);
    }

    public boolean executeUpdateInsuranceVerification(int verificationId, String verificationStatus, String notes)
            throws SQLException {
        return executeProcedure("sp_update_insurance_verification", verificationId, verificationStatus, notes);
    }

    public boolean executeDeleteInsuranceVerification(int verificationId) throws SQLException {
        return executeProcedure("sp_delete_insurance_verification", verificationId);
    }

    public boolean executeVerifyInsurancePolicy(int insuranceId, int verifiedBy, String status, String notes)
            throws SQLException {
        return executeProcedure("sp_verify_insurance_policy", insuranceId, verifiedBy, status, notes);
    }

    // ============================================
    // INSURANCE PAYMENT PROCEDURES
    // ============================================

    public boolean executeRecordInsurancePayment(int insuranceId, double amount, LocalDate paymentDate,
                                                 LocalDate dueDate, double lateFee, String paymentMethod,
                                                 String receiptNumber) throws SQLException {
        return executeProcedure("sp_record_insurance_payment", insuranceId, amount,
                Date.valueOf(paymentDate), Date.valueOf(dueDate), lateFee, paymentMethod, receiptNumber);
    }

    public boolean executeUpdateInsurancePaymentStatus(int paymentId, String status) throws SQLException {
        return executeProcedure("sp_update_insurance_payment_status", paymentId, status);
    }

    public boolean executeCompleteInsurancePayment(int paymentId) throws SQLException {
        return executeProcedure("sp_complete_insurance_payment", paymentId);
    }

    public boolean executeFailInsurancePayment(int paymentId) throws SQLException {
        return executeProcedure("sp_fail_insurance_payment", paymentId);
    }

    public boolean executeDeleteInsurancePayment(int paymentId) throws SQLException {
        return executeProcedure("sp_delete_insurance_payment", paymentId);
    }

    // ============================================
    // INSURANCE DOCUMENT PROCEDURES
    // ============================================

    public Integer executeInsertInsuranceDocument(int insuranceId, String fileName, String filePath,
                                                  String documentType, long fileSize) throws SQLException {
        return executeProcedureWithIntegerOut("sp_insert_insurance_document", insuranceId, fileName,
                filePath, documentType, fileSize);
    }

    public Integer executeInsertInsuranceDocumentWithReturn(int insuranceId, String fileName, String filePath,
                                                            String documentType, long fileSize) throws SQLException {
        return executeProcedureWithIntegerOut("sp_insert_insurance_document", insuranceId, fileName,
                filePath, documentType, fileSize);
    }

    public boolean executeUpdateInsuranceDocument(int documentId, String fileName, String filePath,
                                                  String documentType, long fileSize) throws SQLException {
        return executeProcedure("sp_update_insurance_document", documentId, fileName, filePath, documentType, fileSize);
    }

    public boolean executeDeleteInsuranceDocument(int documentId) throws SQLException {
        return executeProcedure("sp_delete_insurance_document", documentId);
    }

    public boolean executeDeleteInsuranceDocumentsByInsurance(int insuranceId) throws SQLException {
        return executeProcedure("sp_delete_insurance_documents_by_insurance", insuranceId);
    }

    // ============================================
    // POLICY RENEWAL PROCEDURES
    // ============================================

    public boolean executeCreatePolicyRenewal(int insuranceId, LocalDate renewalDate, double premium) throws SQLException {
        return executeProcedure("sp_create_policy_renewal", insuranceId, Date.valueOf(renewalDate), premium);
    }

    public boolean executeProcessRenewalPayment(int renewalId) throws SQLException {
        return executeProcedure("sp_process_renewal_payment", renewalId);
    }

    public boolean executeDeletePolicyRenewal(int renewalId) throws SQLException {
        return executeProcedure("sp_delete_policy_renewal", renewalId);
    }

    // ============================================
    // NO CLAIM BONUS RECORD PROCEDURES
    // ============================================

    public boolean executeInsertNoClaimBonusRecord(int insurancePolicyId, int policyYear, int claimFreeYears,
                                                   double bonusPercentage, LocalDate calculatedDate) throws SQLException {
        return executeProcedure("sp_insert_no_claim_bonus_record", insurancePolicyId, policyYear,
                claimFreeYears, bonusPercentage, Date.valueOf(calculatedDate));
    }

    public boolean executeUpdateNoClaimBonusRecord(int insurancePolicyId, int policyYear, int claimFreeYears,
                                                   double bonusPercentage, LocalDate calculatedDate) throws SQLException {
        return executeProcedure("sp_update_no_claim_bonus_record", insurancePolicyId, policyYear,
                claimFreeYears, bonusPercentage, Date.valueOf(calculatedDate));
    }

    public boolean executeDeleteNoClaimBonusRecord(int recordId) throws SQLException {
        return executeProcedure("sp_delete_no_claim_bonus_record", recordId);
    }

    public boolean executeDeleteNoClaimBonusRecordsByPolicy(int policyId) throws SQLException {
        return executeProcedure("sp_delete_no_claim_bonus_records_by_policy", policyId);
    }

    // ============================================
    // WORKSHOP PROCEDURES
    // ============================================

    public Integer executeRegisterWorkshop(int userId, String workshopName, String address,
                                           String phone, String email, String licenseNumber) throws SQLException {
        return executeProcedureWithIntegerOut("sp_register_workshop", userId, workshopName, address,
                phone, email, licenseNumber);
    }

    public boolean executeApproveWorkshop(int workshopId) throws SQLException {
        return executeProcedure("sp_approve_workshop", workshopId);
    }

    public boolean executeUpdateWorkshop(int id, String workshopName, String address,
                                         String phone, String email, String licenseNumber) throws SQLException {
        return executeProcedure("sp_update_workshop", id, workshopName, address, phone, email, licenseNumber);
    }

    public boolean executeDeleteWorkshop(int workshopId) throws SQLException {
        return executeProcedure("sp_delete_workshop", workshopId);
    }

    // ============================================
    // MECHANIC PROCEDURES
    // ============================================

    public Integer executeAddMechanic(int workshopId, String name, String specialization, String phone) throws SQLException {
        return executeProcedureWithIntegerOut("sp_add_mechanic", workshopId, name, specialization, phone);
    }

    public boolean executeUpdateMechanic(int id, int workshopId, String name, String specialization, String phone)
            throws SQLException {
        return executeProcedure("sp_update_mechanic", id, workshopId, name, specialization, phone);
    }

    public boolean executeDeleteMechanic(int id) throws SQLException {
        return executeProcedure("sp_delete_mechanic", id);
    }

    // ============================================
    // SERVICE RECORD PROCEDURES
    // ============================================

    public Integer executeAddServiceRecord(int vehicleId, int workshopId, Integer mechanicId,
                                           LocalDate serviceDate, String serviceType,
                                           String description, double cost, int odometerReading) throws SQLException {
        return executeProcedureWithIntegerOut("sp_add_service_record", vehicleId, workshopId, mechanicId,
                Date.valueOf(serviceDate), serviceType, description, cost, odometerReading);
    }

    public boolean executeUpdateServiceRecord(int id, int vehicleId, int workshopId, Integer mechanicId,
                                              LocalDate serviceDate, String serviceType, String description,
                                              double cost, int odometerReading) throws SQLException {
        return executeProcedure("sp_update_service_record", id, vehicleId, workshopId, mechanicId,
                Date.valueOf(serviceDate), serviceType, description, cost, odometerReading);
    }

    public boolean executeDeleteServiceRecord(int id) throws SQLException {
        return executeProcedure("sp_delete_service_record", id);
    }

    // ============================================
    // CUSTOMER SERVICE PROCEDURES
    // ============================================

    public Integer executeSubmitQuery(int customerId, int vehicleId, String queryText) throws SQLException {
        return executeProcedureWithIntegerOut("sp_submit_query", customerId, vehicleId, queryText);
    }

    public boolean executeRespondToQuery(int queryId, String responseText) throws SQLException {
        return executeProcedure("sp_respond_to_query", queryId, responseText);
    }

    public boolean executeCloseQuery(int queryId) throws SQLException {
        return executeProcedure("sp_close_query", queryId);
    }

    public boolean executeDeleteQuery(int queryId) throws SQLException {
        return executeProcedure("sp_delete_query", queryId);
    }

    public Integer executeSubmitComplaint(int customerId, int workshopId, String complaintText) throws SQLException {
        return executeProcedureWithIntegerOut("sp_submit_complaint", customerId, workshopId, complaintText);
    }

    public boolean executeUpdateComplaintStatus(int complaintId, String status, String resolutionNotes) throws SQLException {
        return executeProcedure("sp_update_complaint_status", complaintId, status, resolutionNotes);
    }

    public boolean executeDeleteComplaint(int complaintId) throws SQLException {
        return executeProcedure("sp_delete_complaint", complaintId);
    }

    public Integer executeSubmitReview(int customerId, int workshopId, int rating, String reviewText) throws SQLException {
        return executeProcedureWithIntegerOut("sp_submit_review", customerId, workshopId, rating, reviewText);
    }

    public boolean executeUpdateReview(int reviewId, int rating, String reviewText) throws SQLException {
        return executeProcedure("sp_update_review", reviewId, rating, reviewText);
    }

    public boolean executeDeleteReview(int reviewId) throws SQLException {
        return executeProcedure("sp_delete_review", reviewId);
    }

    public boolean executeDeleteReviewsByWorkshop(int workshopId) throws SQLException {
        return executeProcedure("sp_delete_reviews_by_workshop", workshopId);
    }

    // ============================================
    // DIGITAL WALLET PROCEDURES
    // ============================================

    public boolean executeCreateDigitalWallet(int customerId) throws SQLException {
        return executeProcedure("sp_create_digital_wallet", customerId);
    }

    public boolean executeAddWalletBalance(int customerId, double amount, String referenceId, String description)
            throws SQLException {
        return executeProcedure("sp_add_wallet_balance", customerId, amount, referenceId, description);
    }

    public boolean executeDeductWalletBalance(int customerId, double amount, String referenceId, String description)
            throws SQLException {
        return executeProcedure("sp_deduct_wallet_balance", customerId, amount, referenceId, description);
    }

    public boolean executeDeleteDigitalWallet(int walletId) throws SQLException {
        return executeProcedure("sp_delete_digital_wallet", walletId);
    }

    // ============================================
    // WALLET TRANSACTION PROCEDURES
    // ============================================

    public boolean executeInsertWalletTransaction(int walletId, double amount, String transactionType,
                                                  String referenceId, String description, String status)
            throws SQLException {
        return executeProcedure("sp_insert_wallet_transaction", walletId, amount, transactionType,
                referenceId, description, status);
    }

    public boolean executeMarkWalletTransactionCompleted(int transactionId) throws SQLException {
        return executeProcedure("sp_mark_wallet_transaction_completed", transactionId);
    }

    public boolean executeMarkWalletTransactionFailed(int transactionId) throws SQLException {
        return executeProcedure("sp_mark_wallet_transaction_failed", transactionId);
    }

    public boolean executeDeleteWalletTransaction(int transactionId) throws SQLException {
        return executeProcedure("sp_delete_wallet_transaction", transactionId);
    }

    // ============================================
    // PAYMENT METHOD PROCEDURES
    // ============================================

    public Integer executeInsertPaymentMethod(int walletId, String cardLastFour, String cardType,
                                              int expiryMonth, int expiryYear, boolean isDefault) throws SQLException {
        return executeProcedureWithIntegerOut("sp_insert_payment_method", walletId, cardLastFour, cardType,
                expiryMonth, expiryYear, isDefault);
    }

    public boolean executeUpdatePaymentMethod(int methodId, String cardLastFour, String cardType,
                                              int expiryMonth, int expiryYear, boolean isDefault) throws SQLException {
        return executeProcedure("sp_update_payment_method", methodId, cardLastFour, cardType,
                expiryMonth, expiryYear, isDefault);
    }

    public boolean executeDeletePaymentMethod(int methodId) throws SQLException {
        return executeProcedure("sp_delete_payment_method", methodId);
    }

    public boolean executeDeletePaymentMethodsByWallet(int walletId) throws SQLException {
        return executeProcedure("sp_delete_payment_methods_by_wallet", walletId);
    }

    public boolean executeSetDefaultPaymentMethod(int paymentMethodId, int walletId) throws SQLException {
        return executeProcedure("sp_set_default_payment_method", paymentMethodId, walletId);
    }

    // ============================================
    // NOTIFICATION PROCEDURES
    // ============================================

    public boolean executeSendNotification(int userId, String message, String type, Integer referenceId) throws SQLException {
        return executeProcedure("sp_send_notification", userId, message, type, referenceId);
    }

    public boolean executeMarkNotificationRead(int notificationId) throws SQLException {
        return executeProcedure("sp_mark_notification_read", notificationId);
    }

    public boolean executeMarkAllNotificationsRead(int userId) throws SQLException {
        return executeProcedure("sp_mark_all_notifications_read", userId);
    }

    public boolean executeDeleteNotification(int notificationId) throws SQLException {
        return executeProcedure("sp_delete_notification", notificationId);
    }

    public boolean executeDeleteNotificationsByUser(int userId) throws SQLException {
        return executeProcedure("sp_delete_notifications_by_user", userId);
    }

    // ============================================
    // AUDIT PROCEDURES
    // ============================================

    public boolean executeLogAuditAction(int userId, String action, String ipAddress) throws SQLException {
        return executeProcedure("sp_log_audit_action", userId, action, ipAddress);
    }

    public boolean executeDeleteAuditLog(int auditLogId) throws SQLException {
        return executeProcedure("sp_delete_audit_log", auditLogId);
    }

    public boolean executeDeleteAuditLogsOlderThan(int days) throws SQLException {
        return executeProcedure("sp_delete_audit_logs_older_than", days);
    }

    public boolean executeDeleteAuditLogsBefore(Timestamp beforeDate) throws SQLException {
        return executeProcedure("sp_delete_audit_logs_before", beforeDate);
    }

    // ============================================
    // BULK IMPORT PROCEDURES
    // ============================================

    public int executeBulkImportCustomers(String jsonData) throws SQLException {
        return executeProcedureWithIntegerOut("sp_bulk_import_customers", jsonData);
    }

    // ============================================
    // INVENTORY PROCEDURES
    // ============================================

    public Integer executeAddPartToInventory(int workshopId, String partName, String partNumber,
                                             int quantity, int reorderLevel, double unitPrice) throws SQLException {
        return executeProcedureWithIntegerOut("sp_add_part_to_inventory", workshopId, partName, partNumber,
                quantity, reorderLevel, unitPrice);
    }

    public boolean executeUpdatePartInventory(int partId, String partName, int quantity,
                                              int reorderLevel, double unitPrice) throws SQLException {
        return executeProcedure("sp_update_part_inventory", partId, partName, quantity, reorderLevel, unitPrice);
    }

    public boolean executeUpdatePartQuantity(int partId, int quantityChange) throws SQLException {
        return executeProcedure("sp_update_part_quantity", partId, quantityChange);
    }

    public boolean executeDeletePartInventory(int partId) throws SQLException {
        return executeProcedure("sp_delete_part_inventory", partId);
    }

    // ============================================
    // INVENTORY ALERT PROCEDURES
    // ============================================

    public Integer executeInsertInventoryAlert(int partInventoryId, String alertType, String message) throws SQLException {
        return executeProcedureWithIntegerOut("sp_insert_inventory_alert", partInventoryId, alertType, message);
    }

    public boolean executeResolveInventoryAlert(int alertId) throws SQLException {
        return executeProcedure("sp_resolve_inventory_alert", alertId);
    }

    public boolean executeResolveInventoryAlertsByPart(int partInventoryId) throws SQLException {
        return executeProcedure("sp_resolve_inventory_alerts_by_part", partInventoryId);
    }

    public boolean executeDeleteInventoryAlert(int alertId) throws SQLException {
        return executeProcedure("sp_delete_inventory_alert", alertId);
    }

    public boolean executeDeleteResolvedInventoryAlerts() throws SQLException {
        return executeProcedure("sp_delete_resolved_inventory_alerts");
    }

    public boolean executeCheckInventoryAlerts(int workshopId) throws SQLException {
        return executeProcedure("sp_check_inventory_alerts", workshopId);
    }

    // ============================================
    // GEOFENCE PROCEDURES
    // ============================================

    public Integer executeCreateGeofenceZone(String zoneName, double centerLat, double centerLng,
                                             int radiusMeters, String zoneType, int priority) throws SQLException {
        return executeProcedureWithIntegerOut("sp_create_geofence_zone", zoneName, centerLat, centerLng,
                radiusMeters, zoneType, priority);
    }

    public boolean executeUpdateGeofenceZone(int zoneId, String zoneName, double centerLat, double centerLng,
                                             int radiusMeters, String zoneType, int priority, boolean isActive)
            throws SQLException {
        return executeProcedure("sp_update_geofence_zone", zoneId, zoneName, centerLat, centerLng,
                radiusMeters, zoneType, priority, isActive);
    }

    public boolean executeDeactivateGeofenceZone(int zoneId) throws SQLException {
        return executeProcedure("sp_deactivate_geofence_zone", zoneId);
    }

    public boolean executeDeleteGeofenceZone(int zoneId) throws SQLException {
        return executeProcedure("sp_delete_geofence_zone", zoneId);
    }

    public boolean executeSendGeofenceAlert(int vehicleId, int zoneId, String alertType) throws SQLException {
        return executeProcedure("sp_send_geofence_alert", vehicleId, zoneId, alertType);
    }

    public boolean executeMarkGeofenceAlertNotified(int eventId) throws SQLException {
        return executeProcedure("sp_mark_geofence_alert_notified", eventId);
    }

    public boolean executeDeleteGeofenceAlert(int alertId) throws SQLException {
        return executeProcedure("sp_delete_geofence_alert", alertId);
    }

    public Boolean executeIsPointInZone(int vehicleId, int zoneId) throws SQLException {
        return executeProcedureWithBooleanOut("sp_is_point_in_zone", vehicleId, zoneId);
    }

    // ============================================
    // VEHICLE SIGHTING PROCEDURES
    // ============================================

    public Integer executeAddVehicleSighting(String licensePlate, String sourceType, String sourceDeviceId,
                                             double latitude, double longitude, LocalDateTime timestamp,
                                             double confidenceScore) throws SQLException {
        return executeProcedureWithIntegerOut("sp_add_vehicle_sighting", licensePlate, sourceType, sourceDeviceId,
                latitude, longitude, Timestamp.valueOf(timestamp), confidenceScore);
    }

    public boolean executeDeleteVehicleSighting(int sightingId) throws SQLException {
        return executeProcedure("sp_delete_vehicle_sighting", sightingId);
    }

    // ============================================
    // POLICE OFFICER PROCEDURES
    // ============================================

    public Integer executeAddPoliceOfficer(int userId, String badgeNumber, String rank, String department,
                                           String stationAssigned, LocalDate hireDate,
                                           String supervisorName, String phone) throws SQLException {
        return executeProcedureWithIntegerOut("sp_add_police_officer", userId, badgeNumber, rank, department,
                stationAssigned, hireDate != null ? Date.valueOf(hireDate) : null,
                supervisorName, phone);
    }

    public boolean executeUpdatePoliceOfficer(int officerId, String rank, int rankLevel, String department,
                                              String stationAssigned, String supervisorName, String phone, String address)
            throws SQLException {
        return executeProcedure("sp_update_police_officer", officerId, rank, rankLevel, department,
                stationAssigned, supervisorName, phone, address);
    }

    public boolean executeUpdatePoliceOfficerRank(int officerId, String newRank, int newRankLevel) throws SQLException {
        return executeProcedure("sp_update_police_officer_rank", officerId, newRank, newRankLevel);
    }

    public boolean executeUpdatePoliceOfficerProfileImage(int officerId, String imagePath) throws SQLException {
        return executeProcedure("sp_update_police_officer_profile_image", officerId, imagePath);
    }

    public boolean executeClearPoliceOfficerProfileImage(int officerId) throws SQLException {
        return executeProcedure("sp_clear_police_officer_profile_image", officerId);
    }

    public boolean executeDeletePoliceOfficer(int officerId) throws SQLException {
        return executeProcedure("sp_delete_police_officer", officerId);
    }

    public boolean executeLogOfficerAction(String officerName, String badgeNumber, String action, Integer vehicleId)
            throws SQLException {
        return executeProcedure("sp_log_officer_action", officerName, badgeNumber, action, vehicleId);
    }

    // ============================================
    // POLICE UNIT PROCEDURES
    // ============================================

    public boolean executeRegisterPoliceUnit(String unitId, String officerName, String badgeNumber, String deviceId)
            throws SQLException {
        return executeProcedure("sp_register_police_unit", unitId, officerName, badgeNumber, deviceId);
    }

    public boolean executeUpdatePoliceUnitLocation(String unitId, double latitude, double longitude) throws SQLException {
        return executeProcedure("sp_update_police_unit_location", unitId, latitude, longitude);
    }

    public boolean executeUpdatePoliceUnitStatus(int unitId, String status) throws SQLException {
        return executeProcedure("sp_update_police_unit_status", unitId, status);
    }

    public boolean executeUpdatePoliceUnit(int unitId, String officerName, String badgeNumber,
                                           String status, String deviceId) throws SQLException {
        return executeProcedure("sp_update_police_unit", unitId, officerName, badgeNumber, status, deviceId);
    }

    public boolean executeDeletePoliceUnit(int unitId) throws SQLException {
        return executeProcedure("sp_delete_police_unit", unitId);
    }

    // ============================================
    // MOBILE PATROL SYNC PROCEDURES
    // ============================================

    public boolean executeQueueMobilePatrolSync(String unitId, String actionType, String actionData) throws SQLException {
        return executeProcedure("sp_queue_mobile_patrol_sync", unitId, actionType, actionData);
    }

    public boolean executeMarkMobilePatrolSyncCompleted(int syncId) throws SQLException {
        return executeProcedure("sp_mark_mobile_patrol_sync_completed", syncId);
    }

    public boolean executeSyncMobilePatrolUnit(String unitId) throws SQLException {
        return executeProcedure("sp_sync_mobile_patrol_unit", unitId);
    }

    public boolean executeSendBroadcastAlert(String message) throws SQLException {
        return executeProcedure("sp_send_broadcast_alert", message);
    }

    // ============================================
    // OFFICER ACTIVITY LOG PROCEDURES
    // ============================================

    public boolean executeInsertOfficerActivityLog(int officerId, String actionType, String actionDescription,
                                                   String targetType, Integer targetId, String ipAddress)
            throws SQLException {
        return executeProcedure("sp_insert_officer_activity_log", officerId, actionType, actionDescription,
                targetType, targetId, ipAddress);
    }

    public boolean executeDeleteOfficerActivityLog(int logId) throws SQLException {
        return executeProcedure("sp_delete_officer_activity_log", logId);
    }

    public int executeDeleteOldOfficerActivityLogs(LocalDateTime beforeDate) throws SQLException {
        return executeProcedureWithIntegerOut("sp_delete_old_officer_activity_logs",
                Timestamp.valueOf(beforeDate));
    }

    // ============================================
    // OFFICER LOG PROCEDURES
    // ============================================

    public boolean executeDeleteOfficerLog(int logId) throws SQLException {
        return executeProcedure("sp_delete_officer_log", logId);
    }

    public boolean executeDeleteOldOfficerLogs(LocalDateTime beforeDate) throws SQLException {
        return executeProcedure("sp_delete_old_officer_logs", Timestamp.valueOf(beforeDate));
    }

    // ============================================
    // RANK CHANGE REQUEST PROCEDURES
    // ============================================

    public boolean executeInsertRankChangeRequest(int officerId, String currentRank, String requestedRank, String reason)
            throws SQLException {
        return executeProcedure("sp_insert_rank_change_request", officerId, currentRank, requestedRank, reason);
    }

    public boolean executeApproveRankChangeRequest(int requestId, int reviewedBy, String reviewNotes)
            throws SQLException {
        return executeProcedure("sp_approve_rank_change_request", requestId, reviewedBy, reviewNotes);
    }

    public boolean executeRejectRankChangeRequest(int requestId, int reviewedBy, String reviewNotes)
            throws SQLException {
        return executeProcedure("sp_reject_rank_change_request", requestId, reviewedBy, reviewNotes);
    }

    public boolean executeDeleteRankChangeRequest(int requestId) throws SQLException {
        return executeProcedure("sp_delete_rank_change_request", requestId);
    }

    // ============================================
    // DIGITAL INSPECTION PROCEDURES
    // ============================================

    public Integer executeStartDigitalInspection(int serviceRecordId, String inspectorName) throws SQLException {
        return executeProcedureWithIntegerOut("sp_start_digital_inspection", serviceRecordId, inspectorName);
    }

    public boolean executeCompleteInspection(int inspectionId, String overallCondition, String recommendations)
            throws SQLException {
        return executeProcedure("sp_complete_inspection", inspectionId, overallCondition, recommendations);
    }

    public boolean executeAddInspectionChecklistItem(int inspectionId, String itemName, String status, String notes)
            throws SQLException {
        return executeProcedure("sp_add_inspection_checklist_item", inspectionId, itemName, status, notes);
    }

    public Integer executeAddInspectionChecklistItemWithId(int inspectionId, String itemName, String status,
                                                           String notes, String photoPath) throws SQLException {
        return executeProcedureWithIntegerOut("sp_add_inspection_checklist_item_with_photo", inspectionId, itemName, status, notes, photoPath);
    }

    public boolean executeUpdateInspectionChecklistItem(int itemId, String status, String notes, String photoPath)
            throws SQLException {
        return executeProcedure("sp_update_inspection_checklist_item", itemId, status, notes, photoPath);
    }

    public boolean executeDeleteInspectionChecklistItem(int itemId) throws SQLException {
        return executeProcedure("sp_delete_inspection_checklist_item", itemId);
    }

    public boolean executeDeleteInspectionChecklistItemsByInspection(int inspectionId) throws SQLException {
        return executeProcedure("sp_delete_inspection_checklist_items_by_inspection", inspectionId);
    }

    public boolean executeDeleteDigitalInspection(int inspectionId) throws SQLException {
        return executeProcedure("sp_delete_digital_inspection", inspectionId);
    }

    public boolean executeAddInspectionChecklistItemWithPhoto(int inspectionId, String itemName, String status,
                                                              String notes, String photoPath) throws SQLException {
        return executeProcedure("sp_add_inspection_checklist_item_with_photo", inspectionId, itemName, status, notes, photoPath);
    }

    // ============================================
    // VEHICLE RISK SCORE PROCEDURES
    // ============================================

    public boolean executeInsertVehicleRiskScore(int vehicleId, double riskScore, String riskFactors,
                                                 LocalDate lastCalculationDate) throws SQLException {
        return executeProcedure("sp_insert_vehicle_risk_score", vehicleId, riskScore, riskFactors,
                Date.valueOf(lastCalculationDate));
    }

    public boolean executeUpdateVehicleRiskScore(int vehicleId, double riskScore, String riskFactors,
                                                 LocalDate lastCalculationDate) throws SQLException {
        return executeProcedure("sp_update_vehicle_risk_score", vehicleId, riskScore, riskFactors,
                Date.valueOf(lastCalculationDate));
    }

    public boolean executeDeleteVehicleRiskScore(int scoreId) throws SQLException {
        return executeProcedure("sp_delete_vehicle_risk_score", scoreId);
    }

    public boolean executeDeleteVehicleRiskScoreByVehicle(int vehicleId) throws SQLException {
        return executeProcedure("sp_delete_vehicle_risk_score_by_vehicle", vehicleId);
    }

    // ============================================
    // VEHICLE DOCUMENT PROCEDURES
    // ============================================

    public Integer executeInsertVehicleDocument(int vehicleId, String documentType, String documentNumber,
                                                LocalDate issueDate, LocalDate expiryDate,
                                                String documentFilePath, String status) throws SQLException {
        return executeProcedureWithIntegerOut("sp_insert_vehicle_document", vehicleId, documentType, documentNumber,
                Date.valueOf(issueDate), Date.valueOf(expiryDate), documentFilePath, status);
    }

    public boolean executeUpdateVehicleDocument(int documentId, String documentNumber, LocalDate issueDate,
                                                LocalDate expiryDate, String documentFilePath, String status)
            throws SQLException {
        return executeProcedure("sp_update_vehicle_document", documentId, documentNumber,
                Date.valueOf(issueDate), Date.valueOf(expiryDate), documentFilePath, status);
    }

    public boolean executeDeleteVehicleDocument(int documentId) throws SQLException {
        return executeProcedure("sp_delete_vehicle_document", documentId);
    }

    public boolean executeDeleteVehicleDocumentsByVehicle(int vehicleId) throws SQLException {
        return executeProcedure("sp_delete_vehicle_documents_by_vehicle", vehicleId);
    }

    public boolean executeUpdateVehicleDocumentStatus(int documentId, String status) throws SQLException {
        return executeProcedure("sp_update_vehicle_document_status", documentId, status);
    }

    // ============================================
    // EXPIRED DOCUMENT PROCEDURES
    // ============================================

    public boolean executeCheckVehicleDocuments(String registrationNumber) throws SQLException {
        return executeProcedure("sp_check_vehicle_documents", registrationNumber);
    }

    public boolean executeDetectExpiredDocuments() throws SQLException {
        return executeProcedure("sp_detect_expired_documents");
    }

    public boolean executeGenerateViolationForExpiredDocuments(int vehicleId) throws SQLException {
        return executeProcedure("sp_generate_violation_for_expired_documents", vehicleId);
    }

    public boolean executeGenerateExpiredDocumentViolations() throws SQLException {
        return executeProcedure("sp_generate_expired_document_violations");
    }

    public boolean executeDeleteExpiredDocumentsByVehicle(int vehicleId) throws SQLException {
        return executeProcedure("sp_delete_expired_documents_by_vehicle", vehicleId);
    }

    public boolean executeDeleteExpiredDocumentAlert(int alertId) throws SQLException {
        return executeProcedure("sp_delete_expired_document_alert", alertId);
    }

    // ============================================
    // VEHICLE HISTORY PROCEDURES
    // ============================================

    public boolean executeInsertVehicleHistory(int vehicleId, String eventType, LocalDate eventDate,
                                               String description, String details) throws SQLException {
        return executeProcedure("sp_insert_vehicle_history", vehicleId, eventType,
                Date.valueOf(eventDate), description, details);
    }

    public boolean executeUpdateVehicleHistory(int historyId, String description, String details) throws SQLException {
        return executeProcedure("sp_update_vehicle_history", historyId, description, details);
    }

    public boolean executeDeleteVehicleHistory(int historyId) throws SQLException {
        return executeProcedure("sp_delete_vehicle_history", historyId);
    }

    public boolean executeDeleteVehicleHistoryByVehicle(int vehicleId) throws SQLException {
        return executeProcedure("sp_delete_vehicle_history_by_vehicle", vehicleId);
    }

    // ============================================
    // VEHICLE STATUS PROCEDURES
    // ============================================

    public boolean executeInsertVehicleStatus(String statusName, String description, String colorCode) throws SQLException {
        return executeProcedure("sp_insert_vehicle_status", statusName, description, colorCode);
    }

    public boolean executeUpdateVehicleStatus(int statusId, String statusName, String description, String colorCode)
            throws SQLException {
        return executeProcedure("sp_update_vehicle_status", statusId, statusName, description, colorCode);
    }

    public boolean executeDeleteVehicleStatus(int statusId) throws SQLException {
        return executeProcedure("sp_delete_vehicle_status", statusId);
    }

    // ============================================
    // PAYMENT PROCEDURES
    // ============================================

    public Integer executeProcessFinePayment(int violationId, double amount, String paymentMethod,
                                             String receiptNumber, LocalDate paymentDate) throws SQLException {
        return executeProcedureWithIntegerOut("sp_process_fine_payment", violationId, amount, paymentMethod,
                receiptNumber, Date.valueOf(paymentDate));
    }

    public boolean executeUpdatePayment(int paymentId, double amount, String paymentMethod) throws SQLException {
        return executeProcedure("sp_update_payment", paymentId, amount, paymentMethod);
    }

    public boolean executeDeletePayment(int paymentId) throws SQLException {
        return executeProcedure("sp_delete_payment", paymentId);
    }

    // ============================================
    // POLICE REPORT PROCEDURES
    // ============================================

    public Integer executeCreatePoliceReport(int vehicleId, LocalDate reportDate, String reportType,
                                             String description, String officerName, String badgeNumber,
                                             String caseNumber, String location, double latitude, double longitude)
            throws SQLException {
        return executeProcedureWithIntegerOut("sp_create_police_report", vehicleId,
                Date.valueOf(reportDate), reportType, description, officerName,
                badgeNumber, caseNumber, location, latitude, longitude);
    }

    public boolean executeUpdatePoliceReport(int reportId, String description) throws SQLException {
        return executeProcedure("sp_update_police_report", reportId, description);
    }

    public boolean executeDeletePoliceReport(int reportId) throws SQLException {
        return executeProcedure("sp_delete_police_report", reportId);
    }

    // ============================================
    // SYSTEM PROCEDURES
    // ============================================

    public boolean executeSendServiceReminders() throws SQLException {
        return executeProcedure("sp_send_service_reminders");
    }

    public boolean executeCalculateVehicleRiskScoreForVehicle(int vehicleId) throws SQLException {
        return executeProcedure("sp_calculate_vehicle_risk_score_for_vehicle", vehicleId);
    }

    public boolean executeCalculateAllVehicleRiskScores() throws SQLException {
        return executeProcedure("sp_calculate_all_vehicle_risk_scores");
    }

    // ============================================
    // SCHEDULED REPORTS PROCEDURES
    // ============================================

    public boolean executeScheduleReport(String reportName, String frequency, String recipientEmail) throws SQLException {
        return executeProcedure("sp_schedule_report", reportName, frequency, recipientEmail);
    }

    public boolean executeUnscheduleReport(String reportName) throws SQLException {
        return executeProcedure("sp_unschedule_report", reportName);
    }

    // ============================================
    // CRIME HOTSPOT PREDICTION PROCEDURES
    // ============================================

    public boolean executeRunCrimeHotspotPrediction() throws SQLException {
        return executeProcedure("sp_run_crime_hotspot_prediction");
    }

    public boolean executeInsertCrimeHotspotPrediction(LocalDate predictionDate, double centerLat,
                                                       double centerLng, int radiusMeters, String crimeType,
                                                       double probabilityScore, String riskLevel) throws SQLException {
        return executeProcedure("sp_insert_crime_hotspot_prediction", Date.valueOf(predictionDate),
                centerLat, centerLng, radiusMeters, crimeType, probabilityScore, riskLevel);
    }

    public boolean executeDeleteCrimeHotspotPrediction(int predictionId) throws SQLException {
        return executeProcedure("sp_delete_crime_hotspot_prediction", predictionId);
    }

    public boolean executeDeleteOldCrimeHotspotPredictions(LocalDate beforeDate) throws SQLException {
        return executeProcedure("sp_delete_old_crime_hotspot_predictions", Date.valueOf(beforeDate));
    }

    // ============================================
    // SERVICE SCHEDULE PROCEDURES
    // ============================================

    public boolean executeInsertServiceSchedule(int vehicleId, String serviceType, LocalDate dueDate,
                                                Integer dueOdometer, LocalDate lastServiceDate, Integer lastServiceOdometer)
            throws SQLException {
        return executeProcedure("sp_insert_service_schedule", vehicleId, serviceType,
                Date.valueOf(dueDate), dueOdometer,
                lastServiceDate != null ? Date.valueOf(lastServiceDate) : null,
                lastServiceOdometer);
    }

    public boolean executeUpdateServiceSchedule(int scheduleId, LocalDate dueDate, Integer dueOdometer,
                                                boolean reminderSent, LocalDate reminderSentDate)
            throws SQLException {
        return executeProcedure("sp_update_service_schedule", scheduleId, Date.valueOf(dueDate),
                dueOdometer, reminderSent, reminderSentDate != null ? Date.valueOf(reminderSentDate) : null);
    }

    public boolean executeMarkServiceReminderSent(int scheduleId) throws SQLException {
        return executeProcedure("sp_mark_service_reminder_sent", scheduleId);
    }

    public boolean executeDeleteServiceSchedule(int scheduleId) throws SQLException {
        return executeProcedure("sp_delete_service_schedule", scheduleId);
    }

    // ============================================
    // WORKSHOP PERFORMANCE PROCEDURES
    // ============================================

    public double executeGetSumInventoryValueByWorkshop(int workshopId) throws SQLException {
        return executeProcedureWithDoubleOut("sp_get_sum_inventory_value_by_workshop", workshopId);
    }

    public double executeGetSumServiceCostByWorkshop(int workshopId) throws SQLException {
        return executeProcedureWithDoubleOut("sp_get_sum_service_cost_by_workshop", workshopId);
    }

    public double executeGetAverageServiceCostByWorkshop(int workshopId) throws SQLException {
        return executeProcedureWithDoubleOut("sp_get_average_service_cost_by_workshop", workshopId);
    }

    public int executeCountDistinctVehiclesByWorkshopAndMonth(int workshopId, LocalDate month) throws SQLException {
        return executeProcedureWithIntegerOut("sp_count_distinct_vehicles_by_workshop_and_month",
                workshopId, Date.valueOf(month));
    }

    // ============================================
    // INSURANCE POLICY STATISTICS PROCEDURES
    // ============================================

    public double executeGetSumPremiumByProvider(int providerId) throws SQLException {
        return executeProcedureWithDoubleOut("sp_get_sum_premium_by_provider", providerId);
    }

    // ============================================
    // RISK PREMIUM CALCULATION PROCEDURE
    // ============================================

    /**
     * Executes the stored procedure to calculate risk premium for a vehicle.
     *
     * @param vehicleId the vehicle ID
     * @return the calculated risk premium amount
     * @throws SQLException if database error occurs
     */
    public double executeCalculateRiskPremium(int vehicleId) throws SQLException {
        return executeProcedureWithDoubleOut("sp_calculate_risk_premium", vehicleId);
    }

    // ============================================
    // ROLE PERMISSION PROCEDURES
    // ============================================

    /**
     * Executes the stored procedure to insert a role permission.
     *
     * @param roleName the role name
     * @param permissionKey the permission key
     * @param permissionValue the permission value (true/false)
     * @return true if successful, false otherwise
     * @throws SQLException if database error occurs
     */
    public boolean executeInsertRolePermission(String roleName, String permissionKey, boolean permissionValue) throws SQLException {
        return executeProcedure("sp_insert_role_permission", roleName, permissionKey, permissionValue);
    }

    /**
     * Executes the stored procedure to update a role permission.
     *
     * @param roleName the role name
     * @param permissionKey the permission key
     * @param permissionValue the permission value (true/false)
     * @return true if successful, false otherwise
     * @throws SQLException if database error occurs
     */
    public boolean executeUpdateRolePermission(String roleName, String permissionKey, boolean permissionValue) throws SQLException {
        return executeProcedure("sp_update_role_permission", roleName, permissionKey, permissionValue);
    }

    /**
     * Executes the stored procedure to delete a role permission by ID.
     *
     * @param id the permission ID
     * @return true if successful, false otherwise
     * @throws SQLException if database error occurs
     */
    public boolean executeDeleteRolePermission(int id) throws SQLException {
        return executeProcedure("sp_delete_role_permission", id);
    }

    /**
     * Executes the stored procedure to delete a role permission by role and permission key.
     *
     * @param roleName the role name
     * @param permissionKey the permission key
     * @return true if successful, false otherwise
     * @throws SQLException if database error occurs
     */
    public boolean executeDeleteRolePermissionByKey(String roleName, String permissionKey) throws SQLException {
        return executeProcedure("sp_delete_role_permission_by_key", roleName, permissionKey);
    }

    /**
     * Executes the stored procedure to grant a permission to a role.
     *
     * @param roleName the role name
     * @param permissionKey the permission key
     * @return true if successful, false otherwise
     * @throws SQLException if database error occurs
     */
    public boolean executeGrantPermission(String roleName, String permissionKey) throws SQLException {
        return executeProcedure("sp_grant_permission", roleName, permissionKey);
    }

    /**
     * Executes the stored procedure to revoke a permission from a role.
     *
     * @param roleName the role name
     * @param permissionKey the permission key
     * @return true if successful, false otherwise
     * @throws SQLException if database error occurs
     */
    public boolean executeRevokePermission(String roleName, String permissionKey) throws SQLException {
        return executeProcedure("sp_revoke_permission", roleName, permissionKey);
    }

    // ============================================
    // WORKSHOP DELETE SAMPLE REVIEWS PROCEDURE
    // ============================================

    public boolean executeDeleteWorkshopSampleReviews() throws SQLException {
        return executeProcedure("sp_delete_workshop_sample_reviews");
    }

    // ============================================
    // VEHICLE MOVEMENT PROCEDURES (Legacy - Not Implemented)
    // ============================================

    // These methods are kept for compatibility but throw UnsupportedOperationException
    // as they require complex result set handling

    public VehicleMovementRecord executeReconstructVehicleMovement(int vehicleId, LocalDate startDate, LocalDate endDate)
            throws SQLException {
        throw new UnsupportedOperationException("Reconstruct vehicle movement requires custom implementation");
    }

    public VehicleMovementRecord executeGetVehicleReconstructionWithMap(int vehicleId, LocalDate startDate, LocalDate endDate)
            throws SQLException {
        throw new UnsupportedOperationException("Get vehicle reconstruction with map requires custom implementation");
    }

    public List<StolenVehicle> executeFindNearbyStolenVehicles(double latitude, double longitude, double radiusKm)
            throws SQLException {
        throw new UnsupportedOperationException("Find nearby stolen vehicles requires custom implementation");
    }

    public List<GeofenceZone> executeFindZonesContainingPoint(double latitude, double longitude)
            throws SQLException {
        throw new UnsupportedOperationException("Find zones containing point requires custom implementation");
    }

    public List<InsuranceProvider> executeSearchInsuranceProviders(String keyword)
            throws SQLException {
        throw new UnsupportedOperationException("Search insurance providers requires custom implementation");
    }

    public List<Map<String, Object>> executeSearchStolenVehicles(String keyword)
            throws SQLException {
        throw new UnsupportedOperationException("Search stolen vehicles requires custom implementation");
    }

    public List<Map<String, Object>> executeSearchWorkshops(String keyword)
            throws SQLException {
        throw new UnsupportedOperationException("Search workshops requires custom implementation");
    }
}