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
import utils.SessionManager;
import utils.ValidationUtil;
import utils.CurrencyUtil;
import dao.ServiceRecordDAO;
import dao.VehicleDAO;
import dao.MechanicDAO;
import dao.NotificationDAO;
import models.ServiceRecord;
import models.Vehicle;
import models.Mechanic;
import models.Notification;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller for Workshop Service Management
 * Handles service records for vehicles serviced at the workshop
 * Allows adding, updating, deleting, and viewing service history
 */
public class WorkshopServiceController {

    // ============================================
    // FXML UI COMPONENTS - TABLE
    // ============================================

    @FXML private TableView<ServiceRecord> servicesTable;
    @FXML private TableColumn<ServiceRecord, String> dateColumn;
    @FXML private TableColumn<ServiceRecord, String> vehicleColumn;
    @FXML private TableColumn<ServiceRecord, String> serviceTypeColumn;
    @FXML private TableColumn<ServiceRecord, Double> costColumn;
    @FXML private TableColumn<ServiceRecord, String> mechanicColumn;
    @FXML private TableColumn<ServiceRecord, String> statusColumn;

    // ============================================
    // FORM COMPONENTS
    // ============================================

    @FXML private ComboBox<Vehicle> vehicleComboBox;
    @FXML private ComboBox<Mechanic> mechanicComboBox;
    @FXML private TextField serviceTypeField;
    @FXML private TextField costField;
    @FXML private TextField odometerField;
    @FXML private DatePicker serviceDatePicker;
    @FXML private TextArea descriptionArea;
    @FXML private ComboBox<String> statusComboBox;

    // ============================================
    // STATISTICS LABELS
    // ============================================

    @FXML private Label totalServicesLabel;
    @FXML private Label totalRevenueLabel;
    @FXML private Label averageCostLabel;
    @FXML private Label pendingCountLabel;
    @FXML private Label completedCountLabel;
    @FXML private Label statusLabel;

    // ============================================
    // BUTTONS
    // ============================================

    @FXML private Button addButton;
    @FXML private Button updateButton;
    @FXML private Button deleteButton;
    @FXML private Button clearButton;
    @FXML private Button refreshButton;
    @FXML private Button backButton;
    @FXML private Button fadeButton;

    // ============================================
    // PROGRESS INDICATORS
    // ============================================

    @FXML private ProgressIndicator loadProgress;
    @FXML private ProgressBar operationProgress;
    @FXML private Pagination servicesPagination;

    // ============================================
    // DAO INSTANCES & DATA MODELS
    // ============================================

    private ServiceRecordDAO serviceDAO;
    private VehicleDAO vehicleDAO;
    private MechanicDAO mechanicDAO;
    private NotificationDAO notificationDAO;
    private ServiceRecord selectedRecord;
    private ObservableList<ServiceRecord> serviceList;
    private List<ServiceRecord> fullServiceData;
    private int workshopId;
    private int currentPage = 0;
    private int pageSize = 10;

    // ============================================
    // INITIALIZATION METHODS
    // ============================================

    /**
     * Initializes the workshop service controller
     * Sets up DAOs, table columns, loads data, and configures UI
     */
    @FXML
    public void initialize() {
        serviceDAO = new ServiceRecordDAO();
        vehicleDAO = new VehicleDAO();
        mechanicDAO = new MechanicDAO();
        notificationDAO = new NotificationDAO();
        serviceList = FXCollections.observableArrayList();

        workshopId = SessionManager.getInstance().getWorkshopId();

        setupTableColumns();
        loadComboBoxes();
        loadServices();
        setupComboBoxes();
        setupButtonHandlers();
        setupTableSelection();
        setupPagination();
        applyVisualEffects();
        loadStatistics();

        serviceDatePicker.setValue(LocalDate.now());
        statusComboBox.setValue("PENDING");
        statusLabel.setText("Ready");
    }

    /**
     * Configures table columns with cell value factories
     */
    private void setupTableColumns() {
        dateColumn.setCellValueFactory(cellData -> cellData.getValue().serviceDateProperty().asString());
        vehicleColumn.setCellValueFactory(cellData -> cellData.getValue().registrationNumberProperty());
        serviceTypeColumn.setCellValueFactory(cellData -> cellData.getValue().serviceTypeProperty());
        costColumn.setCellValueFactory(cellData -> cellData.getValue().costProperty().asObject());
        mechanicColumn.setCellValueFactory(cellData -> cellData.getValue().mechanicNameProperty());
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());

        dateColumn.setStyle("-fx-alignment: CENTER;");
        vehicleColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        serviceTypeColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        costColumn.setStyle("-fx-alignment: CENTER-RIGHT;");
        mechanicColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        statusColumn.setStyle("-fx-alignment: CENTER;");
    }

    /**
     * Configures pagination for the services table
     */
    private void setupPagination() {
        if (servicesPagination != null) {
            servicesPagination.setPageCount(1);
            servicesPagination.setMaxPageIndicatorCount(5);
            servicesPagination.currentPageIndexProperty().addListener((obs, old, newPage) -> {
                currentPage = newPage.intValue();
                updateTablePage();
            });
        }
    }

    /**
     * Updates the table to show current page of services
     */
    private void updateTablePage() {
        if (fullServiceData == null || fullServiceData.isEmpty()) return;
        int start = currentPage * pageSize;
        int end = Math.min(start + pageSize, fullServiceData.size());
        if (start < fullServiceData.size()) {
            serviceList.setAll(fullServiceData.subList(start, end));
            servicesTable.setItems(serviceList);
        }
    }

    /**
     * Loads vehicles and mechanics into combo boxes
     */
    private void loadComboBoxes() {
        showProgress(true);
        try {
            // Load all vehicles (for workshop to select)
            List<Vehicle> vehicles = vehicleDAO.findAll();
            vehicleComboBox.getItems().setAll(vehicles);

            // Load mechanics for this workshop
            List<Mechanic> mechanics = mechanicDAO.findByWorkshopId(workshopId);
            mechanicComboBox.getItems().setAll(mechanics);

            statusLabel.setText("Loaded " + vehicles.size() + " vehicles and " + mechanics.size() + " mechanics");
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error loading combo boxes: " + e.getMessage());
        } finally {
            showProgress(false);
        }
    }

    /**
     * Sets up combo box options
     */
    private void setupComboBoxes() {
        statusComboBox.getItems().addAll("PENDING", "IN_PROGRESS", "COMPLETED", "CANCELLED");
        statusComboBox.setValue("PENDING");
    }

    /**
     * Loads all service records for this workshop from database
     */
    private void loadServices() {
        showProgress(true);
        statusLabel.setText("Loading service records...");

        try {
            fullServiceData = serviceDAO.findByWorkshopId(workshopId);
            int totalPages = (int) Math.ceil((double) fullServiceData.size() / pageSize);
            if (servicesPagination != null) servicesPagination.setPageCount(Math.max(1, totalPages));
            updateTablePage();
            statusLabel.setText("Loaded " + fullServiceData.size() + " service records");
            loadStatistics();
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error loading services: " + e.getMessage());
            AlertUtil.showError("Load Failed", "Failed to load service records: " + e.getMessage());
        } finally {
            hideProgressAfterDelay();
        }
    }

    /**
     * Loads statistics for the workshop dashboard
     */
    private void loadStatistics() {
        try {
            int totalServices = serviceDAO.countByWorkshopId(workshopId);
            totalServicesLabel.setText(String.valueOf(totalServices));

            double totalRevenue = serviceDAO.sumRevenueByWorkshopId(workshopId);
            totalRevenueLabel.setText(CurrencyUtil.format(totalRevenue));

            double avgCost = serviceDAO.averageCostByWorkshopId(workshopId);
            averageCostLabel.setText(CurrencyUtil.format(avgCost));

            // Count by status (simplified - you may want to add these methods to DAO)
            List<ServiceRecord> allServices = serviceDAO.findByWorkshopId(workshopId);
            long pendingCount = allServices.stream().filter(s -> "PENDING".equals(s.getStatus())).count();
            long completedCount = allServices.stream().filter(s -> "COMPLETED".equals(s.getStatus())).count();

            pendingCountLabel.setText(String.valueOf(pendingCount));
            completedCountLabel.setText(String.valueOf(completedCount));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets up button click handlers
     */
    private void setupButtonHandlers() {
        addButton.setOnAction(event -> handleAdd());
        updateButton.setOnAction(event -> handleUpdate());
        deleteButton.setOnAction(event -> handleDelete());
        clearButton.setOnAction(event -> handleClear());
        refreshButton.setOnAction(event -> {
            loadServices();
            loadComboBoxes();
        });
        backButton.setOnAction(event -> handleBack());

        if (fadeButton != null) {
            fadeButton.setOnAction(event -> showFadeAnimation());
        }
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

    /**
     * Applies visual effects to buttons
     */
    private void applyVisualEffects() {
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(5.0);
        dropShadow.setOffsetX(2.0);
        dropShadow.setOffsetY(2.0);
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.3));

        addButton.setEffect(dropShadow);
        updateButton.setEffect(dropShadow);
        deleteButton.setEffect(dropShadow);
        clearButton.setEffect(dropShadow);
        refreshButton.setEffect(dropShadow);
        backButton.setEffect(dropShadow);

        if (fadeButton != null) fadeButton.setEffect(dropShadow);
    }

    /**
     * Sets up table selection listener to populate form when row is selected
     */
    private void setupTableSelection() {
        servicesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedRecord = newSelection;
                displayServiceDetails(selectedRecord);
            }
        });
    }

    /**
     * Displays selected service record details in the form
     * @param record The service record to display
     */
    private void displayServiceDetails(ServiceRecord record) {
        try {
            Vehicle vehicle = vehicleDAO.findById(record.getVehicleId());
            vehicleComboBox.getSelectionModel().select(vehicle);

            if (record.getMechanicId() > 0) {
                Mechanic mechanic = mechanicDAO.findById(record.getMechanicId());
                mechanicComboBox.getSelectionModel().select(mechanic);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        serviceTypeField.setText(record.getServiceType());
        costField.setText(String.valueOf(record.getCost()));
        if (record.getOdometerReading() > 0) {
            odometerField.setText(String.valueOf(record.getOdometerReading()));
        }
        serviceDatePicker.setValue(record.getServiceDate());
        descriptionArea.setText(record.getDescription());
        statusComboBox.setValue(record.getStatus());
    }

    /**
     * Clears all form fields
     */
    private void handleClear() {
        vehicleComboBox.getSelectionModel().clearSelection();
        mechanicComboBox.getSelectionModel().clearSelection();
        serviceTypeField.clear();
        costField.clear();
        odometerField.clear();
        serviceDatePicker.setValue(LocalDate.now());
        descriptionArea.clear();
        statusComboBox.setValue("PENDING");
        selectedRecord = null;
        servicesTable.getSelectionModel().clearSelection();
        statusLabel.setText("Form cleared");
    }

    // ============================================
    // CRUD OPERATIONS
    // ============================================

    /**
     * Handles adding a new service record
     */
    private void handleAdd() {
        if (!validateInputs()) {
            return;
        }

        Vehicle selectedVehicle = vehicleComboBox.getSelectionModel().getSelectedItem();
        if (selectedVehicle == null) {
            AlertUtil.showWarning("Validation Error", "Please select a vehicle.");
            return;
        }

        showProgress(true);
        statusLabel.setText("Adding service record...");
        updateProgress(0.3);

        try {
            ServiceRecord record = new ServiceRecord();
            record.setVehicleId(selectedVehicle.getId());
            record.setWorkshopId(workshopId);

            Mechanic selectedMechanic = mechanicComboBox.getSelectionModel().getSelectedItem();
            if (selectedMechanic != null) {
                record.setMechanicId(selectedMechanic.getId());
            }

            record.setServiceDate(serviceDatePicker.getValue());
            record.setServiceType(serviceTypeField.getText().trim());
            record.setDescription(descriptionArea.getText().trim());
            record.setCost(Double.parseDouble(costField.getText()));
            record.setStatus(statusComboBox.getValue());

            if (ValidationUtil.isNotEmpty(odometerField.getText())) {
                record.setOdometerReading(Integer.parseInt(odometerField.getText()));
            }

            updateProgress(0.6);
            boolean success = serviceDAO.insert(record);

            if (success) {
                updateProgress(1.0);
                AlertUtil.showSuccess("Service record added successfully.");

                // Send notification to customer
                sendServiceNotification(selectedVehicle, record);

                handleClear();
                loadServices();
                statusLabel.setText("Service record added successfully");
            } else {
                AlertUtil.showError("Add Failed", "Failed to add service record.");
                statusLabel.setText("Add failed");
            }

        } catch (NumberFormatException e) {
            AlertUtil.showError("Invalid Input", "Please enter valid cost and odometer values.");
            statusLabel.setText("Invalid input");
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error: " + e.getMessage());
            AlertUtil.showError("Database Error", "An error occurred while adding service record.");
        } finally {
            hideProgressAfterDelay();
        }
    }

    /**
     * Sends notification to vehicle owner about service record
     * @param vehicle The vehicle being serviced
     * @param record The service record
     */
    private void sendServiceNotification(Vehicle vehicle, ServiceRecord record) {
        try {
            // Get customer ID from vehicle owner
            int customerId = vehicle.getOwnerId();

            // Get user ID from customer (you may need to add this method)
            // For now, we'll use a generic approach
            String message = "Your vehicle " + vehicle.getRegistrationNumber() +
                    " has been scheduled for " + record.getServiceType() +
                    " service on " + record.getServiceDate() +
                    ". Estimated cost: " + CurrencyUtil.format(record.getCost());

            // Send notification - you may need to adjust based on your NotificationDAO
            // notificationDAO.sendToCustomer(customerId, "SERVICE_SCHEDULED", message);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles updating an existing service record
     */
    private void handleUpdate() {
        if (selectedRecord == null) {
            AlertUtil.showWarning("No Selection", "Please select a service record to update.");
            return;
        }

        if (!validateInputs()) {
            return;
        }

        Vehicle selectedVehicle = vehicleComboBox.getSelectionModel().getSelectedItem();
        if (selectedVehicle == null) {
            AlertUtil.showWarning("Validation Error", "Please select a vehicle.");
            return;
        }

        showProgress(true);
        statusLabel.setText("Updating service record...");
        updateProgress(0.3);

        try {
            selectedRecord.setVehicleId(selectedVehicle.getId());

            Mechanic selectedMechanic = mechanicComboBox.getSelectionModel().getSelectedItem();
            if (selectedMechanic != null) {
                selectedRecord.setMechanicId(selectedMechanic.getId());
            }

            selectedRecord.setServiceDate(serviceDatePicker.getValue());
            selectedRecord.setServiceType(serviceTypeField.getText().trim());
            selectedRecord.setDescription(descriptionArea.getText().trim());
            selectedRecord.setCost(Double.parseDouble(costField.getText()));
            selectedRecord.setStatus(statusComboBox.getValue());

            if (ValidationUtil.isNotEmpty(odometerField.getText())) {
                selectedRecord.setOdometerReading(Integer.parseInt(odometerField.getText()));
            }

            updateProgress(0.6);
            boolean success = serviceDAO.update(selectedRecord);

            if (success) {
                updateProgress(1.0);
                AlertUtil.showSuccess("Service record updated successfully.");
                loadServices();
                statusLabel.setText("Service record updated");
            } else {
                AlertUtil.showError("Update Failed", "Failed to update service record.");
                statusLabel.setText("Update failed");
            }

        } catch (NumberFormatException e) {
            AlertUtil.showError("Invalid Input", "Please enter valid cost and odometer values.");
            statusLabel.setText("Invalid input");
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error: " + e.getMessage());
            AlertUtil.showError("Database Error", "An error occurred while updating service record.");
        } finally {
            hideProgressAfterDelay();
        }
    }

    /**
     * Handles deleting a service record
     */
    private void handleDelete() {
        if (selectedRecord == null) {
            AlertUtil.showWarning("No Selection", "Please select a service record to delete.");
            return;
        }

        boolean confirmed = AlertUtil.showConfirmation("Delete Service Record",
                "Are you sure you want to delete this service record?\n\n" +
                        "Vehicle: " + selectedRecord.getRegistrationNumber() + "\n" +
                        "Service Type: " + selectedRecord.getServiceType() + "\n" +
                        "Date: " + selectedRecord.getServiceDate() + "\n\n" +
                        "This action cannot be undone.");

        if (confirmed) {
            showProgress(true);
            statusLabel.setText("Deleting service record...");
            updateProgress(0.5);

            try {
                boolean success = serviceDAO.delete(selectedRecord.getId());

                if (success) {
                    updateProgress(1.0);
                    AlertUtil.showSuccess("Service record deleted successfully.");
                    handleClear();
                    loadServices();
                    statusLabel.setText("Service record deleted");
                } else {
                    AlertUtil.showError("Delete Failed", "Failed to delete service record.");
                    statusLabel.setText("Delete failed");
                }
            } catch (Exception e) {
                e.printStackTrace();
                statusLabel.setText("Error: " + e.getMessage());
                AlertUtil.showError("Database Error", "An error occurred while deleting service record.");
            } finally {
                hideProgressAfterDelay();
            }
        }
    }

    /**
     * Handles back button navigation
     */
    private void handleBack() {
        String role = SessionManager.getInstance().getUserRole();
        if ("WORKSHOP".equals(role)) {
            SceneManager.getInstance().switchToWorkshopView();
        } else {
            SceneManager.getInstance().switchToWorkshopProfileView();
        }
    }

    // ============================================
    // VALIDATION METHODS
    // ============================================

    /**
     * Validates all form inputs before saving
     * @return true if all inputs are valid
     */
    private boolean validateInputs() {
        if (!ValidationUtil.isNotEmpty(serviceTypeField.getText())) {
            AlertUtil.showWarning("Validation Error", "Service type is required.");
            serviceTypeField.requestFocus();
            return false;
        }

        if (!ValidationUtil.isNotEmpty(costField.getText())) {
            AlertUtil.showWarning("Validation Error", "Cost is required.");
            costField.requestFocus();
            return false;
        }

        try {
            double cost = Double.parseDouble(costField.getText());
            if (cost < 0) {
                AlertUtil.showWarning("Validation Error", "Cost cannot be negative.");
                costField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            AlertUtil.showWarning("Validation Error", "Please enter a valid cost.");
            costField.requestFocus();
            return false;
        }

        if (serviceDatePicker.getValue() == null) {
            AlertUtil.showWarning("Validation Error", "Service date is required.");
            return false;
        }

        if (statusComboBox.getValue() == null) {
            AlertUtil.showWarning("Validation Error", "Please select a status.");
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
            if (loadProgress != null) {
                loadProgress.setVisible(false);
            }
        });
        delay.play();
    }
}