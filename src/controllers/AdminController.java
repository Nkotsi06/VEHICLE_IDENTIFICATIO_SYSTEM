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

    // Progress Indicators
    @FXML private ProgressIndicator loadProgress;
    @FXML private ProgressBar operationProgress;
    @FXML private Pagination activityPagination;

    private AuditDAO auditDAO;
    private ObservableList<Map<String, Object>> activityList;
    private List<Map<String, Object>> fullActivityData;
    private int currentPage = 0;
    private int pageSize = 10;

    @FXML
    public void initialize() {
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
                updateStatusText("Dashboard refreshed");
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

    /**
     * 1. Total Vehicles - from vehicles table
     */
    private void loadTotalVehicles() {
        String sql = "SELECT COUNT(*) as count FROM vehicles";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                int count = rs.getInt("count");
                if (totalVehiclesLabel != null) totalVehiclesLabel.setText(String.valueOf(count));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            if (totalVehiclesLabel != null) totalVehiclesLabel.setText("0");
        }
    }

    /**
     * 2. Total Customers - from customers table
     */
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

    /**
     * 3. Stolen Vehicles - where status_id references 'stolen' status
     */
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
            }
        } catch (SQLException e) {
            e.printStackTrace();
            if (stolenCountLabel != null) stolenCountLabel.setText("0");
        }
    }

    /**
     * 4. Active Insurance - where status = 'ACTIVE'
     */
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

    /**
     * 5. Unpaid Fines - sum of unpaid fines from violations table
     */
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

    /**
     * 6. Pending Queries - where status = 'PENDING'
     */
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

    /**
     * 7. Pending Workshops - where is_approved = false
     */
    private void loadPendingWorkshops() {
        String sql = "SELECT COUNT(*) as count FROM workshops WHERE is_approved = false";
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

    /**
     * 8. Pending Claims - where status = 'PENDING'
     */
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

    private void setDefaultStats() {
        if (totalVehiclesLabel != null) totalVehiclesLabel.setText("0");
        if (totalCustomersLabel != null) totalCustomersLabel.setText("0");
        if (stolenCountLabel != null) stolenCountLabel.setText("0");
        if (activeInsuranceLabel != null) activeInsuranceLabel.setText("0");
        if (unpaidFinesLabel != null) unpaidFinesLabel.setText("M0.00");
        if (pendingQueriesLabel != null) pendingQueriesLabel.setText("0");
        if (pendingWorkshopsLabel != null) pendingWorkshopsLabel.setText("0");
        if (pendingClaimsLabel != null) pendingClaimsLabel.setText("0");
    }

    private void loadRecentActivity() {
        String sql = "SELECT a.action, a.timestamp, a.ip_address, u.username " +
                "FROM audit_logs a " +
                "JOIN users u ON a.user_id = u.id " +
                "ORDER BY a.timestamp DESC LIMIT 100";

        fullActivityData = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("username", rs.getString("username"));
                row.put("action", rs.getString("action"));
                row.put("timestamp", rs.getTimestamp("timestamp"));
                row.put("ip_address", rs.getString("ip_address"));
                fullActivityData.add(row);
            }

            int totalPages = (int) Math.ceil((double) fullActivityData.size() / pageSize);
            if (activityPagination != null) activityPagination.setPageCount(Math.max(1, totalPages));
            updateTablePage();

        } catch (SQLException e) {
            e.printStackTrace();
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