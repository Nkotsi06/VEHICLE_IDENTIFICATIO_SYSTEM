package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import utils.AlertUtil;
import utils.SceneManager;
import dao.VehicleDocumentDAO;
import dao.VehicleDAO;
import dao.InsurancePolicyDAO;
import models.VehicleDocument;
import models.Vehicle;
import models.InsurancePolicy;
import java.io.File;
import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OCRDocumentScannerController {

    @FXML private ImageView documentImageView;
    @FXML private TextArea rawTextArea;
    @FXML private TextField documentNumberField;
    @FXML private TextField vehicleRegField;
    @FXML private TextField ownerNameField;
    @FXML private TextField providerField;
    @FXML private ComboBox<String> documentTypeComboBox;
    @FXML private DatePicker issueDatePicker;
    @FXML private DatePicker expiryDatePicker;

    @FXML private Button uploadButton;
    @FXML private Button scanButton;
    @FXML private Button importButton;
    @FXML private Button clearButton;
    @FXML private Button backButton;

    @FXML private Label fileNameLabel;
    @FXML private Label statusLabel;
    @FXML private ProgressBar scanProgressBar;

    private VehicleDocumentDAO documentDAO;
    private VehicleDAO vehicleDAO;
    private InsurancePolicyDAO policyDAO;
    private File currentFile;
    private String extractedRawText;

    @FXML
    public void initialize() {
        documentDAO = new VehicleDocumentDAO();
        vehicleDAO = new VehicleDAO();
        policyDAO = new InsurancePolicyDAO();

        setupDocumentTypes();
        setupButtonHandlers();

        scanButton.setDisable(true);
        importButton.setDisable(true);
        scanProgressBar.setVisible(false);

        issueDatePicker.setValue(LocalDate.now());
        expiryDatePicker.setValue(LocalDate.now().plusYears(1));
    }

    private void setupDocumentTypes() {
        documentTypeComboBox.getItems().addAll(
                "REGISTRATION_CERTIFICATE",
                "INSURANCE_DISC",
                "ROAD_TAX_DISC",
                "FITNESS_CERTIFICATE",
                "EMISSION_TEST_CERTIFICATE",
                "PCO_LICENSE"
        );
        documentTypeComboBox.setValue("INSURANCE_DISC");
    }

    private void setupButtonHandlers() {
        uploadButton.setOnAction(event -> handleUpload());
        scanButton.setOnAction(event -> handleScan());
        importButton.setOnAction(event -> handleImport());
        clearButton.setOnAction(event -> handleClear());
        backButton.setOnAction(event -> handleBack());
    }

    private void handleUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Document");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.bmp", "*.tiff"),
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        currentFile = fileChooser.showOpenDialog(null);

        if (currentFile != null) {
            fileNameLabel.setText(currentFile.getName());
            statusLabel.setText("File loaded. Click 'Scan Document' to extract data.");
            scanButton.setDisable(false);
            importButton.setDisable(true);

            try {
                Image image = new Image(currentFile.toURI().toString());
                documentImageView.setImage(image);
            } catch (Exception e) {
                documentImageView.setImage(null);
                statusLabel.setText("Could not preview file. Scan may still work.");
            }
        }
    }

    private void handleScan() {
        if (currentFile == null) {
            AlertUtil.showWarning("No File", "Please upload a document first.");
            return;
        }

        scanButton.setDisable(true);
        scanProgressBar.setVisible(true);
        scanProgressBar.setProgress(0.2);
        statusLabel.setText("Scanning document...");

        // Simulate OCR processing with progress animation
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(0.5), e -> scanProgressBar.setProgress(0.4)),
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(1.0), e -> scanProgressBar.setProgress(0.6)),
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(1.5), e -> scanProgressBar.setProgress(0.8)),
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(2.0), e -> {
                    scanProgressBar.setProgress(1.0);
                    performOCRExtraction();
                    scanProgressBar.setVisible(false);
                    scanButton.setDisable(false);
                    statusLabel.setText("Scan completed. Review extracted data.");
                })
        );
        timeline.play();
    }

    private void performOCRExtraction() {
        // Simulated OCR extraction based on file name or document type
        String fileName = currentFile.getName().toLowerCase();
        extractedRawText = generateSimulatedOCRText(fileName);
        rawTextArea.setText(extractedRawText);

        // Extract data from simulated text
        extractDataFromText(extractedRawText);

        importButton.setDisable(false);
    }

    private String generateSimulatedOCRText(String fileName) {
        String documentType = documentTypeComboBox.getValue();

        StringBuilder text = new StringBuilder();
        text.append("=== EXTRACTED DOCUMENT DATA ===\n\n");

        if (documentType.equals("INSURANCE_DISC")) {
            text.append("INSURANCE CERTIFICATE\n");
            text.append("Policy Number: INS-POL-2024-987654\n");
            text.append("Vehicle Registration: BMD1234\n");
            text.append("Owner: John M. Doe\n");
            text.append("Insurance Provider: AutoShield Insurance\n");
            text.append("Issue Date: 2024-01-15\n");
            text.append("Expiry Date: 2025-01-14\n");
            text.append("Coverage Type: Comprehensive\n");
            text.append("Premium Amount: ZAR 2,500.00\n");
        } else if (documentType.equals("REGISTRATION_CERTIFICATE")) {
            text.append("VEHICLE REGISTRATION CERTIFICATE\n");
            text.append("Registration Number: BMD1234\n");
            text.append("Make: Toyota\n");
            text.append("Model: Hilux\n");
            text.append("Year: 2020\n");
            text.append("Owner: John M. Doe\n");
            text.append("Engine Number: 2TR-FE-123456\n");
            text.append("Chassis Number: MR0FZ29G001234567\n");
            text.append("Registration Date: 2020-03-10\n");
            text.append("Expiry Date: 2025-03-09\n");
        } else if (documentType.equals("ROAD_TAX_DISC")) {
            text.append("ROAD TAX DISC\n");
            text.append("Vehicle: BMD1234\n");
            text.append("Owner: John M. Doe\n");
            text.append("Tax Period: 2024-2025\n");
            text.append("Issue Date: 2024-02-01\n");
            text.append("Expiry Date: 2025-01-31\n");
            text.append("Amount Paid: ZAR 450.00\n");
        } else {
            text.append("VEHICLE DOCUMENT\n");
            text.append("Document Number: DOC-" + System.currentTimeMillis() + "\n");
            text.append("Vehicle: BMD1234\n");
            text.append("Owner: John Doe\n");
            text.append("Issue Date: 2024-01-01\n");
            text.append("Expiry Date: 2025-12-31\n");
        }

        text.append("\n=== END OF DOCUMENT ===\n");
        return text.toString();
    }

    private void extractDataFromText(String text) {
        // Extract Document Number
        Pattern docNumPattern = Pattern.compile("(?:Policy|Document) Number:?\\s*([A-Z0-9\\-]+)", Pattern.CASE_INSENSITIVE);
        Matcher docNumMatcher = docNumPattern.matcher(text);
        if (docNumMatcher.find()) {
            documentNumberField.setText(docNumMatcher.group(1));
        }

        // Extract Vehicle Registration
        Pattern regPattern = Pattern.compile("(?:Registration|Vehicle) (?:Number|Registration):?\\s*([A-Z0-9]+)", Pattern.CASE_INSENSITIVE);
        Matcher regMatcher = regPattern.matcher(text);
        if (regMatcher.find()) {
            vehicleRegField.setText(regMatcher.group(1).toUpperCase());
        }

        // Extract Owner Name
        Pattern ownerPattern = Pattern.compile("Owner:?\\s*([A-Za-z\\.\\s]+)(?:\\n|$)", Pattern.CASE_INSENSITIVE);
        Matcher ownerMatcher = ownerPattern.matcher(text);
        if (ownerMatcher.find()) {
            ownerNameField.setText(ownerMatcher.group(1).trim());
        }

        // Extract Provider
        Pattern providerPattern = Pattern.compile("(?:Insurance Provider|Provider):?\\s*([A-Za-z\\s]+)", Pattern.CASE_INSENSITIVE);
        Matcher providerMatcher = providerPattern.matcher(text);
        if (providerMatcher.find()) {
            providerField.setText(providerMatcher.group(1).trim());
        }

        // Extract Issue Date
        Pattern issuePattern = Pattern.compile("(?:Issue Date|Registration Date):?\\s*(\\d{4}-\\d{2}-\\d{2})", Pattern.CASE_INSENSITIVE);
        Matcher issueMatcher = issuePattern.matcher(text);
        if (issueMatcher.find()) {
            try {
                issueDatePicker.setValue(LocalDate.parse(issueMatcher.group(1)));
            } catch (Exception e) {}
        }

        // Extract Expiry Date
        Pattern expiryPattern = Pattern.compile("(?:Expiry Date|Expiration):?\\s*(\\d{4}-\\d{2}-\\d{2})", Pattern.CASE_INSENSITIVE);
        Matcher expiryMatcher = expiryPattern.matcher(text);
        if (expiryMatcher.find()) {
            try {
                expiryDatePicker.setValue(LocalDate.parse(expiryMatcher.group(1)));
            } catch (Exception e) {}
        }
    }

    private void handleImport() {
        String documentType = documentTypeComboBox.getValue();
        String documentNumber = documentNumberField.getText().trim();
        LocalDate issueDate = issueDatePicker.getValue();
        LocalDate expiryDate = expiryDatePicker.getValue();
        String vehicleReg = vehicleRegField.getText().trim();

        if (!utils.ValidationUtil.isNotEmpty(documentNumber)) {
            AlertUtil.showWarning("Validation Error", "Document number is required.");
            documentNumberField.requestFocus();
            return;
        }

        if (issueDate == null || expiryDate == null) {
            AlertUtil.showWarning("Validation Error", "Please select issue and expiry dates.");
            return;
        }

        if (!utils.ValidationUtil.isNotEmpty(vehicleReg)) {
            AlertUtil.showWarning("Validation Error", "Vehicle registration is required.");
            vehicleRegField.requestFocus();
            return;
        }

        try {
            Vehicle vehicle = vehicleDAO.findByRegistrationNumber(vehicleReg);

            if (vehicle == null) {
                boolean createVehicle = AlertUtil.showConfirmation("Vehicle Not Found",
                        "Vehicle " + vehicleReg + " not found in system. Create new vehicle record?");

                if (createVehicle) {
                    AlertUtil.showInfo("Create Vehicle", "Please use Vehicle Registration form to add the vehicle first.");
                    return;
                } else {
                    return;
                }
            }

            VehicleDocument document = new VehicleDocument();
            document.setVehicleId(vehicle.getId());
            document.setDocumentType(documentType);
            document.setDocumentNumber(documentNumber);
            document.setIssueDate(issueDate);
            document.setExpiryDate(expiryDate);
            document.setStatus("ACTIVE");

            boolean success = documentDAO.insert(document);

            if (success) {
                AlertUtil.showSuccess("Document imported and saved successfully.");

                if (documentType.equals("INSURANCE_DISC") && utils.ValidationUtil.isNotEmpty(providerField.getText())) {
                    AlertUtil.showInfo("Insurance Policy", "Insurance policy data can now be added.");
                }

                handleClear();
            } else {
                AlertUtil.showError("Import Failed", "Failed to save document to database.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Database Error", "An error occurred while importing data.");
        }
    }

    private void handleClear() {
        documentImageView.setImage(null);
        rawTextArea.clear();
        documentNumberField.clear();
        vehicleRegField.clear();
        ownerNameField.clear();
        providerField.clear();
        issueDatePicker.setValue(LocalDate.now());
        expiryDatePicker.setValue(LocalDate.now().plusYears(1));
        documentTypeComboBox.setValue("INSURANCE_DISC");
        fileNameLabel.setText("No file selected");
        statusLabel.setText("");
        currentFile = null;
        extractedRawText = null;
        scanButton.setDisable(true);
        importButton.setDisable(true);
    }

    private void handleBack() {
        SceneManager.getInstance().switchToInsurancePolicyView();
    }
}