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
import dao.InsuranceClaimDAO;
import dao.InsurancePolicyDAO;
import models.InsuranceClaim;
import models.InsurancePolicy;

import java.util.List;

public class InsuranceClaimApprovalController {

    @FXML private TableView<InsuranceClaim> pendingClaimsTable;
    @FXML private TableColumn<InsuranceClaim, String> claimIdColumn;
    @FXML private TableColumn<InsuranceClaim, String> policyNumberColumn;
    @FXML private TableColumn<InsuranceClaim, String> vehicleColumn;
    @FXML private TableColumn<InsuranceClaim, Double> claimAmountColumn;
    @FXML private TableColumn<InsuranceClaim, String> claimDateColumn;
    @FXML private TableColumn<InsuranceClaim, String> priorityColumn;

    @FXML private Label claimIdLabel;
    @FXML private Label policyHolderLabel;
    @FXML private Label incidentDateLabel;
    @FXML private Label incidentLocationLabel;
    @FXML private TextArea descriptionArea;
    @FXML private ListView<String> documentsList;
    @FXML private TextField approvedAmountField;
    @FXML private TextArea rejectionReasonArea;
    @FXML private Label statusLabel;

    @FXML private Button approveButton;
    @FXML private Button rejectButton;
    @FXML private Button requestInfoButton;
    @FXML private Button refreshButton;
    @FXML private Button backButton;
    @FXML private Button fadeButton;

    @FXML private ProgressIndicator loadProgress;
    @FXML private ProgressBar operationProgress;
    @FXML private Pagination claimsPagination;

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
        loadPendingClaims();
        setupButtonHandlers();
        setupTableSelection();
        setupPagination();
        applyVisualEffects();

        statusLabel.setText("Ready");
    }

    private void setupTableColumns() {
        claimIdColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject().asString());
        policyNumberColumn.setCellValueFactory(cellData -> cellData.getValue().policyNumberProperty());
        vehicleColumn.setCellValueFactory(cellData -> cellData.getValue().registrationNumberProperty());
        claimAmountColumn.setCellValueFactory(cellData -> cellData.getValue().claimAmountProperty().asObject());
        claimDateColumn.setCellValueFactory(cellData -> cellData.getValue().claimDateProperty().asString());
        priorityColumn.setCellValueFactory(cellData -> {
            double amount = cellData.getValue().getClaimAmount();
            if (amount > 10000) return new javafx.beans.property.SimpleStringProperty("HIGH");
            if (amount > 5000) return new javafx.beans.property.SimpleStringProperty("MEDIUM");
            return new javafx.beans.property.SimpleStringProperty("LOW");
        });

        claimIdColumn.setStyle("-fx-alignment: CENTER;");
        policyNumberColumn.setStyle("-fx-alignment: CENTER;");
        vehicleColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        claimAmountColumn.setStyle("-fx-alignment: CENTER-RIGHT;");
        claimDateColumn.setStyle("-fx-alignment: CENTER;");
        priorityColumn.setStyle("-fx-alignment: CENTER;");
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
        }
    }

    private void loadPendingClaims() {
        showProgress(true);
        statusLabel.setText("Loading pending claims...");

        try {
            List<InsuranceClaim> claims = claimDAO.findByStatus("PENDING");
            fullData = claims;
            int totalPages = (int) Math.ceil((double) claims.size() / pageSize);
            if (claimsPagination != null) claimsPagination.setPageCount(Math.max(1, totalPages));
            updateTablePage();
            statusLabel.setText("Loaded " + claims.size() + " pending claims");
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error loading claims");
            AlertUtil.showError("Load Failed", "Failed to load pending claims.");
        } finally {
            showProgress(false);
        }
    }

    private void setupButtonHandlers() {
        approveButton.setOnAction(event -> handleApprove());
        rejectButton.setOnAction(event -> handleReject());
        requestInfoButton.setOnAction(event -> handleRequestInfo());
        refreshButton.setOnAction(event -> loadPendingClaims());
        backButton.setOnAction(event -> SceneManager.getInstance().switchToInsurancePolicyView());
        if (fadeButton != null) fadeButton.setOnAction(event -> showFadeAnimation());
    }

    private void setupTableSelection() {
        pendingClaimsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedClaim = newSelection;
                displayClaimDetails(selectedClaim);
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

        approveButton.setEffect(dropShadow);
        rejectButton.setEffect(dropShadow);
        requestInfoButton.setEffect(dropShadow);
        refreshButton.setEffect(dropShadow);
        backButton.setEffect(dropShadow);
    }

    private void displayClaimDetails(InsuranceClaim claim) {
        claimIdLabel.setText(String.valueOf(claim.getId()));

        try {
            InsurancePolicy policy = policyDAO.findById(claim.getPolicyId());
            if (policy != null) {
                policyHolderLabel.setText(policy.getOwnerName());
                incidentLocationLabel.setText(policy.getRegistrationNumber());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        incidentDateLabel.setText(claim.getClaimDate().toString());
        descriptionArea.setText(claim.getDescription() != null ? claim.getDescription() : "");
        approvedAmountField.setText(String.valueOf(claim.getClaimAmount()));

        documentsList.getItems().clear();
        documentsList.getItems().add("Claim Form - Submitted");
        documentsList.getItems().add("Vehicle Photos - Pending");
        documentsList.getItems().add("Police Report - Not Required");

        approveButton.setDisable(false);
        rejectButton.setDisable(false);
    }

    private void handleApprove() {
        if (selectedClaim == null) {
            AlertUtil.showWarning("No Selection", "Please select a claim to approve.");
            return;
        }

        String approvedAmountStr = approvedAmountField.getText().trim();
        if (!ValidationUtil.isNotEmpty(approvedAmountStr)) {
            AlertUtil.showWarning("Validation Error", "Please enter approved amount.");
            approvedAmountField.requestFocus();
            return;
        }

        try {
            double approvedAmount = Double.parseDouble(approvedAmountStr);
            if (approvedAmount <= 0) {
                AlertUtil.showWarning("Validation Error", "Approved amount must be greater than 0.");
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
                        statusLabel.setText("Claim approved");
                        loadPendingClaims();
                        clearForm();
                    } else {
                        AlertUtil.showError("Approval Failed", "Failed to approve claim.");
                        statusLabel.setText("Approval failed");
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
    }

    private void handleReject() {
        if (selectedClaim == null) {
            AlertUtil.showWarning("No Selection", "Please select a claim to reject.");
            return;
        }

        String reason = rejectionReasonArea.getText().trim();
        if (!ValidationUtil.isNotEmpty(reason)) {
            AlertUtil.showWarning("Validation Error", "Please provide a rejection reason.");
            rejectionReasonArea.requestFocus();
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
                    statusLabel.setText("Claim rejected");
                    loadPendingClaims();
                    clearForm();
                } else {
                    AlertUtil.showError("Rejection Failed", "Failed to reject claim.");
                    statusLabel.setText("Rejection failed");
                }
            } catch (Exception e) {
                e.printStackTrace();
                statusLabel.setText("Error: " + e.getMessage());
                AlertUtil.showError("Database Error", "An error occurred.");
            } finally {
                hideProgressAfterDelay();
            }
        }
    }

    private void handleRequestInfo() {
        if (selectedClaim == null) {
            AlertUtil.showWarning("No Selection", "Please select a claim.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Request Information");
        dialog.setHeaderText("Request additional information for Claim #" + selectedClaim.getId());
        dialog.setContentText("Message to claimant:");

        dialog.showAndWait().ifPresent(message -> {
            if (ValidationUtil.isNotEmpty(message)) {
                AlertUtil.showInfo("Request Sent", "Additional information request sent to claimant.");
                statusLabel.setText("Info request sent");
            }
        });
    }

    private void clearForm() {
        claimIdLabel.setText("");
        policyHolderLabel.setText("");
        incidentDateLabel.setText("");
        incidentLocationLabel.setText("");
        descriptionArea.clear();
        documentsList.getItems().clear();
        approvedAmountField.clear();
        rejectionReasonArea.clear();
        selectedClaim = null;
        pendingClaimsTable.getSelectionModel().clearSelection();
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