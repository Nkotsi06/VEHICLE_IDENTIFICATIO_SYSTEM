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
import utils.ValidationUtil;
import dao.OfficerLogDAO;
import models.OfficerLog;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class OfficerLogController {

    @FXML private TableView<OfficerLog> logsTable;
    @FXML private TableColumn<OfficerLog, String> officerColumn;
    @FXML private TableColumn<OfficerLog, String> badgeColumn;
    @FXML private TableColumn<OfficerLog, String> actionColumn;
    @FXML private TableColumn<OfficerLog, String> vehicleColumn;
    @FXML private TableColumn<OfficerLog, String> detailsColumn;
    @FXML private TableColumn<OfficerLog, String> timestampColumn;

    @FXML private ComboBox<String> actionFilterComboBox;
    @FXML private TextField officerNameField;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Button searchButton;
    @FXML private Button refreshButton;
    @FXML private Button exportButton;
    @FXML private Button backButton;
    @FXML private Button fadeButton;

    @FXML private Label totalCountLabel;
    @FXML private Label statusLabel;
    @FXML private ProgressIndicator loadProgress;
    @FXML private ProgressBar operationProgress;
    @FXML private Pagination logsPagination;

    private OfficerLogDAO logDAO;
    private ObservableList<OfficerLog> logList;
    private List<OfficerLog> fullData;
    private int currentPage = 0;
    private int pageSize = 20;

    @FXML
    public void initialize() {
        logDAO = new OfficerLogDAO();
        logList = FXCollections.observableArrayList();

        setupTableColumns();
        setupComboBoxes();
        setupButtonHandlers();
        setupPagination();
        applyVisualEffects();
        loadLogs();

        startDatePicker.setValue(LocalDate.now().minusDays(7));
        endDatePicker.setValue(LocalDate.now());
        statusLabel.setText("Ready");
    }

    private void setupTableColumns() {
        officerColumn.setCellValueFactory(cellData -> cellData.getValue().officerNameProperty());
        badgeColumn.setCellValueFactory(cellData -> cellData.getValue().badgeNumberProperty());
        actionColumn.setCellValueFactory(cellData -> cellData.getValue().actionProperty());
        vehicleColumn.setCellValueFactory(cellData -> cellData.getValue().registrationNumberProperty());
        // Use actionProperty for details column since actionDetailsProperty doesn't exist
        detailsColumn.setCellValueFactory(cellData -> cellData.getValue().actionProperty());
        timestampColumn.setCellValueFactory(cellData -> cellData.getValue().timestampProperty().asString());

        officerColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        badgeColumn.setStyle("-fx-alignment: CENTER;");
        actionColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        vehicleColumn.setStyle("-fx-alignment: CENTER;");
        detailsColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        timestampColumn.setStyle("-fx-alignment: CENTER;");
    }

    private void setupComboBoxes() {
        actionFilterComboBox.getItems().addAll("ALL", "LOGIN", "LOGOUT", "SEARCH_VEHICLE",
                "REPORT_STOLEN", "ADD_VIOLATION", "ISSUE_WARRANT", "GENERATE_BOLO",
                "VERIFY_INSURANCE", "EXPORT_DATA", "UPDATE_PROFILE");
        actionFilterComboBox.setValue("ALL");
    }

    private void setupPagination() {
        if (logsPagination != null) {
            logsPagination.setPageCount(1);
            logsPagination.setMaxPageIndicatorCount(10);
            logsPagination.currentPageIndexProperty().addListener((obs, old, newPage) -> {
                currentPage = newPage.intValue();
                updateTablePage();
            });
        }
    }

    private void updateTablePage() {
        if (fullData == null || fullData.isEmpty()) return;
        int start = currentPage * pageSize;
        int end = Math.min(start + pageSize, fullData.size());
        if (start < fullData.size()) {
            logList.setAll(fullData.subList(start, end));
        }
        totalCountLabel.setText("Total: " + fullData.size() + " | Showing: " + (end - start));
    }

    private void setupButtonHandlers() {
        searchButton.setOnAction(event -> handleSearch());
        refreshButton.setOnAction(event -> loadLogs());
        exportButton.setOnAction(event -> handleExport());
        backButton.setOnAction(event -> SceneManager.getInstance().switchToPoliceView());
        if (fadeButton != null) fadeButton.setOnAction(event -> showFadeAnimation());
    }

    private void applyVisualEffects() {
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(5.0);
        dropShadow.setOffsetX(2.0);
        dropShadow.setOffsetY(2.0);
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.3));

        searchButton.setEffect(dropShadow);
        refreshButton.setEffect(dropShadow);
        exportButton.setEffect(dropShadow);
        backButton.setEffect(dropShadow);
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

    private void loadLogs() {
        showProgress(true);
        statusLabel.setText("Loading officer logs...");

        try {
            List<OfficerLog> logs = logDAO.findAll();
            fullData = logs;
            // Set the table items
            logsTable.setItems(FXCollections.observableArrayList(fullData));
            int totalPages = (int) Math.ceil((double) logs.size() / pageSize);
            if (logsPagination != null) logsPagination.setPageCount(Math.max(1, totalPages));
            updateTablePage();
            statusLabel.setText("Loaded " + logs.size() + " log entries");
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error loading logs");
            AlertUtil.showError("Load Failed", "Failed to load officer logs.");
        } finally {
            showProgress(false);
        }
    }

    private void handleSearch() {
        String actionFilter = actionFilterComboBox.getValue();
        String officerName = officerNameField.getText().trim();

        showOperationProgress(true);
        statusLabel.setText("Searching logs...");
        updateProgress(0.3);

        try {
            List<OfficerLog> results = logDAO.findAll();

            if (!"ALL".equals(actionFilter)) {
                results.removeIf(log -> !log.getAction().equals(actionFilter));
                updateProgress(0.5);
            }

            if (ValidationUtil.isNotEmpty(officerName)) {
                results.removeIf(log -> !log.getOfficerName().toLowerCase().contains(officerName.toLowerCase()));
                updateProgress(0.6);
            }

            if (startDatePicker.getValue() != null && endDatePicker.getValue() != null) {
                LocalDateTime start = startDatePicker.getValue().atStartOfDay();
                LocalDateTime end = endDatePicker.getValue().atTime(23, 59, 59);
                results.removeIf(log -> log.getTimestamp().isBefore(start) || log.getTimestamp().isAfter(end));
                updateProgress(0.7);
            }

            fullData = results;
            int totalPages = (int) Math.ceil((double) results.size() / pageSize);
            if (logsPagination != null) logsPagination.setPageCount(Math.max(1, totalPages));
            updateTablePage();

            updateProgress(1.0);
            statusLabel.setText("Found " + results.size() + " matching records");

            if (results.isEmpty()) {
                AlertUtil.showInfo("No Results", "No log entries found matching your criteria.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Search error: " + e.getMessage());
            AlertUtil.showError("Search Failed", "An error occurred during search.");
        } finally {
            hideProgressAfterDelay();
        }
    }

    private void handleExport() {
        if (fullData == null || fullData.isEmpty()) {
            AlertUtil.showWarning("No Data", "No log data to export.");
            return;
        }

        showOperationProgress(true);
        statusLabel.setText("Exporting logs...");
        updateProgress(0.3);

        try {
            // Convert OfficerLog objects to Map format for export
            List<Map<String, Object>> exportData = new ArrayList<>();

            for (OfficerLog log : fullData) {
                Map<String, Object> row = new HashMap<>();
                row.put("Officer", log.getOfficerName());
                row.put("Badge", log.getBadgeNumber());
                row.put("Action", log.getAction());
                row.put("Vehicle", log.getRegistrationNumber() != null ? log.getRegistrationNumber() : "");
                row.put("Details", log.getAction());
                row.put("Timestamp", log.getTimestamp() != null ? log.getTimestamp().toString() : "");
                exportData.add(row);
            }

            String fileName = "officer_logs_" + java.time.LocalDate.now();
            String[] headers = {"Officer", "Badge", "Action", "Vehicle", "Details", "Timestamp"};

            updateProgress(0.6);
            utils.ExportUtil.exportToCSV(exportData, fileName, headers, headers);

            updateProgress(1.0);
            statusLabel.setText("Export completed!");
            AlertUtil.showSuccess("Export completed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Export failed: " + e.getMessage());
            AlertUtil.showError("Export Failed", "Failed to export logs.");
        } finally {
            hideProgressAfterDelay();
        }
    }

    private void showProgress(boolean show) {
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

    private void hideProgressAfterDelay() {
        PauseTransition delay = new PauseTransition(Duration.seconds(1));
        delay.setOnFinished(event -> {
            if (operationProgress != null) {
                operationProgress.setVisible(false);
                operationProgress.setProgress(0);
            }
            if (loadProgress != null) loadProgress.setVisible(false);
        });
        delay.play();
    }
}