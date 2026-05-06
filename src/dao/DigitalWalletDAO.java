package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_digital_wallet", "wallet_id = ?", id);
        if (results.isEmpty()) return null;

        DigitalWallet wallet = mapToDigitalWallet(results.get(0));
        List<Map<String, Object>> txResults = viewLoader.loadViewWithCondition("vw_wallet_transactions", "wallet_id = ?", id);
        List<WalletTransaction> transactions = mapToWalletTransactionList(txResults);
        wallet.setTransactions(transactions);
        return wallet;
    }

    public DigitalWallet findByCustomerId(int customerId) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_digital_wallet", "customer_id = ?", customerId);
        if (results.isEmpty()) return null;

        DigitalWallet wallet = mapToDigitalWallet(results.get(0));
        if (wallet != null) {
            List<Map<String, Object>> txResults = viewLoader.loadViewWithCondition("vw_wallet_transactions", "wallet_id = ?", wallet.getId());
            List<WalletTransaction> transactions = mapToWalletTransactionList(txResults);
            wallet.setTransactions(transactions);
        }
        return wallet;
    }

    @Override
    public List<DigitalWallet> findAll() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadView("vw_digital_wallet");
        return mapToDigitalWalletList(results);
    }

    @Override
    public boolean insert(DigitalWallet entity) throws SQLException {
        return procedureCaller.executeCreateDigitalWallet(entity.getCustomerId());
    }

    public boolean addFunds(int customerId, double amount, String referenceId, String description) throws SQLException {
        return procedureCaller.executeAddWalletBalance(customerId, amount, referenceId, description);
    }

    public boolean addFunds(int customerId, double amount, String referenceId) throws SQLException {
        return addFunds(customerId, amount, referenceId, "Wallet deposit");
    }

    public boolean addBalance(int customerId, double amount, String referenceId) throws SQLException {
        return addFunds(customerId, amount, referenceId, "Wallet deposit");
    }

    public boolean deductFunds(int customerId, double amount, String referenceId, String description) throws SQLException {
        return procedureCaller.executeDeductWalletBalance(customerId, amount, referenceId, description);
    }

    public List<WalletTransaction> getTransactionHistory(int walletId, int limit) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_wallet_transactions",
                "wallet_id = ? ORDER BY created_at DESC LIMIT ?", walletId, limit);
        return mapToWalletTransactionList(results);
    }

    public List<WalletTransaction> getTransactionHistoryByDateRange(int walletId, String startDate, String endDate) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_wallet_transactions",
                "wallet_id = ? AND created_at BETWEEN ? AND ? ORDER BY created_at DESC",
                walletId, startDate, endDate);
        return mapToWalletTransactionList(results);
    }

    public double getWalletBalance(int customerId) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_digital_wallet", "customer_id = ?", customerId);
        if (results.isEmpty()) return 0.0;
        Number balance = (Number) results.get(0).get("balance");
        return balance != null ? balance.doubleValue() : 0.0;
    }

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
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_wallet_transactions",
                "customer_id = ? AND transaction_type = 'DEPOSIT'", customerId);
        double total = 0;
        for (Map<String, Object> map : results) {
            Number amount = (Number) map.get("amount");
            total += amount != null ? amount.doubleValue() : 0;
        }
        return total;
    }

    public double getTotalPayments(int customerId) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_wallet_transactions",
                "customer_id = ? AND transaction_type = 'PAYMENT'", customerId);
        double total = 0;
        for (Map<String, Object> map : results) {
            Number amount = (Number) map.get("amount");
            total += amount != null ? Math.abs(amount.doubleValue()) : 0;
        }
        return total;
    }

    // ============================================
    // HELPER METHODS FOR MAPPING
    // ============================================

    private DigitalWallet mapToDigitalWallet(Map<String, Object> map) {
        if (map == null) return null;

        DigitalWallet wallet = new DigitalWallet();

        if (map.get("wallet_id") != null) wallet.setId(((Number) map.get("wallet_id")).intValue());
        if (map.get("customer_id") != null) wallet.setCustomerId(((Number) map.get("customer_id")).intValue());
        if (map.get("customer_name") != null) wallet.setCustomerName(map.get("customer_name").toString());
        if (map.get("balance") != null) wallet.setBalance(((Number) map.get("balance")).doubleValue());

        if (map.get("created_at") instanceof Timestamp) {
            wallet.setCreatedAt(((Timestamp) map.get("created_at")).toLocalDateTime());
        }
        if (map.get("updated_at") instanceof Timestamp) {
            wallet.setUpdatedAt(((Timestamp) map.get("updated_at")).toLocalDateTime());
        }

        return wallet;
    }

    private List<DigitalWallet> mapToDigitalWalletList(List<Map<String, Object>> maps) {
        List<DigitalWallet> wallets = new ArrayList<>();
        if (maps != null) {
            for (Map<String, Object> map : maps) {
                wallets.add(mapToDigitalWallet(map));
            }
        }
        return wallets;
    }

    private WalletTransaction mapToWalletTransaction(Map<String, Object> map) {
        if (map == null) return null;

        WalletTransaction tx = new WalletTransaction();

        if (map.get("id") != null) tx.setId(((Number) map.get("id")).intValue());
        if (map.get("wallet_id") != null) tx.setWalletId(((Number) map.get("wallet_id")).intValue());
        if (map.get("amount") != null) tx.setAmount(((Number) map.get("amount")).doubleValue());
        if (map.get("transaction_type") != null) tx.setTransactionType(map.get("transaction_type").toString());
        if (map.get("reference_id") != null) tx.setReferenceId(map.get("reference_id").toString());
        if (map.get("description") != null) tx.setDescription(map.get("description").toString());
        if (map.get("status") != null) tx.setStatus(map.get("status").toString());

        if (map.get("created_at") instanceof Timestamp) {
            tx.setTransactionDate(((Timestamp) map.get("created_at")).toLocalDateTime());
            tx.setCreatedAt(((Timestamp) map.get("created_at")).toLocalDateTime());
        }
        if (map.get("updated_at") instanceof Timestamp) {
            tx.setUpdatedAt(((Timestamp) map.get("updated_at")).toLocalDateTime());
        }

        return tx;
    }

    private List<WalletTransaction> mapToWalletTransactionList(List<Map<String, Object>> maps) {
        List<WalletTransaction> transactions = new ArrayList<>();
        if (maps != null) {
            for (Map<String, Object> map : maps) {
                transactions.add(mapToWalletTransaction(map));
            }
        }
        return transactions;
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