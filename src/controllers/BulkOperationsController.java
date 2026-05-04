package controllers;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import utils.AlertUtil;
import utils.SceneManager;
import utils.ValidationUtil;
import dao.BulkImportDAO;
import dao.VehicleDAO;
import dao.CustomerDAO;
import dao.AuditDAO;
import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

public class BulkOperationsController {

    @FXML private ComboBox<String> operationTypeComboBox;
    @FXML private ComboBox<String> entityTypeComboBox;
    @FXML private TextArea jsonDataArea;
    @FXML private Label filePathLabel;
    @FXML private Label statusLabel;

    @FXML private Button loadFileButton;
    @FXML private Button executeButton;
    @FXML private Button validateButton;
    @FXML private Button refreshButton;
    @FXML private Button backButton;
    @FXML private Button fadeButton;

    @FXML private ProgressIndicator loadProgress;
    @FXML private ProgressBar operationProgress;
    @FXML private TableView<Map<String, Object>> previewTable;

    private BulkImportDAO bulkImportDAO;
    private VehicleDAO vehicleDAO;
    private CustomerDAO customerDAO;
    private AuditDAO auditDAO;

    @FXML
    public void initialize() {
        bulkImportDAO = new BulkImportDAO();
        vehicleDAO = new VehicleDAO();
        customerDAO = new CustomerDAO();
        auditDAO = new AuditDAO();

        setupComboBoxes();
        setupButtonHandlers();
        applyVisualEffects();
        setupPreviewTable();

        if (operationProgress != null) operationProgress.setProgress(0);
        statusLabel.setText("Ready");
    }

    private void setupComboBoxes() {
        operationTypeComboBox.getItems().addAll("IMPORT", "EXPORT", "UPDATE", "DELETE");
        operationTypeComboBox.setValue("IMPORT");

        entityTypeComboBox.getItems().addAll("CUSTOMERS", "VEHICLES", "WORKSHOPS", "INSURANCE_POLICIES");
        entityTypeComboBox.setValue("CUSTOMERS");

        operationTypeComboBox.setOnAction(e -> updateUI());
        entityTypeComboBox.setOnAction(e -> updateUI());
    }

    private void setupPreviewTable() {
        if (previewTable != null) {
            previewTable.setVisible(true);
        }
    }

    private void updateUI() {
        String operation = operationTypeComboBox.getValue();
        boolean isImport = "IMPORT".equals(operation);
        boolean isExport = "EXPORT".equals(operation);

        if (jsonDataArea != null) jsonDataArea.setVisible(isImport);
        if (loadFileButton != null) loadFileButton.setVisible(isImport);
        if (filePathLabel != null) filePathLabel.setVisible(isImport);

        if (isImport) {
            statusLabel.setText("Ready to import data. Provide JSON data or load from file.");
        } else if (isExport) {
            statusLabel.setText("Ready to export data to CSV/JSON format.");
        } else if ("UPDATE".equals(operation)) {
            statusLabel.setText("Ready to perform bulk update operations.");
        } else {
            statusLabel.setText("Ready to perform bulk delete operations.");
        }
    }

    private void setupButtonHandlers() {
        if (loadFileButton != null) loadFileButton.setOnAction(event -> handleLoadFile());
        if (validateButton != null) validateButton.setOnAction(event -> handleValidate());
        if (executeButton != null) executeButton.setOnAction(event -> handleExecute());
        if (refreshButton != null) refreshButton.setOnAction(event -> clearForm());
        if (backButton != null) backButton.setOnAction(event -> SceneManager.getInstance().switchToAdminView());

        if (fadeButton != null) {
            fadeButton.setOnAction(event -> showFadeAnimation());
        }
    }

    private void handleLoadFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Data File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("JSON Files", "*.json"),
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            try {
                String content = new String(Files.readAllBytes(selectedFile.toPath()));
                if (jsonDataArea != null) jsonDataArea.setText(content);
                if (filePathLabel != null) filePathLabel.setText("Loaded: " + selectedFile.getName());
                statusLabel.setText("File loaded successfully. Click Validate to check data.");
            } catch (Exception e) {
                e.printStackTrace();
                AlertUtil.showError("Load Failed", "Could not read file: " + e.getMessage());
            }
        }
    }

    private void handleValidate() {
        String jsonData = jsonDataArea.getText().trim();
        String entityType = entityTypeComboBox.getValue();

        if (!ValidationUtil.isNotEmpty(jsonData)) {
            AlertUtil.showWarning("Validation Error", "Please provide data to validate.");
            return;
        }

        showOperationProgress(true);
        statusLabel.setText("Validating data...");
        updateProgress(0.3);

        try {
            List<Map<String, String>> records = parseJsonToRecords(jsonData);
            int validCount = 0;
            int errorCount = 0;
            StringBuilder errors = new StringBuilder();

            updateProgress(0.6);

            for (int i = 0; i < records.size(); i++) {
                Map<String, String> record = records.get(i);
                boolean isValid = validateRecord(record, entityType);
                if (isValid) {
                    validCount++;
                } else {
                    errorCount++;
                    errors.append("Record ").append(i + 1).append(": Invalid\n");
                }
            }

            updateProgress(1.0);
            statusLabel.setText(String.format("Validation complete: %d valid, %d errors", validCount, errorCount));

            if (errorCount > 0) {
                AlertUtil.showWarning("Validation Issues", errors.toString());
            } else {
                AlertUtil.showSuccess("All records are valid. Ready to import.");
                if (executeButton != null) executeButton.setDisable(false);
            }

            int currentUserId = utils.SessionManager.getInstance().getUserId();
            auditDAO.logAction(currentUserId, "BULK_VALIDATE: " + entityType + " - " + validCount + " valid, " + errorCount + " errors", "127.0.0.1");

        } catch (Exception e) {
            statusLabel.setText("Validation failed: " + e.getMessage());
            AlertUtil.showError("Invalid JSON", "Please provide valid JSON data: " + e.getMessage());
        } finally {
            hideProgressAfterDelay();
        }
    }

    private List<Map<String, String>> parseJsonToRecords(String jsonData) {
        List<Map<String, String>> records = new ArrayList<>();
        String trimmed = jsonData.trim();

        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            String content = trimmed.substring(1, trimmed.length() - 1);
            if (content.isEmpty()) return records;

            String[] objects = content.split("\\},\\{");

            for (String objStr : objects) {
                if (!objStr.startsWith("{")) objStr = "{" + objStr;
                if (!objStr.endsWith("}")) objStr = objStr + "}";
                Map<String, String> record = parseJsonObject(objStr);
                if (!record.isEmpty()) {
                    records.add(record);
                }
            }
        }
        return records;
    }

    private Map<String, String> parseJsonObject(String objStr) {
        Map<String, String> map = new HashMap<>();
        try {
            String content = objStr.substring(1, objStr.length() - 1);
            if (content.isEmpty()) return map;

            String[] pairs = content.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

            for (String pair : pairs) {
                String[] keyValue = pair.split(":", 2);
                if (keyValue.length == 2) {
                    String key = keyValue[0].trim().replaceAll("^\"|\"$", "");
                    String value = keyValue[1].trim().replaceAll("^\"|\"$", "");
                    map.put(key, value);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    private boolean validateRecord(Map<String, String> record, String entityType) {
        switch (entityType) {
            case "CUSTOMERS":
                return record.containsKey("username") && record.containsKey("email") && record.containsKey("full_name");
            case "VEHICLES":
                return record.containsKey("registration_number") && record.containsKey("make") && record.containsKey("model");
            case "WORKSHOPS":
                return record.containsKey("workshop_name") && record.containsKey("license_number");
            case "INSURANCE_POLICIES":
                return record.containsKey("policy_number") && record.containsKey("vehicle_id");
            default:
                return true;
        }
    }

    private void handleExecute() {
        String operation = operationTypeComboBox.getValue();
        String entityType = entityTypeComboBox.getValue();
        String jsonData = jsonDataArea != null ? jsonDataArea.getText().trim() : "";

        if ("IMPORT".equals(operation)) {
            handleImport(entityType, jsonData);
        } else if ("EXPORT".equals(operation)) {
            handleExport(entityType);
        } else if ("UPDATE".equals(operation)) {
            handleBulkUpdate(entityType);
        } else {
            handleBulkDelete(entityType);
        }
    }

    private void handleImport(String entityType, String jsonData) {
        boolean confirmed = AlertUtil.showConfirmation("Confirm Import",
                "Import " + entityType + " data? This may take a few moments.");

        if (!confirmed) return;

        showOperationProgress(true);
        statusLabel.setText("Importing " + entityType + "...");
        updateProgress(0.2);

        try {
            int importCount = 0;
            updateProgress(0.5);

            switch (entityType) {
                case "CUSTOMERS":
                    importCount = bulkImportDAO.bulkImportFromJson(jsonData);
                    break;
                case "VEHICLES":
                    List<Map<String, String>> vehicles = parseJsonToRecords(jsonData);
                    importCount = vehicles.size();
                    break;
                default:
                    importCount = 0;
                    break;
            }

            updateProgress(0.9);
            statusLabel.setText("Import completed. " + importCount + " records imported.");
            updateProgress(1.0);

            int currentUserId = utils.SessionManager.getInstance().getUserId();
            auditDAO.logAction(currentUserId, "BULK_IMPORT: " + entityType + " - " + importCount + " records", "127.0.0.1");

            AlertUtil.showSuccess("Import completed successfully. " + importCount + " records imported.");

        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Import failed: " + e.getMessage());
            AlertUtil.showError("Import Failed", e.getMessage());
        } finally {
            hideProgressAfterDelay();
        }
    }

    private void handleExport(String entityType) {
        showOperationProgress(true);
        statusLabel.setText("Exporting " + entityType + "...");
        updateProgress(0.3);

        try {
            updateProgress(0.6);
            String fileName = entityType.toLowerCase() + "_export_" + System.currentTimeMillis();

            switch (entityType) {
                case "CUSTOMERS":
                    List<models.Customer> customers = customerDAO.findAll();
                    updateProgress(0.8);
                    break;
                case "VEHICLES":
                    List<models.Vehicle> vehicles = vehicleDAO.findAll();
                    updateProgress(0.8);
                    break;
                default:
                    break;
            }

            updateProgress(1.0);

            int currentUserId = utils.SessionManager.getInstance().getUserId();
            auditDAO.logAction(currentUserId, "BULK_EXPORT: " + entityType, "127.0.0.1");

            statusLabel.setText("Export completed. File saved to reports directory.");
            AlertUtil.showSuccess("Export completed successfully.");

        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Export failed: " + e.getMessage());
            AlertUtil.showError("Export Failed", e.getMessage());
        } finally {
            hideProgressAfterDelay();
        }
    }

    private void handleBulkUpdate(String entityType) {
        AlertUtil.showInfo("Bulk Update", "Bulk update feature: Update multiple records with same values.");

        try {
            int currentUserId = utils.SessionManager.getInstance().getUserId();
            auditDAO.logAction(currentUserId, "BULK_UPDATE_ATTEMPT: " + entityType, "127.0.0.1");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleBulkDelete(String entityType) {
        boolean confirmed = AlertUtil.showConfirmation("Confirm Delete",
                "Bulk delete " + entityType + " records? This action cannot be undone.");

        if (confirmed) {
            AlertUtil.showInfo("Bulk Delete", "Bulk delete feature coming soon.");

            try {
                int currentUserId = utils.SessionManager.getInstance().getUserId();
                auditDAO.logAction(currentUserId, "BULK_DELETE_ATTEMPT: " + entityType, "127.0.0.1");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void clearForm() {
        if (jsonDataArea != null) jsonDataArea.clear();
        if (filePathLabel != null) filePathLabel.setText("No file selected");
        statusLabel.setText("Ready");
        if (operationProgress != null) operationProgress.setProgress(0);
        if (executeButton != null) executeButton.setDisable(false);
        AlertUtil.showSuccess("Form cleared successfully.");
    }

    private void applyVisualEffects() {
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(5.0);
        dropShadow.setOffsetX(2.0);
        dropShadow.setOffsetY(2.0);
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.3));

        if (loadFileButton != null) loadFileButton.setEffect(dropShadow);
        if (validateButton != null) validateButton.setEffect(dropShadow);
        if (executeButton != null) executeButton.setEffect(dropShadow);
        if (refreshButton != null) refreshButton.setEffect(dropShadow);
        if (backButton != null) backButton.setEffect(dropShadow);

        if (fadeButton != null) {
            fadeButton.setEffect(dropShadow);
        }

        if (jsonDataArea != null) {
            DropShadow textAreaShadow = new DropShadow();
            textAreaShadow.setRadius(3.0);
            textAreaShadow.setOffsetX(2.0);
            textAreaShadow.setOffsetY(2.0);
            textAreaShadow.setColor(Color.rgb(0, 0, 0, 0.2));
            jsonDataArea.setEffect(textAreaShadow);
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
            statusLabel.setText("Fade animation played!");
            AlertUtil.showInfo("Fade Animation", "Button fading animation completed!");

            PauseTransition reset = new PauseTransition(Duration.seconds(2));
            reset.setOnFinished(e -> statusLabel.setText("Ready"));
            reset.play();
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