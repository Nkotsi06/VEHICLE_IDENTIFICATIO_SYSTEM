package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Label;
import utils.AlertUtil;
import utils.SceneManager;
import utils.SessionManager;
import dao.VehicleDAO;
import models.Vehicle;

public class CustomerVehicleController {

    @FXML private TableView<Vehicle> vehiclesTable;
    @FXML private TableColumn<Vehicle, String> registrationColumn;
    @FXML private TableColumn<Vehicle, String> makeColumn;
    @FXML private TableColumn<Vehicle, String> modelColumn;
    @FXML private TableColumn<Vehicle, Integer> yearColumn;
    @FXML private TableColumn<Vehicle, String> statusColumn;
    @FXML private TableColumn<Vehicle, String> colorColumn;

    @FXML private Label customerNameLabel;
    @FXML private Label vehicleCountLabel;

    @FXML private Button viewDetailsButton;
    @FXML private Button viewHistoryButton;
    @FXML private Button viewInsuranceButton;
    @FXML private Button refreshButton;
    @FXML private Button backButton;

    private VehicleDAO vehicleDAO;
    private int customerId;

    @FXML
    public void initialize() {
        vehicleDAO = new VehicleDAO();
        customerId = SessionManager.getInstance().getCustomerId();

        setupTableColumns();
        loadVehicles();
        setupButtonHandlers();

        customerNameLabel.setText("Vehicles owned by: " + SessionManager.getInstance().getFullName());
    }

    private void setupTableColumns() {
        registrationColumn.setCellValueFactory(cellData -> cellData.getValue().registrationNumberProperty());
        makeColumn.setCellValueFactory(cellData -> cellData.getValue().makeProperty());
        modelColumn.setCellValueFactory(cellData -> cellData.getValue().modelProperty());
        yearColumn.setCellValueFactory(cellData -> cellData.getValue().yearProperty().asObject());
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusNameProperty());
        colorColumn.setCellValueFactory(cellData -> cellData.getValue().colorProperty());
    }

    private void loadVehicles() {
        try {
            java.util.List<Vehicle> vehicles = vehicleDAO.findByOwnerId(customerId);
            vehiclesTable.getItems().setAll(vehicles);
            vehicleCountLabel.setText("Total Vehicles: " + vehicles.size());
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Load Failed", "Failed to load vehicles.");
        }
    }

    private void setupButtonHandlers() {
        viewDetailsButton.setOnAction(event -> handleViewDetails());
        viewHistoryButton.setOnAction(event -> handleViewHistory());
        viewInsuranceButton.setOnAction(event -> handleViewInsurance());
        refreshButton.setOnAction(event -> loadVehicles());
        backButton.setOnAction(event -> SceneManager.getInstance().switchToCustomerProfileView());
    }

    private void handleViewDetails() {
        Vehicle selectedVehicle = vehiclesTable.getSelectionModel().getSelectedItem();

        if (selectedVehicle == null) {
            AlertUtil.showWarning("No Selection", "Please select a vehicle to view details.");
            return;
        }

        AlertUtil.showInfo("Vehicle Details",
                "Registration: " + selectedVehicle.getRegistrationNumber() + "\n" +
                        "Make/Model: " + selectedVehicle.getMake() + " " + selectedVehicle.getModel() + "\n" +
                        "Year: " + selectedVehicle.getYear() + "\n" +
                        "Color: " + selectedVehicle.getColor() + "\n" +
                        "Status: " + selectedVehicle.getStatusName() + "\n" +
                        "Engine Number: " + (selectedVehicle.getEngineNumber() != null ? selectedVehicle.getEngineNumber() : "N/A") + "\n" +
                        "Chassis Number: " + (selectedVehicle.getChassisNumber() != null ? selectedVehicle.getChassisNumber() : "N/A"));
    }

    private void handleViewHistory() {
        Vehicle selectedVehicle = vehiclesTable.getSelectionModel().getSelectedItem();

        if (selectedVehicle == null) {
            AlertUtil.showWarning("No Selection", "Please select a vehicle to view history.");
            return;
        }

        SceneManager.getInstance().switchToVehicleHistory();
    }

    private void handleViewInsurance() {
        Vehicle selectedVehicle = vehiclesTable.getSelectionModel().getSelectedItem();

        if (selectedVehicle == null) {
            AlertUtil.showWarning("No Selection", "Please select a vehicle to view insurance.");
            return;
        }

        SceneManager.getInstance().switchToInsurancePolicyView();
    }
}