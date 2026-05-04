package controllers;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Duration;
import utils.AlertUtil;
import utils.ValidationUtil;
import utils.SessionManager;
import utils.SceneManager;
import dao.ViolationDAO;
import dao.VehicleDAO;
import dao.AuditDAO;
import models.Violation;
import models.Vehicle;

/**
 * Controller for Violation Management
 * Handles creating, updating, deleting, and marking violations as paid
 */
public class ViolationController {

    // ============================================
    // FXML UI COMPONENTS
    // ============================================

    // Table Components
    @FXML private TableView<Violation> violationsTable;
    @FXML private TableColumn<Violation, String> regNumberColumn;
    @FXML private TableColumn<Violation, String> violationTypeColumn;
    @FXML private TableColumn<Violation, String> violationDateColumn;
    @FXML private TableColumn<Violation, Double> fineAmountColumn;
    @FXML private TableColumn<Violation, String> paymentStatusColumn;
    @FXML private TableColumn<Violation, String> officerColumn;

    // Form Components
    @FXML private ComboBox<Vehicle> vehicleComboBox;
    @FXML private TextField violationTypeField;
    @FXML private TextField fineAmountField;
    @FXML private TextField locationField;
    @FXML private DatePicker violationDatePicker;
    @FXML private ComboBox<String> paymentStatusComboBox;
    @FXML private TextArea descriptionArea;

    // Buttons
    @FXML private Button addButton;
    @FXML private Button updateButton;
    @FXML private Button deleteButton;
    @FXML private Button markPaidButton;
    @FXML private Button refreshButton;
    @FXML private Button backButton;
    @FXML private Button fadeButton;

    // Progress Indicators
    @FXML private ProgressIndicator loadProgress;
    @FXML private ProgressBar operationProgress;
    @FXML private Label statusLabel;

    // ============================================
    // DAO INSTANCES & DATA MODELS
    // ============================================

    private ViolationDAO violationDAO;
    private VehicleDAO vehicleDAO;
    private AuditDAO auditDAO;
    private Violation selectedViolation;

    // ============================================
    // INITIALIZATION METHODS
    // ============================================

    /**
     * Initializes the controller - sets up DAOs, loads data, configures UI
     */
    @FXML
    public void initialize() {
        violationDAO = new ViolationDAO();
        vehicleDAO = new VehicleDAO();
        auditDAO = new AuditDAO();

        setupTableColumns();
        loadComboBoxes();
        loadViolations();
        setupButtonHandlers();
        setupTableSelection();

        // Configure combo box items
        paymentStatusComboBox.getItems().setAll("UNPAID", "PAID", "DISPUTED");
        violationDatePicker.setValue(java.time.LocalDate.now());
        statusLabel.setText("Ready");
    }

    /**
     * Configures table columns with cell value factories
     */
    private void setupTableColumns() {
        regNumberColumn.setCellValueFactory(cellData -> cellData.getValue().registrationNumberProperty());
        violationTypeColumn.setCellValueFactory(cellData -> cellData.getValue().violationTypeProperty());
        violationDateColumn.setCellValueFactory(cellData -> cellData.getValue().violationDateProperty().asString());
        fineAmountColumn.setCellValueFactory(cellData -> cellData.getValue().fineAmountProperty().asObject());
        paymentStatusColumn.setCellValueFactory(cellData -> cellData.getValue().paymentStatusProperty());
        officerColumn.setCellValueFactory(cellData -> cellData.getValue().officerNameProperty());
    }

    /**
     * Loads all vehicles into the combo box
     */
    private void loadComboBoxes() {
        try {
            java.util.List<Vehicle> vehicles = vehicleDAO.findAll();
            vehicleComboBox.getItems().setAll(vehicles);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads all violations from the database
     */
    private void loadViolations() {
        try {
            java.util.List<Violation> violations = violationDAO.findAll();
            violationsTable.getItems().setAll(violations);
            statusLabel.setText("Loaded " + violations.size() + " violations");
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error loading violations");
            AlertUtil.showError("Load Failed", "Failed to load violations.");
        }
    }

    // ============================================
    // EVENT HANDLERS
    // ============================================

    /**
     * Sets up button click handlers with animations
     */
    private void setupButtonHandlers() {
        addButton.setOnAction(event -> handleAdd());
        updateButton.setOnAction(event -> handleUpdate());
        deleteButton.setOnAction(event -> handleDelete());
        markPaidButton.setOnAction(event -> handleMarkPaid());
        refreshButton.setOnAction(event -> loadViolations());
        backButton.setOnAction(event -> SceneManager.getInstance().switchToPoliceView());

        if (fadeButton != null) {
            fadeButton.setOnAction(event -> showFadeAnimation());
        }
    }

    /**
     * Sets up table selection listener to populate form when row is selected
     */
    private void setupTableSelection() {
        violationsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedViolation = newSelection;
                displayViolationDetails(selectedViolation);
            }
        });
    }

    /**
     * Plays fade animation on the fade button
     */
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

    // ============================================
    // FORM HANDLING METHODS
    // ============================================

    /**
     * Displays selected violation details in the form
     * @param violation The violation to display
     */
    private void displayViolationDetails(Violation violation) {
        try {
            Vehicle vehicle = vehicleDAO.findById(violation.getVehicleId());
            vehicleComboBox.getSelectionModel().select(vehicle);
        } catch (Exception e) {
            e.printStackTrace();
        }

        violationTypeField.setText(violation.getViolationType());
        fineAmountField.setText(String.valueOf(violation.getFineAmount()));
        locationField.setText(violation.getLocation());
        violationDatePicker.setValue(violation.getViolationDate());
        paymentStatusComboBox.setValue(violation.getPaymentStatus());
        descriptionArea.setText(violation.getDescription());
    }

    /**
     * Clears all form fields
     */
    private void clearForm() {
        vehicleComboBox.getSelectionModel().clearSelection();
        violationTypeField.clear();
        fineAmountField.clear();
        locationField.clear();
        violationDatePicker.setValue(java.time.LocalDate.now());
        paymentStatusComboBox.setValue(null);
        descriptionArea.clear();
        selectedViolation = null;
        violationsTable.getSelectionModel().clearSelection();
    }

    // ============================================
    // BUSINESS LOGIC METHODS
    // ============================================

    /**
     * Handles adding a new violation
     */
    private void handleAdd() {
        Vehicle selectedVehicle = vehicleComboBox.getSelectionModel().getSelectedItem();

        // Input validation
        if (selectedVehicle == null) {
            AlertUtil.showWarning("Validation Error", "Please select a vehicle.");
            return;
        }

        if (!ValidationUtil.isNotEmpty(violationTypeField.getText())) {
            AlertUtil.showWarning("Validation Error", "Violation type is required.");
            violationTypeField.requestFocus();
            return;
        }

        if (!ValidationUtil.isNotEmpty(fineAmountField.getText())) {
            AlertUtil.showWarning("Validation Error", "Fine amount is required.");
            fineAmountField.requestFocus();
            return;
        }

        try {
            double fineAmount = Double.parseDouble(fineAmountField.getText());

            if (fineAmount <= 0) {
                AlertUtil.showWarning("Validation Error", "Fine amount must be greater than 0.");
                fineAmountField.requestFocus();
                return;
            }

            // Create violation object
            Violation violation = new Violation();
            violation.setVehicleId(selectedVehicle.getId());
            violation.setViolationDate(violationDatePicker.getValue());
            violation.setViolationType(violationTypeField.getText().trim());
            violation.setFineAmount(fineAmount);
            violation.setLocation(locationField.getText().trim());
            violation.setOfficerName(SessionManager.getInstance().getFullName());
            violation.setPaymentStatus("UNPAID");
            violation.setDescription(descriptionArea.getText());

            boolean success = violationDAO.insert(violation);

            if (success) {
                // Log the action
                int currentUserId = SessionManager.getInstance().getUserId();
                auditDAO.logAction(currentUserId, "CREATE_VIOLATION: " + violationTypeField.getText().trim() +
                        " for vehicle " + selectedVehicle.getRegistrationNumber() + " - Fine: M" + fineAmount, "127.0.0.1");

                AlertUtil.showSuccess("Violation added successfully.");
                clearForm();
                loadViolations();
                statusLabel.setText("Violation added successfully");
            } else {
                AlertUtil.showError("Add Failed", "Failed to add violation.");
                statusLabel.setText("Failed to add violation");
            }

        } catch (NumberFormatException e) {
            AlertUtil.showError("Invalid Input", "Please enter a valid fine amount.");
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Database Error", "An error occurred while adding the violation.");
        }
    }

    /**
     * Handles updating an existing violation
     */
    private void handleUpdate() {
        if (selectedViolation == null) {
            AlertUtil.showWarning("No Selection", "Please select a violation to update.");
            return;
        }

        Vehicle selectedVehicle = vehicleComboBox.getSelectionModel().getSelectedItem();

        if (selectedVehicle == null) {
            AlertUtil.showWarning("Validation Error", "Please select a vehicle.");
            return;
        }

        try {
            String oldType = selectedViolation.getViolationType();
            double oldFine = selectedViolation.getFineAmount();

            // Update violation object
            selectedViolation.setVehicleId(selectedVehicle.getId());
            selectedViolation.setViolationDate(violationDatePicker.getValue());
            selectedViolation.setViolationType(violationTypeField.getText().trim());
            selectedViolation.setFineAmount(Double.parseDouble(fineAmountField.getText()));
            selectedViolation.setLocation(locationField.getText().trim());
            selectedViolation.setPaymentStatus(paymentStatusComboBox.getValue());
            selectedViolation.setDescription(descriptionArea.getText());

            boolean success = violationDAO.update(selectedViolation);

            if (success) {
                // Log the update
                int currentUserId = SessionManager.getInstance().getUserId();
                auditDAO.logAction(currentUserId, "UPDATE_VIOLATION ID:" + selectedViolation.getId() +
                        " - " + oldType + " -> " + selectedViolation.getViolationType() +
                        " (Fine: " + oldFine + " -> " + selectedViolation.getFineAmount() + ")", "127.0.0.1");

                AlertUtil.showSuccess("Violation updated successfully.");
                loadViolations();
                statusLabel.setText("Violation updated successfully");
                clearForm();
            } else {
                AlertUtil.showError("Update Failed", "Failed to update violation.");
            }

        } catch (NumberFormatException e) {
            AlertUtil.showError("Invalid Input", "Please enter a valid fine amount.");
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Database Error", "An error occurred while updating the violation.");
        }
    }

    /**
     * Handles deleting a violation
     */
    private void handleDelete() {
        if (selectedViolation == null) {
            AlertUtil.showWarning("No Selection", "Please select a violation to delete.");
            return;
        }

        boolean confirmed = AlertUtil.showConfirmation("Delete Violation",
                "Are you sure you want to delete this violation?");

        if (confirmed) {
            try {
                int violationId = selectedViolation.getId();
                String violationType = selectedViolation.getViolationType();

                boolean success = violationDAO.delete(violationId);

                if (success) {
                    // Log the deletion
                    int currentUserId = SessionManager.getInstance().getUserId();
                    auditDAO.logAction(currentUserId, "DELETE_VIOLATION ID:" + violationId + " - " + violationType, "127.0.0.1");

                    AlertUtil.showSuccess("Violation deleted successfully.");
                    clearForm();
                    loadViolations();
                    statusLabel.setText("Violation deleted successfully");
                } else {
                    AlertUtil.showError("Delete Failed", "Failed to delete violation.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                AlertUtil.showError("Database Error", "An error occurred while deleting the violation.");
            }
        }
    }

    /**
     * Handles marking a violation as paid
     */
    private void handleMarkPaid() {
        if (selectedViolation == null) {
            AlertUtil.showWarning("No Selection", "Please select a violation to mark as paid.");
            return;
        }

        if ("PAID".equals(selectedViolation.getPaymentStatus())) {
            AlertUtil.showWarning("Already Paid", "This violation is already marked as paid.");
            return;
        }

        boolean confirmed = AlertUtil.showConfirmation("Mark as Paid",
                "Are you sure you want to mark this violation as paid?");

        if (confirmed) {
            try {
                boolean success = violationDAO.markAsPaid(selectedViolation.getId());

                if (success) {
                    // Log the payment
                    int currentUserId = SessionManager.getInstance().getUserId();
                    auditDAO.logAction(currentUserId, "MARK_VIOLATION_PAID ID:" + selectedViolation.getId() +
                            " - Fine: M" + selectedViolation.getFineAmount(), "127.0.0.1");

                    AlertUtil.showSuccess("Violation marked as paid successfully.");
                    loadViolations();
                    statusLabel.setText("Violation marked as paid");
                    clearForm();
                } else {
                    AlertUtil.showError("Update Failed", "Failed to mark violation as paid.");
                }

            } catch (Exception e) {
                e.printStackTrace();
                AlertUtil.showError("Database Error", "An error occurred while updating.");
            }
        }
    }
}