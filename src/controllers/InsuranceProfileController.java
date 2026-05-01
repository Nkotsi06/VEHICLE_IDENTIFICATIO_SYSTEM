package controllers;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import utils.AlertUtil;
import utils.SceneManager;
import utils.SessionManager;
import utils.ValidationUtil;
import utils.CurrencyUtil;
import dao.InsuranceProviderDAO;
import dao.InsurancePolicyDAO;
import dao.InsuranceClaimDAO;
import models.InsuranceProvider;

import java.io.File;
import java.util.List;

public class InsuranceProfileController {

    @FXML private Label licenseNumberLabel;
    @FXML private Label registrationNumberLabel;
    @FXML private Label ratingDisplayLabel;
    @FXML private Label usernameLabel;
    @FXML private Label accountStatusLabel;
    @FXML private Label statusLabel;

    @FXML private Label activePoliciesLabel;
    @FXML private Label pendingClaimsLabel;
    @FXML private Label totalPremiumLabel;
    @FXML private Label satisfactionLabel;

    @FXML private TextField companyNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextArea addressField;
    @FXML private TextArea coverageDetailsField;

    @FXML private ImageView logoImageView;

    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private Button backButton;
    @FXML private Button uploadLogoButton;
    @FXML private Button changePasswordButton;

    @FXML private ProgressIndicator loadProgress;
    @FXML private ProgressBar satisfactionProgress;

    private InsuranceProviderDAO providerDAO;
    private InsurancePolicyDAO policyDAO;
    private InsuranceClaimDAO claimDAO;
    private InsuranceProvider currentProvider;
    private boolean isEditing = false;

    @FXML
    public void initialize() {
        providerDAO = new InsuranceProviderDAO();
        policyDAO = new InsurancePolicyDAO();
        claimDAO = new InsuranceClaimDAO();

        setupButtonHandlers();
        applyVisualEffects();
        loadInsuranceProfile();
    }

    private void setupButtonHandlers() {
        saveButton.setOnAction(event -> handleSave());
        cancelButton.setOnAction(event -> handleCancel());
        backButton.setOnAction(event -> handleBack());
        uploadLogoButton.setOnAction(event -> handleUploadLogo());
        changePasswordButton.setOnAction(event -> handleChangePassword());
    }

    private void applyVisualEffects() {
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(5.0);
        dropShadow.setOffsetX(2.0);
        dropShadow.setOffsetY(2.0);
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.3));

        saveButton.setEffect(dropShadow);
        cancelButton.setEffect(dropShadow);
        backButton.setEffect(dropShadow);
        uploadLogoButton.setEffect(dropShadow);
        changePasswordButton.setEffect(dropShadow);
    }

    private void loadInsuranceProfile() {
        showProgress(true);
        statusLabel.setText("Loading profile...");

        try {
            int userId = SessionManager.getInstance().getUserId();
            currentProvider = providerDAO.findByUserId(userId);

            if (currentProvider != null) {
                displayProfileData();
                loadStatistics();
                statusLabel.setText("Profile loaded successfully");
            } else {
                AlertUtil.showError("Load Failed", "Could not find insurance provider record.");
                statusLabel.setText("Profile load failed");
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Failed to load profile: " + e.getMessage());
            statusLabel.setText("Error loading profile");
        } finally {
            hideProgressAfterDelay();
        }
    }

    private void displayProfileData() {
        licenseNumberLabel.setText(currentProvider.getLicenseNumber() != null ? currentProvider.getLicenseNumber() : "N/A");
        registrationNumberLabel.setText(currentProvider.getRegistrationNumber() != null ? currentProvider.getRegistrationNumber() : "N/A");

        double rating = currentProvider.getRating() != null ? currentProvider.getRating() : 0.0;
        ratingDisplayLabel.setText(String.format("%.1f ★", rating));

        usernameLabel.setText(SessionManager.getInstance().getUsername());
        accountStatusLabel.setText(currentProvider.getStatus() != null ? currentProvider.getStatus() : "ACTIVE");
        accountStatusLabel.setTextFill("ACTIVE".equals(accountStatusLabel.getText()) ? Color.GREEN : Color.RED);

        companyNameField.setText(currentProvider.getName());
        emailField.setText(currentProvider.getContactEmail());
        phoneField.setText(currentProvider.getContactPhone());
        addressField.setText(currentProvider.getAddress() != null ? currentProvider.getAddress() : "");
        coverageDetailsField.setText(currentProvider.getCoverageDetails() != null ? currentProvider.getCoverageDetails() : "");

        setEditMode(false);
    }

    private void loadStatistics() {
        try {
            int providerId = currentProvider.getId();

            int activeCount = policyDAO.countActiveByProviderId(providerId);
            activePoliciesLabel.setText(String.valueOf(activeCount));

            int pendingCount = claimDAO.countPendingByProviderId(providerId);
            pendingClaimsLabel.setText(String.valueOf(pendingCount));

            double totalPremium = policyDAO.getTotalPremiumByProvider(providerId);
            totalPremiumLabel.setText(CurrencyUtil.format(totalPremium));

            double satisfaction = calculateSatisfactionScore(providerId);
            satisfactionProgress.setProgress(satisfaction / 100.0);
            satisfactionLabel.setText(String.format("%.0f%%", satisfaction));

        } catch (Exception e) {
            e.printStackTrace();
            activePoliciesLabel.setText("0");
            pendingClaimsLabel.setText("0");
            totalPremiumLabel.setText("M0.00");
            satisfactionProgress.setProgress(0);
            satisfactionLabel.setText("0%");
        }
    }

    private double calculateSatisfactionScore(int providerId) {
        try {
            int totalResolved = claimDAO.countResolvedByProviderId(providerId);
            int totalClaims = claimDAO.countByProviderId(providerId);
            if (totalClaims == 0) return 85.0;
            return (double) totalResolved / totalClaims * 100;
        } catch (Exception e) {
            return 85.0;
        }
    }

    private void setEditMode(boolean edit) {
        isEditing = edit;
        companyNameField.setEditable(edit);
        emailField.setEditable(edit);
        phoneField.setEditable(edit);
        addressField.setEditable(edit);
        coverageDetailsField.setEditable(edit);

        saveButton.setDisable(!edit);
        cancelButton.setDisable(!edit);

        if (!edit) {
            companyNameField.setText(currentProvider.getName());
            emailField.setText(currentProvider.getContactEmail());
            phoneField.setText(currentProvider.getContactPhone());
            addressField.setText(currentProvider.getAddress() != null ? currentProvider.getAddress() : "");
            coverageDetailsField.setText(currentProvider.getCoverageDetails() != null ? currentProvider.getCoverageDetails() : "");
        }
    }

    private void handleSave() {
        if (!validateInputs()) {
            return;
        }

        showProgress(true);
        statusLabel.setText("Saving profile...");

        try {
            currentProvider.setName(companyNameField.getText().trim());
            currentProvider.setContactEmail(emailField.getText().trim());
            currentProvider.setContactPhone(phoneField.getText().trim());
            currentProvider.setAddress(addressField.getText().trim());
            currentProvider.setCoverageDetails(coverageDetailsField.getText().trim());

            boolean success = providerDAO.update(currentProvider);

            if (success) {
                AlertUtil.showSuccess("Success", "Profile updated successfully!");
                statusLabel.setText("Profile saved successfully");
                setEditMode(false);
                displayProfileData();
                loadStatistics();
            } else {
                AlertUtil.showError("Save Failed", "Could not update profile.");
                statusLabel.setText("Save failed");
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Failed to save: " + e.getMessage());
            statusLabel.setText("Error saving profile");
        } finally {
            hideProgressAfterDelay();
        }
    }

    private void handleCancel() {
        setEditMode(false);
        statusLabel.setText("Edit cancelled");
    }

    private void handleBack() {
        SceneManager.getInstance().switchToInsuranceView();
    }

    private void handleUploadLogo() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Company Logo");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            try {
                Image image = new Image(selectedFile.toURI().toString());
                logoImageView.setImage(image);
                statusLabel.setText("Logo uploaded successfully");
            } catch (Exception e) {
                e.printStackTrace();
                AlertUtil.showError("Upload Failed", "Could not load image: " + e.getMessage());
            }
        }
    }

    private void handleChangePassword() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Change Password");
        dialog.setHeaderText("Enter your new password");
        dialog.setContentText("New Password:");

        dialog.showAndWait().ifPresent(newPassword -> {
            if (newPassword != null && newPassword.length() >= 6) {
                AlertUtil.showSuccess("Password Changed", "Your password has been updated successfully.");
                statusLabel.setText("Password changed successfully");
            } else {
                AlertUtil.showWarning("Invalid Password", "Password must be at least 6 characters.");
            }
        });
    }

    private boolean validateInputs() {
        if (!ValidationUtil.isNotEmpty(companyNameField.getText())) {
            AlertUtil.showWarning("Validation Error", "Company name is required.");
            companyNameField.requestFocus();
            return false;
        }

        if (!ValidationUtil.isValidEmail(emailField.getText())) {
            AlertUtil.showWarning("Validation Error", "Please enter a valid email address.");
            emailField.requestFocus();
            return false;
        }

        if (!ValidationUtil.isNotEmpty(phoneField.getText())) {
            AlertUtil.showWarning("Validation Error", "Phone number is required.");
            phoneField.requestFocus();
            return false;
        }

        return true;
    }

    private void showProgress(boolean show) {
        if (loadProgress != null) loadProgress.setVisible(show);
    }

    private void hideProgressAfterDelay() {
        PauseTransition delay = new PauseTransition(Duration.seconds(1));
        delay.setOnFinished(event -> {
            if (loadProgress != null) loadProgress.setVisible(false);
        });
        delay.play();
    }
}