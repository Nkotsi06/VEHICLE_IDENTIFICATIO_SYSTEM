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
import utils.ValidationUtil;
import dao.PoliceReportDAO;
import dao.VehicleDAO;
import models.PoliceReport;
import models.Vehicle;
import java.time.LocalDate;
import java.util.List;

public class IncidentReportController {

    @FXML private TableView<PoliceReport> reportsTable;
    @FXML private TableColumn<PoliceReport, String> caseNumberColumn;
    @FXML private TableColumn<PoliceReport, String> vehicleColumn;
    @FXML private TableColumn<PoliceReport, String> reportTypeColumn;
    @FXML private TableColumn<PoliceReport, String> reportDateColumn;
    @FXML private TableColumn<PoliceReport, String> officerColumn;

    @FXML private ComboBox<Vehicle> vehicleComboBox;
    @FXML private ComboBox<String> reportTypeComboBox;
    @FXML private TextField caseNumberField;
    @FXML private DatePicker reportDatePicker;
    @FXML private TextField locationField;
    @FXML private TextArea descriptionArea;

    @FXML private Button createButton;
    @FXML private Button updateButton;
    @FXML private Button deleteButton;
    @FXML private Button refreshButton;
    @FXML private Button backButton;
    @FXML private Button fadeButton;

    @FXML private ProgressIndicator loadProgress;
    @FXML private ProgressBar operationProgress;
    @FXML private Label statusLabel;

    private PoliceReportDAO reportDAO;
    private VehicleDAO vehicleDAO;
    private ObservableList<PoliceReport> reportList;
    private PoliceReport selectedReport;

    @FXML
    public void initialize() {
        reportDAO = new PoliceReportDAO();
        vehicleDAO = new VehicleDAO();
        reportList = FXCollections.observableArrayList();

        setupTableColumns();
        setupComboBoxes();
        setupButtonHandlers();
        applyVisualEffects();
        loadVehicles();
        loadReports();

        reportDatePicker.setValue(LocalDate.now());
        statusLabel.setText("Ready");
    }

    private void setupTableColumns() {
        caseNumberColumn.setCellValueFactory(cellData -> cellData.getValue().caseNumberProperty());
        vehicleColumn.setCellValueFactory(cellData -> cellData.getValue().registrationNumberProperty());
        reportTypeColumn.setCellValueFactory(cellData -> cellData.getValue().reportTypeProperty());
        reportDateColumn.setCellValueFactory(cellData -> cellData.getValue().reportDateProperty().asString());
        officerColumn.setCellValueFactory(cellData -> cellData.getValue().officerNameProperty());

        caseNumberColumn.setStyle("-fx-alignment: CENTER;");
        vehicleColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        reportTypeColumn.setStyle("-fx-alignment: CENTER;");
        reportDateColumn.setStyle("-fx-alignment: CENTER;");
        officerColumn.setStyle("-fx-alignment: CENTER-LEFT;");

        reportsTable.setItems(reportList);
        reportsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void setupComboBoxes() {
        reportTypeComboBox.getItems().addAll("ACCIDENT", "THEFT", "STOLEN", "RECOVERED", "VIOLATION");
    }

    private void loadVehicles() {
        try {
            List<Vehicle> vehicles = vehicleDAO.findAll();
            vehicleComboBox.getItems().setAll(vehicles);
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error loading vehicles");
        }
    }

    private void loadReports() {
        showProgress(true);
        statusLabel.setText("Loading reports...");

        try {
            List<PoliceReport> reports = reportDAO.findAll();
            reportList.setAll(reports);
            statusLabel.setText("Loaded " + reports.size() + " reports");
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error loading reports");
            AlertUtil.showError("Load Failed", "Failed to load reports.");
        } finally {
            hideProgress();
        }
    }

    private void setupButtonHandlers() {
        createButton.setOnAction(event -> handleCreate());
        updateButton.setOnAction(event -> handleUpdate());
        deleteButton.setOnAction(event -> handleDelete());
        refreshButton.setOnAction(event -> loadReports());
        backButton.setOnAction(event -> SceneManager.getInstance().switchToPoliceView());
        if (fadeButton != null) fadeButton.setOnAction(event -> showFadeAnimation());

        reportsTable.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                selectedReport = newVal;
                displayReportDetails(selectedReport);
            }
        });
    }

    private void applyVisualEffects() {
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(5.0);
        dropShadow.setOffsetX(2.0);
        dropShadow.setOffsetY(2.0);
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.3));

        createButton.setEffect(dropShadow);
        updateButton.setEffect(dropShadow);
        deleteButton.setEffect(dropShadow);
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
            PauseTransition reset = new PauseTransition(Duration.seconds(2));
            reset.setOnFinished(e -> statusLabel.setText("Ready"));
            reset.play();
        }
    }

    private void displayReportDetails(PoliceReport report) {
        try {
            Vehicle vehicle = vehicleDAO.findById(report.getVehicleId());
            vehicleComboBox.getSelectionModel().select(vehicle);
        } catch (Exception e) {
            e.printStackTrace();
        }

        reportTypeComboBox.setValue(report.getReportType());
        caseNumberField.setText(report.getCaseNumber());
        reportDatePicker.setValue(report.getReportDate());
        locationField.setText(report.getLocation() != null ? report.getLocation() : "");
        descriptionArea.setText(report.getDescription() != null ? report.getDescription() : "");
    }

    private void handleCreate() {
        Vehicle selectedVehicle = vehicleComboBox.getSelectionModel().getSelectedItem();

        if (selectedVehicle == null) {
            AlertUtil.showWarning("Validation Error", "Please select a vehicle.");
            return;
        }

        if (!ValidationUtil.isNotEmpty(caseNumberField.getText())) {
            AlertUtil.showWarning("Validation Error", "Case number is required.");
            return;
        }

        if (reportTypeComboBox.getValue() == null) {
            AlertUtil.showWarning("Validation Error", "Please select a report type.");
            return;
        }

        showOperationProgress(true);
        statusLabel.setText("Creating report...");
        updateProgress(0.3);

        try {
            PoliceReport report = new PoliceReport();
            report.setVehicleId(selectedVehicle.getId());
            report.setReportDate(reportDatePicker.getValue());
            report.setReportType(reportTypeComboBox.getValue());
            report.setDescription(descriptionArea.getText());
            report.setOfficerName(SessionManager.getInstance().getFullName());
            report.setCaseNumber(caseNumberField.getText().trim());
            report.setLocation(locationField.getText());

            updateProgress(0.6);
            boolean success = reportDAO.insert(report);

            if (success) {
                updateProgress(1.0);
                AlertUtil.showSuccess("Incident report created successfully.");
                clearForm();
                loadReports();
                statusLabel.setText("Report created successfully");
            } else {
                statusLabel.setText("Failed to create report");
                AlertUtil.showError("Create Failed", "Failed to create report.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error: " + e.getMessage());
            AlertUtil.showError("Database Error", "An error occurred while creating report.");
        } finally {
            hideProgressAfterDelay();
        }
    }

    private void handleUpdate() {
        if (selectedReport == null) {
            AlertUtil.showWarning("No Selection", "Please select a report to update.");
            return;
        }

        Vehicle selectedVehicle = vehicleComboBox.getSelectionModel().getSelectedItem();
        if (selectedVehicle == null) {
            AlertUtil.showWarning("Validation Error", "Please select a vehicle.");
            return;
        }

        showOperationProgress(true);
        statusLabel.setText("Updating report...");
        updateProgress(0.5);

        try {
            selectedReport.setVehicleId(selectedVehicle.getId());
            selectedReport.setReportDate(reportDatePicker.getValue());
            selectedReport.setReportType(reportTypeComboBox.getValue());
            selectedReport.setDescription(descriptionArea.getText());
            selectedReport.setCaseNumber(caseNumberField.getText().trim());
            selectedReport.setLocation(locationField.getText());

            updateProgress(0.8);
            boolean success = reportDAO.update(selectedReport);

            if (success) {
                updateProgress(1.0);
                AlertUtil.showSuccess("Report updated successfully.");
                loadReports();
                statusLabel.setText("Report updated");
            } else {
                statusLabel.setText("Update failed");
                AlertUtil.showError("Update Failed", "Failed to update report.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error: " + e.getMessage());
            AlertUtil.showError("Database Error", "An error occurred while updating report.");
        } finally {
            hideProgressAfterDelay();
        }
    }

    private void handleDelete() {
        if (selectedReport == null) {
            AlertUtil.showWarning("No Selection", "Please select a report to delete.");
            return;
        }

        boolean confirmed = AlertUtil.showConfirmation("Delete Report", "Delete report " + selectedReport.getCaseNumber() + "?");

        if (confirmed) {
            showOperationProgress(true);
            statusLabel.setText("Deleting report...");
            updateProgress(0.5);

            try {
                boolean success = reportDAO.delete(selectedReport.getId());
                if (success) {
                    updateProgress(1.0);
                    AlertUtil.showSuccess("Report deleted successfully.");
                    clearForm();
                    loadReports();
                    statusLabel.setText("Report deleted");
                } else {
                    statusLabel.setText("Delete failed");
                    AlertUtil.showError("Delete Failed", "Failed to delete report.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                statusLabel.setText("Error: " + e.getMessage());
            } finally {
                hideProgressAfterDelay();
            }
        }
    }

    private void clearForm() {
        vehicleComboBox.getSelectionModel().clearSelection();
        reportTypeComboBox.setValue(null);
        caseNumberField.clear();
        reportDatePicker.setValue(LocalDate.now());
        locationField.clear();
        descriptionArea.clear();
        selectedReport = null;
        reportsTable.getSelectionModel().clearSelection();
    }

    private void showProgress(boolean show) {
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

    private void hideProgress() {
        if (loadProgress != null) loadProgress.setVisible(false);
    }

    private void hideProgressAfterDelay() {
        PauseTransition delay = new PauseTransition(Duration.seconds(1));
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