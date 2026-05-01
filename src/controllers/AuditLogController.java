package controllers;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.util.Duration;
import utils.AlertUtil;
import utils.SceneManager;
import utils.ValidationUtil;
import dao.AuditDAO;
import models.AuditLog;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AuditLogController {

    @FXML private TableView<AuditLog> auditTable;
    @FXML private TableColumn<AuditLog, String> usernameColumn;
    @FXML private TableColumn<AuditLog, String> actionColumn;
    @FXML private TableColumn<AuditLog, String> timestampColumn;
    @FXML private TableColumn<AuditLog, String> ipAddressColumn;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> actionFilterComboBox;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Button searchButton;
    @FXML private Button refreshButton;
    @FXML private Button exportButton;
    @FXML private Button clearLogsButton;
    @FXML private Button backButton;
    @FXML private Button fadeButton;
    @FXML private Label totalCountLabel;
    @FXML private ProgressIndicator loadProgress;
    @FXML private ProgressBar operationProgress;

    private AuditDAO auditDAO;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @FXML
    public void initialize() {
        auditDAO = new AuditDAO();

        setupTableColumns();
        loadAllLogs();
        setupFilters();
        setupButtonHandlers();
        applyVisualEffects();
    }

    private void setupTableColumns() {
        usernameColumn.setCellValueFactory(cellData -> cellData.getValue().usernameProperty());
        actionColumn.setCellValueFactory(cellData -> cellData.getValue().actionProperty());
        timestampColumn.setCellValueFactory(cellData -> cellData.getValue().timestampProperty().asString());
        ipAddressColumn.setCellValueFactory(cellData -> cellData.getValue().ipAddressProperty());

        usernameColumn.setStyle("-fx-alignment: CENTER;");
        actionColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        timestampColumn.setStyle("-fx-alignment: CENTER;");
        ipAddressColumn.setStyle("-fx-alignment: CENTER;");
    }

    private void loadAllLogs() {
        showLoadProgress(true);

        try {
            java.util.List<AuditLog> logs = auditDAO.findAll();
            auditTable.getItems().setAll(logs);
            totalCountLabel.setText("Total: " + logs.size());
            System.out.println("Loaded " + logs.size() + " audit logs");
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Load Failed", "Failed to load audit logs: " + e.getMessage());
        } finally {
            showLoadProgress(false);
        }
    }

    private void setupFilters() {
        actionFilterComboBox.getItems().addAll("ALL", "LOGIN", "LOGOUT", "CREATE", "UPDATE", "DELETE", "SEARCH", "EXPORT");
        actionFilterComboBox.setValue("ALL");

        startDatePicker.setValue(LocalDateTime.now().minusDays(30).toLocalDate());
        endDatePicker.setValue(LocalDateTime.now().toLocalDate());
    }

    private void setupButtonHandlers() {
        searchButton.setOnAction(event -> handleSearch());
        refreshButton.setOnAction(event -> loadAllLogs());
        exportButton.setOnAction(event -> handleExport());
        clearLogsButton.setOnAction(event -> handleClearLogs());
        backButton.setOnAction(event -> SceneManager.getInstance().switchToAdminView());

        if (fadeButton != null) {
            fadeButton.setOnAction(event -> showFadeAnimation());
        }
    }

    private void handleSearch() {
        String searchTerm = searchField.getText().trim();
        String actionFilter = actionFilterComboBox.getValue();
        LocalDateTime startDate = startDatePicker.getValue() != null ?
                startDatePicker.getValue().atStartOfDay() : null;
        LocalDateTime endDate = endDatePicker.getValue() != null ?
                endDatePicker.getValue().atTime(23, 59, 59) : null;

        showLoadProgress(true);

        try {
            java.util.List<AuditLog> results = auditDAO.findAll();

            if (ValidationUtil.isNotEmpty(searchTerm)) {
                String lowerSearch = searchTerm.toLowerCase();
                results.removeIf(log -> !log.getUsername().toLowerCase().contains(lowerSearch) &&
                        !log.getAction().toLowerCase().contains(lowerSearch));
            }

            if (!"ALL".equals(actionFilter)) {
                results.removeIf(log -> !log.getAction().contains(actionFilter));
            }

            if (startDate != null) {
                results.removeIf(log -> log.getTimestamp().isBefore(startDate));
            }
            if (endDate != null) {
                results.removeIf(log -> log.getTimestamp().isAfter(endDate));
            }

            auditTable.getItems().setAll(results);
            totalCountLabel.setText("Total: " + results.size());

            if (results.isEmpty()) {
                AlertUtil.showInfo("No Results", "No audit logs found matching your criteria.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Search Failed", "An error occurred during search: " + e.getMessage());
        } finally {
            showLoadProgress(false);
        }
    }

    private void handleExport() {
        showOperationProgress(true);

        try {
            updateProgress(0.3);
            String fileName = "audit_logs_" + java.time.LocalDate.now();
            updateProgress(0.6);
            utils.ExportUtil.exportToCSV(auditTable, fileName);
            updateProgress(1.0);
            AlertUtil.showSuccess("Export completed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Export Failed", "Failed to export audit logs: " + e.getMessage());
        } finally {
            hideProgressWithDelay();
        }
    }

    private void handleClearLogs() {
        boolean confirmed = AlertUtil.showConfirmation("Clear Logs",
                "Are you sure you want to delete all audit logs older than 30 days?\n\nThis action cannot be undone.");

        if (confirmed) {
            showOperationProgress(true);

            try {
                updateProgress(0.5);
                int deleted = auditDAO.deleteLogsOlderThanDays(30);
                updateProgress(1.0);
                AlertUtil.showSuccess(deleted + " audit logs deleted successfully.");
                loadAllLogs();
            } catch (Exception e) {
                e.printStackTrace();
                AlertUtil.showError("Clear Failed", "Failed to clear audit logs: " + e.getMessage());
            } finally {
                hideProgressWithDelay();
            }
        }
    }

    private void applyVisualEffects() {
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(5.0);
        dropShadow.setOffsetX(2.0);
        dropShadow.setOffsetY(2.0);

        searchButton.setEffect(dropShadow);
        refreshButton.setEffect(dropShadow);
        exportButton.setEffect(dropShadow);
        clearLogsButton.setEffect(dropShadow);
        backButton.setEffect(dropShadow);

        if (fadeButton != null) {
            fadeButton.setEffect(dropShadow);
        }
    }

    private void showFadeAnimation() {
        if (fadeButton != null) {
            FadeTransition fadeTransition = new FadeTransition(Duration.seconds(2), fadeButton);
            fadeTransition.setFromValue(1.0);
            fadeTransition.setToValue(0.2);
            fadeTransition.setCycleCount(3);
            fadeTransition.setAutoReverse(true);
            fadeTransition.play();
            AlertUtil.showInfo("Fade Animation", "Button fading animation played!");
        }
    }

    private void showLoadProgress(boolean show) {
        if (loadProgress != null) {
            loadProgress.setVisible(show);
        }
    }

    private void showOperationProgress(boolean show) {
        if (operationProgress != null) {
            operationProgress.setVisible(show);
            operationProgress.setProgress(0);
        }
    }

    private void updateProgress(double progress) {
        if (operationProgress != null) {
            operationProgress.setProgress(progress);
        }
    }

    private void hideProgressWithDelay() {
        PauseTransition delay = new PauseTransition(Duration.seconds(1));
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