package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import utils.AlertUtil;
import utils.SceneManager;
import utils.SessionManager;
import dao.VehicleDocumentDAO;
import dao.VehicleDAO;
import models.VehicleDocument;
import models.Vehicle;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class DocumentExpiryCalendarController {

    @FXML private ComboBox<Vehicle> vehicleComboBox;
    @FXML private ComboBox<Integer> yearComboBox;
    @FXML private ComboBox<Integer> monthComboBox;
    @FXML private Button loadButton;
    @FXML private Button refreshButton;
    @FXML private Button backButton;

    @FXML private GridPane calendarGrid;
    @FXML private Label monthYearLabel;
    @FXML private TableView<VehicleDocument> expiringDocumentsTable;
    @FXML private TableColumn<VehicleDocument, String> documentTypeColumn;
    @FXML private TableColumn<VehicleDocument, String> documentNumberColumn;
    @FXML private TableColumn<VehicleDocument, String> expiryDateColumn;
    @FXML private TableColumn<VehicleDocument, String> statusColumn;

    @FXML private Label expiredCountLabel;
    @FXML private Label expiringSoonLabel;
    @FXML private Label validCountLabel;

    private VehicleDocumentDAO documentDAO;
    private VehicleDAO vehicleDAO;
    private List<VehicleDocument> allDocuments;
    private int currentYear;
    private int currentMonth;

    @FXML
    public void initialize() {
        documentDAO = new VehicleDocumentDAO();
        vehicleDAO = new VehicleDAO();

        setupTableColumns();
        loadVehicles();
        setupYearMonth();
        setupButtonHandlers();

        currentYear = LocalDate.now().getYear();
        currentMonth = LocalDate.now().getMonthValue();
        updateCalendar();
    }

    private void setupTableColumns() {
        documentTypeColumn.setCellValueFactory(cellData -> cellData.getValue().documentTypeProperty());
        documentNumberColumn.setCellValueFactory(cellData -> cellData.getValue().documentNumberProperty());
        expiryDateColumn.setCellValueFactory(cellData -> cellData.getValue().expiryDateProperty().asString());
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().expiryStatusProperty());
    }

    private void loadVehicles() {
        try {
            int customerId = SessionManager.getInstance().getCustomerId();
            List<Vehicle> vehicles = vehicleDAO.findByOwnerId(customerId);
            vehicleComboBox.getItems().setAll(vehicles);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupYearMonth() {
        int currentYearVal = LocalDate.now().getYear();
        for (int i = currentYearVal - 2; i <= currentYearVal + 2; i++) {
            yearComboBox.getItems().add(i);
        }
        yearComboBox.setValue(currentYearVal);

        for (int i = 1; i <= 12; i++) {
            monthComboBox.getItems().add(i);
        }
        monthComboBox.setValue(LocalDate.now().getMonthValue());
    }

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

    private void loadDocumentsForVehicle() {
        Vehicle selectedVehicle = vehicleComboBox.getSelectionModel().getSelectedItem();

        if (selectedVehicle == null) {
            expiringDocumentsTable.getItems().clear();
            return;
        }

        try {
            allDocuments = documentDAO.findByVehicleId(selectedVehicle.getId());
            updateStatistics();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateCalendar() {
        calendarGrid.getChildren().clear();

        YearMonth yearMonth = YearMonth.of(currentYear, currentMonth);
        LocalDate firstOfMonth = yearMonth.atDay(1);
        int daysInMonth = yearMonth.lengthOfMonth();
        int startingOffset = firstOfMonth.getDayOfWeek().getValue() % 7;

        monthYearLabel.setText(yearMonth.getMonth().toString() + " " + currentYear);

        String[] weekDays = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (int i = 0; i < weekDays.length; i++) {
            Label dayLabel = new Label(weekDays[i]);
            dayLabel.setStyle("-fx-font-weight: bold; -fx-padding: 5px;");
            calendarGrid.add(dayLabel, i, 0);
        }

        Map<Integer, List<VehicleDocument>> expiryMap = getExpiryMap();

        for (int day = 1; day <= daysInMonth; day++) {
            int row = (startingOffset + day - 1) / 7 + 1;
            int col = (startingOffset + day - 1) % 7;

            LocalDate date = LocalDate.of(currentYear, currentMonth, day);
            Label dayLabel = new Label(String.valueOf(day));
            dayLabel.setStyle("-fx-padding: 5px; -fx-border-color: #ccc; -fx-min-width: 40px; -fx-min-height: 40px;");

            String status = getExpiryStatusForDate(expiryMap, date);
            if ("EXPIRED".equals(status)) {
                dayLabel.setStyle(dayLabel.getStyle() + " -fx-background-color: #F44336; -fx-text-fill: white;");
            } else if ("CRITICAL".equals(status)) {
                dayLabel.setStyle(dayLabel.getStyle() + " -fx-background-color: #FF9800; -fx-text-fill: white;");
            } else if ("WARNING".equals(status)) {
                dayLabel.setStyle(dayLabel.getStyle() + " -fx-background-color: #FFC107;");
            } else if ("DUE_SOON".equals(status)) {
                dayLabel.setStyle(dayLabel.getStyle() + " -fx-background-color: #8BC34A;");
            }

            final LocalDate clickedDate = date;
            dayLabel.setOnMouseClicked(e -> showDocumentsForDate(clickedDate));

            calendarGrid.add(dayLabel, col, row);
        }
    }

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

    private String getExpiryStatusForDate(Map<Integer, List<VehicleDocument>> expiryMap, LocalDate date) {
        List<VehicleDocument> docs = expiryMap.get(date.getDayOfMonth());
        if (docs == null) return "NONE";

        boolean hasExpired = false;
        boolean hasCritical = false;

        for (VehicleDocument doc : docs) {
            if ("EXPIRED".equals(doc.getExpiryStatus())) hasExpired = true;
            if ("CRITICAL".equals(doc.getExpiryStatus())) hasCritical = true;
        }

        if (hasExpired) return "EXPIRED";
        if (hasCritical) return "CRITICAL";
        return "DUE_SOON";
    }

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
                    docsForDate.size() + " document(s) expire on " + date);
        } else {
            expiringDocumentsTable.getItems().clear();
            AlertUtil.showInfo("No Documents", "No documents expire on " + date);
        }
    }

    private void updateStatistics() {
        if (allDocuments == null) {
            expiredCountLabel.setText("0");
            expiringSoonLabel.setText("0");
            validCountLabel.setText("0");
            return;
        }

        int expired = 0;
        int expiringSoon = 0;
        int valid = 0;

        for (VehicleDocument doc : allDocuments) {
            String status = doc.getExpiryStatus();
            if ("EXPIRED".equals(status)) expired++;
            else if ("CRITICAL".equals(status) || "WARNING".equals(status) || "DUE_SOON".equals(status)) expiringSoon++;
            else valid++;
        }

        expiredCountLabel.setText(String.valueOf(expired));
        expiringSoonLabel.setText(String.valueOf(expiringSoon));
        validCountLabel.setText(String.valueOf(valid));

        expiringDocumentsTable.getItems().setAll(allDocuments);
    }
}