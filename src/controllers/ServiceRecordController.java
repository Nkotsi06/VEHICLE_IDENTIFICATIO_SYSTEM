package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import utils.AlertUtil;
import utils.SceneManager;
import utils.SessionManager;
import dao.ServiceRecordDAO;
import dao.VehicleDAO;
import dao.MechanicDAO;
import models.ServiceRecord;
import models.Vehicle;
import models.Mechanic;

public class ServiceRecordController {

    @FXML private TableView<ServiceRecord> servicesTable;
    @FXML private TableColumn<ServiceRecord, String> vehicleColumn;
    @FXML private TableColumn<ServiceRecord, String> serviceDateColumn;
    @FXML private TableColumn<ServiceRecord, String> serviceTypeColumn;
    @FXML private TableColumn<ServiceRecord, Double> costColumn;
    @FXML private TableColumn<ServiceRecord, String> mechanicColumn;

    @FXML private ComboBox<Vehicle> vehicleComboBox;
    @FXML private ComboBox<Mechanic> mechanicComboBox;
    @FXML private TextField serviceTypeField;
    @FXML private TextField costField;
    @FXML private TextField odometerField;
    @FXML private DatePicker serviceDatePicker;
    @FXML private TextArea descriptionArea;

    @FXML private Button addButton;
    @FXML private Button updateButton;
    @FXML private Button deleteButton;
    @FXML private Button clearButton;
    @FXML private Button refreshButton;
    @FXML private Button backButton;

    private ServiceRecordDAO serviceDAO;
    private VehicleDAO vehicleDAO;
    private MechanicDAO mechanicDAO;
    private ServiceRecord selectedRecord;
    private int workshopId;

    @FXML
    public void initialize() {
        serviceDAO = new ServiceRecordDAO();
        vehicleDAO = new VehicleDAO();
        mechanicDAO = new MechanicDAO();

        workshopId = SessionManager.getInstance().getWorkshopId();

        setupTableColumns();
        loadComboBoxes();
        loadServices();
        setupButtonHandlers();
        setupTableSelection();

        serviceDatePicker.setValue(java.time.LocalDate.now());
    }

    private void setupTableColumns() {
        vehicleColumn.setCellValueFactory(cellData -> cellData.getValue().registrationNumberProperty());
        serviceDateColumn.setCellValueFactory(cellData -> cellData.getValue().serviceDateProperty().asString());
        serviceTypeColumn.setCellValueFactory(cellData -> cellData.getValue().serviceTypeProperty());
        costColumn.setCellValueFactory(cellData -> cellData.getValue().costProperty().asObject());
        mechanicColumn.setCellValueFactory(cellData -> cellData.getValue().mechanicNameProperty());
    }

    private void loadComboBoxes() {
        try {
            java.util.List<Vehicle> vehicles = vehicleDAO.findAll();
            vehicleComboBox.getItems().setAll(vehicles);

            java.util.List<Mechanic> mechanics = mechanicDAO.findByWorkshopId(workshopId);
            mechanicComboBox.getItems().setAll(mechanics);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadServices() {
        try {
            java.util.List<ServiceRecord> services = serviceDAO.findByWorkshopId(workshopId);
            servicesTable.getItems().setAll(services);
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Load Failed", "Failed to load service records.");
        }
    }

    private void setupButtonHandlers() {
        addButton.setOnAction(event -> handleAdd());
        updateButton.setOnAction(event -> handleUpdate());
        deleteButton.setOnAction(event -> handleDelete());
        clearButton.setOnAction(event -> handleClear());
        refreshButton.setOnAction(event -> loadServices());
        backButton.setOnAction(event -> SceneManager.getInstance().switchToWorkshopProfileView());
    }

    private void setupTableSelection() {
        servicesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedRecord = newSelection;
                displayServiceDetails(selectedRecord);
            }
        });
    }

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
    }

    private void handleAdd() {
        if (!validateInputs()) {
            return;
        }

        Vehicle selectedVehicle = vehicleComboBox.getSelectionModel().getSelectedItem();
        if (selectedVehicle == null) {
            AlertUtil.showWarning("Validation Error", "Please select a vehicle.");
            return;
        }

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

            if (utils.ValidationUtil.isNotEmpty(odometerField.getText())) {
                record.setOdometerReading(Integer.parseInt(odometerField.getText()));
            }

            boolean success = serviceDAO.insert(record);

            if (success) {
                AlertUtil.showSuccess("Service record added successfully.");
                handleClear();
                loadServices();
            } else {
                AlertUtil.showError("Add Failed", "Failed to add service record.");
            }

        } catch (NumberFormatException e) {
            AlertUtil.showError("Invalid Input", "Please enter valid cost and odometer values.");
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Database Error", "An error occurred while adding service record.");
        }
    }

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

            if (utils.ValidationUtil.isNotEmpty(odometerField.getText())) {
                selectedRecord.setOdometerReading(Integer.parseInt(odometerField.getText()));
            }

            boolean success = serviceDAO.update(selectedRecord);

            if (success) {
                AlertUtil.showSuccess("Service record updated successfully.");
                loadServices();
            } else {
                AlertUtil.showError("Update Failed", "Failed to update service record.");
            }

        } catch (NumberFormatException e) {
            AlertUtil.showError("Invalid Input", "Please enter valid cost and odometer values.");
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Database Error", "An error occurred while updating service record.");
        }
    }

    private void handleDelete() {
        if (selectedRecord == null) {
            AlertUtil.showWarning("No Selection", "Please select a service record to delete.");
            return;
        }

        boolean confirmed = AlertUtil.showConfirmation("Delete Service Record",
                "Are you sure you want to delete this service record?");

        if (confirmed) {
            try {
                boolean success = serviceDAO.delete(selectedRecord.getId());

                if (success) {
                    AlertUtil.showSuccess("Service record deleted successfully.");
                    handleClear();
                    loadServices();
                } else {
                    AlertUtil.showError("Delete Failed", "Failed to delete service record.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                AlertUtil.showError("Database Error", "An error occurred while deleting service record.");
            }
        }
    }

    private void handleClear() {
        vehicleComboBox.getSelectionModel().clearSelection();
        mechanicComboBox.getSelectionModel().clearSelection();
        serviceTypeField.clear();
        costField.clear();
        odometerField.clear();
        serviceDatePicker.setValue(java.time.LocalDate.now());
        descriptionArea.clear();
        selectedRecord = null;
        servicesTable.getSelectionModel().clearSelection();
    }

    private boolean validateInputs() {
        if (!utils.ValidationUtil.isNotEmpty(serviceTypeField.getText())) {
            AlertUtil.showWarning("Validation Error", "Service type is required.");
            serviceTypeField.requestFocus();
            return false;
        }

        if (!utils.ValidationUtil.isNotEmpty(costField.getText())) {
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

        return true;
    }
}