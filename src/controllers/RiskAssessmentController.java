package controllers;

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
import utils.CurrencyUtil;
import dao.VehicleRiskScoreDAO;
import dao.VehicleDAO;
import models.VehicleRiskScore;
import models.Vehicle;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class RiskAssessmentController {

    @FXML private TableView<VehicleRiskScore> riskTable;
    @FXML private TableColumn<VehicleRiskScore, String> vehicleColumn;
    @FXML private TableColumn<VehicleRiskScore, String> riskScoreColumn;
    @FXML private TableColumn<VehicleRiskScore, String> riskLevelColumn;
    @FXML private TableColumn<VehicleRiskScore, String> lastCalculatedColumn;

    @FXML private ComboBox<Vehicle> vehicleComboBox;
    @FXML private Label vehicleInfoLabel;
    @FXML private Label riskScoreLabel;
    @FXML private Label riskLevelLabel;
    @FXML private Label riskFactorsLabel;
    @FXML private Label recommendationLabel;
    @FXML private Label statusLabel;

    @FXML private Button calculateButton;
    @FXML private Button calculateAllButton;
    @FXML private Button refreshButton;
    @FXML private Button backButton;
    @FXML private Button fadeButton;
    @FXML private Button exportButton;

    @FXML private ProgressIndicator loadProgress;
    @FXML private ProgressBar operationProgress;
    @FXML private Pagination riskPagination;

    private VehicleRiskScoreDAO riskDAO;
    private VehicleDAO vehicleDAO;
    private ObservableList<VehicleRiskScore> riskList;
    private int currentPage = 0;
    private int pageSize = 10;
    private List<VehicleRiskScore> fullData;

    @FXML
    public void initialize() {
        riskDAO = new VehicleRiskScoreDAO();
        vehicleDAO = new VehicleDAO();
        riskList = FXCollections.observableArrayList();

        setupTableColumns();
        loadRiskScores();
        loadVehicles();
        setupButtonHandlers();
        setupPagination();
        applyVisualEffects();

        vehicleComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                loadVehicleRisk(newSelection.getId());
            }
        });

        riskTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                displayRiskDetails(newSelection);
            }
        });

        statusLabel.setText("Ready");
    }

    private void setupTableColumns() {
        vehicleColumn.setCellValueFactory(cellData -> cellData.getValue().registrationNumberProperty());
        riskScoreColumn.setCellValueFactory(cellData -> {
            double score = cellData.getValue().getRiskScore();
            return new javafx.beans.property.SimpleStringProperty(String.format("%.1f%%", score * 100));
        });
        riskLevelColumn.setCellValueFactory(cellData -> cellData.getValue().riskLevelProperty());
        lastCalculatedColumn.setCellValueFactory(cellData -> cellData.getValue().lastCalculationDateProperty().asString());

        vehicleColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        riskScoreColumn.setStyle("-fx-alignment: CENTER;");
        riskLevelColumn.setStyle("-fx-alignment: CENTER;");
        lastCalculatedColumn.setStyle("-fx-alignment: CENTER;");
    }

    private void setupPagination() {
        if (riskPagination != null) {
            riskPagination.setPageCount(1);
            riskPagination.setMaxPageIndicatorCount(5);
            riskPagination.currentPageIndexProperty().addListener((obs, old, newPage) -> {
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
            riskList.setAll(fullData.subList(start, end));
        }
    }

    private void loadRiskScores() {
        showProgress(true);
        statusLabel.setText("Loading risk assessments...");

        try {
            List<VehicleRiskScore> risks = riskDAO.findAll();
            fullData = risks;
            int totalPages = (int) Math.ceil((double) risks.size() / pageSize);
            if (riskPagination != null) riskPagination.setPageCount(Math.max(1, totalPages));
            updateTablePage();
            statusLabel.setText("Loaded " + risks.size() + " risk assessments");
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error loading risk assessments");
            AlertUtil.showError("Load Failed", "Failed to load risk assessments.");
        } finally {
            showProgress(false);
        }
    }

    private void loadVehicles() {
        try {
            List<Vehicle> vehicles = vehicleDAO.findAll();
            vehicleComboBox.getItems().setAll(vehicles);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadVehicleRisk(int vehicleId) {
        try {
            VehicleRiskScore risk = riskDAO.findByVehicleId(vehicleId);
            if (risk != null) {
                displayRiskDetails(risk);
            } else {
                vehicleInfoLabel.setText("Risk score not available for this vehicle.");
                riskScoreLabel.setText("Not calculated");
                riskLevelLabel.setText("Unknown");
                riskFactorsLabel.setText("Run risk calculation to generate score.");
                recommendationLabel.setText("Click 'Calculate Risk' to assess this vehicle.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void displayRiskDetails(VehicleRiskScore risk) {
        vehicleInfoLabel.setText(risk.getRegistrationNumber() + " - " + risk.getRiskLevel());
        riskScoreLabel.setText(String.format("%.1f%%", risk.getRiskScore() * 100));
        riskLevelLabel.setText(risk.getRiskLevel());

        if (risk.getRiskFactors() != null && !risk.getRiskFactors().isEmpty()) {
            riskFactorsLabel.setText(risk.getRiskFactors());
        } else {
            riskFactorsLabel.setText("No specific risk factors identified.");
        }

        generateRecommendation(risk);
    }

    private void generateRecommendation(VehicleRiskScore risk) {
        String recommendation;
        String riskLevel = risk.getRiskLevel();

        switch (riskLevel) {
            case "CRITICAL":
                recommendation = "IMMEDIATE ACTION REQUIRED: This vehicle poses a critical risk. "
                        + "Recommend immediate impoundment, court summons, and enhanced monitoring. "
                        + "Notify all patrol units and issue BOLO alert.";
                break;
            case "HIGH":
                recommendation = "HIGH RISK: This vehicle requires immediate attention. "
                        + "Recommend increased patrol monitoring, notify nearby units, "
                        + "and schedule officer follow-up within 24 hours.";
                break;
            case "MEDIUM":
                recommendation = "MEDIUM RISK: Monitor this vehicle regularly. "
                        + "Recommend periodic checks and documentation of all sightings. "
                        + "Consider adding to watch list.";
                break;
            case "LOW":
                recommendation = "LOW RISK: Standard monitoring recommended. "
                        + "No immediate action required. Continue routine checks.";
                break;
            default:
                recommendation = "MINIMAL RISK: This vehicle shows no concerning patterns. "
                        + "Standard monitoring only.";
                break;
        }

        recommendationLabel.setText(recommendation);

        String riskColor;
        switch (riskLevel) {
            case "CRITICAL":
                riskColor = "#e74c3c";
                break;
            case "HIGH":
                riskColor = "#e67e22";
                break;
            case "MEDIUM":
                riskColor = "#f39c12";
                break;
            case "LOW":
                riskColor = "#27ae60";
                break;
            default:
                riskColor = "#7f8c8d";
                break;
        }
        riskLevelLabel.setStyle("-fx-text-fill: " + riskColor + "; -fx-font-weight: bold;");
        riskScoreLabel.setStyle("-fx-text-fill: " + riskColor + "; -fx-font-weight: bold;");
    }

    private void setupButtonHandlers() {
        calculateButton.setOnAction(event -> handleCalculate());
        calculateAllButton.setOnAction(event -> handleCalculateAll());
        refreshButton.setOnAction(event -> {
            loadRiskScores();
            loadVehicles();
        });
        backButton.setOnAction(event -> handleBack());
        if (exportButton != null) exportButton.setOnAction(event -> handleExport());
        if (fadeButton != null) fadeButton.setOnAction(event -> showFadeAnimation());
    }

    private void showFadeAnimation() {
        if (fadeButton != null) {
            javafx.animation.FadeTransition fadeTransition = new javafx.animation.FadeTransition(Duration.seconds(1.5), fadeButton);
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

    private void applyVisualEffects() {
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(5.0);
        dropShadow.setOffsetX(2.0);
        dropShadow.setOffsetY(2.0);
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.3));

        calculateButton.setEffect(dropShadow);
        calculateAllButton.setEffect(dropShadow);
        refreshButton.setEffect(dropShadow);
        backButton.setEffect(dropShadow);
        if (exportButton != null) exportButton.setEffect(dropShadow);
    }

    private void handleCalculate() {
        Vehicle selectedVehicle = vehicleComboBox.getSelectionModel().getSelectedItem();

        if (selectedVehicle == null) {
            AlertUtil.showWarning("No Vehicle", "Please select a vehicle to calculate risk score.");
            return;
        }

        showProgress(true);
        statusLabel.setText("Calculating risk score...");
        updateProgress(0.3);

        try {
            updateProgress(0.6);
            riskDAO.calculateRiskScore(selectedVehicle.getId());
            Thread.sleep(500);

            VehicleRiskScore updatedRisk = riskDAO.findByVehicleId(selectedVehicle.getId());

            updateProgress(0.9);

            if (updatedRisk != null) {
                displayRiskDetails(updatedRisk);
                loadRiskScores();
                updateProgress(1.0);
                AlertUtil.showSuccess("Risk score calculated for " + selectedVehicle.getRegistrationNumber());
                statusLabel.setText("Risk score: " + String.format("%.1f%%", updatedRisk.getRiskScore() * 100));
            } else {
                AlertUtil.showError("Calculation Failed", "Failed to calculate risk score.");
                statusLabel.setText("Calculation failed");
            }

        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error: " + e.getMessage());
            AlertUtil.showError("Calculation Error", "An error occurred while calculating risk score.");
        } finally {
            hideProgressAfterDelay();
        }
    }

    private void handleCalculateAll() {
        boolean confirmed = AlertUtil.showConfirmation("Calculate All",
                "Calculate risk scores for all vehicles? This may take a few moments.");

        if (confirmed) {
            showProgress(true);
            statusLabel.setText("Calculating risk scores for all vehicles...");
            updateProgress(0.2);

            try {
                updateProgress(0.5);
                riskDAO.calculateAllRiskScores();

                Thread.sleep(1000);

                updateProgress(0.9);
                loadRiskScores();
                updateProgress(1.0);
                AlertUtil.showSuccess("Risk scores calculated for all vehicles.");
                statusLabel.setText("All risk scores updated");
            } catch (Exception e) {
                e.printStackTrace();
                statusLabel.setText("Error: " + e.getMessage());
                AlertUtil.showError("Calculation Failed", "Failed to calculate all risk scores.");
            } finally {
                hideProgressAfterDelay();
            }
        }
    }

    private void handleExport() {
        if (fullData == null || fullData.isEmpty()) {
            AlertUtil.showWarning("No Data", "No risk assessment data to export.");
            return;
        }

        showProgress(true);
        statusLabel.setText("Exporting risk data...");

        try {
            // Convert VehicleRiskScore objects to Map format for export
            List<Map<String, Object>> exportData = new ArrayList<>();

            for (VehicleRiskScore risk : fullData) {
                Map<String, Object> row = new HashMap<>();
                row.put("Registration", risk.getRegistrationNumber());
                row.put("Risk Score", String.format("%.1f%%", risk.getRiskScore() * 100));
                row.put("Risk Level", risk.getRiskLevel());
                row.put("Last Calculated", risk.getLastCalculationDate() != null ? risk.getLastCalculationDate().toString() : "");
                row.put("Risk Factors", risk.getRiskFactors() != null ? risk.getRiskFactors() : "");
                exportData.add(row);
            }

            String fileName = "risk_assessment_" + java.time.LocalDate.now();
            String[] columns = {"Registration", "Risk Score", "Risk Level", "Last Calculated", "Risk Factors"};

            utils.ExportUtil.exportToCSV(exportData, fileName, columns, columns);
            statusLabel.setText("Export completed");
            AlertUtil.showSuccess("Export completed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Export failed: " + e.getMessage());
            AlertUtil.showError("Export Failed", "Failed to export risk data.");
        } finally {
            showProgress(false);
        }
    }

    private void handleBack() {
        String role = SessionManager.getInstance().getUserRole();
        if ("ADMIN".equals(role)) {
            SceneManager.getInstance().switchToAdminView();
        } else if ("POLICE".equals(role)) {
            SceneManager.getInstance().switchToPoliceView();
        } else if ("INSURANCE".equals(role)) {
            SceneManager.getInstance().switchToInsurancePolicyView();
        } else {
            SceneManager.getInstance().switchToDashboard();
        }
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
        delay.setOnFinished(event -> {
            if (operationProgress != null) operationProgress.setVisible(false);
            if (loadProgress != null) loadProgress.setVisible(false);
        });
        delay.play();
    }
}