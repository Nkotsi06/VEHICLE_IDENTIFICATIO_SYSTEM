package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import database.ProcedureCaller;
import database.ViewLoader;
import models.DigitalWallet;
import models.WalletTransaction;

/**
 * DigitalWalletDAO - Uses ONLY stored procedures and views for all operations.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class DigitalWalletDAO extends BaseDAO<DigitalWallet> {

    private final ProcedureCaller procedureCaller;
    private final ViewLoader viewLoader;

    public DigitalWalletDAO() {
        this.procedureCaller = new ProcedureCaller();
        this.viewLoader = new ViewLoader();
    }

    @Override
    public DigitalWallet findById(int id) throws SQLException {
        List<DigitalWallet> results = viewLoader.loadViewWithCondition("vw_digital_wallet", "wallet_id = ?", id);
        if (results.isEmpty()) return null;

        DigitalWallet wallet = results.get(0);
        List<WalletTransaction> transactions = viewLoader.loadViewWithCondition("vw_wallet_transactions", "wallet_id = ?", id);
        wallet.setTransactions(transactions);
        return wallet;
    }

    public DigitalWallet findByCustomerId(int customerId) throws SQLException {
        Map<String, Object> result = viewLoader.loadViewSingle("vw_digital_wallet", "customer_id = ?", customerId);
        if (result == null) return null;

        DigitalWallet wallet = new DigitalWallet();
        wallet.setId((Integer) result.get("wallet_id"));
        wallet.setCustomerId((Integer) result.get("customer_id"));
        wallet.setCustomerName((String) result.get("customer_name"));
        wallet.setBalance((Double) result.get("balance"));

        if (result.get("created_at") != null) {
            wallet.setCreatedAt(((java.sql.Timestamp) result.get("created_at")).toLocalDateTime());
        }
        if (result.get("updated_at") != null) {
            wallet.setUpdatedAt(((java.sql.Timestamp) result.get("updated_at")).toLocalDateTime());
        }

        List<WalletTransaction> transactions = viewLoader.loadViewWithCondition("vw_wallet_transactions", "wallet_id = ?", wallet.getId());
        wallet.setTransactions(transactions);
        return wallet;
    }

    @Override
    public List<DigitalWallet> findAll() throws SQLException {
        return viewLoader.loadView("vw_digital_wallet");
    }

    @Override
    public boolean insert(DigitalWallet entity) throws SQLException {
        return procedureCaller.executeCreateDigitalWallet(entity.getCustomerId());
    }

    public boolean addFunds(int customerId, double amount, String referenceId, String description) throws SQLException {
        return procedureCaller.executeAddWalletBalance(customerId, amount, referenceId, description);
    }

    /**
     * Convenience method for adding funds without description.
     * Calls addFunds with default description.
     */
    public boolean addFunds(int customerId, double amount, String referenceId) throws SQLException {
        return addFunds(customerId, amount, referenceId, "Wallet deposit");
    }

    /**
     * Alias method for backward compatibility with controllers expecting addBalance.
     */
    public boolean addBalance(int customerId, double amount, String referenceId) throws SQLException {
        return addFunds(customerId, amount, referenceId, "Wallet deposit");
    }

    public boolean deductFunds(int customerId, double amount, String referenceId, String description) throws SQLException {
        return procedureCaller.executeDeductWalletBalance(customerId, amount, referenceId, description);
    }

    public List<WalletTransaction> getTransactionHistory(int walletId, int limit) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_wallet_transactions", "wallet_id = ? ORDER BY created_at DESC LIMIT ?", walletId, limit);
    }

    public List<WalletTransaction> getTransactionHistoryByDateRange(int walletId, String startDate, String endDate) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_wallet_transactions",
                "wallet_id = ? AND created_at BETWEEN ? AND ? ORDER BY created_at DESC",
                walletId, startDate, endDate);
    }

    public double getWalletBalance(int customerId) throws SQLException {
        Map<String, Object> result = viewLoader.loadViewSingle("vw_digital_wallet", "customer_id = ?", customerId);
        if (result == null) return 0.0;
        return (Double) result.get("balance");
    }

    /**
     * Alias method for getWalletBalance for compatibility with CustomerController.
     */
    public double getBalanceByCustomerId(int customerId) throws SQLException {
        return getWalletBalance(customerId);
    }

    @Override
    public boolean update(DigitalWallet entity) throws SQLException {
        return false;
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return procedureCaller.executeDeleteDigitalWallet(id);
    }

    public double getTotalDeposits(int customerId) throws SQLException {
        List<WalletTransaction> transactions = viewLoader.loadViewWithCondition("vw_wallet_transactions",
                "customer_id = ? AND transaction_type = 'DEPOSIT'", customerId);
        double total = 0;
        for (WalletTransaction tx : transactions) {
            total += tx.getAmount();
        }
        return total;
    }

    public double getTotalPayments(int customerId) throws SQLException {
        List<WalletTransaction> transactions = viewLoader.loadViewWithCondition("vw_wallet_transactions",
                "customer_id = ? AND transaction_type = 'PAYMENT'", customerId);
        double total = 0;
        for (WalletTransaction tx : transactions) {
            total += Math.abs(tx.getAmount());
        }
        return total;
    }

    @Override
    protected DigitalWallet mapRow(ResultSet rs) throws SQLException {
        DigitalWallet wallet = new DigitalWallet();
        wallet.setId(rs.getInt("wallet_id"));
        wallet.setCustomerId(rs.getInt("customer_id"));
        wallet.setCustomerName(rs.getString("customer_name"));
        wallet.setBalance(rs.getDouble("balance"));

        if (rs.getTimestamp("created_at") != null) {
            wallet.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            wallet.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        return wallet;
    }
}