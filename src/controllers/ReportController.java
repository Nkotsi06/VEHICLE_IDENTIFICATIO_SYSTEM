package controllers;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Duration;
import utils.AlertUtil;
import utils.SceneManager;
import utils.ExportUtil;
import dao.ReportGeneratorDAO;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class ReportController {

    @FXML
    private ComboBox<String> reportTypeComboBox;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;
    @FXML
    private Button generateButton;
    @FXML
    private Button exportButton;
    @FXML
    private Button printButton;
    @FXML
    private Button backButton;
    @FXML
    private Button clearButton;
    @FXML
    private Button fadeButton;

    @FXML
    private TableView<Map<String, Object>> reportTable;
    @FXML
    private TextArea reportSummaryArea;
    @FXML
    private Label recordCountLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private ProgressIndicator loadProgress;
    @FXML
    private ProgressBar operationProgress;

    private ReportGeneratorDAO reportDAO;
    private ObservableList<Map<String, Object>> reportData;
    private String currentReportType;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @FXML
    public void initialize() {
        reportDAO = new ReportGeneratorDAO();
        reportData = FXCollections.observableArrayList();

        setupReportTypes();
        setupDatePickers();
        setupButtonHandlers();
        applyVisualEffects();

        statusLabel.setText("Ready");
        reportTable.setItems(reportData);
    }

    private void setupReportTypes() {
        reportTypeComboBox.getItems().addAll(
                "Vehicle Report",
                "Violation Report",
                "Financial Report",
                "Stolen Vehicle Report",
                "Expired Documents Report",
                "Workshop Performance Report",
                "Summary Statistics"
        );
        reportTypeComboBox.setValue("Vehicle Report");
    }

    private void setupDatePickers() {
        startDatePicker.setValue(LocalDate.now().minusMonths(1));
        endDatePicker.setValue(LocalDate.now());
    }

    private void setupButtonHandlers() {
        generateButton.setOnAction(event -> handleGenerate());
        exportButton.setOnAction(event -> handleExport());
        printButton.setOnAction(event -> handlePrint());
        backButton.setOnAction(event -> handleBack());

        if (clearButton != null) {
            clearButton.setOnAction(event -> clearForm());
        }

        if (fadeButton != null) {
            fadeButton.setOnAction(event -> showFadeAnimation());
        }
    }

    private void handleGenerate() {
        currentReportType = reportTypeComboBox.getValue();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        if (startDate == null || endDate == null) {
            AlertUtil.showWarning("Date Required", "Please select both start and end dates.");
            return;
        }

        if (startDate.isAfter(endDate)) {
            AlertUtil.showWarning("Invalid Date Range", "Start date must be before end date.");
            return;
        }

        showOperationProgress(true);
        statusLabel.setText("Generating " + currentReportType + "...");
        updateProgress(0.2);

        try {
            updateProgress(0.4);
            List<Map<String, Object>> data = null;

            switch (currentReportType) {
                case "Vehicle Report":
                    data = reportDAO.generateVehicleReport(startDate, endDate);
                    displayVehicleReport(data);
                    break;
                case "Violation Report":
                    data = reportDAO.generateViolationReport(startDate, endDate);
                    displayViolationReport(data);
                    break;
                case "Financial Report":
                    data = reportDAO.generateFinancialReport(startDate, endDate);
                    displayFinancialReport(data);
                    break;
                case "Stolen Vehicle Report":
                    data = reportDAO.generateStolenVehicleReport();
                    displayStolenVehicleReport(data);
                    break;
                case "Expired Documents Report":
                    data = reportDAO.generateExpiredDocumentsReport();
                    displayExpiredDocumentsReport(data);
                    break;
                case "Workshop Performance Report":
                    data = reportDAO.generateWorkshopPerformanceReport();
                    displayWorkshopPerformanceReport(data);
                    break;
                case "Summary Statistics":
                    updateProgress(0.6);
                    Map<String, Object> stats = reportDAO.generateSummaryStatistics();
                    displaySummaryStatistics(stats);
                    updateProgress(1.0);
                    statusLabel.setText("Summary statistics generated");
                    hideProgressAfterDelay();
                    return;
                default:
                    AlertUtil.showWarning("Not Implemented", "This report type is coming soon.");
                    hideProgressAfterDelay();
                    return;
            }

            updateProgress(0.8);

            if (data != null && !data.isEmpty()) {
                reportData.setAll(data);
                updateProgress(1.0);

                int rowCount = data.size();
                recordCountLabel.setText("Total Records: " + rowCount);
                reportSummaryArea.setText(generateSummaryText(currentReportType, startDate, endDate, rowCount));

                statusLabel.setText("Report generated successfully - " + rowCount + " records");
                exportButton.setDisable(false);
                printButton.setDisable(false);
            } else {
                reportData.clear();
                recordCountLabel.setText("Total Records: 0");
                reportSummaryArea.setText("No data found for the selected criteria.");
                statusLabel.setText("No data found for the selected report");
                exportButton.setDisable(true);
                printButton.setDisable(true);
            }

        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error: " + e.getMessage());
            AlertUtil.showError("Generation Failed", "Failed to generate report: " + e.getMessage());
        } finally {
            hideProgressAfterDelay();
        }
    }

    private void displayVehicleReport(List<Map<String, Object>> data) {
        setupTableColumns(new String[]{"registration_number", "make", "model", "year", "owner_name", "status_name", "service_count", "total_service_cost"},
                new String[]{"Registration", "Make", "Model", "Year", "Owner", "Status", "Services", "Total Cost (M)"});
        centerTableColumns();
    }

    private void displayViolationReport(List<Map<String, Object>> data) {
        setupTableColumns(new String[]{"registration_number", "make", "model", "violation_type", "violation_date", "fine_amount", "payment_status", "officer_name"},
                new String[]{"Registration", "Make", "Model", "Violation", "Date", "Fine (M)", "Status", "Officer"});
        centerTableColumns();
    }

    private void displayFinancialReport(List<Map<String, Object>> data) {
        setupTableColumns(new String[]{"type", "date", "amount", "reference"},
                new String[]{"Transaction Type", "Date", "Amount (M)", "Reference"});
        centerTableColumns();

        double total = 0;
        for (Map<String, Object> row : data) {
            Object amount = row.get("amount");
            if (amount instanceof Number) {
                total += ((Number) amount).doubleValue();
            }
        }
        reportSummaryArea.appendText("\n\nTotal Amount: M " + String.format("%,.2f", total));
    }

    private void displayStolenVehicleReport(List<Map<String, Object>> data) {
        setupTableColumns(new String[]{"registration_number", "make", "model", "reported_date", "case_number", "assigned_officer", "status"},
                new String[]{"Registration", "Make", "Model", "Reported Date", "Case Number", "Officer", "Status"});
        centerTableColumns();
    }

    private void displayExpiredDocumentsReport(List<Map<String, Object>> data) {
        setupTableColumns(new String[]{"registration_number", "document_type", "expiry_date", "days_remaining", "expiry_status"},
                new String[]{"Registration", "Document Type", "Expiry Date", "Days Remaining", "Status"});
        centerTableColumns();
    }

    private void displayWorkshopPerformanceReport(List<Map<String, Object>> data) {
        setupTableColumns(new String[]{"workshop_name", "service_count", "total_revenue", "average_service_cost", "avg_rating"},
                new String[]{"Workshop", "Services", "Total Revenue (M)", "Avg Cost (M)", "Rating"});
        centerTableColumns();
    }

    @SuppressWarnings("unchecked")
    private void setupTableColumns(String[] columnKeys, String[] columnHeaders) {
        reportTable.getColumns().clear();

        for (int i = 0; i < columnKeys.length; i++) {
            final int index = i;
            TableColumn<Map<String, Object>, String> column = new TableColumn<>(columnHeaders[i]);
            column.setCellValueFactory(cellData -> {
                Object value = cellData.getValue().get(columnKeys[index]);
                String displayValue = formatCellValue(value, columnKeys[index]);
                return new javafx.beans.property.SimpleStringProperty(displayValue);
            });
            column.setPrefWidth(120);
            reportTable.getColumns().add(column);
        }
    }

    private void centerTableColumns() {
        for (TableColumn<?, ?> column : reportTable.getColumns()) {
            column.setStyle("-fx-alignment: CENTER;");
        }
    }

    private String formatCellValue(Object value, String key) {
        if (value == null) return "";
        if (value instanceof Number) {
            if (key.contains("amount") || key.contains("cost") || key.contains("fine") || key.contains("revenue")) {
                return String.format("M %,d", ((Number) value).intValue());
            }
            return value.toString();
        }
        return value.toString();
    }

    private void displaySummaryStatistics(Map<String, Object> stats) {
        reportTable.getColumns().clear();
        reportData.clear();

        StringBuilder sb = new StringBuilder();
        sb.append("╔════════════════════════════════════════════════════════════════╗\n");
        sb.append("║                    SYSTEM SUMMARY STATISTICS                   ║\n");
        sb.append("╚════════════════════════════════════════════════════════════════╝\n\n");
        sb.append("  📊 Total Vehicles:          ").append(stats.getOrDefault("total_vehicles", 0)).append("\n");
        sb.append("  👥 Total Customers:         ").append(stats.getOrDefault("total_customers", 0)).append("\n");
        sb.append("  🚗 Active Stolen Vehicles:  ").append(stats.getOrDefault("stolen_count", 0)).append("\n");
        sb.append("  📄 Active Insurance:        ").append(stats.getOrDefault("active_insurance", 0)).append("\n");
        sb.append("  💰 Unpaid Fines:            M ").append(String.format("%,.2f", stats.getOrDefault("unpaid_fines", 0.0))).append("\n");
        sb.append("  ❓ Pending Queries:         ").append(stats.getOrDefault("pending_queries", 0)).append("\n");
        sb.append("  🔧 Pending Workshops:       ").append(stats.getOrDefault("pending_workshops", 0)).append("\n");
        sb.append("  📋 Pending Claims:          ").append(stats.getOrDefault("pending_claims", 0)).append("\n");

        reportSummaryArea.setText(sb.toString());
        recordCountLabel.setText("Summary Statistics");
        exportButton.setDisable(true);
        printButton.setDisable(false);
        statusLabel.setText("Summary statistics generated");
    }

    private String generateSummaryText(String reportType, LocalDate startDate, LocalDate endDate, int rowCount) {
        StringBuilder sb = new StringBuilder();
        sb.append("Report generated successfully.\n");
        sb.append("━".repeat(40)).append("\n");
        sb.append("Report Type:    ").append(reportType).append("\n");
        sb.append("Date Range:     ").append(startDate.format(formatter)).append(" to ").append(endDate.format(formatter)).append("\n");
        sb.append("Generated:      ").append(LocalDate.now().format(formatter)).append("\n");
        sb.append("Total Records:  ").append(rowCount).append("\n");
        sb.append("━".repeat(40));
        return sb.toString();
    }

    private void handleExport() {
        if (reportData.isEmpty()) {
            AlertUtil.showWarning("No Data", "Please generate a report first.");
            return;
        }

        showOperationProgress(true);
        statusLabel.setText("Exporting report...");
        updateProgress(0.3);

        try {
            String fileName = currentReportType.toLowerCase().replace(" ", "_");
            updateProgress(0.6);
            ExportUtil.exportToCSV(reportData, fileName,
                    reportTable.getColumns().stream().map(c -> c.getText()).toArray(String[]::new),
                    null);
            updateProgress(1.0);
            statusLabel.setText("Report exported successfully");
            AlertUtil.showSuccess("Export completed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Export failed: " + e.getMessage());
            AlertUtil.showError("Export Failed", "Failed to export report.");
        } finally {
            hideProgressAfterDelay();
        }
    }

    private void handlePrint() {
        String content = reportSummaryArea.getText();
        if (content == null || content.isEmpty()) {
            AlertUtil.showWarning("No Data", "Please generate a report first.");
            return;
        }

        utils.PrintUtil.printNode(reportSummaryArea);
        statusLabel.setText("Print job sent");
    }

    private void clearForm() {
        reportTypeComboBox.setValue("Vehicle Report");
        startDatePicker.setValue(LocalDate.now().minusMonths(1));
        endDatePicker.setValue(LocalDate.now());
        reportData.clear();
        reportTable.getColumns().clear();
        reportSummaryArea.clear();
        recordCountLabel.setText("");
        statusLabel.setText("Form cleared");
        exportButton.setDisable(true);
        printButton.setDisable(true);
        AlertUtil.showSuccess("Form cleared successfully.");
    }

    private void handleBack() {
        String role = utils.SessionManager.getInstance().getUserRole();
        if ("ADMIN".equals(role)) {
            SceneManager.getInstance().switchToAdminView();
        } else if ("POLICE".equals(role)) {
            SceneManager.getInstance().switchToPoliceView();
        } else if ("WORKSHOP".equals(role)) {
            SceneManager.getInstance().switchToWorkshopProfileView();
        } else if ("INSURANCE".equals(role)) {
            SceneManager.getInstance().switchToInsurancePolicyView();
        } else {
            SceneManager.getInstance().switchToDashboard();
        }
    }

    private void applyVisualEffects() {
        // DropShadow effect on all buttons using JavaFX Color
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(5.0);
        dropShadow.setOffsetX(2.0);
        dropShadow.setOffsetY(2.0);
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.3));

        generateButton.setEffect(dropShadow);
        exportButton.setEffect(dropShadow);
        printButton.setEffect(dropShadow);
        backButton.setEffect(dropShadow);

        if (clearButton != null) clearButton.setEffect(dropShadow);
        if (fadeButton != null) fadeButton.setEffect(dropShadow);

        // Drop shadow for table using JavaFX Color
        DropShadow tableShadow = new DropShadow();
        tableShadow.setRadius(3.0);
        tableShadow.setOffsetX(2.0);
        tableShadow.setOffsetY(2.0);
        tableShadow.setColor(Color.rgb(0, 0, 0, 0.2));
        reportTable.setEffect(tableShadow);
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