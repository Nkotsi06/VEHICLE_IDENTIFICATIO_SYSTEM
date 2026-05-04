package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import utils.AlertUtil;
import utils.SceneManager;
import utils.SessionManager;
import utils.ValidationUtil;
import dao.CustomerQueryDAO;
import dao.VehicleDAO;
import models.CustomerQuery;
import models.Vehicle;

import java.util.List;

public class CustomerQueryController {

    @FXML private TableView<CustomerQuery> queriesTable;
    @FXML private TableColumn<CustomerQuery, String> vehicleColumn;
    @FXML private TableColumn<CustomerQuery, String> queryDateColumn;
    @FXML private TableColumn<CustomerQuery, String> queryTextColumn;
    @FXML private TableColumn<CustomerQuery, String> responseColumn;
    @FXML private TableColumn<CustomerQuery, String> statusColumn;

    @FXML private ComboBox<Vehicle> vehicleComboBox;
    @FXML private TextArea queryTextArea;
    @FXML private TextArea responseTextArea;
    @FXML private TextField statusField;
    @FXML private Label queryDateLabel;

    @FXML private Button submitButton;
    @FXML private Button respondButton;
    @FXML private Button closeButton;
    @FXML private Button refreshButton;
    @FXML private Button backButton;

    private CustomerQueryDAO queryDAO;
    private VehicleDAO vehicleDAO;
    private CustomerQuery selectedQuery;
    private int customerId;

    @FXML
    public void initialize() {
        queryDAO = new CustomerQueryDAO();
        vehicleDAO = new VehicleDAO();

        customerId = SessionManager.getInstance().getCustomerId();

        setupTableColumns();
        loadVehicles();
        loadQueries();
        setupButtonHandlers();
        setupTableSelection();

        responseTextArea.setEditable(SessionManager.getInstance().isAdmin() || SessionManager.getInstance().isWorkshop());
    }

    private void setupTableColumns() {
        vehicleColumn.setCellValueFactory(cellData -> cellData.getValue().registrationNumberProperty());
        queryDateColumn.setCellValueFactory(cellData -> cellData.getValue().queryDateProperty().asString());
        queryTextColumn.setCellValueFactory(cellData -> cellData.getValue().queryTextProperty());
        responseColumn.setCellValueFactory(cellData -> cellData.getValue().responseTextProperty());
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
    }

    private void loadVehicles() {
        try {
            List<Vehicle> vehicles = vehicleDAO.findByOwnerId(customerId);
            vehicleComboBox.getItems().setAll(vehicles);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadQueries() {
        try {
            List<CustomerQuery> queries;
            if (SessionManager.getInstance().isAdmin() || SessionManager.getInstance().isWorkshop()) {
                queries = queryDAO.findAll();
            } else {
                queries = queryDAO.findByCustomerId(customerId);
            }
            queriesTable.getItems().setAll(queries);
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Load Failed", "Failed to load queries.");
        }
    }

    private void setupButtonHandlers() {
        submitButton.setOnAction(event -> handleSubmit());
        respondButton.setOnAction(event -> handleRespond());
        closeButton.setOnAction(event -> handleClose());
        refreshButton.setOnAction(event -> loadQueries());
        backButton.setOnAction(event -> SceneManager.getInstance().switchToCustomerProfileView());
    }

    private void setupTableSelection() {
        queriesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedQuery = newSelection;
                displayQueryDetails(selectedQuery);
            }
        });
    }

    private void displayQueryDetails(CustomerQuery query) {
        if (query.getVehicleId() > 0) {
            try {
                Vehicle vehicle = vehicleDAO.findById(query.getVehicleId());
                vehicleComboBox.getSelectionModel().select(vehicle);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        queryTextArea.setText(query.getQueryText());
        if (query.getResponseText() != null) {
            responseTextArea.setText(query.getResponseText());
        } else {
            responseTextArea.clear();
        }
        statusField.setText(query.getStatus());

        if (query.getQueryDate() != null) {
            queryDateLabel.setText(query.getQueryDate().toString());
        }
    }

    private void handleSubmit() {
        Vehicle selectedVehicle = vehicleComboBox.getSelectionModel().getSelectedItem();

        if (selectedVehicle == null) {
            AlertUtil.showWarning("Validation Error", "Please select a vehicle.");
            return;
        }

        if (!ValidationUtil.isNotEmpty(queryTextArea.getText())) {
            AlertUtil.showWarning("Validation Error", "Please enter your query.");
            queryTextArea.requestFocus();
            return;
        }

        try {
            CustomerQuery query = new CustomerQuery();
            query.setCustomerId(customerId);
            query.setVehicleId(selectedVehicle.getId());
            query.setQueryText(queryTextArea.getText().trim());

            boolean success = queryDAO.insert(query);

            if (success) {
                AlertUtil.showSuccess("Query submitted successfully.");
                clearForm();
                loadQueries();
            } else {
                AlertUtil.showError("Submit Failed", "Failed to submit query.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Database Error", "An error occurred while submitting query.");
        }
    }

    private void handleRespond() {
        if (selectedQuery == null) {
            AlertUtil.showWarning("No Selection", "Please select a query to respond to.");
            return;
        }

        if (!ValidationUtil.isNotEmpty(responseTextArea.getText())) {
            AlertUtil.showWarning("Validation Error", "Please enter a response.");
            responseTextArea.requestFocus();
            return;
        }

        try {
            boolean success = queryDAO.respondToQuery(selectedQuery.getId(), responseTextArea.getText().trim());

            if (success) {
                AlertUtil.showSuccess("Response sent successfully.");
                loadQueries();
            } else {
                AlertUtil.showError("Response Failed", "Failed to send response.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Database Error", "An error occurred while sending response.");
        }
    }

    private void handleClose() {
        if (selectedQuery == null) {
            AlertUtil.showWarning("No Selection", "Please select a query to close.");
            return;
        }

        if ("CLOSED".equals(selectedQuery.getStatus())) {
            AlertUtil.showWarning("Already Closed", "This query is already closed.");
            return;
        }

        boolean confirmed = AlertUtil.showConfirmation("Close Query", "Are you sure you want to close this query?");

        if (confirmed) {
            try {
                boolean success = queryDAO.closeQuery(selectedQuery.getId());

                if (success) {
                    AlertUtil.showSuccess("Query closed successfully.");
                    loadQueries();
                } else {
                    AlertUtil.showError("Close Failed", "Failed to close query.");
                }

            } catch (Exception e) {
                e.printStackTrace();
                AlertUtil.showError("Database Error", "An error occurred while closing query.");
            }
        }
    }

    private void clearForm() {
        vehicleComboBox.getSelectionModel().clearSelection();
        queryTextArea.clear();
        responseTextArea.clear();
        statusField.clear();
        queryDateLabel.setText("");
        selectedQuery = null;
        queriesTable.getSelectionModel().clearSelection();
    }
}