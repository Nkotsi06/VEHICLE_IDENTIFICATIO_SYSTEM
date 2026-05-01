package controllers;

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
import utils.SessionManager;
import dao.NotificationDAO;
import models.Notification;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NotificationController {

    private static final Logger LOGGER = Logger.getLogger(NotificationController.class.getName());

    @FXML private TableView<Notification> notificationsTable;
    @FXML private TableColumn<Notification, String> messageColumn;
    @FXML private TableColumn<Notification, String> typeColumn;
    @FXML private TableColumn<Notification, String> createdAtColumn;
    @FXML private TableColumn<Notification, String> statusColumn;

    @FXML private TextArea messageArea;
    @FXML private Label typeLabel;
    @FXML private Label createdAtLabel;
    @FXML private Label referenceIdLabel;

    @FXML private Button markReadButton;
    @FXML private Button markAllReadButton;
    @FXML private Button refreshButton;
    @FXML private Button deleteButton;
    @FXML private Button backButton;
    @FXML private Button fadeButton;

    @FXML private ProgressIndicator loadProgress;
    @FXML private ProgressBar operationProgress;
    @FXML private Label statusLabel;

    private NotificationDAO notificationDAO;
    private ObservableList<Notification> notificationList;
    private Notification selectedNotification;
    private int userId;

    @FXML
    public void initialize() {
        notificationDAO = new NotificationDAO();
        notificationList = FXCollections.observableArrayList();
        userId = SessionManager.getInstance().getUserId();

        setupTableColumns();
        setupButtonHandlers();
        applyVisualEffects();
        loadNotifications();

        if (statusLabel != null) {
            statusLabel.setText("Ready");
        }
    }

    private void setupTableColumns() {
        messageColumn.setCellValueFactory(cellData -> cellData.getValue().messageProperty());
        typeColumn.setCellValueFactory(cellData -> cellData.getValue().typeProperty());
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().readProperty());
        createdAtColumn.setCellValueFactory(cellData -> cellData.getValue().createdAtProperty().asString());

        // Center align columns
        messageColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        typeColumn.setStyle("-fx-alignment: CENTER;");
        createdAtColumn.setStyle("-fx-alignment: CENTER;");
        statusColumn.setStyle("-fx-alignment: CENTER;");

        notificationsTable.setItems(notificationList);
        notificationsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void setupButtonHandlers() {
        markReadButton.setOnAction(event -> handleMarkRead());
        markAllReadButton.setOnAction(event -> handleMarkAllRead());
        refreshButton.setOnAction(event -> loadNotifications());
        deleteButton.setOnAction(event -> handleDelete());
        backButton.setOnAction(event -> handleBack());
        if (fadeButton != null) fadeButton.setOnAction(event -> showFadeAnimation());

        notificationsTable.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                selectedNotification = newVal;
                displayNotificationDetails(selectedNotification);
                if (!selectedNotification.isRead()) {
                    handleMarkRead();
                }
            }
        });
    }

    private void applyVisualEffects() {
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(5.0);
        dropShadow.setOffsetX(2.0);
        dropShadow.setOffsetY(2.0);
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.3));

        markReadButton.setEffect(dropShadow);
        markAllReadButton.setEffect(dropShadow);
        refreshButton.setEffect(dropShadow);
        deleteButton.setEffect(dropShadow);
        backButton.setEffect(dropShadow);
        if (fadeButton != null) fadeButton.setEffect(dropShadow);
    }

    private void showFadeAnimation() {
        if (fadeButton != null) {
            FadeTransition fadeTransition = new FadeTransition(Duration.seconds(1.5), fadeButton);
            fadeTransition.setFromValue(1.0);
            fadeTransition.setToValue(0.2);
            fadeTransition.setCycleCount(4);
            fadeTransition.setAutoReverse(true);
            fadeTransition.play();
            if (statusLabel != null) statusLabel.setText("Animation played!");
            PauseTransition reset = new PauseTransition(Duration.seconds(2));
            reset.setOnFinished(e -> {
                if (statusLabel != null) statusLabel.setText("Ready");
            });
            reset.play();
        }
    }

    private void loadNotifications() {
        showProgress(true);
        if (statusLabel != null) statusLabel.setText("Loading notifications...");

        try {
            List<Notification> notifications = notificationDAO.findByUserId(userId);
            notificationList.setAll(notifications);

            int unreadCount = notificationDAO.countUnreadByUserId(userId);
            markAllReadButton.setText("Mark All Read" + (unreadCount > 0 ? " (" + unreadCount + ")" : ""));
            if (statusLabel != null) statusLabel.setText("Loaded " + notifications.size() + " notifications");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load notifications", e);
            if (statusLabel != null) statusLabel.setText("Error loading notifications");
            AlertUtil.showError("Load Failed", "Failed to load notifications.");
        } finally {
            hideProgress();
        }
    }

    private void displayNotificationDetails(Notification notification) {
        messageArea.setText(notification.getMessage());
        typeLabel.setText(notification.getType());
        if (notification.getCreatedAt() != null) {
            createdAtLabel.setText(notification.getCreatedAt().toString());
        }
        referenceIdLabel.setText(String.valueOf(notification.getReferenceId()));
    }

    private void handleMarkRead() {
        if (selectedNotification == null) {
            AlertUtil.showWarning("No Selection", "Please select a notification.");
            return;
        }

        if (selectedNotification.isRead()) return;

        showOperationProgress(true);
        if (statusLabel != null) statusLabel.setText("Marking as read...");
        updateProgress(0.5);

        try {
            boolean success = notificationDAO.markAsRead(selectedNotification.getId());
            if (success) {
                updateProgress(1.0);
                selectedNotification.setRead(true);
                loadNotifications();
                if (statusLabel != null) statusLabel.setText("Notification marked as read");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to mark notification as read", e);
            if (statusLabel != null) statusLabel.setText("Error: " + e.getMessage());
        } finally {
            hideProgressAfterDelay();
        }
    }

    private void handleMarkAllRead() {
        showOperationProgress(true);
        if (statusLabel != null) statusLabel.setText("Marking all as read...");
        updateProgress(0.5);

        try {
            boolean success = notificationDAO.markAllAsRead(userId);
            if (success) {
                updateProgress(1.0);
                AlertUtil.showSuccess("All notifications marked as read.");
                loadNotifications();
                if (statusLabel != null) statusLabel.setText("All notifications marked as read");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to mark all as read", e);
            if (statusLabel != null) statusLabel.setText("Error: " + e.getMessage());
            AlertUtil.showError("Update Failed", "Failed to mark notifications as read.");
        } finally {
            hideProgressAfterDelay();
        }
    }

    private void handleDelete() {
        if (selectedNotification == null) {
            AlertUtil.showWarning("No Selection", "Please select a notification to delete.");
            return;
        }

        boolean confirmed = AlertUtil.showConfirmation("Delete Notification", "Delete this notification?");

        if (confirmed) {
            showOperationProgress(true);
            if (statusLabel != null) statusLabel.setText("Deleting notification...");
            updateProgress(0.5);

            try {
                boolean success = notificationDAO.delete(selectedNotification.getId());
                if (success) {
                    updateProgress(1.0);
                    AlertUtil.showSuccess("Notification deleted.");
                    loadNotifications();
                    clearDetails();
                    if (statusLabel != null) statusLabel.setText("Notification deleted");
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to delete notification", e);
                if (statusLabel != null) statusLabel.setText("Error: " + e.getMessage());
                AlertUtil.showError("Delete Failed", "Failed to delete notification.");
            } finally {
                hideProgressAfterDelay();
            }
        }
    }

    private void clearDetails() {
        messageArea.clear();
        typeLabel.setText("");
        createdAtLabel.setText("");
        referenceIdLabel.setText("");
        selectedNotification = null;
        notificationsTable.getSelectionModel().clearSelection();
    }

    private void handleBack() {
        String role = SessionManager.getInstance().getUserRole();
        if ("CUSTOMER".equals(role)) {
            SceneManager.getInstance().switchToCustomerProfileView();
        } else if ("INSURANCE".equals(role)) {
            SceneManager.getInstance().switchToInsuranceView();
        } else if ("WORKSHOP".equals(role)) {
            SceneManager.getInstance().switchToWorkshopView();
        } else if ("POLICE".equals(role)) {
            SceneManager.getInstance().switchToPoliceView();
        } else if ("ADMIN".equals(role)) {
            SceneManager.getInstance().switchToAdminView();
        } else {
            SceneManager.getInstance().switchToDashboard();
        }
    }

    private void showProgress(boolean show) {
        if (loadProgress != null) loadProgress.setVisible(show);
    }

    private void showOperationProgress(boolean show) {
        if (operationProgress != null) {
            operationProgress.setVisible(show);
            operationProgress.setProgress(0);
        }
    }

    private void updateProgress(double progress) {
        if (operationProgress != null) operationProgress.setProgress(progress);
    }

    private void hideProgress() {
        if (loadProgress != null) loadProgress.setVisible(false);
    }

    private void hideProgressAfterDelay() {
        PauseTransition delay = new PauseTransition(Duration.seconds(1));
        delay.setOnFinished(event -> {
            if (operationProgress != null) {
                operationProgress.setVisible(false);
                operationProgress.setProgress(0);
            }
            if (loadProgress != null) loadProgress.setVisible(false);
        });
        delay.play();
    }
}