package controllers;

import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import utils.AlertUtil;
import utils.SceneManager;
import dao.BOLOAlertDAO;
import dao.VehicleDAO;
import models.BOLOAlert;
import models.Vehicle;
import java.util.List;

public class BOLOController {

    @FXML private TableView<BOLOAlert> boloTable;
    @FXML private TableColumn<BOLOAlert, String> vehicleColumn;
    @FXML private TableColumn<BOLOAlert, String> messageColumn;
    @FXML private TableColumn<BOLOAlert, String> priorityColumn;
    @FXML private TableColumn<BOLOAlert, String> expiryDateColumn;
    @FXML private TableColumn<BOLOAlert, String> boloStatusColumn; // Fixed - was "statusColumn"

    @FXML private ComboBox<Vehicle> vehicleComboBox;
    @FXML private TextField messageField;
    @FXML private ComboBox<String> priorityComboBox;
    @FXML private DatePicker expiryDatePicker;
    @FXML private TextArea detailsArea;

    @FXML private Button generateButton;
    @FXML private Button cancelButton;
    @FXML private Button refreshButton;
    @FXML private Button backButton;
    @FXML private ProgressIndicator loadProgress;
    @FXML private ProgressBar operationProgress;
    @FXML private Pagination boloPagination;
    @FXML private Label statusLabel;

    private BOLOAlertDAO boloDAO;
    private VehicleDAO vehicleDAO;
    private BOLOAlert selectedAlert;
    private List<BOLOAlert> fullAlertList;
    private int currentPage = 0;
    private int pageSize = 20;

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
        setupPagination();

        expiryDatePicker.setValue(java.time.LocalDate.now().plusDays(30));
        statusLabel.setText("Ready");
    }

    private void setupTableColumns() {
        vehicleColumn.setCellValueFactory(cellData -> cellData.getValue().registrationNumberProperty());
        messageColumn.setCellValueFactory(cellData -> cellData.getValue().messageProperty());
        priorityColumn.setCellValueFactory(cellData -> cellData.getValue().priorityProperty());
        expiryDateColumn.setCellValueFactory(cellData -> cellData.getValue().expiryDateProperty().asString());
        boloStatusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
    }

    private void setupPagination() {
        if (boloPagination != null) {
            boloPagination.currentPageIndexProperty().addListener((obs, old, newPage) -> {
                currentPage = newPage.intValue();
                updateTablePage();
            });
        }
    }

    private void updateTablePage() {
        if (fullAlertList == null || fullAlertList.isEmpty()) return;
        int start = currentPage * pageSize;
        int end = Math.min(start + pageSize, fullAlertList.size());
        if (start < fullAlertList.size()) {
            boloTable.getItems().setAll(fullAlertList.subList(start, end));
        }
    }

    private void loadVehicles() {
        try {
            List<Vehicle> vehicles = vehicleDAO.findAll();
            vehicleComboBox.getItems().setAll(vehicles);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadBOLOAlerts() {
        showProgress(true);
        statusLabel.setText("Loading BOLO alerts...");

        try {
            fullAlertList = boloDAO.findAll();
            int totalPages = (int) Math.ceil((double) fullAlertList.size() / pageSize);
            if (boloPagination != null) boloPagination.setPageCount(Math.max(1, totalPages));
            updateTablePage();
            statusLabel.setText("Loaded " + fullAlertList.size() + " alerts");
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Load Failed", "Failed to load BOLO alerts.");
            statusLabel.setText("Error loading alerts");
        } finally {
            hideProgressAfterDelay();
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

        showProgress(true);
        statusLabel.setText("Generating BOLO alert...");

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
                statusLabel.setText("Alert generated successfully");
            } else {
                AlertUtil.showError("Generate Failed", "Failed to generate BOLO alert.");
                statusLabel.setText("Generation failed");
            }

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Database Error", "An error occurred while generating BOLO alert.");
            statusLabel.setText("Error: " + e.getMessage());
        } finally {
            hideProgressAfterDelay();
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
            showProgress(true);
            statusLabel.setText("Cancelling alert...");

            try {
                boolean success = boloDAO.cancelAlert(selectedAlert.getId());

                if (success) {
                    AlertUtil.showSuccess("BOLO alert cancelled successfully.");
                    loadBOLOAlerts();
                    statusLabel.setText("Alert cancelled");
                } else {
                    AlertUtil.showError("Cancel Failed", "Failed to cancel BOLO alert.");
                    statusLabel.setText("Cancel failed");
                }

            } catch (Exception e) {
                e.printStackTrace();
                AlertUtil.showError("Database Error", "An error occurred while cancelling alert.");
                statusLabel.setText("Error: " + e.getMessage());
            } finally {
                hideProgressAfterDelay();
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

    private void showProgress(boolean show) {
        if (loadProgress != null) loadProgress.setVisible(show);
        if (operationProgress != null) {
            operationProgress.setVisible(show);
            operationProgress.setProgress(0);
        }
    }

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