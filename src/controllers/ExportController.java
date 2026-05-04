package controllers;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Duration;
import utils.AlertUtil;
import utils.SceneManager;
import utils.ExportUtil;
import utils.ValidationUtil;
import dao.ReportGeneratorDAO;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class ExportController {

    @FXML private ComboBox<String> exportTypeComboBox;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TextField fileNameField;
    @FXML private Button exportButton;
    @FXML private Button cancelButton;
    @FXML private Button backButton;
    @FXML private Label statusLabel;
    @FXML private ProgressIndicator loadProgress;
    @FXML private ProgressBar operationProgress;

    private ReportGeneratorDAO reportDAO;

    @FXML
    public void initialize() {
        reportDAO = new ReportGeneratorDAO();

        exportTypeComboBox.getItems().addAll(
                "Vehicle Report",
                "Violation Report",
                "Financial Report",
                "Stolen Vehicle Report",
                "Expired Documents Report",
                "Workshop Performance Report",
                "Audit Logs"
        );
        exportTypeComboBox.setValue("Vehicle Report");

        startDatePicker.setValue(LocalDate.now().minusMonths(1));
        endDatePicker.setValue(LocalDate.now());
        fileNameField.setText("export_report");

        setupButtonHandlers();
        statusLabel.setText("Ready");
    }

    private void setupButtonHandlers() {
        exportButton.setOnAction(event -> handleExport());
        cancelButton.setOnAction(event -> handleCancel());
        backButton.setOnAction(event -> SceneManager.getInstance().switchToDashboard());
    }

    private void handleCancel() {
        exportTypeComboBox.setValue("Vehicle Report");
        startDatePicker.setValue(LocalDate.now().minusMonths(1));
        endDatePicker.setValue(LocalDate.now());
        fileNameField.setText("export_report");
        statusLabel.setText("Form cleared");
        AlertUtil.showSuccess("Form Cleared", "Export settings have been reset.");
    }

    private void handleExport() {
        String exportType = exportTypeComboBox.getValue();
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

            switch (exportType) {
                case "Vehicle Report":
                    if (startDate != null && endDate != null) {
                        data = reportDAO.generateVehicleReport(startDate, endDate);
                    }
                    headers = new String[]{"Registration", "Make", "Model", "Year", "Owner", "Status", "Services", "Total Cost"};
                    fields = new String[]{"registration_number", "make", "model", "year", "owner_name", "status_name", "service_count", "total_service_cost"};
                    break;
                case "Violation Report":
                    if (startDate != null && endDate != null) {
                        data = reportDAO.generateViolationReport(startDate, endDate);
                    }
                    headers = new String[]{"Registration", "Make", "Model", "Violation", "Date", "Fine", "Status", "Officer"};
                    fields = new String[]{"registration_number", "make", "model", "violation_type", "violation_date", "fine_amount", "payment_status", "officer_name"};
                    break;
                case "Financial Report":
                    if (startDate != null && endDate != null) {
                        data = reportDAO.generateFinancialReport(startDate, endDate);
                    }
                    headers = new String[]{"Transaction Type", "Date", "Amount", "Reference"};
                    fields = new String[]{"type", "date", "amount", "reference"};
                    break;
                case "Stolen Vehicle Report":
                    data = reportDAO.generateStolenVehicleReport();
                    headers = new String[]{"Registration", "Make", "Model", "Reported Date", "Case Number", "Officer", "Status"};
                    fields = new String[]{"registration_number", "make", "model", "reported_date", "case_number", "assigned_officer", "status"};
                    break;
                case "Expired Documents Report":
                    data = reportDAO.generateExpiredDocumentsReport();
                    headers = new String[]{"Registration", "Document Type", "Expiry Date", "Days Remaining", "Status"};
                    fields = new String[]{"registration_number", "document_type", "expiry_date", "days_remaining", "expiry_status"};
                    break;
                case "Workshop Performance Report":
                    data = reportDAO.generateWorkshopPerformanceReport();
                    headers = new String[]{"Workshop", "Services", "Total Revenue", "Avg Cost", "Rating"};
                    fields = new String[]{"workshop_name", "service_count", "total_revenue", "average_service_cost", "avg_rating"};
                    break;
                case "Audit Logs":
                    data = reportDAO.generateAuditLogsReport();
                    headers = new String[]{"User", "Action", "Timestamp", "IP Address"};
                    fields = new String[]{"username", "action", "timestamp", "ip_address"};
                    break;
                default:
                    AlertUtil.showWarning("Not Available", "This export type is not available yet.");
                    hideProgressAfterDelay();
                    return;
            }

            if (data != null && !data.isEmpty()) {
                ExportUtil.exportToCSV(data, fileName, headers, fields);
                statusLabel.setText("Export completed successfully!");
                AlertUtil.showSuccess("Export Complete", "File exported successfully to reports directory.");
            } else {
                statusLabel.setText("No data found for the selected criteria.");
                AlertUtil.showWarning("No Data", "No data found for the selected export type and date range.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Export failed: " + e.getMessage());
            AlertUtil.showError("Export Failed", "An error occurred during export: " + e.getMessage());
        } finally {
            hideProgressAfterDelay();
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