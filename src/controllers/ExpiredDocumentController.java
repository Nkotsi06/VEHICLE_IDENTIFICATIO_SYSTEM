package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import utils.AlertUtil;
import utils.SceneManager;
import utils.ExpiryDetector;
import dao.VehicleDocumentDAO;
import dao.VehicleDAO;
import models.VehicleDocument;
import models.ExpiredDocumentAlert;
import models.Vehicle;

public class ExpiredDocumentController {

    @FXML private TextField searchRegistrationField;
    @FXML private Button searchButton;
    @FXML private Button refreshButton;
    @FXML private Button generateViolationsButton;
    @FXML private Button backButton;

    @FXML private TableView<VehicleDocument> documentsTable;
    @FXML private TableColumn<VehicleDocument, String> documentTypeColumn;
    @FXML private TableColumn<VehicleDocument, String> documentNumberColumn;
    @FXML private TableColumn<VehicleDocument, String> issueDateColumn;
    @FXML private TableColumn<VehicleDocument, String> expiryDateColumn;
    @FXML private TableColumn<VehicleDocument, String> expiryStatusColumn;

    @FXML private Label expiredCountLabel;
    @FXML private Label criticalCountLabel;
    @FXML private Label warningCountLabel;
    @FXML private Label overallStatusLabel;

    private VehicleDocumentDAO documentDAO;
    private VehicleDAO vehicleDAO;
    private ExpiryDetector expiryDetector;

    @FXML
    public void initialize() {
        documentDAO = new VehicleDocumentDAO();
        vehicleDAO = new VehicleDAO();
        expiryDetector = ExpiryDetector.getInstance();

        setupTableColumns();
        setupButtonHandlers();
        loadAllDocuments();
    }

    private void setupTableColumns() {
        documentTypeColumn.setCellValueFactory(cellData -> cellData.getValue().documentTypeProperty());
        documentNumberColumn.setCellValueFactory(cellData -> cellData.getValue().documentNumberProperty());
        issueDateColumn.setCellValueFactory(cellData -> cellData.getValue().issueDateProperty().asString());
        expiryDateColumn.setCellValueFactory(cellData -> cellData.getValue().expiryDateProperty().asString());
        expiryStatusColumn.setCellValueFactory(cellData -> cellData.getValue().expiryStatusProperty());
    }

    private void setupButtonHandlers() {
        searchButton.setOnAction(event -> handleSearch());
        refreshButton.setOnAction(event -> loadAllDocuments());
        generateViolationsButton.setOnAction(event -> handleGenerateViolations());
        backButton.setOnAction(event -> SceneManager.getInstance().switchToPoliceView());
    }

    private void loadAllDocuments() {
        try {
            java.util.List<VehicleDocument> documents = documentDAO.findAll();
            documentsTable.getItems().setAll(documents);
            updateSummary(documents);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleSearch() {
        String registrationNumber = searchRegistrationField.getText().trim();

        if (!utils.ValidationUtil.isNotEmpty(registrationNumber)) {
            AlertUtil.showWarning("Search Error", "Please enter a registration number.");
            return;
        }

        try {
            Vehicle vehicle = vehicleDAO.findByRegistrationNumber(registrationNumber);

            if (vehicle != null) {
                java.util.List<VehicleDocument> documents = documentDAO.findByVehicleId(vehicle.getId());
                documentsTable.getItems().setAll(documents);
                updateSummary(documents);

                java.util.Map<String, Object> checkResult = expiryDetector.checkVehicleDocuments(registrationNumber);
                showExpiryAlert(checkResult);
            } else {
                AlertUtil.showWarning("Not Found", "No vehicle found with registration number: " + registrationNumber);
                documentsTable.getItems().clear();
                clearSummary();
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Search Failed", "An error occurred while searching.");
        }
    }

    private void updateSummary(java.util.List<VehicleDocument> documents) {
        int expired = 0;
        int critical = 0;
        int warning = 0;

        for (VehicleDocument doc : documents) {
            String status = doc.getExpiryStatus();
            if ("EXPIRED".equals(status)) expired++;
            else if ("CRITICAL".equals(status)) critical++;
            else if ("WARNING".equals(status) || "DUE_SOON".equals(status)) warning++;
        }

        expiredCountLabel.setText(String.valueOf(expired));
        criticalCountLabel.setText(String.valueOf(critical));
        warningCountLabel.setText(String.valueOf(warning));

        if (expired > 0) {
            overallStatusLabel.setText("VEHICLE IMPOUND RECOMMENDED");
            overallStatusLabel.setStyle("-fx-text-fill: #F44336; -fx-font-weight: bold;");
        } else if (critical > 0) {
            overallStatusLabel.setText("IMMEDIATE FINE REQUIRED");
            overallStatusLabel.setStyle("-fx-text-fill: #FF9800; -fx-font-weight: bold;");
        } else if (warning > 0) {
            overallStatusLabel.setText("WARNING NOTICE");
            overallStatusLabel.setStyle("-fx-text-fill: #FFC107; -fx-font-weight: bold;");
        } else {
            overallStatusLabel.setText("ALL DOCUMENTS VALID");
            overallStatusLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
        }
    }

    private void clearSummary() {
        expiredCountLabel.setText("0");
        criticalCountLabel.setText("0");
        warningCountLabel.setText("0");
        overallStatusLabel.setText("No vehicle selected");
        overallStatusLabel.setStyle("-fx-text-fill: #666;");
    }

    private void showExpiryAlert(java.util.Map<String, Object> result) {
        int expiredCount = (int) result.getOrDefault("expiredCount", 0);
        int criticalCount = (int) result.getOrDefault("criticalCount", 0);
        String overallStatus = (String) result.get("overallStatus");

        if (expiredCount > 0 || criticalCount > 0) {
            StringBuilder message = new StringBuilder();
            message.append("Document Status:\n");
            message.append("- Expired: ").append(expiredCount).append("\n");
            message.append("- Critical: ").append(criticalCount).append("\n");
            message.append("- Warning: ").append(result.get("warningCount")).append("\n\n");
            message.append("Recommended Action: ").append(overallStatus);

            AlertUtil.showWarning("Expired Documents Detected", message.toString());
        }
    }

    private void handleGenerateViolations() {
        boolean confirmed = AlertUtil.showConfirmation("Generate Violations",
                "This will generate violations for all expired documents. Continue?");

        if (confirmed) {
            try {
                expiryDetector.detectAndGenerateViolations();
                AlertUtil.showSuccess("Violations generated for expired documents.");
                loadAllDocuments();
            } catch (Exception e) {
                e.printStackTrace();
                AlertUtil.showError("Generation Failed", "An error occurred while generating violations.");
            }
        }
    }
}