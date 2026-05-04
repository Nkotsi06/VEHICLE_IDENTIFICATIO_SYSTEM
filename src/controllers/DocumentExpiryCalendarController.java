package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;
import utils.AlertUtil;
import utils.SceneManager;
import utils.SessionManager;
import dao.VehicleDocumentDAO;
import dao.VehicleDAO;
import models.VehicleDocument;
import models.Vehicle;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Controller for Document Expiry Calendar
 * Displays vehicle document expirations in a visual calendar format
 * Shows color-coded dates for expired, critical, and due-soon documents
 * Provides quick view of document status for a selected vehicle
 */
public class DocumentExpiryCalendarController {

    // ============================================
    // FXML UI COMPONENTS - SELECTION CONTROLS
    // ============================================
    @FXML private ComboBox<Vehicle> vehicleComboBox;
    @FXML private ComboBox<Integer> yearComboBox;
    @FXML private ComboBox<Integer> monthComboBox;
    @FXML private Button loadButton;
    @FXML private Button refreshButton;
    @FXML private Button backButton;

    // ============================================
    // CALENDAR DISPLAY COMPONENTS
    // ============================================
    @FXML private GridPane calendarGrid;          // Grid for calendar days
    @FXML private Label monthYearLabel;           // Display current month/year
    @FXML private TableView<VehicleDocument> expiringDocumentsTable;
    @FXML private TableColumn<VehicleDocument, String> documentTypeColumn;
    @FXML private TableColumn<VehicleDocument, String> documentNumberColumn;
    @FXML private TableColumn<VehicleDocument, String> expiryDateColumn;
    @FXML private TableColumn<VehicleDocument, String> statusColumn;

    // ============================================
    // STATISTICS LABELS
    // ============================================
    @FXML private Label expiredCountLabel;        // Count of expired documents
    @FXML private Label expiringSoonLabel;        // Count of documents expiring soon
    @FXML private Label validCountLabel;          // Count of valid documents

    // ============================================
    // DAO INSTANCES & DATA MODELS
    // ============================================
    private VehicleDocumentDAO documentDAO;
    private VehicleDAO vehicleDAO;
    private List<VehicleDocument> allDocuments;    // All documents for selected vehicle
    private int currentYear;                       // Currently displayed year
    private int currentMonth;                      // Currently displayed month
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ============================================
    // INITIALIZATION METHODS
    // ============================================

    /**
     * Initializes the document expiry calendar controller
     * Sets up DAOs, table columns, loads vehicles, and initializes calendar
     */
    @FXML
    public void initialize() {
        documentDAO = new VehicleDocumentDAO();
        vehicleDAO = new VehicleDAO();

        setupTableColumns();
        loadVehicles();
        setupYearMonth();
        setupButtonHandlers();

        // Set default to current month/year
        currentYear = LocalDate.now().getYear();
        currentMonth = LocalDate.now().getMonthValue();
        updateCalendar();
    }

    /**
     * Configures table columns for the documents table
     */
    private void setupTableColumns() {
        documentTypeColumn.setCellValueFactory(cellData -> cellData.getValue().documentTypeProperty());
        documentNumberColumn.setCellValueFactory(cellData -> cellData.getValue().documentNumberProperty());
        expiryDateColumn.setCellValueFactory(cellData -> cellData.getValue().expiryDateProperty().asString());
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().expiryStatusProperty());

        // Center align columns
        documentTypeColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        documentNumberColumn.setStyle("-fx-alignment: CENTER;");
        expiryDateColumn.setStyle("-fx-alignment: CENTER;");
        statusColumn.setStyle("-fx-alignment: CENTER;");
    }

    /**
     * Loads vehicles owned by the current customer into the combo box
     */
    private void loadVehicles() {
        try {
            int customerId = SessionManager.getInstance().getCustomerId();
            List<Vehicle> vehicles = vehicleDAO.findByOwnerId(customerId);
            vehicleComboBox.getItems().setAll(vehicles);

            if (!vehicles.isEmpty()) {
                vehicleComboBox.getSelectionModel().selectFirst();
                loadDocumentsForVehicle();
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Load Failed", "Failed to load vehicles: " + e.getMessage());
        }
    }

    /**
     * Sets up year and month combo boxes with appropriate ranges
     * Shows 2 years before and after current year
     */
    private void setupYearMonth() {
        int currentYearVal = LocalDate.now().getYear();
        // Add years from current-2 to current+2
        for (int i = currentYearVal - 2; i <= currentYearVal + 2; i++) {
            yearComboBox.getItems().add(i);
        }
        yearComboBox.setValue(currentYearVal);

        // Add months 1-12
        for (int i = 1; i <= 12; i++) {
            monthComboBox.getItems().add(i);
        }
        monthComboBox.setValue(LocalDate.now().getMonthValue());
    }

    /**
     * Sets up button click handlers
     */
    private void setupButtonHandlers() {
        loadButton.setOnAction(event -> {
            currentYear = yearComboBox.getValue();
            currentMonth = monthComboBox.getValue();
            updateCalendar();
            loadDocumentsForVehicle();
        });
        refreshButton.setOnAction(event -> loadDocumentsForVehicle());
        backButton.setOnAction(event -> SceneManager.getInstance().switchToCustomerProfileView());

        vehicleComboBox.setOnAction(event -> loadDocumentsForVehicle());
    }

    // ============================================
    // DOCUMENT LOADING METHODS
    // ============================================

    /**
     * Loads all documents for the selected vehicle
     * Updates statistics and refreshes the table
     */
    private void loadDocumentsForVehicle() {
        Vehicle selectedVehicle = vehicleComboBox.getSelectionModel().getSelectedItem();

        if (selectedVehicle == null) {
            expiringDocumentsTable.getItems().clear();
            clearStatistics();
            return;
        }

        try {
            allDocuments = documentDAO.findByVehicleId(selectedVehicle.getId());
            updateStatistics();
            updateCalendar(); // Refresh calendar with new data
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Load Failed", "Failed to load documents: " + e.getMessage());
        }
    }

    /**
     * Clears statistics labels when no vehicle is selected
     */
    private void clearStatistics() {
        expiredCountLabel.setText("0");
        expiringSoonLabel.setText("0");
        validCountLabel.setText("0");
    }

    // ============================================
    // CALENDAR RENDERING METHODS
    // ============================================

    /**
     * Updates the calendar display for the selected month/year
     * Creates color-coded date cells based on document expiry status
     */
    private void updateCalendar() {
        calendarGrid.getChildren().clear();

        YearMonth yearMonth = YearMonth.of(currentYear, currentMonth);
        LocalDate firstOfMonth = yearMonth.atDay(1);
        int daysInMonth = yearMonth.lengthOfMonth();
        // Calculate starting day of week (0 = Sunday, 6 = Saturday)
        int startingOffset = firstOfMonth.getDayOfWeek().getValue() % 7;

        // Set month/year display
        monthYearLabel.setText(yearMonth.getMonth().toString() + " " + currentYear);

        // Add weekday headers
        String[] weekDays = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (int i = 0; i < weekDays.length; i++) {
            Label dayLabel = new Label(weekDays[i]);
            dayLabel.setStyle("-fx-font-weight: bold; -fx-padding: 5px; -fx-alignment: CENTER;");
            dayLabel.setPrefWidth(60);
            calendarGrid.add(dayLabel, i, 0);
        }

        // Get expiry map for the current month
        Map<Integer, List<VehicleDocument>> expiryMap = getExpiryMap();

        // Create calendar day cells
        for (int day = 1; day <= daysInMonth; day++) {
            int row = (startingOffset + day - 1) / 7 + 1;
            int col = (startingOffset + day - 1) % 7;

            LocalDate date = LocalDate.of(currentYear, currentMonth, day);
            Label dayLabel = new Label(String.valueOf(day));
            dayLabel.setPrefWidth(60);
            dayLabel.setPrefHeight(50);
            dayLabel.setStyle("-fx-padding: 5px; -fx-border-color: #dddddd; -fx-alignment: CENTER; -fx-font-size: 14px;");

            // Color-code based on document expiry status
            String status = getExpiryStatusForDate(expiryMap, date);
            String styleClass = getCalendarCellStyle(status);
            dayLabel.getStyleClass().add(styleClass);

            if ("EXPIRED".equals(status)) {
                dayLabel.setStyle(dayLabel.getStyle() + " -fx-background-color: #F44336; -fx-text-fill: white; -fx-font-weight: bold;");
            } else if ("CRITICAL".equals(status)) {
                dayLabel.setStyle(dayLabel.getStyle() + " -fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-weight: bold;");
            } else if ("WARNING".equals(status)) {
                dayLabel.setStyle(dayLabel.getStyle() + " -fx-background-color: #FFC107; -fx-font-weight: bold;");
            } else if ("DUE_SOON".equals(status)) {
                dayLabel.setStyle(dayLabel.getStyle() + " -fx-background-color: #8BC34A; -fx-font-weight: bold;");
            }

            // Add click handler to show documents for selected date
            final LocalDate clickedDate = date;
            dayLabel.setOnMouseClicked(e -> showDocumentsForDate(clickedDate));

            calendarGrid.add(dayLabel, col, row);
        }
    }

    /**
     * Gets the CSS style class for a calendar cell based on expiry status
     * @param status The expiry status
     * @return CSS class name
     */
    private String getCalendarCellStyle(String status) {
        switch (status) {
            case "EXPIRED": return "calendar-cell-expired";
            case "CRITICAL": return "calendar-cell-critical";
            case "WARNING": return "calendar-cell-warning";
            case "DUE_SOON": return "calendar-cell-duesoon";
            default: return "calendar-cell-normal";
        }
    }

    /**
     * Creates a map of day of month to list of documents expiring on that day
     * @return Map of day -> list of documents
     */
    private Map<Integer, List<VehicleDocument>> getExpiryMap() {
        Map<Integer, List<VehicleDocument>> map = new HashMap<>();

        if (allDocuments != null) {
            for (VehicleDocument doc : allDocuments) {
                if (doc.getExpiryDate() != null &&
                        doc.getExpiryDate().getYear() == currentYear &&
                        doc.getExpiryDate().getMonthValue() == currentMonth) {
                    int day = doc.getExpiryDate().getDayOfMonth();
                    map.computeIfAbsent(day, k -> new java.util.ArrayList<>()).add(doc);
                }
            }
        }

        return map;
    }

    /**
     * Determines the overall expiry status for a specific date
     * @param expiryMap Map of documents by day
     * @param date The date to check
     * @return Status string (EXPIRED, CRITICAL, DUE_SOON, or NONE)
     */
    private String getExpiryStatusForDate(Map<Integer, List<VehicleDocument>> expiryMap, LocalDate date) {
        List<VehicleDocument> docs = expiryMap.get(date.getDayOfMonth());
        if (docs == null) return "NONE";

        boolean hasExpired = false;
        boolean hasCritical = false;

        for (VehicleDocument doc : docs) {
            String status = doc.getExpiryStatus();
            if ("EXPIRED".equals(status)) hasExpired = true;
            if ("CRITICAL".equals(status)) hasCritical = true;
        }

        if (hasExpired) return "EXPIRED";
        if (hasCritical) return "CRITICAL";
        return "DUE_SOON";
    }

    // ============================================
    // DOCUMENT DISPLAY METHODS
    // ============================================

    /**
     * Shows documents that expire on the selected date
     * @param date The date selected by user
     */
    private void showDocumentsForDate(LocalDate date) {
        if (allDocuments == null) return;

        List<VehicleDocument> docsForDate = new java.util.ArrayList<>();
        for (VehicleDocument doc : allDocuments) {
            if (doc.getExpiryDate() != null && doc.getExpiryDate().equals(date)) {
                docsForDate.add(doc);
            }
        }

        if (!docsForDate.isEmpty()) {
            expiringDocumentsTable.getItems().setAll(docsForDate);
            AlertUtil.showInfo("Documents Expiring",
                    docsForDate.size() + " document(s) expire on " + date.format(formatter));
        } else {
            expiringDocumentsTable.getItems().clear();
            AlertUtil.showInfo("No Documents", "No documents expire on " + date.format(formatter));
        }
    }

    /**
     * Updates statistics labels based on document status
     * Calculates expired, expiring soon, and valid document counts
     */
    private void updateStatistics() {
        if (allDocuments == null || allDocuments.isEmpty()) {
            expiredCountLabel.setText("0");
            expiringSoonLabel.setText("0");
            validCountLabel.setText("0");
            expiringDocumentsTable.getItems().clear();
            return;
        }

        int expired = 0;
        int expiringSoon = 0;
        int valid = 0;

        for (VehicleDocument doc : allDocuments) {
            String status = doc.getExpiryStatus();
            if ("EXPIRED".equals(status)) {
                expired++;
            } else if ("CRITICAL".equals(status) || "WARNING".equals(status) || "DUE_SOON".equals(status)) {
                expiringSoon++;
            } else {
                valid++;
            }
        }

        expiredCountLabel.setText(String.valueOf(expired));
        expiringSoonLabel.setText(String.valueOf(expiringSoon));
        validCountLabel.setText(String.valueOf(valid));

        // Set style based on urgency
        if (expired > 0) {
            expiredCountLabel.setStyle("-fx-text-fill: #F44336; -fx-font-weight: bold;");
        } else {
            expiredCountLabel.setStyle("-fx-text-fill: #666;");
        }

        if (expiringSoon > 0) {
            expiringSoonLabel.setStyle("-fx-text-fill: #FF9800; -fx-font-weight: bold;");
        } else {
            expiringSoonLabel.setStyle("-fx-text-fill: #666;");
        }

        // Update table with all documents
        expiringDocumentsTable.getItems().setAll(allDocuments);
    }
}