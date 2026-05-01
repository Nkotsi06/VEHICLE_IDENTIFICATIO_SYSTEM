package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import utils.AlertUtil;
import utils.SceneManager;
import utils.SessionManager;
import dao.ServiceRecordDAO;
import dao.VehicleDAO;
import models.Vehicle;
import models.ServiceRecord;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class ServiceHistoryGraphController {

    @FXML private ComboBox<Vehicle> vehicleComboBox;
    @FXML private Button loadButton;
    @FXML private Button refreshButton;
    @FXML private Button backButton;

    @FXML private Label vehicleInfoLabel;
    @FXML private Label totalServicesLabel;
    @FXML private Label totalCostLabel;
    @FXML private Label averageCostLabel;
    @FXML private Label lastServiceLabel;

    @FXML private BarChart<String, Number> costChart;
    @FXML private LineChart<String, Number> trendChart;

    private ServiceRecordDAO serviceDAO;
    private VehicleDAO vehicleDAO;

    @FXML
    public void initialize() {
        serviceDAO = new ServiceRecordDAO();
        vehicleDAO = new VehicleDAO();

        loadVehicles();
        setupButtonHandlers();
    }

    private void loadVehicles() {
        try {
            int customerId = SessionManager.getInstance().getCustomerId();
            List<Vehicle> vehicles = vehicleDAO.findByOwnerId(customerId);
            vehicleComboBox.getItems().setAll(vehicles);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupButtonHandlers() {
        loadButton.setOnAction(event -> handleLoad());
        refreshButton.setOnAction(event -> handleLoad());
        backButton.setOnAction(event -> SceneManager.getInstance().switchToCustomerProfileView());
    }

    private void handleLoad() {
        Vehicle selectedVehicle = vehicleComboBox.getSelectionModel().getSelectedItem();

        if (selectedVehicle == null) {
            AlertUtil.showWarning("Validation Error", "Please select a vehicle.");
            return;
        }

        try {
            List<ServiceRecord> services = serviceDAO.findByVehicleId(selectedVehicle.getId());

            vehicleInfoLabel.setText(selectedVehicle.getMake() + " " + selectedVehicle.getModel() +
                    " (" + selectedVehicle.getYear() + ")");
            totalServicesLabel.setText(String.valueOf(services.size()));

            double totalCost = 0;
            for (ServiceRecord s : services) {
                totalCost += s.getCost();
            }
            totalCostLabel.setText(utils.CurrencyUtil.format(totalCost));
            averageCostLabel.setText(services.isEmpty() ? "0" :
                    utils.CurrencyUtil.format(totalCost / services.size()));

            if (!services.isEmpty()) {
                lastServiceLabel.setText(services.get(services.size() - 1).getServiceDate().toString());
            } else {
                lastServiceLabel.setText("No services");
            }

            loadCostChart(services);
            loadTrendChart(services);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadCostChart(List<ServiceRecord> services) {
        costChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Service Cost");

        Map<String, Double> monthlyCost = new HashMap<>();
        for (ServiceRecord service : services) {
            String month = service.getServiceDate().getMonth().toString() + " " + service.getServiceDate().getYear();
            monthlyCost.put(month, monthlyCost.getOrDefault(month, 0.0) + service.getCost());
        }

        List<String> sortedMonths = new java.util.ArrayList<>(monthlyCost.keySet());
        sortedMonths.sort((a, b) -> a.compareTo(b));

        for (String month : sortedMonths) {
            series.getData().add(new XYChart.Data<>(month, monthlyCost.get(month)));
        }

        costChart.getData().add(series);
    }

    private void loadTrendChart(List<ServiceRecord> services) {
        trendChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Service Frequency");

        Map<String, Integer> monthlyCount = new HashMap<>();
        for (ServiceRecord service : services) {
            String month = service.getServiceDate().getMonth().toString();
            monthlyCount.put(month, monthlyCount.getOrDefault(month, 0) + 1);
        }

        String[] months = {"JANUARY", "FEBRUARY", "MARCH", "APRIL", "MAY", "JUNE",
                "JULY", "AUGUST", "SEPTEMBER", "OCTOBER", "NOVEMBER", "DECEMBER"};
        for (String month : months) {
            series.getData().add(new XYChart.Data<>(month.substring(0, 3), monthlyCount.getOrDefault(month, 0)));
        }

        trendChart.getData().add(series);
    }
}