package controllers;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;
import utils.AlertUtil;
import utils.SessionManager;
import dao.SystemHealthDAO;
import models.SystemHealth;

public class DashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label roleLabel;
    @FXML private Label dateLabel;
    @FXML private FlowPane statsContainer;
    @FXML private ProgressIndicator loadProgress;

    private SystemHealthDAO healthDAO;

    @FXML
    public void initialize() {
        healthDAO = new SystemHealthDAO();

        String fullName = SessionManager.getInstance().getFullName();
        String role = SessionManager.getInstance().getUserRole();

        welcomeLabel.setText("Welcome, " + (fullName != null ? fullName : "User"));
        roleLabel.setText("Role: " + (role != null ? role : "Unknown"));
        dateLabel.setText(java.time.LocalDate.now().toString());

        loadDashboardStats();
    }

    private void loadDashboardStats() {
        showProgress(true);
        statsContainer.getChildren().clear();

        try {
            SystemHealth health = healthDAO.getSystemHealth();

            if (health != null) {
                // Core metrics
                addStatCard("Total Vehicles", String.valueOf(health.getTotalVehicles()), "#3498db");
                addStatCard("Total Users", String.valueOf(health.getTotalUsers()), "#2ecc71");
                addStatCard("Active Users", String.valueOf(health.getActiveUsers()), "#27ae60");

                // Recent activity
                addStatCard("New Users (30d)", String.valueOf(health.getNewUsersLast30Days()), "#2980b9");
                addStatCard("New Vehicles (30d)", String.valueOf(health.getNewVehiclesLast30Days()), "#1abc9c");
                addStatCard("Services (30d)", String.valueOf(health.getServicesLast30Days()), "#f39c12");

                // Issues
                int violations = health.getViolationsLast30Days();
                addStatCard("Violations (30d)", String.valueOf(violations), violations > 50 ? "#e74c3c" : "#f1c40f");
                addStatCard("Unread Notifications", String.valueOf(health.getUnreadNotifications()), "#9b59b6");
                addStatCard("Policies Expiring", String.valueOf(health.getPoliciesExpiringSoon()), "#e67e22");

                // System health
                String dbStatus = health.getDatabaseStatus();
                addStatCard("Database Status", dbStatus, "HEALTHY".equals(dbStatus) ? "#27ae60" : "#e74c3c");

                String overallHealth = health.getOverallHealthStatus();
                String healthColor;
                switch (overallHealth) {
                    case "EXCELLENT": healthColor = "#2ecc71"; break;
                    case "GOOD": healthColor = "#27ae60"; break;
                    case "WARNING": healthColor = "#f39c12"; break;
                    case "CRITICAL": healthColor = "#e74c3c"; break;
                    default: healthColor = "#7f8c8d";
                }
                addStatCard("Overall Health", overallHealth, healthColor);
                addStatCard("Uptime", health.getFormattedUptime(), "#95a5a6");

            } else {
                addStatCard("Status", "No data available", "#7f8c8d");
                addStatCard("Hint", "Run database setup script", "#f39c12");
            }

        } catch (Exception e) {
            addStatCard("Error", "Failed to load stats", "#e74c3c");
            addStatCard("Details", e.getMessage(), "#e67e22");
            AlertUtil.showError("Dashboard Error", "Failed to load statistics: " + e.getMessage());
        } finally {
            hideProgressAfterDelay();
        }
    }

    private void addStatCard(String title, String value, String color) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666; -fx-wrap-text: true;");

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

        VBox card = new VBox(5, titleLabel, valueLabel);
        card.setStyle("-fx-padding: 12px; -fx-background-color: #f8f9fa; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 2, 0, 0, 1);");
        card.setPrefWidth(160);
        card.setMinWidth(140);
        card.setMaxWidth(180);

        statsContainer.getChildren().add(card);
    }

    private void showProgress(boolean show) {
        if (loadProgress != null) loadProgress.setVisible(show);
    }

    private void hideProgressAfterDelay() {
        PauseTransition delay = new PauseTransition(Duration.seconds(1));
        delay.setOnFinished(event -> {
            if (loadProgress != null) loadProgress.setVisible(false);
        });
        delay.play();
    }
}