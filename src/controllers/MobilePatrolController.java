package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import utils.AlertUtil;
import utils.SceneManager;
import utils.SessionManager;
import dao.PoliceUnitDAO;
import dao.MobilePatrolSyncDAO;
import dao.StolenVehicleDAO;
import dao.BOLOAlertDAO;
import models.PoliceUnit;
import models.StolenVehicle;
import models.BOLOAlert;

import java.util.List;

/**
 * Controller for Mobile Patrol Integration
 * Coordinates and tracks mobile police patrol units in real-time
 * Provides location updates, BOLO alerts, and stolen vehicle detection
 */
public class MobilePatrolController {

    // ============================================
    // FXML UI COMPONENTS
    // ============================================

    // Selection and Control Components
    @FXML private ComboBox<PoliceUnit> unitComboBox;
    @FXML private Button syncButton;
    @FXML private Button locateButton;
    @FXML private Button alertButton;
    @FXML private Button refreshButton;
    @FXML private Button backButton;

    // Status Display Labels
    @FXML private Label unitStatusLabel;
    @FXML private Label unitLocationLabel;
    @FXML private Label lastSyncLabel;

    // Tables and Lists
    @FXML private TableView<StolenVehicle> nearbyStolenTable;
    @FXML private TableColumn<StolenVehicle, String> regColumn;
    @FXML private TableColumn<StolenVehicle, String> makeColumn;
    @FXML private TableColumn<StolenVehicle, String> modelColumn;
    @FXML private TableColumn<StolenVehicle, String> distanceColumn;

    @FXML private ListView<String> boloAlertsList;
    @FXML private ListView<String> pendingSyncList;

    // Location Update Fields
    @FXML private TextField latitudeField;
    @FXML private TextField longitudeField;
    @FXML private Button updateLocationButton;

    // ============================================
    // DAO INSTANCES
    // ============================================

    private PoliceUnitDAO unitDAO;
    private MobilePatrolSyncDAO syncDAO;
    private StolenVehicleDAO stolenDAO;
    private BOLOAlertDAO boloDAO;

    // ============================================
    // INITIALIZATION METHODS
    // ============================================

    @FXML
    public void initialize() {
        unitDAO = new PoliceUnitDAO();
        syncDAO = new MobilePatrolSyncDAO();
        stolenDAO = new StolenVehicleDAO();
        boloDAO = new BOLOAlertDAO();

        setupTableColumns();
        loadUnits();
        setupButtonHandlers();
    }

    private void setupTableColumns() {
        regColumn.setCellValueFactory(cellData -> cellData.getValue().registrationNumberProperty());
        makeColumn.setCellValueFactory(cellData -> cellData.getValue().makeProperty());
        modelColumn.setCellValueFactory(cellData -> cellData.getValue().modelProperty());
        distanceColumn.setCellValueFactory(cellData -> {
            Double distance = cellData.getValue().getDistance();
            return new javafx.beans.property.SimpleStringProperty(
                    distance != null ? String.format("%.2f km", distance) : "N/A");
        });
    }

    private void loadUnits() {
        try {
            // PoliceUnitDAO has findAvailableUnits() method - confirmed
            List<PoliceUnit> units = unitDAO.findAvailableUnits();
            unitComboBox.getItems().setAll(units);

            if (!units.isEmpty()) {
                unitComboBox.getSelectionModel().selectFirst();
                loadUnitData();
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Load Failed", "Failed to load police units: " + e.getMessage());
        }
    }

    private void setupButtonHandlers() {
        syncButton.setOnAction(event -> handleSync());
        locateButton.setOnAction(event -> handleLocate());
        alertButton.setOnAction(event -> handleSendAlert());
        refreshButton.setOnAction(event -> refreshData());
        backButton.setOnAction(event -> SceneManager.getInstance().switchToPoliceView());
        updateLocationButton.setOnAction(event -> handleUpdateLocation());

        unitComboBox.setOnAction(event -> loadUnitData());
    }

    // ============================================
    // UNIT DATA LOADING METHODS
    // ============================================

    private void loadUnitData() {
        PoliceUnit selectedUnit = unitComboBox.getSelectionModel().getSelectedItem();
        if (selectedUnit != null) {
            // Display unit status
            unitStatusLabel.setText("Status: " + selectedUnit.getStatus());

            // Display location if available
            if (selectedUnit.getCurrentLocationLat() != null && selectedUnit.getCurrentLocationLat() != 0) {
                unitLocationLabel.setText(String.format("Location: %.6f, %.6f",
                        selectedUnit.getCurrentLocationLat(), selectedUnit.getCurrentLocationLng()));
            } else {
                unitLocationLabel.setText("Location: Not available");
            }

            // Display last sync time
            lastSyncLabel.setText("Last Update: " +
                    (selectedUnit.getLastLocationUpdate() != null ? selectedUnit.getLastLocationUpdate().toString() : "Never"));

            // Load related data
            loadNearbyStolen(selectedUnit);
            loadBOLOAlerts();
            loadPendingSync(selectedUnit);
        }
    }

    private void loadNearbyStolen(PoliceUnit unit) {
        try {
            nearbyStolenTable.getItems().clear();

            if (unit.getCurrentLocationLat() != null && unit.getCurrentLocationLng() != null &&
                    unit.getCurrentLocationLat() != 0 && unit.getCurrentLocationLng() != 0) {

                // Find stolen vehicles within 10km radius
                List<StolenVehicle> nearbyStolen = stolenDAO.findNearbyStolen(
                        unit.getCurrentLocationLat(), unit.getCurrentLocationLng(), 10.0);

                if (nearbyStolen != null && !nearbyStolen.isEmpty()) {
                    for (StolenVehicle vehicle : nearbyStolen) {
                        double distance = calculateDistance(
                                unit.getCurrentLocationLat(), unit.getCurrentLocationLng(),
                                vehicle.getLatitude(), vehicle.getLongitude()
                        );
                        vehicle.setDistance(distance);
                    }
                    nearbyStolenTable.getItems().setAll(nearbyStolen);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private double calculateDistance(Double lat1, Double lng1, Double lat2, Double lng2) {
        if (lat1 == null || lng1 == null || lat2 == null || lng2 == null || lat1 == 0 || lng1 == 0) {
            return 999.0;
        }
        double earthRadius = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }

    private void loadBOLOAlerts() {
        try {
            List<BOLOAlert> alerts = boloDAO.findActiveAlerts();
            boloAlertsList.getItems().clear();

            if (alerts != null && !alerts.isEmpty()) {
                for (BOLOAlert alert : alerts) {
                    String alertText = "[" + alert.getPriority() + "] " +
                            alert.getRegistrationNumber() + " - " +
                            alert.getMessage();
                    boloAlertsList.getItems().add(alertText);
                }
            } else {
                boloAlertsList.getItems().add("No active BOLO alerts");
            }

        } catch (Exception e) {
            e.printStackTrace();
            boloAlertsList.getItems().clear();
            boloAlertsList.getItems().add("Failed to load BOLO alerts");
        }
    }

    private void loadPendingSync(PoliceUnit unit) {
        try {
            List<String> pendingItems = syncDAO.getPendingSyncItems(unit.getUnitId());
            pendingSyncList.getItems().clear();

            if (pendingItems != null && !pendingItems.isEmpty()) {
                pendingSyncList.getItems().addAll(pendingItems);
            } else {
                pendingSyncList.getItems().add("No pending sync items");
            }

        } catch (Exception e) {
            e.printStackTrace();
            pendingSyncList.getItems().clear();
            pendingSyncList.getItems().add("Failed to load pending sync data");
        }
    }

    // ============================================
    // REFRESH AND SYNC METHODS
    // ============================================

    private void refreshData() {
        loadUnits();
        PoliceUnit selected = unitComboBox.getSelectionModel().getSelectedItem();
        if (selected != null) {
            loadUnitData();
        }
        AlertUtil.showSuccess("Data Refreshed", "Mobile patrol data has been refreshed.");
    }

    private void handleSync() {
        PoliceUnit selectedUnit = unitComboBox.getSelectionModel().getSelectedItem();
        if (selectedUnit == null) {
            AlertUtil.showWarning("No Selection", "Please select a police unit.");
            return;
        }

        try {
            boolean success = syncDAO.syncUnitData(selectedUnit.getUnitId());

            if (success) {
                AlertUtil.showSuccess("Sync Completed", "Data synchronized successfully.");
                refreshData();
            } else {
                AlertUtil.showError("Sync Failed", "Failed to synchronize data.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Sync Error", "An error occurred during synchronization: " + e.getMessage());
        }
    }

    // ============================================
    // LOCATION AND ALERT METHODS
    // ============================================

    private void handleLocate() {
        PoliceUnit selectedUnit = unitComboBox.getSelectionModel().getSelectedItem();
        if (selectedUnit == null) {
            AlertUtil.showWarning("No Selection", "Please select a police unit.");
            return;
        }

        String locationInfo = "Unit: " + selectedUnit.getUnitId() + "\n" +
                "Officer: " + selectedUnit.getOfficerName() + "\n" +
                "Badge: " + selectedUnit.getBadgeNumber() + "\n" +
                "Status: " + selectedUnit.getStatus() + "\n" +
                "Location: " + unitLocationLabel.getText();

        AlertUtil.showInfo("Unit Location", locationInfo);
    }

    private void handleSendAlert() {
        PoliceUnit selectedUnit = unitComboBox.getSelectionModel().getSelectedItem();
        if (selectedUnit == null) {
            AlertUtil.showWarning("No Selection", "Please select a police unit.");
            return;
        }

        String alertMessage = "BROADCAST ALERT from Unit " + selectedUnit.getUnitId() +
                ":\nOfficer: " + selectedUnit.getOfficerName() +
                "\nLocation: " + unitLocationLabel.getText();

        boolean confirmed = AlertUtil.showConfirmation("Send Broadcast",
                "Send alert to all mobile units?\n\n" + alertMessage);

        if (confirmed) {
            try {
                boolean success = syncDAO.sendBroadcastAlert(selectedUnit.getUnitId(), alertMessage);

                if (success) {
                    AlertUtil.showSuccess("Alert Sent", "Broadcast alert sent to all units.");
                } else {
                    AlertUtil.showError("Send Failed", "Failed to send broadcast alert.");
                }

            } catch (Exception e) {
                e.printStackTrace();
                AlertUtil.showError("Alert Error", "An error occurred while sending alert: " + e.getMessage());
            }
        }
    }

    private void handleUpdateLocation() {
        PoliceUnit selectedUnit = unitComboBox.getSelectionModel().getSelectedItem();
        if (selectedUnit == null) {
            AlertUtil.showWarning("No Selection", "Please select a police unit.");
            return;
        }

        String latText = latitudeField.getText().trim();
        String lngText = longitudeField.getText().trim();

        if (latText.isEmpty() || lngText.isEmpty()) {
            AlertUtil.showWarning("Invalid Input", "Please enter both latitude and longitude.");
            return;
        }

        try {
            double lat = Double.parseDouble(latText);
            double lng = Double.parseDouble(lngText);

            // Validate coordinate ranges
            if (lat < -90 || lat > 90) {
                AlertUtil.showWarning("Invalid Latitude", "Latitude must be between -90 and 90.");
                return;
            }

            if (lng < -180 || lng > 180) {
                AlertUtil.showWarning("Invalid Longitude", "Longitude must be between -180 and 180.");
                return;
            }

            boolean success = syncDAO.syncPoliceUnitLocation(selectedUnit.getUnitId(), lat, lng);

            if (success) {
                AlertUtil.showSuccess("Location Updated", "Police unit location updated successfully.");
                loadUnits();
                loadUnitData();
                latitudeField.clear();
                longitudeField.clear();
            } else {
                AlertUtil.showError("Update Failed", "Failed to update location.");
            }

        } catch (NumberFormatException e) {
            AlertUtil.showError("Invalid Input", "Please enter valid numeric coordinates.");
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Update Error", "An error occurred while updating location: " + e.getMessage());
        }
    }
}