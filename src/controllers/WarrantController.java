package controllers;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Duration;
import utils.AlertUtil;
import utils.SceneManager;
import utils.ValidationUtil;
import dao.WarrantDAO;
import dao.ViolationDAO;
import dao.VehicleDAO;
import models.Warrant;
import models.Violation;
import java.util.List;

/**
 * Controller for Warrant Management
 * Handles issuing and executing warrants for vehicle violations
 */
public class WarrantController {

    // ============================================
    // FXML UI COMPONENTS
    // ============================================

    @FXML private TableView<Warrant> warrantsTable;
    @FXML private TableColumn<Warrant, String> vehicleColumn;
    @FXML private TableColumn<Warrant, String> issueDateColumn;
    @FXML private TableColumn<Warrant, String> expiryDateColumn;
    @FXML private TableColumn<Warrant, String> judgeColumn;
    @FXML private TableColumn<Warrant, String> warrantStatusColumn;

    @FXML private ComboBox<Violation> violationComboBox;
    @FXML private TextField judgeNameField;
    @FXML private DatePicker issueDatePicker;
    @FXML private DatePicker expiryDatePicker;
    @FXML private TextArea notesArea;

    @FXML private Button issueButton;
    @FXML private Button executeButton;
    @FXML private Button refreshButton;
    @FXML private Button backButton;

    @FXML private ProgressIndicator loadProgress;
    @FXML private ProgressBar operationProgress;
    @FXML private Pagination warrantsPagination;
    @FXML private Label statusLabel;

    // ============================================
    // DAO INSTANCES & DATA MODELS
    // ============================================

    private WarrantDAO warrantDAO;
    private ViolationDAO violationDAO;
    private VehicleDAO vehicleDAO;
    private Warrant selectedWarrant;
    private List<Warrant> fullWarrantList;
    private int currentPage = 0;
    private int pageSize = 20;

    // ============================================
    // INITIALIZATION METHODS
    // ============================================

    /**
     * Initializes the controller - sets up DAOs, table columns, loads data
     * Called automatically after FXML is loaded
     */
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
        setupPagination();

        // Set default dates
        issueDatePicker.setValue(java.time.LocalDate.now());
        expiryDatePicker.setValue(java.time.LocalDate.now().plusMonths(3));
        statusLabel.setText("Ready");
    }

    /**
     * Configures table columns with cell value factories
     */
    private void setupTableColumns() {
        vehicleColumn.setCellValueFactory(cellData -> cellData.getValue().registrationNumberProperty());
        issueDateColumn.setCellValueFactory(cellData -> cellData.getValue().issueDateProperty().asString());
        expiryDateColumn.setCellValueFactory(cellData -> cellData.getValue().expiryDateProperty().asString());
        judgeColumn.setCellValueFactory(cellData -> cellData.getValue().judgeNameProperty());
        warrantStatusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
    }

    /**
     * Configures pagination for the warrants table
     */
    private void setupPagination() {
        if (warrantsPagination != null) {
            warrantsPagination.currentPageIndexProperty().addListener((obs, old, newPage) -> {
                currentPage = newPage.intValue();
                updateTablePage();
            });
        }
    }

    /**
     * Updates the table to show current page of warrants
     */
    private void updateTablePage() {
        if (fullWarrantList == null || fullWarrantList.isEmpty()) return;
        int start = currentPage * pageSize;
        int end = Math.min(start + pageSize, fullWarrantList.size());
        if (start < fullWarrantList.size()) {
            warrantsTable.getItems().setAll(fullWarrantList.subList(start, end));
        }
    }

    // ============================================
    // DATA LOADING METHODS
    // ============================================

    /**
     * Loads all unpaid violations into the combo box
     */
    private void loadViolations() {
        try {
            List<Violation> violations = violationDAO.findUnpaidViolations();
            violationComboBox.getItems().setAll(violations);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads all warrants from database with progress indicator
     */
    private void loadWarrants() {
        showProgress(true);
        statusLabel.setText("Loading warrants...");

        try {
            fullWarrantList = warrantDAO.findAll();
            int totalPages = (int) Math.ceil((double) fullWarrantList.size() / pageSize);
            if (warrantsPagination != null) warrantsPagination.setPageCount(Math.max(1, totalPages));
            updateTablePage();
            statusLabel.setText("Loaded " + fullWarrantList.size() + " warrants");
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Load Failed", "Failed to load warrants.");
            statusLabel.setText("Error loading warrants");
        } finally {
            hideProgressAfterDelay();
        }
    }

    // ============================================
    // EVENT HANDLERS
    // ============================================

    /**
     * Sets up button click handlers
     */
    private void setupButtonHandlers() {
        issueButton.setOnAction(event -> handleIssue());
        executeButton.setOnAction(event -> handleExecute());
        refreshButton.setOnAction(event -> loadWarrants());
        backButton.setOnAction(event -> SceneManager.getInstance().switchToPoliceView());
    }

    /**
     * Sets up table selection listener to display selected warrant details
     */
    private void setupTableSelection() {
        warrantsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedWarrant = newSelection;
            }
        });
    }

    // ============================================
    // BUSINESS LOGIC METHODS
    // ============================================

    /**
     * Handles issuing a new warrant for a selected violation
     */
    private void handleIssue() {
        Violation selectedViolation = violationComboBox.getSelectionModel().getSelectedItem();

        // Validation checks
        if (selectedViolation == null) {
            AlertUtil.showWarning("Validation Error", "Please select a violation.");
            return;
        }

        if (!ValidationUtil.isNotEmpty(judgeNameField.getText())) {
            AlertUtil.showWarning("Validation Error", "Judge name is required.");
            judgeNameField.requestFocus();
            return;
        }

        if (issueDatePicker.getValue() == null || expiryDatePicker.getValue() == null) {
            AlertUtil.showWarning("Validation Error", "Please select issue and expiry dates.");
            return;
        }

        showProgress(true);
        statusLabel.setText("Issuing warrant...");

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
                statusLabel.setText("Warrant issued successfully");
            } else {
                AlertUtil.showError("Issue Failed", "Failed to issue warrant.");
                statusLabel.setText("Issue failed");
            }

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Database Error", "An error occurred while issuing warrant.");
            statusLabel.setText("Error: " + e.getMessage());
        } finally {
            hideProgressAfterDelay();
        }
    }

    /**
     * Handles executing an active warrant
     */
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
            showProgress(true);
            statusLabel.setText("Executing warrant...");

            try {
                boolean success = warrantDAO.closeWarrant(selectedWarrant.getId());

                if (success) {
                    AlertUtil.showSuccess("Warrant executed successfully.");
                    loadWarrants();
                    statusLabel.setText("Warrant executed");
                } else {
                    AlertUtil.showError("Execution Failed", "Failed to execute warrant.");
                    statusLabel.setText("Execution failed");
                }

            } catch (Exception e) {
                e.printStackTrace();
                AlertUtil.showError("Database Error", "An error occurred while executing warrant.");
                statusLabel.setText("Error: " + e.getMessage());
            } finally {
                hideProgressAfterDelay();
            }
        }
    }

    /**
     * Clears all form fields and resets selections
     */
    private void clearForm() {
        violationComboBox.getSelectionModel().clearSelection();
        judgeNameField.clear();
        issueDatePicker.setValue(java.time.LocalDate.now());
        expiryDatePicker.setValue(java.time.LocalDate.now().plusMonths(3));
        notesArea.clear();
        selectedWarrant = null;
        warrantsTable.getSelectionModel().clearSelection();
    }

    // ============================================
    // UI PROGRESS METHODS
    // ============================================

    /**
     * Shows/hides progress indicators
     * @param show true to show, false to hide
     */
    private void showProgress(boolean show) {
        if (loadProgress != null) loadProgress.setVisible(show);
        if (operationProgress != null) {
            operationProgress.setVisible(show);
            operationProgress.setProgress(0);
        }
    }

    /**
     * Hides progress indicators after a short delay
     */
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