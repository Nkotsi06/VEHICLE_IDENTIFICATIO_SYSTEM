package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import database.ProcedureCaller;
import database.ViewLoader;
import models.WalletTransaction;

/**
 * WalletTransactionDAO - Uses ONLY stored procedures and views for all operations.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class WalletTransactionDAO extends BaseDAO<WalletTransaction> {

    private final ProcedureCaller procedureCaller;
    private final ViewLoader viewLoader;

    public WalletTransactionDAO() {
        this.procedureCaller = new ProcedureCaller();
        this.viewLoader = new ViewLoader();
    }

    @Override
    public WalletTransaction findById(int id) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_wallet_transactions", "id = ?", id);
        if (results.isEmpty()) {
            return null;
        }
        return mapMapToWalletTransaction(results.get(0));
    }

    @Override
    public List<WalletTransaction> findAll() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadView("vw_wallet_transactions");
        return mapMapsToWalletTransactions(results);
    }

    public List<WalletTransaction> findByWalletId(int walletId) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_wallet_transactions", "wallet_id = ? ORDER BY created_at DESC", walletId);
        return mapMapsToWalletTransactions(results);
    }

    public List<WalletTransaction> findByCustomerId(int customerId) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_wallet_transactions", "customer_id = ? ORDER BY created_at DESC", customerId);
        return mapMapsToWalletTransactions(results);
    }

    public List<WalletTransaction> findByTransactionType(String transactionType) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_wallet_transactions", "transaction_type = ? ORDER BY created_at DESC", transactionType);
        return mapMapsToWalletTransactions(results);
    }

    public List<WalletTransaction> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_wallet_transactions", "created_at BETWEEN ? AND ? ORDER BY created_at DESC", startDate, endDate);
        return mapMapsToWalletTransactions(results);
    }

    public List<WalletTransaction> findPendingTransactions() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_wallet_transactions", "status = 'PENDING' ORDER BY created_at");
        return mapMapsToWalletTransactions(results);
    }

    @Override
    public boolean insert(WalletTransaction entity) throws SQLException {
        return procedureCaller.executeInsertWalletTransaction(
                entity.getWalletId(),
                entity.getAmount(),
                entity.getTransactionType(),
                entity.getReferenceId(),
                entity.getDescription(),
                entity.getStatus()
        );
    }

    public boolean markAsCompleted(int transactionId) throws SQLException {
        return procedureCaller.executeMarkWalletTransactionCompleted(transactionId);
    }

    public boolean markAsFailed(int transactionId) throws SQLException {
        return procedureCaller.executeMarkWalletTransactionFailed(transactionId);
    }

    @Override
    public boolean update(WalletTransaction entity) throws SQLException {
        if ("COMPLETED".equals(entity.getStatus())) {
            return markAsCompleted(entity.getId());
        } else if ("FAILED".equals(entity.getStatus())) {
            return markAsFailed(entity.getId());
        }
        return false;
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return procedureCaller.executeDeleteWalletTransaction(id);
    }

    public double getTotalByWalletAndType(int walletId, String transactionType) throws SQLException {
        return viewLoader.getSumWalletTransactionsByType(walletId, transactionType);
    }

    /**
     * Converts a List of Maps to a List of WalletTransaction objects.
     */
    private List<WalletTransaction> mapMapsToWalletTransactions(List<Map<String, Object>> maps) {
        List<WalletTransaction> transactions = new ArrayList<>();
        if (maps == null) {
            return transactions;
        }
        for (Map<String, Object> map : maps) {
            WalletTransaction transaction = mapMapToWalletTransaction(map);
            if (transaction != null) {
                transactions.add(transaction);
            }
        }
        return transactions;
    }

    /**
     * Converts a Map to a WalletTransaction object.
     */
    private WalletTransaction mapMapToWalletTransaction(Map<String, Object> map) {
        if (map == null) {
            return null;
        }

        WalletTransaction transaction = new WalletTransaction();

        transaction.setId(getIntValue(map, "id"));
        transaction.setWalletId(getIntValue(map, "wallet_id"));
        transaction.setAmount(getDoubleValue(map, "amount"));
        transaction.setTransactionType(getStringValue(map, "transaction_type"));
        transaction.setReferenceId(getStringValue(map, "reference_id"));
        transaction.setDescription(getStringValue(map, "description"));
        transaction.setStatus(getStringValue(map, "status"));

        LocalDateTime created = getLocalDateTimeValue(map, "created_at");
        transaction.setTransactionDate(created);
        transaction.setCreatedAt(created);
        transaction.setUpdatedAt(getLocalDateTimeValue(map, "updated_at"));

        return transaction;
    }

    private Integer getIntValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return 0;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Long) return ((Long) value).intValue();
        if (value instanceof Number) return ((Number) value).intValue();
        return 0;
    }

    private Double getDoubleValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return 0.0;
        if (value instanceof Double) return (Double) value;
        if (value instanceof Integer) return ((Integer) value).doubleValue();
        if (value instanceof Long) return ((Long) value).doubleValue();
        if (value instanceof Number) return ((Number) value).doubleValue();
        return 0.0;
    }

    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "";
    }

    private LocalDateTime getLocalDateTimeValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof java.sql.Timestamp) return ((java.sql.Timestamp) value).toLocalDateTime();
        if (value instanceof LocalDateTime) return (LocalDateTime) value;
        return null;
    }

    @Override
    protected WalletTransaction mapRow(ResultSet rs) throws SQLException {
        WalletTransaction transaction = new WalletTransaction();
        transaction.setId(rs.getInt("id"));
        transaction.setWalletId(rs.getInt("wallet_id"));
        transaction.setAmount(rs.getDouble("amount"));
        transaction.setTransactionType(rs.getString("transaction_type"));
        transaction.setReferenceId(rs.getString("reference_id"));
        transaction.setDescription(rs.getString("description"));
        transaction.setStatus(rs.getString("status"));

        if (rs.getTimestamp("created_at") != null) {
            transaction.setTransactionDate(rs.getTimestamp("created_at").toLocalDateTime());
            transaction.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            transaction.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        return transaction;
    }
}