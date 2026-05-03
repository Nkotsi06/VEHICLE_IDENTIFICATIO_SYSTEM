package utils;

public class SceneManager {

    private static SceneManager instance;
    private RootManager rootManager;

    private SceneManager() {
        rootManager = RootManager.getInstance();
    }

    public static synchronized SceneManager getInstance() {
        if (instance == null) {
            instance = new SceneManager();
        }
        return instance;
    }

    public void setPrimaryStage(javafx.stage.Stage stage) {
        rootManager.setPrimaryStage(stage);
    }

    // ============================================
    // AUTHENTICATION VIEWS
    // ============================================

    public void switchToLogin() {
        rootManager.setRoot("LoginView.fxml", "Login");
    }

    public void switchToForgotPassword() {
        rootManager.setRoot("ForgotPasswordView.fxml", "ForgotPassword");
    }

    // ============================================
    // DASHBOARD VIEWS (Role-Based)
    // ============================================

    public void switchToDashboard() {
        String role = SessionManager.getInstance().getUserRole();
        if (role == null) {
            switchToLogin();
            return;
        }

        switch (role) {
            case "ADMIN" -> switchToAdminView();
            case "POLICE" -> switchToPoliceView();
            case "CUSTOMER" -> switchToCustomerView();
            case "WORKSHOP" -> switchToWorkshopView();
            case "INSURANCE" -> switchToInsuranceView();
            default -> switchToLogin();
        }
    }

    public void switchToAdminView() {
        rootManager.setRoot("AdminView.fxml", "Admin");
    }

    public void switchToPoliceView() {
        rootManager.setRoot("PoliceView.fxml", "Police");
    }

    public void switchToInsuranceView() {
        rootManager.setRoot("InsuranceView.fxml", "Insurance");
    }

    public void switchToWorkshopView() {
        rootManager.setRoot("WorkshopView.fxml", "Workshop");
    }

    public void switchToCustomerView() {
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

    public void switchToPoliceProfileView() {
        rootManager.setRoot("PoliceProfileView.fxml", "PoliceProfile");
    }

    // ============================================
    // POLICE REPORT VIEW
    // ============================================

    public void switchToPoliceReportView() {
        rootManager.setRoot("PoliceReportView.fxml", "Police Reports");
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

    public void switchToMechanicView() {
        rootManager.setRoot("MechanicView.fxml", "Mechanic");
    }

    public void switchToWorkshopServiceView() {
        rootManager.setRoot("WorkshopServiceView.fxml", "WorkshopService");
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

    // ============================================
    // DUMMY DATA VIEW
    // ============================================

    public void switchToDummyDataView() {
        rootManager.setRoot("DummyDataView.fxml", "Dummy Data");
    }

    // ============================================
    // REPORTS AND EXPORT VIEWS
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

    public void switchToPoliceExportView() {
        rootManager.setRoot("PoliceExportView.fxml", "PoliceExport");
    }

    public void switchToExportView() {
        switchToReportView();
    }

    // ============================================
    // SEARCH AND SETTINGS VIEWS
    // ============================================

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
    // ADMIN PROFILE VIEW
    // ============================================

    public void switchToAdminProfileView() {
        rootManager.setRoot("AdminProfileView.fxml", "AdminProfile");
    }

    // ============================================
    // ADVANCED FEATURES VIEWS
    // ============================================

    public void switchToClaimStatusTrackerView() {
        rootManager.setRoot("ClaimStatusTrackerView.fxml", "ClaimStatusTracker");
    }

    public void switchToOCRScannerView() {
        rootManager.setRoot("OCRScannerView.fxml", "OCRScanner");
    }

    public void switchToPredictiveAnalyticsView() {
        rootManager.setRoot("PredictiveAnalyticsView.fxml", "PredictiveAnalytics");
    }

    // ============================================
    // UTILITY METHODS
    // ============================================

    public javafx.stage.Stage getPrimaryStage() {
        return rootManager.getPrimaryStage();
    }

    public String getCurrentScene() {
        return rootManager.getCurrentScene();
    }

    public Object getCurrentController() {
        return rootManager.getCurrentController();
    }

    public void refreshCurrentScene() {
        rootManager.refreshCurrentScene();
    }
}