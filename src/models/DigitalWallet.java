package models;

import java.util.ArrayList;
import java.util.List;

public class DigitalWallet extends BaseEntity {
    private int customerId;
    private String customerName;
    private double balance;
    private List<WalletTransaction> transactions;

    public DigitalWallet() {
        super();
        this.balance = 0.0;
        this.transactions = new ArrayList<>();
    }

    public DigitalWallet(int customerId, double balance) {
        this();
        this.customerId = customerId;
        this.balance = balance;
    }

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

    public void addTransaction(WalletTransaction transaction) {
        this.transactions.add(transaction);
    }

    public boolean canPay(double amount) {
        return balance >= amount;
    }

    @Override
    public String toString() {
        return "Wallet - Balance: $" + balance;
    }
}