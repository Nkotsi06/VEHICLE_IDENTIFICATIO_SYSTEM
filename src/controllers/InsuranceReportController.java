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
import utils.CurrencyUtil;
import dao.ReportGeneratorDAO;
import dao.InsurancePolicyDAO;
import dao.InsuranceClaimDAO;
import dao.InsuranceProviderDAO;
import models.InsurancePolicy;
import models.InsuranceClaim;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

public class InsuranceReportController {

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
    private InsurancePolicyDAO policyDAO;
    private InsuranceClaimDAO claimDAO;
    private InsuranceProviderDAO providerDAO;
    private ObservableList<Map<String, Object>> reportData;
    private String currentReportType;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private int providerId;
    private int currentPage = 0;
    private int pageSize = 20;
    private List<Map<String, Object>> fullData;

    @FXML
    public void initialize() {
        reportDAO = new ReportGeneratorDAO();
        policyDAO = new InsurancePolicyDAO();
        claimDAO = new InsuranceClaimDAO();
        providerDAO = new InsuranceProviderDAO();
        reportData = FXCollections.observableArrayList();

        // Get the logged-in provider's ID
        try {
            int userId = SessionManager.getInstance().getUserId();
            var provider = providerDAO.findByUserId(userId);
            if (provider != null) {
                providerId = provider.getId();
            }
        } catch (Exception e) {
            e.printStackTrace();
            providerId = -1;
        }

        setupReportTypes();
        setupDatePickers();
        setupButtonHandlers();
        setupPagination();
        applyVisualEffects();

        statusLabel.setText("Ready");
        reportTable.setItems(reportData);
    }

    private void setupReportTypes() {
        reportTypeComboBox.getItems().addAll(
                "Active Policies Report",
                "Expiring Policies Report",
                "Claims Report",
                "Premium Collection Report",
                "Expired Documents Report",
                "Audit Logs Report"
        );
        reportTypeComboBox.setValue("Active Policies Report");
    }

    private void setupDatePickers() {
        startDatePicker.setValue(LocalDate.now().minusMonths(6));
        endDatePicker.setValue(LocalDate.now());
    }

    private void setupPagination() {
        if (reportPagination != null) {
            reportPagination.setPageCount(1);
            reportPagination.setMaxPageIndicatorCount(5);
            reportPagination.currentPageIndexProperty().addListener((obs, old, newPage) -> {
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
            reportData.setAll(fullData.subList(start, end));
        }
    }

    private void setupButtonHandlers() {
        generateButton.setOnAction(event -> handleGenerate());
        exportButton.setOnAction(event -> handleExport());
        printButton.setOnAction(event -> handlePrint());
        backButton.setOnAction(event -> handleBack());
        clearButton.setOnAction(event -> clearForm());
        if (fadeButton != null) fadeButton.setOnAction(event -> showFadeAnimation());
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

        showProgress(true);
        statusLabel.setText("Generating " + currentReportType + "...");
        updateProgress(0.2);

        try {
            updateProgress(0.4);
            List<Map<String, Object>> data = null;
            String[] columnKeys = null;
            String[] columnHeaders = null;

            switch (currentReportType) {
                case "Active Policies Report":
                    if (providerId > 0) {
                        List<InsurancePolicy> policies = policyDAO.findByProviderId(providerId);
                        data = convertPoliciesToMap(policies, true);
                    } else {
                        List<InsurancePolicy> policies = policyDAO.findActivePolicies();
                        data = convertPoliciesToMap(policies, true);
                    }
                    columnKeys = new String[]{"policy_number", "registration_number", "vehicle_make", "vehicle_model", "start_date", "end_date", "premium", "coverage_amount"};
                    columnHeaders = new String[]{"Policy #", "Vehicle", "Make", "Model", "Start Date", "End Date", "Premium", "Coverage"};
                    break;

                case "Expiring Policies Report":
                    List<InsurancePolicy> expiringPolicies = policyDAO.findExpiringPolicies(90);
                    data = convertPoliciesToMap(expiringPolicies, false);
                    columnKeys = new String[]{"policy_number", "registration_number", "vehicle_make", "vehicle_model", "end_date", "days_remaining", "premium"};
                    columnHeaders = new String[]{"Policy #", "Vehicle", "Make", "Model", "Expiry Date", "Days Left", "Premium"};
                    break;

                case "Claims Report":
                    if (providerId > 0) {
                        List<InsuranceClaim> claims = claimDAO.findByProviderId(providerId);
                        data = convertClaimsToMap(claims, startDate, endDate);
                    } else {
                        List<InsuranceClaim> claims = claimDAO.findByDateRange(startDate, endDate);
                        data = convertClaimsToMap(claims, startDate, endDate);
                    }
                    columnKeys = new String[]{"claim_id", "policy_number", "registration_number", "claim_date", "claim_amount", "status", "approved_amount"};
                    columnHeaders = new String[]{"Claim #", "Policy #", "Vehicle", "Claim Date", "Amount", "Status", "Approved"};
                    break;

                case "Premium Collection Report":
                    // Use ReportGeneratorDAO for financial data
                    data = reportDAO.generateFinancialReport(startDate, endDate);
                    columnKeys = new String[]{"transaction_type", "transaction_date", "amount", "reference"};
                    columnHeaders = new String[]{"Transaction Type", "Date", "Amount", "Reference"};
                    break;

                case "Expired Documents Report":
                    data = reportDAO.generateExpiredDocumentsReport();
                    columnKeys = new String[]{"registration_number", "document_type", "expiry_date", "days_remaining", "expiry_status"};
                    columnHeaders = new String[]{"Registration", "Document Type", "Expiry Date", "Days Remaining", "Status"};
                    break;

                case "Audit Logs Report":
                    data = reportDAO.generateAuditLogsReport();
                    columnKeys = new String[]{"username", "action", "timestamp", "ip_address"};
                    columnHeaders = new String[]{"User", "Action", "Timestamp", "IP Address"};
                    break;

                default:
                    AlertUtil.showWarning("Not Implemented", "This report type is coming soon.");
                    hideProgress();
                    return;
            }

            updateProgress(0.8);

            if (data != null && !data.isEmpty()) {
                fullData = data;
                int totalPages = (int) Math.ceil((double) data.size() / pageSize);
                if (reportPagination != null) reportPagination.setPageCount(Math.max(1, totalPages));
                updateTablePage();

                displayReport(columnKeys, columnHeaders);

                double total = calculateTotal(data);
                int rowCount = data.size();
                recordCountLabel.setText("Total Records: " + rowCount + " | Total Amount: " + CurrencyUtil.format(total));
                reportSummaryArea.setText(generateSummaryText(currentReportType, startDate, endDate, rowCount, total));

                statusLabel.setText("Report generated successfully - " + rowCount + " records");
                exportButton.setDisable(false);
                printButton.setDisable(false);
            } else {
                fullData = null;
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

    private List<Map<String, Object>> convertPoliciesToMap(List<InsurancePolicy> policies, boolean includeStartDate) {
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (InsurancePolicy policy : policies) {
            Map<String, Object> row = new HashMap<>();
            row.put("policy_number", policy.getPolicyNumber());
            row.put("registration_number", policy.getRegistrationNumber());
            row.put("vehicle_make", policy.getVehicleMake() != null ? policy.getVehicleMake() : "");
            row.put("vehicle_model", policy.getVehicleModel() != null ? policy.getVehicleModel() : "");

            if (includeStartDate && policy.getStartDate() != null) {
                row.put("start_date", policy.getStartDate().format(formatter));
            }

            if (policy.getEndDate() != null) {
                row.put("end_date", policy.getEndDate().format(formatter));
                long daysRemaining = java.time.temporal.ChronoUnit.DAYS.between(today, policy.getEndDate());
                row.put("days_remaining", Math.max(0, daysRemaining));
            }

            row.put("premium", policy.getPremium());
            row.put("coverage_amount", policy.getCoverageAmount());
            row.put("status", policy.getStatus());

            result.add(row);
        }
        return result;
    }

    private List<Map<String, Object>> convertClaimsToMap(List<InsuranceClaim> claims, LocalDate startDate, LocalDate endDate) {
        List<Map<String, Object>> result = new ArrayList<>();

        for (InsuranceClaim claim : claims) {
            if (claim.getClaimDate() != null &&
                    !claim.getClaimDate().isBefore(startDate) &&
                    !claim.getClaimDate().isAfter(endDate)) {
                Map<String, Object> row = new HashMap<>();
                row.put("claim_id", claim.getId());
                row.put("policy_number", claim.getPolicyNumber());
                row.put("registration_number", claim.getRegistrationNumber());
                row.put("claim_date", claim.getClaimDate().format(formatter));
                row.put("claim_amount", claim.getClaimAmount());
                row.put("status", claim.getStatus());
                row.put("approved_amount", claim.getApprovedAmount() != null ? claim.getApprovedAmount() : 0);
                result.add(row);
            }
        }
        return result;
    }

    private void displayReport(String[] columnKeys, String[] columnHeaders) {
        reportTable.getColumns().clear();

        for (int i = 0; i < columnKeys.length; i++) {
            final String key = columnKeys[i];
            TableColumn<Map<String, Object>, String> column = new TableColumn<>(columnHeaders[i]);
            column.setCellValueFactory(cellData -> {
                Object value = cellData.getValue().get(key);
                String displayValue = formatCellValue(value, key);
                return new javafx.beans.property.SimpleStringProperty(displayValue);
            });
            column.setPrefWidth(120);
            column.setStyle("-fx-alignment: CENTER;");
            reportTable.getColumns().add(column);
        }
    }

    private String formatCellValue(Object value, String key) {
        if (value == null) return "";
        if (value instanceof Number) {
            if (key.contains("premium") || key.contains("amount") || key.contains("coverage")) {
                return CurrencyUtil.format(((Number) value).doubleValue());
            }
            if (key.contains("days_remaining")) {
                long days = ((Number) value).longValue();
                return days + " days";
            }
            return value.toString();
        }
        return value.toString();
    }

    private double calculateTotal(List<Map<String, Object>> data) {
        double total = 0;
        for (Map<String, Object> row : data) {
            Object premium = row.get("premium");
            if (premium instanceof Number) {
                total += ((Number) premium).doubleValue();
            }
            Object amount = row.get("claim_amount");
            if (amount instanceof Number) {
                total += ((Number) amount).doubleValue();
            }
        }
        return total;
    }

    private String generateSummaryText(String reportType, LocalDate startDate, LocalDate endDate, int rowCount, double total) {
        StringBuilder sb = new StringBuilder();
        sb.append("Report generated successfully.\n");
        sb.append("━".repeat(40)).append("\n");
        sb.append("Report Type:    ").append(reportType).append("\n");
        sb.append("Date Range:     ").append(startDate.format(formatter)).append(" to ").append(endDate.format(formatter)).append("\n");
        sb.append("Generated:      ").append(LocalDate.now().format(formatter)).append("\n");
        sb.append("Total Records:  ").append(rowCount).append("\n");
        sb.append("Total Amount:   ").append(CurrencyUtil.format(total)).append("\n");
        sb.append("━".repeat(40));
        return sb.toString();
    }

    private void handleExport() {
        if (fullData == null || fullData.isEmpty()) {
            AlertUtil.showWarning("No Data", "Please generate a report first.");
            return;
        }

        showProgress(true);
        statusLabel.setText("Exporting report...");
        updateProgress(0.3);

        try {
            String fileName = "insurance_" + currentReportType.toLowerCase().replace(" ", "_") + "_" + LocalDate.now();
            updateProgress(0.6);
            String[] headers = reportTable.getColumns().stream().map(c -> c.getText()).toArray(String[]::new);
            ExportUtil.exportToCSV(fullData, fileName, headers, null);
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
        reportTypeComboBox.setValue("Active Policies Report");
        startDatePicker.setValue(LocalDate.now().minusMonths(6));
        endDatePicker.setValue(LocalDate.now());
        reportData.clear();
        reportTable.getColumns().clear();
        reportSummaryArea.clear();
        recordCountLabel.setText("");
        statusLabel.setText("Form cleared");
        exportButton.setDisable(true);
        printButton.setDisable(true);
        fullData = null;
        if (reportPagination != null) reportPagination.setPageCount(1);
    }

    private void handleBack() {
        SceneManager.getInstance().switchToInsuranceView();
    }

    private void showProgress(boolean show) {
        if (loadProgress != null) loadProgress.setVisible(show);
        if (operationProgress != null) {
            operationProgress.setVisible(show);
            operationProgress.setProgress(0);
        }
    }

    private void updateProgress(double progress) {
        if (operationProgress != null) operationProgress.setProgress(progress);
    }

    private void hideProgress() {
        if (loadProgress != null) loadProgress.setVisible(false);
        if (operationProgress != null) operationProgress.setVisible(false);
    }

    private void hideProgressAfterDelay() {
        PauseTransition delay = new PauseTransition(Duration.seconds(1.5));
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