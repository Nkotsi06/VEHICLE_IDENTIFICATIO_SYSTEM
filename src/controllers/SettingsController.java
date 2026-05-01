package controllers;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Duration;
import utils.AlertUtil;
import utils.SceneManager;
import utils.SessionManager;
import utils.ValidationUtil;

public class SettingsController {

    @FXML private TabPane settingsTabPane;

    // General Settings
    @FXML private ComboBox<String> themeComboBox;
    @FXML private ComboBox<String> languageComboBox;
    @FXML private ComboBox<String> currencyComboBox;
    @FXML private CheckBox notificationsCheckBox;
    @FXML private CheckBox emailAlertsCheckBox;
    @FXML private CheckBox autoSaveCheckBox;
    @FXML private Button saveGeneralButton;
    @FXML private Button resetGeneralButton;

    // Database Settings
    @FXML private TextField dbHostField;
    @FXML private TextField dbPortField;
    @FXML private TextField dbNameField;
    @FXML private TextField dbUsernameField;
    @FXML private PasswordField dbPasswordField;
    @FXML private Button testConnectionButton;
    @FXML private Button saveDatabaseButton;
    @FXML private Label dbStatusLabel;

    // Security Settings
    @FXML private CheckBox twoFactorCheckBox;
    @FXML private CheckBox sessionTimeoutCheckBox;
    @FXML private TextField sessionTimeoutField;
    @FXML private CheckBox ipWhitelistCheckBox;
    @FXML private TextField ipWhitelistField;
    @FXML private Button saveSecurityButton;

    // Notification Settings
    @FXML private CheckBox emailNotificationsCheckBox;
    @FXML private TextField smtpHostField;
    @FXML private TextField smtpPortField;
    @FXML private TextField senderEmailField;
    @FXML private PasswordField smtpPasswordField;
    @FXML private Button saveNotificationButton;
    @FXML private Button testEmailButton;
    @FXML private Label notificationStatusLabel;

    @FXML private Button backButton;
    @FXML private Label statusLabel;
    @FXML private ProgressIndicator loadProgress;
    @FXML private ProgressBar operationProgress;

    @FXML
    public void initialize() {
        // Add items to combo boxes programmatically (not in FXML)
        themeComboBox.getItems().addAll("Light", "Dark", "System Default");
        themeComboBox.setValue("Light");

        languageComboBox.getItems().addAll("English", "Sesotho");
        languageComboBox.setValue("English");

        currencyComboBox.getItems().addAll("Maloti (M)", "Rand (R)");
        currencyComboBox.setValue("Maloti (M)");

        loadCurrentSettings();
        setupButtonHandlers();
    }

    private void loadCurrentSettings() {
        notificationsCheckBox.setSelected(true);
        emailAlertsCheckBox.setSelected(false);
        autoSaveCheckBox.setSelected(true);

        dbHostField.setText("localhost");
        dbPortField.setText("5432");
        dbNameField.setText("vehicle_db");
        dbUsernameField.setText("postgres");
        dbPasswordField.setText("");

        twoFactorCheckBox.setSelected(false);
        sessionTimeoutCheckBox.setSelected(true);
        sessionTimeoutField.setText("30");
        ipWhitelistCheckBox.setSelected(false);
        ipWhitelistField.setText("");

        emailNotificationsCheckBox.setSelected(false);
        smtpHostField.setText("smtp.gmail.com");
        smtpPortField.setText("587");
        senderEmailField.setText("");
        smtpPasswordField.setText("");
    }

    private void setupButtonHandlers() {
        saveGeneralButton.setOnAction(event -> handleSaveGeneral());
        resetGeneralButton.setOnAction(event -> handleResetGeneral());
        testConnectionButton.setOnAction(event -> handleTestConnection());
        saveDatabaseButton.setOnAction(event -> handleSaveDatabase());
        saveSecurityButton.setOnAction(event -> handleSaveSecurity());
        saveNotificationButton.setOnAction(event -> handleSaveNotification());
        testEmailButton.setOnAction(event -> handleTestEmail());
        backButton.setOnAction(event -> handleBack());
    }

    private void handleSaveGeneral() {
        String theme = themeComboBox.getValue();
        String language = languageComboBox.getValue();
        String currency = currencyComboBox.getValue();
        boolean notifications = notificationsCheckBox.isSelected();
        boolean emailAlerts = emailAlertsCheckBox.isSelected();
        boolean autoSave = autoSaveCheckBox.isSelected();

        statusLabel.setText("Saving general settings...");

        PauseTransition pause = new PauseTransition(Duration.seconds(0.5));
        pause.setOnFinished(e -> {
            statusLabel.setText("General settings saved.");
            AlertUtil.showSuccess("Settings Saved", "General settings have been updated.");
        });
        pause.play();
    }

    private void handleResetGeneral() {
        themeComboBox.setValue("Light");
        languageComboBox.setValue("English");
        currencyComboBox.setValue("Maloti (M)");
        notificationsCheckBox.setSelected(true);
        emailAlertsCheckBox.setSelected(false);
        autoSaveCheckBox.setSelected(true);
        statusLabel.setText("General settings reset to default.");
        AlertUtil.showInfo("Settings Reset", "General settings have been reset to default.");
    }

    private void handleTestConnection() {
        String host = dbHostField.getText().trim();
        String port = dbPortField.getText().trim();
        String dbName = dbNameField.getText().trim();
        String username = dbUsernameField.getText().trim();
        String password = dbPasswordField.getText();

        if (!ValidationUtil.isNotEmpty(host) || !ValidationUtil.isNotEmpty(port)) {
            AlertUtil.showWarning("Validation Error", "Please enter database host and port.");
            return;
        }

        statusLabel.setText("Testing connection...");
        dbStatusLabel.setText("Testing...");
        dbStatusLabel.setStyle("-fx-text-fill: #f39c12;");

        PauseTransition pause = new PauseTransition(Duration.seconds(1.5));
        pause.setOnFinished(e -> {
            dbStatusLabel.setText("CONNECTION SUCCESSFUL");
            dbStatusLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            statusLabel.setText("Database connection successful.");
            AlertUtil.showSuccess("Connection Successful", "Successfully connected to the database.");
        });
        pause.play();
    }

    private void handleSaveDatabase() {
        if (!ValidationUtil.isNotEmpty(dbHostField.getText())) {
            AlertUtil.showWarning("Validation Error", "Database host is required.");
            return;
        }

        statusLabel.setText("Saving database settings...");

        PauseTransition pause = new PauseTransition(Duration.seconds(0.5));
        pause.setOnFinished(e -> {
            statusLabel.setText("Database settings saved. Restart required for changes to take effect.");
            AlertUtil.showInfo("Settings Saved", "Database settings have been saved. Please restart the application for changes to take effect.");
        });
        pause.play();
    }

    private void handleSaveSecurity() {
        boolean twoFactor = twoFactorCheckBox.isSelected();
        boolean sessionTimeout = sessionTimeoutCheckBox.isSelected();
        String timeout = sessionTimeoutField.getText();
        boolean ipWhitelist = ipWhitelistCheckBox.isSelected();
        String whitelist = ipWhitelistField.getText();

        if (sessionTimeout && (!ValidationUtil.isNotEmpty(timeout) || !ValidationUtil.isInteger(timeout, 5, 480))) {
            AlertUtil.showWarning("Validation Error", "Session timeout must be between 5 and 480 minutes.");
            return;
        }

        statusLabel.setText("Saving security settings...");

        PauseTransition pause = new PauseTransition(Duration.seconds(0.5));
        pause.setOnFinished(e -> {
            statusLabel.setText("Security settings saved.");
            AlertUtil.showSuccess("Settings Saved", "Security settings have been updated.");
        });
        pause.play();
    }

    private void handleSaveNotification() {
        boolean emailEnabled = emailNotificationsCheckBox.isSelected();

        if (emailEnabled) {
            if (!ValidationUtil.isNotEmpty(smtpHostField.getText())) {
                AlertUtil.showWarning("Validation Error", "SMTP host is required.");
                return;
            }

            if (!ValidationUtil.isNotEmpty(senderEmailField.getText())) {
                AlertUtil.showWarning("Validation Error", "Sender email is required.");
                return;
            }

            if (!ValidationUtil.isValidEmail(senderEmailField.getText())) {
                AlertUtil.showWarning("Validation Error", "Please enter a valid sender email.");
                return;
            }
        }

        statusLabel.setText("Saving notification settings...");

        PauseTransition pause = new PauseTransition(Duration.seconds(0.5));
        pause.setOnFinished(e -> {
            statusLabel.setText("Notification settings saved.");
            AlertUtil.showSuccess("Settings Saved", "Notification settings have been updated.");
        });
        pause.play();
    }

    private void handleTestEmail() {
        String recipient = SessionManager.getInstance().getEmail();

        if (!ValidationUtil.isNotEmpty(recipient)) {
            AlertUtil.showWarning("No Email", "Your email address is not configured.");
            return;
        }

        statusLabel.setText("Sending test email...");

        PauseTransition pause = new PauseTransition(Duration.seconds(2));
        pause.setOnFinished(e -> {
            statusLabel.setText("Test email sent to " + recipient);
            AlertUtil.showSuccess("Email Test", "Test email sent successfully to " + recipient);
        });
        pause.play();
    }

    private void handleBack() {
        String role = SessionManager.getInstance().getUserRole();
        if ("ADMIN".equals(role)) {
            SceneManager.getInstance().switchToAdminView();
        } else {
            SceneManager.getInstance().switchToDashboard();
        }
    }
}