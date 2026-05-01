package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import utils.AlertUtil;
import utils.SceneManager;
import utils.SessionManager;
import dao.ServiceRecordDAO;
import models.ServiceRecord;
import java.util.Map;
import java.util.HashMap;

public class ServiceAnalyticsController {

    @FXML private Label totalServicesLabel;
    @FXML private Label mostCommonServiceLabel;
    @FXML private Label averageCostLabel;
    @FXML private Label busiestMonthLabel;

    @FXML private TableView<ServiceTypeStat> serviceTypeTable;
    @FXML private TableColumn<ServiceTypeStat, String> typeColumn;
    @FXML private TableColumn<ServiceTypeStat, Integer> countColumn;
    @FXML private TableColumn<ServiceTypeStat, Double> avgCostColumn;

    @FXML private PieChart serviceTypeChart;
    @FXML private BarChart<String, Number> monthlyTrendChart;
    @FXML private Button refreshButton;
    @FXML private Button backButton;

    private ServiceRecordDAO serviceDAO;
    private int workshopId;

    public static class ServiceTypeStat {
        private String serviceType;
        private int count;
        private double avgCost;

        public ServiceTypeStat(String serviceType, int count, double avgCost) {
            this.serviceType = serviceType;
            this.count = count;
            this.avgCost = avgCost;
        }

        public String getServiceType() { return serviceType; }
        public int getCount() { return count; }
        public double getAvgCost() { return avgCost; }
    }

    @FXML
    public void initialize() {
        serviceDAO = new ServiceRecordDAO();
        workshopId = SessionManager.getInstance().getWorkshopId();

        setupTableColumns();
        setupButtonHandlers();
        loadAnalytics();
    }

    private void setupTableColumns() {
        typeColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getServiceType()));
        countColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getCount()).asObject());
        avgCostColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getAvgCost()).asObject());
    }

    private void setupButtonHandlers() {
        refreshButton.setOnAction(event -> loadAnalytics());
        backButton.setOnAction(event -> SceneManager.getInstance().switchToWorkshopProfileView());
    }

    private void loadAnalytics() {
        try {
            java.util.List<ServiceRecord> services = serviceDAO.findByWorkshopId(workshopId);

            totalServicesLabel.setText(String.valueOf(services.size()));

            Map<String, Integer> typeCount = new HashMap<>();
            Map<String, Double> typeTotalCost = new HashMap<>();
            Map<String, Integer> monthlyCount = new HashMap<>();

            for (ServiceRecord service : services) {
                String type = service.getServiceType();
                typeCount.put(type, typeCount.getOrDefault(type, 0) + 1);
                typeTotalCost.put(type, typeTotalCost.getOrDefault(type, 0.0) + service.getCost());

                String month = service.getServiceDate().getMonth().toString();
                monthlyCount.put(month, monthlyCount.getOrDefault(month, 0) + 1);
            }

            String mostCommon = "";
            int maxCount = 0;
            for (Map.Entry<String, Integer> entry : typeCount.entrySet()) {
                if (entry.getValue() > maxCount) {
                    maxCount = entry.getValue();
                    mostCommon = entry.getKey();
                }
            }
            mostCommonServiceLabel.setText(mostCommon.isEmpty() ? "N/A" : mostCommon);

            double totalCost = 0;
            for (ServiceRecord service : services) {
                totalCost += service.getCost();
            }
            averageCostLabel.setText(services.isEmpty() ? "0" :
                    utils.CurrencyUtil.format(totalCost / services.size()));

            String busiestMonth = "";
            int busiestCount = 0;
            for (Map.Entry<String, Integer> entry : monthlyCount.entrySet()) {
                if (entry.getValue() > busiestCount) {
                    busiestCount = entry.getValue();
                    busiestMonth = entry.getKey();
                }
            }
            busiestMonthLabel.setText(busiestMonth.isEmpty() ? "N/A" : busiestMonth);

            serviceTypeTable.getItems().clear();
            for (Map.Entry<String, Integer> entry : typeCount.entrySet()) {
                double avgCost = typeTotalCost.get(entry.getKey()) / entry.getValue();
                serviceTypeTable.getItems().add(new ServiceTypeStat(entry.getKey(), entry.getValue(), avgCost));
            }

            serviceTypeChart.getData().clear();
            for (Map.Entry<String, Integer> entry : typeCount.entrySet()) {
                PieChart.Data slice = new PieChart.Data(entry.getKey(), entry.getValue());
                serviceTypeChart.getData().add(slice);
            }

            monthlyTrendChart.getData().clear();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Services per Month");

            String[] months = {"JANUARY", "FEBRUARY", "MARCH", "APRIL", "MAY", "JUNE",
                    "JULY", "AUGUST", "SEPTEMBER", "OCTOBER", "NOVEMBER", "DECEMBER"};
            for (String month : months) {
                series.getData().add(new XYChart.Data<>(month.substring(0, 3),
                        monthlyCount.getOrDefault(month, 0)));
            }
            monthlyTrendChart.getData().add(series);

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Load Failed", "Failed to load analytics data.");
        }
    }
}