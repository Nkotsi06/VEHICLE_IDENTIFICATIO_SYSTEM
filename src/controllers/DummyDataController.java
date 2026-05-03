package controllers;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import utils.AlertUtil;
import utils.SceneManager;
import utils.SessionManager;
import dao.DummyDataDAO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class DummyDataController {

    // Users Table
    @FXML private TableView<Map<String, Object>> usersTable;
    @FXML private TableColumn<Map<String, Object>, String> userIdColumn;
    @FXML private TableColumn<Map<String, Object>, String> userUsernameColumn;
    @FXML private TableColumn<Map<String, Object>, String> userFullNameColumn;
    @FXML private TableColumn<Map<String, Object>, String> userEmailColumn;
    @FXML private TableColumn<Map<String, Object>, String> userRoleColumn;
    @FXML private TableColumn<Map<String, Object>, String> userActiveColumn;
    @FXML private TableColumn<Map<String, Object>, String> userCreatedAtColumn;
    @FXML private Pagination usersPagination;
    @FXML private Label usersCountLabel;

    // Vehicles Table
    @FXML private TableView<Map<String, Object>> vehiclesTable;
    @FXML private TableColumn<Map<String, Object>, String> vehicleIdColumn;
    @FXML private TableColumn<Map<String, Object>, String> vehicleRegColumn;
    @FXML private TableColumn<Map<String, Object>, String> vehicleMakeColumn;
    @FXML private TableColumn<Map<String, Object>, String> vehicleModelColumn;
    @FXML private TableColumn<Map<String, Object>, String> vehicleYearColumn;
    @FXML private TableColumn<Map<String, Object>, String> vehicleOwnerColumn;
    @FXML private TableColumn<Map<String, Object>, String> vehicleStatusColumn;
    @FXML private TableColumn<Map<String, Object>, String> vehicleCreatedAtColumn;
    @FXML private Pagination vehiclesPagination;
    @FXML private Label vehiclesCountLabel;

    // Customers Table
    @FXML private TableView<Map<String, Object>> customersTable;
    @FXML private TableColumn<Map<String, Object>, String> customerIdColumn;
    @FXML private TableColumn<Map<String, Object>, String> customerNameColumn;
    @FXML private TableColumn<Map<String, Object>, String> customerEmailColumn;
    @FXML private TableColumn<Map<String, Object>, String> customerPhoneColumn;
    @FXML private TableColumn<Map<String, Object>, String> customerAddressColumn;
    @FXML private TableColumn<Map<String, Object>, String> customerVehiclesColumn;
    @FXML private TableColumn<Map<String, Object>, String> customerCreatedAtColumn;
    @FXML private Pagination customersPagination;
    @FXML private Label customersCountLabel;

    // Service Records Table
    @FXML private TableView<Map<String, Object>> serviceRecordsTable;
    @FXML private TableColumn<Map<String, Object>, String> serviceIdColumn;
    @FXML private TableColumn<Map<String, Object>, String> serviceVehicleColumn;
    @FXML private TableColumn<Map<String, Object>, String> serviceWorkshopColumn;
    @FXML private TableColumn<Map<String, Object>, String> serviceTypeColumn;
    @FXML private TableColumn<Map<String, Object>, String> serviceDateColumn;
    @FXML private TableColumn<Map<String, Object>, String> serviceCostColumn;
    @FXML private TableColumn<Map<String, Object>, String> serviceOdometerColumn;
    @FXML private TableColumn<Map<String, Object>, String> serviceStatusColumn;
    @FXML private Pagination servicePagination;
    @FXML private Label serviceCountLabel;

    // Controls
    @FXML private Button refreshAllButton;
    @FXML private Button backButton;
    @FXML private Button fadeButton;
    @FXML private ProgressIndicator loadProgress;
    @FXML private ProgressBar operationProgress;
    @FXML private Label statusLabel;
    @FXML private Label lastUpdatedLabel;

    private DummyDataDAO dummyDataDAO;
    private ObservableList<Map<String, Object>> userList;
    private ObservableList<Map<String, Object>> vehicleList;
    private ObservableList<Map<String, Object>> customerList;
    private ObservableList<Map<String, Object>> serviceList;

    private List<Map<String, Object>> fullUserData;
    private List<Map<String, Object>> fullVehicleData;
    private List<Map<String, Object>> fullCustomerData;
    private List<Map<String, Object>> fullServiceData;

    private int currentUserPage = 0;
    private int currentVehiclePage = 0;
    private int currentCustomerPage = 0;
    private int currentServicePage = 0;
    private int pageSize = 20;

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @FXML
    public void initialize() {
        if (!SessionManager.getInstance().isAdmin()) {
            AlertUtil.showError("Access Denied", "Only administrators can access this feature.");
            SceneManager.getInstance().switchToAdminView();
            return;
        }

        dummyDataDAO = new DummyDataDAO();

        userList = FXCollections.observableArrayList();
        vehicleList = FXCollections.observableArrayList();
        customerList = FXCollections.observableArrayList();
        serviceList = FXCollections.observableArrayList();

        setupTableColumns();
        setupPagination();
        setupButtonHandlers();
        applyVisualEffects();
        loadAllData();

        statusLabel.setText("Ready - Dummy Data Viewer (ADMIN Only)");
    }

    private void setupTableColumns() {
        // Users Table Columns
        userIdColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(getStringValue(cellData.getValue(), "id")));
        userUsernameColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(getStringValue(cellData.getValue(), "username")));
        userFullNameColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(getStringValue(cellData.getValue(), "full_name")));
        userEmailColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(getStringValue(cellData.getValue(), "email")));
        userRoleColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(getStringValue(cellData.getValue(), "role")));
        userActiveColumn.setCellValueFactory(cellData -> {
            Boolean isActive = (Boolean) cellData.getValue().get("is_active");
            return new javafx.beans.property.SimpleStringProperty(isActive != null && isActive ? "ACTIVE" : "INACTIVE");
        });
        userCreatedAtColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(formatDate(getValue(cellData.getValue(), "created_at"))));

        // Vehicles Table Columns
        vehicleIdColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(getStringValue(cellData.getValue(), "id")));
        vehicleRegColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(getStringValue(cellData.getValue(), "registration_number")));
        vehicleMakeColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(getStringValue(cellData.getValue(), "make")));
        vehicleModelColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(getStringValue(cellData.getValue(), "model")));
        vehicleYearColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(getStringValue(cellData.getValue(), "year")));
        vehicleOwnerColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(getStringValue(cellData.getValue(), "owner_name")));
        vehicleStatusColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(getStringValue(cellData.getValue(), "status_name")));
        vehicleCreatedAtColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(formatDate(getValue(cellData.getValue(), "created_at"))));

        // Customers Table Columns
        customerIdColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(getStringValue(cellData.getValue(), "id")));
        customerNameColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(getStringValue(cellData.getValue(), "name")));
        customerEmailColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(getStringValue(cellData.getValue(), "email")));
        customerPhoneColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(getStringValue(cellData.getValue(), "phone")));
        customerAddressColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(getStringValue(cellData.getValue(), "address")));
        customerVehiclesColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(getStringValue(cellData.getValue(), "vehicle_count")));
        customerCreatedAtColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(formatDate(getValue(cellData.getValue(), "created_at"))));

        // Service Records Table Columns
        serviceIdColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(getStringValue(cellData.getValue(), "id")));
        serviceVehicleColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(getStringValue(cellData.getValue(), "registration_number")));
        serviceWorkshopColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(getStringValue(cellData.getValue(), "workshop_name")));
        serviceTypeColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(getStringValue(cellData.getValue(), "service_type")));
        serviceDateColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(formatDate(getValue(cellData.getValue(), "service_date"))));
        serviceCostColumn.setCellValueFactory(cellData -> {
            Object cost = cellData.getValue().get("cost");
            double amount = cost instanceof Number ? ((Number) cost).doubleValue() : 0;
            return new javafx.beans.property.SimpleStringProperty(String.format("M%,.2f", amount));
        });
        serviceOdometerColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(getStringValue(cellData.getValue(), "odometer_reading")));
        serviceStatusColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(getStringValue(cellData.getValue(), "status")));

        // Set table items
        usersTable.setItems(userList);
        vehiclesTable.setItems(vehicleList);
        customersTable.setItems(customerList);
        serviceRecordsTable.setItems(serviceList);
    }

    private void setupPagination() {
        if (usersPagination != null) {
            usersPagination.currentPageIndexProperty().addListener((obs, old, newPage) -> {
                currentUserPage = newPage.intValue();
                updateUsersPage();
            });
        }
        if (vehiclesPagination != null) {
            vehiclesPagination.currentPageIndexProperty().addListener((obs, old, newPage) -> {
                currentVehiclePage = newPage.intValue();
                updateVehiclesPage();
            });
        }
        if (customersPagination != null) {
            customersPagination.currentPageIndexProperty().addListener((obs, old, newPage) -> {
                currentCustomerPage = newPage.intValue();
                updateCustomersPage();
            });
        }
        if (servicePagination != null) {
            servicePagination.currentPageIndexProperty().addListener((obs, old, newPage) -> {
                currentServicePage = newPage.intValue();
                updateServicePage();
            });
        }
    }

    private void updateUsersPage() {
        if (fullUserData == null || fullUserData.isEmpty()) return;
        int start = currentUserPage * pageSize;
        int end = Math.min(start + pageSize, fullUserData.size());
        if (start < fullUserData.size()) {
            userList.setAll(fullUserData.subList(start, end));
        }
    }

    private void updateVehiclesPage() {
        if (fullVehicleData == null || fullVehicleData.isEmpty()) return;
        int start = currentVehiclePage * pageSize;
        int end = Math.min(start + pageSize, fullVehicleData.size());
        if (start < fullVehicleData.size()) {
            vehicleList.setAll(fullVehicleData.subList(start, end));
        }
    }

    private void updateCustomersPage() {
        if (fullCustomerData == null || fullCustomerData.isEmpty()) return;
        int start = currentCustomerPage * pageSize;
        int end = Math.min(start + pageSize, fullCustomerData.size());
        if (start < fullCustomerData.size()) {
            customerList.setAll(fullCustomerData.subList(start, end));
        }
    }

    private void updateServicePage() {
        if (fullServiceData == null || fullServiceData.isEmpty()) return;
        int start = currentServicePage * pageSize;
        int end = Math.min(start + pageSize, fullServiceData.size());
        if (start < fullServiceData.size()) {
            serviceList.setAll(fullServiceData.subList(start, end));
        }
    }

    private void setupButtonHandlers() {
        refreshAllButton.setOnAction(event -> {
            loadAllData();
            statusLabel.setText("All data refreshed");
            PauseTransition reset = new PauseTransition(Duration.seconds(2));
            reset.setOnFinished(e -> statusLabel.setText("Ready - Dummy Data Viewer (ADMIN Only)"));
            reset.play();
        });
        backButton.setOnAction(event -> SceneManager.getInstance().switchToAdminView());
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
        refreshAllButton.setEffect(dropShadow);
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
            reset.setOnFinished(e -> statusLabel.setText("Ready - Dummy Data Viewer (ADMIN Only)"));
            reset.play();
        }
    }

    private void loadAllData() {
        showProgress(true);
        statusLabel.setText("Loading data from database...");

        new Thread(() -> {
            try {
                List<Map<String, Object>> users = dummyDataDAO.getUsers();
                List<Map<String, Object>> vehicles = dummyDataDAO.getVehicles();
                List<Map<String, Object>> customers = dummyDataDAO.getCustomers();
                List<Map<String, Object>> services = dummyDataDAO.getServiceRecords();

                Platform.runLater(() -> {
                    try {
                        fullUserData = users;
                        userList.setAll(users);
                        int totalUsers = users.size();
                        if (usersPagination != null) usersPagination.setPageCount(Math.max(1, (int) Math.ceil(totalUsers / (double) pageSize)));
                        if (usersCountLabel != null) usersCountLabel.setText("Total Users: " + totalUsers);

                        fullVehicleData = vehicles;
                        vehicleList.setAll(vehicles);
                        int totalVehicles = vehicles.size();
                        if (vehiclesPagination != null) vehiclesPagination.setPageCount(Math.max(1, (int) Math.ceil(totalVehicles / (double) pageSize)));
                        if (vehiclesCountLabel != null) vehiclesCountLabel.setText("Total Vehicles: " + totalVehicles);

                        fullCustomerData = customers;
                        customerList.setAll(customers);
                        int totalCustomers = customers.size();
                        if (customersPagination != null) customersPagination.setPageCount(Math.max(1, (int) Math.ceil(totalCustomers / (double) pageSize)));
                        if (customersCountLabel != null) customersCountLabel.setText("Total Customers: " + totalCustomers);

                        fullServiceData = services;
                        serviceList.setAll(services);
                        int totalServices = services.size();
                        if (servicePagination != null) servicePagination.setPageCount(Math.max(1, (int) Math.ceil(totalServices / (double) pageSize)));
                        if (serviceCountLabel != null) serviceCountLabel.setText("Total Service Records: " + totalServices);

                        updateLastUpdated();

                        if (totalUsers == 0 && totalVehicles == 0 && totalCustomers == 0 && totalServices == 0) {
                            statusLabel.setText("No data found in database. Please add records first.");
                        } else {
                            statusLabel.setText("Data loaded: " + totalUsers + " users, " + totalVehicles + " vehicles, " +
                                    totalCustomers + " customers, " + totalServices + " services");
                        }
                    } catch (Exception e) {
                        statusLabel.setText("Error updating UI: " + e.getMessage());
                    } finally {
                        showProgress(false);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    statusLabel.setText("Error loading data: " + e.getMessage());
                    AlertUtil.showError("Load Error", "Failed to load dummy data: " + e.getMessage());
                    showProgress(false);
                });
            }
        }).start();
    }

    private void updateLastUpdated() {
        if (lastUpdatedLabel != null) {
            lastUpdatedLabel.setText("Last Updated: " + LocalDateTime.now().format(formatter));
        }
    }

    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "";
    }

    private Object getValue(Map<String, Object> map, String key) {
        return map.get(key);
    }

    private String formatDate(Object date) {
        if (date == null) return "";
        if (date instanceof java.sql.Timestamp) {
            return ((java.sql.Timestamp) date).toLocalDateTime().format(formatter);
        }
        if (date instanceof java.sql.Date) {
            return ((java.sql.Date) date).toLocalDate().toString();
        }
        return date.toString();
    }

    private void showProgress(boolean show) {
        if (loadProgress != null) loadProgress.setVisible(show);
        if (operationProgress != null) {
            operationProgress.setVisible(show);
            operationProgress.setProgress(0);
        }
    }
}