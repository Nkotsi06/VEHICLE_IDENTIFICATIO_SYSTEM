package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import utils.AlertUtil;
import utils.SceneManager;
import utils.SessionManager;

/**
 * This controller is deprecated. Use MenuBarComponentController instead.
 * Kept for backward compatibility.
 */
public class MenuBarController {

    @FXML private MenuItem exitMenuItem;
    @FXML private MenuItem logoutMenuItem;
    @FXML private MenuItem aboutMenuItem;
    @FXML private MenuItem helpMenuItem;

    @FXML
    public void initialize() {
        setupEventHandlers();
    }

    private void setupEventHandlers() {
        if (exitMenuItem != null) exitMenuItem.setOnAction(event -> handleExit());
        if (logoutMenuItem != null) logoutMenuItem.setOnAction(event -> handleLogout());
        if (aboutMenuItem != null) aboutMenuItem.setOnAction(event -> showAboutDialog());
        if (helpMenuItem != null) helpMenuItem.setOnAction(event -> showHelpDialog());
    }

    private void handleExit() {
        boolean confirmed = AlertUtil.showConfirmation("Exit", "Are you sure you want to exit?");
        if (confirmed) javafx.application.Platform.exit();
    }

    private void handleLogout() {
        boolean confirmed = AlertUtil.showConfirmation("Logout", "Are you sure you want to logout?");
        if (confirmed) {
            SessionManager.getInstance().clearSession();
            SceneManager.getInstance().switchToLogin();
        }
    }

    private void showAboutDialog() {
        AlertUtil.showInfo("About", "Vehicle Identification System v2.0");
    }

    private void showHelpDialog() {
        AlertUtil.showInfo("Help", "Contact support for assistance.");
    }
}