package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import utils.AlertUtil;
import utils.SceneManager;
import utils.SessionManager;
import utils.ValidationUtil;
import dao.VehicleDAO;
import dao.CustomerDAO;
import dao.VehicleStatusDAO;
import models.Vehicle;
import models.Customer;
import models.VehicleStatus;

import java.util.List;  // ADD THIS IMPORT

public class VehicleRegistrationController {

    @FXML private TextField registrationNumberField;
    @FXML private TextField makeField;
    @FXML private TextField modelField;
    @FXML private TextField yearField;
    @FXML private TextField colorField;
    @FXML private TextField engineNumberField;
    @FXML private TextField chassisNumberField;
    @FXML private ComboBox<Customer> ownerComboBox;
    @FXML private ComboBox<VehicleStatus> statusComboBox;

    @FXML private Button registerButton;
    @FXML private Button clearButton;
    @FXML private Button backButton;
    @FXML private Label statusLabel;

    private VehicleDAO vehicleDAO;
    private CustomerDAO customerDAO;
    private VehicleStatusDAO statusDAO;

    @FXML
    public void initialize() {
        vehicleDAO = new VehicleDAO();
        customerDAO = new CustomerDAO();
        statusDAO = new VehicleStatusDAO();

        loadOwners();
        loadStatuses();
        setupButtonHandlers();
    }

    private void loadOwners() {
        try {
            List<Customer> customers = customerDAO.findAll();
            ownerComboBox.getItems().setAll(customers);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadStatuses() {
        try {
            List<VehicleStatus> statuses = statusDAO.findAll();
            statusComboBox.getItems().setAll(statuses);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupButtonHandlers() {
        registerButton.setOnAction(event -> handleRegister());
        clearButton.setOnAction(event -> clearForm());
        backButton.setOnAction(event -> handleBack());
    }

    private void handleRegister() {
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

            Customer owner = ownerComboBox.getSelectionModel().getSelectedItem();
            if (owner != null) {
                vehicle.setOwnerId(owner.getId());
            }

            VehicleStatus status = statusComboBox.getSelectionModel().getSelectedItem();
            if (status == null) {
                status = statusDAO.findByStatusName("CLEAN");
            }
            if (status != null) {
                vehicle.setStatusId(status.getId());
            }

            boolean success = vehicleDAO.insert(vehicle);

            if (success) {
                AlertUtil.showSuccess("Vehicle registered successfully.");
                statusLabel.setText("Vehicle " + vehicle.getRegistrationNumber() + " registered.");
                clearForm();
            } else {
                AlertUtil.showError("Registration Failed", "Failed to register vehicle.");
            }

        } catch (NumberFormatException e) {
            AlertUtil.showError("Invalid Input", "Please enter a valid year.");
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Database Error", "An error occurred during registration.");
        }
    }

    private void clearForm() {
        registrationNumberField.clear();
        makeField.clear();
        modelField.clear();
        yearField.clear();
        colorField.clear();
        engineNumberField.clear();
        chassisNumberField.clear();
        ownerComboBox.getSelectionModel().clearSelection();
        statusComboBox.getSelectionModel().clearSelection();
        statusLabel.setText("");
    }

    private void handleBack() {
        String role = SessionManager.getInstance().getUserRole();
        if ("WORKSHOP".equals(role)) {
            SceneManager.getInstance().switchToWorkshopProfileView();
        } else if ("ADMIN".equals(role)) {
            SceneManager.getInstance().switchToAdminView();
        } else {
            SceneManager.getInstance().switchToVehicleView();
        }
    }

    private boolean validateInputs() {
        if (!ValidationUtil.isNotEmpty(registrationNumberField.getText())) {
            AlertUtil.showWarning("Validation Error", "Registration number is required.");
            registrationNumberField.requestFocus();
            return false;
        }

        if (!ValidationUtil.isValidRegistrationNumber(registrationNumberField.getText())) {
            AlertUtil.showWarning("Validation Error", "Invalid registration number format.");
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

        if (ownerComboBox.getSelectionModel().getSelectedItem() == null) {
            AlertUtil.showWarning("Validation Error", "Please select an owner.");
            return false;
        }

        return true;
    }
}