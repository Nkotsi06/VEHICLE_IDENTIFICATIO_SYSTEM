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
import utils.ExportUtil;
import utils.ValidationUtil;
import dao.ReportGeneratorDAO;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class PoliceExportController {

    @FXML private ComboBox<String> exportTypeComboBox;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TextField fileNameField;
    @FXML private Button exportButton;
    @FXML private Button backButton;
    @FXML private Button fadeButton;
    @FXML private Label statusLabel;
    @FXML private ProgressIndicator loadProgress;
    @FXML private ProgressBar operationProgress;

    private ReportGeneratorDAO reportDAO;

    @FXML
    public void initialize() {
        reportDAO = new ReportGeneratorDAO();

        // Police-only export types
        exportTypeComboBox.getItems().addAll(
                "Stolen Vehicles Report",
                "Violations Report",
                "Warrants Report",
                "BOLO Alerts Report",
                "Expired Documents Report",
                "Geofence Alerts Report",
                "Officer Activity Report"
        );
        exportTypeComboBox.setValue("Stolen Vehicles Report");

        startDatePicker.setValue(LocalDate.now().minusMonths(1));
        endDatePicker.setValue(LocalDate.now());
        fileNameField.setText("police_export");

        setupButtonHandlers();
        applyVisualEffects();
        statusLabel.setText("Ready");
    }

    private void setupButtonHandlers() {
        exportButton.setOnAction(event -> handleExport());
        backButton.setOnAction(event -> SceneManager.getInstance().switchToPoliceView());
        if (fadeButton != null) {
            fadeButton.setOnAction(event -> showFadeAnimation());
        }
    }

    private void applyVisualEffects() {
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(5.0);
        dropShadow.setOffsetX(2.0);
        dropShadow.setOffsetY(2.0);
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.3));

        exportButton.setEffect(dropShadow);
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
            statusLabel.setText("Animation played!");

            PauseTransition reset = new PauseTransition(Duration.seconds(2));
            reset.setOnFinished(e -> statusLabel.setText("Ready"));
            reset.play();
        }
    }

    private void handleExport() {
        String exportType = exportTypeComboBox.getValue();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        String fileName = fileNameField.getText().trim();

        if (!ValidationUtil.isNotEmpty(fileName)) {
            AlertUtil.showWarning("Validation Error", "Please enter a file name.");
            fileNameField.requestFocus();
            return;
        }

        showProgress(true);
        statusLabel.setText("Exporting " + exportType + "...");

        try {
            List<Map<String, Object>> data = null;
            String[] headers = null;
            String[] fields = null;

            switch (exportType) {
                case "Stolen Vehicles Report":
                    data = reportDAO.generateStolenVehicleReport();
                    headers = new String[]{"Registration", "Make", "Model", "Reported Date", "Case Number", "Officer", "Status"};
                    fields = new String[]{"registration_number", "make", "model", "reported_date", "case_number", "assigned_officer", "status"};
                    break;
                case "Violations Report":
                    if (startDate != null && endDate != null) {
                        data = reportDAO.generateViolationReport(startDate, endDate);
                    } else {
                        data = reportDAO.generateViolationReport(LocalDate.now().minusMonths(1), LocalDate.now());
                    }
                    headers = new String[]{"Registration", "Make", "Model", "Violation", "Date", "Fine", "Status", "Officer"};
                    fields = new String[]{"registration_number", "make", "model", "violation_type", "violation_date", "fine_amount", "payment_status", "officer_name"};
                    break;
                case "Warrants Report":
                    data = reportDAO.generateWarrantsReport();
                    headers = new String[]{"Registration", "Violation", "Issue Date", "Expiry Date", "Judge", "Status"};
                    fields = new String[]{"registration_number", "violation_type", "issue_date", "expiry_date", "judge_name", "status"};
                    break;
                case "BOLO Alerts Report":
                    data = reportDAO.generateBOLOAlertsReport();
                    headers = new String[]{"Registration", "Message", "Priority", "Alert Date", "Expiry Date", "Status"};
                    fields = new String[]{"registration_number", "message", "priority", "alert_date", "expiry_date", "status"};
                    break;
                case "Expired Documents Report":
                    data = reportDAO.generateExpiredDocumentsReport();
                    headers = new String[]{"Registration", "Document Type", "Expiry Date", "Days Remaining", "Status"};
                    fields = new String[]{"registration_number", "document_type", "expiry_date", "days_remaining", "expiry_status"};
                    break;
                case "Geofence Alerts Report":
                    data = reportDAO.generateGeofenceAlertsReport();
                    headers = new String[]{"Zone", "Vehicle", "Alert Type", "Timestamp", "Status"};
                    fields = new String[]{"zone_name", "registration_number", "alert_type", "alert_timestamp", "is_notified"};
                    break;
                case "Officer Activity Report":
                    data = reportDAO.generateOfficerActivityReport();
                    headers = new String[]{"Officer", "Action", "Vehicle", "Timestamp"};
                    fields = new String[]{"officer_name", "action", "registration_number", "timestamp"};
                    break;
                default:
                    AlertUtil.showWarning("Not Available", "This export type is not available.");
                    hideProgress();
                    return;
            }

            if (data != null && !data.isEmpty()) {
                ExportUtil.exportToCSV(data, fileName, headers, fields);
                statusLabel.setText("Export completed successfully!");
                AlertUtil.showSuccess("Export Complete", "File exported successfully.");
            } else {
                AlertUtil.showWarning("No Data", "No data found for the selected criteria.");
                statusLabel.setText("No data to export.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Export failed: " + e.getMessage());
            AlertUtil.showError("Export Failed", "An error occurred during export.");
        } finally {
            hideProgress();
        }
    }

    private void showProgress(boolean show) {
        if (loadProgress != null) loadProgress.setVisible(show);
        if (operationProgress != null) {
            operationProgress.setVisible(show);
            operationProgress.setProgress(0);
        }
    }

    private void hideProgress() {
        PauseTransition delay = new PauseTransition(Duration.seconds(1));
        delay.setOnFinished(event -> {
            if (loadProgress != null) loadProgress.setVisible(false);
            if (operationProgress != null) {
                operationProgress.setVisible(false);
                operationProgress.setProgress(0);
            }
        });
        delay.play();
    }
}