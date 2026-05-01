package controllers;

import javafx.animation.FadeTransition;
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
import utils.ValidationUtil;
import dao.VehicleSightingDAO;
import dao.VehicleDAO;
import models.VehicleSighting;
import models.Vehicle;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class TrafficCameraIntegrationController {

    @FXML private TableView<VehicleSighting> sightingsTable;
    @FXML private TableColumn<VehicleSighting, String> timestampColumn;
    @FXML private TableColumn<VehicleSighting, String> vehicleColumn;
    @FXML private TableColumn<VehicleSighting, String> cameraColumn;
    @FXML private TableColumn<VehicleSighting, String> locationColumn;
    @FXML private TableColumn<VehicleSighting, Double> confidenceColumn;

    @FXML private TextField licensePlateField;
    @FXML private TextField cameraIdField;
    @FXML private TextField latitudeField;
    @FXML private TextField longitudeField;
    @FXML private DatePicker sightingDatePicker;
    @FXML private ComboBox<String> sourceTypeComboBox;

    @FXML private Button addSightingButton;
    @FXML private Button searchButton;
    @FXML private Button refreshButton;
    @FXML private Button backButton;
    @FXML private Button fadeButton;

    @FXML private ProgressIndicator loadProgress;
    @FXML private ProgressBar operationProgress;
    @FXML private Label statusLabel;

    private VehicleSightingDAO sightingDAO;
    private VehicleDAO vehicleDAO;
    private ObservableList<VehicleSighting> sightingList;

    @FXML
    public void initialize() {
        sightingDAO = new VehicleSightingDAO();
        vehicleDAO = new VehicleDAO();
        sightingList = FXCollections.observableArrayList();

        setupTableColumns();
        setupComboBoxes();
        setupButtonHandlers();
        applyVisualEffects();
        loadRecentSightings();

        sightingDatePicker.setValue(LocalDate.now());
        statusLabel.setText("Ready");
    }

    private void setupTableColumns() {
        timestampColumn.setCellValueFactory(cellData -> cellData.getValue().timestampProperty().asString());
        vehicleColumn.setCellValueFactory(cellData -> cellData.getValue().licensePlateProperty());
        cameraColumn.setCellValueFactory(cellData -> cellData.getValue().sourceDeviceIdProperty());
        locationColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        String.format("%.4f, %.4f", cellData.getValue().getLatitude(), cellData.getValue().getLongitude())
                ));
        confidenceColumn.setCellValueFactory(cellData -> cellData.getValue().confidenceScoreProperty().asObject());

        // Center align columns
        timestampColumn.setStyle("-fx-alignment: CENTER;");
        vehicleColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        cameraColumn.setStyle("-fx-alignment: CENTER;");
        locationColumn.setStyle("-fx-alignment: CENTER;");
        confidenceColumn.setStyle("-fx-alignment: CENTER;");

        sightingsTable.setItems(sightingList);
        sightingsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void setupComboBoxes() {
        sourceTypeComboBox.getItems().addAll("traffic_camera", "toll_gate", "parking_lot", "gas_station", "anpr_system");
        sourceTypeComboBox.setValue("traffic_camera");
    }

    private void setupButtonHandlers() {
        addSightingButton.setOnAction(event -> handleAddSighting());
        searchButton.setOnAction(event -> handleSearch());
        refreshButton.setOnAction(event -> loadRecentSightings());
        backButton.setOnAction(event -> SceneManager.getInstance().switchToPoliceView());
        if (fadeButton != null) fadeButton.setOnAction(event -> showFadeAnimation());
    }

    private void applyVisualEffects() {
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(5.0);
        dropShadow.setOffsetX(2.0);
        dropShadow.setOffsetY(2.0);
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.3));

        addSightingButton.setEffect(dropShadow);
        searchButton.setEffect(dropShadow);
        refreshButton.setEffect(dropShadow);
        backButton.setEffect(dropShadow);
        if (fadeButton != null) fadeButton.setEffect(dropShadow);
    }

    private void showFadeAnimation() {
        if (fadeButton != null) {
            FadeTransition fadeTransition = new FadeTransition(Duration.seconds(1.5), fadeButton);
            fadeTransition.setFromValue(1.0);
            fadeTransition.setToValue(0.2);
            fadeTransition.setCycleCount(4);
            fadeTransition.setAutoReverse(true);
            fadeTransition.play();
            statusLabel.setText("Animation played!");
            AlertUtil.showInfo("Animation", "Fade animation completed!");
            PauseTransition reset = new PauseTransition(Duration.seconds(2));
            reset.setOnFinished(e -> statusLabel.setText("Ready"));
            reset.play();
        }
    }

    private void loadRecentSightings() {
        showProgress(true);
        statusLabel.setText("Loading sightings...");

        try {
            List<VehicleSighting> sightings = sightingDAO.findAll();
            sightingList.setAll(sightings);
            statusLabel.setText("Loaded " + sightings.size() + " recent sightings");
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error loading sightings");
            AlertUtil.showError("Load Failed", "Failed to load sightings: " + e.getMessage());
        } finally {
            hideProgress();
        }
    }

    private void handleAddSighting() {
        if (!ValidationUtil.isNotEmpty(licensePlateField.getText())) {
            AlertUtil.showWarning("Validation Error", "License plate is required.");
            return;
        }

        if (!ValidationUtil.isNotEmpty(latitudeField.getText()) || !ValidationUtil.isNotEmpty(longitudeField.getText())) {
            AlertUtil.showWarning("Validation Error", "Location coordinates are required.");
            return;
        }

        showOperationProgress(true);
        statusLabel.setText("Recording sighting...");
        updateProgress(0.3);

        try {
            String licensePlate = licensePlateField.getText().trim().toUpperCase();
            String sourceType = sourceTypeComboBox.getValue();
            String deviceId = cameraIdField.getText().trim();
            double latitude = Double.parseDouble(latitudeField.getText());
            double longitude = Double.parseDouble(longitudeField.getText());
            LocalDateTime timestamp = sightingDatePicker.getValue().atTime(LocalDateTime.now().toLocalTime());

            updateProgress(0.6);
            Vehicle vehicle = vehicleDAO.findByRegistrationNumber(licensePlate);
            Integer vehicleId = vehicle != null ? vehicle.getId() : null;

            VehicleSighting sighting = new VehicleSighting(
                    vehicleId != null ? vehicleId : 0,
                    licensePlate,
                    sourceType,
                    latitude,
                    longitude,
                    timestamp
            );
            sighting.setSourceDeviceId(deviceId);
            sighting.setConfidenceScore(0.95);

            updateProgress(0.8);
            boolean success = sightingDAO.insert(sighting);

            if (success) {
                updateProgress(1.0);
                AlertUtil.showSuccess("Sighting recorded successfully.");
                clearForm();
                loadRecentSightings();
                statusLabel.setText("Sighting added for " + licensePlate);
            } else {
                statusLabel.setText("Failed to record sighting");
                AlertUtil.showError("Add Failed", "Failed to record sighting.");
            }

        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid coordinates");
            AlertUtil.showError("Invalid Input", "Please enter valid coordinates.");
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error: " + e.getMessage());
            AlertUtil.showError("Database Error", "An error occurred while recording sighting.");
        } finally {
            hideProgressAfterDelay();
        }
    }

    private void handleSearch() {
        String licensePlate = licensePlateField.getText().trim();

        if (!ValidationUtil.isNotEmpty(licensePlate)) {
            AlertUtil.showWarning("Search Error", "Please enter a license plate number.");
            return;
        }

        showOperationProgress(true);
        statusLabel.setText("Searching for " + licensePlate + "...");
        updateProgress(0.5);

        try {
            List<VehicleSighting> sightings = sightingDAO.findByLicensePlate(licensePlate.toUpperCase());
            sightingList.setAll(sightings);
            updateProgress(1.0);
            statusLabel.setText("Found " + sightings.size() + " sightings for " + licensePlate);
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Search error: " + e.getMessage());
            AlertUtil.showError("Search Failed", "Failed to search sightings.");
        } finally {
            hideProgressAfterDelay();
        }
    }

    private void clearForm() {
        licensePlateField.clear();
        cameraIdField.clear();
        latitudeField.clear();
        longitudeField.clear();
        sightingDatePicker.setValue(LocalDate.now());
        sourceTypeComboBox.setValue("traffic_camera");
    }

    private void showProgress(boolean show) {
        if (loadProgress != null) loadProgress.setVisible(show);
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

    private void hideProgress() {
        if (loadProgress != null) loadProgress.setVisible(false);
    }

    private void hideProgressAfterDelay() {
        PauseTransition delay = new PauseTransition(Duration.seconds(1));
        delay.setOnFinished(event -> {
            if (operationProgress != null) {
                operationProgress.setVisible(false);
                operationProgress.setProgress(0);
            }
            if (loadProgress != null) loadProgress.setVisible(false);
        });
        delay.play();
    }
}