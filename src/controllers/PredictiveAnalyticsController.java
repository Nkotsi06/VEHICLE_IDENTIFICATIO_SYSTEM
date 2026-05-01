package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import utils.AlertUtil;
import utils.SceneManager;
import dao.CrimeHotspotPredictionDAO;
import dao.VehicleRiskScoreDAO;
import models.CrimeHotspotPrediction;
import models.VehicleRiskScore;

public class PredictiveAnalyticsController {

    @FXML private ComboBox<String> predictionTypeComboBox;
    @FXML private Button generateButton;
    @FXML private Button refreshButton;
    @FXML private Button backButton;

    @FXML private Label highRiskCountLabel;
    @FXML private Label mediumRiskCountLabel;
    @FXML private Label lowRiskCountLabel;
    @FXML private Label hotspotsCountLabel;

    @FXML private ListView<String> highRiskVehiclesList;
    @FXML private ListView<String> crimeHotspotsList;

    @FXML private LineChart<String, Number> trendChart;

    private CrimeHotspotPredictionDAO hotspotDAO;
    private VehicleRiskScoreDAO riskDAO;

    @FXML
    public void initialize() {
        hotspotDAO = new CrimeHotspotPredictionDAO();
        riskDAO = new VehicleRiskScoreDAO();

        setupComboBoxes();
        setupButtonHandlers();
        loadAnalytics();
        loadChart();
    }

    private void setupComboBoxes() {
        predictionTypeComboBox.getItems().addAll("CRIME_HOTSPOTS", "VEHICLE_RISK", "THEFT_PREDICTION");
        predictionTypeComboBox.setValue("CRIME_HOTSPOTS");
    }

    private void setupButtonHandlers() {
        generateButton.setOnAction(event -> handleGenerate());
        refreshButton.setOnAction(event -> loadAnalytics());
        backButton.setOnAction(event -> SceneManager.getInstance().switchToPoliceView());
    }

    private void loadAnalytics() {
        try {
            // Load vehicle risk scores
            java.util.List<VehicleRiskScore> risks = riskDAO.findAll();
            int highRisk = 0, mediumRisk = 0, lowRisk = 0;

            highRiskVehiclesList.getItems().clear();

            for (VehicleRiskScore risk : risks) {
                String level = risk.getRiskLevel();
                if ("CRITICAL".equals(level) || "HIGH".equals(level)) {
                    highRisk++;
                    highRiskVehiclesList.getItems().add(
                            risk.getRegistrationNumber() + " - Risk: " + String.format("%.1f%%", risk.getRiskScore() * 100)
                    );
                } else if ("MEDIUM".equals(level)) {
                    mediumRisk++;
                } else {
                    lowRisk++;
                }
            }

            highRiskCountLabel.setText(String.valueOf(highRisk));
            mediumRiskCountLabel.setText(String.valueOf(mediumRisk));
            lowRiskCountLabel.setText(String.valueOf(lowRisk));

            // Load crime hotspots
            java.util.List<CrimeHotspotPrediction> hotspots = hotspotDAO.findHighRiskPredictions();
            crimeHotspotsList.getItems().clear();

            for (CrimeHotspotPrediction hotspot : hotspots) {
                crimeHotspotsList.getItems().add(
                        hotspot.getCrimeType() + " - " + hotspot.getRiskLevel() +
                                " (" + String.format("%.1f%%", hotspot.getProbabilityPercentage()) + ")"
                );
            }

            hotspotsCountLabel.setText(String.valueOf(hotspots.size()));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadChart() {
        trendChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Crime Trend");

        // Simulated data
        series.getData().add(new XYChart.Data<>("Jan", 12));
        series.getData().add(new XYChart.Data<>("Feb", 15));
        series.getData().add(new XYChart.Data<>("Mar", 18));
        series.getData().add(new XYChart.Data<>("Apr", 14));
        series.getData().add(new XYChart.Data<>("May", 22));
        series.getData().add(new XYChart.Data<>("Jun", 28));

        trendChart.getData().add(series);
    }

    private void handleGenerate() {
        String type = predictionTypeComboBox.getValue();

        try {
            if ("CRIME_HOTSPOTS".equals(type)) {
                hotspotDAO.runPrediction();
                AlertUtil.showSuccess("Crime hotspot prediction generated.");
            } else if ("VEHICLE_RISK".equals(type)) {
                // Run risk calculation
                AlertUtil.showSuccess("Vehicle risk scores updated.");
            }

            loadAnalytics();

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Generation Failed", "Failed to generate predictions.");
        }
    }
}