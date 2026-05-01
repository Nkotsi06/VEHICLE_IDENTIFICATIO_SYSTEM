package controllers;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import utils.AlertUtil;
import utils.SceneManager;
import utils.SessionManager;
import utils.CurrencyUtil;
import dao.WorkshopDAO;
import dao.UserDAO;
import dao.ServiceRecordDAO;
import dao.MechanicDAO;
import models.Workshop;
import models.User;

public class WorkshopProfileController {

    @FXML private TabPane mainTabPane;

    @FXML private TextField workshopNameField;
    @FXML private TextField addressField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private TextField licenseNumberField;
    @FXML private Label approvalStatusLabel;
    @FXML private Button updateProfileButton;

    @FXML private Label totalServicesStatLabel;
    @FXML private Label totalRevenueStatLabel;
    @FXML private Label activeMechanicsStatLabel;

    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button changePasswordButton;

    @FXML private Button backButton;
    @FXML private ProgressIndicator loadProgress;
    @FXML private Label statusLabel;
    @FXML private Label passwordStatusLabel;

    private WorkshopDAO workshopDAO;
    private UserDAO userDAO;
    private ServiceRecordDAO serviceDAO;
    private MechanicDAO mechanicDAO;
    private Workshop currentWorkshop;

    @FXML
    public void initialize() {
        workshopDAO = new WorkshopDAO();
        userDAO = new UserDAO();
        serviceDAO = new ServiceRecordDAO();
        mechanicDAO = new MechanicDAO();

        loadWorkshopProfile();
        setupButtonHandlers();
        applyVisualEffects();
    }

    private void applyVisualEffects() {
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(5.0);
        dropShadow.setOffsetX(2.0);
        dropShadow.setOffsetY(2.0);
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.3));

        updateProfileButton.setEffect(dropShadow);
        changePasswordButton.setEffect(dropShadow);
        backButton.setEffect(dropShadow);
    }

    private void loadWorkshopProfile() {
        showProgress(true);
        statusLabel.setText("Loading profile...");

        try {
            int userId = SessionManager.getInstance().getUserId();
            currentWorkshop = workshopDAO.findByUserId(userId);

            if (currentWorkshop != null) {
                workshopNameField.setText(currentWorkshop.getWorkshopName());
                addressField.setText(currentWorkshop.getAddress());
                phoneField.setText(currentWorkshop.getPhone());
                emailField.setText(currentWorkshop.getEmail());
                licenseNumberField.setText(currentWorkshop.getLicenseNumber());

                if (currentWorkshop.isApproved()) {
                    approvalStatusLabel.setText("APPROVED");
                    approvalStatusLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                } else {
                    approvalStatusLabel.setText("PENDING APPROVAL");
                    approvalStatusLabel.setStyle("-fx-text-fill: #FF9800; -fx-font-weight: bold;");
                }

                SessionManager.getInstance().setWorkshopId(currentWorkshop.getId());

                // Load statistics
                int workshopId = currentWorkshop.getId();
                int totalServices = serviceDAO.countByWorkshopId(workshopId);
                double totalRevenue = serviceDAO.sumRevenueByWorkshopId(workshopId);
                int activeMechanics = mechanicDAO.countByWorkshopId(workshopId);

                totalServicesStatLabel.setText(String.valueOf(totalServices));
                totalRevenueStatLabel.setText(CurrencyUtil.format(totalRevenue));
                activeMechanicsStatLabel.setText(String.valueOf(activeMechanics));

                statusLabel.setText("Profile loaded successfully");
            } else {
                statusLabel.setText("Profile not found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error loading profile: " + e.getMessage());
            AlertUtil.showError("Load Failed", "Failed to load workshop profile.");
        } finally {
            hideProgressAfterDelay();
        }
    }

    private void setupButtonHandlers() {
        updateProfileButton.setOnAction(event -> handleUpdateProfile());
        changePasswordButton.setOnAction(event -> handleChangePassword());
        backButton.setOnAction(event -> SceneManager.getInstance().switchToWorkshopView());
    }

    private void handleUpdateProfile() {
        if (!validateProfileInputs()) {
            return;
        }

        showProgress(true);
        statusLabel.setText("Updating profile...");

        try {
            currentWorkshop.setWorkshopName(workshopNameField.getText().trim());
            currentWorkshop.setAddress(addressField.getText().trim());
            currentWorkshop.setPhone(phoneField.getText().trim());
            currentWorkshop.setEmail(emailField.getText().trim());
            currentWorkshop.setLicenseNumber(licenseNumberField.getText().trim());

            boolean success = workshopDAO.update(currentWorkshop);

            if (success) {
                User user = userDAO.findById(currentWorkshop.getUserId());
                user.setFullName(currentWorkshop.getWorkshopName());
                user.setEmail(currentWorkshop.getEmail());
                userDAO.update(user);

                SessionManager.getInstance().setFullName(currentWorkshop.getWorkshopName());

                AlertUtil.showSuccess("Workshop profile updated successfully.");
                statusLabel.setText("Profile updated successfully");
            } else {
                AlertUtil.showError("Update Failed", "Failed to update profile.");
                statusLabel.setText("Update failed");
            }

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Database Error", "An error occurred while updating profile.");
            statusLabel.setText("Error: " + e.getMessage());
        } finally {
            hideProgressAfterDelay();
        }
    }

    private void handleChangePassword() {
        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (!utils.ValidationUtil.isNotEmpty(currentPassword)) {
            AlertUtil.showWarning("Validation Error", "Please enter your current password.");
            return;
        }

        if (!utils.ValidationUtil.isNotEmpty(newPassword)) {
            AlertUtil.showWarning("Validation Error", "Please enter a new password.");
            return;
        }

        if (newPassword.length() < 6) {
            AlertUtil.showWarning("Validation Error", "Password must be at least 6 characters.");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            AlertUtil.showWarning("Validation Error", "Passwords do not match.");
            return;
        }

        showProgress(true);
        passwordStatusLabel.setText("Changing password...");

        try {
            int userId = SessionManager.getInstance().getUserId();
            User user = userDAO.findById(userId);

            if (!user.getPassword().equals(currentPassword)) {
                AlertUtil.showError("Password Error", "Current password is incorrect.");
                passwordStatusLabel.setText("Current password is incorrect");
                hideProgressAfterDelay();
                return;
            }

            boolean success = userDAO.updatePassword(userId, newPassword);

            if (success) {
                AlertUtil.showSuccess("Password changed successfully.");
                passwordStatusLabel.setText("Password changed successfully");
                currentPasswordField.clear();
                newPasswordField.clear();
                confirmPasswordField.clear();
            } else {
                AlertUtil.showError("Update Failed", "Failed to change password.");
                passwordStatusLabel.setText("Password change failed");
            }

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Database Error", "An error occurred.");
            passwordStatusLabel.setText("Error: " + e.getMessage());
        } finally {
            hideProgressAfterDelay();
        }
    }

    private boolean validateProfileInputs() {
        if (!utils.ValidationUtil.isNotEmpty(workshopNameField.getText())) {
            AlertUtil.showWarning("Validation Error", "Workshop name is required.");
            return false;
        }

        if (!utils.ValidationUtil.isNotEmpty(phoneField.getText())) {
            AlertUtil.showWarning("Validation Error", "Phone number is required.");
            return false;
        }

        if (!utils.ValidationUtil.isNotEmpty(emailField.getText())) {
            AlertUtil.showWarning("Validation Error", "Email is required.");
            return false;
        }

        if (!utils.ValidationUtil.isValidEmail(emailField.getText())) {
            AlertUtil.showWarning("Validation Error", "Please enter a valid email.");
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