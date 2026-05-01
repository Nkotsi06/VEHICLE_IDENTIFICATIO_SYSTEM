package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import utils.AlertUtil;
import utils.SceneManager;
import dao.VehicleSightingDAO;
import dao.VehicleMovementDAO;
import dao.VehicleDAO;
import models.VehicleSighting;
import models.Vehicle;
import java.time.LocalDateTime;

public class VehicleSightingController {

    @FXML private TableView<VehicleSighting> sightingsTable;
    @FXML private TableColumn<VehicleSighting, String> timestampColumn;
    @FXML private TableColumn<VehicleSighting, String> vehicleColumn;
    @FXML private TableColumn<VehicleSighting, String> sourceColumn;
    @FXML private TableColumn<VehicleSighting, Double> confidenceColumn;
    @FXML private TableColumn<VehicleSighting, String> locationColumn;

    @FXML private TextField licensePlateField;
    @FXML private TextField cameraIdField;
    @FXML private TextField latitudeField;
    @FXML private TextField longitudeField;
    @FXML private DatePicker sightingDatePicker;
    @FXML private ComboBox<String> sourceTypeComboBox;

    @FXML private Button addButton;
    @FXML private Button searchButton;
    @FXML private Button refreshButton;
    @FXML private Button mapViewButton;
    @FXML private Button backButton;

    @FXML private Label statusLabel;

    private VehicleSightingDAO sightingDAO;
    private VehicleMovementDAO movementDAO;
    private VehicleDAO vehicleDAO;

    @FXML
    public void initialize() {
        sightingDAO = new VehicleSightingDAO();
        movementDAO = new VehicleMovementDAO();
        vehicleDAO = new VehicleDAO();

        setupTableColumns();
        setupComboBoxes();
        setupButtonHandlers();
        loadRecentSightings();

        sightingDatePicker.setValue(java.time.LocalDate.now());
    }

    private void setupTableColumns() {
        timestampColumn.setCellValueFactory(cellData -> cellData.getValue().timestampProperty().asString());
        vehicleColumn.setCellValueFactory(cellData -> cellData.getValue().licensePlateProperty());
        sourceColumn.setCellValueFactory(cellData -> cellData.getValue().sourceTypeProperty());
        confidenceColumn.setCellValueFactory(cellData -> cellData.getValue().confidenceScoreProperty().asObject());
        locationColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        String.format("%.4f, %.4f", cellData.getValue().getLatitude(), cellData.getValue().getLongitude())
                ));
    }

    private void setupComboBoxes() {
        sourceTypeComboBox.getItems().addAll("traffic_camera", "toll_gate", "parking_lot", "gas_station", "anpr_system");
        sourceTypeComboBox.setValue("traffic_camera");
    }

    private void setupButtonHandlers() {
        addButton.setOnAction(event -> handleAddSighting());
        searchButton.setOnAction(event -> handleSearch());
        refreshButton.setOnAction(event -> loadRecentSightings());
        mapViewButton.setOnAction(event -> handleMapView());
        backButton.setOnAction(event -> SceneManager.getInstance().switchToPoliceView());
    }

    private void loadRecentSightings() {
        try {
            java.util.List<VehicleSighting> sightings = sightingDAO.findAll();
            sightingsTable.getItems().setAll(sightings);
            statusLabel.setText("Loaded " + sightings.size() + " recent sightings");
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error loading sightings");
        }
    }

    private void handleAddSighting() {
        if (!utils.ValidationUtil.isNotEmpty(licensePlateField.getText())) {
            AlertUtil.showWarning("Validation Error", "License plate is required.");
            return;
        }

        if (!utils.ValidationUtil.isNotEmpty(latitudeField.getText()) ||
                !utils.ValidationUtil.isNotEmpty(longitudeField.getText())) {
            AlertUtil.showWarning("Validation Error", "Location coordinates are required.");
            return;
        }

        try {
            String licensePlate = licensePlateField.getText().trim().toUpperCase();
            String sourceType = sourceTypeComboBox.getValue();
            String deviceId = cameraIdField.getText().trim();
            double latitude = Double.parseDouble(latitudeField.getText());
            double longitude = Double.parseDouble(longitudeField.getText());
            LocalDateTime timestamp = sightingDatePicker.getValue().atTime(java.time.LocalTime.now());

            Vehicle vehicle = vehicleDAO.findByRegistrationNumber(licensePlate);
            Integer vehicleId = vehicle != null ? vehicle.getId() : null;

            boolean success = false;

            switch (sourceType) {
                case "traffic_camera":
                    success = sightingDAO.insertTrafficCameraSighting(
                            vehicleId, licensePlate, deviceId, latitude, longitude, timestamp);
                    break;
                case "toll_gate":
                    success = movementDAO.addTollGateSighting(
                            vehicleId != null ? vehicleId : 0,
                            licensePlate,
                            deviceId,
                            latitude,
                            longitude,
                            timestamp,
                            "NORTH",
                            0.0);
                    break;
                case "parking_lot":
                    success = movementDAO.addParkingLog(
                            vehicleId != null ? vehicleId : 0,
                            licensePlate,
                            deviceId,
                            latitude,
                            longitude,
                            timestamp,
                            null);
                    break;
                case "gas_station":
                    success = movementDAO.addGasStationSighting(
                            vehicleId != null ? vehicleId : 0,
                            licensePlate,
                            deviceId,
                            latitude,
                            longitude,
                            timestamp,
                            "UNLEADED");
                    break;
                case "anpr_system":
                    success = sightingDAO.insertANPRSighting(
                            vehicleId, licensePlate, deviceId, latitude, longitude, timestamp, 0.95);
                    break;
                default:
                    success = sightingDAO.insertTrafficCameraSighting(
                            vehicleId, licensePlate, deviceId, latitude, longitude, timestamp);
                    break;
            }

            if (success) {
                AlertUtil.showSuccess("Sighting recorded successfully.");
                clearForm();
                loadRecentSightings();
                statusLabel.setText("Sighting added for " + licensePlate);
            } else {
                AlertUtil.showError("Add Failed", "Failed to record sighting.");
            }

        } catch (NumberFormatException e) {
            AlertUtil.showError("Invalid Input", "Please enter valid coordinates.");
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "An error occurred: " + e.getMessage());
        }
    }

    private void handleSearch() {
        String licensePlate = licensePlateField.getText().trim();

        if (!utils.ValidationUtil.isNotEmpty(licensePlate)) {
            AlertUtil.showWarning("Search Error", "Please enter a license plate number.");
            return;
        }

        try {
            java.util.List<VehicleSighting> sightings = sightingDAO.findByLicensePlate(licensePlate.toUpperCase());
            sightingsTable.getItems().setAll(sightings);
            statusLabel.setText("Found " + sightings.size() + " sightings for " + licensePlate);
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error searching sightings");
        }
    }

    private void handleMapView() {
        VehicleSighting selected = sightingsTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            AlertUtil.showWarning("No Selection", "Please select a sighting to view on map.");
            return;
        }

        AlertUtil.showInfo("Location",
                "Latitude: " + selected.getLatitude() + "\n" +
                        "Longitude: " + selected.getLongitude() + "\n" +
                        "Source: " + selected.getSourceType() + "\n" +
                        "Time: " + selected.getTimestamp());
    }

    private void clearForm() {
        licensePlateField.clear();
        cameraIdField.clear();
        latitudeField.clear();
        longitudeField.clear();
        sightingDatePicker.setValue(java.time.LocalDate.now());
        sourceTypeComboBox.setValue("traffic_camera");
    }
}