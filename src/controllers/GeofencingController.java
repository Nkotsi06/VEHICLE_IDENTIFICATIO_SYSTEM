package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.control.Label;
import utils.AlertUtil;
import utils.SceneManager;
import utils.GeofencingUtil;
import dao.GeofenceZoneDAO;
import dao.GeofenceAlertEventDAO;
import models.GeofenceZone;
import models.GeofenceAlertEvent;

public class GeofencingController {

    @FXML private TableView<GeofenceZone> zonesTable;
    @FXML private TableColumn<GeofenceZone, String> nameColumn;
    @FXML private TableColumn<GeofenceZone, String> typeColumn;
    @FXML private TableColumn<GeofenceZone, Integer> radiusColumn;
    @FXML private TableColumn<GeofenceZone, String> statusColumn;

    @FXML private TableView<GeofenceAlertEvent> alertsTable;
    @FXML private TableColumn<GeofenceAlertEvent, String> zoneColumn;
    @FXML private TableColumn<GeofenceAlertEvent, String> vehicleColumn;
    @FXML private TableColumn<GeofenceAlertEvent, String> alertTypeColumn;
    @FXML private TableColumn<GeofenceAlertEvent, String> timestampColumn;

    @FXML private TextField zoneNameField;
    @FXML private TextField latitudeField;
    @FXML private TextField longitudeField;
    @FXML private TextField radiusField;
    @FXML private ComboBox<String> zoneTypeComboBox;
    @FXML private ComboBox<String> zoneStatusComboBox;

    @FXML private Button addZoneButton;
    @FXML private Button updateZoneButton;
    @FXML private Button deleteZoneButton;
    @FXML private Button refreshButton;
    @FXML private Button backButton;

    private GeofenceZoneDAO zoneDAO;
    private GeofenceAlertEventDAO alertDAO;
    private GeofencingUtil geofencingUtil;
    private GeofenceZone selectedZone;

    @FXML
    public void initialize() {
        zoneDAO = new GeofenceZoneDAO();
        alertDAO = new GeofenceAlertEventDAO();
        geofencingUtil = GeofencingUtil.getInstance();

        setupTableColumns();
        loadZones();
        loadAlerts();
        setupComboBoxes();
        setupButtonHandlers();
        setupTableSelection();
    }

    private void setupTableColumns() {
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().zoneNameProperty());
        typeColumn.setCellValueFactory(cellData -> cellData.getValue().zoneTypeProperty());
        radiusColumn.setCellValueFactory(cellData -> cellData.getValue().radiusMetersProperty().asObject());
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().activeProperty().asString());

        zoneColumn.setCellValueFactory(cellData -> cellData.getValue().zoneNameProperty());
        vehicleColumn.setCellValueFactory(cellData -> cellData.getValue().registrationNumberProperty());
        alertTypeColumn.setCellValueFactory(cellData -> cellData.getValue().alertTypeProperty());
        timestampColumn.setCellValueFactory(cellData -> cellData.getValue().alertTimestampProperty().asString());
    }

    private void loadZones() {
        try {
            java.util.List<GeofenceZone> zones = zoneDAO.findAll();
            zonesTable.getItems().setAll(zones);
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Load Failed", "Failed to load geofence zones.");
        }
    }

    private void loadAlerts() {
        try {
            java.util.List<GeofenceAlertEvent> alerts = alertDAO.findAll();
            alertsTable.getItems().setAll(alerts);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupComboBoxes() {
        zoneTypeComboBox.getItems().addAll("HIGH_CRIME", "RESTRICTED", "MONITORED", "SCHOOL_ZONE");
        zoneStatusComboBox.getItems().addAll("ACTIVE", "INACTIVE");
    }

    private void setupButtonHandlers() {
        addZoneButton.setOnAction(event -> handleAddZone());
        updateZoneButton.setOnAction(event -> handleUpdateZone());
        deleteZoneButton.setOnAction(event -> handleDeleteZone());
        refreshButton.setOnAction(event -> {
            loadZones();
            loadAlerts();
        });
        backButton.setOnAction(event -> SceneManager.getInstance().switchToPoliceView());
    }

    private void setupTableSelection() {
        zonesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedZone = newSelection;
                displayZoneDetails(selectedZone);
            }
        });
    }

    private void displayZoneDetails(GeofenceZone zone) {
        zoneNameField.setText(zone.getZoneName());
        latitudeField.setText(String.valueOf(zone.getCenterLat()));
        longitudeField.setText(String.valueOf(zone.getCenterLng()));
        radiusField.setText(String.valueOf(zone.getRadiusMeters()));
        zoneTypeComboBox.setValue(zone.getZoneType());
        zoneStatusComboBox.setValue(zone.isActive() ? "ACTIVE" : "INACTIVE");
    }

    private boolean validateCoordinates(double latitude, double longitude) {
        // Latitude must be between -90 and 90
        if (Math.abs(latitude) > 90) {
            AlertUtil.showError("Invalid Latitude",
                    "Latitude must be between -90 and 90 degrees.\n" +
                            "You entered: " + latitude);
            return false;
        }

        // Longitude must be between -180 and 180
        if (Math.abs(longitude) > 180) {
            AlertUtil.showError("Invalid Longitude",
                    "Longitude must be between -180 and 180 degrees.\n" +
                            "You entered: " + longitude);
            return false;
        }

        return true;
    }

    private void handleAddZone() {
        if (!validateInputs()) {
            return;
        }

        try {
            double latitude = Double.parseDouble(latitudeField.getText());
            double longitude = Double.parseDouble(longitudeField.getText());

            // Validate coordinates before processing
            if (!validateCoordinates(latitude, longitude)) {
                return;
            }

            GeofenceZone zone = new GeofenceZone();
            zone.setZoneName(zoneNameField.getText().trim());
            zone.setCenterLat(latitude);
            zone.setCenterLng(longitude);
            zone.setRadiusMeters(Integer.parseInt(radiusField.getText()));
            zone.setZoneType(zoneTypeComboBox.getValue());
            zone.setActive("ACTIVE".equals(zoneStatusComboBox.getValue()));

            boolean success = zoneDAO.insert(zone);

            if (success) {
                AlertUtil.showSuccess("Geofence zone added successfully.");
                clearForm();
                loadZones();
            } else {
                AlertUtil.showError("Add Failed", "Failed to add geofence zone.");
            }

        } catch (NumberFormatException e) {
            AlertUtil.showError("Invalid Input", "Please enter valid numbers for coordinates and radius.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleUpdateZone() {
        if (selectedZone == null) {
            AlertUtil.showWarning("No Selection", "Please select a zone to update.");
            return;
        }

        if (!validateInputs()) {
            return;
        }

        try {
            double latitude = Double.parseDouble(latitudeField.getText());
            double longitude = Double.parseDouble(longitudeField.getText());

            // Validate coordinates before processing
            if (!validateCoordinates(latitude, longitude)) {
                return;
            }

            selectedZone.setZoneName(zoneNameField.getText().trim());
            selectedZone.setCenterLat(latitude);
            selectedZone.setCenterLng(longitude);
            selectedZone.setRadiusMeters(Integer.parseInt(radiusField.getText()));
            selectedZone.setZoneType(zoneTypeComboBox.getValue());
            selectedZone.setActive("ACTIVE".equals(zoneStatusComboBox.getValue()));

            boolean success = zoneDAO.update(selectedZone);

            if (success) {
                AlertUtil.showSuccess("Geofence zone updated successfully.");
                loadZones();
            } else {
                AlertUtil.showError("Update Failed", "Failed to update geofence zone.");
            }

        } catch (NumberFormatException e) {
            AlertUtil.showError("Invalid Input", "Please enter valid numbers.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleDeleteZone() {
        if (selectedZone == null) {
            AlertUtil.showWarning("No Selection", "Please select a zone to delete.");
            return;
        }

        boolean confirmed = AlertUtil.showConfirmation("Delete Zone",
                "Delete geofence zone '" + selectedZone.getZoneName() + "'?");

        if (confirmed) {
            try {
                boolean success = zoneDAO.delete(selectedZone.getId());

                if (success) {
                    AlertUtil.showSuccess("Geofence zone deleted successfully.");
                    clearForm();
                    loadZones();
                } else {
                    AlertUtil.showError("Delete Failed", "Failed to delete zone.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void clearForm() {
        zoneNameField.clear();
        latitudeField.clear();
        longitudeField.clear();
        radiusField.clear();
        zoneTypeComboBox.setValue(null);
        zoneStatusComboBox.setValue(null);
        selectedZone = null;
        zonesTable.getSelectionModel().clearSelection();
    }

    private boolean validateInputs() {
        if (!utils.ValidationUtil.isNotEmpty(zoneNameField.getText())) {
            AlertUtil.showWarning("Validation Error", "Zone name is required.");
            return false;
        }

        if (!utils.ValidationUtil.isNotEmpty(latitudeField.getText())) {
            AlertUtil.showWarning("Validation Error", "Latitude is required.");
            return false;
        }

        if (!utils.ValidationUtil.isNotEmpty(longitudeField.getText())) {
            AlertUtil.showWarning("Validation Error", "Longitude is required.");
            return false;
        }

        if (!utils.ValidationUtil.isNotEmpty(radiusField.getText())) {
            AlertUtil.showWarning("Validation Error", "Radius is required.");
            return false;
        }

        if (zoneTypeComboBox.getValue() == null) {
            AlertUtil.showWarning("Validation Error", "Please select a zone type.");
            return false;
        }

        if (zoneStatusComboBox.getValue() == null) {
            AlertUtil.showWarning("Validation Error", "Please select a zone status.");
            return false;
        }

        return true;
    }
}