package controllers;

import javafx.animation.PauseTransition;
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
import dao.DigitalInspectionDAO;
import dao.InspectionChecklistItemDAO;
import dao.ServiceRecordDAO;
import dao.VehicleDAO;
import models.DigitalInspection;
import models.InspectionChecklistItem;
import models.ServiceRecord;
import models.Vehicle;

import java.util.List;

public class DigitalInspectionController {

    @FXML private ComboBox<ServiceRecord> serviceRecordComboBox;
    @FXML private TextField inspectorNameField;
    @FXML private Button startInspectionButton;

    @FXML private TableView<InspectionChecklistItem> checklistTable;
    @FXML private TableColumn<InspectionChecklistItem, String> itemNameColumn;
    @FXML private TableColumn<InspectionChecklistItem, String> statusColumn;
    @FXML private TableColumn<InspectionChecklistItem, String> notesColumn;

    @FXML private TextField newItemField;
    @FXML private ComboBox<String> newItemStatusComboBox;
    @FXML private TextArea newItemNotesArea;
    @FXML private Button addItemButton;

    @FXML private ComboBox<String> overallConditionComboBox;
    @FXML private TextArea recommendationsArea;
    @FXML private Button completeInspectionButton;

    @FXML private Label vehicleInfoLabel;
    @FXML private Label inspectionIdLabel;
    @FXML private Label statusLabel;

    @FXML private Button refreshButton;
    @FXML private Button backButton;
    @FXML private Button fadeButton;

    @FXML private ProgressIndicator loadProgress;
    @FXML private ProgressBar operationProgress;
    @FXML private Pagination checklistPagination;

    private DigitalInspectionDAO inspectionDAO;
    private InspectionChecklistItemDAO itemDAO;
    private ServiceRecordDAO serviceDAO;
    private VehicleDAO vehicleDAO;

    private DigitalInspection currentInspection;
    private ObservableList<InspectionChecklistItem> checklistItems;
    private List<InspectionChecklistItem> fullData;
    private int workshopId;
    private int currentPage = 0;
    private int pageSize = 10;

    @FXML
    public void initialize() {
        inspectionDAO = new DigitalInspectionDAO();
        itemDAO = new InspectionChecklistItemDAO();
        serviceDAO = new ServiceRecordDAO();
        vehicleDAO = new VehicleDAO();
        checklistItems = FXCollections.observableArrayList();

        workshopId = SessionManager.getInstance().getWorkshopId();

        setupTableColumns();
        loadServiceRecords();
        setupComboBoxes();
        setupButtonHandlers();
        setupPagination();
        applyVisualEffects();

        checklistTable.setVisible(false);
        addItemButton.setDisable(true);
        completeInspectionButton.setDisable(true);
        statusLabel.setText("Ready");
    }

    private void setupTableColumns() {
        itemNameColumn.setCellValueFactory(cellData -> cellData.getValue().itemNameProperty());
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        notesColumn.setCellValueFactory(cellData -> cellData.getValue().notesProperty());

        itemNameColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        statusColumn.setStyle("-fx-alignment: CENTER;");
        notesColumn.setStyle("-fx-alignment: CENTER-LEFT;");
    }

    private void setupPagination() {
        if (checklistPagination != null) {
            checklistPagination.setPageCount(1);
            checklistPagination.setMaxPageIndicatorCount(5);
            checklistPagination.currentPageIndexProperty().addListener((obs, old, newPage) -> {
                currentPage = newPage.intValue();
                updateTablePage();
            });
        }
    }

    private void updateTablePage() {
        if (fullData == null || fullData.isEmpty()) return;
        int start = currentPage * pageSize;
        int end = Math.min(start + pageSize, fullData.size());
        if (start < fullData.size()) {
            checklistItems.setAll(fullData.subList(start, end));
        }
    }

    private void loadServiceRecords() {
        showProgress(true);
        try {
            List<ServiceRecord> records = serviceDAO.findByWorkshopId(workshopId);
            serviceRecordComboBox.getItems().setAll(records);
            statusLabel.setText("Loaded " + records.size() + " service records");
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error loading service records");
        } finally {
            showProgress(false);
        }
    }

    private void setupComboBoxes() {
        newItemStatusComboBox.getItems().addAll("PASS", "FAIL", "WARNING", "NOT_CHECKED");
        newItemStatusComboBox.setValue("PASS");

        overallConditionComboBox.getItems().addAll("EXCELLENT", "GOOD", "FAIR", "POOR", "CRITICAL");
        overallConditionComboBox.setValue("GOOD");
    }

    private void setupButtonHandlers() {
        startInspectionButton.setOnAction(event -> handleStartInspection());
        addItemButton.setOnAction(event -> handleAddItem());
        completeInspectionButton.setOnAction(event -> handleCompleteInspection());
        refreshButton.setOnAction(event -> refreshInspection());
        backButton.setOnAction(event -> SceneManager.getInstance().switchToWorkshopProfileView());
        if (fadeButton != null) fadeButton.setOnAction(event -> showFadeAnimation());
    }

    private void showFadeAnimation() {
        if (fadeButton != null) {
            javafx.animation.FadeTransition fadeTransition = new javafx.animation.FadeTransition(Duration.seconds(1.5), fadeButton);
            fadeTransition.setFromValue(1.0);
            fadeTransition.setToValue(0.2);
            fadeTransition.setCycleCount(4);
            fadeTransition.setAutoReverse(true);
            fadeTransition.play();
            statusLabel.setText("Animation played!");
            PauseTransition reset = new PauseTransition(Duration.seconds(2));
            reset.setOnFinished(e -> statusLabel.setText("Ready"));
            reset.play();
        }
    }

    private void applyVisualEffects() {
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(5.0);
        dropShadow.setOffsetX(2.0);
        dropShadow.setOffsetY(2.0);
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.3));

        startInspectionButton.setEffect(dropShadow);
        addItemButton.setEffect(dropShadow);
        completeInspectionButton.setEffect(dropShadow);
        refreshButton.setEffect(dropShadow);
        backButton.setEffect(dropShadow);
    }

    private void handleStartInspection() {
        ServiceRecord selectedRecord = serviceRecordComboBox.getSelectionModel().getSelectedItem();

        if (selectedRecord == null) {
            AlertUtil.showWarning("Validation Error", "Please select a service record.");
            return;
        }

        if (!ValidationUtil.isNotEmpty(inspectorNameField.getText())) {
            AlertUtil.showWarning("Validation Error", "Inspector name is required.");
            inspectorNameField.requestFocus();
            return;
        }

        showProgress(true);
        statusLabel.setText("Starting inspection...");
        updateProgress(0.3);

        try {
            int inspectionId = inspectionDAO.startInspection(selectedRecord.getId(), inspectorNameField.getText().trim());

            if (inspectionId > 0) {
                currentInspection = inspectionDAO.findById(inspectionId);
                inspectionIdLabel.setText(String.valueOf(inspectionId));
                statusLabel.setText("In Progress");
                updateProgress(0.6);

                Vehicle vehicle = vehicleDAO.findById(selectedRecord.getVehicleId());
                if (vehicle != null) {
                    vehicleInfoLabel.setText(vehicle.getRegistrationNumber() + " - " +
                            vehicle.getMake() + " " + vehicle.getModel());
                }

                updateProgress(0.8);
                loadChecklistItems();

                checklistTable.setVisible(true);
                addItemButton.setDisable(false);
                startInspectionButton.setDisable(true);
                serviceRecordComboBox.setDisable(true);
                inspectorNameField.setDisable(true);

                updateProgress(1.0);
                AlertUtil.showSuccess("Inspection started. Inspection ID: " + inspectionId);
                statusLabel.setText("Inspection started");
            } else {
                AlertUtil.showError("Start Failed", "Failed to start inspection.");
                statusLabel.setText("Start failed");
            }
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error: " + e.getMessage());
            AlertUtil.showError("Database Error", "An error occurred.");
        } finally {
            hideProgressAfterDelay();
        }
    }

    private void loadChecklistItems() {
        if (currentInspection == null) return;

        try {
            List<InspectionChecklistItem> items = itemDAO.findByInspectionId(currentInspection.getId());
            fullData = items;
            int totalPages = (int) Math.ceil((double) items.size() / pageSize);
            if (checklistPagination != null) checklistPagination.setPageCount(Math.max(1, totalPages));
            updateTablePage();
            checklistTable.setItems(checklistItems);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleAddItem() {
        if (currentInspection == null) {
            AlertUtil.showWarning("No Inspection", "Please start an inspection first.");
            return;
        }

        if (!ValidationUtil.isNotEmpty(newItemField.getText())) {
            AlertUtil.showWarning("Validation Error", "Item name is required.");
            newItemField.requestFocus();
            return;
        }

        showProgress(true);
        statusLabel.setText("Adding inspection item...");
        updateProgress(0.3);

        try {
            InspectionChecklistItem item = new InspectionChecklistItem();
            item.setInspectionId(currentInspection.getId());
            item.setItemName(newItemField.getText().trim());
            item.setStatus(newItemStatusComboBox.getValue());
            item.setNotes(newItemNotesArea.getText().trim());

            updateProgress(0.6);
            boolean success = itemDAO.insert(item);

            if (success) {
                updateProgress(1.0);
                loadChecklistItems();
                statusLabel.setText("Item added");

                newItemField.clear();
                newItemNotesArea.clear();
                newItemStatusComboBox.setValue("PASS");

                AlertUtil.showSuccess("Inspection item added.");
            } else {
                AlertUtil.showError("Add Failed", "Failed to add inspection item.");
                statusLabel.setText("Add failed");
            }
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error: " + e.getMessage());
            AlertUtil.showError("Database Error", "An error occurred.");
        } finally {
            hideProgressAfterDelay();
        }
    }

    private void refreshInspection() {
        if (currentInspection != null) {
            loadChecklistItems();
            statusLabel.setText("Refreshed");
        }
    }

    private void handleCompleteInspection() {
        if (currentInspection == null) {
            AlertUtil.showWarning("No Inspection", "Please start an inspection first.");
            return;
        }

        if (overallConditionComboBox.getValue() == null) {
            AlertUtil.showWarning("Validation Error", "Please select overall condition.");
            return;
        }

        if (!ValidationUtil.isNotEmpty(recommendationsArea.getText())) {
            AlertUtil.showWarning("Validation Error", "Please enter recommendations.");
            return;
        }

        boolean confirmed = AlertUtil.showConfirmation("Complete Inspection",
                "Complete this inspection and save results?");

        if (confirmed) {
            showProgress(true);
            statusLabel.setText("Completing inspection...");
            updateProgress(0.3);

            try {
                updateProgress(0.6);
                boolean success = inspectionDAO.completeInspection(
                        currentInspection.getId(),
                        overallConditionComboBox.getValue(),
                        recommendationsArea.getText().trim()
                );

                if (success) {
                    updateProgress(1.0);
                    AlertUtil.showSuccess("Inspection completed successfully.");
                    statusLabel.setText("Inspection completed");
                    completeInspectionButton.setDisable(true);
                    addItemButton.setDisable(true);
                } else {
                    AlertUtil.showError("Complete Failed", "Failed to complete inspection.");
                    statusLabel.setText("Complete failed");
                }
            } catch (Exception e) {
                e.printStackTrace();
                statusLabel.setText("Error: " + e.getMessage());
                AlertUtil.showError("Database Error", "An error occurred.");
            } finally {
                hideProgressAfterDelay();
            }
        }
    }

    private void showProgress(boolean show) {
        if (loadProgress != null) loadProgress.setVisible(show);
        if (operationProgress != null) {
            operationProgress.setVisible(show);
            operationProgress.setProgress(0);
        }
    }

    private void updateProgress(double progress) {
        if (operationProgress != null) operationProgress.setProgress(progress);
    }

    private void hideProgressAfterDelay() {
        PauseTransition delay = new PauseTransition(Duration.seconds(1.5));
        delay.setOnFinished(event -> {
            if (operationProgress != null) operationProgress.setVisible(false);
            if (loadProgress != null) loadProgress.setVisible(false);
        });
        delay.play();
    }
}