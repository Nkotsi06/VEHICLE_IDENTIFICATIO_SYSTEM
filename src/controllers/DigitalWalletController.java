package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import utils.AlertUtil;
import utils.SceneManager;
import utils.SessionManager;
import dao.DigitalWalletDAO;
import dao.WalletTransactionDAO;
import models.DigitalWallet;
import models.WalletTransaction;

public class DigitalWalletController {

    @FXML private Label balanceLabel;
    @FXML private Label customerNameLabel;
    @FXML private TextField amountField;
    @FXML private Button addFundsButton;
    @FXML private Button refreshButton;
    @FXML private Button backButton;

    @FXML private TableView<WalletTransaction> transactionsTable;
    @FXML private TableColumn<WalletTransaction, String> dateColumn;
    @FXML private TableColumn<WalletTransaction, Double> amountColumn;
    @FXML private TableColumn<WalletTransaction, String> typeColumn;
    @FXML private TableColumn<WalletTransaction, String> descriptionColumn;
    @FXML private TableColumn<WalletTransaction, String> statusColumn;

    private DigitalWalletDAO walletDAO;
    private WalletTransactionDAO transactionDAO;
    private DigitalWallet currentWallet;
    private int customerId;

    @FXML
    public void initialize() {
        walletDAO = new DigitalWalletDAO();
        transactionDAO = new WalletTransactionDAO();

        customerId = SessionManager.getInstance().getCustomerId();

        setupTableColumns();
        loadWallet();
        loadTransactions();
        setupButtonHandlers();
    }

    private void setupTableColumns() {
        dateColumn.setCellValueFactory(cellData -> cellData.getValue().transactionDateProperty().asString());
        amountColumn.setCellValueFactory(cellData -> cellData.getValue().amountProperty().asObject());
        typeColumn.setCellValueFactory(cellData -> cellData.getValue().transactionTypeProperty());
        descriptionColumn.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
    }

    private void loadWallet() {
        try {
            currentWallet = walletDAO.findByCustomerId(customerId);

            if (currentWallet == null) {
                walletDAO.insert(new DigitalWallet(customerId, 0));
                currentWallet = walletDAO.findByCustomerId(customerId);
            }

            if (currentWallet != null) {
                balanceLabel.setText(utils.CurrencyUtil.format(currentWallet.getBalance()));
                customerNameLabel.setText(SessionManager.getInstance().getFullName());
            }

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Load Failed", "Failed to load wallet information.");
        }
    }

    private void loadTransactions() {
        try {
            if (currentWallet != null) {
                java.util.List<WalletTransaction> transactions = transactionDAO.findByWalletId(currentWallet.getId());
                transactionsTable.getItems().setAll(transactions);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupButtonHandlers() {
        addFundsButton.setOnAction(event -> handleAddFunds());
        refreshButton.setOnAction(event -> {
            loadWallet();
            loadTransactions();
        });
        backButton.setOnAction(event -> SceneManager.getInstance().switchToCustomerProfileView());
    }

    private void handleAddFunds() {
        String amountStr = amountField.getText().trim();

        if (!utils.ValidationUtil.isNotEmpty(amountStr)) {
            AlertUtil.showWarning("Validation Error", "Please enter an amount.");
            amountField.requestFocus();
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);

            if (amount <= 0) {
                AlertUtil.showWarning("Validation Error", "Amount must be greater than 0.");
                amountField.requestFocus();
                return;
            }

            if (amount > 50000) {
                AlertUtil.showWarning("Limit Exceeded", "Maximum deposit is 50,000 per transaction.");
                amountField.requestFocus();
                return;
            }

            boolean confirmed = AlertUtil.showConfirmation("Add Funds",
                    "Add " + utils.CurrencyUtil.format(amount) + " to your wallet?");

            if (confirmed) {
                String referenceId = "DEP_" + System.currentTimeMillis();
                boolean success = walletDAO.addBalance(customerId, amount, referenceId);

                if (success) {
                    AlertUtil.showSuccess(utils.CurrencyUtil.format(amount) + " added to your wallet.");
                    amountField.clear();
                    loadWallet();
                    loadTransactions();
                } else {
                    AlertUtil.showError("Transaction Failed", "Failed to add funds to wallet.");
                }
            }

        } catch (NumberFormatException e) {
            AlertUtil.showError("Invalid Amount", "Please enter a valid amount.");
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Transaction Error", "An error occurred during the transaction.");
        }
    }
}