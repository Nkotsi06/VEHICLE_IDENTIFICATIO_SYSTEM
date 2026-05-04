package controllers;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import utils.AlertUtil;
import utils.SceneManager;
import utils.SessionManager;
import database.ViewLoader;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Controller for Police Dashboard
 * Displays statistics, recent activity, and provides vehicle search functionality
 * Serves as the main hub for police officers
 */
public class PoliceController {

    // ============================================
    // FXML UI COMPONENTS - LABELS
    // ============================================

    @FXML private Label welcomeLabel;
    @FXML private Label statusLabel;

    // ============================================
    // STATISTICS CARDS LABELS
    // ============================================

    @FXML private Label stolenCountLabel;           // Number of active stolen vehicles
    @FXML private Label violationsTodayLabel;       // Violations reported today
    @FXML private Label activeWarrantsLabel;        // Number of active warrants
    @FXML private Label boloAlertsLabel;            // Active BOLO alerts
    @FXML private Label expiredDocsLabel;           // Expired document count
    @FXML private Label geofenceAlertsLabel;        // Unread geofence alerts
    @FXML private Label unpaidFinesLabel;           // Total unpaid fines amount
    @FXML private Label activeTrackingLabel;        // Active tracking incidents

    // ============================================
    // SEARCH COMPONENTS
    // ============================================

    @FXML private TextField searchRegistrationField;
    @FXML private Button searchButton;
    @FXML private Button refreshButton;
    @FXML private Button fadeButton;

    // ============================================
    // RECENT ACTIVITY TABLE
    // ============================================

    @FXML private TableView<Map<String, Object>> recentActivityTable;
    @FXML private TableColumn<Map<String, Object>, String> activityTimeColumn;
    @FXML private TableColumn<Map<String, Object>, String> activityActionColumn;
    @FXML private TableColumn<Map<String, Object>, String> activityVehicleColumn;
    @FXML private TableColumn<Map<String, Object>, String> activityOfficerColumn;

    // ============================================
    // PROGRESS INDICATORS
    // ============================================

    @FXML private ProgressIndicator loadProgress;
    @FXML private ProgressBar operationProgress;
    @FXML private Pagination activityPagination;

    // ============================================
    // DAO INSTANCES & DATA MODELS
    // ============================================

    private ViewLoader viewLoader;                  // Loader for database views
    private ObservableList<Map<String, Object>> activityList;
    private List<Map<String, Object>> fullActivityData;
    private int currentPage = 0;
    private int pageSize = 10;                      // Items per page

    // ============================================
    // INITIALIZATION METHODS
    // ============================================

    /**
     * Initializes the police dashboard controller
     * Sets up UI components, loads statistics, and starts auto-refresh
     */
    @FXML
    public void initialize() {
        viewLoader = new ViewLoader();
        activityList = FXCollections.observableArrayList();

        setupTableColumns();
        setupPagination();
        setupButtonHandlers();
        applyVisualEffects();
        loadDashboardData();

        // Set welcome message with officer name
        String officerName = SessionManager.getInstance().getFullName();
        if (officerName == null || officerName.isEmpty()) {
            officerName = SessionManager.getInstance().getUsername();
        }
        welcomeLabel.setText("Welcome, Officer " + (officerName != null ? officerName : "User"));
        statusLabel.setText("Ready");

        // Start auto-refresh every 60 seconds to keep data current
        startAutoRefresh();
    }

    /**
     * Configures table columns with cell value factories
     */
    private void setupTableColumns() {
        activityTimeColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(formatTimestamp(cellData.getValue().get("timestamp"))));
        activityActionColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(getStringValue(cellData.getValue(), "action")));
        activityVehicleColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(getStringValue(cellData.getValue(), "registration_number")));
        activityOfficerColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(getStringValue(cellData.getValue(), "officer_name")));

        // Center align columns for better visual appearance
        activityTimeColumn.setStyle("-fx-alignment: CENTER;");
        activityActionColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        activityVehicleColumn.setStyle("-fx-alignment: CENTER;");
        activityOfficerColumn.setStyle("-fx-alignment: CENTER;");
    }

    /**
     * Configures pagination for the activity table
     */
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

    /**
     * Updates the table to show current page of activity logs
     */
    private void updateTablePage() {
        if (fullActivityData == null || fullActivityData.isEmpty()) return;
        int start = currentPage * pageSize;
        int end = Math.min(start + pageSize, fullActivityData.size());
        if (start < fullActivityData.size()) {
            activityList.setAll(fullActivityData.subList(start, end));
            recentActivityTable.setItems(activityList);
        }
    }

    /**
     * Safely extracts string value from a map
     * @param map The data map
     * @param key The key to extract
     * @return String value or empty string if null
     */
    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "";
    }

    /**
     * Formats timestamp for display
     * @param timestamp The timestamp object
     * @return Formatted date-time string
     */
    private String formatTimestamp(Object timestamp) {
        if (timestamp instanceof java.sql.Timestamp) {
            return ((java.sql.Timestamp) timestamp).toLocalDateTime()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        return timestamp != null ? timestamp.toString() : "";
    }

    /**
     * Sets up button click handlers
     */
    private void setupButtonHandlers() {
        if (searchButton != null) {
            searchButton.setOnAction(event -> searchVehicle());
        }
        if (refreshButton != null) {
            refreshButton.setOnAction(event -> {
                loadDashboardData();
                statusLabel.setText("Data refreshed");
            });
        }
        if (fadeButton != null) {
            fadeButton.setOnAction(event -> showFadeAnimation());
        }
    }

    /**
     * Applies drop shadow visual effects to buttons
     */
    private void applyVisualEffects() {
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(5.0);
        dropShadow.setOffsetX(2.0);
        dropShadow.setOffsetY(2.0);
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.3));

        if (searchButton != null) searchButton.setEffect(dropShadow);
        if (refreshButton != null) refreshButton.setEffect(dropShadow);
        if (fadeButton != null) fadeButton.setEffect(dropShadow);
    }

    /**
     * Plays fade animation on the animate button
     */
    private void showFadeAnimation() {
        if (fadeButton != null) {
            FadeTransition fadeTransition = new FadeTransition(Duration.seconds(1.5), fadeButton);
            fadeTransition.setFromValue(1.0);
            fadeTransition.setToValue(0.2);
            fadeTransition.setCycleCount(4);
            fadeTransition.setAutoReverse(true);
            fadeTransition.play();
            statusLabel.setText("Animation played!");
            PauseTransition reset = new PauseTransition(Duration.seconds(2));
            reset.setOnFinished(e -> statusLabel.setText("Ready"));
            reset.play();
        }
    }

    /**
     * Starts automatic refresh of dashboard data every 60 seconds
     */
    private void startAutoRefresh() {
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(60),
                        e -> loadDashboardData())
        );
        timeline.setCycleCount(javafx.animation.Timeline.INDEFINITE);
        timeline.play();
    }

    // ============================================
    // DATA LOADING METHODS
    // ============================================

    /**
     * Loads all dashboard data including statistics and recent activity
     */
    private void loadDashboardData() {
        showLoadProgress(true);
        statusLabel.setText("Loading dashboard data...");

        try {
            loadStatistics();
            loadRecentActivity();
            statusLabel.setText("Dashboard loaded successfully");
        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Error loading data: " + e.getMessage());
            AlertUtil.showError("Load Error", "Failed to load dashboard data: " + e.getMessage());
        } finally {
            hideLoadProgressAfterDelay();
        }
    }

    /**
     * Loads statistics for dashboard cards from database views
     * @throws SQLException if database error occurs
     */
    private void loadStatistics() throws SQLException {
        // Load stolen vehicles count
        List<Map<String, Object>> stolenList = viewLoader.loadActiveStolenVehicles();
        stolenCountLabel.setText(String.valueOf(stolenList != null ? stolenList.size() : 0));

        // Load violations and count today's violations
        List<Map<String, Object>> violationsList = viewLoader.loadViolationsView();
        if (violationsList != null) {
            long todayCount = violationsList.stream()
                    .filter(v -> {
                        Object date = v.get("violation_date");
                        return date != null && date.toString().startsWith(LocalDateTime.now().toLocalDate().toString());
                    })
                    .count();
            violationsTodayLabel.setText(String.valueOf(todayCount));
        } else {
            violationsTodayLabel.setText("0");
        }

        // Load active warrants count
        List<Map<String, Object>> warrantsList = viewLoader.loadActiveWarrants();
        activeWarrantsLabel.setText(String.valueOf(warrantsList != null ? warrantsList.size() : 0));

        // Load active BOLO alerts count
        List<Map<String, Object>> boloList = viewLoader.loadActiveBOLOAlerts();
        boloAlertsLabel.setText(String.valueOf(boloList != null ? boloList.size() : 0));

        // Load expired documents count
        List<Map<String, Object>> expiredDocs = viewLoader.loadExpiredDocuments();
        expiredDocsLabel.setText(String.valueOf(expiredDocs != null ? expiredDocs.size() : 0));

        // Load geofence alerts and count unread ones
        List<Map<String, Object>> geofenceAlerts = viewLoader.loadGeofenceAlerts();
        if (geofenceAlerts != null) {
            long unreadCount = geofenceAlerts.stream()
                    .filter(a -> !Boolean.TRUE.equals(a.get("is_notified")))
                    .count();
            geofenceAlertsLabel.setText(String.valueOf(unreadCount));
        } else {
            geofenceAlertsLabel.setText("0");
        }

        // Calculate total unpaid fines
        double unpaidFinesTotal = 0;
        if (violationsList != null) {
            unpaidFinesTotal = violationsList.stream()
                    .filter(v -> "UNPAID".equals(v.get("payment_status")))
                    .mapToDouble(v -> {
                        Object fine = v.get("fine_amount");
                        return fine instanceof Number ? ((Number) fine).doubleValue() : 0;
                    })
                    .sum();
        }
        unpaidFinesLabel.setText(String.format("M%,.2f", unpaidFinesTotal));

        // Load officer logs for today
        List<Map<String, Object>> officerLogs = viewLoader.loadOfficerLogs();
        long todayLogs = officerLogs != null ?
                officerLogs.stream().filter(l -> {
                    Object ts = l.get("timestamp");
                    return ts != null && ts.toString().startsWith(LocalDateTime.now().toLocalDate().toString());
                }).count() : 0;
        activeTrackingLabel.setText(String.valueOf(todayLogs));
    }

    /**
     * Loads recent police officer activity for the table
     * @throws SQLException if database error occurs
     */
    private void loadRecentActivity() throws SQLException {
        List<Map<String, Object>> logs = viewLoader.loadOfficerLogs();
        if (logs != null && !logs.isEmpty()) {
            fullActivityData = logs;
            int totalPages = (int) Math.ceil((double) logs.size() / pageSize);
            if (activityPagination != null) activityPagination.setPageCount(Math.max(1, totalPages));
            updateTablePage();
        }
    }

    // ============================================
    // BUSINESS LOGIC METHODS
    // ============================================

    /**
     * Searches for a vehicle by registration number
     * Displays vehicle details in an alert dialog
     */
    private void searchVehicle() {
        String regNumber = searchRegistrationField.getText().trim();
        if (regNumber.isEmpty()) {
            AlertUtil.showWarning("Search Error", "Please enter a registration number");
            return;
        }

        showOperationProgress(true);
        statusLabel.setText("Searching for vehicle: " + regNumber);
        updateProgress(0.3);

        try {
            updateProgress(0.6);
            Map<String, Object> vehicle = viewLoader.loadVehicleByRegistration(regNumber);
            updateProgress(0.9);

            if (vehicle != null) {
                // Build detailed vehicle information display
                StringBuilder info = new StringBuilder();
                info.append("Vehicle Found!\n\n");
                info.append("Registration: ").append(vehicle.get("registration_number")).append("\n");
                info.append("Make: ").append(vehicle.get("make")).append("\n");
                info.append("Model: ").append(vehicle.get("model")).append("\n");
                info.append("Year: ").append(vehicle.get("year")).append("\n");
                info.append("Owner: ").append(vehicle.get("owner_name")).append("\n");
                info.append("Status: ").append(vehicle.get("status_name"));

                updateProgress(1.0);
                AlertUtil.showInfo("Vehicle Found", info.toString());
                statusLabel.setText("Vehicle found: " + regNumber);
            } else {
                AlertUtil.showInfo("Not Found", "No vehicle found with registration: " + regNumber);
                statusLabel.setText("Vehicle not found: " + regNumber);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Search error: " + e.getMessage());
            AlertUtil.showError("Search Error", "Failed to search vehicle: " + e.getMessage());
        } finally {
            hideProgressAfterDelay();
        }
    }

    // ============================================
    // UI PROGRESS METHODS
    // ============================================

    /**
     * Shows/hides load progress indicator
     * @param show true to show, false to hide
     */
    private void showLoadProgress(boolean show) {
        if (loadProgress != null) loadProgress.setVisible(show);
    }

    /**
     * Shows/hides operation progress bar
     * @param show true to show, false to hide
     */
    private void showOperationProgress(boolean show) {
        if (operationProgress != null) {
            operationProgress.setVisible(show);
            operationProgress.setProgress(0);
        }
    }

    /**
     * Updates progress bar value
     * @param progress value between 0 and 1
     */
    private void updateProgress(double progress) {
        if (operationProgress != null) operationProgress.setProgress(progress);
    }

    /**
     * Hides load progress indicator after a short delay
     */
    private void hideLoadProgressAfterDelay() {
        PauseTransition delay = new PauseTransition(Duration.seconds(1));
        delay.setOnFinished(event -> {
            if (loadProgress != null) loadProgress.setVisible(false);
        });
        delay.play();
    }

    /**
     * Hides progress indicators after a short delay
     */
    private void hideProgressAfterDelay() {
        PauseTransition delay = new PauseTransition(Duration.seconds(1.5));
        delay.setOnFinished(event -> {
            if (operationProgress != null) {
                operationProgress.setVisible(false);
                operationProgress.setProgress(0);
            }
            if (loadProgress != null) {
                loadProgress.setVisible(false);
            }
        });
        delay.play();
    }
}