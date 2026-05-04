package controllers;

import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Duration;
import utils.AlertUtil;
import utils.SceneManager;
import utils.ValidationUtil;
import dao.GeofenceZoneDAO;
import dao.GeofenceAlertEventDAO;
import models.GeofenceZone;
import models.GeofenceAlertEvent;
import java.util.List;

/**
 * Controller for Geofencing Management
 * Creates and manages geographic zones that trigger alerts when vehicles enter/exit
 * Used for high-crime areas, restricted zones, school zones, etc.
 */
public class GeofencingController {

    // ============================================
    // FXML UI COMPONENTS - ZONES TABLE
    // ============================================

    @FXML private TableView<GeofenceZone> zonesTable;
    @FXML private TableColumn<GeofenceZone, String> zoneNameColumn;
    @FXML private TableColumn<GeofenceZone, String> zoneTypeColumn;
    @FXML private TableColumn<GeofenceZone, Integer> radiusColumn;
    @FXML private TableColumn<GeofenceZone, String> zoneStatusColumn;

    // ============================================
    // ALERTS TABLE
    // ============================================

    @FXML private TableView<GeofenceAlertEvent> alertsTable;
    @FXML private TableColumn<GeofenceAlertEvent, String> alertZoneColumn;
    @FXML private TableColumn<GeofenceAlertEvent, String> alertVehicleColumn;
    @FXML private TableColumn<GeofenceAlertEvent, String> alertTypeColumn;
    @FXML private TableColumn<GeofenceAlertEvent, String> alertTimestampColumn;

    // ============================================
    // FORM COMPONENTS
    // ============================================

    @FXML private TextField zoneNameField;
    @FXML private TextField latitudeField;
    @FXML private TextField longitudeField;
    @FXML private TextField radiusField;
    @FXML private ComboBox<String> zoneTypeComboBox;
    @FXML private ComboBox<String> zoneStatusComboBox;

    // ============================================
    // BUTTONS
    // ============================================

    @FXML private Button addZoneButton;
    @FXML private Button updateZoneButton;
    @FXML private Button deleteZoneButton;
    @FXML private Button refreshButton;
    @FXML private Button backButton;

    // ============================================
    // PROGRESS INDICATORS
    // ============================================

    @FXML private ProgressIndicator loadProgress;
    @FXML private ProgressBar operationProgress;
    @FXML private Pagination zonesPagination;
    @FXML private Pagination alertsPagination;
    @FXML private Label statusLabel;

    // ============================================
    // DAO INSTANCES & DATA MODELS
    // ============================================

    private GeofenceZoneDAO zoneDAO;
    private GeofenceAlertEventDAO alertDAO;
    private GeofenceZone selectedZone;
    private List<GeofenceZone> fullZoneList;
    private List<GeofenceAlertEvent> fullAlertList;
    private int currentZonePage = 0;
    private int currentAlertPage = 0;
    private int pageSize = 20;

    // ============================================
    // INITIALIZATION METHODS
    // ============================================

    /**
     * Initializes the geofencing controller
     * Sets up DAOs, table columns, loads data, and configures UI
     */
    @FXML
    public void initialize() {
        zoneDAO = new GeofenceZoneDAO();
        alertDAO = new GeofenceAlertEventDAO();

        setupTableColumns();
        loadZones();
        loadAlerts();
        setupComboBoxes();
        setupButtonHandlers();
        setupTableSelection();
        setupPagination();

        statusLabel.setText("Ready");
    }

    /**
     * Configures table columns with cell value factories
     */
    private void setupTableColumns() {
        zoneNameColumn.setCellValueFactory(cellData -> cellData.getValue().zoneNameProperty());
        zoneTypeColumn.setCellValueFactory(cellData -> cellData.getValue().zoneTypeProperty());
        radiusColumn.setCellValueFactory(cellData -> cellData.getValue().radiusMetersProperty().asObject());
        zoneStatusColumn.setCellValueFactory(cellData -> cellData.getValue().activeProperty().asString());

        alertZoneColumn.setCellValueFactory(cellData -> cellData.getValue().zoneNameProperty());
        alertVehicleColumn.setCellValueFactory(cellData -> cellData.getValue().registrationNumberProperty());
        alertTypeColumn.setCellValueFactory(cellData -> cellData.getValue().alertTypeProperty());
        alertTimestampColumn.setCellValueFactory(cellData -> cellData.getValue().alertTimestampProperty().asString());
    }

    /**
     * Configures pagination for both zones and alerts tables
     */
    private void setupPagination() {
        if (zonesPagination != null) {
            zonesPagination.currentPageIndexProperty().addListener((obs, old, newPage) -> {
                currentZonePage = newPage.intValue();
                updateZonesPage();
            });
        }
        if (alertsPagination != null) {
            alertsPagination.currentPageIndexProperty().addListener((obs, old, newPage) -> {
                currentAlertPage = newPage.intValue();
                updateAlertsPage();
            });
        }
    }

    /**
     * Updates zones table to show current page
     */
    private void updateZonesPage() {
        if (fullZoneList == null || fullZoneList.isEmpty()) return;
        int start = currentZonePage * pageSize;
        int end = Math.min(start + pageSize, fullZoneList.size());
        if (start < fullZoneList.size()) {
            zonesTable.getItems().setAll(fullZoneList.subList(start, end));
        }
    }

    /**
     * Updates alerts table to show current page
     */
    private void updateAlertsPage() {
        if (fullAlertList == null || fullAlertList.isEmpty()) return;
        int start = currentAlertPage * pageSize;
        int end = Math.min(start + pageSize, fullAlertList.size());
        if (start < fullAlertList.size()) {
            alertsTable.getItems().setAll(fullAlertList.subList(start, end));
        }
    }

    /**
     * Loads all geofence zones from database
     */
    private void loadZones() {
        showProgress(true);
        statusLabel.setText("Loading geofence zones...");

        try {
            fullZoneList = zoneDAO.findAll();
            int totalPages = (int) Math.ceil((double) fullZoneList.size() / pageSize);
            if (zonesPagination != null) zonesPagination.setPageCount(Math.max(1, totalPages));
            updateZonesPage();
            statusLabel.setText("Loaded " + fullZoneList.size() + " zones");
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Load Failed", "Failed to load geofence zones.");
            statusLabel.setText("Error loading zones");
        } finally {
            hideProgressAfterDelay();
        }
    }

    /**
     * Loads all geofence alert events from database
     */
    private void loadAlerts() {
        try {
            fullAlertList = alertDAO.findAll();
            int totalPages = (int) Math.ceil((double) fullAlertList.size() / pageSize);
            if (alertsPagination != null) alertsPagination.setPageCount(Math.max(1, totalPages));
            updateAlertsPage();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Configures combo boxes with available options
     */
    private void setupComboBoxes() {
        zoneTypeComboBox.getItems().addAll("HIGH_CRIME", "RESTRICTED", "MONITORED", "SCHOOL_ZONE");
        zoneStatusComboBox.getItems().addAll("ACTIVE", "INACTIVE");
        zoneStatusComboBox.setValue("ACTIVE");
    }

    /**
     * Sets up button click handlers
     */
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

    /**
     * Sets up table selection listener for zones
     */
    private void setupTableSelection() {
        zonesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedZone = newSelection;
                displayZoneDetails(selectedZone);
            }
        });
    }

    /**
     * Displays selected zone details in the form
     * @param zone The geofence zone to display
     */
    private void displayZoneDetails(GeofenceZone zone) {
        zoneNameField.setText(zone.getZoneName());
        latitudeField.setText(String.valueOf(zone.getCenterLat()));
        longitudeField.setText(String.valueOf(zone.getCenterLng()));
        radiusField.setText(String.valueOf(zone.getRadiusMeters()));
        zoneTypeComboBox.setValue(zone.getZoneType());
        zoneStatusComboBox.setValue(zone.isActive() ? "ACTIVE" : "INACTIVE");
    }

    // ============================================
    // VALIDATION METHODS
    // ============================================

    /**
     * Validates geographic coordinates
     * @param latitude Latitude value (-90 to 90)
     * @param longitude Longitude value (-180 to 180)
     * @return true if coordinates are valid
     */
    private boolean validateCoordinates(double latitude, double longitude) {
        if (Math.abs(latitude) > 90) {
            AlertUtil.showError("Invalid Latitude", "Latitude must be between -90 and 90 degrees.\nYou entered: " + latitude);
            return false;
        }
        if (Math.abs(longitude) > 180) {
            AlertUtil.showError("Invalid Longitude", "Longitude must be between -180 and 180 degrees.\nYou entered: " + longitude);
            return false;
        }
        return true;
    }

    // ============================================
    // CRUD OPERATIONS
    // ============================================

    /**
     * Handles adding a new geofence zone
     */
    private void handleAddZone() {
        if (!validateInputs()) return;

        showProgress(true);
        statusLabel.setText("Adding geofence zone...");

        try {
            double latitude = Double.parseDouble(latitudeField.getText());
            double longitude = Double.parseDouble(longitudeField.getText());

            if (!validateCoordinates(latitude, longitude)) {
                hideProgressAfterDelay();
                return;
            }

            // Create and populate zone object
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
                statusLabel.setText("Zone added successfully");
            } else {
                AlertUtil.showError("Add Failed", "Failed to add geofence zone.");
                statusLabel.setText("Add failed");
            }

        } catch (NumberFormatException e) {
            AlertUtil.showError("Invalid Input", "Please enter valid numbers for coordinates and radius.");
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error: " + e.getMessage());
        } finally {
            hideProgressAfterDelay();
        }
    }

    /**
     * Handles updating an existing geofence zone
     */
    private void handleUpdateZone() {
        if (selectedZone == null) {
            AlertUtil.showWarning("No Selection", "Please select a zone to update.");
            return;
        }

        if (!validateInputs()) return;

        showProgress(true);
        statusLabel.setText("Updating geofence zone...");

        try {
            double latitude = Double.parseDouble(latitudeField.getText());
            double longitude = Double.parseDouble(longitudeField.getText());

            if (!validateCoordinates(latitude, longitude)) {
                hideProgressAfterDelay();
                return;
            }

            // Update zone object with form values
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
                statusLabel.setText("Zone updated successfully");
            } else {
                AlertUtil.showError("Update Failed", "Failed to update geofence zone.");
                statusLabel.setText("Update failed");
            }

        } catch (NumberFormatException e) {
            AlertUtil.showError("Invalid Input", "Please enter valid numbers.");
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error: " + e.getMessage());
        } finally {
            hideProgressAfterDelay();
        }
    }

    /**
     * Handles deleting a geofence zone
     */
    private void handleDeleteZone() {
        if (selectedZone == null) {
            AlertUtil.showWarning("No Selection", "Please select a zone to delete.");
            return;
        }

        boolean confirmed = AlertUtil.showConfirmation("Delete Zone",
                "Delete geofence zone '" + selectedZone.getZoneName() + "'?");

        if (confirmed) {
            showProgress(true);
            statusLabel.setText("Deleting geofence zone...");

            try {
                boolean success = zoneDAO.delete(selectedZone.getId());

                if (success) {
                    AlertUtil.showSuccess("Geofence zone deleted successfully.");
                    clearForm();
                    loadZones();
                    statusLabel.setText("Zone deleted successfully");
                } else {
                    AlertUtil.showError("Delete Failed", "Failed to delete zone.");
                    statusLabel.setText("Delete failed");
                }
            } catch (Exception e) {
                e.printStackTrace();
                statusLabel.setText("Error: " + e.getMessage());
            } finally {
                hideProgressAfterDelay();
            }
        }
    }

    /**
     * Clears all form fields and resets selection
     */
    private void clearForm() {
        zoneNameField.clear();
        latitudeField.clear();
        longitudeField.clear();
        radiusField.clear();
        zoneTypeComboBox.setValue(null);
        zoneStatusComboBox.setValue("ACTIVE");
        selectedZone = null;
        zonesTable.getSelectionModel().clearSelection();
    }

    /**
     * Validates all form inputs
     * @return true if all inputs are valid
     */
    private boolean validateInputs() {
        if (!ValidationUtil.isNotEmpty(zoneNameField.getText())) {
            AlertUtil.showWarning("Validation Error", "Zone name is required.");
            return false;
        }
        if (!ValidationUtil.isNotEmpty(latitudeField.getText())) {
            AlertUtil.showWarning("Validation Error", "Latitude is required.");
            return false;
        }
        if (!ValidationUtil.isNotEmpty(longitudeField.getText())) {
            AlertUtil.showWarning("Validation Error", "Longitude is required.");
            return false;
        }
        if (!ValidationUtil.isNotEmpty(radiusField.getText())) {
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

    // ============================================
    // UI PROGRESS METHODS
    // ============================================

    /**
     * Shows/hides progress indicators
     * @param show true to show, false to hide
     */
    private void showProgress(boolean show) {
        if (loadProgress != null) loadProgress.setVisible(show);
        if (operationProgress != null) {
            operationProgress.setVisible(show);
            operationProgress.setProgress(0);
        }
    }

    /**
     * Hides progress indicators after a short delay
     */
    private void hideProgressAfterDelay() {
        PauseTransition delay = new PauseTransition(Duration.seconds(1));
        delay.setOnFinished(event -> {
            if (loadProgress != null) loadProgress.setVisible(false);
            if (operationProgress != null) {
                operationProgress.setVisible(false);
                operationProgress.setProgress(0);
            }
        });
        delay.play();
    }
}