package controllers;

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
import utils.CurrencyUtil;
import dao.InsurancePolicyDAO;
import dao.InsuranceProviderDAO;
import dao.VehicleDAO;
import models.InsurancePolicy;
import models.InsuranceProvider;
import models.Vehicle;

import java.time.LocalDate;
import java.util.List;

public class InsurancePolicyController {

    @FXML private TableView<InsurancePolicy> policiesTable;
    @FXML private TableColumn<InsurancePolicy, String> policyNumberColumn;
    @FXML private TableColumn<InsurancePolicy, String> vehicleColumn;
    @FXML private TableColumn<InsurancePolicy, String> providerColumn;
    @FXML private TableColumn<InsurancePolicy, String> startDateColumn;
    @FXML private TableColumn<InsurancePolicy, String> endDateColumn;
    @FXML private TableColumn<InsurancePolicy, String> statusColumn;
    @FXML private TableColumn<InsurancePolicy, Double> premiumColumn;

    @FXML private ComboBox<Vehicle> vehicleComboBox;
    @FXML private ComboBox<InsuranceProvider> providerComboBox;
    @FXML private TextField policyNumberField;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TextField premiumField;
    @FXML private TextField coverageAmountField;
    @FXML private ComboBox<String> statusComboBox;

    @FXML private Label vehicleInfoLabel;
    @FXML private Label providerInfoLabel;
    @FXML private Label statusLabel;

    @FXML private Button addButton;
    @FXML private Button updateButton;
    @FXML private Button deleteButton;
    @FXML private Button renewButton;
    @FXML private Button viewClaimsButton;
    @FXML private Button verifyButton;
    @FXML private Button refreshButton;
    @FXML private Button backButton;
    @FXML private Button clearButton;
    @FXML private Button fadeButton;

    @FXML private ProgressIndicator loadProgress;
    @FXML private ProgressBar operationProgress;
    @FXML private Pagination policiesPagination;

    private InsurancePolicyDAO policyDAO;
    private InsuranceProviderDAO providerDAO;
    private VehicleDAO vehicleDAO;
    private InsurancePolicy selectedPolicy;
    private ObservableList<InsurancePolicy> policyList;
    private int currentPage = 0;
    private int pageSize = 10;

    @FXML
    public void initialize() {
        policyDAO = new InsurancePolicyDAO();
        providerDAO = new InsuranceProviderDAO();
        vehicleDAO = new VehicleDAO();
        policyList = FXCollections.observableArrayList();

        setupTableColumns();
        loadComboBoxes();
        loadPolicies();
        setupButtonHandlers();
        setupTableSelection();
        setupPagination();
        applyVisualEffects();

        startDatePicker.setValue(LocalDate.now());
        endDatePicker.setValue(LocalDate.now().plusYears(1));
        statusComboBox.getItems().addAll("ACTIVE", "EXPIRED", "CANCELLED", "PENDING");
        statusLabel.setText("Ready");

        vehicleComboBox.setOnAction(e -> updateVehicleInfo());
        providerComboBox.setOnAction(e -> updateProviderInfo());
    }

    private void setupTableColumns() {
        policyNumberColumn.setCellValueFactory(cellData -> cellData.getValue().policyNumberProperty());
        vehicleColumn.setCellValueFactory(cellData -> cellData.getValue().registrationNumberProperty());
        providerColumn.setCellValueFactory(cellData -> cellData.getValue().providerNameProperty());
        startDateColumn.setCellValueFactory(cellData -> cellData.getValue().startDateProperty().asString());
        endDateColumn.setCellValueFactory(cellData -> cellData.getValue().endDateProperty().asString());
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        premiumColumn.setCellValueFactory(cellData -> cellData.getValue().premiumProperty().asObject());

        policyNumberColumn.setStyle("-fx-alignment: CENTER;");
        vehicleColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        providerColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        startDateColumn.setStyle("-fx-alignment: CENTER;");
        endDateColumn.setStyle("-fx-alignment: CENTER;");
        statusColumn.setStyle("-fx-alignment: CENTER;");
        premiumColumn.setStyle("-fx-alignment: CENTER-RIGHT;");
    }

    private void setupPagination() {
        if (policiesPagination != null) {
            policiesPagination.setPageCount(1);
            policiesPagination.setMaxPageIndicatorCount(5);
            policiesPagination.currentPageIndexProperty().addListener((obs, old, newPage) -> {
                currentPage = newPage.intValue();
                updateTablePage();
            });
        }
    }

    private void updateTablePage() {
        int start = currentPage * pageSize;
        int end = Math.min(start + pageSize, policyList.size());
        if (start < policyList.size()) {
            policiesTable.setItems(FXCollections.observableArrayList(policyList.subList(start, end)));
        }
    }

    private void loadComboBoxes() {
        showProgress(true);
        try {
            List<Vehicle> vehicles = vehicleDAO.findAll();
            vehicleComboBox.getItems().setAll(vehicles);

            List<InsuranceProvider> providers = providerDAO.findAll();
            providerComboBox.getItems().setAll(providers);
            statusLabel.setText("Loaded " + vehicles.size() + " vehicles and " + providers.size() + " providers");
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error loading data");
        } finally {
            showProgress(false);
        }
    }

    private void loadPolicies() {
        showProgress(true);
        statusLabel.setText("Loading policies...");

        try {
            String role = SessionManager.getInstance().getUserRole();
            List<InsurancePolicy> policies;

            if ("CUSTOMER".equals(role)) {
                int customerId = SessionManager.getInstance().getCustomerId();
                List<Vehicle> customerVehicles = vehicleDAO.findByOwnerId(customerId);
                policies = new java.util.ArrayList<>();

                for (InsurancePolicy policy : policyDAO.findAll()) {
                    for (Vehicle v : customerVehicles) {
                        if (policy.getVehicleId() == v.getId()) {
                            policies.add(policy);
                            break;
                        }
                    }
                }
            } else {
                policies = policyDAO.findAll();
            }

            policyList.setAll(policies);
            int totalPages = (int) Math.ceil((double) policies.size() / pageSize);
            if (policiesPagination != null) policiesPagination.setPageCount(Math.max(1, totalPages));
            updateTablePage();

            statusLabel.setText("Loaded " + policies.size() + " policies");
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error loading policies");
            AlertUtil.showError("Load Failed", "Failed to load policies.");
        } finally {
            showProgress(false);
        }
    }

    private void setupButtonHandlers() {
        addButton.setOnAction(event -> handleAdd());
        updateButton.setOnAction(event -> handleUpdate());
        deleteButton.setOnAction(event -> handleDelete());
        renewButton.setOnAction(event -> handleRenew());
        viewClaimsButton.setOnAction(event -> SceneManager.getInstance().switchToInsuranceClaimView());
        verifyButton.setOnAction(event -> SceneManager.getInstance().switchToInsuranceVerificationView());
        refreshButton.setOnAction(event -> loadPolicies());
        backButton.setOnAction(event -> handleBack());
        if (clearButton != null) clearButton.setOnAction(event -> clearForm());
        if (fadeButton != null) fadeButton.setOnAction(event -> showFadeAnimation());
    }

    private void showFadeAnimation() {
        if (fadeButton != null) {
            javafx.animation.FadeTransition fadeTransition = new javafx.animation.FadeTransition(Duration.seconds(1.5), fadeButton);
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

    private void applyVisualEffects() {
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(5.0);
        dropShadow.setOffsetX(2.0);
        dropShadow.setOffsetY(2.0);
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.3));

        addButton.setEffect(dropShadow);
        updateButton.setEffect(dropShadow);
        deleteButton.setEffect(dropShadow);
        renewButton.setEffect(dropShadow);
        refreshButton.setEffect(dropShadow);
        backButton.setEffect(dropShadow);
        if (clearButton != null) clearButton.setEffect(dropShadow);
    }

    private void setupTableSelection() {
        policiesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedPolicy = newSelection;
                displayPolicyDetails(selectedPolicy);
            }
        });
    }

    private void updateVehicleInfo() {
        Vehicle selected = vehicleComboBox.getSelectionModel().getSelectedItem();
        if (selected != null) {
            vehicleInfoLabel.setText(selected.getMake() + " " + selected.getModel() +
                    " (" + selected.getYear() + ") - " + selected.getRegistrationNumber());
        }
    }

    private void updateProviderInfo() {
        InsuranceProvider selected = providerComboBox.getSelectionModel().getSelectedItem();
        if (selected != null) {
            providerInfoLabel.setText("Rating: " + (selected.getRating() != null ? selected.getRating() : "N/A") +
                    " | Status: " + (selected.getStatus() != null ? selected.getStatus() : "ACTIVE"));
        }
    }

    private void displayPolicyDetails(InsurancePolicy policy) {
        try {
            Vehicle vehicle = vehicleDAO.findById(policy.getVehicleId());
            vehicleComboBox.getSelectionModel().select(vehicle);
            updateVehicleInfo();

            InsuranceProvider provider = providerDAO.findById(policy.getProviderId());
            providerComboBox.getSelectionModel().select(provider);
            updateProviderInfo();
        } catch (Exception e) {
            e.printStackTrace();
        }

        policyNumberField.setText(policy.getPolicyNumber());
        startDatePicker.setValue(policy.getStartDate());
        endDatePicker.setValue(policy.getEndDate());
        premiumField.setText(String.valueOf(policy.getPremium()));
        coverageAmountField.setText(String.valueOf(policy.getCoverageAmount()));
        statusComboBox.setValue(policy.getStatus());
    }

    private void handleAdd() {
        if (!validateInputs()) return;

        showProgress(true);
        statusLabel.setText("Adding policy...");
        updateProgress(0.3);

        try {
            Vehicle selectedVehicle = vehicleComboBox.getValue();
            InsuranceProvider selectedProvider = providerComboBox.getValue();

            InsurancePolicy policy = new InsurancePolicy();
            policy.setVehicleId(selectedVehicle.getId());
            policy.setProviderId(selectedProvider.getId());
            policy.setPolicyNumber(policyNumberField.getText().trim());
            policy.setStartDate(startDatePicker.getValue());
            policy.setEndDate(endDatePicker.getValue());
            policy.setPremium(Double.parseDouble(premiumField.getText()));
            policy.setCoverageAmount(Double.parseDouble(coverageAmountField.getText()));
            policy.setStatus(statusComboBox.getValue());

            updateProgress(0.6);
            boolean success = policyDAO.insert(policy);

            if (success) {
                updateProgress(1.0);
                AlertUtil.showSuccess("Insurance policy added successfully.");
                clearForm();
                loadPolicies();
                statusLabel.setText("Policy added successfully");
            } else {
                AlertUtil.showError("Add Failed", "Failed to add policy.");
                statusLabel.setText("Failed to add policy");
            }
        } catch (NumberFormatException e) {
            AlertUtil.showError("Invalid Input", "Please enter valid numeric values.");
            statusLabel.setText("Invalid input");
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error: " + e.getMessage());
            AlertUtil.showError("Database Error", "An error occurred while adding policy.");
        } finally {
            hideProgressAfterDelay();
        }
    }

    private void handleUpdate() {
        if (selectedPolicy == null) {
            AlertUtil.showWarning("No Selection", "Please select a policy to update.");
            return;
        }

        if (!validateInputs()) return;

        showProgress(true);
        statusLabel.setText("Updating policy...");
        updateProgress(0.3);

        try {
            selectedPolicy.setVehicleId(vehicleComboBox.getValue().getId());
            selectedPolicy.setProviderId(providerComboBox.getValue().getId());
            selectedPolicy.setPolicyNumber(policyNumberField.getText().trim());
            selectedPolicy.setStartDate(startDatePicker.getValue());
            selectedPolicy.setEndDate(endDatePicker.getValue());
            selectedPolicy.setPremium(Double.parseDouble(premiumField.getText()));
            selectedPolicy.setCoverageAmount(Double.parseDouble(coverageAmountField.getText()));
            selectedPolicy.setStatus(statusComboBox.getValue());

            updateProgress(0.6);
            boolean success = policyDAO.update(selectedPolicy);

            if (success) {
                updateProgress(1.0);
                AlertUtil.showSuccess("Policy updated successfully.");
                loadPolicies();
                statusLabel.setText("Policy updated successfully");
            } else {
                AlertUtil.showError("Update Failed", "Failed to update policy.");
                statusLabel.setText("Update failed");
            }
        } catch (NumberFormatException e) {
            AlertUtil.showError("Invalid Input", "Please enter valid numeric values.");
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error: " + e.getMessage());
            AlertUtil.showError("Database Error", "An error occurred while updating policy.");
        } finally {
            hideProgressAfterDelay();
        }
    }

    private void handleDelete() {
        if (selectedPolicy == null) {
            AlertUtil.showWarning("No Selection", "Please select a policy to delete.");
            return;
        }

        boolean confirmed = AlertUtil.showConfirmation("Delete Policy",
                "Delete policy " + selectedPolicy.getPolicyNumber() + "?\nThis will also delete all associated claims.");

        if (confirmed) {
            showProgress(true);
            statusLabel.setText("Deleting policy...");
            updateProgress(0.5);

            try {
                boolean success = policyDAO.delete(selectedPolicy.getId());
                if (success) {
                    updateProgress(1.0);
                    AlertUtil.showSuccess("Policy deleted successfully.");
                    clearForm();
                    loadPolicies();
                    statusLabel.setText("Policy deleted");
                } else {
                    AlertUtil.showError("Delete Failed", "Failed to delete policy.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                statusLabel.setText("Error: " + e.getMessage());
                AlertUtil.showError("Database Error", "An error occurred while deleting policy.");
            } finally {
                hideProgressAfterDelay();
            }
        }
    }

    private void handleRenew() {
        if (selectedPolicy == null) {
            AlertUtil.showWarning("No Selection", "Please select a policy to renew.");
            return;
        }
        SceneManager.getInstance().switchToPolicyRenewalView();
    }

    private void handleBack() {
        String role = SessionManager.getInstance().getUserRole();
        if ("CUSTOMER".equals(role)) {
            SceneManager.getInstance().switchToCustomerProfileView();
        } else {
            SceneManager.getInstance().switchToDashboard();
        }
    }

    private void clearForm() {
        vehicleComboBox.getSelectionModel().clearSelection();
        providerComboBox.getSelectionModel().clearSelection();
        policyNumberField.clear();
        startDatePicker.setValue(LocalDate.now());
        endDatePicker.setValue(LocalDate.now().plusYears(1));
        premiumField.clear();
        coverageAmountField.clear();
        statusComboBox.setValue(null);
        selectedPolicy = null;
        policiesTable.getSelectionModel().clearSelection();
        vehicleInfoLabel.setText("");
        providerInfoLabel.setText("");
        statusLabel.setText("Form cleared");
    }

    private boolean validateInputs() {
        if (vehicleComboBox.getValue() == null) {
            AlertUtil.showWarning("Validation Error", "Please select a vehicle.");
            return false;
        }
        if (providerComboBox.getValue() == null) {
            AlertUtil.showWarning("Validation Error", "Please select a provider.");
            return false;
        }
        if (!ValidationUtil.isNotEmpty(policyNumberField.getText())) {
            AlertUtil.showWarning("Validation Error", "Policy number is required.");
            policyNumberField.requestFocus();
            return false;
        }
        if (startDatePicker.getValue() == null) {
            AlertUtil.showWarning("Validation Error", "Start date is required.");
            return false;
        }
        if (endDatePicker.getValue() == null) {
            AlertUtil.showWarning("Validation Error", "End date is required.");
            return false;
        }
        if (startDatePicker.getValue().isAfter(endDatePicker.getValue())) {
            AlertUtil.showWarning("Validation Error", "Start date must be before end date.");
            return false;
        }
        if (!ValidationUtil.isNotEmpty(premiumField.getText())) {
            AlertUtil.showWarning("Validation Error", "Premium amount is required.");
            premiumField.requestFocus();
            return false;
        }
        try {
            double premium = Double.parseDouble(premiumField.getText());
            if (premium <= 0) {
                AlertUtil.showWarning("Validation Error", "Premium must be greater than 0.");
                return false;
            }
        } catch (NumberFormatException e) {
            AlertUtil.showWarning("Validation Error", "Please enter a valid premium amount.");
            return false;
        }
        return true;
    }

    private void showProgress(boolean show) {
        if (loadProgress != null) loadProgress.setVisible(show);
    }

    private void updateProgress(double progress) {
        if (operationProgress != null) operationProgress.setProgress(progress);
    }

    private void hideProgressAfterDelay() {
        PauseTransition delay = new PauseTransition(Duration.seconds(1));
        delay.setOnFinished(event -> {
            if (operationProgress != null) operationProgress.setVisible(false);
            if (loadProgress != null) loadProgress.setVisible(false);
        });
        delay.play();
    }
}