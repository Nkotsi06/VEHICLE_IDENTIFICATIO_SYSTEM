package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import utils.AlertUtil;
import utils.SceneManager;
import utils.QRCodeUtil;
import dao.VehicleDAO;
import dao.ServiceRecordDAO;
import models.Vehicle;
import models.ServiceRecord;
import java.time.LocalDate;

public class QRCheckinController {

    @FXML private TextField qrCodeField;
    @FXML private Button scanButton;
    @FXML private Button manualEntryButton;
    @FXML private Button checkinButton;
    @FXML private Button backButton;

    @FXML private Label vehicleInfoLabel;
    @FXML private Label ownerInfoLabel;
    @FXML private Label statusLabel;
    @FXML private ImageView qrImageView;

    @FXML private TextField serviceTypeField;
    @FXML private TextField costField;
    @FXML private TextField odometerField;

    private VehicleDAO vehicleDAO;
    private ServiceRecordDAO serviceDAO;
    private Vehicle currentVehicle;
    private int workshopId;

    @FXML
    public void initialize() {
        vehicleDAO = new VehicleDAO();
        serviceDAO = new ServiceRecordDAO();
        workshopId = utils.SessionManager.getInstance().getWorkshopId();

        setupButtonHandlers();

        checkinButton.setDisable(true);
        generateQRCodePreview();
    }

    private void setupButtonHandlers() {
        scanButton.setOnAction(event -> handleScan());
        manualEntryButton.setOnAction(event -> handleManualEntry());
        checkinButton.setOnAction(event -> handleCheckin());
        backButton.setOnAction(event -> SceneManager.getInstance().switchToWorkshopProfileView());
    }

    private void generateQRCodePreview() {
        // Simulate QR code generation
        String qrData = QRCodeUtil.getInstance().generateQRCodeData(123, "BMD1234");
        qrCodeField.setText(qrData);
    }

    private void handleScan() {
        String qrData = qrCodeField.getText().trim();

        if (!utils.ValidationUtil.isNotEmpty(qrData)) {
            AlertUtil.showWarning("Input Error", "Please enter or scan QR code data.");
            return;
        }

        if (!QRCodeUtil.getInstance().validateQRCode(qrData)) {
            AlertUtil.showError("Invalid QR Code", "The QR code data is invalid.");
            return;
        }

        java.util.Map<String, String> parsed = QRCodeUtil.getInstance().parseQRCodeData(qrData);

        if (parsed.containsKey("REG")) {
            String registrationNumber = parsed.get("REG");
            processVehicleCheckin(registrationNumber);
        } else {
            AlertUtil.showError("Invalid Data", "QR code does not contain vehicle information.");
        }
    }

    private void handleManualEntry() {
        javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog();
        dialog.setTitle("Manual Entry");
        dialog.setHeaderText("Enter Vehicle Registration Number");
        dialog.setContentText("Registration Number:");

        dialog.showAndWait().ifPresent(registration -> {
            if (utils.ValidationUtil.isNotEmpty(registration)) {
                processVehicleCheckin(registration.toUpperCase());
            }
        });
    }

    private void processVehicleCheckin(String registrationNumber) {
        try {
            currentVehicle = vehicleDAO.findByRegistrationNumber(registrationNumber);

            if (currentVehicle != null) {
                vehicleInfoLabel.setText(currentVehicle.getMake() + " " + currentVehicle.getModel() +
                        " (" + currentVehicle.getYear() + ")");
                ownerInfoLabel.setText("Owner ID: " + currentVehicle.getOwnerId());
                statusLabel.setText("Vehicle found. Ready for check-in.");

                checkinButton.setDisable(false);
            } else {
                AlertUtil.showWarning("Vehicle Not Found", "No vehicle found with registration: " + registrationNumber);
                clearVehicleInfo();
                checkinButton.setDisable(true);
            }

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Failed to lookup vehicle.");
        }
    }

    private void handleCheckin() {
        if (currentVehicle == null) {
            AlertUtil.showWarning("No Vehicle", "Please scan or enter a vehicle first.");
            return;
        }

        if (!utils.ValidationUtil.isNotEmpty(serviceTypeField.getText())) {
            AlertUtil.showWarning("Validation Error", "Service type is required.");
            serviceTypeField.requestFocus();
            return;
        }

        if (!utils.ValidationUtil.isNotEmpty(costField.getText())) {
            AlertUtil.showWarning("Validation Error", "Cost is required.");
            costField.requestFocus();
            return;
        }

        try {
            double cost = Double.parseDouble(costField.getText());
            int odometer = utils.ValidationUtil.isNotEmpty(odometerField.getText()) ?
                    Integer.parseInt(odometerField.getText()) : 0;

            ServiceRecord record = new ServiceRecord();
            record.setVehicleId(currentVehicle.getId());
            record.setWorkshopId(workshopId);
            record.setServiceDate(LocalDate.now());
            record.setServiceType(serviceTypeField.getText().trim());
            record.setCost(cost);
            record.setOdometerReading(odometer);

            boolean success = serviceDAO.insert(record);

            if (success) {
                AlertUtil.showSuccess("Vehicle checked in successfully. Service record created.");
                clearForm();
                checkinButton.setDisable(true);
            } else {
                AlertUtil.showError("Check-in Failed", "Failed to create service record.");
            }

        } catch (NumberFormatException e) {
            AlertUtil.showError("Invalid Input", "Please enter valid cost and odometer values.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearVehicleInfo() {
        vehicleInfoLabel.setText("No vehicle selected");
        ownerInfoLabel.setText("");
        statusLabel.setText("");
        currentVehicle = null;
    }

    private void clearForm() {
        qrCodeField.clear();
        serviceTypeField.clear();
        costField.clear();
        odometerField.clear();
        clearVehicleInfo();
        generateQRCodePreview();
    }
}