package controllers;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import utils.AlertUtil;
import utils.SceneManager;
import utils.ValidationUtil;
import dao.VehicleDAO;
import dao.StolenVehicleDAO;
import models.Vehicle;
import models.StolenVehicle;

/**
 * Controller for Vehicle Tracking
 * Allows police officers to track vehicle locations and send alerts
 */
public class VehicleTrackingController {

    // ============================================
    // FXML UI COMPONENTS
    // ============================================

    @FXML private TextField registrationField;
    @FXML private Button trackButton;
    @FXML private Button locateButton;
    @FXML private Button alertButton;
    @FXML private Button backButton;
    @FXML private Button fadeButton;

    @FXML private Label registrationInfoLabel;
    @FXML private Label vehicleInfoLabel;
    @FXML private Label statusLabel;
    @FXML private Label lastLocationLabel;
    @FXML private Label lastUpdatedLabel;
    @FXML private Label statusMessageLabel;
    @FXML private TextArea alertMessageArea;

    // FIXED: Changed from TitledPane to VBox to match FXML
    @FXML private VBox trackingDetailsBox;

    @FXML private ProgressIndicator loadProgress;
    @FXML private ProgressBar operationProgress;

    // Table columns for location history
    @FXML private TableColumn<Object, String> historyDateColumn;
    @FXML private TableColumn<Object, String> historyLocationColumn;
    @FXML private TableColumn<Object, String> historySourceColumn;
    @FXML private Pagination historyPagination;

    // ============================================
    // DAO INSTANCES & DATA MODELS
    // ============================================

    private VehicleDAO vehicleDAO;
    private StolenVehicleDAO stolenVehicleDAO;
    private Vehicle currentVehicle;

    // ============================================
    // INITIALIZATION METHODS
    // ============================================

    /**
     * Initializes the controller - sets up DAOs and UI components
     */
    @FXML
    public void initialize() {
        vehicleDAO = new VehicleDAO();
        stolenVehicleDAO = new StolenVehicleDAO();

        setupButtonHandlers();
        applyVisualEffects();
        setupTableColumns();

        // Hide tracking details initially (VBox doesn't have setExpanded, only setVisible)
        trackingDetailsBox.setVisible(false);
        statusMessageLabel.setText("Ready");
    }

    /**
     * Sets up table column cell value factories
     */
    private void setupTableColumns() {
        if (historyDateColumn != null) {
            historyDateColumn.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(""));
        }
        if (historyLocationColumn != null) {
            historyLocationColumn.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(""));
        }
        if (historySourceColumn != null) {
            historySourceColumn.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(""));
        }
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

        trackButton.setEffect(dropShadow);
        locateButton.setEffect(dropShadow);
        alertButton.setEffect(dropShadow);
        backButton.setEffect(dropShadow);
        if (fadeButton != null) fadeButton.setEffect(dropShadow);
    }

    /**
     * Sets up button click handlers
     */
    private void setupButtonHandlers() {
        trackButton.setOnAction(event -> handleTrack());
        locateButton.setOnAction(event -> handleLocate());
        alertButton.setOnAction(event -> handleSendAlert());
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
            statusMessageLabel.setText("Animation played!");
            PauseTransition reset = new PauseTransition(Duration.seconds(2));
            reset.setOnFinished(e -> statusMessageLabel.setText("Ready"));
            reset.play();
        }
    }

    // ============================================
    // BUSINESS LOGIC METHODS
    // ============================================

    /**
     * Handles tracking a vehicle by registration number
     */
    private void handleTrack() {
        String registrationNumber = registrationField.getText().trim();

        if (!ValidationUtil.isNotEmpty(registrationNumber)) {
            AlertUtil.showWarning("Input Error", "Please enter a registration number.");
            return;
        }

        showOperationProgress(true);
        statusMessageLabel.setText("Searching for vehicle...");
        updateProgress(0.3);

        try {
            // Search for vehicle in database
            currentVehicle = vehicleDAO.findByRegistrationNumber(registrationNumber);
            updateProgress(0.8);

            if (currentVehicle != null) {
                // Display vehicle information - VBox only has setVisible, not setExpanded
                trackingDetailsBox.setVisible(true);

                registrationInfoLabel.setText(currentVehicle.getRegistrationNumber());
                vehicleInfoLabel.setText(currentVehicle.getMake() + " " + currentVehicle.getModel() +
                        " (" + currentVehicle.getYear() + ")");
                statusLabel.setText(currentVehicle.getStatusName());

                // Display location if available
                if (currentVehicle.getCurrentLocationLat() != null && currentVehicle.getCurrentLocationLng() != null) {
                    lastLocationLabel.setText(String.format("Lat: %.6f, Lng: %.6f",
                            currentVehicle.getCurrentLocationLat(), currentVehicle.getCurrentLocationLng()));
                } else {
                    lastLocationLabel.setText("Location not available");
                }

                if (currentVehicle.getLastUpdatedLocation() != null) {
                    lastUpdatedLabel.setText(currentVehicle.getLastUpdatedLocation().toString());
                } else {
                    lastUpdatedLabel.setText("Not available");
                }

                updateProgress(1.0);
                statusMessageLabel.setText("Vehicle found - Tracking active");
                checkStolenStatus();
            } else {
                AlertUtil.showWarning("Not Found", "No vehicle found with registration: " + registrationNumber);
                trackingDetailsBox.setVisible(false);
                statusMessageLabel.setText("No vehicle found with that registration number");
            }

        } catch (Exception e) {
            e.printStackTrace();
            statusMessageLabel.setText("Error occurred while tracking");
            AlertUtil.showError("Tracking Failed", "An error occurred while tracking the vehicle.");
        } finally {
            hideProgressAfterDelay();
        }
    }

    /**
     * Checks if the tracked vehicle has been reported stolen
     */
    private void checkStolenStatus() {
        if (currentVehicle != null) {
            try {
                StolenVehicle stolen = stolenVehicleDAO.findActiveByVehicleId(currentVehicle.getId());
                if (stolen != null) {
                    AlertUtil.showWarning("STOLEN VEHICLE ALERT",
                            "This vehicle has been reported as stolen!\nCase Number: " + stolen.getCaseNumber());
                    statusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    statusLabel.setText("STOLEN - " + currentVehicle.getStatusName());
                } else {
                    statusLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: normal;");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Displays location information for the tracked vehicle
     */
    private void handleLocate() {
        if (currentVehicle == null) {
            AlertUtil.showWarning("No Vehicle", "Please track a vehicle first.");
            return;
        }

        AlertUtil.showInfo("Location Information",
                "Vehicle Location Details:\n\n" +
                        "Registration: " + currentVehicle.getRegistrationNumber() + "\n" +
                        "Make/Model: " + currentVehicle.getMake() + " " + currentVehicle.getModel() + "\n" +
                        "Status: " + currentVehicle.getStatusName() + "\n\n" +
                        "Last Known Location: " + lastLocationLabel.getText() + "\n" +
                        "Last Updated: " + lastUpdatedLabel.getText());
    }

    /**
     * Sends an alert message for the tracked vehicle
     */
    private void handleSendAlert() {
        if (currentVehicle == null) {
            AlertUtil.showWarning("No Vehicle", "Please track a vehicle first.");
            return;
        }

        String alertMessage = alertMessageArea.getText().trim();

        if (!ValidationUtil.isNotEmpty(alertMessage)) {
            AlertUtil.showWarning("Input Error", "Please enter an alert message.");
            return;
        }

        boolean confirmed = AlertUtil.showConfirmation("Send Alert",
                "Send alert for vehicle " + currentVehicle.getRegistrationNumber() + "?\n\n" +
                        "Message: " + alertMessage);

        if (confirmed) {
            try {
                // In production, this would send to all police units
                String fullMessage = "ALERT: " + alertMessage + " - Vehicle: " + currentVehicle.getRegistrationNumber();

                // Log the alert (in production, this would send to backend)
                System.out.println("Alert sent: " + fullMessage);

                AlertUtil.showSuccess("Alert Sent", "Alert sent successfully to all police units.");
                alertMessageArea.clear();
                statusMessageLabel.setText("Alert sent successfully");
            } catch (Exception e) {
                e.printStackTrace();
                AlertUtil.showError("Alert Failed", "Failed to send alert. Please try again.");
                statusMessageLabel.setText("Failed to send alert");
            }
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
            if (show) {
                operationProgress.setProgress(0);
            }
        }
        if (loadProgress != null) {
            loadProgress.setVisible(show);
        }
    }

    /**
     * Updates progress bar value
     * @param progress value between 0 and 1
     */
    private void updateProgress(double progress) {
        if (operationProgress != null) {
            operationProgress.setProgress(progress);
        }
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
            if (loadProgress != null) {
                loadProgress.setVisible(false);
            }
        });
        delay.play();
    }
}