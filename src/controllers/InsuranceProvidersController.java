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
import dao.InsuranceProviderDAO;
import dao.InsurancePolicyDAO;
import dao.InsuranceClaimDAO;
import models.InsuranceProvider;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InsuranceProvidersController {

    private static final Logger LOGGER = Logger.getLogger(InsuranceProvidersController.class.getName());

    @FXML private TableView<InsuranceProvider> providersTable;
    @FXML private TableColumn<InsuranceProvider, String> nameColumn;
    @FXML private TableColumn<InsuranceProvider, Double> ratingColumn;
    @FXML private TableColumn<InsuranceProvider, String> phoneColumn;
    @FXML private TableColumn<InsuranceProvider, String> emailColumn;
    @FXML private TableColumn<InsuranceProvider, String> statusColumn;

    @FXML private TextField nameField;
    @FXML private TextField registrationNumberField;
    @FXML private TextField licenseNumberField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private TextArea addressField;
    @FXML private TextArea coverageDetailsField;
    @FXML private ComboBox<String> ratingComboBox;
    @FXML private ComboBox<String> statusComboBox;

    @FXML private Label totalPoliciesLabel;
    @FXML private Label activePoliciesLabel;
    @FXML private Label totalClaimsLabel;
    @FXML private Label pendingClaimsLabel;
    @FXML private Label statusLabel;

    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button refreshButton;
    @FXML private Button saveButton;
    @FXML private Button clearButton;
    @FXML private Button backButton;
    @FXML private Button fadeButton;

    @FXML private ProgressIndicator loadProgress;
    @FXML private ProgressBar operationProgress;
    @FXML private Pagination providersPagination;

    private InsuranceProviderDAO providerDAO;
    private InsurancePolicyDAO policyDAO;
    private InsuranceClaimDAO claimDAO;
    private ObservableList<InsuranceProvider> providerList;
    private InsuranceProvider selectedProvider;
    private boolean isEditMode = false;
    private int currentPage = 0;
    private int pageSize = 10;

    @FXML
    public void initialize() {
        providerDAO = new InsuranceProviderDAO();
        policyDAO = new InsurancePolicyDAO();
        claimDAO = new InsuranceClaimDAO();
        providerList = FXCollections.observableArrayList();

        setupTableColumns();
        setupComboBoxes();
        setupButtonHandlers();
        setupPagination();
        applyVisualEffects();

        if (statusLabel != null) statusLabel.setText("Ready");

        providersTable.setItems(providerList);
        loadProviders();

        providersTable.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                selectedProvider = newVal;
                displayProviderDetails(selectedProvider);
                loadProviderStatistics(selectedProvider.getId());
                isEditMode = true;
                saveButton.setText("Update Provider");
            }
        });
    }

    private void setupTableColumns() {
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        // FIXED: For ObjectProperty<Double>, get the value directly and convert to String
        ratingColumn.setCellValueFactory(cellData -> {
            Double rating = cellData.getValue().getRating();
            return new javafx.beans.property.SimpleObjectProperty<>(rating != null ? rating : 0.0);
        });
        phoneColumn.setCellValueFactory(cellData -> cellData.getValue().contactPhoneProperty());
        emailColumn.setCellValueFactory(cellData -> cellData.getValue().contactEmailProperty());
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());

        nameColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        ratingColumn.setStyle("-fx-alignment: CENTER;");
        phoneColumn.setStyle("-fx-alignment: CENTER;");
        emailColumn.setStyle("-fx-alignment: CENTER;");
        statusColumn.setStyle("-fx-alignment: CENTER;");
    }

    private void setupPagination() {
        if (providersPagination != null) {
            providersPagination.setPageCount(1);
            providersPagination.setMaxPageIndicatorCount(5);
            providersPagination.currentPageIndexProperty().addListener((obs, old, newPage) -> {
                currentPage = newPage.intValue();
                updateTablePage();
            });
        }
    }

    private void updateTablePage() {
        int start = currentPage * pageSize;
        int end = Math.min(start + pageSize, providerList.size());
        if (start < providerList.size()) {
            providersTable.setItems(FXCollections.observableArrayList(providerList.subList(start, end)));
        }
    }

    private void setupComboBoxes() {
        ratingComboBox.getItems().addAll("1.0", "1.5", "2.0", "2.5", "3.0", "3.5", "4.0", "4.5", "5.0");
        ratingComboBox.setValue("3.5");
        statusComboBox.getItems().addAll("ACTIVE", "INACTIVE", "SUSPENDED");
        statusComboBox.setValue("ACTIVE");
    }

    private void setupButtonHandlers() {
        addButton.setOnAction(e -> handleAddMode());
        editButton.setOnAction(e -> handleEditMode());
        deleteButton.setOnAction(e -> handleDelete());
        refreshButton.setOnAction(e -> loadProviders());
        saveButton.setOnAction(e -> handleSave());
        clearButton.setOnAction(e -> clearForm());
        backButton.setOnAction(e -> handleBack());
        if (fadeButton != null) fadeButton.setOnAction(e -> showFadeAnimation());
    }

    private void applyVisualEffects() {
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(5.0);
        dropShadow.setOffsetX(2.0);
        dropShadow.setOffsetY(2.0);
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.3));

        addButton.setEffect(dropShadow);
        editButton.setEffect(dropShadow);
        deleteButton.setEffect(dropShadow);
        refreshButton.setEffect(dropShadow);
        saveButton.setEffect(dropShadow);
        clearButton.setEffect(dropShadow);
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
            if (statusLabel != null) statusLabel.setText("Animation played!");
            PauseTransition reset = new PauseTransition(Duration.seconds(2));
            reset.setOnFinished(e -> {
                if (statusLabel != null) statusLabel.setText("Ready");
            });
            reset.play();
        }
    }

    private void loadProviders() {
        if (loadProgress != null) loadProgress.setVisible(true);
        if (statusLabel != null) statusLabel.setText("Loading providers...");

        try {
            List<InsuranceProvider> providers = providerDAO.findAll();
            providerList.setAll(providers);
            int totalPages = (int) Math.ceil((double) providers.size() / pageSize);
            if (providersPagination != null) providersPagination.setPageCount(Math.max(1, totalPages));
            updateTablePage();
            if (statusLabel != null) statusLabel.setText("Loaded " + providers.size() + " providers");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to load providers", e);
            if (statusLabel != null) statusLabel.setText("Error loading providers: " + e.getMessage());
            AlertUtil.showError("Load Failed", "Failed to load insurance providers: " + e.getMessage());
        } finally {
            if (loadProgress != null) loadProgress.setVisible(false);
        }
    }

    private void loadProviderStatistics(int providerId) {
        try {
            int totalPolicies = policyDAO.countByProviderId(providerId);
            int activePolicies = policyDAO.countActiveByProviderId(providerId);
            int totalClaims = claimDAO.countByProviderId(providerId);
            int pendingClaims = claimDAO.countPendingByProviderId(providerId);

            if (totalPoliciesLabel != null) totalPoliciesLabel.setText(String.valueOf(totalPolicies));
            if (activePoliciesLabel != null) activePoliciesLabel.setText(String.valueOf(activePolicies));
            if (totalClaimsLabel != null) totalClaimsLabel.setText(String.valueOf(totalClaims));
            if (pendingClaimsLabel != null) pendingClaimsLabel.setText(String.valueOf(pendingClaims));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to load provider statistics", e);
            if (totalPoliciesLabel != null) totalPoliciesLabel.setText("0");
            if (activePoliciesLabel != null) activePoliciesLabel.setText("0");
            if (totalClaimsLabel != null) totalClaimsLabel.setText("0");
            if (pendingClaimsLabel != null) pendingClaimsLabel.setText("0");
        }
    }

    private void displayProviderDetails(InsuranceProvider provider) {
        nameField.setText(provider.getName());
        registrationNumberField.setText(provider.getRegistrationNumber() != null ? provider.getRegistrationNumber() : "");
        licenseNumberField.setText(provider.getLicenseNumber() != null ? provider.getLicenseNumber() : "");
        phoneField.setText(provider.getContactPhone());
        emailField.setText(provider.getContactEmail());
        addressField.setText(provider.getAddress() != null ? provider.getAddress() : "");
        coverageDetailsField.setText(provider.getCoverageDetails() != null ? provider.getCoverageDetails() : "");

        if (provider.getRating() != null) {
            ratingComboBox.setValue(String.valueOf(provider.getRating()));
        } else {
            ratingComboBox.setValue("3.5");
        }

        if (provider.getStatus() != null) {
            statusComboBox.setValue(provider.getStatus());
        } else {
            statusComboBox.setValue("ACTIVE");
        }
    }

    private void handleAddMode() {
        clearForm();
        isEditMode = false;
        saveButton.setText("Add Provider");
        selectedProvider = null;
        providersTable.getSelectionModel().clearSelection();
        if (statusLabel != null) statusLabel.setText("Enter new provider details");
    }

    private void handleEditMode() {
        if (selectedProvider == null) {
            AlertUtil.showWarning("No Selection", "Please select a provider to edit.");
            return;
        }
        isEditMode = true;
        saveButton.setText("Update Provider");
        if (statusLabel != null) statusLabel.setText("Edit provider details");
    }

    private void handleSave() {
        if (!validateInputs()) {
            return;
        }

        if (loadProgress != null) loadProgress.setVisible(true);
        if (statusLabel != null) statusLabel.setText(isEditMode ? "Updating provider..." : "Adding provider...");

        try {
            InsuranceProvider provider = new InsuranceProvider();
            provider.setName(nameField.getText().trim());
            provider.setRegistrationNumber(registrationNumberField.getText().trim().isEmpty() ? null : registrationNumberField.getText().trim());
            provider.setLicenseNumber(licenseNumberField.getText().trim().isEmpty() ? null : licenseNumberField.getText().trim());
            provider.setContactPhone(phoneField.getText().trim());
            provider.setContactEmail(emailField.getText().trim());
            provider.setAddress(addressField.getText().trim());
            provider.setCoverageDetails(coverageDetailsField.getText().trim());

            String ratingStr = ratingComboBox.getValue();
            if (ratingStr != null && !ratingStr.isEmpty()) {
                provider.setRating(Double.parseDouble(ratingStr));
            }
            provider.setStatus(statusComboBox.getValue());

            boolean success;

            if (isEditMode && selectedProvider != null) {
                provider.setId(selectedProvider.getId());
                success = providerDAO.update(provider);
                if (success) {
                    AlertUtil.showSuccess("Provider updated successfully.");
                    if (statusLabel != null) statusLabel.setText("Provider updated");
                }
            } else {
                success = providerDAO.insert(provider);
                if (success) {
                    AlertUtil.showSuccess("Provider added successfully.");
                    if (statusLabel != null) statusLabel.setText("Provider added");
                }
            }

            if (success) {
                loadProviders();
                clearForm();
            } else {
                AlertUtil.showError("Save Failed", "Failed to save provider.");
                if (statusLabel != null) statusLabel.setText("Save failed");
            }
        } catch (NumberFormatException e) {
            AlertUtil.showError("Invalid Rating", "Please select a valid rating.");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to save provider", e);
            if (statusLabel != null) statusLabel.setText("Error: " + e.getMessage());
            AlertUtil.showError("Save Failed", "An error occurred: " + e.getMessage());
        } finally {
            if (loadProgress != null) loadProgress.setVisible(false);
        }
    }

    private void handleDelete() {
        if (selectedProvider == null) {
            AlertUtil.showWarning("No Selection", "Please select a provider to delete.");
            return;
        }

        boolean confirmed = AlertUtil.showConfirmation("Delete Provider",
                "Delete provider '" + selectedProvider.getName() + "'?\n" +
                        "This will also delete all associated policies and claims!");

        if (confirmed) {
            if (loadProgress != null) loadProgress.setVisible(true);
            if (statusLabel != null) statusLabel.setText("Deleting provider...");

            try {
                boolean success = providerDAO.delete(selectedProvider.getId());

                if (success) {
                    AlertUtil.showSuccess("Provider deleted successfully.");
                    clearForm();
                    loadProviders();
                    if (statusLabel != null) statusLabel.setText("Provider deleted");
                } else {
                    AlertUtil.showError("Delete Failed", "Failed to delete provider.");
                    if (statusLabel != null) statusLabel.setText("Delete failed");
                }
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Failed to delete provider", e);
                if (statusLabel != null) statusLabel.setText("Error: " + e.getMessage());
                AlertUtil.showError("Delete Failed", "An error occurred: " + e.getMessage());
            } finally {
                if (loadProgress != null) loadProgress.setVisible(false);
            }
        }
    }

    private void clearForm() {
        nameField.clear();
        registrationNumberField.clear();
        licenseNumberField.clear();
        phoneField.clear();
        emailField.clear();
        addressField.clear();
        coverageDetailsField.clear();
        ratingComboBox.setValue("3.5");
        statusComboBox.setValue("ACTIVE");

        if (totalPoliciesLabel != null) totalPoliciesLabel.setText("0");
        if (activePoliciesLabel != null) activePoliciesLabel.setText("0");
        if (totalClaimsLabel != null) totalClaimsLabel.setText("0");
        if (pendingClaimsLabel != null) pendingClaimsLabel.setText("0");

        selectedProvider = null;
        isEditMode = false;
        saveButton.setText("Add Provider");
        providersTable.getSelectionModel().clearSelection();
        if (statusLabel != null) statusLabel.setText("Form cleared");
    }

    private void handleBack() {
        String role = SessionManager.getInstance().getUserRole();
        if ("ADMIN".equals(role)) {
            SceneManager.getInstance().switchToAdminView();
        } else if ("INSURANCE".equals(role)) {
            SceneManager.getInstance().switchToInsuranceView();
        } else {
            SceneManager.getInstance().switchToDashboard();
        }
    }

    private boolean validateInputs() {
        if (!ValidationUtil.isNotEmpty(nameField.getText())) {
            AlertUtil.showWarning("Validation Error", "Provider name is required.");
            nameField.requestFocus();
            return false;
        }

        if (!ValidationUtil.isNotEmpty(phoneField.getText())) {
            AlertUtil.showWarning("Validation Error", "Phone number is required.");
            phoneField.requestFocus();
            return false;
        }

        if (!ValidationUtil.isValidEmail(emailField.getText())) {
            AlertUtil.showWarning("Validation Error", "Please enter a valid email address.");
            emailField.requestFocus();
            return false;
        }

        if (ratingComboBox.getValue() == null) {
            AlertUtil.showWarning("Validation Error", "Please select a rating.");
            return false;
        }

        if (statusComboBox.getValue() == null) {
            AlertUtil.showWarning("Validation Error", "Please select a status.");
            return false;
        }

        return true;
    }
}