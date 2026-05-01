package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import models.WalletTransaction;

public class WalletTransactionDAO extends BaseDAO<WalletTransaction> {

    @Override
    public WalletTransaction findById(int id) throws SQLException {
        String sql = "SELECT * FROM wallet_transactions WHERE id = ?";
        return executeQuerySingle(sql, id);
    }

    @Override
    public List<WalletTransaction> findAll() throws SQLException {
        String sql = "SELECT * FROM wallet_transactions ORDER BY created_at DESC";
        return executeQuery(sql);
    }

    public List<WalletTransaction> findByWalletId(int walletId) throws SQLException {
        String sql = "SELECT * FROM wallet_transactions WHERE wallet_id = ? ORDER BY created_at DESC";
        return executeQuery(sql, walletId);
    }

    public List<WalletTransaction> findByCustomerId(int customerId) throws SQLException {
        String sql = "SELECT wt.* FROM wallet_transactions wt " +
                "JOIN digital_wallets dw ON wt.wallet_id = dw.id " +
                "WHERE dw.customer_id = ? ORDER BY wt.created_at DESC";
        return executeQuery(sql, customerId);
    }

    public List<WalletTransaction> findByTransactionType(String transactionType) throws SQLException {
        String sql = "SELECT * FROM wallet_transactions WHERE transaction_type = ? ORDER BY created_at DESC";
        return executeQuery(sql, transactionType);
    }

    public List<WalletTransaction> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) throws SQLException {
        String sql = "SELECT * FROM wallet_transactions WHERE created_at BETWEEN ? AND ? ORDER BY created_at DESC";
        return executeQuery(sql, startDate, endDate);
    }

    public List<WalletTransaction> findPendingTransactions() throws SQLException {
        String sql = "SELECT * FROM wallet_transactions WHERE status = 'PENDING' ORDER BY created_at";
        return executeQuery(sql);
    }

    @Override
    public boolean insert(WalletTransaction entity) throws SQLException {
        String sql = "INSERT INTO wallet_transactions (wallet_id, amount, transaction_type, reference_id, description, status) VALUES (?, ?, ?, ?, ?, ?)";
        int result = executeUpdate(sql,
                entity.getWalletId(),
                entity.getAmount(),
                entity.getTransactionType(),
                entity.getReferenceId(),
                entity.getDescription(),
                entity.getStatus()
        );
        return result > 0;
    }

    public boolean markAsCompleted(int transactionId) throws SQLException {
        String sql = "UPDATE wallet_transactions SET status = 'COMPLETED' WHERE id = ?";
        int result = executeUpdate(sql, transactionId);
        return result > 0;
    }

    public boolean markAsFailed(int transactionId) throws SQLException {
        String sql = "UPDATE wallet_transactions SET status = 'FAILED' WHERE id = ?";
        int result = executeUpdate(sql, transactionId);
        return result > 0;
    }

    @Override
    public boolean update(WalletTransaction entity) throws SQLException {
        String sql = "UPDATE wallet_transactions SET status = ? WHERE id = ?";
        int result = executeUpdate(sql, entity.getStatus(), entity.getId());
        return result > 0;
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM wallet_transactions WHERE id = ?";
        int result = executeUpdate(sql, id);
        return result > 0;
    }

    public double getTotalByWalletAndType(int walletId, String transactionType) throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM wallet_transactions WHERE wallet_id = ? AND transaction_type = ? AND status = 'COMPLETED'";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, walletId);
            ps.setString(2, transactionType);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
            return 0;
        } finally {
            closeResources(rs, ps, conn);
        }
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