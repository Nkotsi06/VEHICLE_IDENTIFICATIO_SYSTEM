package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import utils.AlertUtil;
import utils.SceneManager;
import utils.SessionManager;
import dao.PartInventoryDAO;
import dao.InventoryAlertDAO;
import models.PartInventory;
import models.InventoryAlert;

public class PartInventoryController {

    @FXML private TableView<PartInventory> inventoryTable;
    @FXML private TableColumn<PartInventory, String> partNameColumn;
    @FXML private TableColumn<PartInventory, String> partNumberColumn;
    @FXML private TableColumn<PartInventory, Integer> quantityColumn;
    @FXML private TableColumn<PartInventory, Integer> reorderLevelColumn;
    @FXML private TableColumn<PartInventory, Double> unitPriceColumn;
    @FXML private TableColumn<PartInventory, String> stockStatusColumn;

    @FXML private TextField partNameField;
    @FXML private TextField partNumberField;
    @FXML private TextField quantityField;
    @FXML private TextField reorderLevelField;
    @FXML private TextField unitPriceField;

    @FXML private Label totalValueLabel;
    @FXML private Label lowStockCountLabel;
    @FXML private Label outOfStockCountLabel;

    @FXML private Button addButton;
    @FXML private Button updateButton;
    @FXML private Button deleteButton;
    @FXML private Button refreshButton;
    @FXML private Button viewAlertsButton;
    @FXML private Button backButton;

    private PartInventoryDAO inventoryDAO;
    private InventoryAlertDAO alertDAO;
    private PartInventory selectedPart;
    private int workshopId;

    @FXML
    public void initialize() {
        inventoryDAO = new PartInventoryDAO();
        alertDAO = new InventoryAlertDAO();
        workshopId = SessionManager.getInstance().getWorkshopId();

        setupTableColumns();
        loadInventory();
        setupButtonHandlers();
        setupTableSelection();
        loadStats();
    }

    private void setupTableColumns() {
        partNameColumn.setCellValueFactory(cellData -> cellData.getValue().partNameProperty());
        partNumberColumn.setCellValueFactory(cellData -> cellData.getValue().partNumberProperty());
        quantityColumn.setCellValueFactory(cellData -> cellData.getValue().quantityProperty().asObject());
        reorderLevelColumn.setCellValueFactory(cellData -> cellData.getValue().reorderLevelProperty().asObject());
        unitPriceColumn.setCellValueFactory(cellData -> cellData.getValue().unitPriceProperty().asObject());
        stockStatusColumn.setCellValueFactory(cellData -> cellData.getValue().stockStatusProperty());
    }

    private void loadInventory() {
        try {
            java.util.List<PartInventory> parts = inventoryDAO.findByWorkshopId(workshopId);
            inventoryTable.getItems().setAll(parts);
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Load Failed", "Failed to load inventory.");
        }
    }

    private void loadStats() {
        try {
            java.util.List<PartInventory> parts = inventoryDAO.findByWorkshopId(workshopId);

            double totalValue = 0;
            int lowStock = 0;
            int outOfStock = 0;

            for (PartInventory part : parts) {
                totalValue += part.getQuantity() * part.getUnitPrice();
                if (part.isOutOfStock()) outOfStock++;
                else if (part.isLowStock()) lowStock++;
            }

            totalValueLabel.setText(utils.CurrencyUtil.format(totalValue));
            lowStockCountLabel.setText(String.valueOf(lowStock));
            outOfStockCountLabel.setText(String.valueOf(outOfStock));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupButtonHandlers() {
        addButton.setOnAction(event -> handleAdd());
        updateButton.setOnAction(event -> handleUpdate());
        deleteButton.setOnAction(event -> handleDelete());
        refreshButton.setOnAction(event -> {
            loadInventory();
            loadStats();
        });
        viewAlertsButton.setOnAction(event -> handleViewAlerts());
        backButton.setOnAction(event -> SceneManager.getInstance().switchToWorkshopProfileView());
    }

    private void setupTableSelection() {
        inventoryTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedPart = newSelection;
                displayPartDetails(selectedPart);
            }
        });
    }

    private void displayPartDetails(PartInventory part) {
        partNameField.setText(part.getPartName());
        partNumberField.setText(part.getPartNumber());
        quantityField.setText(String.valueOf(part.getQuantity()));
        reorderLevelField.setText(String.valueOf(part.getReorderLevel()));
        unitPriceField.setText(String.valueOf(part.getUnitPrice()));
    }

    private void handleAdd() {
        if (!validateInputs()) {
            return;
        }

        try {
            PartInventory part = new PartInventory();
            part.setWorkshopId(workshopId);
            part.setPartName(partNameField.getText().trim());
            part.setPartNumber(partNumberField.getText().trim());
            part.setQuantity(Integer.parseInt(quantityField.getText()));
            part.setReorderLevel(Integer.parseInt(reorderLevelField.getText()));
            part.setUnitPrice(Double.parseDouble(unitPriceField.getText()));

            boolean success = inventoryDAO.insert(part);

            if (success) {
                AlertUtil.showSuccess("Part added to inventory.");
                clearForm();
                loadInventory();
                loadStats();
            } else {
                AlertUtil.showError("Add Failed", "Failed to add part.");
            }

        } catch (NumberFormatException e) {
            AlertUtil.showError("Invalid Input", "Please enter valid numbers.");
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Database Error", "An error occurred.");
        }
    }

    private void handleUpdate() {
        if (selectedPart == null) {
            AlertUtil.showWarning("No Selection", "Please select a part to update.");
            return;
        }

        if (!validateInputs()) {
            return;
        }

        try {
            selectedPart.setPartName(partNameField.getText().trim());
            selectedPart.setPartNumber(partNumberField.getText().trim());
            selectedPart.setQuantity(Integer.parseInt(quantityField.getText()));
            selectedPart.setReorderLevel(Integer.parseInt(reorderLevelField.getText()));
            selectedPart.setUnitPrice(Double.parseDouble(unitPriceField.getText()));

            boolean success = inventoryDAO.update(selectedPart);

            if (success) {
                AlertUtil.showSuccess("Part updated successfully.");
                loadInventory();
                loadStats();
            } else {
                AlertUtil.showError("Update Failed", "Failed to update part.");
            }

        } catch (NumberFormatException e) {
            AlertUtil.showError("Invalid Input", "Please enter valid numbers.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleDelete() {
        if (selectedPart == null) {
            AlertUtil.showWarning("No Selection", "Please select a part to delete.");
            return;
        }

        boolean confirmed = AlertUtil.showConfirmation("Delete Part",
                "Delete " + selectedPart.getPartName() + " from inventory?");

        if (confirmed) {
            try {
                boolean success = inventoryDAO.delete(selectedPart.getId());

                if (success) {
                    AlertUtil.showSuccess("Part deleted successfully.");
                    clearForm();
                    loadInventory();
                    loadStats();
                } else {
                    AlertUtil.showError("Delete Failed", "Failed to delete part.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void handleViewAlerts() {
        try {
            java.util.List<InventoryAlert> alerts = alertDAO.findByWorkshopId(workshopId);
            if (alerts.isEmpty()) {
                AlertUtil.showInfo("No Alerts", "No inventory alerts at this time.");
            } else {
                StringBuilder message = new StringBuilder("Inventory Alerts:\n\n");
                for (InventoryAlert alert : alerts) {
                    message.append("- ").append(alert.getMessage()).append("\n");
                }
                AlertUtil.showWarning("Inventory Alerts", message.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearForm() {
        partNameField.clear();
        partNumberField.clear();
        quantityField.clear();
        reorderLevelField.clear();
        unitPriceField.clear();
        selectedPart = null;
        inventoryTable.getSelectionModel().clearSelection();
    }

    private boolean validateInputs() {
        if (!utils.ValidationUtil.isNotEmpty(partNameField.getText())) {
            AlertUtil.showWarning("Validation Error", "Part name is required.");
            return false;
        }

        if (!utils.ValidationUtil.isNotEmpty(partNumberField.getText())) {
            AlertUtil.showWarning("Validation Error", "Part number is required.");
            return false;
        }

        if (!utils.ValidationUtil.isNotEmpty(quantityField.getText())) {
            AlertUtil.showWarning("Validation Error", "Quantity is required.");
            return false;
        }

        try {
            int qty = Integer.parseInt(quantityField.getText());
            if (qty < 0) {
                AlertUtil.showWarning("Validation Error", "Quantity cannot be negative.");
                return false;
            }
        } catch (NumberFormatException e) {
            AlertUtil.showWarning("Validation Error", "Please enter a valid quantity.");
            return false;
        }

        return true;
    }
}