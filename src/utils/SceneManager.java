package utils;

import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.stage.Stage;

/**
 * Scene Manager for handling navigation between different views in the application.
 * Provides centralized control for switching between FXML views and managing scene transitions.
 * Implements Singleton pattern to ensure only one instance exists.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class SceneManager {

    private static final Logger LOGGER = Logger.getLogger(SceneManager.class.getName());

    // ============================================
    // SINGLETON INSTANCE
    // ============================================
    private static SceneManager instance;
    private RootManager rootManager;

    /**
     * Private constructor for Singleton pattern.
     * Initializes the RootManager instance.
     */
    private SceneManager() {
        rootManager = RootManager.getInstance();
    }

    /**
     * Gets the singleton instance of SceneManager.
     *
     * @return The SceneManager instance
     */
    public static synchronized SceneManager getInstance() {
        if (instance == null) {
            instance = new SceneManager();
        }
        return instance;
    }

    /**
     * Sets the primary stage for the application.
     *
     * @param stage The primary stage
     */
    public void setPrimaryStage(Stage stage) {
        if (stage == null) {
            throw new IllegalArgumentException("Primary stage cannot be null");
        }
        rootManager.setPrimaryStage(stage);
        LOGGER.info("Primary stage set in SceneManager");
    }

    // ============================================
    // AUTHENTICATION VIEWS
    // ============================================

    /**
     * Switches to the login view.
     */
    public void switchToLogin() {
        LOGGER.fine("Switching to Login view");
        rootManager.setRoot("LoginView.fxml", "Login");
    }

    /**
     * Switches to the forgot password view.
     */
    public void switchToForgotPassword() {
        LOGGER.fine("Switching to ForgotPassword view");
        rootManager.setRoot("ForgotPasswordView.fxml", "ForgotPassword");
    }

    /**
     * Switches to the welcome screen (FIRST SCREEN WHEN APP STARTS).
     * This screen introduces the system before login.
     */
    public void switchToWelcome() {
        LOGGER.fine("Switching to Welcome view");
        rootManager.setRoot("WelcomeView.fxml", "Welcome");
    }

    // ============================================
    // DASHBOARD VIEWS (Role-Based)
    // ============================================

    /**
     * Switches to the appropriate dashboard based on user role.
     */
    public void switchToDashboard() {
        String role = SessionManager.getInstance().getUserRole();

        // Debug output
        System.out.println("DEBUG SceneManager: User role = " + role);

        if (role == null) {
            LOGGER.warning("User role is null, switching to login");
            System.err.println("DEBUG SceneManager: Role is null, switching to login");
            switchToLogin();
            return;
        }

        LOGGER.info("Switching to dashboard for role: " + role);
        System.out.println("DEBUG SceneManager: Switching to dashboard for role: " + role);

        switch (role) {
            case "ADMIN":
                switchToAdminView();
                break;
            case "POLICE":
                switchToPoliceView();
                break;
            case "CUSTOMER":
                switchToCustomerView();
                break;
            case "WORKSHOP":
                switchToWorkshopView();
                break;
            case "INSURANCE":
                switchToInsuranceView();
                break;
            default:
                LOGGER.warning("Unknown role: " + role + ", switching to login");
                System.err.println("DEBUG SceneManager: Unknown role: " + role);
                switchToLogin();
                break;
        }
    }

    /**
     * Switches to admin dashboard view.
     */
    public void switchToAdminView() {
        System.out.println("DEBUG SceneManager: Loading AdminView");
        rootManager.setRoot("AdminView.fxml", "Admin");
    }

    /**
     * Switches to police dashboard view.
     */
    public void switchToPoliceView() {
        System.out.println("DEBUG SceneManager: Loading PoliceView");
        rootManager.setRoot("PoliceView.fxml", "Police");
    }

    /**
     * Switches to insurance dashboard view.
     */
    public void switchToInsuranceView() {
        System.out.println("DEBUG SceneManager: Loading InsuranceView");
        rootManager.setRoot("InsuranceView.fxml", "Insurance");
    }

    /**
     * Switches to workshop dashboard view.
     */
    public void switchToWorkshopView() {
        System.out.println("DEBUG SceneManager: Loading WorkshopView");
        rootManager.setRoot("WorkshopView.fxml", "Workshop");
    }

    /**
     * Switches to customer dashboard view.
     */
    public void switchToCustomerView() {
        System.out.println("DEBUG SceneManager: Loading CustomerView");
        rootManager.setRoot("CustomerView.fxml", "Customer");
    }

    // ============================================
    // VEHICLE MANAGEMENT VIEWS
    // ============================================

    public void switchToVehicleView() {
        rootManager.setRoot("VehicleView.fxml", "Vehicle");
    }

    public void switchToVehicleRegistrationWizard() {
        rootManager.setRoot("VehicleRegistrationWizardView.fxml", "VehicleRegistrationWizard");
    }

    public void switchToVehicleRegistrationWizardView() {
        rootManager.setRoot("VehicleRegistrationWizardView.fxml", "VehicleRegistrationWizard");
    }

    public void switchToVehicleHistory() {
        rootManager.setRoot("VehicleHistoryView.fxml", "VehicleHistory");
    }

    public void switchToVehicleStatus() {
        rootManager.setRoot("VehicleStatusView.fxml", "VehicleStatus");
    }

    // ============================================
    // POLICE MODULE VIEWS
    // ============================================

    public void switchToStolenVehicleView() {
        rootManager.setRoot("StolenVehicleView.fxml", "StolenVehicle");
    }

    public void switchToViolationView() {
        rootManager.setRoot("ViolationView.fxml", "Violation");
    }

    public void switchToWarrantView() {
        rootManager.setRoot("WarrantView.fxml", "Warrant");
    }

    public void switchToIncidentReportView() {
        rootManager.setRoot("IncidentReportView.fxml", "IncidentReport");
    }

    public void switchToBOLOView() {
        rootManager.setRoot("BOLOView.fxml", "BOLO");
    }

    public void switchToGeofencingView() {
        rootManager.setRoot("GeofencingView.fxml", "Geofencing");
    }

    public void switchToVehicleTrackingView() {
        rootManager.setRoot("VehicleTrackingView.fxml", "VehicleTracking");
    }

    public void switchToVehicleReconstructionView() {
        rootManager.setRoot("VehicleReconstructionView.fxml", "VehicleReconstruction");
    }

    public void switchToExpiredDocumentView() {
        rootManager.setRoot("ExpiredDocumentView.fxml", "ExpiredDocument");
    }

    public void switchToMobilePatrolView() {
        rootManager.setRoot("MobilePatrolView.fxml", "MobilePatrol");
    }

    public void switchToTrafficCameraView() {
        rootManager.setRoot("TrafficCameraView.fxml", "TrafficCamera");
    }

    public void switchToOfficerLogView() {
        rootManager.setRoot("OfficerLogView.fxml", "OfficerLog");
    }

    public void switchToPoliceProfileView() {
        rootManager.setRoot("PoliceProfileView.fxml", "PoliceProfile");
    }

    public void switchToPoliceReportView() {
        rootManager.setRoot("PoliceReportView.fxml", "Police Reports");
    }

    public void switchToPoliceExportView() {
        rootManager.setRoot("PoliceExportView.fxml", "PoliceExport");
    }

    public void switchToOCRScannerView() {
        rootManager.setRoot("OCRDocumentScannerView.fxml", "OCR Scanner");
    }

    // ============================================
    // CUSTOMER MODULE VIEWS
    // ============================================

    public void switchToCustomerProfileView() {
        rootManager.setRoot("CustomerProfileView.fxml", "CustomerProfile");
    }

    public void switchToCustomerQueryView() {
        rootManager.setRoot("CustomerQueryView.fxml", "CustomerQuery");
    }

    public void switchToCustomerComplaintView() {
        rootManager.setRoot("CustomerComplaintView.fxml", "CustomerComplaint");
    }

    public void switchToCustomerReviewView() {
        rootManager.setRoot("CustomerReviewView.fxml", "CustomerReview");
    }

    public void switchToCustomerVehicleView() {
        rootManager.setRoot("CustomerVehicleView.fxml", "CustomerVehicle");
    }

    public void switchToDigitalWalletView() {
        rootManager.setRoot("DigitalWalletView.fxml", "DigitalWallet");
    }

    public void switchToServiceHistoryGraphView() {
        rootManager.setRoot("ServiceHistoryGraphView.fxml", "ServiceHistoryGraph");
    }

    public void switchToDocumentExpiryCalendarView() {
        rootManager.setRoot("DocumentExpiryCalendarView.fxml", "DocumentExpiryCalendar");
    }

    // ============================================
    // WORKSHOP MODULE VIEWS
    // ============================================

    public void switchToWorkshopProfileView() {
        rootManager.setRoot("WorkshopProfileView.fxml", "WorkshopProfile");
    }

    public void switchToWorkshopRegistrationView() {
        rootManager.setRoot("WorkshopRegistrationView.fxml", "Workshop Registration");
    }

    public void switchToWorkshopServiceView() {
        rootManager.setRoot("WorkshopServiceView.fxml", "Workshop Services");
    }

    public void switchToMechanicView() {
        rootManager.setRoot("MechanicView.fxml", "Mechanic");
    }

    public void switchToServiceRecordView() {
        rootManager.setRoot("ServiceRecordView.fxml", "ServiceRecord");
    }

    public void switchToServiceReminderView() {
        rootManager.setRoot("ServiceReminderView.fxml", "ServiceReminder");
    }

    public void switchToPartInventoryView() {
        rootManager.setRoot("PartInventoryView.fxml", "PartInventory");
    }

    public void switchToDigitalInspectionView() {
        rootManager.setRoot("DigitalInspectionView.fxml", "DigitalInspection");
    }

    public void switchToWaitTimeEstimatorView() {
        rootManager.setRoot("WaitTimeEstimatorView.fxml", "WaitTimeEstimator");
    }

    public void switchToWorkshopRevenueView() {
        rootManager.setRoot("WorkshopRevenueView.fxml", "WorkshopRevenue");
    }

    public void switchToServiceAnalyticsView() {
        rootManager.setRoot("ServiceAnalyticsView.fxml", "ServiceAnalytics");
    }

    public void switchToQRCheckinView() {
        rootManager.setRoot("QRCheckinView.fxml", "QRCheckin");
    }

    public void switchToWorkshopReportView() {
        rootManager.setRoot("WorkshopReportView.fxml", "WorkshopReport");
    }

    // ============================================
    // INSURANCE MODULE VIEWS
    // ============================================

    public void switchToInsurancePolicyView() {
        rootManager.setRoot("InsurancePolicyView.fxml", "InsurancePolicy");
    }

    public void switchToInsuranceClaimView() {
        rootManager.setRoot("InsuranceClaimView.fxml", "InsuranceClaim");
    }

    public void switchToInsuranceClaimApprovalView() {
        rootManager.setRoot("InsuranceClaimApprovalView.fxml", "InsuranceClaimApproval");
    }

    public void switchToInsuranceComparisonView() {
        rootManager.setRoot("InsuranceComparisonView.fxml", "InsuranceComparison");
    }

    public void switchToInsuranceVerificationView() {
        rootManager.setRoot("InsuranceVerificationView.fxml", "InsuranceVerification");
    }

    public void switchToPolicyRenewalView() {
        rootManager.setRoot("PolicyRenewalView.fxml", "PolicyRenewal");
    }

    public void switchToRiskAssessmentView() {
        rootManager.setRoot("RiskAssessmentView.fxml", "RiskAssessment");
    }

    public void switchToNoClaimBonusView() {
        rootManager.setRoot("NoClaimBonusView.fxml", "NoClaimBonus");
    }

    public void switchToProviderComparisonView() {
        rootManager.setRoot("ProviderComparisonView.fxml", "ProviderComparison");
    }

    public void switchToInsuranceProvidersView() {
        rootManager.setRoot("InsuranceProvidersView.fxml", "InsuranceProviders");
    }

    public void switchToInsuranceProfileView() {
        rootManager.setRoot("InsuranceProfileView.fxml", "InsuranceProfile");
    }

    public void switchToInsuranceReportView() {
        rootManager.setRoot("InsuranceReportView.fxml", "InsuranceReport");
    }

    // ============================================
    // ADMIN MODULE VIEWS
    // ============================================

    public void switchToUserManagementView() {
        rootManager.setRoot("UserManagementView.fxml", "UserManagement");
    }

    public void switchToAuditLogView() {
        rootManager.setRoot("AuditLogView.fxml", "AuditLog");
    }

    public void switchToSystemHealthView() {
        rootManager.setRoot("SystemHealthView.fxml", "SystemHealth");
    }

    public void switchToRoleManagementView() {
        rootManager.setRoot("RoleManagementView.fxml", "RoleManagement");
    }

    public void switchToBulkOperationsView() {
        rootManager.setRoot("BulkOperationsView.fxml", "BulkOperations");
    }

    public void switchToReportScheduleView() {
        rootManager.setRoot("ReportScheduleView.fxml", "ReportSchedule");
    }

    public void switchToBackupView() {
        rootManager.setRoot("BackupView.fxml", "Backup");
    }

    public void switchToWorkshopApprovalView() {
        rootManager.setRoot("WorkshopApprovalView.fxml", "WorkshopApproval");
    }

    public void switchToDummyDataView() {
        rootManager.setRoot("DummyDataView.fxml", "Dummy Data");
    }

    public void switchToAdminProfileView() {
        rootManager.setRoot("AdminProfileView.fxml", "AdminProfile");
    }

    // ============================================
    // REPORTS AND GENERAL VIEWS
    // ============================================

    public void switchToReportView() {
        String role = SessionManager.getInstance().getUserRole();
        if ("POLICE".equals(role)) {
            switchToPoliceReportView();
        } else if ("INSURANCE".equals(role)) {
            switchToInsuranceReportView();
        } else if ("WORKSHOP".equals(role)) {
            switchToWorkshopReportView();
        } else {
            rootManager.setRoot("ReportView.fxml", "Report");
        }
    }

    public void switchToExportView() {
        switchToReportView();
    }

    public void switchToSearchView() {
        rootManager.setRoot("SearchView.fxml", "Search");
    }

    public void switchToSettingsView() {
        rootManager.setRoot("SettingsView.fxml", "Settings");
    }

    public void switchToNotificationView() {
        rootManager.setRoot("NotificationView.fxml", "Notification");
    }

    // ============================================
    // ADVANCED FEATURES VIEWS
    // ============================================

    public void switchToClaimStatusTrackerView() {
        rootManager.setRoot("ClaimStatusTrackerView.fxml", "ClaimStatusTracker");
    }

    public void switchToPredictiveAnalyticsView() {
        rootManager.setRoot("PredictiveAnalyticsView.fxml", "PredictiveAnalytics");
    }

    // ============================================
    // UTILITY METHODS
    // ============================================

    /**
     * Gets the primary stage of the application.
     *
     * @return The primary stage
     */
    public Stage getPrimaryStage() {
        return rootManager.getPrimaryStage();
    }

    /**
     * Gets the name of the current scene.
     *
     * @return Current scene name
     */
    public String getCurrentScene() {
        return rootManager.getCurrentScene();
    }

    /**
     * Gets the controller of the current scene.
     *
     * @return Current controller instance
     */
    public Object getCurrentController() {
        return rootManager.getCurrentController();
    }

    /**
     * Refreshes the current scene.
     */
    public void refreshCurrentScene() {
        rootManager.refreshCurrentScene();
    }

    /**
     * Preloads a scene for faster access.
     *
     * @param fxmlFile the FXML file path
     * @param sceneName the scene name
     * @return true if preload was successful
     */
    public boolean preloadScene(String fxmlFile, String sceneName) {
        return rootManager.preloadScene(fxmlFile, sceneName);
    }

    /**
     * Preloads common scenes for better performance.
     */
    public void preloadCommonScenes() {
        String role = SessionManager.getInstance().getUserRole();

        if (role != null) {
            switch (role) {
                case "ADMIN":
                    rootManager.preloadScene("UserManagementView.fxml", "UserManagement");
                    rootManager.preloadScene("AuditLogView.fxml", "AuditLog");
                    break;
                case "POLICE":
                    rootManager.preloadScene("StolenVehicleView.fxml", "StolenVehicle");
                    rootManager.preloadScene("ViolationView.fxml", "Violation");
                    break;
                case "CUSTOMER":
                    rootManager.preloadScene("CustomerProfileView.fxml", "CustomerProfile");
                    rootManager.preloadScene("CustomerVehicleView.fxml", "CustomerVehicle");
                    break;
                case "WORKSHOP":
                    rootManager.preloadScene("WorkshopProfileView.fxml", "WorkshopProfile");
                    rootManager.preloadScene("ServiceRecordView.fxml", "ServiceRecord");
                    break;
                case "INSURANCE":
                    rootManager.preloadScene("InsurancePolicyView.fxml", "InsurancePolicy");
                    rootManager.preloadScene("InsuranceClaimView.fxml", "InsuranceClaim");
                    break;
            }
        }

        LOGGER.info("Common scenes preloaded for role: " + role);
    }

    /**
     * Logs out the current user and returns to login screen.
     */
    public void logout() {
        LOGGER.info("Logging out user: " + SessionManager.getInstance().getUsername());
        SessionManager.getInstance().clearSession();
        switchToLogin();
    }

    /**
     * Checks if a user is logged in and session is valid.
     *
     * @return true if logged in and session not expired
     */
    public boolean isSessionValid() {
        return SessionManager.getInstance().isLoggedIn() && !SessionManager.getInstance().isSessionExpired();
    }
}