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
import utils.ExportUtil;
import dao.ReportGeneratorDAO;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class PoliceReportController {

    @FXML private ComboBox<String> reportTypeComboBox;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Button generateButton;
    @FXML private Button exportButton;
    @FXML private Button printButton;
    @FXML private Button backButton;
    @FXML private Button clearButton;
    @FXML private Button fadeButton;

    @FXML private TableView<Map<String, Object>> reportTable;
    @FXML private TextArea reportSummaryArea;
    @FXML private Label recordCountLabel;
    @FXML private Label statusLabel;
    @FXML private ProgressIndicator loadProgress;
    @FXML private ProgressBar operationProgress;
    @FXML private Pagination reportPagination;

    private ReportGeneratorDAO reportDAO;
    private ObservableList<Map<String, Object>> reportData;
    private String currentReportType;
    private List<Map<String, Object>> fullReportData;
    private int currentPage = 0;
    private int pageSize = 20;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @FXML
    public void initialize() {
        // Check if user is POLICE
        if (!"POLICE".equals(SessionManager.getInstance().getUserRole())) {
            AlertUtil.showError("Access Denied", "Only police officers can access this report section.");
            SceneManager.getInstance().switchToDashboard();
            return;
        }

        reportDAO = new ReportGeneratorDAO();
        reportData = FXCollections.observableArrayList();

        setupReportTypes();
        setupDatePickers();
        setupButtonHandlers();
        setupPagination();
        applyVisualEffects();

        statusLabel.setText("Ready - Police Reports");
        reportTable.setItems(reportData);
    }

    private void setupReportTypes() {
        reportTypeComboBox.getItems().addAll(
                "Stolen Vehicles Report",
                "Violations Report",
                "Warrants Report",
                "BOLO Alerts Report",
                "Geofence Alerts Report",
                "Officer Activity Report",
                "Expired Documents Report"
        );
        reportTypeComboBox.setValue("Stolen Vehicles Report");
    }

    private void setupDatePickers() {
        startDatePicker.setValue(LocalDate.now().minusMonths(1));
        endDatePicker.setValue(LocalDate.now());
    }

    private void setupPagination() {
        if (reportPagination != null) {
            reportPagination.currentPageIndexProperty().addListener((obs, old, newPage) -> {
                currentPage = newPage.intValue();
                updateTablePage();
            });
        }
    }

    private void updateTablePage() {
        if (fullReportData == null || fullReportData.isEmpty()) {
            reportData.clear();
            return;
        }
        int start = currentPage * pageSize;
        int end = Math.min(start + pageSize, fullReportData.size());
        if (start < fullReportData.size()) {
            reportData.setAll(fullReportData.subList(start, end));
        }
    }

    private void setupButtonHandlers() {
        generateButton.setOnAction(event -> handleGenerate());
        exportButton.setOnAction(event -> handleExport());
        printButton.setOnAction(event -> handlePrint());
        backButton.setOnAction(event -> SceneManager.getInstance().switchToPoliceView());

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

        if ("Violations Report".equals(currentReportType)) {
            if (startDate == null || endDate == null) {
                AlertUtil.showWarning("Date Required", "Please select both start and end dates.");
                return;
            }
            if (startDate.isAfter(endDate)) {
                AlertUtil.showWarning("Invalid Date Range", "Start date must be before end date.");
                return;
            }
        }

        showOperationProgress(true);
        statusLabel.setText("Generating " + currentReportType + "...");
        updateProgress(0.2);

        try {
            updateProgress(0.4);
            List<Map<String, Object>> data = null;

            switch (currentReportType) {
                case "Stolen Vehicles Report":
                    data = reportDAO.generateStolenVehicleReport();
                    displayStolenVehicleReport(data);
                    break;
                case "Violations Report":
                    data = reportDAO.generateViolationReport(startDate, endDate);
                    displayViolationReport(data);
                    break;
                case "Warrants Report":
                    data = reportDAO.generateWarrantsReport();
                    displayWarrantsReport(data);
                    break;
                case "BOLO Alerts Report":
                    data = reportDAO.generateBOLOAlertsReport();
                    displayBOLOAlertsReport(data);
                    break;
                case "Geofence Alerts Report":
                    data = reportDAO.generateGeofenceAlertsReport();
                    displayGeofenceAlertsReport(data);
                    break;
                case "Officer Activity Report":
                    data = reportDAO.generateOfficerActivityReport();
                    displayOfficerActivityReport(data);
                    break;
                case "Expired Documents Report":
                    data = reportDAO.generateExpiredDocumentsReport();
                    displayExpiredDocumentsReport(data);
                    break;
                default:
                    AlertUtil.showWarning("Not Implemented", "This report type is coming soon.");
                    hideProgressAfterDelay();
                    return;
            }

            updateProgress(0.8);

            if (data != null && !data.isEmpty()) {
                fullReportData = data;
                int totalPages = (int) Math.ceil((double) data.size() / pageSize);
                if (reportPagination != null) reportPagination.setPageCount(Math.max(1, totalPages));
                updateTablePage();
                updateProgress(1.0);

                int rowCount = data.size();
                recordCountLabel.setText("Total Records: " + rowCount);
                reportSummaryArea.setText(generateSummaryText(currentReportType, startDate, endDate, rowCount));

                statusLabel.setText("Report generated successfully - " + rowCount + " records");
                exportButton.setDisable(false);
                printButton.setDisable(false);
            } else {
                fullReportData = null;
                reportData.clear();
                recordCountLabel.setText("Total Records: 0");
                reportSummaryArea.setText("No data found for the selected criteria.");
                statusLabel.setText("No data found for the selected report");
                exportButton.setDisable(true);
                printButton.setDisable(true);
                if (reportPagination != null) reportPagination.setPageCount(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error: " + e.getMessage());
            AlertUtil.showError("Generation Failed", "Failed to generate report: " + e.getMessage());
        } finally {
            hideProgressAfterDelay();
        }
    }

    private void displayStolenVehicleReport(List<Map<String, Object>> data) {
        setupTableColumns(new String[]{"registration_number", "make", "model", "reported_date", "case_number", "assigned_officer", "status"},
                new String[]{"Registration", "Make", "Model", "Reported Date", "Case Number", "Officer", "Status"});
        centerTableColumns();
    }

    private void displayViolationReport(List<Map<String, Object>> data) {
        setupTableColumns(new String[]{"registration_number", "make", "model", "violation_type", "violation_date", "fine_amount", "payment_status", "officer_name"},
                new String[]{"Registration", "Make", "Model", "Violation", "Date", "Fine (M)", "Status", "Officer"});
        centerTableColumns();
    }

    private void displayWarrantsReport(List<Map<String, Object>> data) {
        setupTableColumns(new String[]{"registration_number", "violation_type", "issue_date", "expiry_date", "judge_name", "status"},
                new String[]{"Registration", "Violation", "Issue Date", "Expiry Date", "Judge", "Status"});
        centerTableColumns();
    }

    private void displayBOLOAlertsReport(List<Map<String, Object>> data) {
        setupTableColumns(new String[]{"registration_number", "message", "priority", "alert_date", "expiry_date", "status"},
                new String[]{"Registration", "BOLO Message", "Priority", "Alert Date", "Expiry Date", "Status"});
        centerTableColumns();
    }

    private void displayGeofenceAlertsReport(List<Map<String, Object>> data) {
        setupTableColumns(new String[]{"zone_name", "registration_number", "alert_type", "alert_timestamp", "is_notified"},
                new String[]{"Zone", "Vehicle", "Alert Type", "Timestamp", "Notified"});
        centerTableColumns();
    }

    private void displayOfficerActivityReport(List<Map<String, Object>> data) {
        setupTableColumns(new String[]{"officer_name", "action", "registration_number", "timestamp"},
                new String[]{"Officer", "Action", "Vehicle", "Timestamp"});
        centerTableColumns();
    }

    private void displayExpiredDocumentsReport(List<Map<String, Object>> data) {
        setupTableColumns(new String[]{"registration_number", "document_type", "expiry_date", "days_remaining", "expiry_status"},
                new String[]{"Registration", "Document Type", "Expiry Date", "Days Remaining", "Status"});
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
            if (key.contains("amount") || key.contains("fine")) {
                return String.format("M %,.2f", ((Number) value).doubleValue());
            }
            return value.toString();
        }
        if (value instanceof LocalDate) {
            return ((LocalDate) value).format(formatter);
        }
        if (value instanceof java.time.LocalDateTime) {
            return ((java.time.LocalDateTime) value).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        return value.toString();
    }

    private String generateSummaryText(String reportType, LocalDate startDate, LocalDate endDate, int rowCount) {
        StringBuilder sb = new StringBuilder();
        sb.append("Police Report Generated Successfully\n");
        sb.append("━".repeat(40)).append("\n");
        sb.append("Report Type:    ").append(reportType).append("\n");
        if (startDate != null && endDate != null && "Violations Report".equals(reportType)) {
            sb.append("Date Range:     ").append(startDate.format(formatter)).append(" to ").append(endDate.format(formatter)).append("\n");
        }
        sb.append("Generated By:   ").append(SessionManager.getInstance().getFullName()).append("\n");
        sb.append("Generated:      ").append(LocalDate.now().format(formatter)).append("\n");
        sb.append("Total Records:  ").append(rowCount).append("\n");
        sb.append("━".repeat(40));
        return sb.toString();
    }

    private void handleExport() {
        if (fullReportData == null || fullReportData.isEmpty()) {
            AlertUtil.showWarning("No Data", "Please generate a report first.");
            return;
        }

        showOperationProgress(true);
        statusLabel.setText("Exporting report...");
        updateProgress(0.3);

        try {
            String fileName = "police_" + currentReportType.toLowerCase().replace(" ", "_");
            updateProgress(0.6);
            ExportUtil.exportToCSV(fullReportData, fileName,
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
        reportTypeComboBox.setValue("Stolen Vehicles Report");
        startDatePicker.setValue(LocalDate.now().minusMonths(1));
        endDatePicker.setValue(LocalDate.now());
        fullReportData = null;
        reportData.clear();
        reportTable.getColumns().clear();
        reportSummaryArea.clear();
        recordCountLabel.setText("");
        statusLabel.setText("Form cleared");
        exportButton.setDisable(true);
        printButton.setDisable(true);
        if (reportPagination != null) reportPagination.setPageCount(1);
        AlertUtil.showSuccess("Form cleared successfully.");
    }

    private void applyVisualEffects() {
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
        if (loadProgress != null) loadProgress.setVisible(show);
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