package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import utils.AlertUtil;
import utils.SceneManager;
import utils.SessionManager;
import dao.MechanicDAO;
import models.Mechanic;

public class MechanicController {

    @FXML private TableView<Mechanic> mechanicsTable;
    @FXML private TableColumn<Mechanic, String> nameColumn;
    @FXML private TableColumn<Mechanic, String> specializationColumn;
    @FXML private TableColumn<Mechanic, String> phoneColumn;

    @FXML private TextField nameField;
    @FXML private TextField specializationField;
    @FXML private TextField phoneField;
    @FXML private ComboBox<String> specializationComboBox;

    @FXML private Button addButton;
    @FXML private Button updateButton;
    @FXML private Button deleteButton;
    @FXML private Button clearButton;
    @FXML private Button refreshButton;
    @FXML private Button backButton;

    private MechanicDAO mechanicDAO;
    private Mechanic selectedMechanic;
    private int workshopId;

    @FXML
    public void initialize() {
        mechanicDAO = new MechanicDAO();

        workshopId = SessionManager.getInstance().getWorkshopId();

        setupTableColumns();
        loadSpecializations();
        loadMechanics();
        setupButtonHandlers();
        setupTableSelection();
    }

    private void setupTableColumns() {
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        specializationColumn.setCellValueFactory(cellData -> cellData.getValue().specializationProperty());
        phoneColumn.setCellValueFactory(cellData -> cellData.getValue().phoneProperty());
    }

    private void loadSpecializations() {
        specializationComboBox.getItems().addAll(
                "Engine Specialist", "Transmission Specialist", "Brake Specialist",
                "Electrical Specialist", "AC Specialist", "Body Repair",
                "Paint Specialist", "General Mechanic", "Diagnostic Specialist"
        );
    }

    private void loadMechanics() {
        try {
            java.util.List<Mechanic> mechanics = mechanicDAO.findByWorkshopId(workshopId);
            mechanicsTable.getItems().setAll(mechanics);
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Load Failed", "Failed to load mechanics.");
        }
    }

    private void setupButtonHandlers() {
        addButton.setOnAction(event -> handleAdd());
        updateButton.setOnAction(event -> handleUpdate());
        deleteButton.setOnAction(event -> handleDelete());
        clearButton.setOnAction(event -> handleClear());
        refreshButton.setOnAction(event -> loadMechanics());
        backButton.setOnAction(event -> SceneManager.getInstance().switchToWorkshopProfileView());
    }

    private void setupTableSelection() {
        mechanicsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedMechanic = newSelection;
                displayMechanicDetails(selectedMechanic);
            }
        });
    }

    private void displayMechanicDetails(Mechanic mechanic) {
        nameField.setText(mechanic.getName());
        specializationField.setText(mechanic.getSpecialization());
        phoneField.setText(mechanic.getPhone());
        specializationComboBox.setValue(mechanic.getSpecialization());
    }

    private void handleAdd() {
        if (!validateInputs()) {
            return;
        }

        try {
            Mechanic mechanic = new Mechanic();
            mechanic.setWorkshopId(workshopId);
            mechanic.setName(nameField.getText().trim());
            mechanic.setSpecialization(specializationField.getText().trim());
            mechanic.setPhone(phoneField.getText().trim());

            boolean success = mechanicDAO.insert(mechanic);

            if (success) {
                AlertUtil.showSuccess("Mechanic added successfully.");
                handleClear();
                loadMechanics();
            } else {
                AlertUtil.showError("Add Failed", "Failed to add mechanic.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Database Error", "An error occurred while adding mechanic.");
        }
    }

    private void handleUpdate() {
        if (selectedMechanic == null) {
            AlertUtil.showWarning("No Selection", "Please select a mechanic to update.");
            return;
        }

        if (!validateInputs()) {
            return;
        }

        try {
            selectedMechanic.setName(nameField.getText().trim());
            selectedMechanic.setSpecialization(specializationField.getText().trim());
            selectedMechanic.setPhone(phoneField.getText().trim());

            boolean success = mechanicDAO.update(selectedMechanic);

            if (success) {
                AlertUtil.showSuccess("Mechanic updated successfully.");
                loadMechanics();
            } else {
                AlertUtil.showError("Update Failed", "Failed to update mechanic.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Database Error", "An error occurred while updating mechanic.");
        }
    }

    private void handleDelete() {
        if (selectedMechanic == null) {
            AlertUtil.showWarning("No Selection", "Please select a mechanic to delete.");
            return;
        }

        boolean confirmed = AlertUtil.showConfirmation("Delete Mechanic",
                "Are you sure you want to delete " + selectedMechanic.getName() + "?");

        if (confirmed) {
            try {
                boolean success = mechanicDAO.delete(selectedMechanic.getId());

                if (success) {
                    AlertUtil.showSuccess("Mechanic deleted successfully.");
                    handleClear();
                    loadMechanics();
                } else {
                    AlertUtil.showError("Delete Failed", "Failed to delete mechanic.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                AlertUtil.showError("Database Error", "An error occurred while deleting mechanic.");
            }
        }
    }

    private void handleClear() {
        nameField.clear();
        specializationField.clear();
        phoneField.clear();
        specializationComboBox.setValue(null);
        selectedMechanic = null;
        mechanicsTable.getSelectionModel().clearSelection();
    }

    private boolean validateInputs() {
        if (!utils.ValidationUtil.isNotEmpty(nameField.getText())) {
            AlertUtil.showWarning("Validation Error", "Mechanic name is required.");
            nameField.requestFocus();
            return false;
        }

        if (!utils.ValidationUtil.isNotEmpty(phoneField.getText())) {
            AlertUtil.showWarning("Validation Error", "Phone number is required.");
            phoneField.requestFocus();
            return false;
        }

        return true;
    }
}