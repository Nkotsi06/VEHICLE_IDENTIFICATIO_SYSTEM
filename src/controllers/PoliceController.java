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

public class PoliceController {

    @FXML private Label welcomeLabel;
    @FXML private Label statusLabel;

    // Statistics Cards Labels
    @FXML private Label stolenCountLabel;
    @FXML private Label violationsTodayLabel;
    @FXML private Label activeWarrantsLabel;
    @FXML private Label boloAlertsLabel;
    @FXML private Label expiredDocsLabel;
    @FXML private Label geofenceAlertsLabel;
    @FXML private Label unpaidFinesLabel;
    @FXML private Label activeTrackingLabel;

    // Search
    @FXML private TextField searchRegistrationField;
    @FXML private Button searchButton;
    @FXML private Button refreshButton;
    @FXML private Button fadeButton;

    // Recent Activity Table
    @FXML private TableView<Map<String, Object>> recentActivityTable;
    @FXML private TableColumn<Map<String, Object>, String> activityTimeColumn;
    @FXML private TableColumn<Map<String, Object>, String> activityActionColumn;
    @FXML private TableColumn<Map<String, Object>, String> activityVehicleColumn;
    @FXML private TableColumn<Map<String, Object>, String> activityOfficerColumn;

    // Progress Indicators
    @FXML private ProgressIndicator loadProgress;
    @FXML private ProgressBar operationProgress;
    @FXML private Pagination activityPagination;

    private ViewLoader viewLoader;
    private ObservableList<Map<String, Object>> activityList;
    private List<Map<String, Object>> fullActivityData;
    private int currentPage = 0;
    private int pageSize = 10;

    @FXML
    public void initialize() {
        viewLoader = new ViewLoader();
        activityList = FXCollections.observableArrayList();

        setupTableColumns();
        setupPagination();
        setupButtonHandlers();
        applyVisualEffects();
        loadDashboardData();

        String officerName = SessionManager.getInstance().getFullName();
        if (officerName == null || officerName.isEmpty()) {
            officerName = SessionManager.getInstance().getUsername();
        }
        welcomeLabel.setText("Welcome, Officer " + (officerName != null ? officerName : "User"));
        statusLabel.setText("Ready");

        // Start auto-refresh every 60 seconds
        startAutoRefresh();
    }

    private void setupTableColumns() {
        activityTimeColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(formatTimestamp(cellData.getValue().get("timestamp"))));
        activityActionColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(getStringValue(cellData.getValue(), "action")));
        activityVehicleColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(getStringValue(cellData.getValue(), "registration_number")));
        activityOfficerColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(getStringValue(cellData.getValue(), "officer_name")));

        activityTimeColumn.setStyle("-fx-alignment: CENTER;");
        activityActionColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        activityVehicleColumn.setStyle("-fx-alignment: CENTER;");
        activityOfficerColumn.setStyle("-fx-alignment: CENTER;");
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

    private void startAutoRefresh() {
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(60),
                        e -> loadDashboardData())
        );
        timeline.setCycleCount(javafx.animation.Timeline.INDEFINITE);
        timeline.play();
    }

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

    private void loadStatistics() throws SQLException {
        List<Map<String, Object>> stolenList = viewLoader.loadActiveStolenVehicles();
        stolenCountLabel.setText(String.valueOf(stolenList != null ? stolenList.size() : 0));

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

        List<Map<String, Object>> warrantsList = viewLoader.loadActiveWarrants();
        activeWarrantsLabel.setText(String.valueOf(warrantsList != null ? warrantsList.size() : 0));

        List<Map<String, Object>> boloList = viewLoader.loadActiveBOLOAlerts();
        boloAlertsLabel.setText(String.valueOf(boloList != null ? boloList.size() : 0));

        List<Map<String, Object>> expiredDocs = viewLoader.loadExpiredDocuments();
        expiredDocsLabel.setText(String.valueOf(expiredDocs != null ? expiredDocs.size() : 0));

        List<Map<String, Object>> geofenceAlerts = viewLoader.loadGeofenceAlerts();
        if (geofenceAlerts != null) {
            long unreadCount = geofenceAlerts.stream()
                    .filter(a -> !Boolean.TRUE.equals(a.get("is_notified")))
                    .count();
            geofenceAlertsLabel.setText(String.valueOf(unreadCount));
        } else {
            geofenceAlertsLabel.setText("0");
        }

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

        List<Map<String, Object>> officerLogs = viewLoader.loadOfficerLogs();
        long todayLogs = officerLogs != null ?
                officerLogs.stream().filter(l -> {
                    Object ts = l.get("timestamp");
                    return ts != null && ts.toString().startsWith(LocalDateTime.now().toLocalDate().toString());
                }).count() : 0;
        activeTrackingLabel.setText(String.valueOf(todayLogs));
    }

    private void loadRecentActivity() throws SQLException {
        List<Map<String, Object>> logs = viewLoader.loadOfficerLogs();
        if (logs != null && !logs.isEmpty()) {
            fullActivityData = logs;
            int totalPages = (int) Math.ceil((double) logs.size() / pageSize);
            if (activityPagination != null) activityPagination.setPageCount(Math.max(1, totalPages));
            updateTablePage();
        }
    }

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

    private void showLoadProgress(boolean show) {
        if (loadProgress != null) loadProgress.setVisible(show);
    }

    private void showOperationProgress(boolean show) {
        if (operationProgress != null) {
            operationProgress.setVisible(show);
            operationProgress.setProgress(0);
        }
    }

    private void updateProgress(double progress) {
        if (operationProgress != null) operationProgress.setProgress(progress);
    }

    private void hideLoadProgressAfterDelay() {
        PauseTransition delay = new PauseTransition(Duration.seconds(1));
        delay.setOnFinished(event -> {
            if (loadProgress != null) loadProgress.setVisible(false);
        });
        delay.play();
    }

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