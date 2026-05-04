package controllers;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.stage.DirectoryChooser;
import javafx.util.Duration;
import utils.AlertUtil;
import utils.SceneManager;
import utils.BackupUtil;
import utils.FileHandler;
import dao.AuditDAO;
import utils.SessionManager;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BackupController {

    @FXML private ComboBox<String> backupTypeComboBox;
    @FXML private ComboBox<String> scheduleComboBox;
    @FXML private DatePicker scheduleDatePicker;
    @FXML private Button createBackupButton;
    @FXML private Button scheduleBackupButton;
    @FXML private Button restoreButton;
    @FXML private Button refreshButton;
    @FXML private Button backButton;
    @FXML private Button deleteBackupButton;
    @FXML private Button fadeButton;
    @FXML private Button chooseLocationButton;

    @FXML private ListView<String> backupsList;
    @FXML private Label lastBackupLabel;
    @FXML private Label backupSizeLabel;
    @FXML private Label backupCountLabel;
    @FXML private Label statusLabel;
    @FXML private Label backupLocationLabel;
    @FXML private ProgressBar operationProgress;
    @FXML private ProgressIndicator loadProgress;

    private String backupLocation = FileHandler.getBackupsPath();
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private AuditDAO auditDAO;

    @FXML
    public void initialize() {
        auditDAO = new AuditDAO();
        setupComboBoxes();
        setupButtonHandlers();
        loadBackupsList();
        loadBackupStats();
        applyVisualEffects();

        if (backupLocationLabel != null) backupLocationLabel.setText("Location: " + backupLocation);
        if (operationProgress != null) operationProgress.setProgress(0);
        if (statusLabel != null) statusLabel.setText("Ready");
    }

    private void setupComboBoxes() {
        if (backupTypeComboBox != null) {
            backupTypeComboBox.getItems().addAll("DATABASE_ONLY", "DOCUMENTS_ONLY", "FULL_SYSTEM");
            backupTypeComboBox.setValue("FULL_SYSTEM");
        }

        if (scheduleComboBox != null) {
            scheduleComboBox.getItems().addAll("DAILY", "WEEKLY", "MONTHLY", "MANUAL");
            scheduleComboBox.setValue("MANUAL");
        }

        if (scheduleDatePicker != null) {
            scheduleDatePicker.setValue(LocalDate.now().plusDays(1));
            scheduleDatePicker.setDisable(true);
        }

        if (scheduleComboBox != null) {
            scheduleComboBox.setOnAction(e -> {
                boolean isManual = "MANUAL".equals(scheduleComboBox.getValue());
                if (scheduleDatePicker != null) scheduleDatePicker.setDisable(isManual);
            });
        }
    }

    private void setupButtonHandlers() {
        if (createBackupButton != null) createBackupButton.setOnAction(event -> handleCreateBackup());
        if (scheduleBackupButton != null) scheduleBackupButton.setOnAction(event -> handleScheduleBackup());
        if (restoreButton != null) restoreButton.setOnAction(event -> handleRestore());
        if (refreshButton != null) refreshButton.setOnAction(event -> {
            loadBackupsList();
            loadBackupStats();
        });
        if (backButton != null) backButton.setOnAction(event -> SceneManager.getInstance().switchToAdminView());
        if (chooseLocationButton != null) chooseLocationButton.setOnAction(event -> handleChooseLocation());

        if (deleteBackupButton != null) {
            deleteBackupButton.setOnAction(event -> handleDeleteBackup());
        }

        if (fadeButton != null) {
            fadeButton.setOnAction(event -> showFadeAnimation());
        }
    }

    private void loadBackupsList() {
        showLoadProgress(true);

        try {
            if (backupsList != null) {
                backupsList.getItems().clear();
                List<String> backups = FileHandler.listFiles(backupLocation);

                for (String backup : backups) {
                    if (backup.endsWith(".zip") || backup.endsWith(".sql") || backup.endsWith(".bak") || backup.endsWith(".csv")) {
                        backupsList.getItems().add(backup);
                    }
                }
                if (backupCountLabel != null) backupCountLabel.setText("Total: " + backupsList.getItems().size());

                if (backupsList.getItems().isEmpty()) {
                    backupsList.setPlaceholder(new Label("No backups found"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (statusLabel != null) statusLabel.setText("Error loading backups: " + e.getMessage());
        } finally {
            showLoadProgress(false);
        }
    }

    private void loadBackupStats() {
        try {
            List<String> backups = FileHandler.listFiles(backupLocation);
            if (backups != null && !backups.isEmpty()) {
                String latest = backups.get(backups.size() - 1);
                if (lastBackupLabel != null) lastBackupLabel.setText(latest);

                long size = FileHandler.getFileSize(backupLocation, latest);
                if (backupSizeLabel != null) backupSizeLabel.setText(formatFileSize(size));
            } else {
                if (lastBackupLabel != null) lastBackupLabel.setText("No backups found");
                if (backupSizeLabel != null) backupSizeLabel.setText("0 KB");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.2f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }

    private void handleChooseLocation() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Backup Directory");

        File currentDir = new File(backupLocation);
        if (currentDir.exists()) {
            directoryChooser.setInitialDirectory(currentDir);
        }

        File selectedDir = directoryChooser.showDialog(null);
        if (selectedDir != null) {
            backupLocation = selectedDir.getAbsolutePath();
            if (backupLocationLabel != null) backupLocationLabel.setText("Location: " + backupLocation);
            loadBackupsList();

            int currentUserId = SessionManager.getInstance().getUserId();
            try {
                auditDAO.logAction(currentUserId, "CHANGE_BACKUP_LOCATION: " + backupLocation, "127.0.0.1");
            } catch (Exception e) {
                e.printStackTrace();
            }

            AlertUtil.showSuccess("Backup location changed successfully.");
        }
    }

    private void handleCreateBackup() {
        String backupType = backupTypeComboBox != null ? backupTypeComboBox.getValue() : "FULL_SYSTEM";

        showOperationProgress(true);
        if (statusLabel != null) statusLabel.setText("Creating backup...");
        updateProgress(0.2);

        PauseTransition pause = new PauseTransition(Duration.seconds(1));
        pause.setOnFinished(e -> {
            try {
                updateProgress(0.4);

                if ("FULL_SYSTEM".equals(backupType)) {
                    BackupUtil.createFullSystemBackup();
                } else if ("DATABASE_ONLY".equals(backupType)) {
                    BackupUtil.createDatabaseBackup();
                } else {
                    BackupUtil.createDatabaseBackup();
                }

                updateProgress(1.0);

                int currentUserId = SessionManager.getInstance().getUserId();
                try {
                    auditDAO.logAction(currentUserId, "CREATE_BACKUP: " + backupType, "127.0.0.1");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                if (statusLabel != null) statusLabel.setText("Backup completed successfully!");
                loadBackupsList();
                loadBackupStats();

                PauseTransition reset = new PauseTransition(Duration.seconds(2));
                reset.setOnFinished(resetEvent -> {
                    if (statusLabel != null) statusLabel.setText("Ready");
                    hideProgressAfterDelay();
                });
                reset.play();

            } catch (Exception ex) {
                ex.printStackTrace();
                if (statusLabel != null) statusLabel.setText("Backup failed: " + ex.getMessage());
                AlertUtil.showError("Backup Failed", ex.getMessage());
                hideProgressAfterDelay();
            }
        });
        pause.play();
    }

    private void handleScheduleBackup() {
        String schedule = scheduleComboBox != null ? scheduleComboBox.getValue() : "MANUAL";
        LocalDate date = scheduleDatePicker != null ? scheduleDatePicker.getValue() : LocalDate.now();

        if ("MANUAL".equals(schedule)) {
            AlertUtil.showInfo("Manual Backup", "Please use 'Create Backup' button for manual backups.");
            return;
        }

        boolean confirmed = AlertUtil.showConfirmation("Schedule Backup",
                "Schedule " + schedule + " backup starting from " + date + "?");

        if (confirmed) {
            int currentUserId = SessionManager.getInstance().getUserId();
            try {
                auditDAO.logAction(currentUserId, "SCHEDULE_BACKUP: " + schedule + " from " + date, "127.0.0.1");
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (statusLabel != null) statusLabel.setText("Backup scheduled: " + schedule + " from " + date);
            AlertUtil.showSuccess("Backup scheduled successfully.");
        }
    }

    private void handleRestore() {
        if (backupsList == null) return;

        String selectedBackup = backupsList.getSelectionModel().getSelectedItem();

        if (selectedBackup == null) {
            AlertUtil.showWarning("No Selection", "Please select a backup file to restore.");
            return;
        }

        boolean confirmed = AlertUtil.showConfirmation("Restore Backup",
                "Restore from backup: " + selectedBackup + "?\n\nThis will overwrite current data. This action cannot be undone.");

        if (confirmed) {
            int currentUserId = SessionManager.getInstance().getUserId();
            try {
                auditDAO.logAction(currentUserId, "RESTORE_BACKUP: " + selectedBackup, "127.0.0.1");
            } catch (Exception e) {
                e.printStackTrace();
            }

            showOperationProgress(true);
            if (statusLabel != null) statusLabel.setText("Restoring from backup...");
            updateProgress(0.5);

            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            pause.setOnFinished(e -> {
                updateProgress(0.8);
                BackupUtil.restoreFromBackup(backupLocation + "/" + selectedBackup);
                updateProgress(1.0);
                if (statusLabel != null) statusLabel.setText("Restore completed. Please restart the application.");
                AlertUtil.showInfo("Restore Complete", "Backup restored successfully. Please restart the application.");
                hideProgressAfterDelay();
            });
            pause.play();
        }
    }

    private void handleDeleteBackup() {
        if (backupsList == null) return;

        String selectedBackup = backupsList.getSelectionModel().getSelectedItem();

        if (selectedBackup == null) {
            AlertUtil.showWarning("No Selection", "Please select a backup file to delete.");
            return;
        }

        boolean confirmed = AlertUtil.showConfirmation("Delete Backup",
                "Delete backup file: " + selectedBackup + "?\n\nThis action cannot be undone.");

        if (confirmed) {
            try {
                File file = new File(backupLocation + "/" + selectedBackup);
                if (file.delete()) {
                    int currentUserId = SessionManager.getInstance().getUserId();
                    try {
                        auditDAO.logAction(currentUserId, "DELETE_BACKUP: " + selectedBackup, "127.0.0.1");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    AlertUtil.showSuccess("Backup file deleted successfully.");
                    loadBackupsList();
                    loadBackupStats();
                } else {
                    AlertUtil.showError("Delete Failed", "Could not delete backup file.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                AlertUtil.showError("Delete Error", "Failed to delete backup: " + e.getMessage());
            }
        }
    }

    private void applyVisualEffects() {
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(5.0);
        dropShadow.setOffsetX(2.0);
        dropShadow.setOffsetY(2.0);

        if (createBackupButton != null) createBackupButton.setEffect(dropShadow);
        if (scheduleBackupButton != null) scheduleBackupButton.setEffect(dropShadow);
        if (restoreButton != null) restoreButton.setEffect(dropShadow);
        if (refreshButton != null) refreshButton.setEffect(dropShadow);
        if (backButton != null) backButton.setEffect(dropShadow);
        if (chooseLocationButton != null) chooseLocationButton.setEffect(dropShadow);

        if (deleteBackupButton != null) {
            deleteBackupButton.setEffect(dropShadow);
        }

        if (fadeButton != null) {
            fadeButton.setEffect(dropShadow);
        }
    }

    private void showFadeAnimation() {
        if (fadeButton != null) {
            FadeTransition fadeTransition = new FadeTransition(Duration.seconds(2), fadeButton);
            fadeTransition.setFromValue(1.0);
            fadeTransition.setToValue(0.2);
            fadeTransition.setCycleCount(3);
            fadeTransition.setAutoReverse(true);
            fadeTransition.play();

            if (statusLabel != null) statusLabel.setText("Fade animation played!");
            AlertUtil.showInfo("Fade Animation", "Button fading animation played!");

            PauseTransition reset = new PauseTransition(Duration.seconds(2));
            reset.setOnFinished(e -> {
                if (statusLabel != null) statusLabel.setText("Ready");
            });
            reset.play();
        }
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