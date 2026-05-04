package controllers;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
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
import utils.ValidationUtil;
import dao.VehicleDAO;
import dao.CustomerDAO;
import dao.ViolationDAO;
import dao.StolenVehicleDAO;
import dao.WorkshopDAO;
import dao.InsurancePolicyDAO;
import dao.AuditDAO;
import dao.NotificationDAO;
import models.Vehicle;
import models.Customer;
import models.Violation;
import models.StolenVehicle;
import models.Workshop;
import models.InsurancePolicy;
import models.AuditLog;
import models.Notification;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class SearchController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> searchTypeComboBox;
    @FXML private Button searchButton;
    @FXML private Button clearButton;
    @FXML private Button backButton;
    @FXML private Button fadeButton;
    @FXML private TabPane resultsTabPane;
    @FXML private ProgressIndicator loadProgress;
    @FXML private ProgressBar operationProgress;
    @FXML private Label statusLabel;

    // Existing Tables
    @FXML private TableView<Vehicle> vehiclesTable;
    @FXML private TableView<Customer> customersTable;
    @FXML private TableView<Violation> violationsTable;
    @FXML private TableView<StolenVehicle> stolenTable;
    @FXML private TableView<Workshop> workshopsTable;
    @FXML private TableView<InsurancePolicy> insuranceTable;

    // New Tables for Audit Logs and Notifications
    @FXML private TableView<AuditLog> auditLogsTable;
    @FXML private TableView<Notification> notificationsTable;

    // Existing Table Columns
    @FXML private TableColumn<Vehicle, String> searchRegColumn;
    @FXML private TableColumn<Vehicle, String> searchMakeColumn;
    @FXML private TableColumn<Vehicle, String> searchModelColumn;
    @FXML private TableColumn<Vehicle, Integer> searchYearColumn;
    @FXML private TableColumn<Vehicle, String> searchStatusColumn;

    @FXML private TableColumn<Customer, String> searchNameColumn;
    @FXML private TableColumn<Customer, String> searchEmailColumn;
    @FXML private TableColumn<Customer, String> searchPhoneColumn;

    @FXML private TableColumn<Violation, String> searchViolationRegColumn;
    @FXML private TableColumn<Violation, String> searchViolationTypeColumn;
    @FXML private TableColumn<Violation, Double> searchFineColumn;
    @FXML private TableColumn<Violation, String> searchPaymentColumn;

    @FXML private TableColumn<StolenVehicle, String> searchStolenRegColumn;
    @FXML private TableColumn<StolenVehicle, String> searchCaseNumberColumn;
    @FXML private TableColumn<StolenVehicle, String> searchStolenStatusColumn;

    @FXML private TableColumn<Workshop, String> searchWorkshopNameColumn;
    @FXML private TableColumn<Workshop, String> searchWorkshopPhoneColumn;
    @FXML private TableColumn<Workshop, String> searchWorkshopStatusColumn;

    @FXML private TableColumn<InsurancePolicy, String> searchPolicyColumn;
    @FXML private TableColumn<InsurancePolicy, String> searchProviderColumn;
    @FXML private TableColumn<InsurancePolicy, String> searchInsuranceStatusColumn;

    // New Table Columns for Audit Logs
    @FXML private TableColumn<AuditLog, String> logUserColumn;
    @FXML private TableColumn<AuditLog, String> logActionColumn;
    @FXML private TableColumn<AuditLog, String> logTimestampColumn;
    @FXML private TableColumn<AuditLog, String> logIpColumn;

    // New Table Columns for Notifications
    @FXML private TableColumn<Notification, String> notifMessageColumn;
    @FXML private TableColumn<Notification, String> notifTypeColumn;
    @FXML private TableColumn<Notification, String> notifCreatedAtColumn;
    @FXML private TableColumn<Notification, String> notifStatusColumn;

    // Pagination
    @FXML private Pagination auditLogsPagination;
    @FXML private Pagination notificationsPagination;
    @FXML private Pagination vehiclesPagination;
    @FXML private Pagination customersPagination;
    @FXML private Pagination violationsPagination;
    @FXML private Pagination stolenPagination;
    @FXML private Pagination workshopsPagination;
    @FXML private Pagination insurancePagination;

    private VehicleDAO vehicleDAO;
    private CustomerDAO customerDAO;
    private ViolationDAO violationDAO;
    private StolenVehicleDAO stolenVehicleDAO;
    private WorkshopDAO workshopDAO;
    private InsurancePolicyDAO insuranceDAO;
    private AuditDAO auditDAO;
    private NotificationDAO notificationDAO;

    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private ObservableList<Vehicle> vehicleList;
    private ObservableList<Customer> customerList;
    private ObservableList<Violation> violationList;
    private ObservableList<StolenVehicle> stolenList;
    private ObservableList<Workshop> workshopList;
    private ObservableList<InsurancePolicy> insuranceList;
    private ObservableList<AuditLog> auditLogList;
    private ObservableList<Notification> notificationList;

    @FXML
    public void initialize() {
        vehicleDAO = new VehicleDAO();
        customerDAO = new CustomerDAO();
        violationDAO = new ViolationDAO();
        stolenVehicleDAO = new StolenVehicleDAO();
        workshopDAO = new WorkshopDAO();
        insuranceDAO = new InsurancePolicyDAO();
        auditDAO = new AuditDAO();
        notificationDAO = new NotificationDAO();

        // Initialize Observable Lists
        vehicleList = FXCollections.observableArrayList();
        customerList = FXCollections.observableArrayList();
        violationList = FXCollections.observableArrayList();
        stolenList = FXCollections.observableArrayList();
        workshopList = FXCollections.observableArrayList();
        insuranceList = FXCollections.observableArrayList();
        auditLogList = FXCollections.observableArrayList();
        notificationList = FXCollections.observableArrayList();

        setupSearchTypes();
        setupTableColumns();
        setupButtonHandlers();
        applyRoleBasedVisibility();
        applyVisualEffects();

        // Set table items
        if (vehiclesTable != null) vehiclesTable.setItems(vehicleList);
        if (customersTable != null) customersTable.setItems(customerList);
        if (violationsTable != null) violationsTable.setItems(violationList);
        if (stolenTable != null) stolenTable.setItems(stolenList);
        if (workshopsTable != null) workshopsTable.setItems(workshopList);
        if (insuranceTable != null) insuranceTable.setItems(insuranceList);
        if (auditLogsTable != null) auditLogsTable.setItems(auditLogList);
        if (notificationsTable != null) notificationsTable.setItems(notificationList);

        if (statusLabel != null) statusLabel.setText("Ready");
    }

    private void applyRoleBasedVisibility() {
        String role = SessionManager.getInstance().getUserRole();

        if (resultsTabPane == null) return;

        if ("POLICE".equals(role)) {
            for (int i = resultsTabPane.getTabs().size() - 1; i >= 0; i--) {
                String tabText = resultsTabPane.getTabs().get(i).getText();
                if (tabText.equals("Customers") || tabText.equals("Workshops") ||
                        tabText.equals("Insurance") || tabText.equals("Notifications")) {
                    resultsTabPane.getTabs().remove(i);
                }
            }
            if (searchTypeComboBox != null) {
                searchTypeComboBox.getItems().removeAll("Customers", "Workshops", "Insurance", "Notifications");
                searchTypeComboBox.setValue("Vehicles");
            }
        } else if ("INSURANCE".equals(role)) {
            for (int i = resultsTabPane.getTabs().size() - 1; i >= 0; i--) {
                String tabText = resultsTabPane.getTabs().get(i).getText();
                if (tabText.equals("Customers") || tabText.equals("Violations") ||
                        tabText.equals("Stolen Vehicles")) {
                    resultsTabPane.getTabs().remove(i);
                }
            }
            if (searchTypeComboBox != null) {
                searchTypeComboBox.getItems().removeAll("Customers", "Violations", "Stolen Vehicles");
                searchTypeComboBox.setValue("Vehicles");
            }
        } else if ("ADMIN".equals(role)) {
            // Admin sees everything
        } else {
            for (int i = resultsTabPane.getTabs().size() - 1; i >= 0; i--) {
                String tabText = resultsTabPane.getTabs().get(i).getText();
                if (!tabText.equals("Vehicles")) {
                    resultsTabPane.getTabs().remove(i);
                }
            }
            if (searchTypeComboBox != null) {
                searchTypeComboBox.getItems().removeIf(item -> !item.equals("Vehicles"));
                searchTypeComboBox.setValue("Vehicles");
            }
        }
    }

    private void applyVisualEffects() {
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(5.0);
        dropShadow.setOffsetX(2.0);
        dropShadow.setOffsetY(2.0);
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.3));

        if (searchButton != null) searchButton.setEffect(dropShadow);
        if (clearButton != null) clearButton.setEffect(dropShadow);
        if (backButton != null) backButton.setEffect(dropShadow);
        if (fadeButton != null) fadeButton.setEffect(dropShadow);
    }

    private void setupSearchTypes() {
        if (searchTypeComboBox != null) {
            searchTypeComboBox.getItems().addAll(
                    "Vehicles", "Customers", "Violations", "Stolen Vehicles",
                    "Workshops", "Insurance", "Audit Logs", "Notifications"
            );
            searchTypeComboBox.setValue("Vehicles");
        }
    }

    private void setupTableColumns() {
        // Vehicles Table
        if (vehiclesTable != null && searchRegColumn != null) {
            searchRegColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getRegistrationNumber()));
            searchMakeColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getMake()));
            searchModelColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getModel()));
            // FIXED: Use SimpleObjectProperty for Integer column
            searchYearColumn.setCellValueFactory(cellData ->
                    new SimpleObjectProperty<>(cellData.getValue().getYear()));
            searchStatusColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getStatusName()));
        }

        // Customers Table
        if (customersTable != null && searchNameColumn != null) {
            searchNameColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getName()));
            searchEmailColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getEmail()));
            searchPhoneColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getPhone()));
        }

        // Violations Table
        if (violationsTable != null && searchViolationRegColumn != null) {
            searchViolationRegColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getRegistrationNumber()));
            searchViolationTypeColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getViolationType()));
            // FIXED: Use SimpleObjectProperty for Double column
            searchFineColumn.setCellValueFactory(cellData ->
                    new SimpleObjectProperty<>(cellData.getValue().getFineAmount()));
            searchPaymentColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getPaymentStatus()));
        }

        // Stolen Vehicles Table
        if (stolenTable != null && searchStolenRegColumn != null) {
            searchStolenRegColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getRegistrationNumber()));
            searchCaseNumberColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getCaseNumber()));
            searchStolenStatusColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getStatus()));
        }

        // Workshops Table
        if (workshopsTable != null && searchWorkshopNameColumn != null) {
            searchWorkshopNameColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getWorkshopName()));
            searchWorkshopPhoneColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getPhone()));
            searchWorkshopStatusColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().isApproved() ? "Approved" : "Pending"));
        }

        // Insurance Table
        if (insuranceTable != null && searchPolicyColumn != null) {
            searchPolicyColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getPolicyNumber()));
            searchProviderColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getProviderName()));
            searchInsuranceStatusColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getStatus()));
        }

        // Audit Logs Table
        if (auditLogsTable != null && logUserColumn != null) {
            logUserColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getUsername()));
            logActionColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getAction()));
            logTimestampColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getTimestamp() != null ?
                            cellData.getValue().getTimestamp().format(dateTimeFormatter) : ""));
            logIpColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getIpAddress()));
        }

        // Notifications Table
        if (notificationsTable != null && notifMessageColumn != null) {
            notifMessageColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getMessage()));
            notifTypeColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getType()));
            notifCreatedAtColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getCreatedAt() != null ?
                            cellData.getValue().getCreatedAt().format(dateTimeFormatter) : ""));
            notifStatusColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().isRead() ? "Read" : "Unread"));
        }
    }

    private void setupButtonHandlers() {
        if (searchButton != null) searchButton.setOnAction(event -> handleSearch());
        if (clearButton != null) clearButton.setOnAction(event -> handleClear());
        if (backButton != null) backButton.setOnAction(event -> handleBack());
        if (fadeButton != null) {
            fadeButton.setOnAction(event -> showFadeAnimation());
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
            if (statusLabel != null) statusLabel.setText("Animation played!");
            PauseTransition reset = new PauseTransition(Duration.seconds(2));
            reset.setOnFinished(e -> {
                if (statusLabel != null) statusLabel.setText("Ready");
            });
            reset.play();
        }
    }

    private void handleSearch() {
        String keyword = searchField != null ? searchField.getText().trim() : "";
        String searchType = searchTypeComboBox != null ? searchTypeComboBox.getValue() : "Vehicles";
        String role = SessionManager.getInstance().getUserRole();

        if (!ValidationUtil.isNotEmpty(keyword)) {
            AlertUtil.showWarning("Search Error", "Please enter a search term.");
            return;
        }

        showProgress(true);
        if (statusLabel != null) statusLabel.setText("Searching for " + keyword + "...");

        // Log search action
        int currentUserId = SessionManager.getInstance().getUserId();
        try {
            auditDAO.logAction(currentUserId, "SEARCH: " + searchType + " - " + keyword, "127.0.0.1");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            switch (searchType) {
                case "Vehicles":
                    List<Vehicle> vehicles = vehicleDAO.searchVehicles(keyword);
                    if (vehiclesTable != null) vehicleList.setAll(vehicles);
                    int vehicleIndex = findTabIndex("Vehicles");
                    if (vehicleIndex >= 0 && resultsTabPane != null) resultsTabPane.getSelectionModel().select(vehicleIndex);
                    AlertUtil.showInfo("Search Results", "Found " + vehicles.size() + " vehicles.");
                    if (statusLabel != null) statusLabel.setText("Found " + vehicles.size() + " vehicles");
                    break;

                case "Customers":
                    if (!"INSURANCE".equals(role) && !"POLICE".equals(role)) {
                        List<Customer> customers = customerDAO.searchCustomers(keyword);
                        if (customersTable != null) customerList.setAll(customers);
                        int customerIndex = findTabIndex("Customers");
                        if (customerIndex >= 0 && resultsTabPane != null) resultsTabPane.getSelectionModel().select(customerIndex);
                        AlertUtil.showInfo("Search Results", "Found " + customers.size() + " customers.");
                        if (statusLabel != null) statusLabel.setText("Found " + customers.size() + " customers");
                    } else {
                        AlertUtil.showWarning("Access Denied", "You do not have permission to view customer details.");
                    }
                    break;

                case "Violations":
                    if (!"INSURANCE".equals(role)) {
                        List<Violation> violations = violationDAO.findByViolationType(keyword);
                        if (violationsTable != null) violationList.setAll(violations);
                        int violationIndex = findTabIndex("Violations");
                        if (violationIndex >= 0 && resultsTabPane != null) resultsTabPane.getSelectionModel().select(violationIndex);
                        AlertUtil.showInfo("Search Results", "Found " + violations.size() + " violations.");
                        if (statusLabel != null) statusLabel.setText("Found " + violations.size() + " violations");
                    } else {
                        AlertUtil.showWarning("Access Denied", "Insurance providers cannot view violation details.");
                    }
                    break;

                case "Stolen Vehicles":
                    if (!"INSURANCE".equals(role)) {
                        List<StolenVehicle> stolen = stolenVehicleDAO.findByRegistrationNumber(keyword);
                        if (stolenTable != null) stolenList.setAll(stolen);
                        int stolenIndex = findTabIndex("Stolen Vehicles");
                        if (stolenIndex >= 0 && resultsTabPane != null) resultsTabPane.getSelectionModel().select(stolenIndex);
                        AlertUtil.showInfo("Search Results", "Found " + stolen.size() + " stolen vehicles.");
                        if (statusLabel != null) statusLabel.setText("Found " + stolen.size() + " stolen vehicles");
                    } else {
                        AlertUtil.showWarning("Access Denied", "Insurance providers cannot view stolen vehicle details.");
                    }
                    break;

                case "Workshops":
                    List<Workshop> workshops = workshopDAO.searchWorkshops(keyword);
                    if (workshopsTable != null) workshopList.setAll(workshops);
                    int workshopIndex = findTabIndex("Workshops");
                    if (workshopIndex >= 0 && resultsTabPane != null) resultsTabPane.getSelectionModel().select(workshopIndex);
                    AlertUtil.showInfo("Search Results", "Found " + workshops.size() + " workshops.");
                    if (statusLabel != null) statusLabel.setText("Found " + workshops.size() + " workshops");
                    break;

                case "Insurance":
                    List<InsurancePolicy> policies = insuranceDAO.searchInsurance(keyword);
                    if (insuranceTable != null) insuranceList.setAll(policies);
                    int insuranceIndex = findTabIndex("Insurance");
                    if (insuranceIndex >= 0 && resultsTabPane != null) resultsTabPane.getSelectionModel().select(insuranceIndex);
                    AlertUtil.showInfo("Search Results", "Found " + policies.size() + " insurance policies.");
                    if (statusLabel != null) statusLabel.setText("Found " + policies.size() + " insurance policies");
                    break;

                case "Audit Logs":
                    List<AuditLog> auditLogs = auditDAO.findAll();
                    if (auditLogsTable != null) {
                        if (keyword != null && !keyword.isEmpty()) {
                            String lowerKeyword = keyword.toLowerCase();
                            auditLogs.removeIf(log ->
                                    (log.getUsername() == null || !log.getUsername().toLowerCase().contains(lowerKeyword)) &&
                                            (log.getAction() == null || !log.getAction().toLowerCase().contains(lowerKeyword)));
                        }
                        auditLogList.setAll(auditLogs);
                    }
                    int auditIndex = findTabIndex("Audit Logs");
                    if (auditIndex >= 0 && resultsTabPane != null) resultsTabPane.getSelectionModel().select(auditIndex);
                    AlertUtil.showInfo("Search Results", "Found " + auditLogs.size() + " audit logs.");
                    if (statusLabel != null) statusLabel.setText("Found " + auditLogs.size() + " audit logs");
                    break;

                case "Notifications":
                    List<Notification> notifications = notificationDAO.findByUserId(SessionManager.getInstance().getUserId());
                    if (notificationsTable != null) {
                        if (keyword != null && !keyword.isEmpty()) {
                            String lowerKeyword = keyword.toLowerCase();
                            notifications.removeIf(notif ->
                                    notif.getMessage() == null || !notif.getMessage().toLowerCase().contains(lowerKeyword));
                        }
                        notificationList.setAll(notifications);
                    }
                    int notifIndex = findTabIndex("Notifications");
                    if (notifIndex >= 0 && resultsTabPane != null) resultsTabPane.getSelectionModel().select(notifIndex);
                    AlertUtil.showInfo("Search Results", "Found " + notifications.size() + " notifications.");
                    if (statusLabel != null) statusLabel.setText("Found " + notifications.size() + " notifications");
                    break;

                default:
                    AlertUtil.showWarning("Invalid Search Type", "Please select a valid search type.");
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Search Failed", "An error occurred during search: " + e.getMessage());
            if (statusLabel != null) statusLabel.setText("Search failed: " + e.getMessage());
        } finally {
            hideProgressAfterDelay();
        }
    }

    private int findTabIndex(String tabText) {
        if (resultsTabPane == null) return -1;
        for (int i = 0; i < resultsTabPane.getTabs().size(); i++) {
            if (resultsTabPane.getTabs().get(i).getText().equals(tabText)) {
                return i;
            }
        }
        return -1;
    }

    private void handleClear() {
        if (searchField != null) searchField.clear();

        if (vehicleList != null) vehicleList.clear();
        if (customerList != null) customerList.clear();
        if (violationList != null) violationList.clear();
        if (stolenList != null) stolenList.clear();
        if (workshopList != null) workshopList.clear();
        if (insuranceList != null) insuranceList.clear();
        if (auditLogList != null) auditLogList.clear();
        if (notificationList != null) notificationList.clear();

        if (statusLabel != null) statusLabel.setText("Form cleared");
        AlertUtil.showSuccess("Form Cleared", "All search results have been cleared.");
    }

    private void handleBack() {
        String role = SessionManager.getInstance().getUserRole();
        if ("ADMIN".equals(role)) {
            SceneManager.getInstance().switchToAdminView();
        } else if ("POLICE".equals(role)) {
            SceneManager.getInstance().switchToPoliceView();
        } else if ("INSURANCE".equals(role)) {
            SceneManager.getInstance().switchToInsuranceView();
        } else if ("WORKSHOP".equals(role)) {
            SceneManager.getInstance().switchToWorkshopView();
        } else if ("CUSTOMER".equals(role)) {
            SceneManager.getInstance().switchToCustomerView();
        } else {
            SceneManager.getInstance().switchToDashboard();
        }
    }

    private void showProgress(boolean show) {
        if (loadProgress != null) loadProgress.setVisible(show);
        if (operationProgress != null) {
            operationProgress.setVisible(show);
            operationProgress.setProgress(0);
        }
    }

    private void hideProgressAfterDelay() {
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