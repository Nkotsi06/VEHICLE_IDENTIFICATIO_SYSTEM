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
import dao.InsurancePolicyDAO;
import dao.InsuranceClaimDAO;
import dao.InsuranceProviderDAO;
import dao.NotificationDAO;
import models.InsurancePolicy;
import models.InsuranceClaim;

import java.time.LocalDate;
import java.util.List;

public class InsuranceController {

    @FXML private Label welcomeLabel;
    @FXML private Label statusLabel;

    // Statistics Cards Labels
    @FXML private Label activePoliciesLabel;
    @FXML private Label pendingClaimsLabel;
    @FXML private Label expiredPoliciesLabel;
    @FXML private Label totalPremiumLabel;
    @FXML private Label expiringSoonLabel;
    @FXML private Label unreadNotificationsLabel;

    // Tables
    @FXML private TableView<InsurancePolicy> policiesTable;
    @FXML private TableView<InsuranceClaim> claimsTable;

    // Progress Indicators
    @FXML private ProgressIndicator loadProgress;
    @FXML private ProgressBar operationProgress;
    @FXML private Pagination policiesPagination;
    @FXML private Pagination claimsPagination;

    private InsurancePolicyDAO policyDAO;
    private InsuranceClaimDAO claimDAO;
    private InsuranceProviderDAO providerDAO;
    private NotificationDAO notificationDAO;
    private ObservableList<InsurancePolicy> policyList;
    private ObservableList<InsuranceClaim> claimList;
    private List<InsurancePolicy> fullPolicyData;
    private List<InsuranceClaim> fullClaimData;
    private int currentPolicyPage = 0;
    private int currentClaimPage = 0;
    private int pageSize = 10;

    @FXML
    public void initialize() {
        policyDAO = new InsurancePolicyDAO();
        claimDAO = new InsuranceClaimDAO();
        providerDAO = new InsuranceProviderDAO();
        notificationDAO = new NotificationDAO();
        policyList = FXCollections.observableArrayList();
        claimList = FXCollections.observableArrayList();

        String fullName = SessionManager.getInstance().getFullName();
        welcomeLabel.setText("Welcome, " + (fullName != null ? fullName : "Insurance Provider"));

        setupTableColumns();
        setupPagination();
        setupButtonHandlers();
        applyVisualEffects();
        loadDashboardData();

        statusLabel.setText("Ready");
        policiesTable.setItems(policyList);
        claimsTable.setItems(claimList);

        // Start auto-refresh every 60 seconds
        startAutoRefresh();
    }

    private void setupTableColumns() {
        // Policies Table
        TableColumn<InsurancePolicy, String> policyNumberCol = new TableColumn<>("Policy Number");
        policyNumberCol.setCellValueFactory(cellData -> cellData.getValue().policyNumberProperty());
        policyNumberCol.setPrefWidth(150);
        policyNumberCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<InsurancePolicy, String> vehicleCol = new TableColumn<>("Vehicle");
        vehicleCol.setCellValueFactory(cellData -> cellData.getValue().registrationNumberProperty());
        vehicleCol.setPrefWidth(150);
        vehicleCol.setStyle("-fx-alignment: CENTER-LEFT;");

        TableColumn<InsurancePolicy, String> providerCol = new TableColumn<>("Provider");
        providerCol.setCellValueFactory(cellData -> cellData.getValue().providerNameProperty());
        providerCol.setPrefWidth(150);
        providerCol.setStyle("-fx-alignment: CENTER-LEFT;");

        TableColumn<InsurancePolicy, String> startDateCol = new TableColumn<>("Start Date");
        startDateCol.setCellValueFactory(cellData -> cellData.getValue().startDateProperty().asString());
        startDateCol.setPrefWidth(100);
        startDateCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<InsurancePolicy, String> endDateCol = new TableColumn<>("End Date");
        endDateCol.setCellValueFactory(cellData -> cellData.getValue().endDateProperty().asString());
        endDateCol.setPrefWidth(100);
        endDateCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<InsurancePolicy, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        statusCol.setPrefWidth(80);
        statusCol.setStyle("-fx-alignment: CENTER;");

        policiesTable.getColumns().addAll(policyNumberCol, vehicleCol, providerCol, startDateCol, endDateCol, statusCol);

        // Claims Table
        TableColumn<InsuranceClaim, String> claimPolicyCol = new TableColumn<>("Policy Number");
        claimPolicyCol.setCellValueFactory(cellData -> cellData.getValue().policyNumberProperty());
        claimPolicyCol.setPrefWidth(150);
        claimPolicyCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<InsuranceClaim, String> claimDateCol = new TableColumn<>("Claim Date");
        claimDateCol.setCellValueFactory(cellData -> cellData.getValue().claimDateProperty().asString());
        claimDateCol.setPrefWidth(100);
        claimDateCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<InsuranceClaim, Double> claimAmountCol = new TableColumn<>("Amount");
        claimAmountCol.setCellValueFactory(cellData -> cellData.getValue().claimAmountProperty().asObject());
        claimAmountCol.setPrefWidth(100);
        claimAmountCol.setStyle("-fx-alignment: CENTER-RIGHT;");

        TableColumn<InsuranceClaim, String> claimStatusCol = new TableColumn<>("Status");
        claimStatusCol.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        claimStatusCol.setPrefWidth(100);
        claimStatusCol.setStyle("-fx-alignment: CENTER;");

        claimsTable.getColumns().addAll(claimPolicyCol, claimDateCol, claimAmountCol, claimStatusCol);
    }

    private void setupPagination() {
        if (policiesPagination != null) {
            policiesPagination.setPageCount(1);
            policiesPagination.setMaxPageIndicatorCount(5);
            policiesPagination.currentPageIndexProperty().addListener((obs, old, newPage) -> {
                currentPolicyPage = newPage.intValue();
                updatePolicyTablePage();
            });
        }

        if (claimsPagination != null) {
            claimsPagination.setPageCount(1);
            claimsPagination.setMaxPageIndicatorCount(5);
            claimsPagination.currentPageIndexProperty().addListener((obs, old, newPage) -> {
                currentClaimPage = newPage.intValue();
                updateClaimTablePage();
            });
        }
    }

    private void updatePolicyTablePage() {
        if (fullPolicyData == null || fullPolicyData.isEmpty()) return;
        int start = currentPolicyPage * pageSize;
        int end = Math.min(start + pageSize, fullPolicyData.size());
        if (start < fullPolicyData.size()) {
            policyList.setAll(fullPolicyData.subList(start, end));
        }
    }

    private void updateClaimTablePage() {
        if (fullClaimData == null || fullClaimData.isEmpty()) return;
        int start = currentClaimPage * pageSize;
        int end = Math.min(start + pageSize, fullClaimData.size());
        if (start < fullClaimData.size()) {
            claimList.setAll(fullClaimData.subList(start, end));
        }
    }

    private void setupButtonHandlers() {
        policiesTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                InsurancePolicy selected = policiesTable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    SceneManager.getInstance().switchToInsurancePolicyView();
                }
            }
        });

        claimsTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                InsuranceClaim selected = claimsTable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    SceneManager.getInstance().switchToInsuranceClaimView();
                }
            }
        });
    }

    private void applyVisualEffects() {
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(5.0);
        dropShadow.setOffsetX(2.0);
        dropShadow.setOffsetY(2.0);
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.3));

        policiesTable.setEffect(dropShadow);
        claimsTable.setEffect(dropShadow);
    }

    private void startAutoRefresh() {
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(60),
                        e -> {
                            loadStatistics();
                            loadPolicies();
                            loadClaims();
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
            loadPolicies();
            loadClaims();
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
            int providerId = getProviderId();

            if (providerId <= 0) {
                setDefaultStats();
                return;
            }

            int activeCount = policyDAO.countActiveByProviderId(providerId);
            activePoliciesLabel.setText(String.valueOf(activeCount));

            int pendingCount = claimDAO.countPendingByProviderId(providerId);
            pendingClaimsLabel.setText(String.valueOf(pendingCount));

            List<InsurancePolicy> allPolicies = policyDAO.findByProviderId(providerId);
            long expiredCount = allPolicies.stream()
                    .filter(p -> "EXPIRED".equals(p.getStatus()) ||
                            (p.getEndDate() != null && p.getEndDate().isBefore(LocalDate.now())))
                    .count();
            expiredPoliciesLabel.setText(String.valueOf(expiredCount));

            double totalPremium = policyDAO.getTotalPremiumByProvider(providerId);
            totalPremiumLabel.setText(CurrencyUtil.format(totalPremium));

            long expiringCount = allPolicies.stream()
                    .filter(p -> "ACTIVE".equals(p.getStatus()) &&
                            p.getEndDate() != null &&
                            p.getEndDate().isAfter(LocalDate.now()) &&
                            p.getEndDate().isBefore(LocalDate.now().plusDays(30)))
                    .count();
            expiringSoonLabel.setText(String.valueOf(expiringCount));

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
        activePoliciesLabel.setText("0");
        pendingClaimsLabel.setText("0");
        expiredPoliciesLabel.setText("0");
        totalPremiumLabel.setText("M0.00");
        expiringSoonLabel.setText("0");
    }

    private void loadPolicies() {
        try {
            int providerId = getProviderId();

            if (providerId > 0) {
                fullPolicyData = policyDAO.findByProviderId(providerId);
            } else {
                fullPolicyData = policyDAO.findAll();
            }

            int totalPages = (int) Math.ceil((double) fullPolicyData.size() / pageSize);
            if (policiesPagination != null) policiesPagination.setPageCount(Math.max(1, totalPages));
            updatePolicyTablePage();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadClaims() {
        try {
            int providerId = getProviderId();

            if (providerId > 0) {
                fullClaimData = claimDAO.findByProviderId(providerId);
            } else {
                fullClaimData = claimDAO.findAll();
            }

            int totalPages = (int) Math.ceil((double) fullClaimData.size() / pageSize);
            if (claimsPagination != null) claimsPagination.setPageCount(Math.max(1, totalPages));
            updateClaimTablePage();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getProviderId() {
        try {
            int userId = SessionManager.getInstance().getUserId();
            var provider = providerDAO.findByUserId(userId);
            if (provider != null) {
                return provider.getId();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
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