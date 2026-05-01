package controllers;

import utils.AlertUtil;
import utils.SceneManager;
import utils.SessionManager;

public class LogoutController {

    public void handleLogout() {
        boolean confirmed = AlertUtil.showConfirmation("Logout", "Are you sure you want to logout?");

        if (confirmed) {
            SessionManager.getInstance().clearSession();
            SceneManager.getInstance().switchToLogin();
        }
    }
}