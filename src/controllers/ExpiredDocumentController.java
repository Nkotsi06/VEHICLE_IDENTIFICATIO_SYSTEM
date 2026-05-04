package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import utils.AlertUtil;
import utils.SceneManager;
import utils.ExpiryDetector;
import dao.VehicleDocumentDAO;
import dao.VehicleDAO;
import models.VehicleDocument;
import models.Vehicle;
import java.util.List;
import java.util.Map;

/**
 * Controller for Expired Document Detection
 * Detects and manages expired vehicle documents
 * Generates violations for expired documents automatically
 */
public class ExpiredDocumentController {

    // ============================================
    // FXML UI COMPONENTS
    // ============================================

    // Search Components
    @FXML private TextField searchRegistrationField;
    @FXML private Button searchButton;
    @FXML private Button refreshButton;
    @FXML private Button generateViolationsButton;
    @FXML private Button backButton;

    // Table Components
    @FXML private TableView<VehicleDocument> documentsTable;
    @FXML private TableColumn<VehicleDocument, String> documentTypeColumn;
    @FXML private TableColumn<VehicleDocument, String> documentNumberColumn;
    @FXML private TableColumn<VehicleDocument, String> issueDateColumn;
    @FXML private TableColumn<VehicleDocument, String> expiryDateColumn;
    @FXML private TableColumn<VehicleDocument, String> expiryStatusColumn;

    // Statistics Labels
    @FXML private Label expiredCountLabel;
    @FXML private Label criticalCountLabel;
    @FXML private Label warningCountLabel;
    @FXML private Label overallStatusLabel;

    // ============================================
    // DAO INSTANCES
    // ============================================

    private VehicleDocumentDAO documentDAO;
    private VehicleDAO vehicleDAO;
    private ExpiryDetector expiryDetector;

    // ============================================
    // INITIALIZATION METHODS
    // ============================================

    /**
     * Initializes the expired document controller
     * Sets up DAOs, table columns, and loads initial data
     */
    @FXML
    public void initialize() {
        documentDAO = new VehicleDocumentDAO();
        vehicleDAO = new VehicleDAO();
        expiryDetector = ExpiryDetector.getInstance();

        setupTableColumns();
        setupButtonHandlers();
        loadAllDocuments();
    }

    /**
     * Configures table columns with cell value factories
     */
    private void setupTableColumns() {
        documentTypeColumn.setCellValueFactory(cellData -> cellData.getValue().documentTypeProperty());
        documentNumberColumn.setCellValueFactory(cellData -> cellData.getValue().documentNumberProperty());
        issueDateColumn.setCellValueFactory(cellData -> cellData.getValue().issueDateProperty().asString());
        expiryDateColumn.setCellValueFactory(cellData -> cellData.getValue().expiryDateProperty().asString());
        expiryStatusColumn.setCellValueFactory(cellData -> cellData.getValue().expiryStatusProperty());
    }

    /**
     * Sets up button click handlers
     */
    private void setupButtonHandlers() {
        searchButton.setOnAction(event -> handleSearch());
        refreshButton.setOnAction(event -> loadAllDocuments());
        generateViolationsButton.setOnAction(event -> handleGenerateViolations());
        backButton.setOnAction(event -> SceneManager.getInstance().switchToPoliceView());
    }

    // ============================================
    // DATA LOADING METHODS
    // ============================================

    /**
     * Loads all vehicle documents from the database
     * Updates statistics summary
     */
    private void loadAllDocuments() {
        try {
            List<VehicleDocument> documents = documentDAO.findAll();
            documentsTable.getItems().setAll(documents);
            updateSummary(documents);
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Load Failed", "Failed to load documents: " + e.getMessage());
        }
    }

    // ============================================
    // SEARCH METHOD
    // ============================================

    /**
     * Searches for documents by vehicle registration number
     * Displays expiry alert if documents are expired or critical
     */
    private void handleSearch() {
        String registrationNumber = searchRegistrationField.getText().trim();

        if (!utils.ValidationUtil.isNotEmpty(registrationNumber)) {
            AlertUtil.showWarning("Search Error", "Please enter a registration number.");
            return;
        }

        try {
            Vehicle vehicle = vehicleDAO.findByRegistrationNumber(registrationNumber);

            if (vehicle != null) {
                List<VehicleDocument> documents = documentDAO.findByVehicleId(vehicle.getId());
                documentsTable.getItems().setAll(documents);
                updateSummary(documents);

                // Check for expired or critical documents and show alert
                Map<String, Object> checkResult = expiryDetector.checkVehicleDocuments(registrationNumber);
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

    // ============================================
    // SUMMARY METHODS
    // ============================================

    /**
     * Updates statistics summary based on document expiry status
     * @param documents List of vehicle documents
     */
    private void updateSummary(List<VehicleDocument> documents) {
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

        // Set overall status message and color
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

    /**
     * Clears summary statistics when no vehicle is selected
     */
    private void clearSummary() {
        expiredCountLabel.setText("0");
        criticalCountLabel.setText("0");
        warningCountLabel.setText("0");
        overallStatusLabel.setText("No vehicle selected");
        overallStatusLabel.setStyle("-fx-text-fill: #666;");
    }

    /**
     * Shows expiry alert dialog if expired or critical documents are found
     * @param result Map containing expiry check results
     */
    private void showExpiryAlert(Map<String, Object> result) {
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

    // ============================================
    // VIOLATION GENERATION METHOD
    // ============================================

    /**
     * Generates violations for all expired documents
     * This automatically creates fines for vehicles with expired documents
     */
    private void handleGenerateViolations() {
        boolean confirmed = AlertUtil.showConfirmation("Generate Violations",
                "This will generate violations for all expired documents. Continue?");

        if (confirmed) {
            try {
                expiryDetector.detectAndGenerateViolations();
                AlertUtil.showSuccess("Violations generated for expired documents.");
                loadAllDocuments(); // Refresh the view
            } catch (Exception e) {
                e.printStackTrace();
                AlertUtil.showError("Generation Failed", "An error occurred while generating violations.");
            }
        }
    }
}