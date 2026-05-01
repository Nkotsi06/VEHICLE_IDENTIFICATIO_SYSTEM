package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import utils.AlertUtil;
import utils.SceneManager;
import utils.SessionManager;
import dao.ServiceRecordDAO;
import models.ServiceRecord;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class WorkshopRevenueController {

    @FXML private Label totalRevenueLabel;
    @FXML private Label averageServiceLabel;
    @FXML private Label serviceCountLabel;
    @FXML private Label thisMonthRevenueLabel;
    @FXML private Label lastMonthRevenueLabel;

    @FXML private TableView<ServiceRecord> servicesTable;
    @FXML private TableColumn<ServiceRecord, String> dateColumn;
    @FXML private TableColumn<ServiceRecord, String> vehicleColumn;
    @FXML private TableColumn<ServiceRecord, String> serviceTypeColumn;
    @FXML private TableColumn<ServiceRecord, Double> costColumn;

    @FXML private BarChart<String, Number> revenueChart;
    @FXML private Button refreshButton;
    @FXML private Button backButton;

    private ServiceRecordDAO serviceDAO;
    private int workshopId;

    @FXML
    public void initialize() {
        serviceDAO = new ServiceRecordDAO();
        workshopId = SessionManager.getInstance().getWorkshopId();

        setupTableColumns();
        setupButtonHandlers();
        loadRevenueData();
        loadChart();
    }

    private void setupTableColumns() {
        dateColumn.setCellValueFactory(cellData -> cellData.getValue().serviceDateProperty().asString());
        vehicleColumn.setCellValueFactory(cellData -> cellData.getValue().registrationNumberProperty());
        serviceTypeColumn.setCellValueFactory(cellData -> cellData.getValue().serviceTypeProperty());
        costColumn.setCellValueFactory(cellData -> cellData.getValue().costProperty().asObject());
    }

    private void setupButtonHandlers() {
        refreshButton.setOnAction(event -> {
            loadRevenueData();
            loadChart();
        });
        backButton.setOnAction(event -> SceneManager.getInstance().switchToWorkshopProfileView());
    }

    private void loadRevenueData() {
        try {
            java.util.List<ServiceRecord> services = serviceDAO.findByWorkshopId(workshopId);
            servicesTable.getItems().setAll(services);

            double totalRevenue = 0;
            double thisMonthRevenue = 0;
            double lastMonthRevenue = 0;

            LocalDate now = LocalDate.now();
            LocalDate thisMonthStart = now.withDayOfMonth(1);
            LocalDate lastMonthStart = now.minusMonths(1).withDayOfMonth(1);
            LocalDate lastMonthEnd = now.withDayOfMonth(1).minusDays(1);

            for (ServiceRecord service : services) {
                totalRevenue += service.getCost();

                if (service.getServiceDate().isAfter(thisMonthStart.minusDays(1))) {
                    thisMonthRevenue += service.getCost();
                } else if (service.getServiceDate().isAfter(lastMonthStart.minusDays(1)) &&
                        service.getServiceDate().isBefore(thisMonthStart)) {
                    lastMonthRevenue += service.getCost();
                }
            }

            totalRevenueLabel.setText(utils.CurrencyUtil.format(totalRevenue));
            averageServiceLabel.setText(services.isEmpty() ? "0" :
                    utils.CurrencyUtil.format(totalRevenue / services.size()));
            serviceCountLabel.setText(String.valueOf(services.size()));
            thisMonthRevenueLabel.setText(utils.CurrencyUtil.format(thisMonthRevenue));
            lastMonthRevenueLabel.setText(utils.CurrencyUtil.format(lastMonthRevenue));

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Load Failed", "Failed to load revenue data.");
        }
    }

    private void loadChart() {
        revenueChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Monthly Revenue");

        try {
            java.util.List<ServiceRecord> services = serviceDAO.findByWorkshopId(workshopId);
            java.util.Map<String, Double> monthlyRevenue = new java.util.HashMap<>();

            for (ServiceRecord service : services) {
                String month = service.getServiceDate().format(DateTimeFormatter.ofPattern("MMM yyyy"));
                monthlyRevenue.put(month, monthlyRevenue.getOrDefault(month, 0.0) + service.getCost());
            }

            java.util.List<String> sortedMonths = new java.util.ArrayList<>(monthlyRevenue.keySet());
            sortedMonths.sort((a, b) -> {
                try {
                    return java.time.YearMonth.parse(a, DateTimeFormatter.ofPattern("MMM yyyy"))
                            .compareTo(java.time.YearMonth.parse(b, DateTimeFormatter.ofPattern("MMM yyyy")));
                } catch (Exception e) {
                    return a.compareTo(b);
                }
            });

            for (String month : sortedMonths) {
                series.getData().add(new XYChart.Data<>(month, monthlyRevenue.get(month)));
            }

            revenueChart.getData().add(series);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}