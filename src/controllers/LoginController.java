package controllers;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import utils.AlertUtil;
import utils.SceneManager;
import utils.SessionManager;
import utils.ValidationUtil;
import dao.UserDAO;
import dao.AuditDAO;
import models.User;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField visiblePasswordField;
    @FXML private Button loginButton;
    @FXML private Button togglePasswordButton;
    @FXML private ProgressIndicator loginProgress;
    @FXML private Hyperlink forgotPasswordLink;

    private UserDAO userDAO;
    private AuditDAO auditDAO;
    private boolean passwordVisible = false;

    @FXML
    public void initialize() {
        userDAO = new UserDAO();
        auditDAO = new AuditDAO();
        setupEventHandlers();
        setupFocusEffects();
        setupButtonEffects();
        setupPasswordToggle();
    }

    private void setupPasswordToggle() {
        visiblePasswordField.setVisible(false);
        visiblePasswordField.setManaged(false);
        passwordField.setVisible(true);
        passwordField.setManaged(true);
        visiblePasswordField.textProperty().bindBidirectional(passwordField.textProperty());
        togglePasswordButton.setText("Show Password");
    }

    private void setupEventHandlers() {
        loginButton.setOnAction(e -> handleLogin());
        togglePasswordButton.setOnAction(e -> togglePasswordVisibility());
        forgotPasswordLink.setOnAction(e -> handleForgotPassword());

        usernameField.setOnKeyPressed(this::handleKeyPress);
        passwordField.setOnKeyPressed(this::handleKeyPress);
        visiblePasswordField.setOnKeyPressed(this::handleKeyPress);
    }

    private void setupFocusEffects() {
        usernameField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                usernameField.setStyle("-fx-border-color: #006400; -fx-border-width: 2px; -fx-border-radius: 24;");
            } else {
                usernameField.setStyle("-fx-border-color: #dddddd; -fx-border-width: 1px; -fx-border-radius: 24;");
            }
        });

        passwordField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                passwordField.setStyle("-fx-border-color: #006400; -fx-border-width: 2px; -fx-border-radius: 24;");
            } else {
                passwordField.setStyle("-fx-border-color: #dddddd; -fx-border-width: 1px; -fx-border-radius: 24;");
            }
        });

        visiblePasswordField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                visiblePasswordField.setStyle("-fx-border-color: #006400; -fx-border-width: 2px; -fx-border-radius: 24;");
            } else {
                visiblePasswordField.setStyle("-fx-border-color: #dddddd; -fx-border-width: 1px; -fx-border-radius: 24;");
            }
        });
    }

    private void setupButtonEffects() {
        loginButton.setOnMouseEntered(e -> loginButton.setStyle("-fx-background-color: #008000; -fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold; -fx-background-radius: 26; -fx-cursor: hand;"));
        loginButton.setOnMouseExited(e -> loginButton.setStyle("-fx-background-color: #006400; -fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold; -fx-background-radius: 26; -fx-cursor: hand;"));
        loginButton.setOnMousePressed(e -> loginButton.setStyle("-fx-background-color: #004d00; -fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold; -fx-background-radius: 26; -fx-cursor: hand;"));

        togglePasswordButton.setOnMouseEntered(e -> togglePasswordButton.setStyle("-fx-background-color: #e8e8e8; -fx-text-fill: #2c3e50; -fx-font-size: 12px; -fx-font-weight: bold; -fx-border-color: #cccccc; -fx-border-radius: 20; -fx-background-radius: 20; -fx-cursor: hand;"));
        togglePasswordButton.setOnMouseExited(e -> togglePasswordButton.setStyle("-fx-background-color: #f8f9fa; -fx-text-fill: #2c3e50; -fx-font-size: 12px; -fx-font-weight: bold; -fx-border-color: #dddddd; -fx-border-radius: 20; -fx-background-radius: 20; -fx-cursor: hand;"));
    }

    private void togglePasswordVisibility() {
        passwordVisible = !passwordVisible;

        if (passwordVisible) {
            visiblePasswordField.setVisible(true);
            visiblePasswordField.setManaged(true);
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            togglePasswordButton.setText("Hide Password");
            visiblePasswordField.requestFocus();
        } else {
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            visiblePasswordField.setVisible(false);
            visiblePasswordField.setManaged(false);
            togglePasswordButton.setText("Show Password");
            passwordField.requestFocus();
        }
    }

    private void handleKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            handleLogin();
        }
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordVisible ? visiblePasswordField.getText() : passwordField.getText();

        if (!validateInputs(username, password)) return;

        loginButton.setDisable(true);
        loginProgress.setVisible(true);
        loginButton.setText("LOGGING IN...");

        try {
            User user = userDAO.login(username, password);

            if (user != null && user.isActive()) {
                auditDAO.logAction(user.getId(), "LOGIN_SUCCESS", "127.0.0.1");

                SessionManager.getInstance().createSession(user.getId(), user.getUsername(),
                        user.getRole(), user.getFullName(), user.getEmail());

                loadRoleSpecificIds(user.getRole(), user.getId());

                showSuccessAnimation();
                AlertUtil.showInfo("Login Successful", "Welcome " + user.getFullName());
                clearForm();
                SceneManager.getInstance().switchToDashboard();
            } else {
                auditDAO.logAction(0, "LOGIN_FAILED - " + username, "127.0.0.1");
                showErrorAnimation();
                AlertUtil.showError("Login Failed", "Invalid username or password.");
                passwordField.clear();
                if (visiblePasswordField != null) visiblePasswordField.clear();
                loginButton.setText("LOGIN");
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Login failed. Please try again.");
            loginButton.setText("LOGIN");
        } finally {
            loginButton.setDisable(false);
            loginProgress.setVisible(false);
        }
    }

    private void showSuccessAnimation() {
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(0.5), loginButton);
        fadeTransition.setFromValue(1.0);
        fadeTransition.setToValue(0.7);
        fadeTransition.setAutoReverse(true);
        fadeTransition.setCycleCount(2);
        fadeTransition.play();
    }

    private void showErrorAnimation() {
        loginButton.setStyle("-fx-background-color: #E31E2C; -fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold; -fx-background-radius: 26;");
        PauseTransition pause = new PauseTransition(Duration.seconds(1));
        pause.setOnFinished(e -> loginButton.setStyle("-fx-background-color: #006400; -fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold; -fx-background-radius: 26;"));
        pause.play();
    }

    private void loadRoleSpecificIds(String role, int userId) {
        try {
            if ("CUSTOMER".equals(role)) {
                dao.CustomerDAO customerDAO = new dao.CustomerDAO();
                models.Customer customer = customerDAO.findByUserId(userId);
                if (customer != null) {
                    SessionManager.getInstance().setCustomerId(customer.getId());
                }
            } else if ("WORKSHOP".equals(role)) {
                dao.WorkshopDAO workshopDAO = new dao.WorkshopDAO();
                models.Workshop workshop = workshopDAO.findByUserId(userId);
                if (workshop != null) {
                    SessionManager.getInstance().setWorkshopId(workshop.getId());
                }
            } else if ("INSURANCE".equals(role)) {
                dao.InsuranceProviderDAO providerDAO = new dao.InsuranceProviderDAO();
                models.InsuranceProvider provider = providerDAO.findByUserId(userId);
                if (provider != null) {
                    SessionManager.getInstance().setInsuranceProviderId(provider.getId());
                }
            } else if ("POLICE".equals(role)) {
                dao.PoliceOfficerDAO officerDAO = new dao.PoliceOfficerDAO();
                models.PoliceOfficer officer = officerDAO.findByUserId(userId);
                if (officer != null) {
                    SessionManager.getInstance().setPoliceOfficerId(officer.getId());
                    SessionManager.getInstance().setBadgeNumber(officer.getBadgeNumber());
                    SessionManager.getInstance().setRank(officer.getRank());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean validateInputs(String username, String password) {
        if (username.isEmpty()) {
            AlertUtil.showWarning("Validation Error", "Please enter username");
            usernameField.requestFocus();
            return false;
        }
        if (password.isEmpty()) {
            AlertUtil.showWarning("Validation Error", "Please enter password");
            (passwordVisible ? visiblePasswordField : passwordField).requestFocus();
            return false;
        }
        return true;
    }

    private void clearForm() {
        usernameField.clear();
        passwordField.clear();
        if (visiblePasswordField != null) visiblePasswordField.clear();
    }

    private void handleForgotPassword() {
        SceneManager.getInstance().switchToForgotPassword();
    }
}