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
import utils.SessionManager;
import utils.ValidationUtil;
import dao.VehicleDAO;
import dao.CustomerDAO;
import dao.ViolationDAO;
import dao.StolenVehicleDAO;
import dao.WorkshopDAO;
import dao.InsurancePolicyDAO;
import dao.AuditDAO;
import models.Vehicle;
import models.Customer;
import models.Violation;
import models.StolenVehicle;
import models.Workshop;
import models.InsurancePolicy;

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

    @FXML private TableView<Vehicle> vehiclesTable;
    @FXML private TableView<Customer> customersTable;
    @FXML private TableView<Violation> violationsTable;
    @FXML private TableView<StolenVehicle> stolenTable;
    @FXML private TableView<Workshop> workshopsTable;
    @FXML private TableView<InsurancePolicy> insuranceTable;

    private VehicleDAO vehicleDAO;
    private CustomerDAO customerDAO;
    private ViolationDAO violationDAO;
    private StolenVehicleDAO stolenVehicleDAO;
    private WorkshopDAO workshopDAO;
    private InsurancePolicyDAO insuranceDAO;
    private AuditDAO auditDAO;

    @FXML
    public void initialize() {
        vehicleDAO = new VehicleDAO();
        customerDAO = new CustomerDAO();
        violationDAO = new ViolationDAO();
        stolenVehicleDAO = new StolenVehicleDAO();
        workshopDAO = new WorkshopDAO();
        insuranceDAO = new InsurancePolicyDAO();
        auditDAO = new AuditDAO();

        setupSearchTypes();
        setupTableColumns();
        setupButtonHandlers();
        applyRoleBasedVisibility();
        applyVisualEffects();

        if (statusLabel != null) statusLabel.setText("Ready");
    }

    private void applyRoleBasedVisibility() {
        String role = SessionManager.getInstance().getUserRole();

        if (resultsTabPane == null) return;

        if ("POLICE".equals(role)) {
            // Police: Vehicles, Violations, Stolen Vehicles only
            if (resultsTabPane.getTabs().size() > 1) resultsTabPane.getTabs().remove(1);
            if (resultsTabPane.getTabs().size() > 3) resultsTabPane.getTabs().remove(3);
            if (resultsTabPane.getTabs().size() > 3) resultsTabPane.getTabs().remove(3);

            if (searchTypeComboBox != null) {
                searchTypeComboBox.getItems().remove("Customers");
                searchTypeComboBox.getItems().remove("Workshops");
                searchTypeComboBox.getItems().remove("Insurance");
                searchTypeComboBox.setValue("Vehicles");
            }

        } else if ("INSURANCE".equals(role)) {
            // Insurance: Vehicles, Workshops, Insurance only
            if (resultsTabPane.getTabs().size() > 1) resultsTabPane.getTabs().remove(1);
            if (resultsTabPane.getTabs().size() > 1) resultsTabPane.getTabs().remove(1);
            if (resultsTabPane.getTabs().size() > 1) resultsTabPane.getTabs().remove(1);

            if (searchTypeComboBox != null) {
                searchTypeComboBox.getItems().remove("Customers");
                searchTypeComboBox.getItems().remove("Violations");
                searchTypeComboBox.getItems().remove("Stolen Vehicles");
                searchTypeComboBox.setValue("Vehicles");
            }

        } else if ("ADMIN".equals(role)) {
            // Admin sees everything - no changes
        } else {
            // Other roles (Customer, Workshop) - limit what they can search
            if (resultsTabPane.getTabs().size() > 1) resultsTabPane.getTabs().remove(1);
            if (resultsTabPane.getTabs().size() > 3) resultsTabPane.getTabs().remove(3);
            if (resultsTabPane.getTabs().size() > 3) resultsTabPane.getTabs().remove(3);
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
                    "Vehicles", "Customers", "Violations", "Stolen Vehicles", "Workshops", "Insurance"
            );
            searchTypeComboBox.setValue("Vehicles");
        }
    }

    private void setupTableColumns() {
        // Vehicles Table
        if (vehiclesTable != null) {
            vehiclesTable.getColumns().clear();
            TableColumn<Vehicle, String> regCol = new TableColumn<>("Registration");
            regCol.setCellValueFactory(cellData -> cellData.getValue().registrationNumberProperty());
            TableColumn<Vehicle, String> makeCol = new TableColumn<>("Make");
            makeCol.setCellValueFactory(cellData -> cellData.getValue().makeProperty());
            TableColumn<Vehicle, String> modelCol = new TableColumn<>("Model");
            modelCol.setCellValueFactory(cellData -> cellData.getValue().modelProperty());
            TableColumn<Vehicle, String> yearCol = new TableColumn<>("Year");
            yearCol.setCellValueFactory(cellData -> cellData.getValue().yearProperty().asString());
            vehiclesTable.getColumns().addAll(regCol, makeCol, modelCol, yearCol);
        }

        // Customers Table
        if (customersTable != null) {
            customersTable.getColumns().clear();
            TableColumn<Customer, String> nameCol = new TableColumn<>("Name");
            nameCol.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
            TableColumn<Customer, String> emailCol = new TableColumn<>("Email");
            emailCol.setCellValueFactory(cellData -> cellData.getValue().emailProperty());
            TableColumn<Customer, String> phoneCol = new TableColumn<>("Phone");
            phoneCol.setCellValueFactory(cellData -> cellData.getValue().phoneProperty());
            customersTable.getColumns().addAll(nameCol, emailCol, phoneCol);
        }

        // Violations Table
        if (violationsTable != null) {
            violationsTable.getColumns().clear();
            TableColumn<Violation, String> typeCol = new TableColumn<>("Violation Type");
            typeCol.setCellValueFactory(cellData -> cellData.getValue().violationTypeProperty());
            TableColumn<Violation, String> dateCol = new TableColumn<>("Date");
            dateCol.setCellValueFactory(cellData -> cellData.getValue().violationDateProperty().asString());
            TableColumn<Violation, Double> fineCol = new TableColumn<>("Fine");
            fineCol.setCellValueFactory(cellData -> cellData.getValue().fineAmountProperty().asObject());
            violationsTable.getColumns().addAll(typeCol, dateCol, fineCol);
        }

        // Stolen Vehicles Table
        if (stolenTable != null) {
            stolenTable.getColumns().clear();
            TableColumn<StolenVehicle, String> regStolenCol = new TableColumn<>("Registration");
            regStolenCol.setCellValueFactory(cellData -> cellData.getValue().registrationNumberProperty());
            TableColumn<StolenVehicle, String> caseCol = new TableColumn<>("Case Number");
            caseCol.setCellValueFactory(cellData -> cellData.getValue().caseNumberProperty());
            TableColumn<StolenVehicle, String> statusStolenCol = new TableColumn<>("Status");
            statusStolenCol.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
            stolenTable.getColumns().addAll(regStolenCol, caseCol, statusStolenCol);
        }

        // Workshops Table
        if (workshopsTable != null) {
            workshopsTable.getColumns().clear();
            TableColumn<Workshop, String> workshopNameCol = new TableColumn<>("Workshop Name");
            workshopNameCol.setCellValueFactory(cellData -> cellData.getValue().workshopNameProperty());
            TableColumn<Workshop, String> workshopAddressCol = new TableColumn<>("Address");
            workshopAddressCol.setCellValueFactory(cellData -> cellData.getValue().addressProperty());
            TableColumn<Workshop, String> workshopPhoneCol = new TableColumn<>("Phone");
            workshopPhoneCol.setCellValueFactory(cellData -> cellData.getValue().phoneProperty());
            workshopsTable.getColumns().addAll(workshopNameCol, workshopAddressCol, workshopPhoneCol);
        }

        // Insurance Table
        if (insuranceTable != null) {
            insuranceTable.getColumns().clear();
            TableColumn<InsurancePolicy, String> policyCol = new TableColumn<>("Policy Number");
            policyCol.setCellValueFactory(cellData -> cellData.getValue().policyNumberProperty());
            TableColumn<InsurancePolicy, String> providerCol = new TableColumn<>("Provider");
            providerCol.setCellValueFactory(cellData -> cellData.getValue().providerNameProperty());
            TableColumn<InsurancePolicy, String> statusInsuranceCol = new TableColumn<>("Status");
            statusInsuranceCol.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
            insuranceTable.getColumns().addAll(policyCol, providerCol, statusInsuranceCol);
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
                    if (vehiclesTable != null) vehiclesTable.getItems().setAll(vehicles);
                    int vehicleIndex = findTabIndex("Vehicles");
                    if (vehicleIndex >= 0 && resultsTabPane != null) resultsTabPane.getSelectionModel().select(vehicleIndex);
                    AlertUtil.showInfo("Search Results", "Found " + vehicles.size() + " vehicles.");
                    if (statusLabel != null) statusLabel.setText("Found " + vehicles.size() + " vehicles");
                    break;

                case "Customers":
                    if (!"INSURANCE".equals(role) && !"POLICE".equals(role)) {
                        List<Customer> customers = customerDAO.searchCustomers(keyword);
                        if (customersTable != null) customersTable.getItems().setAll(customers);
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
                        if (violationsTable != null) violationsTable.getItems().setAll(violations);
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
                        if (stolenTable != null) stolenTable.getItems().setAll(stolen);
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
                    if (workshopsTable != null) workshopsTable.getItems().setAll(workshops);
                    int workshopIndex = findTabIndex("Workshops");
                    if (workshopIndex >= 0 && resultsTabPane != null) resultsTabPane.getSelectionModel().select(workshopIndex);
                    AlertUtil.showInfo("Search Results", "Found " + workshops.size() + " workshops.");
                    if (statusLabel != null) statusLabel.setText("Found " + workshops.size() + " workshops");
                    break;

                case "Insurance":
                    List<InsurancePolicy> policies = insuranceDAO.searchInsurance(keyword);
                    if (insuranceTable != null) insuranceTable.getItems().setAll(policies);
                    int insuranceIndex = findTabIndex("Insurance");
                    if (insuranceIndex >= 0 && resultsTabPane != null) resultsTabPane.getSelectionModel().select(insuranceIndex);
                    AlertUtil.showInfo("Search Results", "Found " + policies.size() + " insurance policies.");
                    if (statusLabel != null) statusLabel.setText("Found " + policies.size() + " insurance policies");
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Search Failed", "An error occurred during search.");
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
        if (vehiclesTable != null) vehiclesTable.getItems().clear();
        if (customersTable != null) customersTable.getItems().clear();
        if (violationsTable != null) violationsTable.getItems().clear();
        if (stolenTable != null) stolenTable.getItems().clear();
        if (workshopsTable != null) workshopsTable.getItems().clear();
        if (insuranceTable != null) insuranceTable.getItems().clear();
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