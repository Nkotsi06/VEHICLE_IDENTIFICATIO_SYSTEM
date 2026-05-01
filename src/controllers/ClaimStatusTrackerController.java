package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;  // ADD THIS MISSING IMPORT
import utils.AlertUtil;
import utils.SceneManager;
import utils.ValidationUtil;
import utils.CurrencyUtil;
import dao.InsuranceClaimDAO;
import models.InsuranceClaim;

public class ClaimStatusTrackerController {

    @FXML private TableView<InsuranceClaim> claimsTable;
    @FXML private TableColumn<InsuranceClaim, String> claimIdColumn;
    @FXML private TableColumn<InsuranceClaim, String> vehicleColumn;
    @FXML private TableColumn<InsuranceClaim, Double> amountColumn;
    @FXML private TableColumn<InsuranceClaim, String> statusColumn;
    @FXML private TableColumn<InsuranceClaim, String> dateColumn;

    @FXML private ComboBox<String> statusFilterComboBox;
    @FXML private TextField claimIdField;
    @FXML private Button searchButton;
    @FXML private Button refreshButton;
    @FXML private Button trackButton;
    @FXML private Button backButton;

    @FXML private Label selectedClaimIdLabel;
    @FXML private Label selectedClaimStatusLabel;
    @FXML private Label selectedClaimAmountLabel;
    @FXML private Label selectedClaimDateLabel;
    @FXML private ProgressBar statusProgressBar;
    @FXML private Label progressLabel;

    private InsuranceClaimDAO claimDAO;
    private InsuranceClaim selectedClaim;

    @FXML
    public void initialize() {
        claimDAO = new InsuranceClaimDAO();

        setupTableColumns();
        loadClaims();
        setupComboBoxes();
        setupButtonHandlers();
        setupTableSelection();
    }

    private void setupTableColumns() {
        claimIdColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty().asObject().asString());
        vehicleColumn.setCellValueFactory(cellData -> cellData.getValue().registrationNumberProperty());
        amountColumn.setCellValueFactory(cellData -> cellData.getValue().claimAmountProperty().asObject());
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        dateColumn.setCellValueFactory(cellData -> cellData.getValue().claimDateProperty().asString());
    }

    private void loadClaims() {
        try {
            java.util.List<InsuranceClaim> claims = claimDAO.findAll();
            claimsTable.getItems().setAll(claims);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupComboBoxes() {
        statusFilterComboBox.getItems().addAll("ALL", "PENDING", "APPROVED", "REJECTED", "PAID");
        statusFilterComboBox.setValue("ALL");
    }

    private void setupButtonHandlers() {
        searchButton.setOnAction(event -> handleSearch());
        refreshButton.setOnAction(event -> loadClaims());
        trackButton.setOnAction(event -> handleTrack());
        backButton.setOnAction(event -> SceneManager.getInstance().switchToInsurancePolicyView());

        statusFilterComboBox.setOnAction(event -> filterClaims());
    }

    private void setupTableSelection() {
        claimsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedClaim = newSelection;
                displayClaimStatus(selectedClaim);
            }
        });
    }

    private void filterClaims() {
        String filter = statusFilterComboBox.getValue();

        if ("ALL".equals(filter)) {
            loadClaims();
        } else {
            try {
                java.util.List<InsuranceClaim> filtered = claimDAO.findByStatus(filter);
                claimsTable.getItems().setAll(filtered);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void handleSearch() {
        String claimIdStr = claimIdField.getText().trim();

        if (!ValidationUtil.isNotEmpty(claimIdStr)) {
            AlertUtil.showWarning("Input Error", "Please enter a Claim ID.");
            return;
        }

        try {
            int claimId = Integer.parseInt(claimIdStr);
            InsuranceClaim claim = claimDAO.findById(claimId);

            if (claim != null) {
                claimsTable.getItems().clear();
                claimsTable.getItems().add(claim);
                claimsTable.getSelectionModel().select(claim);
            } else {
                AlertUtil.showWarning("Not Found", "No claim found with ID: " + claimId);
            }
        } catch (NumberFormatException e) {
            AlertUtil.showError("Invalid ID", "Please enter a valid numeric Claim ID.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleTrack() {
        if (selectedClaim == null) {
            AlertUtil.showWarning("No Selection", "Please select a claim to track.");
            return;
        }

        displayClaimStatus(selectedClaim);
    }

    private void displayClaimStatus(InsuranceClaim claim) {
        selectedClaimIdLabel.setText(String.valueOf(claim.getId()));
        selectedClaimStatusLabel.setText(claim.getStatus());
        selectedClaimAmountLabel.setText(CurrencyUtil.format(claim.getClaimAmount()));
        selectedClaimDateLabel.setText(claim.getClaimDate().toString());

        double progress = getStatusProgress(claim.getStatus());
        statusProgressBar.setProgress(progress);
        progressLabel.setText(String.format("%.0f%%", progress * 100));

        String statusMessage = getStatusMessage(claim.getStatus());
        if (claim.getRejectionReason() != null && !claim.getRejectionReason().isEmpty()) {
            statusMessage += "\nReason: " + claim.getRejectionReason();
        }

        AlertUtil.showInfo("Claim Status", statusMessage);
    }

    private double getStatusProgress(String status) {
        switch (status) {
            case "PENDING": return 0.25;
            case "APPROVED": return 0.50;
            case "PAID": return 1.0;
            case "REJECTED": return 0.0;
            default: return 0.0;
        }
    }

    private String getStatusMessage(String status) {
        switch (status) {
            case "PENDING":
                return "Your claim is pending review. We will notify you once processed.";
            case "APPROVED":
                return "Your claim has been approved! Payment will be processed shortly.";
            case "PAID":
                return "Your claim has been paid. Funds have been transferred.";
            case "REJECTED":
                return "Your claim has been rejected. Please contact support for details.";
            default:
                return "Status unknown. Please contact support.";
        }
    }
}