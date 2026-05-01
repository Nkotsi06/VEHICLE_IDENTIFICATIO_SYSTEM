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
import dao.ServiceRecordDAO;
import dao.MechanicDAO;
import dao.PartInventoryDAO;
import models.ServiceRecord;
import models.Mechanic;
import models.PartInventory;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WorkshopReportController {

    private static final Logger LOGGER = Logger.getLogger(WorkshopReportController.class.getName());

    @FXML private ComboBox<String> reportTypeComboBox;
    @FXML private ComboBox<Mechanic> mechanicComboBox;
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

    private ServiceRecordDAO serviceDAO;
    private MechanicDAO mechanicDAO;
    private PartInventoryDAO inventoryDAO;
    private ObservableList<Map<String, Object>> reportData;
    private String currentReportType;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private int workshopId;

    @FXML
    public void initialize() {
        serviceDAO = new ServiceRecordDAO();
        mechanicDAO = new MechanicDAO();
        inventoryDAO = new PartInventoryDAO();

        workshopId = SessionManager.getInstance().getWorkshopId();
        reportData = FXCollections.observableArrayList();

        setupReportTypes();
        setupDatePickers();
        loadMechanics();
        setupButtonHandlers();
        applyVisualEffects();

        statusLabel.setText("Ready");
        reportTable.setItems(reportData);
    }

    private void setupReportTypes() {
        reportTypeComboBox.getItems().addAll(
                "Services Report",
                "Revenue Report",
                "Monthly Summary Report",
                "Mechanic Performance Report",
                "Inventory Report"
        );
        reportTypeComboBox.setValue("Services Report");
    }

    private void setupDatePickers() {
        startDatePicker.setValue(LocalDate.now().minusMonths(1));
        endDatePicker.setValue(LocalDate.now());
    }

    private void loadMechanics() {
        try {
            List<Mechanic> mechanics = mechanicDAO.findByWorkshopId(workshopId);
            mechanicComboBox.getItems().add(null);
            mechanicComboBox.getItems().addAll(mechanics);
            mechanicComboBox.setPromptText("All Mechanics");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to load mechanics", e);
            statusLabel.setText("Error loading mechanics");
        }
    }

    private void setupButtonHandlers() {
        generateButton.setOnAction(e -> handleGenerate());
        exportButton.setOnAction(e -> handleExport());
        printButton.setOnAction(e -> handlePrint());
        backButton.setOnAction(e -> handleBack());
        clearButton.setOnAction(e -> clearForm());
        if (fadeButton != null) fadeButton.setOnAction(e -> showFadeAnimation());
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
        Integer mechanicId = (mechanicComboBox.getValue() != null) ? mechanicComboBox.getValue().getId() : null;

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

            switch (currentReportType) {
                case "Services Report":
                    data = generateServicesReport(startDate, endDate, mechanicId);
                    displayServicesReport();
                    break;
                case "Revenue Report":
                    data = generateRevenueReport(startDate, endDate);
                    displayRevenueReport();
                    break;
                case "Monthly Summary Report":
                    data = generateMonthlySummary();
                    displayMonthlySummary();
                    break;
                case "Mechanic Performance Report":
                    data = generateMechanicPerformanceReport(startDate, endDate);
                    displayMechanicPerformanceReport();
                    break;
                case "Inventory Report":
                    data = generateInventoryReport();
                    displayInventoryReport();
                    break;
                default:
                    AlertUtil.showWarning("Not Implemented", "This report type is coming soon.");
                    hideProgressAfterDelay();
                    return;
            }

            updateProgress(0.8);

            if (data != null && !data.isEmpty()) {
                reportData.setAll(data);
                updateProgress(1.0);

                double total = calculateTotal(data);
                int rowCount = data.size();
                recordCountLabel.setText("Total Records: " + rowCount + " | Total: " + CurrencyUtil.format(total));
                String summary = generateSummaryText(currentReportType, startDate, endDate, rowCount, total);
                reportSummaryArea.setText(summary);

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

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to generate report", e);
            statusLabel.setText("Error: " + e.getMessage());
            AlertUtil.showError("Generation Failed", "Failed to generate report: " + e.getMessage());
        } finally {
            hideProgressAfterDelay();
        }
    }

    private List<Map<String, Object>> generateServicesReport(LocalDate startDate, LocalDate endDate, Integer mechanicId) throws SQLException {
        List<ServiceRecord> services = serviceDAO.findByWorkshopId(workshopId);
        List<Map<String, Object>> result = new java.util.ArrayList<>();

        for (ServiceRecord service : services) {
            if (service.getServiceDate() != null &&
                    !service.getServiceDate().isBefore(startDate) &&
                    !service.getServiceDate().isAfter(endDate)) {

                if (mechanicId != null && service.getMechanicId() != mechanicId) {
                    continue;
                }

                Map<String, Object> row = new HashMap<>();
                row.put("date", service.getServiceDate());
                row.put("vehicle", service.getRegistrationNumber());
                row.put("service_type", service.getServiceType());
                row.put("cost", service.getCost());
                row.put("mechanic", service.getMechanicName());
                row.put("odometer", service.getOdometerReading());
                result.add(row);
            }
        }
        return result;
    }

    private List<Map<String, Object>> generateRevenueReport(LocalDate startDate, LocalDate endDate) throws SQLException {
        List<ServiceRecord> services = serviceDAO.findByWorkshopId(workshopId);
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        Map<String, Double> monthlyRevenue = new HashMap<>();

        for (ServiceRecord service : services) {
            if (service.getServiceDate() != null &&
                    !service.getServiceDate().isBefore(startDate) &&
                    !service.getServiceDate().isAfter(endDate)) {

                String month = service.getServiceDate().format(DateTimeFormatter.ofPattern("MMM yyyy"));
                monthlyRevenue.merge(month, service.getCost(), Double::sum);
            }
        }

        for (Map.Entry<String, Double> entry : monthlyRevenue.entrySet()) {
            Map<String, Object> row = new HashMap<>();
            row.put("month", entry.getKey());
            row.put("revenue", entry.getValue());
            result.add(row);
        }

        result.sort((a, b) -> ((String) a.get("month")).compareTo((String) b.get("month")));
        return result;
    }

    private List<Map<String, Object>> generateMonthlySummary() throws SQLException {
        List<ServiceRecord> services = serviceDAO.findByWorkshopId(workshopId);
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        Map<String, Map<String, Object>> monthlyData = new HashMap<>();

        for (ServiceRecord service : services) {
            String month = service.getServiceDate().format(DateTimeFormatter.ofPattern("MMM yyyy"));

            monthlyData.computeIfAbsent(month, k -> {
                Map<String, Object> row = new HashMap<>();
                row.put("month", month);
                row.put("service_count", 0);
                row.put("total_revenue", 0.0);
                row.put("avg_cost", 0.0);
                return row;
            });

            Map<String, Object> row = monthlyData.get(month);
            int count = (int) row.get("service_count");
            double revenue = (double) row.get("total_revenue");
            row.put("service_count", count + 1);
            row.put("total_revenue", revenue + service.getCost());
        }

        for (Map<String, Object> row : monthlyData.values()) {
            int count = (int) row.get("service_count");
            double revenue = (double) row.get("total_revenue");
            row.put("avg_cost", count > 0 ? revenue / count : 0);
            result.add(row);
        }

        result.sort((a, b) -> ((String) a.get("month")).compareTo((String) b.get("month")));
        return result;
    }

    private List<Map<String, Object>> generateMechanicPerformanceReport(LocalDate startDate, LocalDate endDate) throws SQLException {
        List<ServiceRecord> services = serviceDAO.findByWorkshopId(workshopId);
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        Map<Integer, Map<String, Object>> mechanicData = new HashMap<>();

        for (ServiceRecord service : services) {
            if (service.getServiceDate() != null &&
                    !service.getServiceDate().isBefore(startDate) &&
                    !service.getServiceDate().isAfter(endDate) &&
                    service.getMechanicId() > 0) {

                int mechId = service.getMechanicId();

                mechanicData.computeIfAbsent(mechId, k -> {
                    Map<String, Object> row = new HashMap<>();
                    row.put("mechanic_name", service.getMechanicName());
                    row.put("service_count", 0);
                    row.put("total_revenue", 0.0);
                    row.put("avg_cost", 0.0);
                    return row;
                });

                Map<String, Object> row = mechanicData.get(mechId);
                int count = (int) row.get("service_count");
                double revenue = (double) row.get("total_revenue");
                row.put("service_count", count + 1);
                row.put("total_revenue", revenue + service.getCost());
            }
        }

        for (Map<String, Object> row : mechanicData.values()) {
            int count = (int) row.get("service_count");
            double revenue = (double) row.get("total_revenue");
            row.put("avg_cost", count > 0 ? revenue / count : 0);
            result.add(row);
        }

        result.sort((a, b) -> Integer.compare((int) b.get("service_count"), (int) a.get("service_count")));
        return result;
    }

    private List<Map<String, Object>> generateInventoryReport() throws SQLException {
        List<PartInventory> parts = inventoryDAO.findByWorkshopId(workshopId);
        List<Map<String, Object>> result = new java.util.ArrayList<>();

        for (PartInventory part : parts) {
            Map<String, Object> row = new HashMap<>();
            row.put("part_name", part.getPartName());
            row.put("part_number", part.getPartNumber());
            row.put("quantity", part.getQuantity());
            row.put("reorder_level", part.getReorderLevel());
            row.put("unit_price", part.getUnitPrice());
            row.put("stock_value", part.getQuantity() * part.getUnitPrice());
            row.put("stock_status", part.getStockStatus());
            result.add(row);
        }
        return result;
    }

    private void displayServicesReport() {
        setupTableColumns(new String[]{"date", "vehicle", "service_type", "cost", "mechanic", "odometer"},
                new String[]{"Date", "Vehicle", "Service Type", "Cost", "Mechanic", "Odometer"});
    }

    private void displayRevenueReport() {
        setupTableColumns(new String[]{"month", "revenue"},
                new String[]{"Month", "Revenue"});
    }

    private void displayMonthlySummary() {
        setupTableColumns(new String[]{"month", "service_count", "total_revenue", "avg_cost"},
                new String[]{"Month", "Services", "Total Revenue", "Average Cost"});
    }

    private void displayMechanicPerformanceReport() {
        setupTableColumns(new String[]{"mechanic_name", "service_count", "total_revenue", "avg_cost"},
                new String[]{"Mechanic", "Services", "Total Revenue", "Average Cost"});
    }

    private void displayInventoryReport() {
        setupTableColumns(new String[]{"part_name", "part_number", "quantity", "reorder_level", "unit_price", "stock_value", "stock_status"},
                new String[]{"Part Name", "Part #", "Qty", "Reorder", "Unit Price", "Stock Value", "Status"});
    }

    private void setupTableColumns(String[] columnKeys, String[] columnHeaders) {
        reportTable.getColumns().clear();

        for (int i = 0; i < columnKeys.length; i++) {
            final String key = columnKeys[i];
            TableColumn<Map<String, Object>, String> column = new TableColumn<>(columnHeaders[i]);
            column.setCellValueFactory(cellData -> {
                Object value = cellData.getValue().get(key);
                String displayValue = formatValue(value, key);
                return new javafx.beans.property.SimpleStringProperty(displayValue);
            });
            column.setPrefWidth(120);
            column.setStyle("-fx-alignment: CENTER;");
            reportTable.getColumns().add(column);
        }
    }

    private String formatValue(Object value, String key) {
        if (value == null) return "";
        if (value instanceof Number num) {
            if (key.contains("revenue") || key.contains("cost") || key.contains("price") || key.contains("value")) {
                return CurrencyUtil.format(num.doubleValue());
            }
            return value.toString();
        }
        if (value instanceof LocalDate date) {
            return date.format(formatter);
        }
        return value.toString();
    }

    private double calculateTotal(List<Map<String, Object>> data) {
        return data.stream()
                .mapToDouble(row -> {
                    double total = 0.0;
                    Object revenue = row.get("revenue");
                    if (revenue instanceof Number) total += ((Number) revenue).doubleValue();
                    Object cost = row.get("cost");
                    if (cost instanceof Number) total += ((Number) cost).doubleValue();
                    Object stockValue = row.get("stock_value");
                    if (stockValue instanceof Number) total += ((Number) stockValue).doubleValue();
                    return total;
                })
                .sum();
    }

    private String generateSummaryText(String reportType, LocalDate startDate, LocalDate endDate, int rowCount, double total) {
        return new StringBuilder()
                .append("Report generated successfully.\n")
                .append("━".repeat(40)).append("\n")
                .append("Report Type:    ").append(reportType).append("\n")
                .append("Date Range:     ").append(startDate.format(formatter)).append(" to ").append(endDate.format(formatter)).append("\n")
                .append("Generated:      ").append(LocalDate.now().format(formatter)).append("\n")
                .append("Total Records:  ").append(rowCount).append("\n")
                .append("Total Amount:   ").append(CurrencyUtil.format(total)).append("\n")
                .append("━".repeat(40))
                .toString();
    }

    private void handleExport() {
        if (reportData.isEmpty()) {
            AlertUtil.showWarning("No Data", "Please generate a report first.");
            return;
        }

        showProgress(true);
        statusLabel.setText("Exporting report...");
        updateProgress(0.3);

        try {
            String fileName = "workshop_" + currentReportType.toLowerCase().replace(" ", "_") + "_" + LocalDate.now();
            updateProgress(0.6);
            String[] headers = reportTable.getColumns().stream().map(TableColumn::getText).toArray(String[]::new);
            ExportUtil.exportToCSV(reportData, fileName, headers, null);
            updateProgress(1.0);
            statusLabel.setText("Report exported successfully");
            AlertUtil.showSuccess("Export completed successfully.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Export failed", e);
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
        reportTypeComboBox.setValue("Services Report");
        startDatePicker.setValue(LocalDate.now().minusMonths(1));
        endDatePicker.setValue(LocalDate.now());
        mechanicComboBox.setValue(null);
        reportData.clear();
        reportTable.getColumns().clear();
        reportSummaryArea.clear();
        recordCountLabel.setText("");
        statusLabel.setText("Form cleared");
        exportButton.setDisable(true);
        printButton.setDisable(true);
    }

    private void handleBack() {
        SceneManager.getInstance().switchToWorkshopView();
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

    private void hideProgressAfterDelay() {
        PauseTransition delay = new PauseTransition(Duration.seconds(1.5));
        delay.setOnFinished(e -> {
            if (operationProgress != null) {
                operationProgress.setVisible(false);
                operationProgress.setProgress(0);
            }
            if (loadProgress != null) loadProgress.setVisible(false);
        });
        delay.play();
    }
}