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
import database.DatabaseConnection;
import dao.SystemHealthDAO;
import dao.AuditDAO;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

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
    @FXML private TableView<Map<String, Object>> recentActivityTable;
    @FXML private TableColumn<Map<String, Object>, String> activityUserColumn;
    @FXML private TableColumn<Map<String, Object>, String> activityActionColumn;
    @FXML private TableColumn<Map<String, Object>, String> activityTimestampColumn;
    @FXML private TableColumn<Map<String, Object>, String> activityIpColumn;
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

    private SystemHealthDAO systemHealthDAO;
    private AuditDAO auditDAO;
    private ObservableList<Map<String, Object>> activityList;
    private List<Map<String, Object>> fullActivityData;
    private int currentPage = 0;
    private int pageSize = 10;

    @FXML
    public void initialize() {
        systemHealthDAO = new SystemHealthDAO();
        auditDAO = new AuditDAO();
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
                new javafx.beans.property.SimpleStringProperty(getStringValue(cellData.getValue(), "username")));
        activityActionColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(getStringValue(cellData.getValue(), "action")));
        activityTimestampColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(formatTimestamp(cellData.getValue().get("timestamp"))));
        activityIpColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(getStringValue(cellData.getValue(), "ip_address")));

        activityUserColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        activityActionColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        activityTimestampColumn.setStyle("-fx-alignment: CENTER;");
        activityIpColumn.setStyle("-fx-alignment: CENTER;");
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

    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "";
    }

    private String formatTimestamp(Object timestamp) {
        if (timestamp instanceof java.sql.Timestamp) {
            return ((java.sql.Timestamp) timestamp).toLocalDateTime()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        return timestamp != null ? timestamp.toString() : "";
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
        String sql = "SELECT COUNT(*) as count FROM vehicles";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                int count = rs.getInt("count");
                if (totalVehiclesLabel != null) totalVehiclesLabel.setText(String.valueOf(count));
                if (totalVehiclesSystemLabel != null) totalVehiclesSystemLabel.setText(String.valueOf(count));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            if (totalVehiclesLabel != null) totalVehiclesLabel.setText("0");
            if (totalVehiclesSystemLabel != null) totalVehiclesSystemLabel.setText("0");
        }
    }

    private void loadTotalCustomers() {
        String sql = "SELECT COUNT(*) as count FROM customers";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                int count = rs.getInt("count");
                if (totalCustomersLabel != null) totalCustomersLabel.setText(String.valueOf(count));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            if (totalCustomersLabel != null) totalCustomersLabel.setText("0");
        }
    }

    private void loadStolenVehicles() {
        String sql = "SELECT COUNT(*) as count FROM vehicles v " +
                "JOIN vehicle_status vs ON v.status_id = vs.id " +
                "WHERE vs.status_name = 'stolen'";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                int count = rs.getInt("count");
                if (stolenCountLabel != null) stolenCountLabel.setText(String.valueOf(count));
                if (stolenVehiclesSystemLabel != null) stolenVehiclesSystemLabel.setText(String.valueOf(count));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            if (stolenCountLabel != null) stolenCountLabel.setText("0");
            if (stolenVehiclesSystemLabel != null) stolenVehiclesSystemLabel.setText("0");
        }
    }

    private void loadActiveInsurance() {
        String sql = "SELECT COUNT(*) as count FROM insurance_policies WHERE status = 'ACTIVE' AND end_date >= CURRENT_DATE";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                int count = rs.getInt("count");
                if (activeInsuranceLabel != null) activeInsuranceLabel.setText(String.valueOf(count));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            if (activeInsuranceLabel != null) activeInsuranceLabel.setText("0");
        }
    }

    private void loadUnpaidFines() {
        String sql = "SELECT COALESCE(SUM(fine_amount), 0) as total FROM violations WHERE payment_status = 'UNPAID'";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                double total = rs.getDouble("total");
                if (unpaidFinesLabel != null) unpaidFinesLabel.setText(String.format("M%,.2f", total));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            if (unpaidFinesLabel != null) unpaidFinesLabel.setText("M0.00");
        }
    }

    private void loadPendingQueries() {
        String sql = "SELECT COUNT(*) as count FROM customer_queries WHERE status = 'PENDING'";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                int count = rs.getInt("count");
                if (pendingQueriesLabel != null) pendingQueriesLabel.setText(String.valueOf(count));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            if (pendingQueriesLabel != null) pendingQueriesLabel.setText("0");
        }
    }

    private void loadPendingWorkshops() {
        String sql = "SELECT COUNT(*) as count FROM workshops WHERE is_approved = false OR is_approved IS NULL";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                int count = rs.getInt("count");
                if (pendingWorkshopsLabel != null) pendingWorkshopsLabel.setText(String.valueOf(count));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            if (pendingWorkshopsLabel != null) pendingWorkshopsLabel.setText("0");
        }
    }

    private void loadPendingClaims() {
        String sql = "SELECT COUNT(*) as count FROM insurance_claims WHERE status = 'PENDING'";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                int count = rs.getInt("count");
                if (pendingClaimsLabel != null) pendingClaimsLabel.setText(String.valueOf(count));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            if (pendingClaimsLabel != null) pendingClaimsLabel.setText("0");
        }
    }

    private void loadUserStatistics() {
        String totalUsersSql = "SELECT COUNT(*) as count FROM users";
        String activeUsersSql = "SELECT COUNT(*) as count FROM users WHERE is_active = true";
        String inactiveUsersSql = "SELECT COUNT(*) as count FROM users WHERE is_active = false";

        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(totalUsersSql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next() && totalUsersLabel != null) {
                    totalUsersLabel.setText(String.valueOf(rs.getInt("count")));
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(activeUsersSql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next() && activeUsersLabel != null) {
                    activeUsersLabel.setText(String.valueOf(rs.getInt("count")));
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(inactiveUsersSql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next() && inactiveUsersLabel != null) {
                    inactiveUsersLabel.setText(String.valueOf(rs.getInt("count")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            if (totalUsersLabel != null) totalUsersLabel.setText("0");
            if (activeUsersLabel != null) activeUsersLabel.setText("0");
            if (inactiveUsersLabel != null) inactiveUsersLabel.setText("0");
        }
    }

    private void loadSystemLoad() {
        try {
            String sql = "SELECT " +
                    "(SELECT COUNT(*) FROM vehicles) as vehicles, " +
                    "(SELECT COUNT(*) FROM users) as users, " +
                    "(SELECT COUNT(*) FROM violations WHERE payment_status = 'UNPAID') as unpaid";

            try (Connection conn = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int vehicles = rs.getInt("vehicles");
                    int users = rs.getInt("users");
                    int unpaid = rs.getInt("unpaid");
                    double load = Math.min(1.0, (vehicles / 5000.0) + (users / 1000.0) + (unpaid / 100.0));
                    load = Math.min(1.0, load / 3.0);
                    if (systemLoadProgress != null) systemLoadProgress.setProgress(load);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            if (systemLoadProgress != null) systemLoadProgress.setProgress(0.3);
        }
    }

    private void checkDatabaseStatus() {
        String sql = "SELECT 1";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next() && databaseStatusLabel != null) {
                databaseStatusLabel.setText("Connected");
                databaseStatusLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            }
        } catch (SQLException e) {
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

    private void setDefaultHealthValues() {
        if (totalUsersLabel != null) totalUsersLabel.setText("0");
        if (activeUsersLabel != null) activeUsersLabel.setText("0");
        if (inactiveUsersLabel != null) inactiveUsersLabel.setText("0");
    }

    private void loadRecentActivity() {
        String sql = "SELECT a.action, a.timestamp, a.ip_address, u.username " +
                "FROM audit_logs a " +
                "JOIN users u ON a.user_id = u.id " +
                "ORDER BY a.timestamp DESC LIMIT 200";

        fullActivityData = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("username", rs.getString("username") != null ? rs.getString("username") : "System");
                row.put("action", rs.getString("action"));
                row.put("timestamp", rs.getTimestamp("timestamp"));
                row.put("ip_address", rs.getString("ip_address") != null ? rs.getString("ip_address") : "127.0.0.1");
                fullActivityData.add(row);
            }

            int totalPages = (int) Math.ceil((double) fullActivityData.size() / pageSize);
            if (activityPagination != null) activityPagination.setPageCount(Math.max(1, totalPages));
            updateTablePage();
            statusLabel.setText("Loaded " + fullActivityData.size() + " activity records");

        } catch (SQLException e) {
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