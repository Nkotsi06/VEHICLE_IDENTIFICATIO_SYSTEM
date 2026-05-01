package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import utils.AlertUtil;
import utils.SceneManager;
import utils.SessionManager;
import dao.VehicleDAO;
import dao.CustomerDAO;
import dao.VehicleStatusDAO;
import dao.VehicleDocumentDAO;
import models.Vehicle;
import models.Customer;
import models.VehicleStatus;
import models.VehicleDocument;
import java.time.LocalDate;

public class VehicleRegistrationWizardController {

    @FXML private VBox step1Box;
    @FXML private VBox step2Box;
    @FXML private VBox step3Box;
    @FXML private VBox step4Box;

    @FXML private TextField registrationNumberField;
    @FXML private TextField makeField;
    @FXML private TextField modelField;
    @FXML private TextField yearField;
    @FXML private TextField colorField;
    @FXML private TextField engineNumberField;
    @FXML private TextField chassisNumberField;
    @FXML private ComboBox<Customer> ownerComboBox;

    @FXML private TextField documentNumberField;
    @FXML private ComboBox<String> documentTypeComboBox;
    @FXML private DatePicker issueDatePicker;
    @FXML private DatePicker expiryDatePicker;

    @FXML private Label step1Status;
    @FXML private Label step2Status;
    @FXML private Label step3Status;
    @FXML private Label step4Status;

    @FXML private Button next1Button;
    @FXML private Button back2Button;
    @FXML private Button next2Button;
    @FXML private Button back3Button;
    @FXML private Button next3Button;
    @FXML private Button back4Button;
    @FXML private Button submitButton;
    @FXML private Button cancelButton;

    private VehicleDAO vehicleDAO;
    private CustomerDAO customerDAO;
    private VehicleStatusDAO statusDAO;
    private VehicleDocumentDAO documentDAO;

    private Vehicle newVehicle;
    private int currentStep = 1;
    private int workshopId;

    @FXML
    public void initialize() {
        vehicleDAO = new VehicleDAO();
        customerDAO = new CustomerDAO();
        statusDAO = new VehicleStatusDAO();
        documentDAO = new VehicleDocumentDAO();

        workshopId = SessionManager.getInstance().getWorkshopId();

        setupComboBoxes();
        setupButtonHandlers();
        showStep(1);

        issueDatePicker.setValue(LocalDate.now());
        expiryDatePicker.setValue(LocalDate.now().plusYears(1));

        documentTypeComboBox.getItems().addAll(
                "REGISTRATION_CERTIFICATE", "INSURANCE_DISC", "ROAD_TAX_DISC",
                "FITNESS_CERTIFICATE", "EMISSION_TEST_CERTIFICATE", "PCO_LICENSE"
        );
    }

    private void setupComboBoxes() {
        try {
            java.util.List<Customer> customers = customerDAO.findAll();
            ownerComboBox.getItems().setAll(customers);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupButtonHandlers() {
        next1Button.setOnAction(event -> goToStep2());
        back2Button.setOnAction(event -> goToStep1());
        next2Button.setOnAction(event -> goToStep3());
        back3Button.setOnAction(event -> goToStep2());
        next3Button.setOnAction(event -> goToStep4());
        back4Button.setOnAction(event -> goToStep3());
        submitButton.setOnAction(event -> handleSubmit());
        cancelButton.setOnAction(event -> handleCancel());
    }

    private void showStep(int step) {
        step1Box.setVisible(step == 1);
        step2Box.setVisible(step == 2);
        step3Box.setVisible(step == 3);
        step4Box.setVisible(step == 4);

        updateStepStatus();
    }

    private void updateStepStatus() {
        step1Status.setText(validateStep1() ? "Complete" : "Pending");
        step2Status.setText(validateStep2() ? "Complete" : "Pending");
        step3Status.setText(validateStep3() ? "Complete" : "Pending");

        boolean allComplete = validateStep1() && validateStep2() && validateStep3();
        submitButton.setDisable(!allComplete);
    }

    private boolean validateStep1() {
        return utils.ValidationUtil.isNotEmpty(registrationNumberField.getText()) &&
                utils.ValidationUtil.isNotEmpty(makeField.getText()) &&
                utils.ValidationUtil.isNotEmpty(modelField.getText()) &&
                utils.ValidationUtil.isNotEmpty(yearField.getText()) &&
                ownerComboBox.getSelectionModel().getSelectedItem() != null;
    }

    private boolean validateStep2() {
        return true;
    }

    private boolean validateStep3() {
        return utils.ValidationUtil.isNotEmpty(documentNumberField.getText()) &&
                documentTypeComboBox.getSelectionModel().getSelectedItem() != null &&
                issueDatePicker.getValue() != null &&
                expiryDatePicker.getValue() != null;
    }

    private void goToStep1() {
        currentStep = 1;
        showStep(currentStep);
    }

    private void goToStep2() {
        if (!validateStep1()) {
            AlertUtil.showWarning("Incomplete", "Please complete all required fields in Step 1.");
            return;
        }
        currentStep = 2;
        showStep(currentStep);
    }

    private void goToStep3() {
        currentStep = 3;
        showStep(currentStep);
    }

    private void goToStep4() {
        if (!validateStep3()) {
            AlertUtil.showWarning("Incomplete", "Please complete all required fields in Step 3.");
            return;
        }
        currentStep = 4;
        showStep(currentStep);

        displaySummary();
    }

    private void displaySummary() {
        Customer owner = ownerComboBox.getSelectionModel().getSelectedItem();

        StringBuilder summary = new StringBuilder();
        summary.append("=== VEHICLE REGISTRATION SUMMARY ===\n\n");
        summary.append("Registration: ").append(registrationNumberField.getText()).append("\n");
        summary.append("Make/Model: ").append(makeField.getText()).append(" ").append(modelField.getText()).append("\n");
        summary.append("Year: ").append(yearField.getText()).append("\n");
        summary.append("Color: ").append(colorField.getText()).append("\n");
        summary.append("Owner: ").append(owner != null ? owner.getName() : "Unknown").append("\n");
        summary.append("Engine: ").append(engineNumberField.getText()).append("\n");
        summary.append("Chassis: ").append(chassisNumberField.getText()).append("\n\n");
        summary.append("Document Type: ").append(documentTypeComboBox.getValue()).append("\n");
        summary.append("Document Number: ").append(documentNumberField.getText()).append("\n");
        summary.append("Valid From: ").append(issueDatePicker.getValue()).append(" To: ").append(expiryDatePicker.getValue());

        Label summaryLabel = new Label(summary.toString());
        summaryLabel.setWrapText(true);
        summaryLabel.setStyle("-fx-padding: 10px; -fx-background-color: #f5f5f5;");

        step4Box.getChildren().add(0, summaryLabel);
    }

    private void handleSubmit() {
        if (!validateStep1() || !validateStep3()) {
            AlertUtil.showWarning("Incomplete", "Please complete all required steps.");
            return;
        }

        try {
            Customer owner = ownerComboBox.getSelectionModel().getSelectedItem();
            if (owner == null) {
                AlertUtil.showError("Error", "Please select an owner.");
                return;
            }

            VehicleStatus defaultStatus = statusDAO.findByStatusName("CLEAN");

            newVehicle = new Vehicle();
            newVehicle.setRegistrationNumber(registrationNumberField.getText().trim().toUpperCase());
            newVehicle.setMake(makeField.getText().trim());
            newVehicle.setModel(modelField.getText().trim());
            newVehicle.setYear(Integer.parseInt(yearField.getText().trim()));
            newVehicle.setOwnerId(owner.getId());
            newVehicle.setStatusId(defaultStatus != null ? defaultStatus.getId() : 1);
            newVehicle.setColor(colorField.getText().trim());
            newVehicle.setEngineNumber(engineNumberField.getText().trim());
            newVehicle.setChassisNumber(chassisNumberField.getText().trim());

            int vehicleId = vehicleDAO.insertAndGetId(newVehicle);

            if (vehicleId > 0) {
                VehicleDocument document = new VehicleDocument();
                document.setVehicleId(vehicleId);
                document.setDocumentType(documentTypeComboBox.getValue());
                document.setDocumentNumber(documentNumberField.getText().trim());
                document.setIssueDate(issueDatePicker.getValue());
                document.setExpiryDate(expiryDatePicker.getValue());
                document.setStatus("ACTIVE");
                documentDAO.insert(document);

                AlertUtil.showSuccess("Vehicle registered successfully!\nVehicle ID: " + vehicleId);
                handleCancel();
            } else {
                AlertUtil.showError("Registration Failed", "Failed to register vehicle.");
            }

        } catch (NumberFormatException e) {
            AlertUtil.showError("Invalid Year", "Please enter a valid year.");
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Database Error", "An error occurred during registration.");
        }
    }

    private void handleCancel() {
        SceneManager.getInstance().switchToWorkshopProfileView();
    }
}