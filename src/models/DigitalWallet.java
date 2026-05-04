package models;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * DigitalWallet model representing customer digital wallet for payments.
 * Tracks balance and transaction history.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class DigitalWallet extends BaseEntity {

    // Core fields
    private int id;
    private int customerId;
    private String customerName;
    private double balance;
    private List<WalletTransaction> transactions;

    // Default initial balance
    private static final double DEFAULT_BALANCE = 0.0;

    /**
     * Default constructor - initializes empty transaction list.
     */
    public DigitalWallet() {
        super();
        this.balance = DEFAULT_BALANCE;
        this.transactions = new ArrayList<>();
    }

    /**
     * Constructor for creating a wallet for a customer.
     *
     * @param customerId the customer ID
     * @param balance    initial balance
     */
    public DigitalWallet(int customerId, double balance) {
        this();
        this.customerId = customerId;
        this.balance = balance;
    }

    // ============================================
    // GETTERS AND SETTERS
    // ============================================

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public List<WalletTransaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<WalletTransaction> transactions) {
        this.transactions = transactions;
    }

    // ============================================
    // BUSINESS LOGIC METHODS
    // ============================================

    /**
     * Adds a transaction to the wallet.
     *
     * @param transaction the transaction to add
     */
    public void addTransaction(WalletTransaction transaction) {
        if (transaction != null) {
            this.transactions.add(transaction);
        }
    }

    /**
     * Checks if the wallet has sufficient funds.
     *
     * @param amount the amount to check
     * @return true if balance >= amount, false otherwise
     */
    public boolean canPay(double amount) {
        return balance >= amount;
    }

    /**
     * Gets the formatted balance string.
     *
     * @return formatted balance (e.g., "M1,234.56")
     */
    public String getFormattedBalance() {
        return String.format("M%,.2f", balance);
    }

    /**
     * Gets the total amount deposited.
     *
     * @return total deposits amount
     */
    public double getTotalDeposits() {
        return transactions.stream()
                .filter(t -> t != null && "DEPOSIT".equals(t.getType()))
                .mapToDouble(WalletTransaction::getAmount)
                .sum();
    }

    /**
     * Gets the total amount withdrawn.
     *
     * @return total withdrawals amount
     */
    public double getTotalWithdrawals() {
        return transactions.stream()
                .filter(t -> t != null && "WITHDRAWAL".equals(t.getType()))
                .mapToDouble(WalletTransaction::getAmount)
                .sum();
    }

    /**
     * Gets the total amount paid for services.
     *
     * @return total payments amount
     */
    public double getTotalPayments() {
        return transactions.stream()
                .filter(t -> t != null && "PAYMENT".equals(t.getType()))
                .mapToDouble(WalletTransaction::getAmount)
                .sum();
    }

    /**
     * Gets the number of transactions.
     *
     * @return transaction count
     */
    public int getTransactionCount() {
        return transactions.size();
    }

    /**
     * Gets the formatted balance without symbol.
     *
     * @return formatted balance string
     */
    public String getFormattedBalanceNoSymbol() {
        return String.format("%,.2f", balance);
    }

    /**
     * Gets the formatted creation date.
     *
     * @return formatted date string
     */
    public String getFormattedCreatedDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return createdAt != null ? createdAt.format(formatter) : "";
    }

    /**
     * Gets the last transaction date.
     *
     * @return last transaction date, or null if no transactions
     */
    public LocalDateTime getLastTransactionDate() {
        if (transactions.isEmpty()) return null;
        return transactions.stream()
                .map(WalletTransaction::getTransactionDate)
                .max(LocalDateTime::compareTo)
                .orElse(null);
    }

    /**
     * Gets the formatted last transaction date.
     *
     * @return formatted date string
     */
    public String getFormattedLastTransactionDate() {
        LocalDateTime lastDate = getLastTransactionDate();
        if (lastDate == null) return "No transactions";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return lastDate.format(formatter);
    }

    /**
     * Adds funds to the wallet.
     *
     * @param amount the amount to add
     * @param source the source of funds (e.g., "Bank Transfer", "Cash")
     * @return true if successful, false otherwise
     */
    public boolean addFunds(double amount, String source) {
        if (amount <= 0) return false;

        this.balance += amount;
        WalletTransaction transaction = new WalletTransaction(customerId, "DEPOSIT", amount, source);
        transaction.setWalletId(this.id);
        transactions.add(transaction);

        return true;
    }

    /**
     * Makes a payment from the wallet.
     *
     * @param amount     the amount to pay
     * @param recipient  the payment recipient
     * @param reference  payment reference
     * @return true if successful, false otherwise
     */
    public boolean makePayment(double amount, String recipient, String reference) {
        if (amount <= 0 || !canPay(amount)) return false;

        this.balance -= amount;
        WalletTransaction transaction = new WalletTransaction(customerId, "PAYMENT", amount, recipient);
        transaction.setReference(reference);
        transaction.setWalletId(this.id);
        transactions.add(transaction);

        return true;
    }

    /**
     * Withdraws funds from the wallet.
     *
     * @param amount the amount to withdraw
     * @return true if successful, false otherwise
     */
    public boolean withdraw(double amount) {
        if (amount <= 0 || !canPay(amount)) return false;

        this.balance -= amount;
        WalletTransaction transaction = new WalletTransaction(customerId, "WITHDRAWAL", amount, "Cash Withdrawal");
        transaction.setWalletId(this.id);
        transactions.add(transaction);

        return true;
    }

    // ============================================
    // OVERRIDE METHODS
    // ============================================

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Wallet - Balance: " + getFormattedBalance();
    }

    /**
     * Creates a copy of this wallet (without transactions).
     *
     * @return a new DigitalWallet instance
     */
    public DigitalWallet copy() {
        DigitalWallet copy = new DigitalWallet();
        copy.setId(this.id);
        copy.setCustomerId(this.customerId);
        copy.setCustomerName(this.customerName);
        copy.setBalance(this.balance);
        // Note: Doesn't copy transactions - use separate method if needed
        copy.setCreatedAt(this.getCreatedAt());
        copy.setUpdatedAt(this.getUpdatedAt());
        return copy;
    }
}