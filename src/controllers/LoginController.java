package controllers;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
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

    private static final String LOGIN_BUTTON_STYLE = "-fx-background-color: #006400; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-background-radius: 8;";
    private static final String LOGIN_BUTTON_HOVER_STYLE = "-fx-background-color: #008000; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-background-radius: 8;";
    private static final String LOGIN_BUTTON_PRESSED_STYLE = "-fx-background-color: #004d00; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-background-radius: 8;";
    private static final String TOGGLE_BUTTON_STYLE = "-fx-font-size: 18px; -fx-min-width: 48; -fx-min-height: 48; -fx-background-color: #f8f9fa; -fx-background-radius: 8; -fx-border-color: #ddd; -fx-border-radius: 8; -fx-border-width: 1px; -fx-cursor: hand;";
    private static final String TOGGLE_BUTTON_HOVER_STYLE = "-fx-font-size: 18px; -fx-min-width: 48; -fx-min-height: 48; -fx-background-color: #e8e8e8; -fx-background-radius: 8; -fx-border-color: #bbb; -fx-border-radius: 8; -fx-border-width: 1px; -fx-cursor: hand;";

    @FXML
    public void initialize() {
        userDAO = new UserDAO();
        auditDAO = new AuditDAO();
        setupEventHandlers();
        addVisualEffects();
        setupPasswordToggle();

        loginButton.setStyle(LOGIN_BUTTON_STYLE);
        togglePasswordButton.setStyle(TOGGLE_BUTTON_STYLE);
    }

    private void setupPasswordToggle() {
        visiblePasswordField.setVisible(false);
        visiblePasswordField.setManaged(false);
        passwordField.setVisible(true);
        passwordField.setManaged(true);
        visiblePasswordField.textProperty().bindBidirectional(passwordField.textProperty());
    }

    private void setupEventHandlers() {
        loginButton.setOnAction(e -> handleLogin());
        togglePasswordButton.setOnAction(e -> togglePasswordVisibility());
        forgotPasswordLink.setOnAction(e -> handleForgotPassword());

        usernameField.setOnKeyPressed(this::handleKeyPress);
        passwordField.setOnKeyPressed(this::handleKeyPress);
        visiblePasswordField.setOnKeyPressed(this::handleKeyPress);
    }

    private void addVisualEffects() {
        loginButton.setOnMouseEntered(e -> loginButton.setStyle(LOGIN_BUTTON_HOVER_STYLE));
        loginButton.setOnMouseExited(e -> loginButton.setStyle(LOGIN_BUTTON_STYLE));
        loginButton.setOnMousePressed(e -> loginButton.setStyle(LOGIN_BUTTON_PRESSED_STYLE));
        loginButton.setOnMouseReleased(e -> loginButton.setStyle(LOGIN_BUTTON_HOVER_STYLE));

        togglePasswordButton.setOnMouseEntered(e -> togglePasswordButton.setStyle(TOGGLE_BUTTON_HOVER_STYLE));
        togglePasswordButton.setOnMouseExited(e -> togglePasswordButton.setStyle(TOGGLE_BUTTON_STYLE));

        forgotPasswordLink.setOnMouseEntered(e -> forgotPasswordLink.setStyle("-fx-text-fill: #006400; -fx-underline: true;"));
        forgotPasswordLink.setOnMouseExited(e -> forgotPasswordLink.setStyle("-fx-text-fill: #2980b9; -fx-underline: true;"));

        usernameField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                usernameField.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #006400; -fx-border-width: 2px; -fx-padding: 0 15;");
            } else {
                usernameField.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #ddd; -fx-border-width: 1px; -fx-padding: 0 15;");
            }
        });

        passwordField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                passwordField.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #006400; -fx-border-width: 2px; -fx-padding: 0 15;");
            } else {
                passwordField.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #ddd; -fx-border-width: 1px; -fx-padding: 0 15;");
            }
        });

        visiblePasswordField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                visiblePasswordField.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #006400; -fx-border-width: 2px; -fx-padding: 0 15;");
            } else {
                visiblePasswordField.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #ddd; -fx-border-width: 1px; -fx-padding: 0 15;");
            }
        });
    }

    private void togglePasswordVisibility() {
        passwordVisible = !passwordVisible;

        if (passwordVisible) {
            visiblePasswordField.setText(passwordField.getText());
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            visiblePasswordField.setVisible(true);
            visiblePasswordField.setManaged(true);
            visiblePasswordField.requestFocus();
            visiblePasswordField.positionCaret(visiblePasswordField.getText().length());
            togglePasswordButton.setText("🙈");
        } else {
            passwordField.setText(visiblePasswordField.getText());
            visiblePasswordField.setVisible(false);
            visiblePasswordField.setManaged(false);
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            passwordField.requestFocus();
            passwordField.positionCaret(passwordField.getText().length());
            togglePasswordButton.setText("👁");
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

        FadeTransition ft = new FadeTransition(Duration.millis(300), loginButton);
        ft.setFromValue(1.0);
        ft.setToValue(0.5);
        ft.play();

        try {
            User user = userDAO.login(username, password);

            if (user != null && user.isActive()) {
                // Log successful login
                auditDAO.logAction(user.getId(), "LOGIN_SUCCESS", "127.0.0.1");

                SessionManager.getInstance().createSession(user.getId(), user.getUsername(),
                        user.getRole(), user.getFullName(), user.getEmail());

                loadRoleSpecificIds(user.getRole(), user.getId());

                AlertUtil.showInfo("Login Successful", "Welcome " + user.getFullName());

                ft.stop();
                loginButton.setOpacity(1.0);
                clearForm();
                SceneManager.getInstance().switchToDashboard();
            } else {
                // Log failed login attempt
                auditDAO.logAction(0, "LOGIN_FAILED - Username: " + username, "127.0.0.1");
                AlertUtil.showError("Login Failed", "Invalid username or password.");
                passwordField.clear();
                if (visiblePasswordField != null) {
                    visiblePasswordField.clear();
                }
                if (passwordVisible && visiblePasswordField != null) {
                    visiblePasswordField.requestFocus();
                } else {
                    passwordField.requestFocus();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Login failed. Please try again.");
        } finally {
            ft.stop();
            loginButton.setOpacity(1.0);
            loginButton.setDisable(false);
            loginProgress.setVisible(false);
        }
    }

    private void clearForm() {
        usernameField.clear();
        passwordField.clear();
        if (visiblePasswordField != null) {
            visiblePasswordField.clear();
        }
        if (passwordVisible) {
            passwordVisible = false;
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            visiblePasswordField.setVisible(false);
            visiblePasswordField.setManaged(false);
            togglePasswordButton.setText("👁");
        }
        usernameField.setStyle("");
        passwordField.setStyle("");
        if (visiblePasswordField != null) {
            visiblePasswordField.setStyle("");
        }
    }

    private void loadRoleSpecificIds(String role, int userId) {
        try {
            if ("CUSTOMER".equals(role)) {
                dao.CustomerDAO customerDAO = new dao.CustomerDAO();
                var customer = customerDAO.findByUserId(userId);
                if (customer != null) {
                    SessionManager.getInstance().setCustomerId(customer.getId());
                }
            } else if ("WORKSHOP".equals(role)) {
                dao.WorkshopDAO workshopDAO = new dao.WorkshopDAO();
                var workshop = workshopDAO.findByUserId(userId);
                if (workshop != null) {
                    SessionManager.getInstance().setWorkshopId(workshop.getId());
                }
            } else if ("INSURANCE".equals(role)) {
                dao.InsuranceProviderDAO providerDAO = new dao.InsuranceProviderDAO();
                var provider = providerDAO.findByUserId(userId);
                if (provider != null) {
                    SessionManager.getInstance().setInsuranceProviderId(provider.getId());
                }
            } else if ("POLICE".equals(role)) {
                dao.PoliceOfficerDAO policeDAO = new dao.PoliceOfficerDAO();
                var officer = policeDAO.findByUserId(userId);
                if (officer != null) {
                    SessionManager.getInstance().setPoliceOfficerId(officer.getId());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean validateInputs(String username, String password) {
        if (!ValidationUtil.isNotEmpty(username)) {
            AlertUtil.showWarning("Validation Error", "Please enter username");
            usernameField.requestFocus();
            return false;
        }
        if (!ValidationUtil.isNotEmpty(password)) {
            AlertUtil.showWarning("Validation Error", "Please enter password");
            if (passwordVisible && visiblePasswordField != null) {
                visiblePasswordField.requestFocus();
            } else {
                passwordField.requestFocus();
            }
            return false;
        }
        return true;
    }

    private void handleForgotPassword() {
        SceneManager.getInstance().switchToForgotPassword();
    }
}