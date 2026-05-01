package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import utils.AlertUtil;
import utils.SceneManager;
import dao.VehicleHistoryDAO;
import dao.VehicleDAO;
import models.VehicleHistory;
import models.Vehicle;
import java.time.LocalDate;

public class VehicleHistoryController {

    @FXML private ComboBox<Vehicle> vehicleComboBox;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Button searchButton;
    @FXML private Button refreshButton;
    @FXML private Button exportButton;
    @FXML private Button backButton;

    @FXML private TableView<VehicleHistory> historyTable;
    @FXML private TableColumn<VehicleHistory, String> eventTypeColumn;
    @FXML private TableColumn<VehicleHistory, String> eventDateColumn;
    @FXML private TableColumn<VehicleHistory, String> descriptionColumn;
    @FXML private TableColumn<VehicleHistory, String> detailsColumn;

    @FXML private Label vehicleInfoLabel;
    @FXML private Label totalEventsLabel;

    private VehicleHistoryDAO historyDAO;
    private VehicleDAO vehicleDAO;

    @FXML
    public void initialize() {
        historyDAO = new VehicleHistoryDAO();
        vehicleDAO = new VehicleDAO();

        setupTableColumns();
        loadVehicles();
        setupButtonHandlers();

        startDatePicker.setValue(LocalDate.now().minusMonths(6));
        endDatePicker.setValue(LocalDate.now());
    }

    private void setupTableColumns() {
        eventTypeColumn.setCellValueFactory(cellData -> cellData.getValue().eventTypeProperty());
        eventDateColumn.setCellValueFactory(cellData -> cellData.getValue().eventDateProperty().asString());
        descriptionColumn.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        detailsColumn.setCellValueFactory(cellData -> cellData.getValue().detailsProperty());
    }

    private void loadVehicles() {
        try {
            String role = utils.SessionManager.getInstance().getUserRole();
            if ("CUSTOMER".equals(role)) {
                int customerId = utils.SessionManager.getInstance().getCustomerId();
                java.util.List<Vehicle> vehicles = vehicleDAO.findByOwnerId(customerId);
                vehicleComboBox.getItems().setAll(vehicles);
            } else {
                java.util.List<Vehicle> vehicles = vehicleDAO.findAll();
                vehicleComboBox.getItems().setAll(vehicles);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupButtonHandlers() {
        searchButton.setOnAction(event -> handleSearch());
        refreshButton.setOnAction(event -> handleSearch());
        exportButton.setOnAction(event -> handleExport());
        backButton.setOnAction(event -> handleBack());
    }

    private void handleSearch() {
        Vehicle selectedVehicle = vehicleComboBox.getSelectionModel().getSelectedItem();

        if (selectedVehicle == null) {
            AlertUtil.showWarning("Validation Error", "Please select a vehicle.");
            return;
        }

        try {
            java.util.List<VehicleHistory> history;

            if (startDatePicker.getValue() != null && endDatePicker.getValue() != null) {
                history = historyDAO.findByVehicleIdAndDateRange(
                        selectedVehicle.getId(),
                        startDatePicker.getValue(),
                        endDatePicker.getValue()
                );
            } else {
                history = historyDAO.findByVehicleId(selectedVehicle.getId());
            }

            historyTable.getItems().setAll(history);

            vehicleInfoLabel.setText(selectedVehicle.getRegistrationNumber() + " - " +
                    selectedVehicle.getMake() + " " + selectedVehicle.getModel());
            totalEventsLabel.setText("Total Events: " + history.size());

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Search Failed", "Failed to load vehicle history.");
        }
    }

    private void handleExport() {
        if (historyTable.getItems().isEmpty()) {
            AlertUtil.showWarning("No Data", "No history data to export.");
            return;
        }

        try {
            String fileName = "vehicle_history_" + System.currentTimeMillis();
            utils.ExportUtil.exportToCSV(historyTable, fileName);
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Export Failed", "Failed to export history.");
        }
    }

    private void handleBack() {
        String role = utils.SessionManager.getInstance().getUserRole();
        if ("CUSTOMER".equals(role)) {
            SceneManager.getInstance().switchToCustomerProfileView();
        } else if ("ADMIN".equals(role)) {
            SceneManager.getInstance().switchToAdminView();
        } else {
            SceneManager.getInstance().switchToVehicleView();
        }
    }
}