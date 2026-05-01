package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Label;
import utils.AlertUtil;
import utils.SceneManager;
import dao.UserDAO;
import dao.WorkshopDAO;
import models.User;
import models.Workshop;

public class WorkshopRegistrationController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField emailField;
    @FXML private TextField fullNameField;

    @FXML private TextField workshopNameField;
    @FXML private TextField addressField;
    @FXML private TextField phoneField;
    @FXML private TextField licenseNumberField;

    @FXML private Button registerButton;
    @FXML private Button cancelButton;
    @FXML private Label statusLabel;

    private UserDAO userDAO;
    private WorkshopDAO workshopDAO;

    @FXML
    public void initialize() {
        userDAO = new UserDAO();
        workshopDAO = new WorkshopDAO();

        setupButtonHandlers();
    }

    private void setupButtonHandlers() {
        registerButton.setOnAction(event -> handleRegister());
        cancelButton.setOnAction(event -> handleCancel());
    }

    private void handleRegister() {
        if (!validateInputs()) {
            return;
        }

        try {
            User user = new User();
            user.setUsername(usernameField.getText().trim());
            user.setPassword(passwordField.getText());
            user.setRole("WORKSHOP");
            user.setFullName(fullNameField.getText().trim());
            user.setEmail(emailField.getText().trim());
            user.setActive(true);

            int userId = userDAO.insertAndGetId(user);

            if (userId > 0) {
                Workshop workshop = new Workshop();
                workshop.setUserId(userId);
                workshop.setWorkshopName(workshopNameField.getText().trim());
                workshop.setAddress(addressField.getText().trim());
                workshop.setPhone(phoneField.getText().trim());
                workshop.setEmail(emailField.getText().trim());
                workshop.setLicenseNumber(licenseNumberField.getText().trim());
                workshop.setApproved(false);

                boolean success = workshopDAO.insert(workshop);

                if (success) {
                    AlertUtil.showSuccess("Registration submitted successfully!\nYour account will be activated after admin approval.");
                    handleCancel();
                } else {
                    userDAO.delete(userId);
                    AlertUtil.showError("Registration Failed", "Failed to register workshop.");
                }
            } else {
                AlertUtil.showError("Registration Failed", "Username may already exist.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Database Error", "An error occurred during registration.");
        }
    }

    private boolean validateInputs() {
        if (!utils.ValidationUtil.isNotEmpty(usernameField.getText())) {
            AlertUtil.showWarning("Validation Error", "Username is required.");
            usernameField.requestFocus();
            return false;
        }

        if (!utils.ValidationUtil.isNotEmpty(passwordField.getText())) {
            AlertUtil.showWarning("Validation Error", "Password is required.");
            passwordField.requestFocus();
            return false;
        }

        if (passwordField.getText().length() < 6) {
            AlertUtil.showWarning("Validation Error", "Password must be at least 6 characters.");
            passwordField.requestFocus();
            return false;
        }

        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            AlertUtil.showWarning("Validation Error", "Passwords do not match.");
            confirmPasswordField.requestFocus();
            return false;
        }

        if (!utils.ValidationUtil.isNotEmpty(emailField.getText())) {
            AlertUtil.showWarning("Validation Error", "Email is required.");
            emailField.requestFocus();
            return false;
        }

        if (!utils.ValidationUtil.isValidEmail(emailField.getText())) {
            AlertUtil.showWarning("Validation Error", "Please enter a valid email.");
            emailField.requestFocus();
            return false;
        }

        if (!utils.ValidationUtil.isNotEmpty(fullNameField.getText())) {
            AlertUtil.showWarning("Validation Error", "Contact person name is required.");
            fullNameField.requestFocus();
            return false;
        }

        if (!utils.ValidationUtil.isNotEmpty(workshopNameField.getText())) {
            AlertUtil.showWarning("Validation Error", "Workshop name is required.");
            workshopNameField.requestFocus();
            return false;
        }

        if (!utils.ValidationUtil.isNotEmpty(phoneField.getText())) {
            AlertUtil.showWarning("Validation Error", "Phone number is required.");
            phoneField.requestFocus();
            return false;
        }

        return true;
    }

    private void handleCancel() {
        SceneManager.getInstance().switchToLogin();
    }
}