package controllers;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import utils.AlertUtil;
import utils.SceneManager;
import dao.RolePermissionDAO;
import models.RolePermission;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class RoleManagementController {

    @FXML private TableView<RolePermission> permissionsTable;
    @FXML private TableColumn<RolePermission, String> permissionKeyColumn;
    @FXML private TableColumn<RolePermission, Boolean> permissionValueColumn;

    @FXML private ComboBox<String> roleComboBox;
    @FXML private ComboBox<String> permissionComboBox;
    @FXML private CheckBox grantCheckBox;

    @FXML private Button applyButton;
    @FXML private Button refreshButton;
    @FXML private Button backButton;
    @FXML private Button fadeButton;

    @FXML private ProgressIndicator loadProgress;
    @FXML private ProgressBar operationProgress;
    @FXML private Label permissionCountLabel;
    @FXML private Label statusLabel;

    private RolePermissionDAO permissionDAO;
    private ObservableList<RolePermission> permissionList;

    // Predefined list of all possible permissions in the system
    private final String[] ALL_PERMISSIONS = {
            "VIEW_DASHBOARD",
            "MANAGE_USERS",
            "VIEW_USERS",
            "CREATE_USER",
            "EDIT_USER",
            "DELETE_USER",
            "RESET_PASSWORD",
            "MANAGE_ROLES",
            "VIEW_VEHICLES",
            "REGISTER_VEHICLE",
            "EDIT_VEHICLE",
            "DELETE_VEHICLE",
            "REPORT_STOLEN",
            "VIEW_STOLEN_REPORTS",
            "MANAGE_INSURANCE",
            "VIEW_INSURANCE",
            "CREATE_INSURANCE",
            "PROCESS_CLAIMS",
            "VIEW_CLAIMS",
            "MANAGE_WORKSHOPS",
            "VIEW_WORKSHOPS",
            "APPROVE_WORKSHOP",
            "MANAGE_FINES",
            "VIEW_FINES",
            "ISSUE_FINE",
            "PAY_FINE",
            "VIEW_REPORTS",
            "EXPORT_DATA",
            "VIEW_AUDIT_LOGS",
            "MANAGE_NOTIFICATIONS",
            "SEND_NOTIFICATIONS",
            "VIEW_SYSTEM_HEALTH",
            "MANAGE_BACKUPS",
            "CONFIGURE_SYSTEM"
    };

    @FXML
    public void initialize() {
        permissionDAO = new RolePermissionDAO();
        permissionList = FXCollections.observableArrayList();

        setupTableColumns();
        loadRoles();
        loadPermissionsIntoComboBox();
        setupButtonHandlers();
        applyVisualEffects();

        statusLabel.setText("Ready");

        // Add listener to role selection to auto-load permissions
        roleComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadPermissionsForRole();
                updatePermissionComboBoxState();
            }
        });

        // Load permissions for default selected role immediately
        loadPermissionsForRole();
    }

    private void setupTableColumns() {
        permissionKeyColumn.setCellValueFactory(cellData -> cellData.getValue().permissionKeyProperty());
        permissionValueColumn.setCellValueFactory(cellData -> cellData.getValue().permissionValueProperty());

        permissionKeyColumn.setStyle("-fx-alignment: CENTER-LEFT; -fx-padding: 5;");
        permissionValueColumn.setStyle("-fx-alignment: CENTER;");

        // Custom cell factory for Permission Key column with better styling
        permissionKeyColumn.setCellFactory(column -> new TableCell<RolePermission, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(formatPermissionKey(item));
                    setStyle("-fx-font-family: monospace; -fx-font-size: 12px; -fx-padding: 5;");
                }
            }
        });

        // Custom cell factory for Granted column to show "Granted"/"Denied" with colors
        permissionValueColumn.setCellFactory(column -> new TableCell<RolePermission, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    if (item) {
                        setText("✓ Granted");
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-alignment: CENTER;");
                    } else {
                        setText("✗ Denied");
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-alignment: CENTER;");
                    }
                }
            }
        });

        permissionsTable.setItems(permissionList);

        // Add row factory for better visual feedback
        permissionsTable.setRowFactory(tv -> new TableRow<RolePermission>() {
            @Override
            protected void updateItem(RolePermission item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else if (item.hasPermission()) {
                    setStyle("-fx-background-color: rgba(39, 174, 96, 0.05);");
                } else {
                    setStyle("-fx-background-color: rgba(231, 76, 60, 0.05);");
                }
            }
        });
    }

    private String formatPermissionKey(String key) {
        // Convert PERMISSION_NAME to "Permission Name" format for better readability
        String[] parts = key.split("_");
        StringBuilder formatted = new StringBuilder();
        for (String part : parts) {
            if (part.length() > 0) {
                formatted.append(part.charAt(0)).append(part.substring(1).toLowerCase()).append(" ");
            }
        }
        return formatted.toString().trim();
    }

    private void loadRoles() {
        roleComboBox.getItems().addAll("ADMIN", "POLICE", "CUSTOMER", "WORKSHOP", "INSURANCE");
        roleComboBox.setValue("POLICE");
    }

    private void loadPermissionsIntoComboBox() {
        permissionComboBox.getItems().clear();
        permissionComboBox.getItems().addAll(ALL_PERMISSIONS);
        permissionComboBox.setPromptText("Select a permission to manage");

        // Add tooltip to show full permission name
        permissionComboBox.setCellFactory(listView -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatPermissionKey(item));
                    setTooltip(new Tooltip(item));
                }
            }
        });

        permissionComboBox.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Select permission");
                } else {
                    setText(formatPermissionKey(item));
                }
            }
        });
    }

    private void updatePermissionComboBoxState() {
        String currentRole = roleComboBox.getValue();
        if (currentRole == null) {
            permissionComboBox.setDisable(true);
            grantCheckBox.setDisable(true);
            applyButton.setDisable(true);
            return;
        }

        // ADMIN role has all permissions and cannot be modified
        if ("ADMIN".equals(currentRole)) {
            permissionComboBox.setDisable(true);
            grantCheckBox.setDisable(true);
            applyButton.setDisable(true);
            statusLabel.setText("ADMIN role has all permissions by default and cannot be modified");
        } else {
            permissionComboBox.setDisable(false);
            grantCheckBox.setDisable(false);
            applyButton.setDisable(false);
        }
    }

    private void setupButtonHandlers() {
        applyButton.setOnAction(event -> handleApply());
        refreshButton.setOnAction(event -> {
            loadPermissionsForRole();
            statusLabel.setText("Permissions refreshed");
            PauseTransition reset = new PauseTransition(Duration.seconds(2));
            reset.setOnFinished(e -> statusLabel.setText("Ready"));
            reset.play();
        });
        backButton.setOnAction(event -> SceneManager.getInstance().switchToAdminView());

        if (fadeButton != null) {
            fadeButton.setOnAction(event -> {
                statusLabel.setText("Animation button pressed");
                PauseTransition reset = new PauseTransition(Duration.seconds(2));
                reset.setOnFinished(e -> statusLabel.setText("Ready"));
                reset.play();
            });
        }

        // When a permission is selected, show its current status in the checkbox
        permissionComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && roleComboBox.getValue() != null) {
                updateCheckBoxForSelectedPermission();
            }
        });
    }

    private void updateCheckBoxForSelectedPermission() {
        String role = roleComboBox.getValue();
        String permission = permissionComboBox.getValue();

        if (role == null || permission == null) return;

        try {
            boolean hasPermission = permissionDAO.hasPermission(role, permission);
            grantCheckBox.setSelected(hasPermission);
            statusLabel.setText("Current status for " + formatPermissionKey(permission) + ": " + (hasPermission ? "Granted" : "Denied"));
        } catch (Exception e) {
            e.printStackTrace();
            grantCheckBox.setSelected(false);
        }
    }

    private void loadPermissionsForRole() {
        String role = roleComboBox.getValue();
        if (role == null) {
            statusLabel.setText("Please select a role");
            return;
        }

        showLoadProgress(true);
        statusLabel.setText("Loading permissions for role: " + role + "...");

        try {
            List<RolePermission> permissions = permissionDAO.findByRole(role);

            // Create a set of existing permission keys for quick lookup
            Set<String> existingKeys = new HashSet<>();
            for (RolePermission rp : permissions) {
                existingKeys.add(rp.getPermissionKey());
            }

            // If role is ADMIN, ensure all permissions are granted
            if ("ADMIN".equals(role)) {
                permissionList.clear();
                for (String permissionKey : ALL_PERMISSIONS) {
                    RolePermission rp = new RolePermission();
                    rp.setRoleName(role);
                    rp.setPermissionKey(permissionKey);
                    rp.setPermissionValue(true);
                    permissionList.add(rp);
                }
                permissionCountLabel.setText("Total permissions: " + permissionList.size() + " (All Granted for ADMIN)");
                statusLabel.setText("ADMIN role has all " + permissionList.size() + " permissions granted");
            } else {
                // For other roles, ensure we show all permissions with their current status
                permissionList.clear();
                for (String permissionKey : ALL_PERMISSIONS) {
                    RolePermission rp = new RolePermission();
                    rp.setRoleName(role);
                    rp.setPermissionKey(permissionKey);
                    // Check if this permission exists in the database
                    boolean found = false;
                    for (RolePermission existing : permissions) {
                        if (existing.getPermissionKey().equals(permissionKey)) {
                            rp.setPermissionValue(existing.hasPermission());
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        // Default to false for permissions not explicitly set
                        rp.setPermissionValue(false);
                    }
                    permissionList.add(rp);
                }
                permissionCountLabel.setText("Total permissions: " + permissionList.size());
                statusLabel.setText("Loaded " + permissionList.size() + " permissions for role: " + role);
            }

            permissionsTable.setItems(permissionList);

        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error loading permissions: " + e.getMessage());
            AlertUtil.showError("Load Failed", "Failed to load permissions: " + e.getMessage());
        } finally {
            showLoadProgress(false);
        }
    }

    private void handleApply() {
        String role = roleComboBox.getValue();
        String permissionKey = permissionComboBox.getValue();
        boolean grant = grantCheckBox.isSelected();

        if (role == null) {
            AlertUtil.showWarning("Validation Error", "Please select a role.");
            return;
        }

        if (permissionKey == null) {
            AlertUtil.showWarning("Validation Error", "Please select a permission from the dropdown.");
            return;
        }

        if ("ADMIN".equals(role)) {
            AlertUtil.showWarning("Access Denied", "ADMIN role permissions cannot be modified as they have all permissions by default.");
            return;
        }

        showOperationProgress(true);
        statusLabel.setText((grant ? "Granting" : "Revoking") + " permission: " + formatPermissionKey(permissionKey) + " for role " + role + "...");
        updateProgress(0.3);

        try {
            boolean success;
            updateProgress(0.6);

            if (grant) {
                success = permissionDAO.grantPermission(role, permissionKey);
            } else {
                success = permissionDAO.revokePermission(role, permissionKey);
            }

            updateProgress(0.9);

            if (success) {
                updateProgress(1.0);
                AlertUtil.showSuccess((grant ? "Granted" : "Revoked") + " permission: " +
                        formatPermissionKey(permissionKey) + " for role " + role);
                statusLabel.setText("Permission updated successfully");

                // Reload permissions to reflect changes
                loadPermissionsForRole();

                // Clear selection after successful update
                permissionComboBox.setValue(null);
                grantCheckBox.setSelected(false);
            } else {
                statusLabel.setText("Failed to update permission");
                AlertUtil.showError("Update Failed", "Failed to update permission. It may already be in the desired state.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error: " + e.getMessage());
            AlertUtil.showError("Database Error", "An error occurred while updating permission: " + e.getMessage());
        } finally {
            hideProgressAfterDelay();
        }
    }

    private void applyVisualEffects() {
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(5.0);
        dropShadow.setOffsetX(2.0);
        dropShadow.setOffsetY(2.0);
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.3));

        refreshButton.setEffect(dropShadow);
        applyButton.setEffect(dropShadow);
        backButton.setEffect(dropShadow);

        if (fadeButton != null) {
            fadeButton.setEffect(dropShadow);

            FadeTransition infiniteFadeTransition = new FadeTransition(Duration.seconds(1.5), fadeButton);
            infiniteFadeTransition.setFromValue(1.0);
            infiniteFadeTransition.setToValue(0.3);
            infiniteFadeTransition.setCycleCount(Animation.INDEFINITE);
            infiniteFadeTransition.setAutoReverse(true);
            infiniteFadeTransition.play();
        }

        DropShadow tableShadow = new DropShadow();
        tableShadow.setRadius(3.0);
        tableShadow.setOffsetX(2.0);
        tableShadow.setOffsetY(2.0);
        tableShadow.setColor(Color.rgb(0, 0, 0, 0.2));
        permissionsTable.setEffect(tableShadow);
    }

    private void showLoadProgress(boolean show) {
        if (loadProgress != null) {
            loadProgress.setVisible(show);
        }
    }

    private void showOperationProgress(boolean show) {
        if (operationProgress != null) {
            operationProgress.setVisible(show);
            operationProgress.setProgress(0);
        }
    }

    private void updateProgress(double progress) {
        if (operationProgress != null) {
            operationProgress.setProgress(progress);
        }
    }

    private void hideProgressAfterDelay() {
        PauseTransition delay = new PauseTransition(Duration.seconds(1.5));
        delay.setOnFinished(event -> {
            if (operationProgress != null) {
                operationProgress.setVisible(false);
                operationProgress.setProgress(0);
            }
            if (loadProgress != null) {
                loadProgress.setVisible(false);
            }
        });
        delay.play();
    }
}