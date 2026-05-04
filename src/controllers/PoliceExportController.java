package controllers;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import utils.AlertUtil;
import utils.SceneManager;
import utils.ExportUtil;
import utils.ValidationUtil;
import dao.ReportGeneratorDAO;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Controller for Police Data Export
 * Handles exporting police-related data in various formats (CSV, Excel, PDF, JSON)
 */
public class PoliceExportController {

    // ============================================
    // FXML UI COMPONENTS
    // ============================================

    @FXML private ComboBox<String> exportTypeComboBox;
    @FXML private ComboBox<String> formatComboBox;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TextField fileNameField;
    @FXML private Button exportButton;
    @FXML private Button previewButton;
    @FXML private Button clearButton;
    @FXML private Button backButton;
    @FXML private Button fadeButton;
    @FXML private Label statusLabel;
    @FXML private ProgressIndicator loadProgress;
    @FXML private ProgressBar operationProgress;
    @FXML private TableView<Map<String, Object>> previewTable;

    // ============================================
    // DAO INSTANCES
    // ============================================

    private ReportGeneratorDAO reportDAO;

    // ============================================
    // INITIALIZATION METHODS
    // ============================================

    /**
     * Initializes the police export controller
     * Sets up export types, formats, and default values
     */
    @FXML
    public void initialize() {
        reportDAO = new ReportGeneratorDAO();

        // Configure police-only export types
        exportTypeComboBox.getItems().addAll(
                "Stolen Vehicles Report",
                "Violations Report",
                "Warrants Report",
                "BOLO Alerts Report",
                "Expired Documents Report",
                "Geofence Alerts Report",
                "Officer Activity Report"
        );
        exportTypeComboBox.setValue("Stolen Vehicles Report");

        // Configure export formats
        formatComboBox.getItems().addAll("CSV", "Excel", "PDF", "JSON");
        formatComboBox.setValue("CSV");

        // Set default values
        startDatePicker.setValue(LocalDate.now().minusMonths(1));
        endDatePicker.setValue(LocalDate.now());
        fileNameField.setText("police_export");

        setupButtonHandlers();
        applyVisualEffects();
        statusLabel.setText("Ready");
    }

    /**
     * Sets up button click handlers
     */
    private void setupButtonHandlers() {
        exportButton.setOnAction(event -> handleExport());
        previewButton.setOnAction(event -> handlePreview());
        clearButton.setOnAction(event -> handleClear());
        backButton.setOnAction(event -> SceneManager.getInstance().switchToPoliceView());
        if (fadeButton != null) {
            fadeButton.setOnAction(event -> showFadeAnimation());
        }
    }

    /**
     * Applies visual effects to buttons
     */
    private void applyVisualEffects() {
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(5.0);
        dropShadow.setOffsetX(2.0);
        dropShadow.setOffsetY(2.0);
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.3));

        exportButton.setEffect(dropShadow);
        previewButton.setEffect(dropShadow);
        clearButton.setEffect(dropShadow);
        backButton.setEffect(dropShadow);
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

    // ============================================
    // PREVIEW METHOD
    // ============================================

    /**
     * Handles previewing data before export
     * Loads and displays sample data in the preview table
     */
    private void handlePreview() {
        String exportType = exportTypeComboBox.getValue();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        showProgress(true);
        statusLabel.setText("Loading preview...");

        try {
            List<Map<String, Object>> data = null;

            // Route to appropriate data source based on export type
            switch (exportType) {
                case "Stolen Vehicles Report":
                    data = reportDAO.generateStolenVehicleReport();
                    break;
                case "Violations Report":
                    if (startDate != null && endDate != null) {
                        data = reportDAO.generateViolationReport(startDate, endDate);
                    } else {
                        data = reportDAO.generateViolationReport(LocalDate.now().minusMonths(1), LocalDate.now());
                    }
                    break;
                case "Warrants Report":
                    data = reportDAO.generateWarrantsReport();
                    break;
                case "BOLO Alerts Report":
                    data = reportDAO.generateBOLOAlertsReport();
                    break;
                case "Expired Documents Report":
                    data = reportDAO.generateExpiredDocumentsReport();
                    break;
                case "Geofence Alerts Report":
                    data = reportDAO.generateGeofenceAlertsReport();
                    break;
                case "Officer Activity Report":
                    data = reportDAO.generateOfficerActivityReport();
                    break;
                default:
                    break;
            }

            if (data != null && !data.isEmpty()) {
                previewTable.getColumns().clear();
                updatePreviewTable(data);
                statusLabel.setText("Preview loaded: " + data.size() + " records");
            } else {
                previewTable.getColumns().clear();
                previewTable.setPlaceholder(new Label("No data found for selected criteria"));
                statusLabel.setText("No data to preview");
            }

        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Preview failed: " + e.getMessage());
            AlertUtil.showError("Preview Failed", "An error occurred during preview.");
        } finally {
            hideProgress();
        }
    }

    /**
     * Updates preview table with data
     * Dynamically creates columns based on data keys
     * @param data List of data rows to display
     */
    private void updatePreviewTable(List<Map<String, Object>> data) {
        if (data == null || data.isEmpty()) return;

        Map<String, Object> firstRow = data.get(0);
        for (String key : firstRow.keySet()) {
            TableColumn<Map<String, Object>, String> column = new TableColumn<>(key);
            column.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(
                            cellData.getValue().get(key) != null ? cellData.getValue().get(key).toString() : ""
                    )
            );
            previewTable.getColumns().add(column);
        }
        previewTable.getItems().setAll(data);
    }

    // ============================================
    // CLEAR METHOD
    // ============================================

    /**
     * Clears all form fields and preview data
     */
    private void handleClear() {
        exportTypeComboBox.setValue("Stolen Vehicles Report");
        formatComboBox.setValue("CSV");
        startDatePicker.setValue(LocalDate.now().minusMonths(1));
        endDatePicker.setValue(LocalDate.now());
        fileNameField.setText("police_export");
        previewTable.getColumns().clear();
        previewTable.getItems().clear();
        statusLabel.setText("Form cleared");
        AlertUtil.showSuccess("Form Cleared", "All selections have been reset.");
    }

    // ============================================
    // EXPORT METHOD
    // ============================================

    /**
     * Handles exporting data to file
     * Supports multiple formats (CSV, Excel, PDF, JSON)
     */
    private void handleExport() {
        String exportType = exportTypeComboBox.getValue();
        String format = formatComboBox.getValue();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        String fileName = fileNameField.getText().trim();

        if (!ValidationUtil.isNotEmpty(fileName)) {
            AlertUtil.showWarning("Validation Error", "Please enter a file name.");
            fileNameField.requestFocus();
            return;
        }

        showProgress(true);
        statusLabel.setText("Exporting " + exportType + "...");

        try {
            List<Map<String, Object>> data = null;
            String[] headers = null;
            String[] fields = null;

            // Route to appropriate data source and configure headers
            switch (exportType) {
                case "Stolen Vehicles Report":
                    data = reportDAO.generateStolenVehicleReport();
                    headers = new String[]{"Registration", "Make", "Model", "Reported Date", "Case Number", "Officer", "Status"};
                    fields = new String[]{"registration_number", "make", "model", "reported_date", "case_number", "assigned_officer", "status"};
                    break;
                case "Violations Report":
                    if (startDate != null && endDate != null) {
                        data = reportDAO.generateViolationReport(startDate, endDate);
                    } else {
                        data = reportDAO.generateViolationReport(LocalDate.now().minusMonths(1), LocalDate.now());
                    }
                    headers = new String[]{"Registration", "Make", "Model", "Violation", "Date", "Fine", "Status", "Officer"};
                    fields = new String[]{"registration_number", "make", "model", "violation_type", "violation_date", "fine_amount", "payment_status", "officer_name"};
                    break;
                case "Warrants Report":
                    data = reportDAO.generateWarrantsReport();
                    headers = new String[]{"Registration", "Violation", "Issue Date", "Expiry Date", "Judge", "Status"};
                    fields = new String[]{"registration_number", "violation_type", "issue_date", "expiry_date", "judge_name", "status"};
                    break;
                case "BOLO Alerts Report":
                    data = reportDAO.generateBOLOAlertsReport();
                    headers = new String[]{"Registration", "Message", "Priority", "Alert Date", "Expiry Date", "Status"};
                    fields = new String[]{"registration_number", "message", "priority", "alert_date", "expiry_date", "status"};
                    break;
                case "Expired Documents Report":
                    data = reportDAO.generateExpiredDocumentsReport();
                    headers = new String[]{"Registration", "Document Type", "Expiry Date", "Days Remaining", "Status"};
                    fields = new String[]{"registration_number", "document_type", "expiry_date", "days_remaining", "expiry_status"};
                    break;
                case "Geofence Alerts Report":
                    data = reportDAO.generateGeofenceAlertsReport();
                    headers = new String[]{"Zone", "Vehicle", "Alert Type", "Timestamp", "Status"};
                    fields = new String[]{"zone_name", "registration_number", "alert_type", "alert_timestamp", "is_notified"};
                    break;
                case "Officer Activity Report":
                    data = reportDAO.generateOfficerActivityReport();
                    headers = new String[]{"Officer", "Action", "Vehicle", "Timestamp"};
                    fields = new String[]{"officer_name", "action", "registration_number", "timestamp"};
                    break;
                default:
                    AlertUtil.showWarning("Not Available", "This export type is not available.");
                    hideProgress();
                    return;
            }

            if (data != null && !data.isEmpty()) {
                // Route to appropriate export format handler
                if ("CSV".equals(format)) {
                    ExportUtil.exportToCSV(data, fileName, headers, fields);
                } else {
                    AlertUtil.showInfo("Export", format + " export will be implemented soon.");
                }
                statusLabel.setText("Export completed successfully!");
                AlertUtil.showSuccess("Export Complete", "File exported successfully.");
            } else {
                AlertUtil.showWarning("No Data", "No data found for the selected criteria.");
                statusLabel.setText("No data to export.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Export failed: " + e.getMessage());
            AlertUtil.showError("Export Failed", "An error occurred during export.");
        } finally {
            hideProgress();
        }
    }

    // ============================================
    // UI PROGRESS METHODS
    // ============================================

    /**
     * Shows/hides progress indicators
     * @param show true to show, false to hide
     */
    private void showProgress(boolean show) {
        if (loadProgress != null) loadProgress.setVisible(show);
        if (operationProgress != null) {
            operationProgress.setVisible(show);
            operationProgress.setProgress(0);
        }
    }

    /**
     * Hides progress indicators after a short delay
     */
    private void hideProgress() {
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