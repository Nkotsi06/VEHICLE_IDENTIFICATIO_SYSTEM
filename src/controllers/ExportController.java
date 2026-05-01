package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import utils.AlertUtil;
import utils.SceneManager;
import utils.ExportUtil;
import dao.ReportGeneratorDAO;
import java.util.List;
import java.util.Map;

public class ExportController {

    @FXML private ComboBox<String> exportTypeComboBox;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TextField fileNameField;
    @FXML private Button exportButton;
    @FXML private Button backButton;
    @FXML private Label statusLabel;

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
                "Audit Logs"
        );
        exportTypeComboBox.setValue("Vehicle Report");

        startDatePicker.setValue(java.time.LocalDate.now().minusMonths(1));
        endDatePicker.setValue(java.time.LocalDate.now());

        fileNameField.setText("export_report");

        setupButtonHandlers();
    }

    private void setupButtonHandlers() {
        exportButton.setOnAction(event -> handleExport());
        backButton.setOnAction(event -> handleBack());
    }

    private void handleExport() {
        String exportType = exportTypeComboBox.getValue();
        java.time.LocalDate startDate = startDatePicker.getValue();
        java.time.LocalDate endDate = endDatePicker.getValue();
        String fileName = fileNameField.getText().trim();

        if (!utils.ValidationUtil.isNotEmpty(fileName)) {
            AlertUtil.showWarning("Validation Error", "Please enter a file name.");
            fileNameField.requestFocus();
            return;
        }

        try {
            statusLabel.setText("Exporting... Please wait.");

            List<Map<String, Object>> data = null;
            String[] headers = null;
            String[] fields = null;

            switch (exportType) {
                case "Vehicle Report":
                    data = reportDAO.generateVehicleReport(startDate, endDate);
                    headers = new String[]{"Registration", "Make", "Model", "Year", "Owner", "Status", "Services", "Total Cost"};
                    fields = new String[]{"registration_number", "make", "model", "year", "owner_name", "status_name", "service_count", "total_service_cost"};
                    break;
                case "Violation Report":
                    data = reportDAO.generateViolationReport(startDate, endDate);
                    headers = new String[]{"Registration", "Make", "Model", "Violation", "Date", "Fine", "Status", "Officer"};
                    fields = new String[]{"registration_number", "make", "model", "violation_type", "violation_date", "fine_amount", "payment_status", "officer_name"};
                    break;
                case "Financial Report":
                    data = reportDAO.generateFinancialReport(startDate, endDate);
                    headers = new String[]{"Transaction Type", "Date", "Amount", "Reference"};
                    fields = new String[]{"type", "date", "amount", "reference"};
                    break;
                case "Stolen Vehicle Report":
                    data = reportDAO.generateStolenVehicleReport();
                    headers = new String[]{"Registration", "Make", "Model", "Reported Date", "Case Number", "Officer"};
                    fields = new String[]{"registration_number", "make", "model", "reported_date", "case_number", "assigned_officer"};
                    break;
                case "Expired Documents Report":
                    data = reportDAO.generateExpiredDocumentsReport();
                    headers = new String[]{"Registration", "Document Type", "Expiry Date", "Days Remaining", "Status"};
                    fields = new String[]{"registration_number", "document_type", "expiry_date", "days_remaining", "expiry_status"};
                    break;
                default:
                    AlertUtil.showWarning("Not Available", "This export type is not available yet.");
                    statusLabel.setText("");
                    return;
            }

            if (data != null && !data.isEmpty()) {
                ExportUtil.exportToCSV(data, fileName, headers, fields);
                statusLabel.setText("Export completed successfully!");
            } else {
                AlertUtil.showWarning("No Data", "No data found for the selected criteria.");
                statusLabel.setText("No data to export.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Export failed: " + e.getMessage());
            AlertUtil.showError("Export Failed", "An error occurred during export.");
        }
    }

    private void handleBack() {
        SceneManager.getInstance().switchToDashboard();
    }
}