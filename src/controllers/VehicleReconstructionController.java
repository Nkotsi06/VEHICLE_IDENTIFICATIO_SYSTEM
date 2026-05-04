package controllers;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import utils.AlertUtil;
import utils.SceneManager;
import utils.VehicleTrackerReconstructor;
import dao.VehicleDAO;
import models.Vehicle;

/**
 * Controller for Vehicle Movement Reconstruction
 * Reconstructs vehicle movement patterns and detects suspicious activity
 */
public class VehicleReconstructionController {

    // ============================================
    // FXML UI COMPONENTS
    // ============================================

    @FXML private ComboBox<Vehicle> vehicleComboBox;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Button reconstructButton;
    @FXML private Button exportButton;
    @FXML private Button backButton;
    @FXML private Button fadeButton;

    @FXML private Label totalDistanceLabel;
    @FXML private Label averageSpeedLabel;
    @FXML private Label sightingCountLabel;
    @FXML private Label suspiciousScoreLabel;
    @FXML private Label suspiciousLevelLabel;
    @FXML private TextArea reconstructionDetailsArea;
    @FXML private Label statusLabel;

    @FXML private ProgressIndicator loadProgress;
    @FXML private ProgressBar operationProgress;

    // ============================================
    // DAO INSTANCES & DATA MODELS
    // ============================================

    private VehicleDAO vehicleDAO;
    private VehicleTrackerReconstructor reconstructor;

    // ============================================
    // INITIALIZATION METHODS
    // ============================================

    /**
     * Initializes the controller - sets up DAOs, loads vehicles, configures UI
     */
    @FXML
    public void initialize() {
        vehicleDAO = new VehicleDAO();
        reconstructor = VehicleTrackerReconstructor.getInstance();

        loadVehicles();
        setupDatePickers();
        setupButtonHandlers();
        applyVisualEffects();
        statusLabel.setText("Ready");
    }

    /**
     * Applies drop shadow visual effects to buttons
     */
    private void applyVisualEffects() {
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(5.0);
        dropShadow.setOffsetX(2.0);
        dropShadow.setOffsetY(2.0);
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.3));

        reconstructButton.setEffect(dropShadow);
        exportButton.setEffect(dropShadow);
        backButton.setEffect(dropShadow);
        if (fadeButton != null) fadeButton.setEffect(dropShadow);
    }

    /**
     * Loads all vehicles into the combo box
     */
    private void loadVehicles() {
        try {
            java.util.List<Vehicle> vehicles = vehicleDAO.findAll();
            vehicleComboBox.getItems().setAll(vehicles);
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error loading vehicles");
        }
    }

    /**
     * Sets up default date range (last 7 days)
     */
    private void setupDatePickers() {
        startDatePicker.setValue(java.time.LocalDate.now().minusDays(7));
        endDatePicker.setValue(java.time.LocalDate.now());
    }

    /**
     * Sets up button click handlers
     */
    private void setupButtonHandlers() {
        reconstructButton.setOnAction(event -> handleReconstruct());
        exportButton.setOnAction(event -> handleExport());
        backButton.setOnAction(event -> SceneManager.getInstance().switchToPoliceView());
        if (fadeButton != null) fadeButton.setOnAction(event -> showFadeAnimation());
    }

    /**
     * Plays fade animation on the animate button
     */
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

    // ============================================
    // BUSINESS LOGIC METHODS
    // ============================================

    /**
     * Handles vehicle movement reconstruction
     */
    private void handleReconstruct() {
        Vehicle selectedVehicle = vehicleComboBox.getSelectionModel().getSelectedItem();

        // Input validation
        if (selectedVehicle == null) {
            AlertUtil.showWarning("Validation Error", "Please select a vehicle.");
            return;
        }

        if (startDatePicker.getValue() == null) {
            AlertUtil.showWarning("Validation Error", "Please select a start date.");
            return;
        }

        if (endDatePicker.getValue() == null) {
            AlertUtil.showWarning("Validation Error", "Please select an end date.");
            return;
        }

        if (startDatePicker.getValue().isAfter(endDatePicker.getValue())) {
            AlertUtil.showWarning("Validation Error", "Start date must be before end date.");
            return;
        }

        showOperationProgress(true);
        statusLabel.setText("Reconstructing vehicle movement...");
        updateProgress(0.2);

        try {
            updateProgress(0.5);
            reconstructionDetailsArea.setText("Reconstructing vehicle movement for " +
                    selectedVehicle.getRegistrationNumber() + "...\n");

            // Generate reconstruction report
            VehicleTrackerReconstructor.ReconstructionReport report =
                    reconstructor.generateReconstructionReport(
                            selectedVehicle.getId(),
                            startDatePicker.getValue(),
                            endDatePicker.getValue()
                    );

            updateProgress(0.9);
            displayResults(report);
            updateProgress(1.0);
            statusLabel.setText("Reconstruction completed");

        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error: " + e.getMessage());
            AlertUtil.showError("Reconstruction Failed", "An error occurred during reconstruction.");
        } finally {
            hideProgressAfterDelay();
        }
    }

    /**
     * Displays reconstruction results in the UI
     * @param report The reconstruction report containing all metrics
     */
    private void displayResults(VehicleTrackerReconstructor.ReconstructionReport report) {
        totalDistanceLabel.setText(String.format("%.2f km", report.totalDistance));
        averageSpeedLabel.setText(String.format("%.2f km/h", report.averageSpeed));
        sightingCountLabel.setText(String.valueOf(report.sightingCount));
        suspiciousScoreLabel.setText(String.format("%.1f%%", report.suspiciousScore * 100));
        suspiciousLevelLabel.setText(report.getSuspiciousLevel());

        String levelColor = report.getSuspiciousColor();
        suspiciousLevelLabel.setStyle("-fx-text-fill: " + levelColor + "; -fx-font-weight: bold;");

        // Build detailed report
        StringBuilder details = new StringBuilder();
        details.append("=== VEHICLE MOVEMENT RECONSTRUCTION REPORT ===\n\n");
        details.append("Vehicle ID: ").append(report.vehicleId).append("\n");
        details.append("Period: ").append(report.startDate).append(" to ").append(report.endDate).append("\n");
        details.append("Total Sightings: ").append(report.sightingCount).append("\n");
        details.append("Total Distance: ").append(String.format("%.2f km", report.totalDistance)).append("\n");
        details.append("Average Speed: ").append(String.format("%.2f km/h", report.averageSpeed)).append("\n");
        details.append("Suspicious Score: ").append(String.format("%.1f%%", report.suspiciousScore * 100)).append("\n");
        details.append("Suspicious Level: ").append(report.getSuspiciousLevel()).append("\n\n");

        if (report.suspiciousSegments != null && !report.suspiciousSegments.isEmpty()) {
            details.append("=== SUSPICIOUS SEGMENTS ===\n");
            for (var segment : report.suspiciousSegments) {
                details.append("- Speed: ").append(String.format("%.1f km/h", segment.speedKmph))
                        .append(" | Distance: ").append(segment.getFormattedDistance())
                        .append(" | Duration: ").append(segment.getFormattedDuration()).append("\n");
            }
            details.append("\n");
        }

        if (report.suspiciousTimeGaps != null && !report.suspiciousTimeGaps.isEmpty()) {
            details.append("=== SUSPICIOUS TIME GAPS ===\n");
            for (var gap : report.suspiciousTimeGaps) {
                details.append("- Gap: ").append(gap.getFormattedGap())
                        .append(" | Estimated Distance: ").append(String.format("%.2f km", gap.getEstimatedDistance()))
                        .append("\n");
            }
            details.append("\n");
        }

        if (report.recommendations != null && !report.recommendations.isEmpty()) {
            details.append("=== RECOMMENDATIONS ===\n");
            for (String rec : report.recommendations) {
                details.append("- ").append(rec).append("\n");
            }
            details.append("\n");
        }

        reconstructionDetailsArea.setText(details.toString());
        exportButton.setDisable(false);
    }

    /**
     * Handles exporting the reconstruction report to a file
     */
    private void handleExport() {
        String content = reconstructionDetailsArea.getText();
        if (content == null || content.isEmpty()) {
            AlertUtil.showWarning("No Data", "Please perform a reconstruction first.");
            return;
        }

        Vehicle selectedVehicle = vehicleComboBox.getSelectionModel().getSelectedItem();
        if (selectedVehicle != null) {
            String fileName = "reconstruction_" + selectedVehicle.getRegistrationNumber() + "_" +
                    java.time.LocalDate.now() + ".txt";
            utils.FileHandler.writeTextFile(utils.FileHandler.getReportsPath(), fileName, content);
            AlertUtil.showSuccess("Report exported to " + fileName);
            statusLabel.setText("Report exported");
        }
    }

    // ============================================
    // UI PROGRESS METHODS
    // ============================================

    /**
     * Shows/hides operation progress bar
     * @param show true to show, false to hide
     */
    private void showOperationProgress(boolean show) {
        if (operationProgress != null) {
            operationProgress.setVisible(show);
            operationProgress.setProgress(0);
        }
    }

    /**
     * Updates progress bar value
     * @param progress value between 0 and 1
     */
    private void updateProgress(double progress) {
        if (operationProgress != null) operationProgress.setProgress(progress);
    }

    /**
     * Hides progress indicators after a short delay
     */
    private void hideProgressAfterDelay() {
        PauseTransition delay = new PauseTransition(Duration.seconds(1.5));
        delay.setOnFinished(event -> {
            if (operationProgress != null) {
                operationProgress.setVisible(false);
                operationProgress.setProgress(0);
            }
        });
        delay.play();
    }
}