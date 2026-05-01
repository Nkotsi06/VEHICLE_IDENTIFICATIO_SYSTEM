package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Duration;
import utils.AlertUtil;
import utils.SceneManager;
import utils.VehicleTrackerReconstructor;  // ADDED: Import
import dao.VehicleDAO;
import dao.VehicleMovementRecordDAO;
import models.Vehicle;
import models.VehicleMovementRecord;

public class VehicleMovementController {

    @FXML private ComboBox<Vehicle> vehicleComboBox;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Button reconstructButton;
    @FXML private Button exportButton;
    @FXML private Button backButton;

    @FXML private Label vehicleInfoLabel;
    @FXML private Label totalDistanceLabel;
    @FXML private Label averageSpeedLabel;
    @FXML private Label sightingCountLabel;
    @FXML private Label suspiciousScoreLabel;
    @FXML private Label suspiciousLevelLabel;
    @FXML private TextArea reconstructionDetailsArea;
    @FXML private Label statusLabel;

    @FXML private ProgressIndicator loadProgress;

    private VehicleDAO vehicleDAO;
    private VehicleMovementRecordDAO movementDAO;
    private VehicleTrackerReconstructor reconstructor;
    private VehicleMovementRecord currentRecord;

    @FXML
    public void initialize() {
        vehicleDAO = new VehicleDAO();
        movementDAO = new VehicleMovementRecordDAO();
        reconstructor = VehicleTrackerReconstructor.getInstance();

        loadVehicles();
        setupDatePickers();
        setupButtonHandlers();

        statusLabel.setText("Ready");
    }

    private void loadVehicles() {
        try {
            java.util.List<Vehicle> vehicles = vehicleDAO.findAll();
            vehicleComboBox.getItems().setAll(vehicles);
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error loading vehicles");
        }

        vehicleComboBox.setOnAction(e -> {
            Vehicle selected = vehicleComboBox.getSelectionModel().getSelectedItem();
            if (selected != null) {
                vehicleInfoLabel.setText(selected.getRegistrationNumber() + " - " +
                        selected.getMake() + " " + selected.getModel());
            }
        });
    }

    private void setupDatePickers() {
        startDatePicker.setValue(java.time.LocalDate.now().minusDays(7));
        endDatePicker.setValue(java.time.LocalDate.now());
    }

    private void setupButtonHandlers() {
        reconstructButton.setOnAction(event -> handleReconstruct());
        exportButton.setOnAction(event -> handleExport());
        backButton.setOnAction(event -> SceneManager.getInstance().switchToPoliceView());
    }

    private void handleReconstruct() {
        Vehicle selectedVehicle = vehicleComboBox.getSelectionModel().getSelectedItem();

        if (selectedVehicle == null) {
            AlertUtil.showWarning("Validation Error", "Please select a vehicle.");
            return;
        }

        if (startDatePicker.getValue() == null || endDatePicker.getValue() == null) {
            AlertUtil.showWarning("Validation Error", "Please select start and end dates.");
            return;
        }

        if (startDatePicker.getValue().isAfter(endDatePicker.getValue())) {
            AlertUtil.showWarning("Invalid Date Range", "Start date must be before end date.");
            return;
        }

        showProgress(true);
        statusLabel.setText("Reconstructing vehicle movement...");

        try {
            reconstructionDetailsArea.setText("Reconstructing vehicle movement for " +
                    selectedVehicle.getRegistrationNumber() + "...\n");

            currentRecord = reconstructor.reconstructMovement(
                    selectedVehicle.getId(),
                    startDatePicker.getValue(),
                    endDatePicker.getValue()
            );

            displayResults(currentRecord);
            statusLabel.setText("Reconstruction completed");

        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error: " + e.getMessage());
            AlertUtil.showError("Reconstruction Failed", "An error occurred during reconstruction.");
        } finally {
            showProgress(false);
        }
    }

    private void displayResults(VehicleMovementRecord record) {
        totalDistanceLabel.setText(String.format("%.2f km",
                record.getTotalDistanceKm() != null ? record.getTotalDistanceKm() : 0));
        averageSpeedLabel.setText(String.format("%.2f km/h",
                record.getAverageSpeedKmph() != null ? record.getAverageSpeedKmph() : 0));
        sightingCountLabel.setText(String.valueOf(record.getNumberOfSightings()));
        suspiciousScoreLabel.setText(String.format("%.1f%%",
                (record.getSuspiciousScore() != null ? record.getSuspiciousScore() : 0) * 100));
        suspiciousLevelLabel.setText(record.getSuspiciousLevel());

        StringBuilder details = new StringBuilder();
        details.append("=== VEHICLE MOVEMENT RECONSTRUCTION REPORT ===\n\n");
        details.append("Vehicle: ").append(vehicleComboBox.getValue().getRegistrationNumber()).append("\n");
        details.append("Period: ").append(startDatePicker.getValue()).append(" to ").append(endDatePicker.getValue()).append("\n\n");
        details.append("Total Sightings: ").append(record.getNumberOfSightings()).append("\n");
        details.append("Total Distance: ").append(String.format("%.2f km",
                record.getTotalDistanceKm() != null ? record.getTotalDistanceKm() : 0)).append("\n");
        details.append("Average Speed: ").append(String.format("%.2f km/h",
                record.getAverageSpeedKmph() != null ? record.getAverageSpeedKmph() : 0)).append("\n");
        details.append("Suspicious Score: ").append(String.format("%.1f%%",
                (record.getSuspiciousScore() != null ? record.getSuspiciousScore() : 0) * 100)).append("\n");
        details.append("Suspicious Level: ").append(record.getSuspiciousLevel()).append("\n\n");

        if (record.getSightings() != null && !record.getSightings().isEmpty()) {
            details.append("=== MOVEMENT TIMELINE ===\n");
            for (models.VehicleSighting sighting : record.getSightings()) {
                details.append(sighting.getSequenceNumber()).append(". ")
                        .append(sighting.getTimestamp()).append(" - ")
                        .append(sighting.getSourceType()).append(" - ")
                        .append(String.format("(%.4f, %.4f)", sighting.getLatitude(), sighting.getLongitude()))
                        .append("\n");
            }
        }

        reconstructionDetailsArea.setText(details.toString());
        exportButton.setDisable(false);
    }

    private void handleExport() {
        if (currentRecord == null) {
            AlertUtil.showWarning("No Data", "Please perform a reconstruction first.");
            return;
        }

        String content = reconstructionDetailsArea.getText();
        String fileName = "movement_reconstruction_" + System.currentTimeMillis() + ".txt";
        utils.FileHandler.writeTextFile(utils.FileHandler.getReportsPath(), fileName, content);
        AlertUtil.showSuccess("Export Complete", "Report saved to: " + fileName);
        statusLabel.setText("Report exported");
    }

    private void showProgress(boolean show) {
        if (loadProgress != null) loadProgress.setVisible(show);
    }
}