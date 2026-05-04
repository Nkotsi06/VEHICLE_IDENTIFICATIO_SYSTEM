package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import utils.AlertUtil;
import utils.SceneManager;
import utils.SessionManager;
import dao.AuditDAO;

/**
 * Controller for the Application Menu Bar
 * Handles navigation between different modules based on user role
 * Dynamically shows/hides menu items based on user permissions
 */
public class MenuBarComponentController {

    // ============================================
    // FILE MENU COMPONENTS
    // ============================================
    @FXML private MenuItem exitMenuItem;

    // ============================================
    // MODULE MENUS (ROLE-SPECIFIC)
    // ============================================
    @FXML private Menu adminModuleMenu;
    @FXML private Menu policeModuleMenu;
    @FXML private Menu insuranceModuleMenu;
    @FXML private Menu workshopModuleMenu;
    @FXML private Menu customerModuleMenu;

    // ============================================
    // COMMON MENUS
    // ============================================
    @FXML private Menu reportsMenu;
    @FXML private Menu toolsMenu;
    @FXML private Menu helpMenu;
    @FXML private Menu accountMenu;

    // ============================================
    // POLICE MODULE MENU ITEMS
    // ============================================
    @FXML private MenuItem policeDashboardMenuItem;
    @FXML private MenuItem policeProfileMenuItem;
    @FXML private MenuItem stolenVehiclesMenuItem;
    @FXML private MenuItem violationsMenuItem;
    @FXML private MenuItem warrantsMenuItem;
    @FXML private MenuItem boloAlertsMenuItem;
    @FXML private MenuItem geofencingMenuItem;
    @FXML private MenuItem vehicleTrackingMenuItem;
    @FXML private MenuItem reconstructMovementMenuItem;
    @FXML private MenuItem expiredDocsMenuItem;
    @FXML private MenuItem trafficCameraIntegrationMenuItem;
    @FXML private MenuItem mobilePatrolMenuItem;
    @FXML private MenuItem incidentReportsMenuItem;
    @FXML private MenuItem officerLogsMenuItem;
    @FXML private MenuItem policeReportsMenuItem;
    @FXML private MenuItem policeExportMenuItem;
    @FXML private MenuItem ocrScannerMenuItem;
    @FXML private MenuItem globalSearchMenuItem;

    // ============================================
    // ADMIN MODULE MENU ITEMS
    // ============================================
    @FXML private MenuItem adminDashboardMenuItem;
    @FXML private MenuItem adminProfileMenuItem;
    @FXML private MenuItem userManagementMenuItem;
    @FXML private MenuItem auditLogsMenuItem;
    @FXML private MenuItem systemHealthMenuItem;
    @FXML private MenuItem roleManagementMenuItem;
    @FXML private MenuItem bulkOperationsMenuItem;
    @FXML private MenuItem reportScheduleMenuItem;
    @FXML private MenuItem backupMenuItem;
    @FXML private MenuItem workshopApprovalMenuItem;
    @FXML private MenuItem settingsMenuItem;
    @FXML private MenuItem dummyDataMenuItem;

    // ============================================
    // INSURANCE MODULE MENU ITEMS
    // ============================================
    @FXML private MenuItem insuranceDashboardMenuItem;
    @FXML private MenuItem insuranceProfileMenuItem;
    @FXML private MenuItem policiesMenuItem;
    @FXML private MenuItem claimsMenuItem;
    @FXML private MenuItem verifyInsuranceMenuItem;
    @FXML private MenuItem renewalMenuItem;
    @FXML private MenuItem providersMenuItem;
    @FXML private MenuItem compareProvidersMenuItem;
    @FXML private MenuItem noClaimBonusMenuItem;
    @FXML private MenuItem insuranceReportsMenuItem;
    @FXML private MenuItem insuranceNotificationsMenuItem;

    // ============================================
    // WORKSHOP MODULE MENU ITEMS
    // ============================================
    @FXML private MenuItem workshopDashboardMenuItem;
    @FXML private MenuItem workshopProfileMenuItem;
    @FXML private MenuItem mechanicsMenuItem;
    @FXML private MenuItem serviceRecordsMenuItem;
    @FXML private MenuItem inventoryMenuItem;
    @FXML private MenuItem registerVehicleMenuItem;
    @FXML private MenuItem qrCheckinMenuItem;
    @FXML private MenuItem digitalInspectionMenuItem;
    @FXML private MenuItem workshopRevenueMenuItem;
    @FXML private MenuItem workshopAnalyticsMenuItem;
    @FXML private MenuItem workshopReportsMenuItem;

    // ============================================
    // CUSTOMER MODULE MENU ITEMS
    // ============================================
    @FXML private MenuItem customerDashboardMenuItem;
    @FXML private MenuItem customerProfileMenuItem;
    @FXML private MenuItem myVehiclesMenuItem;
    @FXML private MenuItem myQueriesMenuItem;
    @FXML private MenuItem myComplaintsMenuItem;
    @FXML private MenuItem myReviewsMenuItem;
    @FXML private MenuItem myWalletMenuItem;
    @FXML private MenuItem myInsuranceMenuItem;
    @FXML private MenuItem serviceHistoryMenuItem;
    @FXML private MenuItem serviceRemindersMenuItem;
    @FXML private MenuItem customerNotificationsMenuItem;

    // ============================================
    // REPORTS MENU ITEMS
    // ============================================
    @FXML private MenuItem reportsMenuItem;
    @FXML private MenuItem exportMenuItem;

    // ============================================
    // TOOLS MENU ITEMS
    // ============================================
    @FXML private MenuItem searchMenuItem;

    // ============================================
    // HELP MENU ITEMS
    // ============================================
    @FXML private MenuItem helpMenuItem;
    @FXML private MenuItem aboutMenuItem;

    // ============================================
    // ACCOUNT MENU ITEMS
    // ============================================
    @FXML private MenuItem profileMenuItem;
    @FXML private MenuItem settingsMenuItemGlobal;
    @FXML private MenuItem accountNotificationsMenuItem;
    @FXML private MenuItem logoutMenuItem;

    // ============================================
    // DAO INSTANCES
    // ============================================
    private AuditDAO auditDAO;

    // ============================================
    // INITIALIZATION METHODS
    // ============================================

    /**
     * Initializes the menu bar controller
     * Sets up menu visibility based on user role and configures event handlers
     */
    @FXML
    public void initialize() {
        auditDAO = new AuditDAO();
        setupMenuVisibility();
        setupEventHandlers();
    }

    /**
     * Configures which menus are visible based on the logged-in user's role
     * Different roles see different module menus
     */
    private void setupMenuVisibility() {
        String role = SessionManager.getInstance().getUserRole();

        // First, hide all module menus
        hideAllModuleMenus();

        if (role == null || role.isEmpty()) {
            return;
        }

        // Show menus based on user role
        switch (role.toUpperCase()) {
            case "ADMIN":
                showAdminMenu();
                showAllRoleMenusForAdmin();  // Admin sees all modules
                // Settings only visible to ADMIN
                if (settingsMenuItem != null) settingsMenuItem.setVisible(true);
                if (settingsMenuItemGlobal != null) settingsMenuItemGlobal.setVisible(true);
                break;
            case "POLICE":
                showPoliceMenu();
                // Hide settings for non-admin roles
                if (settingsMenuItem != null) settingsMenuItem.setVisible(false);
                if (settingsMenuItemGlobal != null) settingsMenuItemGlobal.setVisible(false);
                break;
            case "INSURANCE":
                showInsuranceMenu();
                if (settingsMenuItem != null) settingsMenuItem.setVisible(false);
                if (settingsMenuItemGlobal != null) settingsMenuItemGlobal.setVisible(false);
                break;
            case "WORKSHOP":
                showWorkshopMenu();
                if (settingsMenuItem != null) settingsMenuItem.setVisible(false);
                if (settingsMenuItemGlobal != null) settingsMenuItemGlobal.setVisible(false);
                break;
            case "CUSTOMER":
                showCustomerMenu();
                if (settingsMenuItem != null) settingsMenuItem.setVisible(false);
                if (settingsMenuItemGlobal != null) settingsMenuItemGlobal.setVisible(false);
                break;
            default:
                break;
        }
    }

    /**
     * Hides all module-specific menus
     */
    private void hideAllModuleMenus() {
        if (adminModuleMenu != null) adminModuleMenu.setVisible(false);
        if (policeModuleMenu != null) policeModuleMenu.setVisible(false);
        if (insuranceModuleMenu != null) insuranceModuleMenu.setVisible(false);
        if (workshopModuleMenu != null) workshopModuleMenu.setVisible(false);
        if (customerModuleMenu != null) customerModuleMenu.setVisible(false);
    }

    /**
     * Shows admin module menu and all sub-items
     */
    private void showAdminMenu() {
        if (adminModuleMenu != null) {
            adminModuleMenu.setVisible(true);
        }
    }

    /**
     * Shows police module menu and all police-specific items
     */
    private void showPoliceMenu() {
        if (policeModuleMenu != null) {
            policeModuleMenu.setVisible(true);
        }
    }

    /**
     * Shows insurance module menu
     */
    private void showInsuranceMenu() {
        if (insuranceModuleMenu != null) {
            insuranceModuleMenu.setVisible(true);
        }
    }

    /**
     * Shows workshop module menu
     */
    private void showWorkshopMenu() {
        if (workshopModuleMenu != null) {
            workshopModuleMenu.setVisible(true);
        }
    }

    /**
     * Shows customer module menu
     */
    private void showCustomerMenu() {
        if (customerModuleMenu != null) {
            customerModuleMenu.setVisible(true);
        }
    }

    /**
     * For admin users, shows all other role menus for management purposes
     */
    private void showAllRoleMenusForAdmin() {
        if (policeModuleMenu != null) policeModuleMenu.setVisible(true);
        if (insuranceModuleMenu != null) insuranceModuleMenu.setVisible(true);
        if (workshopModuleMenu != null) workshopModuleMenu.setVisible(true);
        if (customerModuleMenu != null) customerModuleMenu.setVisible(true);
        if (toolsMenu != null) toolsMenu.setVisible(true);
    }

    // ============================================
    // EVENT HANDLER SETUP
    // ============================================

    /**
     * Sets up all menu item click event handlers
     */
    private void setupEventHandlers() {
        // Exit Application
        if (exitMenuItem != null) {
            exitMenuItem.setOnAction(event -> handleExit());
        }

        // ========== ADMIN MODULE EVENT HANDLERS ==========
        if (adminDashboardMenuItem != null) {
            adminDashboardMenuItem.setOnAction(event -> SceneManager.getInstance().switchToAdminView());
        }
        if (adminProfileMenuItem != null) {
            adminProfileMenuItem.setOnAction(event -> handleProfile());
        }
        if (userManagementMenuItem != null) {
            userManagementMenuItem.setOnAction(event -> SceneManager.getInstance().switchToUserManagementView());
        }
        if (auditLogsMenuItem != null) {
            auditLogsMenuItem.setOnAction(event -> SceneManager.getInstance().switchToAuditLogView());
        }
        if (systemHealthMenuItem != null) {
            systemHealthMenuItem.setOnAction(event -> SceneManager.getInstance().switchToSystemHealthView());
        }
        if (roleManagementMenuItem != null) {
            roleManagementMenuItem.setOnAction(event -> SceneManager.getInstance().switchToRoleManagementView());
        }
        if (bulkOperationsMenuItem != null) {
            bulkOperationsMenuItem.setOnAction(event -> SceneManager.getInstance().switchToBulkOperationsView());
        }
        if (reportScheduleMenuItem != null) {
            reportScheduleMenuItem.setOnAction(event -> SceneManager.getInstance().switchToReportScheduleView());
        }
        if (backupMenuItem != null) {
            backupMenuItem.setOnAction(event -> SceneManager.getInstance().switchToBackupView());
        }
        if (workshopApprovalMenuItem != null) {
            workshopApprovalMenuItem.setOnAction(event -> SceneManager.getInstance().switchToWorkshopApprovalView());
        }
        if (settingsMenuItem != null) {
            settingsMenuItem.setOnAction(event -> SceneManager.getInstance().switchToSettingsView());
        }
        if (dummyDataMenuItem != null) {
            dummyDataMenuItem.setOnAction(event -> SceneManager.getInstance().switchToDummyDataView());
        }

        // ========== POLICE MODULE EVENT HANDLERS ==========
        if (policeDashboardMenuItem != null) {
            policeDashboardMenuItem.setOnAction(event -> SceneManager.getInstance().switchToPoliceView());
        }
        if (policeProfileMenuItem != null) {
            policeProfileMenuItem.setOnAction(event -> SceneManager.getInstance().switchToPoliceProfileView());
        }
        if (stolenVehiclesMenuItem != null) {
            stolenVehiclesMenuItem.setOnAction(event -> SceneManager.getInstance().switchToStolenVehicleView());
        }
        if (violationsMenuItem != null) {
            violationsMenuItem.setOnAction(event -> SceneManager.getInstance().switchToViolationView());
        }
        if (warrantsMenuItem != null) {
            warrantsMenuItem.setOnAction(event -> SceneManager.getInstance().switchToWarrantView());
        }
        if (boloAlertsMenuItem != null) {
            boloAlertsMenuItem.setOnAction(event -> SceneManager.getInstance().switchToBOLOView());
        }
        if (geofencingMenuItem != null) {
            geofencingMenuItem.setOnAction(event -> SceneManager.getInstance().switchToGeofencingView());
        }
        if (vehicleTrackingMenuItem != null) {
            vehicleTrackingMenuItem.setOnAction(event -> SceneManager.getInstance().switchToVehicleTrackingView());
        }
        if (reconstructMovementMenuItem != null) {
            reconstructMovementMenuItem.setOnAction(event -> SceneManager.getInstance().switchToVehicleReconstructionView());
        }
        if (expiredDocsMenuItem != null) {
            expiredDocsMenuItem.setOnAction(event -> SceneManager.getInstance().switchToExpiredDocumentView());
        }
        if (trafficCameraIntegrationMenuItem != null) {
            trafficCameraIntegrationMenuItem.setOnAction(event -> SceneManager.getInstance().switchToTrafficCameraView());
        }
        if (mobilePatrolMenuItem != null) {
            mobilePatrolMenuItem.setOnAction(event -> SceneManager.getInstance().switchToMobilePatrolView());
        }
        if (incidentReportsMenuItem != null) {
            incidentReportsMenuItem.setOnAction(event -> SceneManager.getInstance().switchToIncidentReportView());
        }
        if (officerLogsMenuItem != null) {
            officerLogsMenuItem.setOnAction(event -> SceneManager.getInstance().switchToOfficerLogView());
        }
        if (policeReportsMenuItem != null) {
            policeReportsMenuItem.setOnAction(event -> SceneManager.getInstance().switchToPoliceReportView());
        }
        if (policeExportMenuItem != null) {
            policeExportMenuItem.setOnAction(event -> SceneManager.getInstance().switchToPoliceExportView());
        }
        if (ocrScannerMenuItem != null) {
            ocrScannerMenuItem.setOnAction(event -> SceneManager.getInstance().switchToOCRScannerView());
        }
        if (globalSearchMenuItem != null) {
            globalSearchMenuItem.setOnAction(event -> SceneManager.getInstance().switchToSearchView());
        }

        // ========== INSURANCE MODULE EVENT HANDLERS ==========
        if (insuranceDashboardMenuItem != null) {
            insuranceDashboardMenuItem.setOnAction(event -> SceneManager.getInstance().switchToInsuranceView());
        }
        if (insuranceProfileMenuItem != null) {
            insuranceProfileMenuItem.setOnAction(event -> SceneManager.getInstance().switchToInsuranceProfileView());
        }
        if (policiesMenuItem != null) {
            policiesMenuItem.setOnAction(event -> SceneManager.getInstance().switchToInsurancePolicyView());
        }
        if (claimsMenuItem != null) {
            claimsMenuItem.setOnAction(event -> SceneManager.getInstance().switchToInsuranceClaimView());
        }
        if (verifyInsuranceMenuItem != null) {
            verifyInsuranceMenuItem.setOnAction(event -> SceneManager.getInstance().switchToInsuranceVerificationView());
        }
        if (renewalMenuItem != null) {
            renewalMenuItem.setOnAction(event -> SceneManager.getInstance().switchToPolicyRenewalView());
        }
        if (providersMenuItem != null) {
            providersMenuItem.setOnAction(event -> SceneManager.getInstance().switchToInsuranceProvidersView());
        }
        if (compareProvidersMenuItem != null) {
            compareProvidersMenuItem.setOnAction(event -> SceneManager.getInstance().switchToProviderComparisonView());
        }
        if (noClaimBonusMenuItem != null) {
            noClaimBonusMenuItem.setOnAction(event -> SceneManager.getInstance().switchToNoClaimBonusView());
        }
        if (insuranceReportsMenuItem != null) {
            insuranceReportsMenuItem.setOnAction(event -> SceneManager.getInstance().switchToInsuranceReportView());
        }
        if (insuranceNotificationsMenuItem != null) {
            insuranceNotificationsMenuItem.setOnAction(event -> SceneManager.getInstance().switchToNotificationView());
        }

        // ========== WORKSHOP MODULE EVENT HANDLERS ==========
        if (workshopDashboardMenuItem != null) {
            workshopDashboardMenuItem.setOnAction(event -> SceneManager.getInstance().switchToWorkshopView());
        }
        if (workshopProfileMenuItem != null) {
            workshopProfileMenuItem.setOnAction(event -> SceneManager.getInstance().switchToWorkshopProfileView());
        }
        if (mechanicsMenuItem != null) {
            mechanicsMenuItem.setOnAction(event -> SceneManager.getInstance().switchToMechanicView());
        }
        if (serviceRecordsMenuItem != null) {
            serviceRecordsMenuItem.setOnAction(event -> SceneManager.getInstance().switchToServiceRecordView());
        }
        if (inventoryMenuItem != null) {
            inventoryMenuItem.setOnAction(event -> SceneManager.getInstance().switchToPartInventoryView());
        }
        if (registerVehicleMenuItem != null) {
            registerVehicleMenuItem.setOnAction(event -> SceneManager.getInstance().switchToVehicleRegistrationWizard());
        }
        if (qrCheckinMenuItem != null) {
            qrCheckinMenuItem.setOnAction(event -> SceneManager.getInstance().switchToQRCheckinView());
        }
        if (digitalInspectionMenuItem != null) {
            digitalInspectionMenuItem.setOnAction(event -> SceneManager.getInstance().switchToDigitalInspectionView());
        }
        if (workshopRevenueMenuItem != null) {
            workshopRevenueMenuItem.setOnAction(event -> SceneManager.getInstance().switchToWorkshopRevenueView());
        }
        if (workshopAnalyticsMenuItem != null) {
            workshopAnalyticsMenuItem.setOnAction(event -> SceneManager.getInstance().switchToServiceAnalyticsView());
        }
        if (workshopReportsMenuItem != null) {
            workshopReportsMenuItem.setOnAction(event -> SceneManager.getInstance().switchToWorkshopReportView());
        }

        // ========== CUSTOMER MODULE EVENT HANDLERS ==========
        if (customerDashboardMenuItem != null) {
            customerDashboardMenuItem.setOnAction(event -> SceneManager.getInstance().switchToCustomerView());
        }
        if (customerProfileMenuItem != null) {
            customerProfileMenuItem.setOnAction(event -> handleProfile());
        }
        if (myVehiclesMenuItem != null) {
            myVehiclesMenuItem.setOnAction(event -> SceneManager.getInstance().switchToCustomerVehicleView());
        }
        if (myQueriesMenuItem != null) {
            myQueriesMenuItem.setOnAction(event -> SceneManager.getInstance().switchToCustomerQueryView());
        }
        if (myComplaintsMenuItem != null) {
            myComplaintsMenuItem.setOnAction(event -> SceneManager.getInstance().switchToCustomerComplaintView());
        }
        if (myReviewsMenuItem != null) {
            myReviewsMenuItem.setOnAction(event -> SceneManager.getInstance().switchToCustomerReviewView());
        }
        if (myWalletMenuItem != null) {
            myWalletMenuItem.setOnAction(event -> SceneManager.getInstance().switchToDigitalWalletView());
        }
        if (myInsuranceMenuItem != null) {
            myInsuranceMenuItem.setOnAction(event -> SceneManager.getInstance().switchToInsurancePolicyView());
        }
        if (serviceHistoryMenuItem != null) {
            serviceHistoryMenuItem.setOnAction(event -> SceneManager.getInstance().switchToServiceHistoryGraphView());
        }
        if (serviceRemindersMenuItem != null) {
            serviceRemindersMenuItem.setOnAction(event -> SceneManager.getInstance().switchToServiceReminderView());
        }
        if (customerNotificationsMenuItem != null) {
            customerNotificationsMenuItem.setOnAction(event -> SceneManager.getInstance().switchToNotificationView());
        }

        // ========== REPORTS MENU EVENT HANDLERS ==========
        if (reportsMenuItem != null) {
            reportsMenuItem.setOnAction(event -> switchToRoleBasedReports());
        }
        if (exportMenuItem != null) {
            exportMenuItem.setOnAction(event -> switchToRoleBasedExport());
        }

        // ========== TOOLS MENU EVENT HANDLERS ==========
        if (searchMenuItem != null) {
            searchMenuItem.setOnAction(event -> SceneManager.getInstance().switchToSearchView());
        }

        // ========== HELP MENU EVENT HANDLERS ==========
        if (helpMenuItem != null) {
            helpMenuItem.setOnAction(event -> showHelpDialog());
        }
        if (aboutMenuItem != null) {
            aboutMenuItem.setOnAction(event -> showAboutDialog());
        }

        // ========== ACCOUNT MENU EVENT HANDLERS ==========
        if (profileMenuItem != null) {
            profileMenuItem.setOnAction(event -> handleProfile());
        }
        if (settingsMenuItemGlobal != null) {
            settingsMenuItemGlobal.setOnAction(event -> SceneManager.getInstance().switchToSettingsView());
        }
        if (accountNotificationsMenuItem != null) {
            accountNotificationsMenuItem.setOnAction(event -> SceneManager.getInstance().switchToNotificationView());
        }
        if (logoutMenuItem != null) {
            logoutMenuItem.setOnAction(event -> handleLogout());
        }
    }

    /**
     * Switches to the appropriate reports view based on user role
     */
    private void switchToRoleBasedReports() {
        String role = SessionManager.getInstance().getUserRole();
        if (role == null) {
            SceneManager.getInstance().switchToReportView();
            return;
        }

        switch (role.toUpperCase()) {
            case "POLICE":
                SceneManager.getInstance().switchToPoliceReportView();
                break;
            case "INSURANCE":
                SceneManager.getInstance().switchToInsuranceReportView();
                break;
            case "WORKSHOP":
                SceneManager.getInstance().switchToWorkshopReportView();
                break;
            default:
                SceneManager.getInstance().switchToReportView();
                break;
        }
    }

    /**
     * Switches to the appropriate export view based on user role
     */
    private void switchToRoleBasedExport() {
        String role = SessionManager.getInstance().getUserRole();
        if (role == null) {
            SceneManager.getInstance().switchToExportView();
            return;
        }

        switch (role.toUpperCase()) {
            case "POLICE":
                SceneManager.getInstance().switchToPoliceExportView();
                break;
            case "INSURANCE":
                SceneManager.getInstance().switchToInsuranceReportView();
                break;
            case "WORKSHOP":
                SceneManager.getInstance().switchToWorkshopReportView();
                break;
            default:
                SceneManager.getInstance().switchToExportView();
                break;
        }
    }

    // ============================================
    // BUSINESS LOGIC METHODS
    // ============================================

    /**
     * Handles application exit with confirmation
     */
    private void handleExit() {
        boolean confirmed = AlertUtil.showConfirmation("Exit", "Are you sure you want to exit the application?");
        if (confirmed) {
            javafx.application.Platform.exit();
        }
    }

    /**
     * Handles user logout with confirmation and audit logging
     */
    private void handleLogout() {
        boolean confirmed = AlertUtil.showConfirmation("Logout", "Are you sure you want to logout?");
        if (confirmed) {
            try {
                int userId = SessionManager.getInstance().getUserId();
                if (userId > 0 && auditDAO != null) {
                    auditDAO.logAction(userId, "LOGOUT", "127.0.0.1");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            SessionManager.getInstance().clearSession();
            SceneManager.getInstance().switchToLogin();
        }
    }

    /**
     * Handles profile view navigation based on user role
     */
    private void handleProfile() {
        String role = SessionManager.getInstance().getUserRole();
        if (role == null) {
            AlertUtil.showInfo("Profile", "Profile: " + SessionManager.getInstance().getFullName());
            return;
        }

        switch (role.toUpperCase()) {
            case "POLICE":
                SceneManager.getInstance().switchToPoliceProfileView();
                break;
            case "CUSTOMER":
                SceneManager.getInstance().switchToCustomerProfileView();
                break;
            case "WORKSHOP":
                SceneManager.getInstance().switchToWorkshopProfileView();
                break;
            case "INSURANCE":
                SceneManager.getInstance().switchToInsuranceProfileView();
                break;
            case "ADMIN":
                SceneManager.getInstance().switchToAdminProfileView();
                break;
            default:
                AlertUtil.showInfo("Profile", "Profile: " + SessionManager.getInstance().getFullName());
                break;
        }
    }

    /**
     * Displays help dialog with contact information
     */
    private void showHelpDialog() {
        AlertUtil.showInfo("Help",
                "Vehicle Identification System Help\n\n" +
                        "For assistance, please contact:\n" +
                        "Email: support@vehicle.com\n" +
                        "Phone: +266 5878 0099\n\n" +
                        "Documentation available in the user manual.");
    }

    /**
     * Displays about dialog with version information
     */
    private void showAboutDialog() {
        AlertUtil.showInfo("About",
                "Vehicle Identification System\n" +
                        "Version: 2.0.0\n\n" +
                        "Developed for Vehicle Management\n" +
                        "Copyright 2026");
    }
}