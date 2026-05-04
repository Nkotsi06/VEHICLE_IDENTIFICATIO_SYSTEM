package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

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
        List<WalletTransaction> results = viewLoader.loadViewWithCondition("vw_wallet_transactions", "id = ?", id);
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public List<WalletTransaction> findAll() throws SQLException {
        return viewLoader.loadView("vw_wallet_transactions");
    }

    public List<WalletTransaction> findByWalletId(int walletId) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_wallet_transactions", "wallet_id = ? ORDER BY created_at DESC", walletId);
    }

    public List<WalletTransaction> findByCustomerId(int customerId) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_wallet_transactions", "customer_id = ? ORDER BY created_at DESC", customerId);
    }

    public List<WalletTransaction> findByTransactionType(String transactionType) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_wallet_transactions", "transaction_type = ? ORDER BY created_at DESC", transactionType);
    }

    public List<WalletTransaction> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_wallet_transactions", "created_at BETWEEN ? AND ? ORDER BY created_at DESC", startDate, endDate);
    }

    public List<WalletTransaction> findPendingTransactions() throws SQLException {
        return viewLoader.loadViewWithCondition("vw_wallet_transactions", "status = 'PENDING' ORDER BY created_at");
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