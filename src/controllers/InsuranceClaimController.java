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
import utils.CurrencyUtil;
import dao.InsuranceClaimDAO;
import dao.InsurancePolicyDAO;
import models.InsuranceClaim;
import models.InsurancePolicy;

import java.time.LocalDate;
import java.util.List;

public class InsuranceClaimController {

    @FXML private TableView<InsuranceClaim> claimsTable;
    @FXML private TableColumn<InsuranceClaim, String> claimIdColumn;
    @FXML private TableColumn<InsuranceClaim, String> policyColumn;
    @FXML private TableColumn<InsuranceClaim, String> claimDateColumn;
    @FXML private TableColumn<InsuranceClaim, Double> amountColumn;
    @FXML private TableColumn<InsuranceClaim, String> claimStatusColumn;
    @FXML private TableColumn<InsuranceClaim, Double> approvedAmountColumn;

    @FXML private ComboBox<InsurancePolicy> policyComboBox;
    @FXML private TextField claimAmountField;
    @FXML private TextArea descriptionArea;
    @FXML private TextArea rejectionReasonArea;
    @FXML private Label statusLabel;
    @FXML private Label approvedAmountLabel;
    @FXML private Label claimIdLabel;
    @FXML private Label policyHolderLabel;
    @FXML private Label incidentDateLabel;
    @FXML private Label incidentLocationLabel;

    @FXML private Button submitButton;
    @FXML private Button approveButton;
    @FXML private Button rejectButton;
    @FXML private Button refreshButton;
    @FXML private Button backButton;
    @FXML private Button clearButton;
    @FXML private Button fadeButton;

    @FXML private ProgressIndicator loadProgress;
    @FXML private ProgressBar operationProgress;
    @FXML private Pagination claimsPagination;
    @FXML private ListView<String> documentsList;

    private InsuranceClaimDAO claimDAO;
    private InsurancePolicyDAO policyDAO;
    private InsuranceClaim selectedClaim;
    private ObservableList<InsuranceClaim> claimList;
    private List<InsuranceClaim> fullData;
    private int currentPage = 0;
    private int pageSize = 10;

    @FXML
    public void initialize() {
        claimDAO = new InsuranceClaimDAO();
        policyDAO = new InsurancePolicyDAO();
        claimList = FXCollections.observableArrayList();

        setupTableColumns();
        loadPolicies();
        loadClaims();
        setupButtonHandlers();
        setupTableSelection();
        setupPagination();
        applyVisualEffects();

        approveButton.setVisible(false);
        rejectButton.setVisible(false);
        rejectionReasonArea.setVisible(false);
        statusLabel.setText("Ready");
    }

    private void setupTableColumns() {
        claimIdColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject().asString());
        policyColumn.setCellValueFactory(cellData -> cellData.getValue().policyNumberProperty());
        claimDateColumn.setCellValueFactory(cellData -> cellData.getValue().claimDateProperty().asString());
        amountColumn.setCellValueFactory(cellData -> cellData.getValue().claimAmountProperty().asObject());
        claimStatusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        approvedAmountColumn.setCellValueFactory(cellData -> cellData.getValue().approvedAmountProperty().asObject());

        policyColumn.setStyle("-fx-alignment: CENTER;");
        claimDateColumn.setStyle("-fx-alignment: CENTER;");
        amountColumn.setStyle("-fx-alignment: CENTER-RIGHT;");
        claimStatusColumn.setStyle("-fx-alignment: CENTER;");
        approvedAmountColumn.setStyle("-fx-alignment: CENTER-RIGHT;");
    }

    private void setupPagination() {
        if (claimsPagination != null) {
            claimsPagination.setPageCount(1);
            claimsPagination.setMaxPageIndicatorCount(5);
            claimsPagination.currentPageIndexProperty().addListener((obs, old, newPage) -> {
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
            claimList.setAll(fullData.subList(start, end));
            claimsTable.setItems(claimList);
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

    private void loadClaims() {
        showProgress(true);
        statusLabel.setText("Loading claims...");

        try {
            fullData = claimDAO.findAll();
            int totalPages = (int) Math.ceil((double) fullData.size() / pageSize);
            if (claimsPagination != null) claimsPagination.setPageCount(Math.max(1, totalPages));
            updateTablePage();
            statusLabel.setText("Loaded " + fullData.size() + " claims");
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error loading claims");
            AlertUtil.showError("Load Failed", "Failed to load claims.");
        } finally {
            showProgress(false);
        }
    }

    private void setupButtonHandlers() {
        submitButton.setOnAction(event -> handleSubmit());
        approveButton.setOnAction(event -> handleApprove());
        rejectButton.setOnAction(event -> handleReject());
        refreshButton.setOnAction(event -> loadClaims());
        backButton.setOnAction(event -> handleBack());
        if (clearButton != null) clearButton.setOnAction(event -> clearForm());
        if (fadeButton != null) fadeButton.setOnAction(event -> showFadeAnimation());
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

    private void applyVisualEffects() {
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(5.0);
        dropShadow.setOffsetX(2.0);
        dropShadow.setOffsetY(2.0);
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.3));

        submitButton.setEffect(dropShadow);
        approveButton.setEffect(dropShadow);
        rejectButton.setEffect(dropShadow);
        refreshButton.setEffect(dropShadow);
        backButton.setEffect(dropShadow);
    }

    private void setupTableSelection() {
        claimsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedClaim = newSelection;
                displayClaimDetails(selectedClaim);
            }
        });
    }

    private void displayClaimDetails(InsuranceClaim claim) {
        claimIdLabel.setText(String.valueOf(claim.getId()));
        statusLabel.setText(claim.getStatus());

        if (claim.getApprovedAmount() != null && claim.getApprovedAmount() > 0) {
            approvedAmountLabel.setText(CurrencyUtil.format(claim.getApprovedAmount()));
        } else {
            approvedAmountLabel.setText("Not approved yet");
        }

        if (claim.getRejectionReason() != null && !claim.getRejectionReason().isEmpty()) {
            rejectionReasonArea.setText(claim.getRejectionReason());
        }

        boolean isPending = "PENDING".equals(claim.getStatus());
        boolean isAdmin = SessionManager.getInstance().isAdmin();

        approveButton.setVisible(isPending && isAdmin);
        rejectButton.setVisible(isPending && isAdmin);
        rejectionReasonArea.setVisible(isPending && isAdmin);
    }

    private void handleSubmit() {
        InsurancePolicy selectedPolicy = policyComboBox.getSelectionModel().getSelectedItem();

        if (selectedPolicy == null) {
            AlertUtil.showWarning("Validation Error", "Please select a policy.");
            return;
        }

        if (!ValidationUtil.isNotEmpty(claimAmountField.getText())) {
            AlertUtil.showWarning("Validation Error", "Please enter claim amount.");
            claimAmountField.requestFocus();
            return;
        }

        showProgress(true);
        statusLabel.setText("Submitting claim...");
        updateProgress(0.3);

        try {
            double amount = Double.parseDouble(claimAmountField.getText());

            if (amount <= 0) {
                AlertUtil.showWarning("Validation Error", "Claim amount must be greater than 0.");
                return;
            }

            if (amount > selectedPolicy.getCoverageAmount()) {
                AlertUtil.showWarning("Insufficient Coverage",
                        "Claim amount exceeds coverage amount of " + CurrencyUtil.format(selectedPolicy.getCoverageAmount()));
                return;
            }

            updateProgress(0.6);
            boolean success = claimDAO.insert(new InsuranceClaim(selectedPolicy.getId(), amount, descriptionArea.getText()));

            if (success) {
                updateProgress(1.0);
                AlertUtil.showSuccess("Claim submitted successfully.");
                clearForm();
                loadClaims();
                statusLabel.setText("Claim submitted successfully");
            } else {
                AlertUtil.showError("Submit Failed", "Failed to submit claim.");
                statusLabel.setText("Submit failed");
            }
        } catch (NumberFormatException e) {
            AlertUtil.showError("Invalid Amount", "Please enter a valid amount.");
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error: " + e.getMessage());
            AlertUtil.showError("Database Error", "An error occurred while submitting claim.");
        } finally {
            hideProgressAfterDelay();
        }
    }

    private void handleApprove() {
        if (selectedClaim == null) {
            AlertUtil.showWarning("No Selection", "Please select a claim to approve.");
            return;
        }

        TextInputDialog amountDialog = new TextInputDialog(String.valueOf(selectedClaim.getClaimAmount()));
        amountDialog.setTitle("Approve Claim");
        amountDialog.setHeaderText("Enter approved amount");
        amountDialog.setContentText("Approved Amount (M):");

        amountDialog.showAndWait().ifPresent(amountStr -> {
            try {
                double approvedAmount = Double.parseDouble(amountStr);
                if (approvedAmount <= 0) {
                    AlertUtil.showWarning("Invalid Amount", "Approved amount must be greater than 0.");
                    return;
                }

                boolean confirmed = AlertUtil.showConfirmation("Approve Claim",
                        "Approve claim for amount " + CurrencyUtil.format(approvedAmount) + "?");

                if (confirmed) {
                    showProgress(true);
                    statusLabel.setText("Approving claim...");
                    updateProgress(0.5);

                    try {
                        boolean success = claimDAO.approveClaim(selectedClaim.getId(), approvedAmount);
                        if (success) {
                            updateProgress(1.0);
                            AlertUtil.showSuccess("Claim approved successfully.");
                            loadClaims();
                            statusLabel.setText("Claim approved");
                        } else {
                            AlertUtil.showError("Approve Failed", "Failed to approve claim.");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        statusLabel.setText("Error: " + e.getMessage());
                        AlertUtil.showError("Database Error", "An error occurred.");
                    } finally {
                        hideProgressAfterDelay();
                    }
                }
            } catch (NumberFormatException e) {
                AlertUtil.showError("Invalid Amount", "Please enter a valid amount.");
            }
        });
    }

    private void handleReject() {
        if (selectedClaim == null) {
            AlertUtil.showWarning("No Selection", "Please select a claim to reject.");
            return;
        }

        TextInputDialog reasonDialog = new TextInputDialog();
        reasonDialog.setTitle("Reject Claim");
        reasonDialog.setHeaderText("Enter rejection reason");
        reasonDialog.setContentText("Rejection Reason:");

        reasonDialog.showAndWait().ifPresent(reason -> {
            if (!ValidationUtil.isNotEmpty(reason)) {
                AlertUtil.showWarning("Reason Required", "Please provide a rejection reason.");
                return;
            }

            boolean confirmed = AlertUtil.showConfirmation("Reject Claim", "Are you sure you want to reject this claim?");

            if (confirmed) {
                showProgress(true);
                statusLabel.setText("Rejecting claim...");
                updateProgress(0.5);

                try {
                    boolean success = claimDAO.rejectClaim(selectedClaim.getId(), reason);
                    if (success) {
                        updateProgress(1.0);
                        AlertUtil.showSuccess("Claim rejected.");
                        loadClaims();
                        statusLabel.setText("Claim rejected");
                    } else {
                        AlertUtil.showError("Reject Failed", "Failed to reject claim.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    statusLabel.setText("Error: " + e.getMessage());
                    AlertUtil.showError("Database Error", "An error occurred.");
                } finally {
                    hideProgressAfterDelay();
                }
            }
        });
    }

    private void handleBack() {
        String role = SessionManager.getInstance().getUserRole();
        if ("CUSTOMER".equals(role)) {
            SceneManager.getInstance().switchToCustomerProfileView();
        } else {
            SceneManager.getInstance().switchToInsurancePolicyView();
        }
    }

    private void clearForm() {
        policyComboBox.getSelectionModel().clearSelection();
        claimAmountField.clear();
        descriptionArea.clear();
        selectedClaim = null;
        statusLabel.setText("");
        approvedAmountLabel.setText("");
        rejectionReasonArea.clear();
        claimIdLabel.setText("");
        policyHolderLabel.setText("");
        incidentDateLabel.setText("");
        incidentLocationLabel.setText("");
        claimsTable.getSelectionModel().clearSelection();
        approveButton.setVisible(false);
        rejectButton.setVisible(false);
        rejectionReasonArea.setVisible(false);
        statusLabel.setText("Form cleared");
    }

    private void showProgress(boolean show) {
        if (loadProgress != null) loadProgress.setVisible(show);
        if (operationProgress != null) operationProgress.setVisible(show);
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