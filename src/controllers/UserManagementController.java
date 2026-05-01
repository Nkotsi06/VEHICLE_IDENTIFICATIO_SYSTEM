package controllers;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.util.Duration;
import utils.AlertUtil;
import utils.SceneManager;
import utils.ValidationUtil;
import utils.SessionManager;
import dao.UserDAO;
import dao.CustomerDAO;
import dao.WorkshopDAO;
import dao.DigitalWalletDAO;
import dao.AuditDAO;
import models.User;
import models.Customer;
import models.Workshop;
import models.DigitalWallet;

import java.sql.SQLException;

public class UserManagementController {

    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, String> usernameColumn;
    @FXML private TableColumn<User, String> fullNameColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> roleColumn;
    @FXML private TableColumn<User, String> statusColumn;

    @FXML private TextField usernameField;
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private ComboBox<String> statusComboBox;

    @FXML private TextField addressField;
    @FXML private TextField phoneField;
    @FXML private TextField nationalIdField;
    @FXML private TextField driversLicenseField;
    @FXML private TextField workshopNameField;
    @FXML private TextField licenseNumberField;

    @FXML private Label customerFieldsLabel;
    @FXML private Label workshopFieldsLabel;

    @FXML private Label statusLabel;  // Added this - matches the FXML

    @FXML private Button addButton;
    @FXML private Button updateButton;
    @FXML private Button deleteButton;
    @FXML private Button resetPasswordButton;
    @FXML private Button refreshButton;
    @FXML private Button backButton;

    @FXML private ProgressIndicator loadProgress;
    @FXML private ProgressBar operationProgress;
    @FXML private Button fadeButton;
    @FXML private Pagination usersPagination;

    private UserDAO userDAO;
    private CustomerDAO customerDAO;
    private WorkshopDAO workshopDAO;
    private DigitalWalletDAO walletDAO;
    private AuditDAO auditDAO;
    private User selectedUser;
    private int currentPage = 0;
    private int pageSize = 10;
    private java.util.List<User> fullUserList;

    @FXML
    public void initialize() {
        userDAO = new UserDAO();
        customerDAO = new CustomerDAO();
        workshopDAO = new WorkshopDAO();
        walletDAO = new DigitalWalletDAO();
        auditDAO = new AuditDAO();

        setupTableColumns();
        loadUsers();
        setupComboBoxes();
        setupButtonHandlers();
        setupTableSelection();
        setupRoleBasedFields();
        applyVisualEffects();
    }

    private void setupTableColumns() {
        usernameColumn.setCellValueFactory(cellData -> cellData.getValue().usernameProperty());
        fullNameColumn.setCellValueFactory(cellData -> cellData.getValue().fullNameProperty());
        emailColumn.setCellValueFactory(cellData -> cellData.getValue().emailProperty());
        roleColumn.setCellValueFactory(cellData -> cellData.getValue().roleProperty());
        statusColumn.setCellValueFactory(cellData -> {
            boolean isActive = cellData.getValue().isActive();
            return new javafx.beans.property.SimpleStringProperty(isActive ? "ACTIVE" : "INACTIVE");
        });

        usernameColumn.setStyle("-fx-alignment: CENTER;");
        fullNameColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        emailColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        roleColumn.setStyle("-fx-alignment: CENTER;");
        statusColumn.setStyle("-fx-alignment: CENTER;");
    }

    private void loadUsers() {
        if (loadProgress != null) loadProgress.setVisible(true);

        try {
            fullUserList = userDAO.findAll();
            usersTable.getItems().setAll(fullUserList);
            if (usersPagination != null && fullUserList != null) {
                int totalPages = (int) Math.ceil((double) fullUserList.size() / pageSize);
                usersPagination.setPageCount(Math.max(1, totalPages));
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Load Failed", "Failed to load users.");
        } finally {
            if (loadProgress != null) loadProgress.setVisible(false);
        }
    }

    private void setupComboBoxes() {
        roleComboBox.getItems().addAll("CUSTOMER", "WORKSHOP", "INSURANCE", "POLICE");
        statusComboBox.getItems().addAll("ACTIVE", "INACTIVE");
        statusComboBox.setValue("ACTIVE");
        roleComboBox.setValue("CUSTOMER");
    }

    private void setupButtonHandlers() {
        addButton.setOnAction(event -> handleAdd());
        updateButton.setOnAction(event -> handleUpdate());
        deleteButton.setOnAction(event -> handleDelete());
        resetPasswordButton.setOnAction(event -> handleResetPassword());
        refreshButton.setOnAction(event -> loadUsers());
        backButton.setOnAction(event -> SceneManager.getInstance().switchToAdminView());
    }

    private void setupTableSelection() {
        usersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedUser = newSelection;
                displayUserDetails(selectedUser);
            }
        });
    }

    private void setupRoleBasedFields() {
        roleComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean isCustomer = "CUSTOMER".equals(newVal);
            boolean isWorkshop = "WORKSHOP".equals(newVal);

            customerFieldsLabel.setVisible(isCustomer);
            addressField.setVisible(isCustomer || isWorkshop);
            phoneField.setVisible(isCustomer || isWorkshop);
            nationalIdField.setVisible(isCustomer);
            driversLicenseField.setVisible(isCustomer);

            workshopFieldsLabel.setVisible(isWorkshop);
            workshopNameField.setVisible(isWorkshop);
            licenseNumberField.setVisible(isWorkshop);

            if (!isCustomer) {
                nationalIdField.clear();
                driversLicenseField.clear();
            }
            if (!isWorkshop) {
                workshopNameField.clear();
                licenseNumberField.clear();
            }
            if (!isCustomer && !isWorkshop) {
                addressField.clear();
                phoneField.clear();
            }
        });
    }

    private void displayUserDetails(User user) {
        usernameField.setText(user.getUsername());
        fullNameField.setText(user.getFullName());
        emailField.setText(user.getEmail());
        passwordField.clear();

        if ("ADMIN".equals(user.getRole())) {
            roleComboBox.setDisable(true);
            statusComboBox.setDisable(true);
            resetPasswordButton.setDisable(true);
            deleteButton.setDisable(true);
            updateButton.setDisable(true);
            roleComboBox.setValue("ADMIN");
        } else {
            roleComboBox.setDisable(false);
            statusComboBox.setDisable(false);
            resetPasswordButton.setDisable(false);
            deleteButton.setDisable(false);
            updateButton.setDisable(false);
            roleComboBox.setValue(user.getRole());
        }

        statusComboBox.setValue(user.isActive() ? "ACTIVE" : "INACTIVE");

        customerFieldsLabel.setVisible(false);
        workshopFieldsLabel.setVisible(false);
        addressField.setVisible(false);
        phoneField.setVisible(false);
        nationalIdField.setVisible(false);
        driversLicenseField.setVisible(false);
        workshopNameField.setVisible(false);
        licenseNumberField.setVisible(false);

        addressField.clear();
        phoneField.clear();
        nationalIdField.clear();
        driversLicenseField.clear();
        workshopNameField.clear();
        licenseNumberField.clear();

        try {
            if ("CUSTOMER".equals(user.getRole())) {
                Customer customer = customerDAO.findByUserId(user.getId());
                if (customer != null) {
                    customerFieldsLabel.setVisible(true);
                    addressField.setVisible(true);
                    phoneField.setVisible(true);
                    nationalIdField.setVisible(true);
                    driversLicenseField.setVisible(true);
                    addressField.setText(customer.getAddress() != null ? customer.getAddress() : "");
                    phoneField.setText(customer.getPhone() != null ? customer.getPhone() : "");
                    nationalIdField.setText(customer.getNationalId() != null ? customer.getNationalId() : "");
                    driversLicenseField.setText(customer.getDriversLicenseNumber() != null ? customer.getDriversLicenseNumber() : "");
                }
            } else if ("WORKSHOP".equals(user.getRole())) {
                Workshop workshop = workshopDAO.findByUserId(user.getId());
                if (workshop != null) {
                    workshopFieldsLabel.setVisible(true);
                    workshopNameField.setVisible(true);
                    addressField.setVisible(true);
                    phoneField.setVisible(true);
                    licenseNumberField.setVisible(true);
                    workshopNameField.setText(workshop.getWorkshopName() != null ? workshop.getWorkshopName() : "");
                    addressField.setText(workshop.getAddress() != null ? workshop.getAddress() : "");
                    phoneField.setText(workshop.getPhone() != null ? workshop.getPhone() : "");
                    licenseNumberField.setText(workshop.getLicenseNumber() != null ? workshop.getLicenseNumber() : "");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleAdd() {
        if (!validateUserInputs()) return;

        String role = roleComboBox.getValue();
        String username = usernameField.getText().trim();

        if ("ADMIN".equals(role)) {
            AlertUtil.showError("Access Denied", "Admin users cannot be created through the application.");
            return;
        }

        showProgress(true);
        updateProgress(0.2);

        try {
            User existingUser = userDAO.findByUsername(username);
            if (existingUser != null) {
                AlertUtil.showError("Duplicate Username", "Username '" + username + "' already exists.");
                usernameField.requestFocus();
                showProgress(false);
                return;
            }

            updateProgress(0.4);

            User user = new User();
            user.setUsername(username);
            user.setPassword(passwordField.getText());
            user.setRole(role);
            user.setFullName(fullNameField.getText().trim());
            user.setEmail(emailField.getText().trim());
            user.setActive("ACTIVE".equals(statusComboBox.getValue()));

            int userId = userDAO.insertAndGetId(user);
            updateProgress(0.6);

            if (userId > 0) {
                if ("CUSTOMER".equals(user.getRole())) {
                    Customer customer = new Customer();
                    customer.setUserId(userId);
                    customer.setName(fullNameField.getText().trim());
                    customer.setEmail(emailField.getText().trim());
                    customer.setAddress(addressField.getText());
                    customer.setPhone(phoneField.getText());
                    customer.setNationalId(nationalIdField.getText());
                    customer.setDriversLicenseNumber(driversLicenseField.getText());
                    customerDAO.insert(customer);

                    Customer newCustomer = customerDAO.findByUserId(userId);
                    if (newCustomer != null) {
                        DigitalWallet wallet = new DigitalWallet();
                        wallet.setCustomerId(newCustomer.getId());
                        wallet.setBalance(0.0);
                        walletDAO.insert(wallet);
                    }
                } else if ("WORKSHOP".equals(user.getRole())) {
                    Workshop workshop = new Workshop();
                    workshop.setUserId(userId);
                    workshop.setWorkshopName(workshopNameField.getText().trim());
                    workshop.setAddress(addressField.getText());
                    workshop.setPhone(phoneField.getText());
                    workshop.setEmail(emailField.getText().trim());
                    workshop.setLicenseNumber(licenseNumberField.getText());
                    workshop.setApproved(false);
                    workshopDAO.insert(workshop);
                }

                updateProgress(0.9);
                int currentUserId = SessionManager.getInstance().getUserId();
                auditDAO.logAction(currentUserId, "CREATE_USER: " + username + " (Role: " + role + ")", "127.0.0.1");

                AlertUtil.showSuccess("User added successfully.");
                updateProgress(1.0);
                clearForm();
                loadUsers();
            } else {
                AlertUtil.showError("Add Failed", "Failed to add user.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.contains("duplicate key")) {
                AlertUtil.showError("Duplicate Username", "Username '" + usernameField.getText().trim() + "' already exists.");
            } else {
                AlertUtil.showError("Database Error", "An error occurred while adding user: " + e.getMessage());
            }
        } finally {
            hideProgressWithDelay();
        }
    }

    private void handleUpdate() {
        if (selectedUser == null) {
            AlertUtil.showWarning("No Selection", "Please select a user to update.");
            return;
        }

        if ("ADMIN".equals(selectedUser.getRole()) && "Nqosa".equals(selectedUser.getUsername())) {
            AlertUtil.showError("Access Denied", "The default admin user cannot be modified.");
            return;
        }

        String newRole = roleComboBox.getValue();
        if ("ADMIN".equals(newRole) && !"ADMIN".equals(selectedUser.getRole())) {
            AlertUtil.showError("Access Denied", "Cannot promote a user to ADMIN role.");
            return;
        }

        if ("ADMIN".equals(selectedUser.getRole()) && !"ADMIN".equals(newRole)) {
            AlertUtil.showError("Access Denied", "Cannot change the role of an admin user.");
            return;
        }

        String newUsername = usernameField.getText().trim();
        if (!newUsername.equals(selectedUser.getUsername())) {
            try {
                User existingUser = userDAO.findByUsername(newUsername);
                if (existingUser != null) {
                    AlertUtil.showError("Duplicate Username", "Username '" + newUsername + "' already exists.");
                    usernameField.requestFocus();
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (!validateUserInputs()) return;

        showProgress(true);
        updateProgress(0.3);

        try {
            String oldRole = selectedUser.getRole();
            String oldUsername = selectedUser.getUsername();

            selectedUser.setUsername(newUsername);
            selectedUser.setFullName(fullNameField.getText().trim());
            selectedUser.setEmail(emailField.getText().trim());

            if (!"ADMIN".equals(selectedUser.getRole())) {
                selectedUser.setRole(newRole);
                selectedUser.setActive("ACTIVE".equals(statusComboBox.getValue()));
            } else {
                selectedUser.setActive(true);
            }

            boolean success = userDAO.update(selectedUser);
            updateProgress(0.7);

            if (success) {
                if ("CUSTOMER".equals(selectedUser.getRole())) {
                    Customer customer = customerDAO.findByUserId(selectedUser.getId());
                    if (customer != null) {
                        customer.setName(fullNameField.getText().trim());
                        customer.setEmail(emailField.getText().trim());
                        customer.setAddress(addressField.getText());
                        customer.setPhone(phoneField.getText());
                        customer.setNationalId(nationalIdField.getText());
                        customer.setDriversLicenseNumber(driversLicenseField.getText());
                        customerDAO.update(customer);
                    }
                } else if ("WORKSHOP".equals(selectedUser.getRole())) {
                    Workshop workshop = workshopDAO.findByUserId(selectedUser.getId());
                    if (workshop != null) {
                        workshop.setWorkshopName(workshopNameField.getText().trim());
                        workshop.setAddress(addressField.getText());
                        workshop.setPhone(phoneField.getText());
                        workshop.setEmail(emailField.getText().trim());
                        workshop.setLicenseNumber(licenseNumberField.getText());
                        workshopDAO.update(workshop);
                    }
                }

                updateProgress(1.0);
                int currentUserId = SessionManager.getInstance().getUserId();
                auditDAO.logAction(currentUserId, "UPDATE_USER: " + oldUsername + " -> " + newUsername + " (Role: " + oldRole + " -> " + newRole + ")", "127.0.0.1");

                AlertUtil.showSuccess("User updated successfully.");
                loadUsers();
            } else {
                AlertUtil.showError("Update Failed", "Failed to update user.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Database Error", "An error occurred while updating user.");
        } finally {
            hideProgressWithDelay();
        }
    }

    private void handleDelete() {
        if (selectedUser == null) {
            AlertUtil.showWarning("No Selection", "Please select a user to delete.");
            return;
        }

        if ("ADMIN".equals(selectedUser.getRole())) {
            AlertUtil.showError("Access Denied", "Admin users cannot be deleted.");
            return;
        }

        // Confirm deletion with warning about cascade
        boolean confirmed = AlertUtil.showConfirmation("Delete User",
                "⚠️ WARNING: This will permanently delete user '" + selectedUser.getUsername() +
                        "' and ALL associated data including:\n\n" +
                        "• Personal information\n" +
                        "• Vehicles (if customer)\n" +
                        "• Digital wallet and transactions (if customer)\n" +
                        "• Service records (if workshop)\n" +
                        "• Activity logs\n" +
                        "• Notifications\n\n" +
                        "This action CANNOT be undone.\n\n" +
                        "Are you ABSOLUTELY sure you want to proceed?");

        if (confirmed) {
            showProgress(true);
            updateProgress(0.3);
            statusLabel.setText("Deleting user and all associated data...");

            try {
                String deletedUsername = selectedUser.getUsername();
                String deletedRole = selectedUser.getRole();
                int deletedUserId = selectedUser.getId();

                updateProgress(0.6);

                // Perform cascading delete
                boolean success = userDAO.delete(deletedUserId);

                updateProgress(0.9);

                if (success) {
                    updateProgress(1.0);
                    // Log the user deletion
                    int currentUserId = SessionManager.getInstance().getUserId();
                    auditDAO.logAction(currentUserId, "DELETE_USER_COMPLETE: " + deletedUsername + " (Role: " + deletedRole + ", ID: " + deletedUserId + ")", "127.0.0.1");

                    AlertUtil.showSuccess("User Deleted",
                            "User '" + deletedUsername + "' and all associated data have been permanently deleted.");
                    statusLabel.setText("User deleted successfully");
                    clearForm();
                    loadUsers();
                } else {
                    AlertUtil.showError("Delete Failed", "Failed to delete user. Please check logs for details.");
                    statusLabel.setText("Delete failed");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                String errorMsg = e.getMessage();
                if (errorMsg != null && errorMsg.contains("foreign key")) {
                    AlertUtil.showError("Delete Failed",
                            "Cannot delete user due to existing references.\n" +
                                    "Please ensure all associated records are removed first.\n\n" +
                                    "Error: " + errorMsg);
                } else if (errorMsg != null && errorMsg.contains("violates")) {
                    AlertUtil.showError("Delete Failed",
                            "User has dependent records that cannot be automatically deleted.\n" +
                                    "Error: " + errorMsg);
                } else {
                    AlertUtil.showError("Delete Failed", "An error occurred while deleting user: " + errorMsg);
                }
                statusLabel.setText("Delete failed: " + errorMsg);
            } catch (Exception e) {
                e.printStackTrace();
                AlertUtil.showError("Delete Error", "Unexpected error: " + e.getMessage());
                statusLabel.setText("Delete error: " + e.getMessage());
            } finally {
                hideProgressWithDelay();
            }
        }
    }

    private void handleResetPassword() {
        if (selectedUser == null) {
            AlertUtil.showWarning("No Selection", "Please select a user to reset password.");
            return;
        }

        if ("ADMIN".equals(selectedUser.getRole())) {
            AlertUtil.showError("Access Denied", "Admin user passwords cannot be reset.");
            return;
        }

        boolean confirmed = AlertUtil.showConfirmation("Reset Password",
                "Reset password for " + selectedUser.getUsername() + " to default?");

        if (confirmed) {
            showProgress(true);
            try {
                String defaultPassword = "password123";
                boolean success = userDAO.updatePassword(selectedUser.getId(), defaultPassword);
                updateProgress(1.0);

                if (success) {
                    int currentUserId = SessionManager.getInstance().getUserId();
                    auditDAO.logAction(currentUserId, "RESET_PASSWORD: " + selectedUser.getUsername(), "127.0.0.1");

                    AlertUtil.showSuccess("Password reset successfully. New password: " + defaultPassword);
                } else {
                    AlertUtil.showError("Reset Failed", "Failed to reset password.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                AlertUtil.showError("Database Error", "An error occurred while resetting password.");
            } finally {
                hideProgressWithDelay();
            }
        }
    }

    private void clearForm() {
        usernameField.clear();
        fullNameField.clear();
        emailField.clear();
        passwordField.clear();

        roleComboBox.setValue("CUSTOMER");
        statusComboBox.setValue("ACTIVE");

        addressField.clear();
        phoneField.clear();
        nationalIdField.clear();
        driversLicenseField.clear();
        workshopNameField.clear();
        licenseNumberField.clear();

        customerFieldsLabel.setVisible(true);
        workshopFieldsLabel.setVisible(false);
        addressField.setVisible(true);
        phoneField.setVisible(true);
        nationalIdField.setVisible(true);
        driversLicenseField.setVisible(true);
        workshopNameField.setVisible(false);
        licenseNumberField.setVisible(false);

        selectedUser = null;
        usersTable.getSelectionModel().clearSelection();

        roleComboBox.setDisable(false);
        statusComboBox.setDisable(false);
        resetPasswordButton.setDisable(false);
        deleteButton.setDisable(false);
        updateButton.setDisable(false);

        usernameField.requestFocus();
    }

    private boolean validateUserInputs() {
        if (!ValidationUtil.isNotEmpty(usernameField.getText())) {
            AlertUtil.showWarning("Validation Error", "Username is required.");
            usernameField.requestFocus();
            return false;
        }

        if (!ValidationUtil.isNotEmpty(fullNameField.getText())) {
            AlertUtil.showWarning("Validation Error", "Full name is required.");
            fullNameField.requestFocus();
            return false;
        }

        if (!ValidationUtil.isNotEmpty(emailField.getText())) {
            AlertUtil.showWarning("Validation Error", "Email is required.");
            emailField.requestFocus();
            return false;
        }

        if (!ValidationUtil.isValidEmail(emailField.getText())) {
            AlertUtil.showWarning("Validation Error", "Please enter a valid email address.");
            emailField.requestFocus();
            return false;
        }

        if (selectedUser == null && !ValidationUtil.isNotEmpty(passwordField.getText())) {
            AlertUtil.showWarning("Validation Error", "Password is required for new users.");
            passwordField.requestFocus();
            return false;
        }

        if (selectedUser == null && passwordField.getText().length() < 4) {
            AlertUtil.showWarning("Validation Error", "Password must be at least 4 characters.");
            passwordField.requestFocus();
            return false;
        }

        if (roleComboBox.getValue() == null) {
            AlertUtil.showWarning("Validation Error", "Please select a role.");
            return false;
        }

        String role = roleComboBox.getValue();

        if ("CUSTOMER".equals(role)) {
            if (ValidationUtil.isNotEmpty(phoneField.getText()) && phoneField.getText().length() < 10 && phoneField.getText().length() > 0) {
                AlertUtil.showWarning("Validation Error", "Phone number should be at least 10 digits.");
                phoneField.requestFocus();
                return false;
            }
        }

        if ("WORKSHOP".equals(role)) {
            if (!ValidationUtil.isNotEmpty(workshopNameField.getText())) {
                AlertUtil.showWarning("Validation Error", "Workshop name is required.");
                workshopNameField.requestFocus();
                return false;
            }
            if (!ValidationUtil.isNotEmpty(licenseNumberField.getText())) {
                AlertUtil.showWarning("Validation Error", "License number is required.");
                licenseNumberField.requestFocus();
                return false;
            }
        }

        return true;
    }

    private void showProgress(boolean show) {
        if (operationProgress != null) {
            operationProgress.setVisible(show);
            operationProgress.setProgress(0);
        }
        if (loadProgress != null) loadProgress.setVisible(show);
    }

    private void updateProgress(double progress) {
        if (operationProgress != null) operationProgress.setProgress(progress);
    }

    private void hideProgressWithDelay() {
        PauseTransition delay = new PauseTransition(Duration.seconds(1.5));
        delay.setOnFinished(event -> {
            if (operationProgress != null) {
                operationProgress.setVisible(false);
                operationProgress.setProgress(0);
            }
            if (loadProgress != null) loadProgress.setVisible(false);
        });
        delay.play();
    }

    private void applyVisualEffects() {
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(5.0);
        dropShadow.setOffsetX(2.0);
        dropShadow.setOffsetY(2.0);

        if (addButton != null) addButton.setEffect(dropShadow);
        if (updateButton != null) updateButton.setEffect(dropShadow);
        if (deleteButton != null) deleteButton.setEffect(dropShadow);
        if (refreshButton != null) refreshButton.setEffect(dropShadow);
        if (resetPasswordButton != null) resetPasswordButton.setEffect(dropShadow);
        if (backButton != null) backButton.setEffect(dropShadow);

        if (fadeButton != null) {
            FadeTransition fadeTransition = new FadeTransition(Duration.seconds(2), fadeButton);
            fadeTransition.setFromValue(1.0);
            fadeTransition.setToValue(0.3);
            fadeTransition.setCycleCount(FadeTransition.INDEFINITE);
            fadeTransition.setAutoReverse(true);
            fadeTransition.play();
        }
    }
}