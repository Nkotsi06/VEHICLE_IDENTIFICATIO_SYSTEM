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
import dao.InsuranceVerificationDAO;
import dao.InsurancePolicyDAO;
import models.InsuranceVerification;
import models.InsurancePolicy;

import java.time.LocalDate;
import java.util.List;

public class InsuranceVerificationController {

    @FXML private TableView<InsuranceVerification> verificationsTable;
    @FXML private TableColumn<InsuranceVerification, String> policyColumn;
    @FXML private TableColumn<InsuranceVerification, String> vehicleColumn;
    @FXML private TableColumn<InsuranceVerification, String> verificationDateColumn;
    @FXML private TableColumn<InsuranceVerification, String> statusColumn;
    @FXML private TableColumn<InsuranceVerification, String> verifiedByColumn;

    @FXML private ComboBox<InsurancePolicy> policyComboBox;
    @FXML private ComboBox<String> verificationStatusComboBox;
    @FXML private TextArea notesArea;
    @FXML private Label policyDetailsLabel;
    @FXML private Label vehicleDetailsLabel;
    @FXML private Label statusLabel;

    @FXML private Button verifyButton;
    @FXML private Button rejectButton;
    @FXML private Button refreshButton;
    @FXML private Button backButton;
    @FXML private Button clearButton;
    @FXML private Button fadeButton;

    @FXML private ProgressIndicator loadProgress;
    @FXML private ProgressBar operationProgress;
    @FXML private Pagination verificationsPagination;

    private InsuranceVerificationDAO verificationDAO;
    private InsurancePolicyDAO policyDAO;
    private InsuranceVerification selectedVerification;
    private ObservableList<InsuranceVerification> verificationList;
    private List<InsuranceVerification> fullData;
    private int currentPage = 0;
    private int pageSize = 10;

    @FXML
    public void initialize() {
        verificationDAO = new InsuranceVerificationDAO();
        policyDAO = new InsurancePolicyDAO();
        verificationList = FXCollections.observableArrayList();

        setupTableColumns();
        loadPolicies();
        loadVerifications();
        setupButtonHandlers();
        setupTableSelection();
        setupPagination();
        applyVisualEffects();

        verificationStatusComboBox.getItems().addAll("VERIFIED", "REJECTED", "PENDING");
        verificationStatusComboBox.setValue("VERIFIED");
        statusLabel.setText("Ready");
    }

    private void setupTableColumns() {
        policyColumn.setCellValueFactory(cellData -> cellData.getValue().policyNumberProperty());
        vehicleColumn.setCellValueFactory(cellData -> cellData.getValue().registrationNumberProperty());
        verificationDateColumn.setCellValueFactory(cellData -> cellData.getValue().verificationDateProperty().asString());
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().verificationStatusProperty());
        verifiedByColumn.setCellValueFactory(cellData -> cellData.getValue().verifiedByNameProperty());

        policyColumn.setStyle("-fx-alignment: CENTER;");
        vehicleColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        verificationDateColumn.setStyle("-fx-alignment: CENTER;");
        statusColumn.setStyle("-fx-alignment: CENTER;");
        verifiedByColumn.setStyle("-fx-alignment: CENTER-LEFT;");
    }

    private void setupPagination() {
        if (verificationsPagination != null) {
            verificationsPagination.setPageCount(1);
            verificationsPagination.setMaxPageIndicatorCount(5);
            verificationsPagination.currentPageIndexProperty().addListener((obs, old, newPage) -> {
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
            verificationList.setAll(fullData.subList(start, end));
        }
    }

    private void loadPolicies() {
        showProgress(true);
        try {
            List<InsurancePolicy> policies = policyDAO.findAll();
            policyComboBox.getItems().setAll(policies);
            statusLabel.setText("Loaded " + policies.size() + " policies");
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error loading policies");
        } finally {
            showProgress(false);
        }
    }

    private void loadVerifications() {
        showProgress(true);
        statusLabel.setText("Loading verifications...");

        try {
            List<InsuranceVerification> verifications = verificationDAO.findAll();
            fullData = verifications;
            int totalPages = (int) Math.ceil((double) verifications.size() / pageSize);
            if (verificationsPagination != null) verificationsPagination.setPageCount(Math.max(1, totalPages));
            updateTablePage();
            statusLabel.setText("Loaded " + verifications.size() + " verifications");
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error loading verifications");
            AlertUtil.showError("Load Failed", "Failed to load verifications.");
        } finally {
            showProgress(false);
        }
    }

    private void setupButtonHandlers() {
        verifyButton.setOnAction(event -> handleVerify());
        rejectButton.setOnAction(event -> handleReject());
        refreshButton.setOnAction(event -> loadVerifications());
        backButton.setOnAction(event -> SceneManager.getInstance().switchToInsurancePolicyView());
        clearButton.setOnAction(event -> clearForm());
        if (fadeButton != null) fadeButton.setOnAction(event -> showFadeAnimation());

        policyComboBox.setOnAction(event -> {
            InsurancePolicy selected = policyComboBox.getSelectionModel().getSelectedItem();
            if (selected != null) {
                policyDetailsLabel.setText("Policy: " + selected.getPolicyNumber() +
                        " | Valid: " + selected.getStartDate() + " to " + selected.getEndDate());
                vehicleDetailsLabel.setText("Vehicle: " + selected.getRegistrationNumber() +
                        " | Owner: " + selected.getOwnerName());
            }
        });
    }

    private void setupTableSelection() {
        verificationsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedVerification = newSelection;
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

        verifyButton.setEffect(dropShadow);
        rejectButton.setEffect(dropShadow);
        refreshButton.setEffect(dropShadow);
        backButton.setEffect(dropShadow);
        clearButton.setEffect(dropShadow);
    }

    private void handleVerify() {
        InsurancePolicy selectedPolicy = policyComboBox.getSelectionModel().getSelectedItem();

        if (selectedPolicy == null) {
            AlertUtil.showWarning("Validation Error", "Please select a policy to verify.");
            return;
        }

        showProgress(true);
        statusLabel.setText("Verifying policy...");
        updateProgress(0.3);

        try {
            int userId = SessionManager.getInstance().getUserId();
            String notes = notesArea.getText().trim();

            updateProgress(0.6);
            boolean success = verificationDAO.verifyPolicy(
                    selectedPolicy.getId(),
                    userId,
                    "VERIFIED",
                    notes
            );

            if (success) {
                updateProgress(1.0);
                AlertUtil.showSuccess("Insurance policy verified successfully.");
                clearForm();
                loadVerifications();
                statusLabel.setText("Policy verified successfully");
            } else {
                AlertUtil.showError("Verification Failed", "Failed to verify policy.");
                statusLabel.setText("Verification failed");
            }
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error: " + e.getMessage());
            AlertUtil.showError("Database Error", "An error occurred during verification.");
        } finally {
            hideProgressAfterDelay();
        }
    }

    private void handleReject() {
        InsurancePolicy selectedPolicy = policyComboBox.getSelectionModel().getSelectedItem();

        if (selectedPolicy == null) {
            AlertUtil.showWarning("Validation Error", "Please select a policy to reject.");
            return;
        }

        String notes = notesArea.getText().trim();
        if (!ValidationUtil.isNotEmpty(notes)) {
            AlertUtil.showWarning("Validation Error", "Please provide a reason for rejection.");
            notesArea.requestFocus();
            return;
        }

        boolean confirmed = AlertUtil.showConfirmation("Reject Policy",
                "Are you sure you want to reject this insurance policy?");

        if (confirmed) {
            showProgress(true);
            statusLabel.setText("Rejecting policy...");
            updateProgress(0.3);

            try {
                int userId = SessionManager.getInstance().getUserId();
                updateProgress(0.6);
                boolean success = verificationDAO.verifyPolicy(
                        selectedPolicy.getId(),
                        userId,
                        "REJECTED",
                        notes
                );

                if (success) {
                    updateProgress(1.0);
                    AlertUtil.showSuccess("Insurance policy rejected.");
                    clearForm();
                    loadVerifications();
                    statusLabel.setText("Policy rejected");
                } else {
                    AlertUtil.showError("Rejection Failed", "Failed to reject policy.");
                    statusLabel.setText("Rejection failed");
                }
            } catch (Exception e) {
                e.printStackTrace();
                statusLabel.setText("Error: " + e.getMessage());
                AlertUtil.showError("Database Error", "An error occurred during rejection.");
            } finally {
                hideProgressAfterDelay();
            }
        }
    }

    private void clearForm() {
        policyComboBox.getSelectionModel().clearSelection();
        verificationStatusComboBox.setValue("VERIFIED");
        notesArea.clear();
        policyDetailsLabel.setText("");
        vehicleDetailsLabel.setText("");
        selectedVerification = null;
        verificationsTable.getSelectionModel().clearSelection();
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