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
import utils.ValidationUtil;
import dao.VehicleDAO;
import dao.StolenVehicleDAO;
import models.Vehicle;
import models.StolenVehicle;  // ADDED: Missing import

public class VehicleTrackingController {

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
    @FXML private TitledPane trackingDetailsBox;

    @FXML private ProgressIndicator loadProgress;
    @FXML private ProgressBar operationProgress;

    private VehicleDAO vehicleDAO;
    private StolenVehicleDAO stolenVehicleDAO;
    private Vehicle currentVehicle;

    @FXML
    public void initialize() {
        vehicleDAO = new VehicleDAO();
        stolenVehicleDAO = new StolenVehicleDAO();

        setupButtonHandlers();
        applyVisualEffects();
        trackingDetailsBox.setVisible(false);
        trackingDetailsBox.setExpanded(false);
        statusMessageLabel.setText("Ready");
    }

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

    private void setupButtonHandlers() {
        trackButton.setOnAction(event -> handleTrack());
        locateButton.setOnAction(event -> handleLocate());
        alertButton.setOnAction(event -> handleSendAlert());
        backButton.setOnAction(event -> SceneManager.getInstance().switchToPoliceView());
        if (fadeButton != null) fadeButton.setOnAction(event -> showFadeAnimation());
    }

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
            currentVehicle = vehicleDAO.findByRegistrationNumber(registrationNumber);
            updateProgress(0.8);

            if (currentVehicle != null) {
                trackingDetailsBox.setVisible(true);
                trackingDetailsBox.setExpanded(true);

                registrationInfoLabel.setText(currentVehicle.getRegistrationNumber());
                vehicleInfoLabel.setText(currentVehicle.getMake() + " " + currentVehicle.getModel() +
                        " (" + currentVehicle.getYear() + ")");
                statusLabel.setText(currentVehicle.getStatusName());

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

    private void checkStolenStatus() {
        if (currentVehicle != null) {
            try {
                StolenVehicle stolen = stolenVehicleDAO.findActiveByVehicleId(currentVehicle.getId());
                if (stolen != null) {
                    AlertUtil.showWarning("STOLEN VEHICLE ALERT",
                            "This vehicle has been reported as stolen!\nCase Number: " + stolen.getCaseNumber());
                    statusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    statusLabel.setText("STOLEN - " + currentVehicle.getStatusName());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void handleLocate() {
        if (currentVehicle == null) {
            AlertUtil.showWarning("No Vehicle", "Please track a vehicle first.");
            return;
        }

        AlertUtil.showInfo("Location", "Vehicle location tracking:\n\n" +
                "Registration: " + currentVehicle.getRegistrationNumber() + "\n" +
                "Last Known Location: " + lastLocationLabel.getText() + "\n" +
                "Last Updated: " + lastUpdatedLabel.getText());
    }

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
                "Send alert for vehicle " + currentVehicle.getRegistrationNumber() + "?");

        if (confirmed) {
            try {
                String fullMessage = "ALERT: " + alertMessage + " - Vehicle: " + currentVehicle.getRegistrationNumber();
                AlertUtil.showSuccess("Alert sent successfully to all police units.");
                alertMessageArea.clear();
                statusMessageLabel.setText("Alert sent successfully");
            } catch (Exception e) {
                e.printStackTrace();
                AlertUtil.showError("Alert Failed", "Failed to send alert.");
                statusMessageLabel.setText("Failed to send alert");
            }
        }
    }

    private void showOperationProgress(boolean show) {
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
            if (operationProgress != null) {
                operationProgress.setVisible(false);
                operationProgress.setProgress(0);
            }
        });
        delay.play();
    }
}