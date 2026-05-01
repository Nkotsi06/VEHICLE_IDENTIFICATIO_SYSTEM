package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import utils.AlertUtil;
import utils.SceneManager;
import utils.SessionManager;
import utils.ValidationUtil;
import dao.CustomerDAO;
import dao.UserDAO;
import dao.DigitalWalletDAO;
import models.Customer;
import models.User;

public class CustomerProfileController {

    @FXML private TabPane mainTabPane;

    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField addressField;
    @FXML private TextField nationalIdField;
    @FXML private TextField driversLicenseField;
    @FXML private Button updateProfileButton;

    @FXML private TextField currentPasswordField;
    @FXML private TextField newPasswordField;
    @FXML private TextField confirmPasswordField;
    @FXML private Button changePasswordButton;

    @FXML private Label walletBalanceLabel;
    @FXML private Button viewWalletButton;
    @FXML private Button addFundsButton;

    @FXML private Button viewVehiclesButton;
    @FXML private Button viewQueriesButton;
    @FXML private Button viewComplaintsButton;
    @FXML private Button viewReviewsButton;
    @FXML private Button viewNotificationsButton;
    @FXML private Button backButton;

    private CustomerDAO customerDAO;
    private UserDAO userDAO;
    private DigitalWalletDAO walletDAO;
    private Customer currentCustomer;

    @FXML
    public void initialize() {
        customerDAO = new CustomerDAO();
        userDAO = new UserDAO();
        walletDAO = new DigitalWalletDAO();

        loadCustomerProfile();
        loadWalletBalance();
        setupButtonHandlers();
    }

    private void loadCustomerProfile() {
        try {
            int userId = SessionManager.getInstance().getUserId();
            currentCustomer = customerDAO.findByUserId(userId);

            if (currentCustomer != null) {
                fullNameField.setText(currentCustomer.getName());
                emailField.setText(currentCustomer.getEmail());
                phoneField.setText(currentCustomer.getPhone());
                addressField.setText(currentCustomer.getAddress());
                nationalIdField.setText(currentCustomer.getNationalId());
                driversLicenseField.setText(currentCustomer.getDriversLicenseNumber());

                SessionManager.getInstance().setCustomerId(currentCustomer.getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadWalletBalance() {
        try {
            if (currentCustomer != null) {
                models.DigitalWallet wallet = walletDAO.findByCustomerId(currentCustomer.getId());
                if (wallet != null) {
                    walletBalanceLabel.setText(utils.CurrencyUtil.format(wallet.getBalance()));
                } else {
                    walletBalanceLabel.setText(utils.CurrencyUtil.format(0.0));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupButtonHandlers() {
        updateProfileButton.setOnAction(event -> handleUpdateProfile());
        changePasswordButton.setOnAction(event -> handleChangePassword());
        viewWalletButton.setOnAction(event -> SceneManager.getInstance().switchToDigitalWalletView());
        addFundsButton.setOnAction(event -> handleAddFunds());
        viewVehiclesButton.setOnAction(event -> SceneManager.getInstance().switchToCustomerVehicleView());
        viewQueriesButton.setOnAction(event -> SceneManager.getInstance().switchToCustomerQueryView());
        viewComplaintsButton.setOnAction(event -> SceneManager.getInstance().switchToCustomerComplaintView());
        viewReviewsButton.setOnAction(event -> SceneManager.getInstance().switchToCustomerReviewView());
        viewNotificationsButton.setOnAction(event -> SceneManager.getInstance().switchToNotificationView());
        backButton.setOnAction(event -> SceneManager.getInstance().switchToDashboard());
    }

    private void handleUpdateProfile() {
        if (!validateProfileInputs()) {
            return;
        }

        try {
            currentCustomer.setName(fullNameField.getText().trim());
            currentCustomer.setEmail(emailField.getText().trim());
            currentCustomer.setPhone(phoneField.getText().trim());
            currentCustomer.setAddress(addressField.getText().trim());
            currentCustomer.setNationalId(nationalIdField.getText().trim());
            currentCustomer.setDriversLicenseNumber(driversLicenseField.getText().trim());

            boolean success = customerDAO.update(currentCustomer);

            if (success) {
                User user = userDAO.findById(currentCustomer.getUserId());
                user.setFullName(currentCustomer.getName());
                user.setEmail(currentCustomer.getEmail());
                userDAO.update(user);

                SessionManager.getInstance().createSession(
                        user.getId(), user.getUsername(), user.getRole(),
                        user.getFullName(), user.getEmail()
                );

                AlertUtil.showSuccess("Profile updated successfully.");
            } else {
                AlertUtil.showError("Update Failed", "Failed to update profile.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Database Error", "An error occurred while updating profile.");
        }
    }

    private void handleChangePassword() {
        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (!ValidationUtil.isNotEmpty(currentPassword)) {
            AlertUtil.showWarning("Validation Error", "Please enter your current password.");
            return;
        }

        if (!ValidationUtil.isNotEmpty(newPassword)) {
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

        try {
            int userId = SessionManager.getInstance().getUserId();
            User user = userDAO.findById(userId);

            if (!user.getPassword().equals(currentPassword)) {
                AlertUtil.showError("Password Error", "Current password is incorrect.");
                return;
            }

            boolean success = userDAO.updatePassword(userId, newPassword);

            if (success) {
                AlertUtil.showSuccess("Password changed successfully.");
                currentPasswordField.clear();
                newPasswordField.clear();
                confirmPasswordField.clear();
            } else {
                AlertUtil.showError("Update Failed", "Failed to change password.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleAddFunds() {
        AlertUtil.showInfo("Add Funds", "Please use the Digital Wallet section to add funds.");
        SceneManager.getInstance().switchToDigitalWalletView();
    }

    private boolean validateProfileInputs() {
        if (!ValidationUtil.isNotEmpty(fullNameField.getText())) {
            AlertUtil.showWarning("Validation Error", "Full name is required.");
            return false;
        }

        if (!ValidationUtil.isNotEmpty(emailField.getText())) {
            AlertUtil.showWarning("Validation Error", "Email is required.");
            return false;
        }

        if (!ValidationUtil.isValidEmail(emailField.getText())) {
            AlertUtil.showWarning("Validation Error", "Please enter a valid email.");
            return false;
        }

        if (!ValidationUtil.isNotEmpty(phoneField.getText())) {
            AlertUtil.showWarning("Validation Error", "Phone number is required.");
            return false;
        }

        return true;
    }
}