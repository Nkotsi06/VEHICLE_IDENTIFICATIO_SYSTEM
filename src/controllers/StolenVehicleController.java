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
import dao.StolenVehicleDAO;
import dao.VehicleDAO;
import dao.AuditDAO;
import models.StolenVehicle;
import models.Vehicle;
import java.time.LocalDate;

public class StolenVehicleController {

    @FXML private TableView<StolenVehicle> stolenVehiclesTable;
    @FXML private TableColumn<StolenVehicle, String> regNumberColumn;
    @FXML private TableColumn<StolenVehicle, String> makeColumn;
    @FXML private TableColumn<StolenVehicle, String> modelColumn;
    @FXML private TableColumn<StolenVehicle, String> caseNumberColumn;
    @FXML private TableColumn<StolenVehicle, String> statusColumn;
    @FXML private TableColumn<StolenVehicle, String> officerColumn;

    @FXML private ComboBox<Vehicle> vehicleComboBox;
    @FXML private TextField caseNumberField;
    @FXML private TextField assignedOfficerField;
    @FXML private TextArea descriptionArea;
    @FXML private DatePicker reportedDatePicker;
    @FXML private ComboBox<String> statusComboBox;

    @FXML private Button reportButton;
    @FXML private Button markRecoveredButton;
    @FXML private Button refreshButton;
    @FXML private Button backButton;
    @FXML private Button fadeButton;

    @FXML private ProgressIndicator loadProgress;
    @FXML private ProgressBar operationProgress;
    @FXML private Label statusLabel;

    private StolenVehicleDAO stolenVehicleDAO;
    private VehicleDAO vehicleDAO;
    private AuditDAO auditDAO;
    private StolenVehicle selectedStolenVehicle;

    @FXML
    public void initialize() {
        stolenVehicleDAO = new StolenVehicleDAO();
        vehicleDAO = new VehicleDAO();
        auditDAO = new AuditDAO();

        setupTableColumns();
        loadComboBoxes();
        loadStolenVehicles();
        setupButtonHandlers();
        setupTableSelection();
        applyVisualEffects();

        statusComboBox.getItems().setAll("ACTIVE", "RECOVERED", "CLOSED");
        reportedDatePicker.setValue(LocalDate.now());
        statusLabel.setText("Ready");
    }

    private void setupTableColumns() {
        regNumberColumn.setCellValueFactory(cellData -> cellData.getValue().registrationNumberProperty());
        makeColumn.setCellValueFactory(cellData -> cellData.getValue().makeProperty());
        modelColumn.setCellValueFactory(cellData -> cellData.getValue().modelProperty());
        caseNumberColumn.setCellValueFactory(cellData -> cellData.getValue().caseNumberProperty());
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        officerColumn.setCellValueFactory(cellData -> cellData.getValue().assignedOfficerProperty());

        regNumberColumn.setStyle("-fx-alignment: CENTER;");
        makeColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        modelColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        caseNumberColumn.setStyle("-fx-alignment: CENTER;");
        statusColumn.setStyle("-fx-alignment: CENTER;");
        officerColumn.setStyle("-fx-alignment: CENTER-LEFT;");
    }

    private void loadComboBoxes() {
        try {
            java.util.List<Vehicle> vehicles = vehicleDAO.findAll();
            vehicleComboBox.getItems().setAll(vehicles);
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error loading vehicles");
        }
    }

    private void loadStolenVehicles() {
        showLoadProgress(true);
        statusLabel.setText("Loading stolen vehicles...");

        try {
            java.util.List<StolenVehicle> stolenVehicles = stolenVehicleDAO.findAll();
            stolenVehiclesTable.getItems().setAll(stolenVehicles);
            statusLabel.setText("Loaded " + stolenVehicles.size() + " records");
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error loading data");
            AlertUtil.showError("Load Failed", "Failed to load stolen vehicles.");
        } finally {
            showLoadProgress(false);
        }
    }

    private void setupButtonHandlers() {
        reportButton.setOnAction(event -> handleReportStolen());
        markRecoveredButton.setOnAction(event -> handleMarkRecovered());
        refreshButton.setOnAction(event -> {
            loadStolenVehicles();
            statusLabel.setText("Data refreshed");
        });
        backButton.setOnAction(event -> SceneManager.getInstance().switchToPoliceView());

        if (fadeButton != null) {
            fadeButton.setOnAction(event -> showFadeAnimation());
        }
    }

    private void setupTableSelection() {
        stolenVehiclesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedStolenVehicle = newSelection;
                displayStolenVehicleDetails(selectedStolenVehicle);
            }
        });
    }

    private void displayStolenVehicleDetails(StolenVehicle stolen) {
        try {
            Vehicle vehicle = vehicleDAO.findById(stolen.getVehicleId());
            vehicleComboBox.getSelectionModel().select(vehicle);
        } catch (Exception e) {
            e.printStackTrace();
        }

        caseNumberField.setText(stolen.getCaseNumber());
        assignedOfficerField.setText(stolen.getAssignedOfficer());
        reportedDatePicker.setValue(stolen.getReportedDate());
        statusComboBox.setValue(stolen.getStatus());
        descriptionArea.setText(stolen.getDescription());
    }

    private void handleReportStolen() {
        Vehicle selectedVehicle = vehicleComboBox.getSelectionModel().getSelectedItem();

        if (selectedVehicle == null) {
            AlertUtil.showWarning("Validation Error", "Please select a vehicle.");
            return;
        }

        if (!utils.ValidationUtil.isNotEmpty(caseNumberField.getText())) {
            AlertUtil.showWarning("Validation Error", "Case number is required.");
            caseNumberField.requestFocus();
            return;
        }

        showOperationProgress(true);
        statusLabel.setText("Reporting stolen vehicle...");
        updateProgress(0.3);

        try {
            String officerName = SessionManager.getInstance().getFullName();
            if (officerName == null || officerName.isEmpty()) {
                officerName = SessionManager.getInstance().getUsername();
            }

            updateProgress(0.6);

            boolean success = stolenVehicleDAO.insertStolenVehicle(
                    selectedVehicle.getId(),
                    caseNumberField.getText().trim(),
                    officerName,
                    descriptionArea.getText()
            );

            updateProgress(0.9);

            if (success) {
                updateProgress(1.0);
                // Log stolen vehicle report
                int currentUserId = SessionManager.getInstance().getUserId();
                auditDAO.logAction(currentUserId, "REPORT_STOLEN_VEHICLE: " + selectedVehicle.getRegistrationNumber() +
                        " - Case: " + caseNumberField.getText().trim(), "127.0.0.1");

                AlertUtil.showSuccess("Stolen vehicle reported successfully.");
                statusLabel.setText("Vehicle reported as stolen");
                clearForm();
                loadStolenVehicles();
            } else {
                statusLabel.setText("Failed to report vehicle");
                AlertUtil.showError("Report Failed", "Failed to report stolen vehicle.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error: " + e.getMessage());
            AlertUtil.showError("Database Error", "An error occurred while reporting the stolen vehicle.");
        } finally {
            hideProgressAfterDelay();
        }
    }

    private void handleMarkRecovered() {
        if (selectedStolenVehicle == null) {
            AlertUtil.showWarning("No Selection", "Please select a stolen vehicle record to mark as recovered.");
            return;
        }

        if ("RECOVERED".equals(selectedStolenVehicle.getStatus())) {
            AlertUtil.showWarning("Already Recovered", "This vehicle is already marked as recovered.");
            return;
        }

        boolean confirmed = AlertUtil.showConfirmation("Mark as Recovered",
                "Are you sure you want to mark this vehicle as recovered?");

        if (confirmed) {
            showOperationProgress(true);
            statusLabel.setText("Marking vehicle as recovered...");
            updateProgress(0.5);

            try {
                updateProgress(0.8);
                boolean success = stolenVehicleDAO.recoverVehicle(selectedStolenVehicle.getId(), LocalDate.now());

                updateProgress(1.0);

                if (success) {
                    // Log recovery
                    int currentUserId = SessionManager.getInstance().getUserId();
                    auditDAO.logAction(currentUserId, "RECOVER_STOLEN_VEHICLE: " + selectedStolenVehicle.getRegistrationNumber() +
                            " - Case: " + selectedStolenVehicle.getCaseNumber(), "127.0.0.1");

                    AlertUtil.showSuccess("Vehicle marked as recovered successfully.");
                    statusLabel.setText("Vehicle marked as recovered");
                    loadStolenVehicles();
                    clearForm();
                } else {
                    statusLabel.setText("Failed to mark as recovered");
                    AlertUtil.showError("Update Failed", "Failed to mark vehicle as recovered.");
                }

            } catch (Exception e) {
                e.printStackTrace();
                statusLabel.setText("Error: " + e.getMessage());
                AlertUtil.showError("Database Error", "An error occurred while marking as recovered.");
            } finally {
                hideProgressAfterDelay();
            }
        }
    }

    private void clearForm() {
        vehicleComboBox.getSelectionModel().clearSelection();
        caseNumberField.clear();
        assignedOfficerField.clear();
        descriptionArea.clear();
        reportedDatePicker.setValue(LocalDate.now());
        statusComboBox.setValue(null);
        selectedStolenVehicle = null;
        stolenVehiclesTable.getSelectionModel().clearSelection();
    }

    private void applyVisualEffects() {
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(5.0);
        dropShadow.setOffsetX(2.0);
        dropShadow.setOffsetY(2.0);
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.3));

        reportButton.setEffect(dropShadow);
        markRecoveredButton.setEffect(dropShadow);
        refreshButton.setEffect(dropShadow);
        backButton.setEffect(dropShadow);
        if (fadeButton != null) fadeButton.setEffect(dropShadow);
    }

    private void showFadeAnimation() {
        if (fadeButton != null) {
            FadeTransition fadeTransition = new FadeTransition(Duration.seconds(1.5), fadeButton);
            fadeTransition.setFromValue(1.0);
            fadeTransition.setToValue(0.2);
            fadeTransition.setCycleCount(4);
            fadeTransition.setAutoReverse(true);
            fadeTransition.play();
            statusLabel.setText("Animation played!");
            AlertUtil.showInfo("Animation", "Fade animation completed!");

            PauseTransition reset = new PauseTransition(Duration.seconds(2));
            reset.setOnFinished(e -> statusLabel.setText("Ready"));
            reset.play();
        }
    }

    private void showLoadProgress(boolean show) {
        if (loadProgress != null) loadProgress.setVisible(show);
    }

    private void showOperationProgress(boolean show) {
        if (operationProgress != null) {
            operationProgress.setVisible(show);
            operationProgress.setProgress(0);
        }
    }

    private void updateProgress(double progress) {
        if (operationProgress != null) operationProgress.setProgress(progress);
    }

    private void hideProgressAfterDelay() {
        PauseTransition delay = new PauseTransition(Duration.seconds(1.5));
        delay.setOnFinished(event -> {
            if (operationProgress != null) {
                operationProgress.setVisible(false);
                operationProgress.setProgress(0);
            }
            if (loadProgress != null) loadProgress.setVisible(false);
        });
        delay.play();
    }
}