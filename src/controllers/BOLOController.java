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
import dao.BOLOAlertDAO;
import dao.VehicleDAO;
import models.BOLOAlert;
import models.Vehicle;

public class BOLOController {

    @FXML private TableView<BOLOAlert> boloTable;
    @FXML private TableColumn<BOLOAlert, String> vehicleColumn;
    @FXML private TableColumn<BOLOAlert, String> messageColumn;
    @FXML private TableColumn<BOLOAlert, String> priorityColumn;
    @FXML private TableColumn<BOLOAlert, String> expiryDateColumn;
    @FXML private TableColumn<BOLOAlert, String> statusColumn;

    @FXML private ComboBox<Vehicle> vehicleComboBox;
    @FXML private TextField messageField;
    @FXML private ComboBox<String> priorityComboBox;
    @FXML private DatePicker expiryDatePicker;
    @FXML private TextArea detailsArea;

    @FXML private Button generateButton;
    @FXML private Button cancelButton;
    @FXML private Button refreshButton;
    @FXML private Button backButton;

    private BOLOAlertDAO boloDAO;
    private VehicleDAO vehicleDAO;
    private BOLOAlert selectedAlert;

    @FXML
    public void initialize() {
        boloDAO = new BOLOAlertDAO();
        vehicleDAO = new VehicleDAO();

        setupTableColumns();
        loadVehicles();
        loadBOLOAlerts();
        setupComboBoxes();
        setupButtonHandlers();
        setupTableSelection();

        expiryDatePicker.setValue(java.time.LocalDate.now().plusDays(30));
    }

    private void setupTableColumns() {
        vehicleColumn.setCellValueFactory(cellData -> cellData.getValue().registrationNumberProperty());
        messageColumn.setCellValueFactory(cellData -> cellData.getValue().messageProperty());
        priorityColumn.setCellValueFactory(cellData -> cellData.getValue().priorityProperty());
        expiryDateColumn.setCellValueFactory(cellData -> cellData.getValue().expiryDateProperty().asString());
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
    }

    private void loadVehicles() {
        try {
            java.util.List<Vehicle> vehicles = vehicleDAO.findAll();
            vehicleComboBox.getItems().setAll(vehicles);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadBOLOAlerts() {
        try {
            java.util.List<BOLOAlert> alerts = boloDAO.findAll();
            boloTable.getItems().setAll(alerts);
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Load Failed", "Failed to load BOLO alerts.");
        }
    }

    private void setupComboBoxes() {
        priorityComboBox.getItems().addAll("HIGH", "MEDIUM", "LOW");
        priorityComboBox.setValue("MEDIUM");
    }

    private void setupButtonHandlers() {
        generateButton.setOnAction(event -> handleGenerate());
        cancelButton.setOnAction(event -> handleCancel());
        refreshButton.setOnAction(event -> loadBOLOAlerts());
        backButton.setOnAction(event -> SceneManager.getInstance().switchToPoliceView());
    }

    private void setupTableSelection() {
        boloTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedAlert = newSelection;
            }
        });
    }

    private void handleGenerate() {
        Vehicle selectedVehicle = vehicleComboBox.getSelectionModel().getSelectedItem();

        if (selectedVehicle == null) {
            AlertUtil.showWarning("Validation Error", "Please select a vehicle.");
            return;
        }

        if (!utils.ValidationUtil.isNotEmpty(messageField.getText())) {
            AlertUtil.showWarning("Validation Error", "Please enter an alert message.");
            messageField.requestFocus();
            return;
        }

        if (expiryDatePicker.getValue() == null) {
            AlertUtil.showWarning("Validation Error", "Please select an expiry date.");
            return;
        }

        try {
            BOLOAlert alert = new BOLOAlert();
            alert.setVehicleId(selectedVehicle.getId());
            alert.setMessage(messageField.getText().trim());
            alert.setPriority(priorityComboBox.getValue());
            alert.setExpiryDate(expiryDatePicker.getValue());

            boolean success = boloDAO.insert(alert);

            if (success) {
                AlertUtil.showSuccess("BOLO alert generated and distributed to all police units.");
                clearForm();
                loadBOLOAlerts();
            } else {
                AlertUtil.showError("Generate Failed", "Failed to generate BOLO alert.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Database Error", "An error occurred while generating BOLO alert.");
        }
    }

    private void handleCancel() {
        if (selectedAlert == null) {
            AlertUtil.showWarning("No Selection", "Please select an alert to cancel.");
            return;
        }

        if (!"ACTIVE".equals(selectedAlert.getStatus())) {
            AlertUtil.showWarning("Already Processed", "This alert is already " + selectedAlert.getStatus().toLowerCase());
            return;
        }

        boolean confirmed = AlertUtil.showConfirmation("Cancel Alert",
                "Cancel BOLO alert for vehicle " + selectedAlert.getRegistrationNumber() + "?");

        if (confirmed) {
            try {
                boolean success = boloDAO.cancelAlert(selectedAlert.getId());

                if (success) {
                    AlertUtil.showSuccess("BOLO alert cancelled successfully.");
                    loadBOLOAlerts();
                } else {
                    AlertUtil.showError("Cancel Failed", "Failed to cancel BOLO alert.");
                }

            } catch (Exception e) {
                e.printStackTrace();
                AlertUtil.showError("Database Error", "An error occurred while cancelling alert.");
            }
        }
    }

    private void clearForm() {
        vehicleComboBox.getSelectionModel().clearSelection();
        messageField.clear();
        priorityComboBox.setValue("MEDIUM");
        expiryDatePicker.setValue(java.time.LocalDate.now().plusDays(30));
        detailsArea.clear();
        selectedAlert = null;
        boloTable.getSelectionModel().clearSelection();
    }
}