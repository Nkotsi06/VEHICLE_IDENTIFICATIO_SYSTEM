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
import utils.CurrencyUtil;
import dao.NoClaimBonusRecordDAO;
import dao.InsurancePolicyDAO;
import models.NoClaimBonusRecord;
import models.InsurancePolicy;

import java.util.List;

public class NoClaimBonusController {

    @FXML private TableView<NoClaimBonusRecord> bonusTable;
    @FXML private TableColumn<NoClaimBonusRecord, String> policyColumn;
    @FXML private TableColumn<NoClaimBonusRecord, Integer> yearColumn;
    @FXML private TableColumn<NoClaimBonusRecord, Integer> claimFreeYearsColumn;
    @FXML private TableColumn<NoClaimBonusRecord, String> bonusPercentageColumn;
    @FXML private TableColumn<NoClaimBonusRecord, String> calculatedDateColumn;
    @FXML private TableColumn<NoClaimBonusRecord, String> savedAmountColumn;  // ADDED - missing from FXML

    @FXML private ComboBox<InsurancePolicy> policyComboBox;
    @FXML private Label currentBonusLabel;
    @FXML private Label savingsLabel;
    @FXML private Label nextYearBonusLabel;
    @FXML private Label currentPremiumLabel;
    @FXML private Label discountedPremiumLabel;
    @FXML private Label claimFreeYearsLabel;  // ADDED - missing from FXML
    @FXML private Label statusLabel;

    @FXML private Button calculateButton;
    @FXML private Button refreshButton;
    @FXML private Button backButton;
    @FXML private Button fadeButton;

    @FXML private ProgressIndicator loadProgress;
    @FXML private ProgressBar operationProgress;
    @FXML private Pagination bonusPagination;

    private NoClaimBonusRecordDAO bonusDAO;
    private InsurancePolicyDAO policyDAO;
    private ObservableList<NoClaimBonusRecord> bonusList;
    private int currentPage = 0;
    private int pageSize = 10;
    private List<NoClaimBonusRecord> fullData;

    @FXML
    public void initialize() {
        bonusDAO = new NoClaimBonusRecordDAO();
        policyDAO = new InsurancePolicyDAO();
        bonusList = FXCollections.observableArrayList();

        setupTableColumns();
        loadPolicies();
        loadBonusRecords();
        setupButtonHandlers();
        setupPagination();
        applyVisualEffects();
        statusLabel.setText("Ready");
    }

    private void setupTableColumns() {
        policyColumn.setCellValueFactory(cellData -> cellData.getValue().policyNumberProperty());
        yearColumn.setCellValueFactory(cellData -> cellData.getValue().policyYearProperty().asObject());
        claimFreeYearsColumn.setCellValueFactory(cellData -> cellData.getValue().claimFreeYearsProperty().asObject());
        // FIXED: Convert DoubleProperty to String using .asString()
        bonusPercentageColumn.setCellValueFactory(cellData -> cellData.getValue().bonusPercentageProperty().asString());
        calculatedDateColumn.setCellValueFactory(cellData -> cellData.getValue().calculatedDateProperty().asString());

        // Setup saved amount column if it exists
        if (savedAmountColumn != null) {
            savedAmountColumn.setCellValueFactory(cellData -> {
                double basePremium = cellData.getValue().getBasePremium();
                double bonusPercent = cellData.getValue().getBonusPercentage();
                double savings = basePremium * (bonusPercent / 100);
                return new javafx.beans.property.SimpleStringProperty(CurrencyUtil.format(savings));
            });
        }

        policyColumn.setStyle("-fx-alignment: CENTER;");
        yearColumn.setStyle("-fx-alignment: CENTER;");
        claimFreeYearsColumn.setStyle("-fx-alignment: CENTER;");
        bonusPercentageColumn.setStyle("-fx-alignment: CENTER;");
        calculatedDateColumn.setStyle("-fx-alignment: CENTER;");
        if (savedAmountColumn != null) savedAmountColumn.setStyle("-fx-alignment: CENTER;");
    }

    private void setupPagination() {
        if (bonusPagination != null) {
            bonusPagination.setPageCount(1);
            bonusPagination.setMaxPageIndicatorCount(5);
            bonusPagination.currentPageIndexProperty().addListener((obs, old, newPage) -> {
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
            bonusList.setAll(fullData.subList(start, end));
            bonusTable.setItems(bonusList);
        }
    }

    private void loadPolicies() {
        showProgress(true);
        try {
            int customerId = utils.SessionManager.getInstance().getCustomerId();
            if (customerId > 0) {
                List<InsurancePolicy> policies = policyDAO.findByCustomerId(customerId);
                policyComboBox.getItems().setAll(policies);
                statusLabel.setText("Loaded " + policies.size() + " policies");
            } else {
                List<InsurancePolicy> policies = policyDAO.findActivePolicies();
                policyComboBox.getItems().setAll(policies);
                statusLabel.setText("Loaded " + policies.size() + " policies");
            }
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error loading policies");
            AlertUtil.showError("Load Failed", "Failed to load policies.");
        } finally {
            showProgress(false);
        }
    }

    private void loadBonusRecords() {
        showProgress(true);
        statusLabel.setText("Loading NCB records...");

        try {
            List<NoClaimBonusRecord> records = bonusDAO.findAll();
            fullData = records;
            int totalPages = (int) Math.ceil((double) records.size() / pageSize);
            if (bonusPagination != null) bonusPagination.setPageCount(Math.max(1, totalPages));
            updateTablePage();
            statusLabel.setText("Loaded " + records.size() + " NCB records");
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error loading NCB records");
            AlertUtil.showError("Load Failed", "Failed to load NCB records.");
        } finally {
            showProgress(false);
        }
    }

    private void setupButtonHandlers() {
        calculateButton.setOnAction(event -> handleCalculate());
        if (refreshButton != null) refreshButton.setOnAction(event -> {
            loadBonusRecords();
            if (policyComboBox.getValue() != null) handleCalculate();
        });
        backButton.setOnAction(event -> SceneManager.getInstance().switchToInsurancePolicyView());
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

        calculateButton.setEffect(dropShadow);
        if (refreshButton != null) refreshButton.setEffect(dropShadow);
        backButton.setEffect(dropShadow);
        if (fadeButton != null) fadeButton.setEffect(dropShadow);
    }

    private void handleCalculate() {
        InsurancePolicy selectedPolicy = policyComboBox.getSelectionModel().getSelectedItem();

        if (selectedPolicy == null) {
            AlertUtil.showWarning("Validation Error", "Please select a policy.");
            return;
        }

        showProgress(true);
        statusLabel.setText("Calculating NCB...");
        updateProgress(0.3);

        try {
            updateProgress(0.5);
            bonusDAO.calculateBonus(selectedPolicy.getId());

            // Wait a moment for the calculation to complete
            Thread.sleep(500);

            NoClaimBonusRecord record = bonusDAO.findByPolicyId(selectedPolicy.getId());

            updateProgress(0.8);

            if (record != null) {
                double bonusPercent = record.getBonusPercentage();
                currentBonusLabel.setText(String.format("%.0f%%", bonusPercent));

                double basePremium = selectedPolicy.getPremium();
                double discountedPremium = basePremium * (1 - bonusPercent / 100);
                double savings = basePremium - discountedPremium;

                currentPremiumLabel.setText(CurrencyUtil.format(basePremium));
                discountedPremiumLabel.setText(CurrencyUtil.format(discountedPremium));
                savingsLabel.setText(CurrencyUtil.format(savings));

                // Update claim free years label
                if (claimFreeYearsLabel != null) {
                    claimFreeYearsLabel.setText(record.getClaimFreeYears() + " years");
                }

                int nextYearBonus = (int) Math.min(bonusPercent + 10, 50);
                nextYearBonusLabel.setText(nextYearBonus + "% (if claim-free)");

                updateProgress(1.0);
                AlertUtil.showSuccess("No Claim Bonus calculated successfully.");
                statusLabel.setText("NCB: " + String.format("%.0f%%", bonusPercent));

                loadBonusRecords();
            } else {
                statusLabel.setText("No NCB record found");
                AlertUtil.showWarning("No Data", "No NCB record found for this policy.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error: " + e.getMessage());
            AlertUtil.showError("Calculation Failed", "Failed to calculate No Claim Bonus.");
        } finally {
            hideProgressAfterDelay();
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