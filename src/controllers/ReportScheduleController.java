package controllers;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import utils.AlertUtil;
import utils.SceneManager;
import utils.ValidationUtil;
import dao.ReportGeneratorDAO;
import dao.AuditDAO;
import utils.SessionManager;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReportScheduleController {

    @FXML private ComboBox<String> reportTypeComboBox;
    @FXML private ComboBox<String> frequencyComboBox;
    @FXML private ComboBox<String> formatComboBox;
    @FXML private TextField recipientEmailField;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TextField reportNameField;

    @FXML private Button scheduleButton;
    @FXML private Button unscheduleButton;
    @FXML private Button refreshButton;
    @FXML private Button backButton;
    @FXML private Button clearButton;
    @FXML private Button fadeButton;

    @FXML private ListView<String> scheduledReportsList;
    @FXML private Label statusLabel;
    @FXML private Label nextRunLabel;
    @FXML private ProgressIndicator loadProgress;
    @FXML private ProgressBar operationProgress;

    private ReportGeneratorDAO reportDAO;
    private AuditDAO auditDAO;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @FXML
    public void initialize() {
        reportDAO = new ReportGeneratorDAO();
        auditDAO = new AuditDAO();

        setupComboBoxes();
        setupButtonHandlers();
        loadScheduledReports();
        applyVisualEffects();
        updateNextRun();

        if (startDatePicker != null) startDatePicker.setValue(LocalDate.now());
        if (endDatePicker != null) endDatePicker.setValue(LocalDate.now().plusMonths(1));
        if (statusLabel != null) statusLabel.setText("Ready");

        // Add listeners to update next run when frequency or date changes
        if (frequencyComboBox != null) frequencyComboBox.setOnAction(e -> updateNextRun());
        if (startDatePicker != null) startDatePicker.setOnAction(e -> updateNextRun());
    }

    private void setupComboBoxes() {
        // Initialize report type combo box
        if (reportTypeComboBox != null) {
            reportTypeComboBox.getItems().addAll(
                    "Vehicle Report", "Violation Report", "Financial Report",
                    "Stolen Vehicle Report", "Expired Documents Report", "Summary Statistics",
                    "User Activity Report", "Audit Log Report", "Insurance Claims Report"
            );
            reportTypeComboBox.setValue("Vehicle Report");
        }

        // Initialize frequency combo box
        if (frequencyComboBox != null) {
            frequencyComboBox.getItems().addAll("DAILY", "WEEKLY", "MONTHLY", "QUARTERLY");
            frequencyComboBox.setValue("WEEKLY");
        }

        // Initialize format combo box
        if (formatComboBox != null) {
            formatComboBox.getItems().addAll("PDF", "CSV", "Excel");
            formatComboBox.setValue("PDF");
        }
    }

    private void setupButtonHandlers() {
        if (scheduleButton != null) scheduleButton.setOnAction(event -> handleSchedule());
        if (unscheduleButton != null) unscheduleButton.setOnAction(event -> handleUnschedule());
        if (refreshButton != null) refreshButton.setOnAction(event -> loadScheduledReports());
        if (backButton != null) backButton.setOnAction(event -> SceneManager.getInstance().switchToAdminView());

        if (clearButton != null) {
            clearButton.setOnAction(event -> clearForm());
        }

        if (fadeButton != null) {
            fadeButton.setOnAction(event -> showFadeAnimation());
        }
    }

    private void loadScheduledReports() {
        showLoadProgress(true);
        if (statusLabel != null) statusLabel.setText("Loading scheduled reports...");

        try {
            if (scheduledReportsList != null) {
                scheduledReportsList.getItems().clear();
                List<String> reports = reportDAO.getScheduledReports();

                if (reports != null && !reports.isEmpty()) {
                    scheduledReportsList.getItems().addAll(reports);
                } else {
                    // Sample data for demonstration
                    scheduledReportsList.getItems().add("Vehicle Report - Weekly - admin@vehicle.com - Next: 2026-05-04");
                    scheduledReportsList.getItems().add("Violation Report - Daily - police@vehicle.com - Next: 2026-04-28");
                    scheduledReportsList.getItems().add("Financial Report - Monthly - finance@vehicle.com - Next: 2026-05-01");
                    scheduledReportsList.getItems().add("Audit Log Report - Weekly - auditor@vehicle.com - Next: 2026-05-05");
                }

                if (statusLabel != null) {
                    statusLabel.setText("Loaded " + scheduledReportsList.getItems().size() + " scheduled reports");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            if (statusLabel != null) statusLabel.setText("Error loading reports: " + e.getMessage());
            AlertUtil.showError("Load Failed", "Failed to load scheduled reports.");
        } finally {
            showLoadProgress(false);
        }
    }

    private void handleSchedule() {
        String reportType = reportTypeComboBox != null ? reportTypeComboBox.getValue() : null;
        String frequency = frequencyComboBox != null ? frequencyComboBox.getValue() : null;
        String format = formatComboBox != null ? formatComboBox.getValue() : "PDF";
        String recipientEmail = recipientEmailField != null ? recipientEmailField.getText().trim() : "";
        String reportName = reportNameField != null ? reportNameField.getText().trim() : "";

        if (!ValidationUtil.isNotEmpty(recipientEmail)) {
            AlertUtil.showWarning("Validation Error", "Recipient email is required.");
            if (recipientEmailField != null) recipientEmailField.requestFocus();
            return;
        }

        if (!ValidationUtil.isValidEmail(recipientEmail)) {
            AlertUtil.showWarning("Validation Error", "Please enter a valid email address.");
            if (recipientEmailField != null) recipientEmailField.requestFocus();
            return;
        }

        showOperationProgress(true);
        if (statusLabel != null) statusLabel.setText("Scheduling report...");
        updateProgress(0.3);

        try {
            updateProgress(0.6);
            String finalReportName = reportName.isEmpty() ? reportType : reportName;

            boolean success = reportDAO.scheduleReport(
                    finalReportName,
                    frequency,
                    recipientEmail
            );

            updateProgress(0.9);

            if (success) {
                updateProgress(1.0);

                // Log audit
                int currentUserId = SessionManager.getInstance().getUserId();
                auditDAO.logAction(currentUserId, "SCHEDULE_REPORT: " + finalReportName + " - " + frequency + " to " + recipientEmail, "127.0.0.1");

                AlertUtil.showSuccess("Report scheduled successfully.");
                if (statusLabel != null) statusLabel.setText("Scheduled: " + reportType + " - " + frequency + " to " + recipientEmail);
                loadScheduledReports();
                clearForm();
            } else {
                if (statusLabel != null) statusLabel.setText("Failed to schedule report");
                AlertUtil.showError("Schedule Failed", "Could not schedule the report.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            if (statusLabel != null) statusLabel.setText("Error: " + e.getMessage());
            AlertUtil.showError("Database Error", "An error occurred while scheduling.");
        } finally {
            hideProgressAfterDelay();
        }
    }

    private void handleUnschedule() {
        if (scheduledReportsList == null) return;

        String selected = scheduledReportsList.getSelectionModel().getSelectedItem();

        if (selected == null) {
            AlertUtil.showWarning("No Selection", "Please select a report to unschedule.");
            return;
        }

        boolean confirmed = AlertUtil.showConfirmation("Unschedule Report",
                "Remove scheduled report: " + selected + "?");

        if (confirmed) {
            showOperationProgress(true);
            if (statusLabel != null) statusLabel.setText("Unscheduling report...");
            updateProgress(0.5);

            try {
                boolean success = reportDAO.unscheduleReport(selected);
                updateProgress(1.0);

                if (success) {
                    // Log audit
                    int currentUserId = SessionManager.getInstance().getUserId();
                    auditDAO.logAction(currentUserId, "UNSCHEDULE_REPORT: " + selected, "127.0.0.1");

                    scheduledReportsList.getItems().remove(selected);
                    AlertUtil.showSuccess("Report unscheduled successfully.");
                    if (statusLabel != null) statusLabel.setText("Unscheduled: " + selected);
                } else {
                    if (statusLabel != null) statusLabel.setText("Failed to unschedule report");
                    AlertUtil.showError("Unschedule Failed", "Could not unschedule the report.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (statusLabel != null) statusLabel.setText("Error: " + e.getMessage());
                AlertUtil.showError("Error", "Failed to unschedule report.");
            } finally {
                hideProgressAfterDelay();
            }
        }
    }

    private void updateNextRun() {
        String frequency = frequencyComboBox != null ? frequencyComboBox.getValue() : "WEEKLY";
        LocalDate startDate = startDatePicker != null ? startDatePicker.getValue() : LocalDate.now();

        if (startDate == null) {
            if (nextRunLabel != null) nextRunLabel.setText("Not set");
            return;
        }

        LocalDate nextRun = calculateNextRun(startDate, frequency);
        if (nextRunLabel != null) nextRunLabel.setText(nextRun.format(formatter));
    }

    private LocalDate calculateNextRun(LocalDate startDate, String frequency) {
        LocalDate today = LocalDate.now();

        if (startDate.isAfter(today)) {
            return startDate;
        }

        switch (frequency) {
            case "DAILY":
                return today.plusDays(1);
            case "WEEKLY":
                return today.plusWeeks(1);
            case "MONTHLY":
                return today.plusMonths(1);
            case "QUARTERLY":
                return today.plusMonths(3);
            default:
                return today.plusDays(1);
        }
    }

    private void clearForm() {
        if (reportTypeComboBox != null) reportTypeComboBox.setValue("Vehicle Report");
        if (frequencyComboBox != null) frequencyComboBox.setValue("WEEKLY");
        if (formatComboBox != null) formatComboBox.setValue("PDF");
        if (recipientEmailField != null) recipientEmailField.clear();
        if (reportNameField != null) reportNameField.clear();
        if (startDatePicker != null) startDatePicker.setValue(LocalDate.now());
        if (endDatePicker != null) endDatePicker.setValue(LocalDate.now().plusMonths(1));
        if (statusLabel != null) statusLabel.setText("Form cleared");
        AlertUtil.showSuccess("Form cleared successfully.");
    }

    private void applyVisualEffects() {
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(5.0);
        dropShadow.setOffsetX(2.0);
        dropShadow.setOffsetY(2.0);
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.3));

        if (scheduleButton != null) scheduleButton.setEffect(dropShadow);
        if (unscheduleButton != null) unscheduleButton.setEffect(dropShadow);
        if (refreshButton != null) refreshButton.setEffect(dropShadow);
        if (backButton != null) backButton.setEffect(dropShadow);

        if (clearButton != null) clearButton.setEffect(dropShadow);

        if (fadeButton != null) fadeButton.setEffect(dropShadow);

        if (scheduledReportsList != null) {
            DropShadow listShadow = new DropShadow();
            listShadow.setRadius(3.0);
            listShadow.setOffsetX(2.0);
            listShadow.setOffsetY(2.0);
            listShadow.setColor(Color.rgb(0, 0, 0, 0.2));
            scheduledReportsList.setEffect(listShadow);
        }
    }

    private void showFadeAnimation() {
        if (fadeButton != null) {
            FadeTransition fadeTransition = new FadeTransition(Duration.seconds(1.5), fadeButton);
            fadeTransition.setFromValue(1.0);
            fadeTransition.setToValue(0.2);
            fadeTransition.setCycleCount(4);
            fadeTransition.setAutoReverse(true);
            fadeTransition.play();
            if (statusLabel != null) statusLabel.setText("Fade animation played!");
            AlertUtil.showInfo("Fade Animation", "Button fading animation completed!");

            PauseTransition reset = new PauseTransition(Duration.seconds(2));
            reset.setOnFinished(e -> {
                if (statusLabel != null) statusLabel.setText("Ready");
            });
            reset.play();
        }
    }

    private void showLoadProgress(boolean show) {
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

    private void hideProgressAfterDelay() {
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
}