package controllers;

import javafx.animation.FadeTransition;
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
import utils.CurrencyUtil;
import dao.WorkshopDAO;
import dao.MechanicDAO;
import dao.ServiceRecordDAO;
import dao.PartInventoryDAO;
import dao.DigitalInspectionDAO;
import dao.NotificationDAO;
import models.ServiceRecord;
import models.PartInventory;
import models.Notification;

import java.time.LocalDate;
import java.util.List;

public class WorkshopController {

    @FXML private Label welcomeLabel;
    @FXML private Label statusLabel;

    // Statistics Cards Labels
    @FXML private Label totalServicesLabel;
    @FXML private Label totalRevenueLabel;
    @FXML private Label activeMechanicsLabel;
    @FXML private Label lowStockLabel;
    @FXML private Label monthlyVehiclesLabel;
    @FXML private Label avgServiceCostLabel;
    @FXML private Label approvalStatusLabel;
    @FXML private Label completedInspectionsLabel;
    @FXML private Label unreadNotificationsLabel;

    // Tables
    @FXML private TableView<ServiceRecord> recentServicesTable;
    @FXML private TableView<PartInventory> lowStockTable;

    // Progress Indicators
    @FXML private ProgressIndicator loadProgress;
    @FXML private ProgressBar operationProgress;
    @FXML private Pagination servicesPagination;
    @FXML private Pagination stockPagination;

    private WorkshopDAO workshopDAO;
    private MechanicDAO mechanicDAO;
    private ServiceRecordDAO serviceDAO;
    private PartInventoryDAO inventoryDAO;
    private DigitalInspectionDAO inspectionDAO;
    private NotificationDAO notificationDAO;
    private int workshopId;
    private ObservableList<ServiceRecord> serviceList;
    private ObservableList<PartInventory> stockList;
    private List<ServiceRecord> fullServiceData;
    private List<PartInventory> fullStockData;
    private int currentServicePage = 0;
    private int currentStockPage = 0;
    private int pageSize = 10;

    @FXML
    public void initialize() {
        workshopDAO = new WorkshopDAO();
        mechanicDAO = new MechanicDAO();
        serviceDAO = new ServiceRecordDAO();
        inventoryDAO = new PartInventoryDAO();
        inspectionDAO = new DigitalInspectionDAO();
        notificationDAO = new NotificationDAO();
        serviceList = FXCollections.observableArrayList();
        stockList = FXCollections.observableArrayList();

        workshopId = SessionManager.getInstance().getWorkshopId();

        setupTableColumns();
        setupPagination();
        setupButtonHandlers();
        applyVisualEffects();
        loadDashboardData();

        String workshopName = SessionManager.getInstance().getFullName();
        welcomeLabel.setText("Welcome, " + (workshopName != null ? workshopName : "Workshop"));
        statusLabel.setText("Ready");

        // Start auto-refresh every 60 seconds
        startAutoRefresh();
    }

    private void setupTableColumns() {
        // Recent Services Table
        TableColumn<ServiceRecord, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cellData -> cellData.getValue().serviceDateProperty().asString());
        dateCol.setPrefWidth(100);
        dateCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<ServiceRecord, String> vehicleCol = new TableColumn<>("Vehicle");
        vehicleCol.setCellValueFactory(cellData -> cellData.getValue().registrationNumberProperty());
        vehicleCol.setPrefWidth(150);
        vehicleCol.setStyle("-fx-alignment: CENTER-LEFT;");

        TableColumn<ServiceRecord, String> typeCol = new TableColumn<>("Service Type");
        typeCol.setCellValueFactory(cellData -> cellData.getValue().serviceTypeProperty());
        typeCol.setPrefWidth(200);
        typeCol.setStyle("-fx-alignment: CENTER-LEFT;");

        TableColumn<ServiceRecord, Double> costCol = new TableColumn<>("Cost");
        costCol.setCellValueFactory(cellData -> cellData.getValue().costProperty().asObject());
        costCol.setPrefWidth(100);
        costCol.setStyle("-fx-alignment: CENTER-RIGHT;");

        TableColumn<ServiceRecord, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        statusCol.setPrefWidth(100);
        statusCol.setStyle("-fx-alignment: CENTER;");

        recentServicesTable.getColumns().addAll(dateCol, vehicleCol, typeCol, costCol, statusCol);

        // Low Stock Table
        TableColumn<PartInventory, String> partNameCol = new TableColumn<>("Part Name");
        partNameCol.setCellValueFactory(cellData -> cellData.getValue().partNameProperty());
        partNameCol.setPrefWidth(200);
        partNameCol.setStyle("-fx-alignment: CENTER-LEFT;");

        TableColumn<PartInventory, String> partNumberCol = new TableColumn<>("Part Number");
        partNumberCol.setCellValueFactory(cellData -> cellData.getValue().partNumberProperty());
        partNumberCol.setPrefWidth(150);
        partNumberCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<PartInventory, Integer> quantityCol = new TableColumn<>("Current Stock");
        quantityCol.setCellValueFactory(cellData -> cellData.getValue().quantityProperty().asObject());
        quantityCol.setPrefWidth(100);
        quantityCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<PartInventory, Integer> reorderCol = new TableColumn<>("Reorder Level");
        reorderCol.setCellValueFactory(cellData -> cellData.getValue().reorderLevelProperty().asObject());
        reorderCol.setPrefWidth(100);
        reorderCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<PartInventory, String> stockStatusCol = new TableColumn<>("Status");
        stockStatusCol.setCellValueFactory(cellData -> cellData.getValue().stockStatusProperty());
        stockStatusCol.setPrefWidth(100);
        stockStatusCol.setStyle("-fx-alignment: CENTER;");

        lowStockTable.getColumns().addAll(partNameCol, partNumberCol, quantityCol, reorderCol, stockStatusCol);
    }

    private void setupPagination() {
        if (servicesPagination != null) {
            servicesPagination.setPageCount(1);
            servicesPagination.setMaxPageIndicatorCount(5);
            servicesPagination.currentPageIndexProperty().addListener((obs, old, newPage) -> {
                currentServicePage = newPage.intValue();
                updateServiceTablePage();
            });
        }

        if (stockPagination != null) {
            stockPagination.setPageCount(1);
            stockPagination.setMaxPageIndicatorCount(5);
            stockPagination.currentPageIndexProperty().addListener((obs, old, newPage) -> {
                currentStockPage = newPage.intValue();
                updateStockTablePage();
            });
        }
    }

    private void updateServiceTablePage() {
        if (fullServiceData == null || fullServiceData.isEmpty()) return;
        int start = currentServicePage * pageSize;
        int end = Math.min(start + pageSize, fullServiceData.size());
        if (start < fullServiceData.size()) {
            serviceList.setAll(fullServiceData.subList(start, end));
            recentServicesTable.setItems(serviceList);
        }
    }

    private void updateStockTablePage() {
        if (fullStockData == null || fullStockData.isEmpty()) return;
        int start = currentStockPage * pageSize;
        int end = Math.min(start + pageSize, fullStockData.size());
        if (start < fullStockData.size()) {
            stockList.setAll(fullStockData.subList(start, end));
            lowStockTable.setItems(stockList);
        }
    }

    private void setupButtonHandlers() {
        recentServicesTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                ServiceRecord selected = recentServicesTable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    SceneManager.getInstance().switchToServiceRecordView();
                }
            }
        });

        lowStockTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                SceneManager.getInstance().switchToPartInventoryView();
            }
        });
    }

    private void applyVisualEffects() {
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(5.0);
        dropShadow.setOffsetX(2.0);
        dropShadow.setOffsetY(2.0);
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.3));

        recentServicesTable.setEffect(dropShadow);
        lowStockTable.setEffect(dropShadow);
    }

    private void startAutoRefresh() {
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(60),
                        e -> {
                            loadStatistics();
                            loadRecentServices();
                            loadLowStockItems();
                        })
        );
        timeline.setCycleCount(javafx.animation.Timeline.INDEFINITE);
        timeline.play();
    }

    private void loadDashboardData() {
        showProgress(true);
        statusLabel.setText("Loading dashboard data...");

        try {
            loadStatistics();
            loadRecentServices();
            loadLowStockItems();
            loadUnreadNotifications();
            statusLabel.setText("Dashboard loaded successfully");
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error loading dashboard: " + e.getMessage());
            AlertUtil.showError("Load Error", "Failed to load dashboard data.");
        } finally {
            hideProgressAfterDelay();
        }
    }

    private void loadStatistics() {
        try {
            if (workshopId <= 0) {
                setDefaultStats();
                return;
            }

            int totalServices = serviceDAO.countByWorkshopId(workshopId);
            totalServicesLabel.setText(String.valueOf(totalServices));

            double totalRevenue = serviceDAO.sumRevenueByWorkshopId(workshopId);
            totalRevenueLabel.setText(CurrencyUtil.format(totalRevenue));

            int activeMechanics = mechanicDAO.countByWorkshopId(workshopId);
            activeMechanicsLabel.setText(String.valueOf(activeMechanics));

            int lowStockCount = inventoryDAO.countLowStockByWorkshopId(workshopId);
            lowStockLabel.setText(String.valueOf(lowStockCount));
            if (lowStockCount > 0) {
                lowStockLabel.setStyle("-fx-text-fill: #e74c3c;");
            }

            int monthlyVehicles = serviceDAO.countUniqueVehiclesByWorkshopIdAndMonth(workshopId, LocalDate.now());
            monthlyVehiclesLabel.setText(String.valueOf(monthlyVehicles));

            double avgCost = serviceDAO.averageCostByWorkshopId(workshopId);
            avgServiceCostLabel.setText(CurrencyUtil.format(avgCost));

            var workshop = workshopDAO.findById(workshopId);
            if (workshop != null) {
                boolean isApproved = workshop.isApproved();
                approvalStatusLabel.setText(isApproved ? "APPROVED" : "PENDING");
                approvalStatusLabel.setStyle(isApproved ?
                        "-fx-text-fill: #2ecc71; -fx-font-size: 28px; -fx-font-weight: bold;" :
                        "-fx-text-fill: #e74c3c; -fx-font-size: 28px; -fx-font-weight: bold;");
            }

            int completedInspections = inspectionDAO.countCompletedByWorkshopId(workshopId);
            completedInspectionsLabel.setText(String.valueOf(completedInspections));

        } catch (Exception e) {
            e.printStackTrace();
            setDefaultStats();
        }
    }

    private void loadUnreadNotifications() {
        try {
            int userId = SessionManager.getInstance().getUserId();
            int unreadCount = notificationDAO.countUnreadByUserId(userId);
            unreadNotificationsLabel.setText(String.valueOf(unreadCount));
            if (unreadCount > 0) {
                unreadNotificationsLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            } else {
                unreadNotificationsLabel.setStyle("-fx-text-fill: white;");
            }
        } catch (Exception e) {
            e.printStackTrace();
            unreadNotificationsLabel.setText("0");
        }
    }

    private void setDefaultStats() {
        totalServicesLabel.setText("0");
        totalRevenueLabel.setText("M0.00");
        activeMechanicsLabel.setText("0");
        lowStockLabel.setText("0");
        monthlyVehiclesLabel.setText("0");
        avgServiceCostLabel.setText("M0.00");
        approvalStatusLabel.setText("UNKNOWN");
        completedInspectionsLabel.setText("0");
    }

    private void loadRecentServices() {
        try {
            fullServiceData = serviceDAO.findByWorkshopId(workshopId);
            int totalPages = (int) Math.ceil((double) fullServiceData.size() / pageSize);
            if (servicesPagination != null) servicesPagination.setPageCount(Math.max(1, totalPages));
            updateServiceTablePage();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadLowStockItems() {
        try {
            fullStockData = inventoryDAO.findLowStockByWorkshopId(workshopId);
            int totalPages = (int) Math.ceil((double) fullStockData.size() / pageSize);
            if (stockPagination != null) stockPagination.setPageCount(Math.max(1, totalPages));
            updateStockTablePage();
        } catch (Exception e) {
            e.printStackTrace();
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