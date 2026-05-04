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
import dao.VehicleDAO;
import dao.CustomerQueryDAO;
import dao.CustomerComplaintDAO;
import dao.CustomerReviewDAO;
import dao.NotificationDAO;
import dao.DigitalWalletDAO;
import dao.InsurancePolicyDAO;
import dao.ServiceScheduleDAO;
import models.Vehicle;
import models.Notification;

import java.util.List;

public class CustomerController {

    @FXML private Label welcomeLabel;
    @FXML private Label statusLabel;

    // Statistics Cards Labels
    @FXML private Label vehiclesCountLabel;
    @FXML private Label activeQueriesLabel;
    @FXML private Label pendingComplaintsLabel;
    @FXML private Label walletBalanceLabel;
    @FXML private Label unreadNotificationsLabel;
    @FXML private Label reviewsCountLabel;
    @FXML private Label expiredInsuranceLabel;
    @FXML private Label servicesDueLabel;

    // Tables
    @FXML private TableView<Vehicle> recentVehiclesTable;
    @FXML private TableView<Notification> recentNotificationsTable;

    // Progress Indicators
    @FXML private ProgressIndicator loadProgress;
    @FXML private ProgressBar operationProgress;
    @FXML private Pagination vehiclesPagination;
    @FXML private Pagination notificationsPagination;

    private VehicleDAO vehicleDAO;
    private CustomerQueryDAO queryDAO;
    private CustomerComplaintDAO complaintDAO;
    private CustomerReviewDAO reviewDAO;
    private NotificationDAO notificationDAO;
    private DigitalWalletDAO walletDAO;
    private InsurancePolicyDAO policyDAO;
    private ServiceScheduleDAO scheduleDAO;

    private int customerId;
    private int userId;
    private ObservableList<Vehicle> vehicleList;
    private ObservableList<Notification> notificationList;
    private List<Vehicle> fullVehicleData;
    private List<Notification> fullNotificationData;
    private int currentVehiclePage = 0;
    private int currentNotificationPage = 0;
    private int pageSize = 5;

    @FXML
    public void initialize() {
        vehicleDAO = new VehicleDAO();
        queryDAO = new CustomerQueryDAO();
        complaintDAO = new CustomerComplaintDAO();
        reviewDAO = new CustomerReviewDAO();
        notificationDAO = new NotificationDAO();
        walletDAO = new DigitalWalletDAO();
        policyDAO = new InsurancePolicyDAO();
        scheduleDAO = new ServiceScheduleDAO();

        vehicleList = FXCollections.observableArrayList();
        notificationList = FXCollections.observableArrayList();

        customerId = SessionManager.getInstance().getCustomerId();
        userId = SessionManager.getInstance().getUserId();

        setupTableColumns();
        setupPagination();
        setupButtonHandlers();
        applyVisualEffects();
        loadDashboardData();

        String fullName = SessionManager.getInstance().getFullName();
        welcomeLabel.setText("Welcome, " + (fullName != null ? fullName : "Customer"));
        statusLabel.setText("Ready");

        startAutoRefresh();
    }

    private void setupTableColumns() {
        recentVehiclesTable.getColumns().clear();
        recentNotificationsTable.getColumns().clear();

        TableColumn<Vehicle, String> regCol = new TableColumn<>("Registration");
        regCol.setCellValueFactory(cellData -> cellData.getValue().registrationNumberProperty());
        regCol.setPrefWidth(120);
        regCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<Vehicle, String> makeCol = new TableColumn<>("Make");
        makeCol.setCellValueFactory(cellData -> cellData.getValue().makeProperty());
        makeCol.setPrefWidth(100);
        makeCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<Vehicle, String> modelCol = new TableColumn<>("Model");
        modelCol.setCellValueFactory(cellData -> cellData.getValue().modelProperty());
        modelCol.setPrefWidth(100);
        modelCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<Vehicle, Integer> yearCol = new TableColumn<>("Year");
        yearCol.setCellValueFactory(cellData -> cellData.getValue().yearProperty().asObject());
        yearCol.setPrefWidth(80);
        yearCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<Vehicle, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cellData -> cellData.getValue().statusNameProperty());
        statusCol.setPrefWidth(100);
        statusCol.setStyle("-fx-alignment: CENTER;");

        recentVehiclesTable.getColumns().addAll(regCol, makeCol, modelCol, yearCol, statusCol);

        TableColumn<Notification, String> msgCol = new TableColumn<>("Message");
        msgCol.setCellValueFactory(cellData -> cellData.getValue().messageProperty());
        msgCol.setPrefWidth(350);
        msgCol.setStyle("-fx-alignment: CENTER-LEFT;");

        TableColumn<Notification, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(cellData -> cellData.getValue().typeProperty());
        typeCol.setPrefWidth(100);
        typeCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<Notification, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cellData -> cellData.getValue().createdAtProperty().asString());
        dateCol.setPrefWidth(150);
        dateCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<Notification, String> readCol = new TableColumn<>("Status");
        readCol.setCellValueFactory(cellData -> cellData.getValue().readProperty());
        readCol.setPrefWidth(80);
        readCol.setStyle("-fx-alignment: CENTER;");

        recentNotificationsTable.getColumns().addAll(msgCol, typeCol, dateCol, readCol);
    }

    private void setupPagination() {
        if (vehiclesPagination != null) {
            vehiclesPagination.setPageCount(1);
            vehiclesPagination.setMaxPageIndicatorCount(5);
            vehiclesPagination.currentPageIndexProperty().addListener((obs, old, newPage) -> {
                currentVehiclePage = newPage.intValue();
                updateVehicleTablePage();
            });
        }

        if (notificationsPagination != null) {
            notificationsPagination.setPageCount(1);
            notificationsPagination.setMaxPageIndicatorCount(5);
            notificationsPagination.currentPageIndexProperty().addListener((obs, old, newPage) -> {
                currentNotificationPage = newPage.intValue();
                updateNotificationTablePage();
            });
        }
    }

    private void updateVehicleTablePage() {
        if (fullVehicleData == null || fullVehicleData.isEmpty()) return;
        int start = currentVehiclePage * pageSize;
        int end = Math.min(start + pageSize, fullVehicleData.size());
        if (start < fullVehicleData.size()) {
            vehicleList.setAll(fullVehicleData.subList(start, end));
            recentVehiclesTable.setItems(vehicleList);
        }
    }

    private void updateNotificationTablePage() {
        if (fullNotificationData == null || fullNotificationData.isEmpty()) return;
        int start = currentNotificationPage * pageSize;
        int end = Math.min(start + pageSize, fullNotificationData.size());
        if (start < fullNotificationData.size()) {
            notificationList.setAll(fullNotificationData.subList(start, end));
            recentNotificationsTable.setItems(notificationList);
        }
    }

    private void setupButtonHandlers() {
        recentVehiclesTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Vehicle selected = recentVehiclesTable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    SceneManager.getInstance().switchToCustomerVehicleView();
                }
            }
        });

        recentNotificationsTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                SceneManager.getInstance().switchToNotificationView();
            }
        });
    }

    private void applyVisualEffects() {
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(5.0);
        dropShadow.setOffsetX(2.0);
        dropShadow.setOffsetY(2.0);
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.3));

        recentVehiclesTable.setEffect(dropShadow);
        recentNotificationsTable.setEffect(dropShadow);
    }

    private void startAutoRefresh() {
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(60),
                        e -> {
                            loadStatistics();
                            loadRecentVehicles();
                            loadRecentNotifications();
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
            loadRecentVehicles();
            loadRecentNotifications();
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
            if (customerId <= 0) {
                setDefaultStats();
                return;
            }

            int vehicleCount = vehicleDAO.countByOwnerId(customerId);
            vehiclesCountLabel.setText(String.valueOf(vehicleCount));

            int activeQueries = queryDAO.countPendingByCustomerId(customerId);
            activeQueriesLabel.setText(String.valueOf(activeQueries));

            int pendingComplaints = complaintDAO.countPendingByCustomerId(customerId);
            pendingComplaintsLabel.setText(String.valueOf(pendingComplaints));

            double walletBalance = walletDAO.getBalanceByCustomerId(customerId);
            walletBalanceLabel.setText(CurrencyUtil.format(walletBalance));

            int unreadCount = notificationDAO.countUnreadByUserId(userId);
            unreadNotificationsLabel.setText(String.valueOf(unreadCount));

            int reviewCount = reviewDAO.countByCustomerId(customerId);
            reviewsCountLabel.setText(String.valueOf(reviewCount));

            int expiredInsurance = policyDAO.countExpiredByCustomerId(customerId);
            expiredInsuranceLabel.setText(String.valueOf(expiredInsurance));

            int servicesDue = scheduleDAO.countDueByCustomerId(customerId, 30);
            servicesDueLabel.setText(String.valueOf(servicesDue));

        } catch (Exception e) {
            e.printStackTrace();
            setDefaultStats();
        }
    }

    private void setDefaultStats() {
        vehiclesCountLabel.setText("0");
        activeQueriesLabel.setText("0");
        pendingComplaintsLabel.setText("0");
        walletBalanceLabel.setText("M0.00");
        unreadNotificationsLabel.setText("0");
        reviewsCountLabel.setText("0");
        expiredInsuranceLabel.setText("0");
        servicesDueLabel.setText("0");
    }

    private void loadRecentVehicles() {
        try {
            fullVehicleData = vehicleDAO.findByOwnerId(customerId);
            int totalPages = (int) Math.ceil((double) fullVehicleData.size() / pageSize);
            if (vehiclesPagination != null) vehiclesPagination.setPageCount(Math.max(1, totalPages));
            updateVehicleTablePage();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadRecentNotifications() {
        try {
            fullNotificationData = notificationDAO.findByUserId(userId);
            int totalPages = (int) Math.ceil((double) fullNotificationData.size() / pageSize);
            if (notificationsPagination != null) notificationsPagination.setPageCount(Math.max(1, totalPages));
            updateNotificationTablePage();
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