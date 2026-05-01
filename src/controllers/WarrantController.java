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
import dao.WarrantDAO;
import dao.ViolationDAO;
import dao.VehicleDAO;
import models.Warrant;
import models.Violation;
import models.Vehicle;

public class WarrantController {

    @FXML private TableView<Warrant> warrantsTable;
    @FXML private TableColumn<Warrant, String> vehicleColumn;
    @FXML private TableColumn<Warrant, String> issueDateColumn;
    @FXML private TableColumn<Warrant, String> expiryDateColumn;
    @FXML private TableColumn<Warrant, String> judgeColumn;
    @FXML private TableColumn<Warrant, String> statusColumn;

    @FXML private ComboBox<Violation> violationComboBox;
    @FXML private TextField judgeNameField;
    @FXML private DatePicker issueDatePicker;
    @FXML private DatePicker expiryDatePicker;
    @FXML private TextArea notesArea;

    @FXML private Button issueButton;
    @FXML private Button executeButton;
    @FXML private Button refreshButton;
    @FXML private Button backButton;

    private WarrantDAO warrantDAO;
    private ViolationDAO violationDAO;
    private VehicleDAO vehicleDAO;
    private Warrant selectedWarrant;

    @FXML
    public void initialize() {
        warrantDAO = new WarrantDAO();
        violationDAO = new ViolationDAO();
        vehicleDAO = new VehicleDAO();

        setupTableColumns();
        loadViolations();
        loadWarrants();
        setupButtonHandlers();
        setupTableSelection();

        issueDatePicker.setValue(java.time.LocalDate.now());
        expiryDatePicker.setValue(java.time.LocalDate.now().plusMonths(3));
    }

    private void setupTableColumns() {
        vehicleColumn.setCellValueFactory(cellData -> cellData.getValue().registrationNumberProperty());
        issueDateColumn.setCellValueFactory(cellData -> cellData.getValue().issueDateProperty().asString());
        expiryDateColumn.setCellValueFactory(cellData -> cellData.getValue().expiryDateProperty().asString());
        judgeColumn.setCellValueFactory(cellData -> cellData.getValue().judgeNameProperty());
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
    }

    private void loadViolations() {
        try {
            java.util.List<Violation> violations = violationDAO.findUnpaidViolations();
            violationComboBox.getItems().setAll(violations);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadWarrants() {
        try {
            java.util.List<Warrant> warrants = warrantDAO.findAll();
            warrantsTable.getItems().setAll(warrants);
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Load Failed", "Failed to load warrants.");
        }
    }

    private void setupButtonHandlers() {
        issueButton.setOnAction(event -> handleIssue());
        executeButton.setOnAction(event -> handleExecute());
        refreshButton.setOnAction(event -> loadWarrants());
        backButton.setOnAction(event -> SceneManager.getInstance().switchToPoliceView());
    }

    private void setupTableSelection() {
        warrantsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedWarrant = newSelection;
            }
        });
    }

    private void handleIssue() {
        Violation selectedViolation = violationComboBox.getSelectionModel().getSelectedItem();

        if (selectedViolation == null) {
            AlertUtil.showWarning("Validation Error", "Please select a violation.");
            return;
        }

        if (!utils.ValidationUtil.isNotEmpty(judgeNameField.getText())) {
            AlertUtil.showWarning("Validation Error", "Judge name is required.");
            judgeNameField.requestFocus();
            return;
        }

        if (issueDatePicker.getValue() == null || expiryDatePicker.getValue() == null) {
            AlertUtil.showWarning("Validation Error", "Please select issue and expiry dates.");
            return;
        }

        try {
            boolean success = warrantDAO.issueWarrant(
                    selectedViolation.getId(),
                    judgeNameField.getText().trim(),
                    issueDatePicker.getValue(),
                    expiryDatePicker.getValue()
            );

            if (success) {
                AlertUtil.showSuccess("Warrant issued successfully.");
                clearForm();
                loadWarrants();
                loadViolations();
            } else {
                AlertUtil.showError("Issue Failed", "Failed to issue warrant.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Database Error", "An error occurred while issuing warrant.");
        }
    }

    private void handleExecute() {
        if (selectedWarrant == null) {
            AlertUtil.showWarning("No Selection", "Please select a warrant to execute.");
            return;
        }

        if (!"ACTIVE".equals(selectedWarrant.getStatus())) {
            AlertUtil.showWarning("Already Processed", "This warrant is already " + selectedWarrant.getStatus().toLowerCase());
            return;
        }

        boolean confirmed = AlertUtil.showConfirmation("Execute Warrant",
                "Execute warrant for vehicle " + selectedWarrant.getRegistrationNumber() + "?");

        if (confirmed) {
            try {
                boolean success = warrantDAO.closeWarrant(selectedWarrant.getId());

                if (success) {
                    AlertUtil.showSuccess("Warrant executed successfully.");
                    loadWarrants();
                } else {
                    AlertUtil.showError("Execution Failed", "Failed to execute warrant.");
                }

            } catch (Exception e) {
                e.printStackTrace();
                AlertUtil.showError("Database Error", "An error occurred while executing warrant.");
            }
        }
    }

    private void clearForm() {
        violationComboBox.getSelectionModel().clearSelection();
        judgeNameField.clear();
        issueDatePicker.setValue(java.time.LocalDate.now());
        expiryDatePicker.setValue(java.time.LocalDate.now().plusMonths(3));
        notesArea.clear();
        selectedWarrant = null;
        warrantsTable.getSelectionModel().clearSelection();
    }
}