package controllers;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import utils.AlertUtil;
import utils.SceneManager;
import utils.SessionManager;
import dao.SystemHealthDAO;
import models.SystemHealth;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class SystemHealthController {

    @FXML private Label totalUsersLabel;
    @FXML private Label activeUsersLabel;
    @FXML private Label inactiveUsersLabel;
    @FXML private Label totalVehiclesLabel;
    @FXML private Label stolenVehiclesLabel;
    @FXML private Label unpaidFinesLabel;
    @FXML private Label pendingQueriesLabel;
    @FXML private Label pendingWorkshopsLabel;
    @FXML private Label pendingClaimsLabel;
    @FXML private Label lastUpdatedLabel;
    @FXML private Label databaseStatusLabel;
    @FXML private Label statusLabel;
    @FXML private ProgressBar systemLoadProgress;
    @FXML private BarChart<String, Number> monthlyRegistrationsChart;
    @FXML private PieChart vehicleStatusPieChart;
    @FXML private VBox healthMetricsBox;
    @FXML private Button refreshButton;
    @FXML private Button backButton;
    @FXML private Button fadeButton;
    @FXML private ProgressIndicator loadProgress;

    private SystemHealthDAO systemHealthDAO;
    private javafx.animation.Timeline autoRefreshTimeline;

    @FXML
    public void initialize() {
        systemHealthDAO = new SystemHealthDAO();

        setupButtonHandlers();
        applyVisualEffects();
        setupChartStyles();
        loadSystemHealthData();

        statusLabel.setText("Ready");

        // Start auto-refresh every 60 seconds
        startAutoRefresh();
    }

    private void setupButtonHandlers() {
        if (refreshButton != null) {
            refreshButton.setOnAction(event -> {
                refreshButton.setText("Refreshing...");
                loadSystemHealthData();
                PauseTransition reset = new PauseTransition(Duration.seconds(1.5));
                reset.setOnFinished(e -> refreshButton.setText("Refresh Data"));
                reset.play();
            });
        }
        if (backButton != null) {
            backButton.setOnAction(event -> {
                String role = SessionManager.getInstance().getUserRole();
                if ("ADMIN".equals(role)) {
                    SceneManager.getInstance().switchToAdminView();
                } else {
                    SceneManager.getInstance().switchToDashboard();
                }
            });
        }
        if (fadeButton != null) {
            fadeButton.setOnAction(event -> showFadeAnimation());
        }

        // Hover effects for buttons
        if (refreshButton != null) {
            refreshButton.setOnMouseEntered(e -> refreshButton.setStyle("-fx-background-color: #008000; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-pref-width: 120; -fx-padding: 8 15; -fx-border-radius: 5; -fx-background-radius: 5;"));
            refreshButton.setOnMouseExited(e -> refreshButton.setStyle("-fx-background-color: #006400; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-pref-width: 120; -fx-padding: 8 15; -fx-border-radius: 5; -fx-background-radius: 5;"));
        }

        if (backButton != null) {
            backButton.setOnMouseEntered(e -> backButton.setStyle("-fx-background-color: #1a252f; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-pref-width: 150; -fx-padding: 8 15; -fx-border-radius: 5; -fx-background-radius: 5;"));
            backButton.setOnMouseExited(e -> backButton.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-pref-width: 150; -fx-padding: 8 15; -fx-border-radius: 5; -fx-background-radius: 5;"));
        }

        if (fadeButton != null) {
            fadeButton.setOnMouseEntered(e -> fadeButton.setStyle("-fx-background-color: #e6b800; -fx-text-fill: #2c3e50; -fx-font-weight: bold; -fx-cursor: hand; -fx-pref-width: 100; -fx-padding: 8 15; -fx-border-radius: 5; -fx-background-radius: 5;"));
            fadeButton.setOnMouseExited(e -> fadeButton.setStyle("-fx-background-color: #f1c40f; -fx-text-fill: #2c3e50; -fx-font-weight: bold; -fx-cursor: hand; -fx-pref-width: 100; -fx-padding: 8 15; -fx-border-radius: 5; -fx-background-radius: 5;"));
        }
    }

    private void applyVisualEffects() {
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(5.0);
        dropShadow.setOffsetX(2.0);
        dropShadow.setOffsetY(2.0);
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.3));

        if (refreshButton != null) refreshButton.setEffect(dropShadow);
        if (backButton != null) backButton.setEffect(dropShadow);
        if (fadeButton != null) fadeButton.setEffect(dropShadow);

        DropShadow chartShadow = new DropShadow();
        chartShadow.setRadius(3.0);
        chartShadow.setOffsetX(2.0);
        chartShadow.setOffsetY(2.0);
        chartShadow.setColor(Color.rgb(0, 0, 0, 0.2));

        if (monthlyRegistrationsChart != null) monthlyRegistrationsChart.setEffect(chartShadow);
        if (vehicleStatusPieChart != null) vehicleStatusPieChart.setEffect(chartShadow);
    }

    private void setupChartStyles() {
        if (monthlyRegistrationsChart != null) {
            monthlyRegistrationsChart.setBarGap(2);
            monthlyRegistrationsChart.setCategoryGap(15);
        }

        if (vehicleStatusPieChart != null) {
            vehicleStatusPieChart.setLabelsVisible(true);
            vehicleStatusPieChart.setLabelLineLength(10);
        }
    }

    private void startAutoRefresh() {
        if (autoRefreshTimeline != null) {
            autoRefreshTimeline.stop();
        }
        autoRefreshTimeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(60),
                        e -> loadSystemHealthData())
        );
        autoRefreshTimeline.setCycleCount(javafx.animation.Timeline.INDEFINITE);
        autoRefreshTimeline.play();
        statusLabel.setText("Auto-refresh enabled (every 60 seconds)");
    }

    public void stopAutoRefresh() {
        if (autoRefreshTimeline != null) {
            autoRefreshTimeline.stop();
        }
    }

    private void loadSystemHealthData() {
        showLoadProgress(true);
        statusLabel.setText("Loading system health data...");

        // Run database operations in background thread
        new Thread(() -> {
            try {
                // Load health metrics
                SystemHealth health = systemHealthDAO.getSystemHealth();

                // Load chart data
                List<Map<String, Object>> monthlyData = systemHealthDAO.getMonthlyRegistrations();
                List<Map<String, Object>> statusData = systemHealthDAO.getVehicleStatusDistribution();
                double unpaidFines = systemHealthDAO.getTotalUnpaidFines();
                int pendingQueries = systemHealthDAO.getPendingQueries();
                int pendingWorkshops = systemHealthDAO.getPendingWorkshops();
                int pendingClaims = systemHealthDAO.getPendingClaims();

                // Get stolen vehicles count - handle SQLException properly
                int stolenCount = 0;
                try {
                    stolenCount = systemHealthDAO.getActiveStolenCount();
                } catch (SQLException e) {
                    e.printStackTrace();
                    // Log error but continue with default value
                    System.err.println("Failed to get stolen vehicles count: " + e.getMessage());
                }

                final int finalStolenCount = stolenCount;

                Platform.runLater(() -> {
                    try {
                        updateHealthMetrics(health, unpaidFines, finalStolenCount);
                        updatePendingItems(pendingQueries, pendingWorkshops, pendingClaims);
                        updateMonthlyChart(monthlyData);
                        updatePieChart(statusData);
                        updateLastUpdated();
                        statusLabel.setText("Data loaded successfully");
                    } catch (Exception e) {
                        statusLabel.setText("Error updating UI: " + e.getMessage());
                    } finally {
                        hideLoadProgressAfterDelay();
                    }
                });
            } catch (SQLException e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    statusLabel.setText("Error: " + e.getMessage());
                    AlertUtil.showError("Database Error", "Failed to load system health data: " + e.getMessage());
                    hideLoadProgressAfterDelay();
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    statusLabel.setText("Error: " + e.getMessage());
                    hideLoadProgressAfterDelay();
                });
            }
        }).start();
    }

    private void updateHealthMetrics(SystemHealth health, double unpaidFines, int stolenCount) {
        if (health != null) {
            totalUsersLabel.setText(String.valueOf(health.getTotalUsers()));
            activeUsersLabel.setText(String.valueOf(health.getActiveUsers()));
            inactiveUsersLabel.setText(String.valueOf(health.getInactiveUsers()));
            totalVehiclesLabel.setText(String.valueOf(health.getTotalVehicles()));

            // Use the stolen count we safely retrieved
            stolenVehiclesLabel.setText(String.valueOf(stolenCount));

            unpaidFinesLabel.setText(String.format("M%,.2f", unpaidFines));

            // Calculate system load based on various metrics
            int totalItems = health.getViolationsLast30Days() +
                    health.getUnreadNotifications() +
                    health.getPoliciesExpiringSoon();
            double load = Math.min(1.0, totalItems / 100.0);
            systemLoadProgress.setProgress(load);

            databaseStatusLabel.setText("Connected");
            databaseStatusLabel.setStyle("-fx-text-fill: #27ae60;");
        } else {
            setDefaultHealthMetrics();
            databaseStatusLabel.setText("Unknown");
            databaseStatusLabel.setStyle("-fx-text-fill: #E31E2C;");
        }
    }

    private void updatePendingItems(int queries, int workshops, int claims) {
        pendingQueriesLabel.setText(String.valueOf(queries));
        pendingWorkshopsLabel.setText(String.valueOf(workshops));
        pendingClaimsLabel.setText(String.valueOf(claims));
    }

    private void setDefaultHealthMetrics() {
        totalUsersLabel.setText("0");
        activeUsersLabel.setText("0");
        inactiveUsersLabel.setText("0");
        totalVehiclesLabel.setText("0");
        stolenVehiclesLabel.setText("0");
        unpaidFinesLabel.setText("M0.00");
        pendingQueriesLabel.setText("0");
        pendingWorkshopsLabel.setText("0");
        pendingClaimsLabel.setText("0");
        systemLoadProgress.setProgress(0);
    }

    private void updateMonthlyChart(List<Map<String, Object>> monthlyData) {
        if (monthlyRegistrationsChart == null) return;

        monthlyRegistrationsChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Vehicle Registrations");

        if (monthlyData != null && !monthlyData.isEmpty()) {
            for (Map<String, Object> row : monthlyData) {
                String monthName = (String) row.get("month_name");
                Object countObj = row.get("count");
                int count = 0;
                if (countObj instanceof Number) {
                    count = ((Number) countObj).intValue();
                }
                series.getData().add(new XYChart.Data<>(monthName, count));
            }
            monthlyRegistrationsChart.getData().add(series);
        } else {
            // Show empty state with subtle message
            series.getData().add(new XYChart.Data<>("No Data", 0));
            monthlyRegistrationsChart.getData().add(series);
            monthlyRegistrationsChart.setLegendVisible(true);
        }

        // Style the series
        if (!monthlyRegistrationsChart.getData().isEmpty()) {
            javafx.scene.chart.XYChart.Series<String, Number> firstSeries =
                    monthlyRegistrationsChart.getData().get(0);
            for (javafx.scene.chart.XYChart.Data<String, Number> data : firstSeries.getData()) {
                if (data.getNode() != null) {
                    data.getNode().setStyle("-fx-bar-fill: #006400;");
                }
            }
        }
    }

    private void updatePieChart(List<Map<String, Object>> statusData) {
        if (vehicleStatusPieChart == null) return;

        vehicleStatusPieChart.getData().clear();

        if (statusData != null && !statusData.isEmpty()) {
            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

            // Color mapping for pie chart slices
            String[] colors = {"#006400", "#27ae60", "#E31E2C", "#f1c40f", "#2c3e50", "#3498db"};
            int colorIndex = 0;

            for (Map<String, Object> row : statusData) {
                String statusName = (String) row.get("status_name");
                Object countObj = row.get("count");
                int count = 0;
                if (countObj instanceof Number) {
                    count = ((Number) countObj).intValue();
                }
                if (count > 0) {
                    pieData.add(new PieChart.Data(statusName, count));
                }
            }

            if (!pieData.isEmpty()) {
                vehicleStatusPieChart.setData(pieData);
                // Apply colors to pie slices
                int idx = 0;
                for (PieChart.Data data : pieData) {
                    String color = colors[idx % colors.length];
                    if (data.getNode() != null) {
                        data.getNode().setStyle("-fx-pie-color: " + color + ";");
                    }
                    idx++;
                }
            } else {
                // Show empty state
                PieChart.Data noData = new PieChart.Data("No Data Available", 1);
                vehicleStatusPieChart.getData().add(noData);
                if (noData.getNode() != null) {
                    noData.getNode().setStyle("-fx-pie-color: #7f8c8d;");
                }
            }
        } else {
            // Show empty state with subtle message
            PieChart.Data noData = new PieChart.Data("No Data Available", 1);
            vehicleStatusPieChart.getData().add(noData);
            if (noData.getNode() != null) {
                noData.getNode().setStyle("-fx-pie-color: #7f8c8d;");
            }
        }
    }

    private void updateLastUpdated() {
        if (lastUpdatedLabel != null) {
            lastUpdatedLabel.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
    }

    private void showFadeAnimation() {
        if (fadeButton != null) {
            FadeTransition fadeTransition = new FadeTransition(Duration.seconds(1.5), fadeButton);
            fadeTransition.setFromValue(1.0);
            fadeTransition.setToValue(0.2);
            fadeTransition.setCycleCount(4);
            fadeTransition.setAutoReverse(true);
            fadeTransition.play();
            statusLabel.setText("Fade animation played!");
            AlertUtil.showInfo("Fade Animation", "Button fading animation completed!");

            PauseTransition reset = new PauseTransition(Duration.seconds(2));
            reset.setOnFinished(e -> statusLabel.setText("Ready"));
            reset.play();
        }
    }

    private void showLoadProgress(boolean show) {
        if (loadProgress != null) {
            loadProgress.setVisible(show);
        }
    }

    private void hideLoadProgressAfterDelay() {
        PauseTransition delay = new PauseTransition(Duration.seconds(1));
        delay.setOnFinished(event -> {
            if (loadProgress != null) {
                loadProgress.setVisible(false);
            }
        });
        delay.play();
    }
}