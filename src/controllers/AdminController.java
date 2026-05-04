package controllers;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import utils.AlertUtil;
import utils.SceneManager;
import utils.SessionManager;
import dao.AuditDAO;
import dao.SystemHealthDAO;
import dao.VehicleDAO;
import dao.CustomerDAO;
import dao.InsurancePolicyDAO;
import dao.ViolationDAO;
import dao.CustomerQueryDAO;
import dao.WorkshopDAO;
import dao.InsuranceClaimDAO;
import dao.StolenVehicleDAO;
import models.AuditLog;
import models.SystemHealth;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AdminController {

    @FXML private TabPane mainTabPane;
    @FXML private Label welcomeLabel;
    @FXML private Label statusLabel;

    // Dashboard Stats Labels
    @FXML private Label totalVehiclesLabel;
    @FXML private Label totalCustomersLabel;
    @FXML private Label stolenCountLabel;
    @FXML private Label activeInsuranceLabel;
    @FXML private Label unpaidFinesLabel;
    @FXML private Label pendingQueriesLabel;
    @FXML private Label pendingWorkshopsLabel;
    @FXML private Label pendingClaimsLabel;

    // Stats Cards
    @FXML private VBox totalVehiclesCard;
    @FXML private VBox totalCustomersCard;
    @FXML private VBox stolenCountCard;
    @FXML private VBox activeInsuranceCard;
    @FXML private VBox unpaidFinesCard;
    @FXML private VBox pendingQueriesCard;
    @FXML private VBox pendingWorkshopsCard;
    @FXML private VBox pendingClaimsCard;

    // Recent Activity Table
    @FXML private TableView<AuditLog> recentActivityTable;
    @FXML private TableColumn<AuditLog, String> activityUserColumn;
    @FXML private TableColumn<AuditLog, String> activityActionColumn;
    @FXML private TableColumn<AuditLog, String> activityTimestampColumn;
    @FXML private TableColumn<AuditLog, String> activityIpColumn;
    @FXML private Button refreshButton;
    @FXML private Button fadeButton;

    // System Health Tab
    @FXML private Label totalUsersLabel;
    @FXML private Label activeUsersLabel;
    @FXML private Label inactiveUsersLabel;
    @FXML private Label totalVehiclesSystemLabel;
    @FXML private Label stolenVehiclesSystemLabel;
    @FXML private ProgressBar systemLoadProgress;
    @FXML private Label databaseStatusLabel;
    @FXML private Label lastUpdatedLabel;
    @FXML private Button refreshSystemButton;

    // Progress Indicators
    @FXML private ProgressIndicator loadProgress;
    @FXML private ProgressBar operationProgress;
    @FXML private Pagination activityPagination;

    // DAO Instances
    private SystemHealthDAO systemHealthDAO;
    private AuditDAO auditDAO;
    private VehicleDAO vehicleDAO;
    private CustomerDAO customerDAO;
    private InsurancePolicyDAO policyDAO;
    private ViolationDAO violationDAO;
    private CustomerQueryDAO queryDAO;
    private WorkshopDAO workshopDAO;
    private InsuranceClaimDAO claimDAO;
    private StolenVehicleDAO stolenVehicleDAO;

    private ObservableList<AuditLog> activityList;
    private List<AuditLog> fullActivityData;
    private int currentPage = 0;
    private int pageSize = 10;

    @FXML
    public void initialize() {
        // Initialize DAOs
        systemHealthDAO = new SystemHealthDAO();
        auditDAO = new AuditDAO();
        vehicleDAO = new VehicleDAO();
        customerDAO = new CustomerDAO();
        policyDAO = new InsurancePolicyDAO();
        violationDAO = new ViolationDAO();
        queryDAO = new CustomerQueryDAO();
        workshopDAO = new WorkshopDAO();
        claimDAO = new InsuranceClaimDAO();
        stolenVehicleDAO = new StolenVehicleDAO();

        activityList = FXCollections.observableArrayList();

        setupTableColumns();
        setupPagination();
        setupButtonHandlers();
        applyVisualEffects();
        loadAllDashboardData();

        String adminName = SessionManager.getInstance().getFullName();
        if (adminName == null || adminName.isEmpty()) {
            adminName = SessionManager.getInstance().getUsername();
        }
        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome, " + (adminName != null ? adminName : "Administrator"));
        }
        updateStatusText("Ready");
    }

    private void updateStatusText(String text) {
        if (statusLabel != null) {
            statusLabel.setText(text);
        }
    }

    private void setupTableColumns() {
        activityUserColumn.setCellValueFactory(cellData ->
                cellData.getValue().usernameProperty());
        activityActionColumn.setCellValueFactory(cellData ->
                cellData.getValue().actionProperty());
        activityTimestampColumn.setCellValueFactory(cellData ->
                cellData.getValue().timestampProperty().asString());
        activityIpColumn.setCellValueFactory(cellData ->
                cellData.getValue().ipAddressProperty());
    }

    private void setupPagination() {
        if (activityPagination != null) {
            activityPagination.setPageCount(1);
            activityPagination.setMaxPageIndicatorCount(5);
            activityPagination.currentPageIndexProperty().addListener((obs, old, newPage) -> {
                currentPage = newPage.intValue();
                updateTablePage();
            });
        }
    }

    private void updateTablePage() {
        if (fullActivityData == null || fullActivityData.isEmpty()) return;
        int start = currentPage * pageSize;
        int end = Math.min(start + pageSize, fullActivityData.size());
        if (start < fullActivityData.size()) {
            activityList.setAll(fullActivityData.subList(start, end));
            recentActivityTable.setItems(activityList);
        }
    }

    private void setupButtonHandlers() {
        if (refreshButton != null) {
            refreshButton.setOnAction(event -> {
                loadDashboardStats();
                loadRecentActivity();
                updateLastUpdated();
                updateStatusText("Dashboard refreshed");
                PauseTransition reset = new PauseTransition(Duration.seconds(2));
                reset.setOnFinished(e -> updateStatusText("Ready"));
                reset.play();
            });
        }
        if (refreshSystemButton != null) {
            refreshSystemButton.setOnAction(event -> {
                loadSystemHealthData();
                updateStatusText("System health refreshed");
                PauseTransition reset = new PauseTransition(Duration.seconds(2));
                reset.setOnFinished(e -> updateStatusText("Ready"));
                reset.play();
            });
        }
        if (fadeButton != null) {
            fadeButton.setOnAction(event -> showFadeAnimation());
        }
    }

    private void applyVisualEffects() {
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(5.0);
        dropShadow.setOffsetX(2.0);
        dropShadow.setOffsetY(2.0);
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.3));

        if (refreshButton != null) refreshButton.setEffect(dropShadow);
        if (refreshSystemButton != null) refreshSystemButton.setEffect(dropShadow);
        if (fadeButton != null) fadeButton.setEffect(dropShadow);
    }

    private void showFadeAnimation() {
        if (fadeButton != null) {
            FadeTransition fadeTransition = new FadeTransition(Duration.seconds(1.5), fadeButton);
            fadeTransition.setFromValue(1.0);
            fadeTransition.setToValue(0.2);
            fadeTransition.setCycleCount(4);
            fadeTransition.setAutoReverse(true);
            fadeTransition.play();
            updateStatusText("Animation played!");
            PauseTransition reset = new PauseTransition(Duration.seconds(2));
            reset.setOnFinished(e -> updateStatusText("Ready"));
            reset.play();
        }
    }

    private void loadAllDashboardData() {
        showProgress(true);
        updateStatusText("Loading dashboard data...");

        try {
            loadDashboardStats();
            loadRecentActivity();
            loadSystemHealthData();
            updateLastUpdated();
            updateStatusText("Dashboard loaded successfully");
        } catch (Exception e) {
            e.printStackTrace();
            updateStatusText("Error loading dashboard: " + e.getMessage());
            AlertUtil.showError("Load Error", "Failed to load dashboard data.");
        } finally {
            hideProgressAfterDelay();
        }
    }

    // ==================== DASHBOARD STATS METHODS ====================

    private void loadDashboardStats() {
        try {
            loadTotalVehicles();
            loadTotalCustomers();
            loadStolenVehicles();
            loadActiveInsurance();
            loadUnpaidFines();
            loadPendingQueries();
            loadPendingWorkshops();
            loadPendingClaims();
        } catch (Exception e) {
            e.printStackTrace();
            setDefaultStats();
        }
    }

    private void loadTotalVehicles() {
        try {
            int count = vehicleDAO.countVehicles();
            if (totalVehiclesLabel != null) totalVehiclesLabel.setText(String.valueOf(count));
            if (totalVehiclesSystemLabel != null) totalVehiclesSystemLabel.setText(String.valueOf(count));
        } catch (Exception e) {
            e.printStackTrace();
            if (totalVehiclesLabel != null) totalVehiclesLabel.setText("0");
            if (totalVehiclesSystemLabel != null) totalVehiclesSystemLabel.setText("0");
        }
    }

    private void loadTotalCustomers() {
        try {
            int count = customerDAO.countCustomers();
            if (totalCustomersLabel != null) totalCustomersLabel.setText(String.valueOf(count));
        } catch (Exception e) {
            e.printStackTrace();
            if (totalCustomersLabel != null) totalCustomersLabel.setText("0");
        }
    }

    private void loadStolenVehicles() {
        try {
            int count = stolenVehicleDAO.countActiveStolen();
            if (stolenCountLabel != null) stolenCountLabel.setText(String.valueOf(count));
            if (stolenVehiclesSystemLabel != null) stolenVehiclesSystemLabel.setText(String.valueOf(count));
        } catch (Exception e) {
            e.printStackTrace();
            if (stolenCountLabel != null) stolenCountLabel.setText("0");
            if (stolenVehiclesSystemLabel != null) stolenVehiclesSystemLabel.setText("0");
        }
    }

    private void loadActiveInsurance() {
        try {
            int count = policyDAO.countActivePolicies();
            if (activeInsuranceLabel != null) activeInsuranceLabel.setText(String.valueOf(count));
        } catch (Exception e) {
            e.printStackTrace();
            if (activeInsuranceLabel != null) activeInsuranceLabel.setText("0");
        }
    }

    private void loadUnpaidFines() {
        try {
            double total = violationDAO.getTotalUnpaidFines();
            if (unpaidFinesLabel != null) unpaidFinesLabel.setText(String.format("M%,.2f", total));
        } catch (Exception e) {
            e.printStackTrace();
            if (unpaidFinesLabel != null) unpaidFinesLabel.setText("M0.00");
        }
    }

    private void loadPendingQueries() {
        try {
            int count = queryDAO.countPendingQueries();
            if (pendingQueriesLabel != null) pendingQueriesLabel.setText(String.valueOf(count));
        } catch (Exception e) {
            e.printStackTrace();
            if (pendingQueriesLabel != null) pendingQueriesLabel.setText("0");
        }
    }

    private void loadPendingWorkshops() {
        try {
            int count = workshopDAO.countPendingWorkshops();
            if (pendingWorkshopsLabel != null) pendingWorkshopsLabel.setText(String.valueOf(count));
        } catch (Exception e) {
            e.printStackTrace();
            if (pendingWorkshopsLabel != null) pendingWorkshopsLabel.setText("0");
        }
    }

    private void loadPendingClaims() {
        try {
            int count = claimDAO.countPendingClaims();
            if (pendingClaimsLabel != null) pendingClaimsLabel.setText(String.valueOf(count));
        } catch (Exception e) {
            e.printStackTrace();
            if (pendingClaimsLabel != null) pendingClaimsLabel.setText("0");
        }
    }

    private void loadUserStatistics() {
        try {
            SystemHealth health = systemHealthDAO.getSystemHealth();
            if (totalUsersLabel != null) totalUsersLabel.setText(String.valueOf(health.getTotalUsers()));
            if (activeUsersLabel != null) activeUsersLabel.setText(String.valueOf(health.getActiveUsers()));
            if (inactiveUsersLabel != null) inactiveUsersLabel.setText(String.valueOf(health.getInactiveUsers()));
        } catch (Exception e) {
            e.printStackTrace();
            if (totalUsersLabel != null) totalUsersLabel.setText("0");
            if (activeUsersLabel != null) activeUsersLabel.setText("0");
            if (inactiveUsersLabel != null) inactiveUsersLabel.setText("0");
        }
    }

    private void loadSystemLoad() {
        try {
            SystemHealth health = systemHealthDAO.getSystemHealth();
            double load = Math.min(1.0, (health.getTotalVehicles() / 5000.0) +
                    (health.getTotalUsers() / 1000.0) +
                    (violationDAO.getTotalUnpaidFines() / 10000.0));
            load = Math.min(1.0, load / 3.0);
            if (systemLoadProgress != null) systemLoadProgress.setProgress(load);
        } catch (Exception e) {
            e.printStackTrace();
            if (systemLoadProgress != null) systemLoadProgress.setProgress(0.3);
        }
    }

    private void checkDatabaseStatus() {
        try {
            systemHealthDAO.checkDatabaseStatus();
            if (databaseStatusLabel != null) {
                databaseStatusLabel.setText("Connected");
                databaseStatusLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            }
        } catch (Exception e) {
            if (databaseStatusLabel != null) {
                databaseStatusLabel.setText("Disconnected");
                databaseStatusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            }
            e.printStackTrace();
        }
    }

    private void setDefaultStats() {
        if (totalVehiclesLabel != null) totalVehiclesLabel.setText("0");
        if (totalCustomersLabel != null) totalCustomersLabel.setText("0");
        if (stolenCountLabel != null) stolenCountLabel.setText("0");
        if (activeInsuranceLabel != null) activeInsuranceLabel.setText("0");
        if (unpaidFinesLabel != null) unpaidFinesLabel.setText("M0.00");
        if (pendingQueriesLabel != null) pendingQueriesLabel.setText("0");
        if (pendingWorkshopsLabel != null) pendingWorkshopsLabel.setText("0");
        if (pendingClaimsLabel != null) pendingClaimsLabel.setText("0");
        if (totalVehiclesSystemLabel != null) totalVehiclesSystemLabel.setText("0");
        if (stolenVehiclesSystemLabel != null) stolenVehiclesSystemLabel.setText("0");
    }

    private void loadRecentActivity() {
        try {
            fullActivityData = auditDAO.findAll();

            int totalPages = (int) Math.ceil((double) fullActivityData.size() / pageSize);
            if (activityPagination != null) activityPagination.setPageCount(Math.max(1, totalPages));
            updateTablePage();
            statusLabel.setText("Loaded " + fullActivityData.size() + " activity records");

        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error loading recent activity: " + e.getMessage());
        }
    }

    private void loadSystemHealthData() {
        loadUserStatistics();
        loadTotalVehicles();
        loadStolenVehicles();
        loadSystemLoad();
        checkDatabaseStatus();
        updateLastUpdated();
    }

    private void updateLastUpdated() {
        if (lastUpdatedLabel != null) {
            lastUpdatedLabel.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
    }

    private void showProgress(boolean show) {
        if (loadProgress != null) loadProgress.setVisible(show);
        if (operationProgress != null) {
            operationProgress.setVisible(show);
            operationProgress.setProgress(0);
        }
    }

    private void hideProgressAfterDelay() {
        PauseTransition delay = new PauseTransition(Duration.seconds(1));
        delay.setOnFinished(event -> {
            if (loadProgress != null) loadProgress.setVisible(false);
            if (operationProgress != null) {
                operationProgress.setVisible(false);
                operationProgress.setProgress(0);
            }
        });
        delay.play();
    }
}