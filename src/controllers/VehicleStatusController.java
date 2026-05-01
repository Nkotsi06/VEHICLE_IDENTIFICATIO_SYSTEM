package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Label;
import utils.AlertUtil;
import utils.SceneManager;
import dao.VehicleDAO;
import dao.VehicleStatusDAO;
import models.Vehicle;
import models.VehicleStatus;  // ADDED: Missing import

public class VehicleStatusController {

    @FXML private TableView<Vehicle> vehiclesTable;
    @FXML private TableColumn<Vehicle, String> registrationColumn;
    @FXML private TableColumn<Vehicle, String> makeColumn;
    @FXML private TableColumn<Vehicle, String> modelColumn;
    @FXML private TableColumn<Vehicle, String> currentStatusColumn;

    @FXML private ComboBox<Vehicle> vehicleComboBox;
    @FXML private ComboBox<VehicleStatus> statusComboBox;
    @FXML private Label vehicleInfoLabel;

    @FXML private Button updateStatusButton;
    @FXML private Button refreshButton;
    @FXML private Button backButton;

    private VehicleDAO vehicleDAO;
    private VehicleStatusDAO statusDAO;

    @FXML
    public void initialize() {
        vehicleDAO = new VehicleDAO();
        statusDAO = new VehicleStatusDAO();

        setupTableColumns();
        loadVehicles();
        loadStatuses();
        setupButtonHandlers();
    }

    private void setupTableColumns() {
        registrationColumn.setCellValueFactory(cellData -> cellData.getValue().registrationNumberProperty());
        makeColumn.setCellValueFactory(cellData -> cellData.getValue().makeProperty());
        modelColumn.setCellValueFactory(cellData -> cellData.getValue().modelProperty());
        currentStatusColumn.setCellValueFactory(cellData -> cellData.getValue().statusNameProperty());
    }

    private void loadVehicles() {
        try {
            java.util.List<Vehicle> vehicles = vehicleDAO.findAll();
            vehiclesTable.getItems().setAll(vehicles);
            vehicleComboBox.getItems().setAll(vehicles);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadStatuses() {
        try {
            java.util.List<VehicleStatus> statuses = statusDAO.findAll();
            statusComboBox.getItems().setAll(statuses);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupButtonHandlers() {
        updateStatusButton.setOnAction(event -> handleUpdateStatus());
        refreshButton.setOnAction(event -> loadVehicles());
        backButton.setOnAction(event -> SceneManager.getInstance().switchToPoliceView());

        vehicleComboBox.setOnAction(event -> {
            Vehicle selected = vehicleComboBox.getSelectionModel().getSelectedItem();
            if (selected != null) {
                vehicleInfoLabel.setText(selected.getRegistrationNumber() + " - " +
                        selected.getMake() + " " + selected.getModel() +
                        " | Current Status: " + selected.getStatusName());
            }
        });
    }

    private void handleUpdateStatus() {
        Vehicle selectedVehicle = vehicleComboBox.getSelectionModel().getSelectedItem();
        VehicleStatus selectedStatus = statusComboBox.getSelectionModel().getSelectedItem();

        if (selectedVehicle == null) {
            AlertUtil.showWarning("Validation Error", "Please select a vehicle.");
            return;
        }

        if (selectedStatus == null) {
            AlertUtil.showWarning("Validation Error", "Please select a status.");
            return;
        }

        if (selectedVehicle.getStatusId() == selectedStatus.getId()) {
            AlertUtil.showWarning("No Change", "Vehicle already has this status.");
            return;
        }

        boolean confirmed = AlertUtil.showConfirmation("Update Status",
                "Change vehicle " + selectedVehicle.getRegistrationNumber() +
                        " status from " + selectedVehicle.getStatusName() + " to " + selectedStatus.getStatusName() + "?");

        if (confirmed) {
            try {
                boolean success = vehicleDAO.updateStatus(selectedVehicle.getId(), selectedStatus.getId());

                if (success) {
                    AlertUtil.showSuccess("Vehicle status updated successfully.");
                    loadVehicles();
                    vehicleInfoLabel.setText("");
                    vehicleComboBox.getSelectionModel().clearSelection();
                    statusComboBox.getSelectionModel().clearSelection();
                } else {
                    AlertUtil.showError("Update Failed", "Failed to update vehicle status.");
                }

            } catch (Exception e) {
                e.printStackTrace();
                AlertUtil.showError("Database Error", "An error occurred while updating status.");
            }
        }
    }
}