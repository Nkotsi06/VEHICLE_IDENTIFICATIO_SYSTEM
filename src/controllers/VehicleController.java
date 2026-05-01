package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.control.DatePicker;
import utils.AlertUtil;
import utils.SceneManager;
import utils.ValidationUtil;
import dao.VehicleDAO;
import dao.CustomerDAO;
import dao.VehicleStatusDAO;
import models.Vehicle;
import models.Customer;
import models.VehicleStatus;

public class VehicleController {

    @FXML private TableView<Vehicle> vehiclesTable;
    @FXML private TableColumn<Vehicle, String> regNumberColumn;
    @FXML private TableColumn<Vehicle, String> makeColumn;
    @FXML private TableColumn<Vehicle, String> modelColumn;
    @FXML private TableColumn<Vehicle, Integer> yearColumn;
    @FXML private TableColumn<Vehicle, String> statusColumn;
    @FXML private TableColumn<Vehicle, String> ownerColumn;

    @FXML private TextField registrationNumberField;
    @FXML private TextField makeField;
    @FXML private TextField modelField;
    @FXML private TextField yearField;
    @FXML private TextField colorField;
    @FXML private TextField engineNumberField;
    @FXML private TextField chassisNumberField;
    @FXML private ComboBox<Customer> ownerComboBox;
    @FXML private ComboBox<VehicleStatus> statusComboBox;
    @FXML private TextArea descriptionArea;

    @FXML private Button addButton;
    @FXML private Button updateButton;
    @FXML private Button deleteButton;
    @FXML private Button clearButton;
    @FXML private Button refreshButton;
    @FXML private Button viewHistoryButton;

    private VehicleDAO vehicleDAO;
    private CustomerDAO customerDAO;
    private VehicleStatusDAO statusDAO;
    private Vehicle selectedVehicle;

    @FXML
    public void initialize() {
        vehicleDAO = new VehicleDAO();
        customerDAO = new CustomerDAO();
        statusDAO = new VehicleStatusDAO();

        setupTableColumns();
        loadComboBoxes();
        loadVehicles();
        setupButtonHandlers();
        setupTableSelection();
    }

    private void setupTableColumns() {
        regNumberColumn.setCellValueFactory(cellData -> cellData.getValue().registrationNumberProperty());
        makeColumn.setCellValueFactory(cellData -> cellData.getValue().makeProperty());
        modelColumn.setCellValueFactory(cellData -> cellData.getValue().modelProperty());
        yearColumn.setCellValueFactory(cellData -> cellData.getValue().yearProperty().asObject());
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusNameProperty());
        ownerColumn.setCellValueFactory(cellData -> cellData.getValue().ownerNameProperty());
    }

    private void loadComboBoxes() {
        try {
            java.util.List<Customer> customers = customerDAO.findAll();
            ownerComboBox.getItems().setAll(customers);

            java.util.List<VehicleStatus> statuses = statusDAO.findAll();
            statusComboBox.getItems().setAll(statuses);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadVehicles() {
        try {
            java.util.List<Vehicle> vehicles = vehicleDAO.findAll();
            vehiclesTable.getItems().setAll(vehicles);
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Load Failed", "Failed to load vehicles.");
        }
    }

    private void setupButtonHandlers() {
        addButton.setOnAction(event -> handleAdd());
        updateButton.setOnAction(event -> handleUpdate());
        deleteButton.setOnAction(event -> handleDelete());
        clearButton.setOnAction(event -> handleClear());
        refreshButton.setOnAction(event -> loadVehicles());
        viewHistoryButton.setOnAction(event -> handleViewHistory());
    }

    private void setupTableSelection() {
        vehiclesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedVehicle = newSelection;
                displayVehicleDetails(selectedVehicle);
            }
        });
    }

    private void displayVehicleDetails(Vehicle vehicle) {
        registrationNumberField.setText(vehicle.getRegistrationNumber());
        makeField.setText(vehicle.getMake());
        modelField.setText(vehicle.getModel());
        yearField.setText(String.valueOf(vehicle.getYear()));
        colorField.setText(vehicle.getColor());
        engineNumberField.setText(vehicle.getEngineNumber());
        chassisNumberField.setText(vehicle.getChassisNumber());

        if (vehicle.getOwnerId() > 0) {
            try {
                Customer owner = customerDAO.findById(vehicle.getOwnerId());
                ownerComboBox.getSelectionModel().select(owner);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (vehicle.getStatusId() > 0) {
            try {
                VehicleStatus status = statusDAO.findById(vehicle.getStatusId());
                statusComboBox.getSelectionModel().select(status);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void handleAdd() {
        if (!validateInputs()) {
            return;
        }

        try {
            Vehicle vehicle = new Vehicle();
            vehicle.setRegistrationNumber(registrationNumberField.getText().trim().toUpperCase());
            vehicle.setMake(makeField.getText().trim());
            vehicle.setModel(modelField.getText().trim());
            vehicle.setYear(Integer.parseInt(yearField.getText().trim()));
            vehicle.setColor(colorField.getText().trim());
            vehicle.setEngineNumber(engineNumberField.getText().trim());
            vehicle.setChassisNumber(chassisNumberField.getText().trim());

            Customer selectedOwner = ownerComboBox.getSelectionModel().getSelectedItem();
            if (selectedOwner != null) {
                vehicle.setOwnerId(selectedOwner.getId());
            }

            VehicleStatus selectedStatus = statusComboBox.getSelectionModel().getSelectedItem();
            if (selectedStatus != null) {
                vehicle.setStatusId(selectedStatus.getId());
            }

            boolean success = vehicleDAO.insert(vehicle);

            if (success) {
                AlertUtil.showSuccess("Vehicle added successfully.");
                handleClear();
                loadVehicles();
            } else {
                AlertUtil.showError("Add Failed", "Failed to add vehicle.");
            }

        } catch (NumberFormatException e) {
            AlertUtil.showError("Invalid Input", "Please enter a valid year.");
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Database Error", "An error occurred while adding the vehicle.");
        }
    }

    private void handleUpdate() {
        if (selectedVehicle == null) {
            AlertUtil.showWarning("No Selection", "Please select a vehicle to update.");
            return;
        }

        if (!validateInputs()) {
            return;
        }

        try {
            selectedVehicle.setRegistrationNumber(registrationNumberField.getText().trim().toUpperCase());
            selectedVehicle.setMake(makeField.getText().trim());
            selectedVehicle.setModel(modelField.getText().trim());
            selectedVehicle.setYear(Integer.parseInt(yearField.getText().trim()));
            selectedVehicle.setColor(colorField.getText().trim());
            selectedVehicle.setEngineNumber(engineNumberField.getText().trim());
            selectedVehicle.setChassisNumber(chassisNumberField.getText().trim());

            Customer selectedOwner = ownerComboBox.getSelectionModel().getSelectedItem();
            if (selectedOwner != null) {
                selectedVehicle.setOwnerId(selectedOwner.getId());
            }

            VehicleStatus selectedStatus = statusComboBox.getSelectionModel().getSelectedItem();
            if (selectedStatus != null) {
                selectedVehicle.setStatusId(selectedStatus.getId());
            }

            boolean success = vehicleDAO.update(selectedVehicle);

            if (success) {
                AlertUtil.showSuccess("Vehicle updated successfully.");
                loadVehicles();
            } else {
                AlertUtil.showError("Update Failed", "Failed to update vehicle.");
            }

        } catch (NumberFormatException e) {
            AlertUtil.showError("Invalid Input", "Please enter a valid year.");
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Database Error", "An error occurred while updating the vehicle.");
        }
    }

    private void handleDelete() {
        if (selectedVehicle == null) {
            AlertUtil.showWarning("No Selection", "Please select a vehicle to delete.");
            return;
        }

        boolean confirmed = AlertUtil.showConfirmation("Delete Vehicle",
                "Are you sure you want to delete vehicle " + selectedVehicle.getRegistrationNumber() + "?");

        if (confirmed) {
            try {
                boolean success = vehicleDAO.delete(selectedVehicle.getId());

                if (success) {
                    AlertUtil.showSuccess("Vehicle deleted successfully.");
                    handleClear();
                    loadVehicles();
                } else {
                    AlertUtil.showError("Delete Failed", "Failed to delete vehicle.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                AlertUtil.showError("Database Error", "An error occurred while deleting the vehicle.");
            }
        }
    }

    private void handleClear() {
        registrationNumberField.clear();
        makeField.clear();
        modelField.clear();
        yearField.clear();
        colorField.clear();
        engineNumberField.clear();
        chassisNumberField.clear();
        ownerComboBox.getSelectionModel().clearSelection();
        statusComboBox.getSelectionModel().clearSelection();
        descriptionArea.clear();
        selectedVehicle = null;
        vehiclesTable.getSelectionModel().clearSelection();
    }

    private void handleViewHistory() {
        if (selectedVehicle == null) {
            AlertUtil.showWarning("No Selection", "Please select a vehicle to view history.");
            return;
        }

        SceneManager.getInstance().switchToVehicleHistory();
    }

    private boolean validateInputs() {
        if (!ValidationUtil.isNotEmpty(registrationNumberField.getText())) {
            AlertUtil.showWarning("Validation Error", "Registration number is required.");
            registrationNumberField.requestFocus();
            return false;
        }

        if (!ValidationUtil.isValidRegistrationNumber(registrationNumberField.getText())) {
            AlertUtil.showWarning("Validation Error", "Registration number must be 3-10 alphanumeric characters.");
            registrationNumberField.requestFocus();
            return false;
        }

        if (!ValidationUtil.isNotEmpty(makeField.getText())) {
            AlertUtil.showWarning("Validation Error", "Make is required.");
            makeField.requestFocus();
            return false;
        }

        if (!ValidationUtil.isNotEmpty(modelField.getText())) {
            AlertUtil.showWarning("Validation Error", "Model is required.");
            modelField.requestFocus();
            return false;
        }

        if (!ValidationUtil.isNotEmpty(yearField.getText())) {
            AlertUtil.showWarning("Validation Error", "Year is required.");
            yearField.requestFocus();
            return false;
        }

        try {
            int year = Integer.parseInt(yearField.getText());
            if (!ValidationUtil.isValidYear(year)) {
                AlertUtil.showWarning("Validation Error", "Please enter a valid year (1900 - current year + 1).");
                yearField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            AlertUtil.showWarning("Validation Error", "Please enter a valid year.");
            yearField.requestFocus();
            return false;
        }

        return true;
    }
}