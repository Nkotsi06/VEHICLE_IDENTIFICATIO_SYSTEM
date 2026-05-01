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
import utils.CurrencyUtil;
import dao.InsurancePolicyDAO;
import dao.PolicyRenewalDAO;
import models.InsurancePolicy;
import models.PolicyRenewal;

import java.time.LocalDate;
import java.util.List;

public class PolicyRenewalController {

    @FXML private TableView<PolicyRenewal> renewalsTable;
    @FXML private TableColumn<PolicyRenewal, String> policyColumn;
    @FXML private TableColumn<PolicyRenewal, String> vehicleColumn;
    @FXML private TableColumn<PolicyRenewal, String> renewalDateColumn;
    @FXML private TableColumn<PolicyRenewal, Double> premiumColumn;
    @FXML private TableColumn<PolicyRenewal, String> paymentStatusColumn;
    @FXML private TableColumn<PolicyRenewal, String> renewalStatusColumn;  // FIXED: Changed from statusColumn to renewalStatusColumn

    @FXML private ComboBox<InsurancePolicy> policyComboBox;
    @FXML private DatePicker renewalDatePicker;
    @FXML private TextField premiumField;
    @FXML private ComboBox<String> paymentMethodComboBox;
    @FXML private TextArea summaryArea;
    @FXML private Label ncbLabel;
    @FXML private Label discountedPremiumLabel;
    @FXML private Label statusLabel;

    @FXML private Button createButton;
    @FXML private Button processPaymentButton;
    @FXML private Button refreshButton;
    @FXML private Button backButton;
    @FXML private Button clearButton;
    @FXML private Button fadeButton;

    @FXML private ProgressIndicator loadProgress;
    @FXML private ProgressBar operationProgress;
    @FXML private Pagination renewalsPagination;

    private PolicyRenewalDAO renewalDAO;
    private InsurancePolicyDAO policyDAO;
    private PolicyRenewal selectedRenewal;
    private ObservableList<PolicyRenewal> renewalList;
    private List<PolicyRenewal> fullData;
    private int currentPage = 0;
    private int pageSize = 10;

    @FXML
    public void initialize() {
        renewalDAO = new PolicyRenewalDAO();
        policyDAO = new InsurancePolicyDAO();
        renewalList = FXCollections.observableArrayList();

        setupTableColumns();
        setupComboBoxes();
        loadPolicies();
        loadRenewals();
        setupButtonHandlers();
        setupPagination();
        applyVisualEffects();

        renewalDatePicker.setValue(LocalDate.now().plusMonths(1));
        statusLabel.setText("Ready");
    }

    private void setupTableColumns() {
        policyColumn.setCellValueFactory(cellData -> cellData.getValue().policyNumberProperty());
        vehicleColumn.setCellValueFactory(cellData -> cellData.getValue().registrationNumberProperty());
        renewalDateColumn.setCellValueFactory(cellData -> cellData.getValue().renewalDateProperty().asString());
        premiumColumn.setCellValueFactory(cellData -> cellData.getValue().premiumProperty().asObject());
        paymentStatusColumn.setCellValueFactory(cellData -> cellData.getValue().paymentStatusProperty());
        // FIXED: Use paymentStatusProperty instead of non-existent renewalStatusProperty
        renewalStatusColumn.setCellValueFactory(cellData -> cellData.getValue().paymentStatusProperty());

        policyColumn.setStyle("-fx-alignment: CENTER;");
        vehicleColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        renewalDateColumn.setStyle("-fx-alignment: CENTER;");
        premiumColumn.setStyle("-fx-alignment: CENTER-RIGHT;");
        paymentStatusColumn.setStyle("-fx-alignment: CENTER;");
        renewalStatusColumn.setStyle("-fx-alignment: CENTER;");
    }

    private void setupComboBoxes() {
        paymentMethodComboBox.getItems().addAll("Credit Card", "Debit Card", "Bank Transfer", "Digital Wallet", "Cash");
        paymentMethodComboBox.setValue("Credit Card");
    }

    private void setupPagination() {
        if (renewalsPagination != null) {
            renewalsPagination.setPageCount(1);
            renewalsPagination.setMaxPageIndicatorCount(5);
            renewalsPagination.currentPageIndexProperty().addListener((obs, old, newPage) -> {
                currentPage = newPage.intValue();
                updateTablePage();
            });
        }
    }

    private void updateTablePage() {
        if (fullData == null || fullData.isEmpty()) return;
        int start = currentPage * pageSize;
        int end = Math.min(start + pageSize, fullData.size());
        if (start < fullData.size()) {
            renewalList.setAll(fullData.subList(start, end));
        }
    }

    private void loadPolicies() {
        showProgress(true);
        try {
            List<InsurancePolicy> policies = policyDAO.findActivePolicies();
            policyComboBox.getItems().setAll(policies);
            statusLabel.setText("Loaded " + policies.size() + " policies");
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error loading policies");
        } finally {
            showProgress(false);
        }
    }

    private void loadRenewals() {
        showProgress(true);
        statusLabel.setText("Loading renewals...");

        try {
            List<PolicyRenewal> renewals = renewalDAO.findAll();
            fullData = renewals;
            int totalPages = (int) Math.ceil((double) renewals.size() / pageSize);
            if (renewalsPagination != null) renewalsPagination.setPageCount(Math.max(1, totalPages));
            updateTablePage();
            statusLabel.setText("Loaded " + renewals.size() + " renewals");
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error loading renewals");
            AlertUtil.showError("Load Failed", "Failed to load renewals.");
        } finally {
            showProgress(false);
        }
    }

    private void setupButtonHandlers() {
        createButton.setOnAction(event -> handleCreate());
        processPaymentButton.setOnAction(event -> handleProcessPayment());
        refreshButton.setOnAction(event -> loadRenewals());
        backButton.setOnAction(event -> SceneManager.getInstance().switchToInsurancePolicyView());
        clearButton.setOnAction(event -> clearForm());
        if (fadeButton != null) fadeButton.setOnAction(event -> showFadeAnimation());

        renewalsTable.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                selectedRenewal = newVal;
                boolean isPending = "PENDING".equals(selectedRenewal.getPaymentStatus());
                processPaymentButton.setDisable(!isPending);
            }
        });
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

        createButton.setEffect(dropShadow);
        processPaymentButton.setEffect(dropShadow);
        refreshButton.setEffect(dropShadow);
        backButton.setEffect(dropShadow);
        clearButton.setEffect(dropShadow);
    }

    private void handleCreate() {
        InsurancePolicy selectedPolicy = policyComboBox.getSelectionModel().getSelectedItem();

        if (selectedPolicy == null) {
            AlertUtil.showWarning("Validation Error", "Please select a policy.");
            return;
        }

        if (renewalDatePicker.getValue() == null) {
            AlertUtil.showWarning("Validation Error", "Please select a renewal date.");
            return;
        }

        if (!utils.ValidationUtil.isNotEmpty(premiumField.getText())) {
            AlertUtil.showWarning("Validation Error", "Please enter premium amount.");
            premiumField.requestFocus();
            return;
        }

        showProgress(true);
        statusLabel.setText("Creating renewal...");
        updateProgress(0.3);

        try {
            double premium = Double.parseDouble(premiumField.getText());

            // Calculate NCB
            double ncbPercentage = calculateNCB(selectedPolicy);
            double discountedPremium = premium * (1 - ncbPercentage);

            ncbLabel.setText(String.format("%.0f%%", ncbPercentage * 100));
            discountedPremiumLabel.setText(CurrencyUtil.format(discountedPremium));

            updateProgress(0.6);
            boolean success = renewalDAO.createRenewal(selectedPolicy.getId(), renewalDatePicker.getValue(), discountedPremium);

            if (success) {
                updateProgress(1.0);
                AlertUtil.showSuccess("Policy renewal created successfully.");
                statusLabel.setText("Renewal created");
                clearForm();
                loadRenewals();

                // Show summary
                StringBuilder summary = new StringBuilder();
                summary.append("Policy Renewal Created\n");
                summary.append("━".repeat(30)).append("\n");
                summary.append("Policy: ").append(selectedPolicy.getPolicyNumber()).append("\n");
                summary.append("Vehicle: ").append(selectedPolicy.getRegistrationNumber()).append("\n");
                summary.append("Renewal Date: ").append(renewalDatePicker.getValue()).append("\n");
                summary.append("Original Premium: ").append(CurrencyUtil.format(premium)).append("\n");
                summary.append("NCB Applied: ").append(String.format("%.0f%%", ncbPercentage * 100)).append("\n");
                summary.append("Final Premium: ").append(CurrencyUtil.format(discountedPremium)).append("\n");
                summary.append("Payment Method: ").append(paymentMethodComboBox.getValue());
                summaryArea.setText(summary.toString());
            } else {
                AlertUtil.showError("Create Failed", "Failed to create renewal.");
                statusLabel.setText("Create failed");
            }
        } catch (NumberFormatException e) {
            AlertUtil.showError("Invalid Input", "Please enter a valid premium amount.");
            statusLabel.setText("Invalid premium amount");
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error: " + e.getMessage());
            AlertUtil.showError("Database Error", "An error occurred while creating renewal.");
        } finally {
            hideProgressAfterDelay();
        }
    }

    private double calculateNCB(InsurancePolicy policy) {
        try {
            List<PolicyRenewal> renewals = renewalDAO.findByInsuranceId(policy.getId());  // FIXED: Use findByInsuranceId instead of findByPolicyId
            int claimFreeYears = 0;
            for (PolicyRenewal r : renewals) {
                if ("PAID".equals(r.getPaymentStatus())) {
                    claimFreeYears++;
                }
            }

            if (claimFreeYears >= 5) return 0.50;
            if (claimFreeYears >= 4) return 0.40;
            if (claimFreeYears >= 3) return 0.30;
            if (claimFreeYears >= 2) return 0.20;
            if (claimFreeYears >= 1) return 0.10;
            return 0;
        } catch (Exception e) {
            return 0;
        }
    }

    private void handleProcessPayment() {
        if (selectedRenewal == null) {
            AlertUtil.showWarning("No Selection", "Please select a renewal to process.");
            return;
        }

        if (!"PENDING".equals(selectedRenewal.getPaymentStatus())) {
            AlertUtil.showWarning("Already Processed", "This renewal has already been processed.");
            return;
        }

        boolean confirmed = AlertUtil.showConfirmation("Process Payment",
                "Process payment of " + CurrencyUtil.format(selectedRenewal.getPremium()) + " for policy renewal?");

        if (confirmed) {
            showProgress(true);
            statusLabel.setText("Processing payment...");
            updateProgress(0.5);

            try {
                boolean success = renewalDAO.processRenewalPayment(selectedRenewal.getId());

                if (success) {
                    updateProgress(1.0);
                    AlertUtil.showSuccess("Payment processed successfully. Policy extended by 1 year.");
                    statusLabel.setText("Payment processed");
                    loadRenewals();
                } else {
                    AlertUtil.showError("Payment Failed", "Failed to process payment.");
                    statusLabel.setText("Payment failed");
                }
            } catch (Exception e) {
                e.printStackTrace();
                statusLabel.setText("Error: " + e.getMessage());
                AlertUtil.showError("Database Error", "An error occurred while processing payment.");
            } finally {
                hideProgressAfterDelay();
            }
        }
    }

    private void clearForm() {
        policyComboBox.getSelectionModel().clearSelection();
        renewalDatePicker.setValue(LocalDate.now().plusMonths(1));
        premiumField.clear();
        selectedRenewal = null;
        renewalsTable.getSelectionModel().clearSelection();
        ncbLabel.setText("0%");
        discountedPremiumLabel.setText("M0.00");
        summaryArea.clear();
        statusLabel.setText("Form cleared");
    }

    private void showProgress(boolean show) {
        if (loadProgress != null) loadProgress.setVisible(show);
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
            if (operationProgress != null) operationProgress.setVisible(false);
            if (loadProgress != null) loadProgress.setVisible(false);
        });
        delay.play();
    }
}