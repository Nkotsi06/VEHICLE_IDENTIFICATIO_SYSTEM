package controllers;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Duration;
import utils.AlertUtil;
import utils.SceneManager;
import utils.VehicleTrackerReconstructor;
import dao.VehicleDAO;
import dao.VehicleMovementRecordDAO;
import models.Vehicle;
import models.VehicleMovementRecord;
import models.VehicleMovementRecord.MovementSegment;
import models.VehicleSighting;
import java.time.format.DateTimeFormatter;

/**
 * Controller for Vehicle Movement Reconstruction
 * Reconstructs vehicle movement patterns and generates movement reports
 * Used by police to track suspicious vehicle activity and analyze movement patterns
 */
public class VehicleMovementController {

    // ============================================
    // FXML UI COMPONENTS
    // ============================================

    // Selection Controls
    @FXML private ComboBox<Vehicle> vehicleComboBox;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Button reconstructButton;
    @FXML private Button exportButton;
    @FXML private Button backButton;

    // Results Display
    @FXML private Label vehicleInfoLabel;
    @FXML private Label totalDistanceLabel;
    @FXML private Label averageSpeedLabel;
    @FXML private Label sightingCountLabel;
    @FXML private Label suspiciousScoreLabel;
    @FXML private Label suspiciousLevelLabel;
    @FXML private TextArea reconstructionDetailsArea;
    @FXML private Label statusLabel;

    // Progress Indicator
    @FXML private ProgressIndicator loadProgress;

    // ============================================
    // DAO INSTANCES & DATA MODELS
    // ============================================
    private VehicleDAO vehicleDAO;
    private VehicleMovementRecordDAO movementDAO;
    private VehicleTrackerReconstructor reconstructor;
    private VehicleMovementRecord currentRecord;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // ============================================
    // INITIALIZATION METHODS
    // ============================================

    /**
     * Initializes the vehicle movement controller
     * Sets up DAOs, loads vehicles, configures date pickers, and sets up button handlers
     */
    @FXML
    public void initialize() {
        vehicleDAO = new VehicleDAO();
        movementDAO = new VehicleMovementRecordDAO();
        reconstructor = VehicleTrackerReconstructor.getInstance();

        loadVehicles();
        setupDatePickers();
        setupButtonHandlers();

        statusLabel.setText("Ready");
        exportButton.setDisable(true);
    }

    /**
     * Loads all vehicles into the combo box
     * Sets up listener to display vehicle info when selected
     */
    private void loadVehicles() {
        try {
            java.util.List<Vehicle> vehicles = vehicleDAO.findAll();
            vehicleComboBox.getItems().setAll(vehicles);
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error loading vehicles");
            AlertUtil.showError("Load Failed", "Failed to load vehicles: " + e.getMessage());
        }

        // Add listener to display vehicle info when selected
        vehicleComboBox.setOnAction(e -> {
            Vehicle selected = vehicleComboBox.getSelectionModel().getSelectedItem();
            if (selected != null) {
                vehicleInfoLabel.setText(selected.getRegistrationNumber() + " - " +
                        selected.getMake() + " " + selected.getModel() + " (" + selected.getYear() + ")");
            }
        });
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
    }

    // ============================================
    // BUSINESS LOGIC METHODS
    // ============================================

    /**
     * Handles vehicle movement reconstruction
     * Validates input, calls reconstruction service, and displays results
     */
    private void handleReconstruct() {
        Vehicle selectedVehicle = vehicleComboBox.getSelectionModel().getSelectedItem();

        // Input validation
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
        reconstructionDetailsArea.setText("Reconstructing vehicle movement for " +
                selectedVehicle.getRegistrationNumber() + "...\n\n");

        try {
            // Perform movement reconstruction
            currentRecord = reconstructor.reconstructMovement(
                    selectedVehicle.getId(),
                    startDatePicker.getValue(),
                    endDatePicker.getValue()
            );

            if (currentRecord != null) {
                displayResults(currentRecord, selectedVehicle);
                statusLabel.setText("Reconstruction completed successfully");
                exportButton.setDisable(false);
            } else {
                reconstructionDetailsArea.setText("No movement data found for the selected period.");
                statusLabel.setText("No data found");
                AlertUtil.showWarning("No Data", "No movement data found for the selected period.");
                exportButton.setDisable(true);
            }

        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error: " + e.getMessage());
            reconstructionDetailsArea.setText("An error occurred during reconstruction:\n" + e.getMessage());
            AlertUtil.showError("Reconstruction Failed", "An error occurred during reconstruction: " + e.getMessage());
            exportButton.setDisable(true);
        } finally {
            showProgress(false);
        }
    }

    /**
     * Displays reconstruction results in the UI
     * @param record The movement record containing results
     * @param vehicle The vehicle that was analyzed
     */
    private void displayResults(VehicleMovementRecord record, Vehicle vehicle) {
        // Calculate and display statistics
        double totalDistance = record.getTotalDistanceKm() != null ? record.getTotalDistanceKm() : 0;
        double avgSpeed = record.getAverageSpeedKmph() != null ? record.getAverageSpeedKmph() : 0;
        int sightingCount = record.getNumberOfSightings() != null ? record.getNumberOfSightings() : 0;
        double suspiciousScore = record.getSuspiciousScore() != null ? record.getSuspiciousScore() : 0;

        totalDistanceLabel.setText(String.format("%.2f km", totalDistance));
        averageSpeedLabel.setText(String.format("%.2f km/h", avgSpeed));
        sightingCountLabel.setText(String.valueOf(sightingCount));
        suspiciousScoreLabel.setText(String.format("%.1f%%", suspiciousScore * 100));
        suspiciousLevelLabel.setText(record.getSuspiciousLevel());

        // Set color based on suspicious level
        String levelColor = getSuspiciousLevelColor(record.getSuspiciousLevel());
        suspiciousLevelLabel.setStyle("-fx-text-fill: " + levelColor + "; -fx-font-weight: bold;");

        // Build detailed report
        StringBuilder details = new StringBuilder();
        details.append("=".repeat(60)).append("\n");
        details.append("VEHICLE MOVEMENT RECONSTRUCTION REPORT\n");
        details.append("=".repeat(60)).append("\n\n");

        details.append("VEHICLE INFORMATION\n");
        details.append("-".repeat(40)).append("\n");
        details.append("Registration: ").append(vehicle.getRegistrationNumber()).append("\n");
        details.append("Make/Model: ").append(vehicle.getMake()).append(" ").append(vehicle.getModel()).append("\n");
        details.append("Year: ").append(vehicle.getYear()).append("\n\n");

        details.append("ANALYSIS PERIOD\n");
        details.append("-".repeat(40)).append("\n");
        details.append("Start Date: ").append(startDatePicker.getValue().format(dateFormatter)).append("\n");
        details.append("End Date: ").append(endDatePicker.getValue().format(dateFormatter)).append("\n");
        details.append("Duration: ").append(getDurationDays()).append(" days\n\n");

        details.append("MOVEMENT STATISTICS\n");
        details.append("-".repeat(40)).append("\n");
        details.append("Total Sightings: ").append(sightingCount).append("\n");
        details.append("Total Distance: ").append(String.format("%.2f km", totalDistance)).append("\n");
        details.append("Average Speed: ").append(String.format("%.2f km/h", avgSpeed)).append("\n");
        details.append("Suspicious Score: ").append(String.format("%.1f%%", suspiciousScore * 100)).append("\n");
        details.append("Suspicious Level: ").append(record.getSuspiciousLevel()).append("\n\n");

        // Add movement segments (using getSegments() instead of getSuspiciousSegments)
        java.util.List<MovementSegment> segments = record.getSegments();
        if (segments != null && !segments.isEmpty()) {
            details.append("MOVEMENT SEGMENTS\n");
            details.append("-".repeat(40)).append("\n");
            for (MovementSegment segment : segments) {
                details.append("Segment ").append(segment.getSegmentNumber()).append(":\n");
                details.append("  • Time: ").append(segment.getStartTime().format(formatter))
                        .append(" → ").append(segment.getEndTime().format(formatter)).append("\n");
                details.append("  • Distance: ").append(String.format("%.2f km", segment.getDistanceKm())).append("\n");
                details.append("  • Speed: ").append(String.format("%.1f km/h", segment.getSpeedKmph()));
                if (segment.isSpeeding()) {
                    details.append(" ⚠️ SPEEDING");
                }
                details.append("\n");
                details.append("  • Source: ").append(segment.getSourceType()).append("\n\n");
            }
        }

        // Add movement timeline (sightings)
        java.util.List<VehicleSighting> sightings = record.getSightings();
        if (sightings != null && !sightings.isEmpty()) {
            details.append("MOVEMENT TIMELINE\n");
            details.append("-".repeat(40)).append("\n");
            int limit = Math.min(sightings.size(), 20); // Limit to 20 entries
            for (int i = 0; i < limit; i++) {
                VehicleSighting sighting = sightings.get(i);
                details.append(String.format("%3d. ", i + 1))
                        .append(sighting.getTimestamp().format(formatter)).append(" | ")
                        .append(sighting.getSourceType()).append(" | ");
                if (sighting.getEstimatedSpeed() != null) {
                    details.append(String.format("%.1f km/h", sighting.getEstimatedSpeed()));
                } else {
                    details.append("speed unknown");
                }
                details.append("\n");
            }
            if (sightings.size() > 20) {
                details.append("... and ").append(sightings.size() - 20).append(" more sightings\n");
            }
            details.append("\n");
        }

        // Add recommendations based on suspicious score
        details.append("RECOMMENDATIONS\n");
        details.append("-".repeat(40)).append("\n");
        if (suspiciousScore > 0.7) {
            details.append("• HIGH RISK: Immediate investigation recommended\n");
            details.append("• Consider issuing BOLO alert for this vehicle\n");
            details.append("• Notify patrol units in the area\n");
            details.append("• Vehicle may be involved in criminal activity\n");
        } else if (suspiciousScore > 0.4) {
            details.append("• MEDIUM RISK: Monitor vehicle activity\n");
            details.append("• Add to watchlist for further observation\n");
            details.append("• Flag for traffic patrols\n");
        } else if (suspiciousScore > 0.2) {
            details.append("• LOW RISK: Routine monitoring only\n");
            details.append("• No immediate action required\n");
        } else {
            details.append("• MINIMAL RISK: Normal vehicle behavior\n");
            details.append("• No further action recommended\n");
        }

        // Add additional details if vehicle has segments with speeding
        if (segments != null) {
            long speedingCount = segments.stream().filter(MovementSegment::isSpeeding).count();
            if (speedingCount > 0) {
                details.append("\n⚠️ NOTE: ").append(speedingCount)
                        .append(" segment(s) showed excessive speeding detected.\n");
            }
        }

        details.append("\n").append("=".repeat(60)).append("\n");
        details.append("Report generated on: ").append(java.time.LocalDateTime.now().format(formatter)).append("\n");
        details.append("Generated by: ").append(utils.SessionManager.getInstance().getFullName()).append("\n");
        details.append("=".repeat(60));

        reconstructionDetailsArea.setText(details.toString());
    }

    /**
     * Gets the color for suspicious level display
     * @param level The suspicious level string
     * @return CSS color value
     */
    private String getSuspiciousLevelColor(String level) {
        if (level == null) return "#666666";
        switch (level.toUpperCase()) {
            case "VERY_HIGH": return "#F44336";
            case "HIGH": return "#FF5722";
            case "MEDIUM": return "#FF9800";
            case "LOW": return "#4CAF50";
            case "MINIMAL": return "#8BC34A";
            default: return "#666666";
        }
    }

    /**
     * Calculates the number of days in the selected date range
     * @return Number of days between start and end dates
     */
    private long getDurationDays() {
        return java.time.temporal.ChronoUnit.DAYS.between(startDatePicker.getValue(), endDatePicker.getValue()) + 1;
    }

    // ============================================
    // EXPORT METHOD
    // ============================================

    /**
     * Handles exporting the reconstruction report to a text file
     */
    private void handleExport() {
        if (currentRecord == null) {
            AlertUtil.showWarning("No Data", "Please perform a reconstruction first.");
            return;
        }

        Vehicle selectedVehicle = vehicleComboBox.getSelectionModel().getSelectedItem();
        if (selectedVehicle == null) {
            AlertUtil.showWarning("No Vehicle", "No vehicle selected.");
            return;
        }

        String content = reconstructionDetailsArea.getText();
        if (content == null || content.isEmpty()) {
            AlertUtil.showWarning("No Data", "No reconstruction data to export.");
            return;
        }

        try {
            String fileName = "movement_reconstruction_" + selectedVehicle.getRegistrationNumber() +
                    "_" + System.currentTimeMillis() + ".txt";
            utils.FileHandler.writeTextFile(utils.FileHandler.getReportsPath(), fileName, content);
            AlertUtil.showSuccess("Export Complete", "Report saved to: " + fileName);
            statusLabel.setText("Report exported to " + fileName);
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Export Failed", "Failed to export report: " + e.getMessage());
            statusLabel.setText("Export failed");
        }
    }

    // ============================================
    // UI PROGRESS METHODS
    // ============================================

    /**
     * Shows/hides the progress indicator
     * @param show true to show, false to hide
     */
    private void showProgress(boolean show) {
        if (loadProgress != null) {
            loadProgress.setVisible(show);
        }

        // Disable reconstruct button while processing
        reconstructButton.setDisable(show);

        if (show) {
            statusLabel.setText("Processing...");
        } else {
            // Small delay before enabling button to prevent rapid clicks
            PauseTransition delay = new PauseTransition(Duration.millis(500));
            delay.setOnFinished(e -> reconstructButton.setDisable(false));
            delay.play();
        }
    }
}