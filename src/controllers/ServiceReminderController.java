package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import utils.AlertUtil;
import utils.SceneManager;
import utils.SessionManager;
import utils.DateUtil;
import dao.ServiceScheduleDAO;
import dao.VehicleDAO;
import models.ServiceSchedule;
import models.Vehicle;

import java.time.LocalDate;
import java.util.List;

public class ServiceReminderController {

    @FXML private TableView<ServiceSchedule> remindersTable;
    @FXML private TableColumn<ServiceSchedule, String> vehicleColumn;
    @FXML private TableColumn<ServiceSchedule, String> serviceTypeColumn;
    @FXML private TableColumn<ServiceSchedule, String> dueDateColumn;
    @FXML private TableColumn<ServiceSchedule, String> statusColumn;

    @FXML private ComboBox<Vehicle> vehicleComboBox;
    @FXML private TextField serviceTypeField;
    @FXML private DatePicker dueDatePicker;
    @FXML private TextField dueOdometerField;

    @FXML private Button addButton;
    @FXML private Button sendRemindersButton;
    @FXML private Button refreshButton;
    @FXML private Button backButton;
    @FXML private Button deleteButton;

    @FXML private ProgressIndicator loadProgress;
    @FXML private Label statusLabel;

    private ServiceScheduleDAO scheduleDAO;
    private VehicleDAO vehicleDAO;
    private int customerId;

    @FXML
    public void initialize() {
        scheduleDAO = new ServiceScheduleDAO();
        vehicleDAO = new VehicleDAO();

        customerId = SessionManager.getInstance().getCustomerId();

        setupTableColumns();
        loadVehicles();
        loadReminders();
        setupButtonHandlers();

        dueDatePicker.setValue(LocalDate.now().plusMonths(6));
        statusLabel.setText("Ready");
    }

    private void setupTableColumns() {
        vehicleColumn.setCellValueFactory(cellData -> cellData.getValue().registrationNumberProperty());
        serviceTypeColumn.setCellValueFactory(cellData -> cellData.getValue().serviceTypeProperty());
        dueDateColumn.setCellValueFactory(cellData -> cellData.getValue().dueDateProperty().asString());
        statusColumn.setCellValueFactory(cellData -> {
            String status = cellData.getValue().isOverdue() ? "OVERDUE" :
                    (cellData.getValue().isDueSoon() ? "DUE SOON" : "FUTURE");
            return new javafx.beans.property.SimpleStringProperty(status);
        });
    }

    private void loadVehicles() {
        showProgress(true);
        try {
            List<Vehicle> vehicles = vehicleDAO.findByOwnerId(customerId);
            vehicleComboBox.getItems().setAll(vehicles);
            statusLabel.setText("Loaded " + vehicles.size() + " vehicles");
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error loading vehicles");
        } finally {
            showProgress(false);
        }
    }

    private void loadReminders() {
        showProgress(true);
        try {
            List<ServiceSchedule> reminders = new java.util.ArrayList<>();
            List<Vehicle> vehicles = vehicleDAO.findByOwnerId(customerId);

            for (Vehicle vehicle : vehicles) {
                reminders.addAll(scheduleDAO.findByVehicleId(vehicle.getId()));
            }

            remindersTable.getItems().setAll(reminders);
            statusLabel.setText("Loaded " + reminders.size() + " reminders");
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error loading reminders");
            AlertUtil.showError("Load Failed", "Failed to load service reminders.");
        } finally {
            showProgress(false);
        }
    }

    private void setupButtonHandlers() {
        addButton.setOnAction(event -> handleAdd());
        sendRemindersButton.setOnAction(event -> handleSendReminders());
        refreshButton.setOnAction(event -> loadReminders());
        backButton.setOnAction(event -> SceneManager.getInstance().switchToCustomerProfileView());
        if (deleteButton != null) deleteButton.setOnAction(event -> handleDelete());
    }

    private void handleAdd() {
        Vehicle selectedVehicle = vehicleComboBox.getSelectionModel().getSelectedItem();

        if (selectedVehicle == null) {
            AlertUtil.showWarning("Validation Error", "Please select a vehicle.");
            return;
        }

        if (!utils.ValidationUtil.isNotEmpty(serviceTypeField.getText())) {
            AlertUtil.showWarning("Validation Error", "Service type is required.");
            return;
        }

        if (dueDatePicker.getValue() == null) {
            AlertUtil.showWarning("Validation Error", "Please select a due date.");
            return;
        }

        showProgress(true);
        statusLabel.setText("Adding reminder...");

        try {
            ServiceSchedule schedule = new ServiceSchedule();
            schedule.setVehicleId(selectedVehicle.getId());
            schedule.setServiceType(serviceTypeField.getText().trim());
            schedule.setDueDate(dueDatePicker.getValue());

            if (utils.ValidationUtil.isNotEmpty(dueOdometerField.getText())) {
                schedule.setDueOdometer(Integer.parseInt(dueOdometerField.getText()));
            }

            boolean success = scheduleDAO.insert(schedule);

            if (success) {
                AlertUtil.showSuccess("Service reminder added.");
                clearForm();
                loadReminders();
                statusLabel.setText("Reminder added successfully");
            } else {
                AlertUtil.showError("Add Failed", "Failed to add reminder.");
                statusLabel.setText("Failed to add reminder");
            }

        } catch (NumberFormatException e) {
            AlertUtil.showError("Invalid Input", "Please enter a valid odometer value.");
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error: " + e.getMessage());
            AlertUtil.showError("Database Error", "An error occurred.");
        } finally {
            showProgress(false);
        }
    }

    private void handleSendReminders() {
        boolean confirmed = AlertUtil.showConfirmation("Send Reminders",
                "Send service reminders to all customers?");

        if (confirmed) {
            showProgress(true);
            statusLabel.setText("Sending reminders...");

            try {
                scheduleDAO.sendReminders();
                AlertUtil.showSuccess("Service reminders sent successfully.");
                loadReminders();
                statusLabel.setText("Reminders sent successfully");
            } catch (Exception e) {
                e.printStackTrace();
                statusLabel.setText("Error: " + e.getMessage());
                AlertUtil.showError("Send Failed", "Failed to send reminders.");
            } finally {
                showProgress(false);
            }
        }
    }

    private void handleDelete() {
        ServiceSchedule selected = remindersTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            AlertUtil.showWarning("No Selection", "Please select a reminder to delete.");
            return;
        }

        boolean confirmed = AlertUtil.showConfirmation("Delete Reminder",
                "Delete service reminder for " + selected.getRegistrationNumber() + "?");

        if (confirmed) {
            showProgress(true);
            statusLabel.setText("Deleting reminder...");

            try {
                boolean success = scheduleDAO.delete(selected.getId());
                if (success) {
                    AlertUtil.showSuccess("Reminder deleted successfully.");
                    loadReminders();
                    statusLabel.setText("Reminder deleted");
                } else {
                    AlertUtil.showError("Delete Failed", "Failed to delete reminder.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                statusLabel.setText("Error: " + e.getMessage());
                AlertUtil.showError("Database Error", "An error occurred.");
            } finally {
                showProgress(false);
            }
        }
    }

    private void clearForm() {
        vehicleComboBox.getSelectionModel().clearSelection();
        serviceTypeField.clear();
        dueDatePicker.setValue(LocalDate.now().plusMonths(6));
        dueOdometerField.clear();
    }

    private void showProgress(boolean show) {
        if (loadProgress != null) loadProgress.setVisible(show);
    }
}